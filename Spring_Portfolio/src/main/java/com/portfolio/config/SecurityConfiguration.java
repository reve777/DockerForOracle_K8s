package com.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.portfolio.handle.CustomLogoutSuccessHandler;
import com.portfolio.handle.MyAccessDeniedHandler;
import com.portfolio.handle.MyLoginSuccessHandler;

@Configuration
public class SecurityConfiguration {

    private final MyLoginSuccessHandler myLoginSuccessHandler;
    private final MyAccessDeniedHandler myAccessDeniedHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    // 💡 新增：需要 UserDetailsService 來實現 Remember Me 自動登入
    private final UserDetailsService userDetailsService;

    public SecurityConfiguration(UserDetailsService userDetailsService,
            MyLoginSuccessHandler myLoginSuccessHandler,
            MyAccessDeniedHandler myAccessDeniedHandler,
            CustomLogoutSuccessHandler customLogoutSuccessHandler) {
        this.userDetailsService = userDetailsService; // 💡 注入
        this.myLoginSuccessHandler = myLoginSuccessHandler;
        this.myAccessDeniedHandler = myAccessDeniedHandler;
        this.customLogoutSuccessHandler = customLogoutSuccessHandler;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 💡 第一步：排除綠界路徑的 CSRF 檢查
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/ecpay/**", "/pay-result") 
            )

            .authorizeHttpRequests(auth -> auth
                // 💡 第二步：放行路徑
                .requestMatchers(
                    "/", 
                    "/resources/**", 
                    "/register/**", 
                    "/post/**", 
                    "/error", 
                    "/loginpage",
                    "/ecpay/**",      
                    "/pay-result"     
//                    "/portfolio/classify/import" 
                ).permitAll()
                
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasRole("MANAGER")
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/loginpage")
                .loginProcessingUrl("/login")
                .successHandler(myLoginSuccessHandler)
                .permitAll()
            )

            // 💡 第三步：加上 Remember Me 配置
            .rememberMe(remember -> remember
                .key("portfolioSecretKey")           // 加密 Cookie 的金鑰
                .rememberMeParameter("remember-me")  // 對應 HTML 中 Checkbox 的 name
                .tokenValiditySeconds(86400 * 7)     // 有效期 7 天
                .userDetailsService(userDetailsService) // 必須指定以利自動讀取用戶
            )

            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .deleteCookies("remember-me")        // 💡 登出時刪除 Cookie
                .permitAll()
            )

            .exceptionHandling(exception -> exception
                .accessDeniedHandler(myAccessDeniedHandler)
            );

        return http.build();
    }
}