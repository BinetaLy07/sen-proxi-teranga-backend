package sn.senproxiteranga.backend.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import sn.senproxiteranga.backend.security.BearerSessionFilter;
import sn.senproxiteranga.backend.security.EndpointAccess;
import sn.senproxiteranga.backend.security.SessionService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, SessionService sessions, EndpointAccess access) throws Exception {
        http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(c -> c.disable())
                .formLogin(c -> c.disable())
                .httpBasic(c -> c.disable())
                .exceptionHandling(
                        e ->
                                e.authenticationEntryPoint((r, s, x) -> s.sendError(401))
                                        .accessDeniedHandler((r, s, x) -> s.sendError(403)))
                .authorizeHttpRequests(
                        a ->
                                // Laisser passer la page d'erreur interne de Spring (/error) :
                                // sinon un refus 403 (ou 404) est transformé en 401
                                a.dispatcherTypeMatchers(DispatcherType.ERROR)
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/auth/register",
                                                "/api/auth/connexion",
                                                "/api/auth/refresh",
                                                "/api/auth/inscription/client",
                                                "/api/auth/inscription/professionnel",
                                                // Mot de passe oublié : par définition, pas de badge
                                                "/api/auth/mot-de-passe/oublie",
                                                "/api/auth/mot-de-passe/reinitialiser")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/admin/utilisateurs")
                                        .hasRole("ADMINISTRATEUR")
                                        .requestMatchers(
                                                HttpMethod.PATCH,
                                                "/api/admin/utilisateurs/*/statut-compte")
                                        .authenticated()
                                        .requestMatchers("/api/auth/**")
                                        .authenticated()
                                        .requestMatchers("/api/categories/toutes")
                                        .hasRole("ADMINISTRATEUR")
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/categories",
                                                "/api/categories/*",
                                                "/api/zones",
                                                "/api/zones/*",
                                                "/api/zones/*/quartiers",
                                                "/api/zones/*/communes",
                                                "/api/professionnels/recherche",
                                                "/api/professionnels/*/profil",
                                                "/api/professionnels/*/photo",
                                                "/api/professionnels/*/avis",
                                                "/api/realisations/*/fichier",
                                                "/api/services/*",
                                                "/api/professionnels/*/services")
                                        .permitAll()
                                        .requestMatchers("/api/categories/**", "/api/zones/**")
                                        .hasRole("ADMINISTRATEUR")
                                        .requestMatchers(
                                                "/api/clients/**",
                                                "/api/professionnels/**",
                                                "/api/medias/**",
                                                "/api/demandes/**")
                                        .access(
                                                (auth, ctx) ->
                                                        authorize(
                                                                access,
                                                                auth.get(),
                                                                ctx.getRequest()))
                                        // Messagerie : tout compte connecté (l'identité vient
                                        // du badge, le service vérifie les règles)
                                        .requestMatchers("/api/messages/**")
                                        .authenticated()
                                        // Notifications : tout compte connecté ne voit QUE
                                        // les siennes (l'identité vient du badge)
                                        .requestMatchers("/api/notifications/**")
                                        .authenticated()
                                        // Administration (validation des pros, statistiques,
                                        // litiges) : réservée à l'administrateur
                                        .requestMatchers("/api/admin/**")
                                        .hasRole("ADMINISTRATEUR")
                                        .anyRequest()
                                        .denyAll())
                .addFilterBefore(
                        new BearerSessionFilter(sessions),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private AuthorizationDecision authorize(
            EndpointAccess access,
            org.springframework.security.core.Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return new AuthorizationDecision(access.allowed(authentication, path, request.getMethod()));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}