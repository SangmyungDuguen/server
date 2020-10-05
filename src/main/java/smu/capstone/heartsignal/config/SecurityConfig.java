package smu.capstone.heartsignal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.UUID;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
        return http
                    .csrf().disable()
                    .formLogin().disable()
                    .httpBasic().disable()
                    .headers()
                .and()
                    .authorizeExchange()
                        .pathMatchers("/static/**", "/", "/api/**").permitAll()
                        .anyExchange().authenticated()
                .and()
                    .oauth2Login()
                .and()
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(){
        UserDetails user = User
                .withUsername("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }
}
