package com.portfolio.Kafka.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/kafka")
public class KafkaPageController {
	/**
	 * 共用方法：確保與 MyLoginSuccessHandler 使用相同的鍵名
	 */
	private void addSessionToModel(HttpSession session, Model model) {
		// 💡 修正：統一鍵名。HTML 裡使用的是 session.investor_id，這裡確保一致
		model.addAttribute("investorId", session.getAttribute("investor_id"));
		model.addAttribute("username", session.getAttribute("investor_username"));
		model.addAttribute("watchId", session.getAttribute("watch_id"));
	}

	// 當使用者訪問 http://localhost:8084/kafka/kafkaTransaction 時觸發
	@GetMapping("/kafkaTransaction")
	public String goTestPage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "kafkaTransaction";
	}

	// 當使用者訪問 http://localhost:8084/kafka/kafkaListener 時觸發
	@GetMapping("/kafkaListener")
	public String goTestPage2(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "kafkaListener";
	}
}