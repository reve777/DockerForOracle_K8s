package com.portfolio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.portfolio.entity.User;
import com.portfolio.repository.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userName)
			throws UsernameNotFoundException {

		User user = userRepository.findByUserName(userName)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + userName));

		// **修正點：直接返回自訂的 MyUserDetails 實例**
		// MyUserDetails 的建構子已經處理了角色字串到帶有 ROLE_ 前綴的 SimpleGrantedAuthority 的轉換。
		// 這樣做更符合 Spring Security 的自訂 UserDetails 模式，且程式碼更精簡。
		return new MyUserDetails(user);
	}
}