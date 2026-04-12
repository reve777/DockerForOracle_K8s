package com.portfolio.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

	@GetMapping("/welcome")
	public String welcome() {
		return "welcome";
	}

//	@GetMapping("/loginpage")
//	public String loginPage() {
//		return "loginpage";
//	}
//	
	// 在你的 Controller 中增加判斷，或是調整 SecurityConfig
	// 建議在你的 LoginController 中加入：

	@GetMapping("/loginpage")
	public String loginPage(Principal principal) {
	    if (principal != null) {
	        // 💡 如果已經記住我登入了，直接送他去首頁，不要讓他看登入頁
	        return "redirect:/page/home"; 
	    }
	    return "loginpage"; // 沒登入才看登入頁
	}

	@GetMapping("/fail")
	@ResponseBody
	public String fail() {
		return "fail";
	}

	@GetMapping("/admin")
	@ResponseBody
	public String adminPage() {
		return "adminpage";
	}

	@GetMapping("/manager")
	@ResponseBody
	public String managerPage() {
		return "managerpage";
	}

	@GetMapping("/employee")
	@ResponseBody
	public String employeePage() {
		return "employeepage";
	}
}
