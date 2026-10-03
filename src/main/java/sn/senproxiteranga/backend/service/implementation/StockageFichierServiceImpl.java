package sn.senproxiteranga.backend.service.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.service.StockageFichierService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StockageFichierServiceImpl implements StockageFichierService {

    // Chemin complet du dossier de rangement (ex : C:/Projets/SenProxiTeranga/backend/uploads)
    private final Path dossier;

    /**
     * Spring appelle ce constructeur au démarrage.
     * @Value lit le paramètre "app.uploads.dossier" du fichier application.yaml.
     */
    public StockageFichierServiceImpl(@Value("${app.uploads.dossier}") String dossierConfig) {
        this.dossier = Paths.get(dossierConfig).toAbsolutePath().normalize();
        try {
            // Crée le dossier s'il n'existe pas encore (ne fait rien s'il existe déjà)
            Files.createDirectories(this.dossier);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le dossier des fichiers : " + this.dossier, e);
        }
    }

    @Override
    public String enregistrer(MultipartFile fichier, String extension) {
        // Nom unique généré par nous, jamais par l'utilisateur
        String nomStocke = UUID.randomUUID() + "." + extension;
        Path destination = dossier.resolve(nomStocke);

        try (InputStream contenu = fichier.getInputStream()) {
            Files.copy(contenu, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'enregistrer le fichier " + fichier.getOriginalFilename(), e);
        }
        return nomStocke;
    }

    @Override
    public Resource charger(String nomStocke) {
        Path chemin = cheminSecurise(nomStocke);
        if (!Files.exists(chemin)) {
            throw new ResourceNotFoundException("Fichier introuvable : " + nomStocke);
        }
        return new FileSystemResource(chemin);
    }

    @Override
    public void supprimer(String nomStocke) {
        try {
            // Ne fait rien si le fichier n'existe plus
            Files.deleteIfExists(cheminSecurise(nomStocke));
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de supprimer le fichier " + nomStocke, e);
        }
    }

    /**
     * Construit le chemin du fichier et vérifie qu'il reste bien DANS le dossier uploads.
     * Empêche un nom piégé comme "../../application.yaml" de sortir du dossier.
     */
    private Path cheminSecurise(String nomStocke) {
        Path chemin = dossier.resolve(nomStocke).normalize();
        if (!chemin.startsWith(dossier)) {
            throw new ResourceNotFoundException("Fichier introuvable : " + nomStocke);
        }
        return chemin;
    }
}