package com.apirip.trukeamonolito.config;

import com.apirip.trukeamonolito.auth.service.AuthUserDetailsService;
import com.apirip.trukeamonolito.auth.web.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }

    @Bean
    DaoAuthenticationProvider authProvider(AuthUserDetailsService uds, PasswordEncoder enc){
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(enc);
        return p;
    }

    @Bean
    SecurityFilterChain filter(HttpSecurity http, LoginSuccessHandler successHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/products/catalog", "/signin", "/signup",
                                "/forgot-password", "/reset-password",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/h2/**","/product-images/**",
                                "/student-images/**", "/uploads/**", "/ws/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers("/offers/**").authenticated()
                        .requestMatchers("/chat/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/signin")                 // tu vista GET
                        .loginProcessingUrl("/login")         // lo procesa Spring Security (no controller)
                        .usernameParameter("username")        // <input name="username">
                        .passwordParameter("password")        // <input name="password">
                        .successHandler(successHandler)
                        .defaultSuccessUrl("/products/catalog", true)
                        .failureUrl("/signin?error")          // feedback si falla
                        .permitAll()
                )
                .logout(l -> l
                        .logoutUrl("/logout")                 // por defecto POST
                        .logoutSuccessUrl("/signin?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2/**"))
                .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }
}
