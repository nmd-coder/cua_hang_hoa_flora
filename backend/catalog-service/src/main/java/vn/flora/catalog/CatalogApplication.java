package vn.flora.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
public class CatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/products", "/api/products/**").permitAll()
                .requestMatchers("/api/categories", "/api/categories/**").permitAll()
                .requestMatchers("/api/occasions", "/api/occasions/**").permitAll()
                .requestMatchers("/api/flower-types", "/api/flower-types/**").permitAll()
                .requestMatchers("/api/colors", "/api/colors/**").permitAll()
                .requestMatchers("/api/admin/products", "/api/admin/products/**").permitAll()
                .anyRequest().denyAll())
            .build();
    }
}
