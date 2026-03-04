package com.daedong.zipmap.config;

import com.daedong.zipmap.config.security.oauth.CustomOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
    public RoleHierarchy roleHierarchy(){
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("USER")
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/", "/signUp", "/check-id", "/login", "/logout", "/users/loginForm", "/users/signUpForm", "/review", "/review/safety-map", "/post/**"
                                , "/users/mypage", "/users/find/id", "/users/find/password", "/users/reset-password", "/oauth2/**", "/alarm-ws/**").permitAll()

                        .requestMatchers("/css/**", "/js/**","/images/**", "/files/**",
                                "/review/uploadSummernoteImage",
                                "/board/uploadSummernoteImage",
                                "/notice/uploadSummernoteImage",      // 나중에 할 공지사항용
                                "/certification/uploadSummernoteImage",
                                "/search/**").permitAll()

                        // 밑에 코드와 중복
                        .requestMatchers("/error", "/favicon.ico").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 신고 기능은 로그인한 누구나 가능하도록 설정 (403 에러 해결)
                        .requestMatchers("/report/**").authenticated()


                        // 밑에 코드와 중복
                        .requestMatchers("/find/**", "/users/**",
                                "/review/**").hasRole("USER")

                        // [추가] 댓글 기능은 로그인한 유저라면 누구나 접근 가능하도록 명시
                        .requestMatchers("/reply/**").authenticated()

                        .requestMatchers("/find/**", "/users/**", "/review/**").hasRole("USER")

                        .requestMatchers("/error", "/favicon.ico", "/.well-known/**").permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin((formLogin) -> formLogin
                        .loginPage("/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .failureHandler(loginFailureHandler)

                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/",false)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOauth2UserService)
                        )
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )

                // SockJS는 iframe이라는 기술을 사용하는데, 스프링 시큐리티는 보안상
                // 이를 기본적으로 차단하기 때문에 iframe 허용 설정 추가
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 시큐리티가 아예 관여하지 않을 경로 설정
        return (web) -> web.ignoring()
                .requestMatchers("/favicon.ico", "/resources/**", "/static/**");
    }
}
