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

	private final UserDetailsService userDetailsService;
	private final MyLoginSuccessHandler myLoginSuccessHandler;
	private final MyAccessDeniedHandler myAccessDeniedHandler;
	private final CustomLogoutSuccessHandler customLogoutSuccessHandler; // 注入登出處理器

	// 透過建構子注入必要的服務
	public SecurityConfiguration(UserDetailsService userDetailsService,
			MyLoginSuccessHandler myLoginSuccessHandler,
			MyAccessDeniedHandler myAccessDeniedHandler,
			CustomLogoutSuccessHandler customLogoutSuccessHandler) { // 新增注入
		this.userDetailsService = userDetailsService;
		this.myLoginSuccessHandler = myLoginSuccessHandler;
		this.myAccessDeniedHandler = myAccessDeniedHandler;
		this.customLogoutSuccessHandler = customLogoutSuccessHandler; // 設定欄位
	}

	/**
	 * 密碼編碼器
	 */
	@Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * HTTP Security 配置
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// CSRF 保留測試用註解
				.csrf(csrf -> csrf.disable()) // 先關閉 CSRF 方便測試，正式建議開啟

				// 授權請求配置
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/resources/**", "/register/**", "/post/**", "/error", "/loginpage")
						.permitAll() // 這些路徑允許所有訪問
						.requestMatchers("/admin/**")
						.hasRole("ADMIN") // 僅限 ADMIN 角色
						.requestMatchers("/manager/**")
						.hasRole("MANAGER") // 僅限 MANAGER 角色
						.anyRequest().authenticated()) // 其他所有請求都需要驗證

				// 表單登入配置
				.formLogin(form -> form
						.loginPage("/loginpage") // 指定自訂的登入頁面 URL
						.loginProcessingUrl("/login") // 指定登入表單提交的 URL
						.successHandler(myLoginSuccessHandler) // 使用注入的自訂登入成功處理器
						.permitAll())

				// 登出配置
				.logout(logout -> logout
						.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
						.logoutSuccessHandler(customLogoutSuccessHandler) // **使用自訂的 LogoutSuccessHandler**
						.permitAll())

				// 權限不足處理配置
				.exceptionHandling(exception -> exception
						.accessDeniedHandler(myAccessDeniedHandler)); // 使用注入的自訂權限不足處理器

		return http.build();
	}
}