package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Client;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.dto.InscriptionClientRequest;
import sn.senproxiteranga.backend.dto.InscriptionProfessionnelRequest;
import sn.senproxiteranga.backend.dto.UtilisateurResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.UtilisateurMapper;
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

    @Override
    public UtilisateurResponse inscrireClient(InscriptionClientRequest request) {
        verifierCgu(request.cguAcceptees());
        verifierEmailEtTelephoneLibres(request.email(), request.telephone());

        Client client = utilisateurMapper.toClient(request);
        client.setMotDePasseHache(passwordEncoder.encode(request.motDePasse()));
        enregistrerAcceptationCgu(client);

        if (request.zoneId() != null) {
            Zone zone = zoneRepository.findById(request.zoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + request.zoneId()));
            client.setZone(zone);
        }

        return utilisateurMapper.toResponse(utilisateurRepository.save(client));
    }

    @Override
    public UtilisateurResponse inscrireProfessionnel(InscriptionProfessionnelRequest request) {
        verifierCgu(request.cguAcceptees());
        verifierEmailEtTelephoneLibres(request.email(), request.telephone());

        Professionnel pro = utilisateurMapper.toProfessionnel(request);
        pro.setMotDePasseHache(passwordEncoder.encode(request.motDePasse()));
        enregistrerAcceptationCgu(pro);

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

    // Règle : l'acceptation des CGU est obligatoire
    private void verifierCgu(boolean cguAcceptees) {
        if (!cguAcceptees) {
            throw new BusinessException("Vous devez accepter les conditions d'utilisation (CGU)");
        }
    }

    // Règle : un email et un téléphone ne servent qu'à un seul compte
    private void verifierEmailEtTelephoneLibres(String email, String telephone) {
        if (utilisateurRepository.existsByEmailIgnoreCase(email.trim())) {
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