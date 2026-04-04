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
	 * 共用方法：把 session 的投資人資料放到 model
	 */
	private void addSessionToModel(HttpSession session, Model model) {
		model.addAttribute("investorId", session.getAttribute("investor_id"));
//		System.out.println("investor_username"+session.getAttribute("investor_username"));
		model.addAttribute("username", session.getAttribute("investor_username"));
		model.addAttribute("watchId", session.getAttribute("watch_id"));
	}

	@GetMapping("/home")
	public String getHomePage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "home";
	}

	@RequestMapping("/classify")
	public String getClassifyPage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "classify";
	}

//    @GetMapping("/investor")
//    public String investorPage(@RequestParam(value = "investor_id", required = false) Integer investorId,
//                               HttpSession session,
//                               Model model) {
//
//        // 如果有傳參數，更新 session 的投資人資料
//        if (investorId != null) {
//            Investor inv = investorService.getById(investorId);
//            if (inv != null) {
//                session.setAttribute("investor_id", inv.getId());
//                session.setAttribute("username", inv.getUsername());
//
//                if (inv.getWatchs() != null && !inv.getWatchs().isEmpty()) {
//                    session.setAttribute("watch_id", inv.getWatchs().iterator().next().getId());
//                }
//                session.setAttribute("investor", inv);
//            }
//        }
//
//        addSessionToModel(session, model);
//        return "investor";
//    }
	@GetMapping("/investor")
	public String investorPage(@RequestParam(value = "investor_id", required = false) Integer investorId,
			HttpSession session,
			Model model) {

		// 如果有傳參數就更新 session
		if (investorId != null) {
			Investor inv = investorService.getById(investorId);
			if (inv != null) {
				session.setAttribute("investor_id", inv.getId());
				session.setAttribute("username", inv.getUsername());
			}
		}

		addSessionToModel(session, model);

		// 這邊將 session 的 investorId 放到 HTML
		model.addAttribute("investorIdFromSession", session.getAttribute("investor_id"));

		return "investor"; // 對應 investor.html
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

	/**
	 * watchlist.html 對應頁面
	 * 必須傳入 watch_id 參數
	 */
	@GetMapping("/watchlist")
	public String watchlist(HttpSession session, Model model) {
	    // 從 session 取得 watch_id
//	    Object watchId = session.getAttribute("watch_id");
//
//	    // 放到 Model
//	    model.addAttribute("watch_id", watchId);
		addSessionToModel(session, model);
	    return "watchlist";
	}

	@RequestMapping("/asset")
	public String getAssetPage(HttpSession session, Model model) {
		addSessionToModel(session, model);
		return "asset";
	}
}
