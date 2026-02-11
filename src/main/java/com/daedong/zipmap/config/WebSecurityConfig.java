package com.daedong.zipmap.config;

import com.daedong.zipmap.service.CustomOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthenticationFailureHandler loginFailureHandler;
    private final CustomOauth2UserService customOauth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/", "/signUp", "/login","/users/loginForm","/users/signUpForm","/review","/board/**"
                ,"/users/find/id", "/users/find/password", "/users/reset-password", "/oauth2/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/files/notice/**").permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")

                .requestMatchers("/find/**","/users/**",
                        "/review/**").hasRole("WRITER")

                .anyRequest().authenticated()
            )
                .formLogin((formLogin) -> formLogin
                    .loginPage("/login")
                    .usernameParameter("loginId")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/")
                    .failureHandler(loginFailureHandler)

            )
            .oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .defaultSuccessUrl("/")
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOauth2UserService)
                 )
            )

            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
