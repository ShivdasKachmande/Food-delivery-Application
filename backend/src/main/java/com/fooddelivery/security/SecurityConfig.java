
package com.fooddelivery.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    writeError(
                        res, 401, "Please log in to continue"))
                .accessDeniedHandler((req, res, e) ->
                    writeError(
                        res, 403,
                        "You do not have permission to do this")))

            .authorizeHttpRequests(auth -> auth

                // Allow preflight requests
                .requestMatchers(
                    HttpMethod.OPTIONS, "/**").permitAll()

                // Allow Spring Boot error handling
                .requestMatchers("/error").permitAll()

                // Login and registration
                .requestMatchers("/api/auth/**").permitAll()

                // Public restaurant and food browsing
                .requestMatchers(HttpMethod.GET,
                    "/api/restaurants",
                    "/api/restaurants/**",
                    "/api/food-items",
                    "/api/food-items/**").permitAll()

                // Restaurant and food management: admin only
                .requestMatchers(HttpMethod.POST,
                    "/api/restaurants",
                    "/api/food-items").hasRole("ADMIN")

                .requestMatchers(HttpMethod.PUT,
                    "/api/restaurants/**",
                    "/api/food-items/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.DELETE,
                    "/api/restaurants/**",
                    "/api/food-items/**").hasRole("ADMIN")

                // Guest checkout is allowed
                .requestMatchers(HttpMethod.POST, "/api/orders")
                    .permitAll()

                // Customer order history: customer only
                // This must appear before the admin order rules
                .requestMatchers(
                    HttpMethod.GET, "/api/orders/my-orders")
                    .hasRole("CUSTOMER")

                // Customer cancellation endpoint, if implemented
                .requestMatchers(
                    HttpMethod.PUT, "/api/orders/my/*/cancel")
                    .hasRole("CUSTOMER")

                // All other order-reading endpoints: admin only
                .requestMatchers(HttpMethod.GET,
                    "/api/orders",
                    "/api/orders/**").hasRole("ADMIN")

                // Updating order status: admin only
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/orders/*/status").hasRole("ADMIN")

                // All remaining endpoints require authentication
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS configuration for the frontend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    // JSON responses for authentication and authorization errors
    private static void writeError(
            HttpServletResponse res,
            int status,
            String message) throws IOException {

        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");

        res.getWriter().write(
            "{\"status\":" + status
                + ",\"message\":\"" + message + "\"}");
    }

    // Prevent the JWT filter from being registered twice
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
            jwtFilterServletRegistration() {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
            new FilterRegistrationBean<>(jwtAuthenticationFilter);

        registration.setEnabled(false);

        return registration;
    }
}
