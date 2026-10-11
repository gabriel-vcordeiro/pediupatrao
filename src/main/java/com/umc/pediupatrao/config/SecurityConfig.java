package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Proteção CSRF ativada com repositório de cookie seguro para formulários e requisições AJAX/Fetch
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/login", "/logout")
                )
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos e autenticação públicos
                        .requestMatchers("/login", "/css/**", "/dist/**", "/plugins/**", "/js/**", "/images/**").permitAll()
                        
                        // RF01: Administração de usuários e perfis restrita EXCLUSIVAMENTE ao ADMIN
                        .requestMatchers("/usuarios/**", "/api/usuarios/**").hasRole("ADMIN")

                        // RF05: Consulta à auditoria restrita a ADMIN e GERENTE
                        .requestMatchers("/auditoria/**", "/api/auditoria/**").hasAnyRole("ADMIN", "GERENTE")

                        // Gestão de produtos restrita a ADMIN e GERENTE
                        .requestMatchers("/produtos/**", "/api/produtos/**").hasAnyRole("ADMIN", "GERENTE")

                        // RF04: Cancelamentos e Descontos restritos EXCLUSIVAMENTE ao GERENTE
                        .requestMatchers("/pedidos/*/cancelar", "/pedidos/*/desconto").hasRole("GERENTE")
                        .requestMatchers("/api/pedidos/*/cancelar", "/api/pedidos/*/desconto").hasRole("GERENTE")

                        // Gestão e registro de pedidos permitidos para ATENDENTE e GERENTE (ADMIN tem consulta)
                        .requestMatchers(HttpMethod.POST, "/pedidos/**", "/api/pedidos/**").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/pedidos/*/status", "/api/pedidos/*/status").hasAnyRole("ATENDENTE", "GERENTE")

                        // Gestão de clientes permitida a ATENDENTE e GERENTE
                        .requestMatchers(HttpMethod.POST, "/clientes/**", "/api/clientes/**").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/clientes/**", "/api/clientes/**").hasAnyRole("ATENDENTE", "GERENTE")

                        // Qualquer outra requisição requer autenticação
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(authenticationSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.sendRedirect("/");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            LoggerFactory.getLogger(SecurityConfig.class).warn("Acesso negado em [{} {}] para usuário [{}] - Motivo: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anônimo",
                    accessDeniedException.getMessage());
            String uri = request.getRequestURI();
            if (uri.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"erro\": \"Acesso negado: você não possui permissão para executar esta operação.\"}");
            } else {
                response.sendRedirect("/?erroAcesso=true");
            }
        };
    }
}
