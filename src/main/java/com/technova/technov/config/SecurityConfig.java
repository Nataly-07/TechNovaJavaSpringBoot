package com.technova.technov.config;

import com.technova.technov.service.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security para protección de rutas según roles.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    @Lazy
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        // Permitir acceso sin autenticación a recursos estáticos y páginas públicas
                        .requestMatchers("/frontend/css/**", "/frontend/js/**", "/frontend/imagenes/**", 
                                        "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/", "/inicio", "/login", "/registro").permitAll()
                        .requestMatchers("/categoria/**", "/marca/**", "/producto/**", "/ofertas").permitAll()
                        .requestMatchers("/api/usuarios").permitAll()
                        // Rutas protegidas por rol
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/empleado/**").hasRole("EMPLEADO")
                        .requestMatchers("/cliente/**", "/favoritos", "/carrito", "/checkout/**").hasRole("CLIENTE")
                        // APIs protegidas
                        .requestMatchers("/api/**").authenticated()
                        // Todas las demás URLs requieren autenticación
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/login", "/logout", "/api/atencion-cliente", "/api/favoritos/**", "/api/carrito/**")
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler)
                        .permitAll()
                        .failureUrl("/login?error=true")
                )
                // Permitir acceso a la página de error
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/login?error=access_denied")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );
        
        return http.build();
    }
}
