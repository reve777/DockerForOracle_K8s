package com.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

	@GetMapping("/welcome")
	public String welcome() {
		return "welcome";
	}

	@GetMapping("/loginpage")
	public String loginPage() {
		return "loginpage";
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
