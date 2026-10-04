package sn.senproxiteranga.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.repository.RoleRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:context-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "security.bootstrap-admin.password=ContextTestPassword123!"
})
class BackendApplicationTests {

    @Autowired
    private UtilisateurRepository utilisateurs;

    @Autowired
    private RoleRepository roles;

	@Test
	void contextLoads() {
	}

    @Test
    @Transactional
    void professionalSearchUsesRoleAndVisibilityFilters() {
        for (NomRole role : new NomRole[] {NomRole.CLIENT, NomRole.PROFESSIONNEL}) {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setRole(roles.findByNom(role).orElseThrow());
            utilisateur.setPrenom("Test");
            utilisateur.setNom(role.name());
            utilisateur.setEmail(role.name() + "@search.example");
            utilisateur.setTelephone(role == NomRole.CLIENT ? "771111111" : "772222222");
            utilisateur.setMotDePasseHache("test-only-hash");
            utilisateur.setMetier("plombier");
            utilisateur.setStatutVerification(StatutVerification.VALIDE);
            utilisateurs.saveAndFlush(utilisateur);
        }
        var resultat = utilisateurs.rechercher("plomb", null, null,
                StatutVerification.VALIDE, StatutCompte.ACTIF);
        assertEquals(1, resultat.size());
        assertEquals(NomRole.PROFESSIONNEL, resultat.getFirst().getRole().getNom());
        resultat.getFirst().setStatutCompte(StatutCompte.SUSPENDU);
        utilisateurs.flush();
        assertEquals(0, utilisateurs.rechercher("plomb", null, null,
                StatutVerification.VALIDE, StatutCompte.ACTIF).size());
    }

}
