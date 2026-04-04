package com.portfolio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class StsPortfolioApplicationTests {

//	@Test
//	void contextLoads() {
//	}
	public static void main(String[] args) {

		// 跟 Bean 一致的強度
		PasswordEncoder pe = new BCryptPasswordEncoder(12);

		String password1 = "1234";
		String encoded1 = pe.encode(password1);
		System.out.println("Encoded 1234: " + encoded1);
		System.out.println("Matches 1234: " + pe.matches(password1, encoded1));

		System.out.println();

		String password2 = "5678";
		String encoded2 = pe.encode(password2);
		System.out.println("Encoded 5678: " + encoded2);
		System.out.println("Matches 5678: " + pe.matches(password2, encoded2));
	}

}
