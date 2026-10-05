package sn.senproxiteranga.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Active les tâches automatiques (@Scheduled) :
// Spring lancera tout seul, à intervalle régulier, les méthodes marquées @Scheduled
// (ex : passer en EXPIREE les demandes sans réponse après 48 h)
@Configuration
@EnableScheduling
public class PlanificationConfig {
}