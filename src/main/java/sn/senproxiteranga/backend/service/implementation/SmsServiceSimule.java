package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.SmsSimule;
import sn.senproxiteranga.backend.repository.SmsSimuleRepository;
import sn.senproxiteranga.backend.service.SmsService;

// Simulation d'envoi de SMS : le message est affiché dans la console et gardé en base.
// Aucun vrai SMS n'est envoyé.
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SmsServiceSimule implements SmsService {

    // Un SMS standard fait 160 caractères : au-delà, on coupe
    private static final int TAILLE_MAX_SMS = 160;

    private final SmsSimuleRepository smsSimuleRepository;

    @Override
    public void envoyer(String numero, String contenu) {
        if (numero == null || numero.isBlank()) {
            log.warn("[SMS SIMULE] Pas de numéro : SMS non envoyé");
            return;
        }
        String texte = contenu.length() > TAILLE_MAX_SMS
                ? contenu.substring(0, TAILLE_MAX_SMS)
                : contenu;

        SmsSimule sms = new SmsSimule();
        sms.setNumero(numero);
        sms.setContenu(texte);
        smsSimuleRepository.save(sms);

        log.info("[SMS SIMULE] vers {} : {}", numero, texte);
    }
}