package cocha.vive.backend.auth;

import cocha.vive.backend.model.User;
import cocha.vive.backend.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
        ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        String userEmail = null;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found for request: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT received for request: {} {}", request.getMethod(), request.getRequestURI());
        } catch (Exception e) {
            log.warn("Invalid JWT received for request: {} {} ({})",
                request.getMethod(),
                request.getRequestURI(),
                e.getClass().getSimpleName());
        }

        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            final String tokenEmail = userEmail;
            User user = this.userService.getByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("JWT email does not belong to a registered user: {}", tokenEmail);
                    return new RuntimeException("Token doesn't belong to a registered user");
                });

            if (jwtService.isTokenValid(jwt, user)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JWT authenticated user id: {} for request: {} {}",
                    user.getId(),
                    request.getMethod(),
                    request.getRequestURI());
            } else {
                log.warn("JWT validation failed for user email: {} on request: {} {}",
                    userEmail,
                    request.getMethod(),
                    request.getRequestURI());
            }
        } else if (userEmail != null) {
            log.debug("Security context already has authentication for request: {} {}",
                request.getMethod(),
                request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
