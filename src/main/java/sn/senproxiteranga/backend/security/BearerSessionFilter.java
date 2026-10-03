package sn.senproxiteranga.backend.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class BearerSessionFilter extends OncePerRequestFilter {
    private final SessionService sessions;

    public BearerSessionFilter(SessionService sessions) {
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null) {
            if (!header.startsWith("Bearer ") || header.length() > 256) {
                res.sendError(401);
                return;
            }
            try {
                var p = sessions.authenticate(header.substring(7));
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                p, null, List.of(new SimpleGrantedAuthority("ROLE_" + p.role()))));
                SecurityContextHolder.setContext(context);
            } catch (AuthenticationException ex) {
                res.sendError(401);
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
