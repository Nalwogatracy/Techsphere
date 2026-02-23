
package com.app.TechSphere.security;

import com.app.TechSphere.model.User;
import com.app.TechSphere.repository.UserRepository;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {
    
     private final CustomLoginSuccessHandler successHandler;
     private final CustomAccessDeniedHandler accessDeniedHandler;
     public SecurityConfig(CustomLoginSuccessHandler successHandler,
             CustomAccessDeniedHandler accessDeniedHandler) {
        this.successHandler = successHandler;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index", "/login", "/user/register", "/css/**", "/js/**", "/images/**","/cart/add", "/user/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/vendor/**", "/old-sales/submit/**", "/user/products","/user/contact").hasRole("VENDOR")
                .requestMatchers("/user/**").hasRole("CUSTOMER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler) // redirects after login
                .permitAll()
            )
               /* .formLogin(form -> form
                // Customer login
                .loginPage("/user/login")             // user login page
                .loginProcessingUrl("/user/login")    // POST URL
                .defaultSuccessUrl("/user/dashboard", true)
                .permitAll()
            ) */
                .exceptionHandling(ex -> ex
            .accessDeniedHandler(accessDeniedHandler)
        )
                
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
            )
            .rememberMe(remember -> remember
                .key("techsphere-remember-me")
                .tokenValiditySeconds(604800)
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found: " + username));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name()) // ADMIN or USER
                    .build();
        };
    }

}
