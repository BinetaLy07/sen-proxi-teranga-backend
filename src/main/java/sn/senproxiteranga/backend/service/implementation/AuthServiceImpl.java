package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.TypeZone;
import sn.senproxiteranga.backend.dto.CreationUtilisateurRequest;
import sn.senproxiteranga.backend.dto.InscriptionClientRequest;
import sn.senproxiteranga.backend.dto.InscriptionProfessionnelRequest;
import sn.senproxiteranga.backend.dto.RegisterRequest;
import sn.senproxiteranga.backend.dto.UtilisateurResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.UtilisateurMapper;
import sn.senproxiteranga.backend.repository.AuthSessionRepository;
import sn.senproxiteranga.backend.repository.RoleRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.repository.ZoneRepository;
import sn.senproxiteranga.backend.service.AuthService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final ZoneRepository zoneRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthSessionRepository authSessionRepository;

    // Administration : la liste des comptes d'un rôle, les plus anciens d'abord
    // (même requête que pour la liste des professionnels de l'admin)
    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerUtilisateurs(NomRole role) {
        if (role == null) {
            throw new BusinessException("Le rôle est obligatoire : CLIENT ou PROFESSIONNEL");
        }
        return utilisateurRepository.findByRoleNomOrderByCreatedAtAsc(role).stream()
                .map(utilisateurMapper::toResponse)
                .toList();
    }

    @Override
    public UtilisateurResponse changerStatutCompte(Long id, StatutCompte statutCompte) {
        if (statutCompte == null) {
            throw new BusinessException("Le statut du compte est obligatoire");
        }
        Utilisateur utilisateur =
                utilisateurRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Utilisateur introuvable : " + id));
        // Règle : on ne suspend pas un administrateur (sinon il pourrait se bloquer lui-même)
        if (utilisateur.aRole(NomRole.ADMINISTRATEUR)) {
            throw new BusinessException("Le compte d'un administrateur ne peut pas être suspendu");
        }
        utilisateur.setStatutCompte(statutCompte);
        if (statutCompte == StatutCompte.SUSPENDU) {
            authSessionRepository
                    .findByUtilisateurIdAndRevokedFalse(id)
                    .forEach(session -> session.setRevoked(true));
        }
        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    public UtilisateurResponse creerUtilisateur(CreationUtilisateurRequest request) {
        if (request.role() != NomRole.ADMINISTRATEUR) {
            return register(
                    new RegisterRequest(
                            request.prenom(),
                            request.nom(),
                            request.telephone(),
                            request.email(),
                            request.motDePasse(),
                            request.cguAcceptees(),
                            request.role(),
                            request.adresse(),
                            request.zoneId(),
                            request.metier(),
                            request.competences(),
                            request.description(),
                            request.whatsapp(),
                            request.zoneIds()));
        }

        verifierCgu(request.cguAcceptees());
        verifierEmailEtTelephoneLibres(request.email(), request.telephone());

        Utilisateur utilisateur =
                utilisateurMapper.toClient(
                        new InscriptionClientRequest(
                                request.prenom(),
                                request.nom(),
                                request.telephone(),
                                request.email(),
                                request.motDePasse(),
                                request.cguAcceptees(),
                                request.adresse(),
                                request.zoneId(),
                                null));
        utilisateur.setRole(
                roleRepository
                        .findByNom(NomRole.ADMINISTRATEUR)
                        .orElseThrow(
                                () -> new IllegalStateException("Rôle ADMINISTRATEUR absent")));
        utilisateur.setMotDePasseHache(passwordEncoder.encode(request.motDePasse()));
        enregistrerAcceptationCgu(utilisateur);

        if (request.zoneId() != null) {
            utilisateur.setZone(
                    zoneRepository
                            .findById(request.zoneId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Zone introuvable : " + request.zoneId())));
        }

        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    public UtilisateurResponse register(RegisterRequest request) {
        if (request.role() == NomRole.CLIENT) {
            return inscrireClient(
                    new InscriptionClientRequest(
                            request.prenom(),
                            request.nom(),
                            request.telephone(),
                            request.email(),
                            request.motDePasse(),
                            request.cguAcceptees(),
                            request.adresse(),
                            request.zoneId(),
                            null));
        }
        if (request.role() == NomRole.PROFESSIONNEL) {
            if (request.metier() == null || request.metier().isBlank()) {
                throw new BusinessException("Le métier est obligatoire pour un professionnel");
            }
            return inscrireProfessionnel(
                    new InscriptionProfessionnelRequest(
                            request.prenom(),
                            request.nom(),
                            request.telephone(),
                            request.email(),
                            request.motDePasse(),
                            request.cguAcceptees(),
                            request.metier(),
                            request.competences(),
                            request.description(),
                            request.whatsapp(),
                            request.zoneIds(),
                            null));
        }
        throw new BusinessException(
                "L'inscription publique accepte uniquement CLIENT ou PROFESSIONNEL");
    }

    @Override
    public UtilisateurResponse inscrireClient(InscriptionClientRequest request) {
        verifierCgu(request.cguAcceptees());
        verifierEmailEtTelephoneLibres(request.email(), request.telephone());

        Utilisateur client = utilisateurMapper.toClient(request);
        client.setRole(
                roleRepository
                        .findByNom(NomRole.CLIENT)
                        .orElseThrow(() -> new IllegalStateException("Rôle CLIENT absent")));
        client.setMotDePasseHache(passwordEncoder.encode(request.motDePasse()));
        enregistrerAcceptationCgu(client);

        if (request.quartier() != null && !request.quartier().isBlank()) {
            // Le quartier écrit par le client : retrouvé, ou ajouté s'il est nouveau
            client.setZone(trouverOuCreerQuartier(request.quartier()));
        } else if (request.zoneId() != null) {
            Zone zone =
                    zoneRepository
                            .findById(request.zoneId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Zone introuvable : " + request.zoneId()));
            client.setZone(zone);
        }

        return utilisateurMapper.toResponse(utilisateurRepository.save(client));
    }

    @Override
    public UtilisateurResponse inscrireProfessionnel(InscriptionProfessionnelRequest request) {
        verifierCgu(request.cguAcceptees());
        verifierEmailEtTelephoneLibres(request.email(), request.telephone());

        Utilisateur pro = utilisateurMapper.toProfessionnel(request);
        pro.setRole(
                roleRepository
                        .findByNom(NomRole.PROFESSIONNEL)
                        .orElseThrow(() -> new IllegalStateException("Rôle PROFESSIONNEL absent")));
        pro.setMotDePasseHache(passwordEncoder.encode(request.motDePasse()));
        enregistrerAcceptationCgu(pro);

        // Les zones écrites par le pro ("Médina, Fass") : retrouvées, ou ajoutées si nouvelles
        if (request.zones() != null && !request.zones().isBlank()) {
            for (String nom : request.zones().split("[,;]")) {
                if (!nom.isBlank()) {
                    pro.getZones().add(trouverOuCreerQuartier(nom));
                }
            }
        }

        List<Long> zoneIds = request.zoneIds();
        if (zoneIds != null && !zoneIds.isEmpty()) {
            List<Zone> zones = zoneRepository.findAllById(zoneIds);
            if (zones.size() != new HashSet<>(zoneIds).size()) {
                throw new ResourceNotFoundException("Une ou plusieurs zones sont introuvables");
            }
            pro.getZones().addAll(zones);
        }

        return utilisateurMapper.toResponse(utilisateurRepository.save(pro));
    }

    // ---------- Méthodes internes ----------

    /**
     * Un quartier écrit par l'utilisateur (ex : " sacré-cœur 3 ").
     * S'il existe déjà (même nom, majuscules ignorées), on le réutilise ;
     * sinon on l'ajoute à la liste des quartiers. Ainsi la recherche par quartier
     * continue de marcher, et la liste se remplit avec les vrais quartiers des utilisateurs.
     */
    private Zone trouverOuCreerQuartier(String nomEcrit) {
        String nom = nomEcrit.trim().replaceAll("\\s+", " ");
        if (nom.length() > 100) {
            nom = nom.substring(0, 100);
        }
        String nomFinal = nom;
        return zoneRepository
                .findFirstByNomIgnoreCaseAndType(nomFinal, TypeZone.QUARTIER)
                .orElseGet(() -> {
                    Zone quartier = new Zone();
                    quartier.setNom(nomFinal);
                    quartier.setType(TypeZone.QUARTIER);
                    return zoneRepository.save(quartier);
                });
    }

    // Règle : l'acceptation des CGU est obligatoire
    private void verifierCgu(boolean cguAcceptees) {
        if (!cguAcceptees) {
            throw new BusinessException("Vous devez accepter les conditions d'utilisation (CGU)");
        }
    }

    // Règle : un email et un téléphone ne servent qu'à un seul compte.
    // L'email est facultatif : on ne le vérifie que s'il est rempli.
    private void verifierEmailEtTelephoneLibres(String email, String telephone) {
        String mail = utilisateurMapper.normaliserEmail(email);
        if (mail != null && utilisateurRepository.existsByEmailIgnoreCase(mail)) {
            throw new BusinessException("Cet email est déjà utilisé");
        }
        String tel = utilisateurMapper.normaliserTelephone(telephone);
        if (utilisateurRepository.existsByTelephone(tel)) {
            throw new BusinessException("Ce numéro de téléphone est déjà utilisé");
        }
    }

    // Trace de l'acceptation des CGU (avec la date)
    private void enregistrerAcceptationCgu(Utilisateur utilisateur) {
        utilisateur.setCguAcceptees(true);
        utilisateur.setDateAcceptationCgu(LocalDateTime.now());
    }
}
