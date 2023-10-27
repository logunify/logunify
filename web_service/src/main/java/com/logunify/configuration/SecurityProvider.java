package com.logunify.configuration;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class SecurityProvider {
    @NoArgsConstructor
    public static class ApiKeyAuthFilter extends AbstractPreAuthenticatedProcessingFilter {
        @Override
        protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
            return request.getHeader(API_KEY_AUTH_HEADER_NAME);
        }

        @Override
        protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
            return null;
        }
    }

    @AllArgsConstructor
    public static class ApiKeyAuthManager implements AuthenticationManager {

        private Set<String> validApiKeys;

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String principal = (String) authentication.getPrincipal();

            authentication.setAuthenticated(validApiKeys.contains(principal));
            return authentication;
        }
    }

    private static final String API_KEY_AUTH_HEADER_NAME = "X-Auth-Token";

    private final ConfigProvider.SecurityConfig securityConfig;

    @Autowired
    public SecurityProvider(ConfigProvider.SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Setting up the auth type: {}", securityConfig.getAuthType());
        if (securityConfig.getAuthType() == ConfigProvider.SecurityConfig.AuthType.NONE) {
            return http.authorizeRequests().anyRequest().permitAll().and().csrf().disable().build();
        }

        if (Optional.ofNullable(securityConfig.getBasicAuthConfig())
                .map(basicAuthConfig -> basicAuthConfig.getApiKeys().isEmpty())
                .orElse(true)
        ) {
            throw new IllegalArgumentException("Api key cannot be null for basic auth, please set api key with security.basic-auth.api-keys in the config " +
                    "file.");
        }
        var filter = new ApiKeyAuthFilter();
        filter.setAuthenticationManager(new ApiKeyAuthManager(securityConfig.getBasicAuthConfig().getApiKeys()));

        http.antMatcher("/api/**")
                .csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(filter)
                .authorizeRequests()
                .anyRequest()
                .authenticated();

        return http.build();
    }
}
