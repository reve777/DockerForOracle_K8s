package com.portfolio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.portfolio.entity.Investor;
import com.portfolio.service.InvestorService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/page")
public class PageController {

	@Autowired
	private InvestorService investorService;

	/**
	 * 共用方法：確保與 MyLoginSuccessHandler 使用相同的鍵名
	 */
	private void addSessionToModel(HttpSession session, Model model) {
		// 💡 修正：統一鍵名。HTML 裡使用的是 session.investor_id，這裡確保一致
		model.addAttribute("investorId", session.getAttribute("investor_id"));
		model.addAttribute("username", session.getAttribute("investor_username"));
		model.addAttribute("watchId", session.getAttribute("watch_id"));
	}

	@GetMapping("/home")
	public String getHomePage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "home";
	}

	@GetMapping("/investor")
	public String investorPage(@RequestParam(value = "investor_id", required = false) Integer investorId,
			HttpSession session, Model model) {

		// 💡 修正 1：點擊「登入身份」切換時的操作
		if (investorId != null) {
			// 💡 修正 2：如果 Service 內部查詢 investor 出現 balance 溢位，這裡會報 500
			// 確保 Investor Entity 的 balance 欄位已經改為 Long
			Investor inv = investorService.getById(investorId);
			if (inv != null) {
				// 💡 修正 3：統一鍵名，這會讓 HTML 裡的 "當前帳戶" 判定生效
				session.setAttribute("investor_id", inv.getId());
				session.setAttribute("investor_username", inv.getUsername());

				// 補齊 watch_id 確保頁面功能正常
				if (inv.getWatches() != null && !inv.getWatches().isEmpty()) {
					session.setAttribute("watch_id", inv.getWatches().iterator().next().getId());
				}
			}
		}

		addSessionToModel(session, model);
		return "investor"; // 對應 investor.html
	}

	// --- 其餘方法保持不變 ---
	@RequestMapping("/classify")
	public String getClassifyPage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "classify";
	}

	@RequestMapping("/tstock")
	public String getTStockPage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "tstock";
	}

	@RequestMapping("/watch")
	public String getWatchPage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "watch";
	}

	@GetMapping("/watchlist")
	public String watchlist(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "watchlist";
	}

	@RequestMapping("/asset")
	public String getAssetPage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "asset";
	}

	@RequestMapping("/classifyExcel")
	public String getlassifyExcelPage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "classifyExcel";
	}

	/**
	 * JWE 建立頁面 - 統一由 PageController 處理路由
	 * 對外路徑: /page/jwekey/new
	 */
	@RequestMapping("/jwekey/new")
	public String getJweKeyCreatePage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "jwe/JweKeyCreate"; // src/main/resources/templates/jwe/JweKeyCreate.html
	}

	@RequestMapping("/bank")
	public String getBankPage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "bank"; // src/main/resources/templates/jwe/JweKeyCreate.html
	}
}