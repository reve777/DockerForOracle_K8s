package com.portfolio.handle;

import com.portfolio.repository.InvestorRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final InvestorRepository investorRepository;

	public MyLoginSuccessHandler(InvestorRepository investorRepository) {
		this.investorRepository = investorRepository;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication)
			throws IOException, ServletException {

		System.out.println("✅ MyLoginSuccessHandler (onAuthenticationSuccess) 被呼叫！");

		String username = authentication.getName();

		// 1. 設定 Session 屬性
		HttpSession session = request.getSession();
		session.setAttribute("investor_username", username); // 設定通用的 username
		session.setMaxInactiveInterval(30 * 60); // 30 分鐘

		investorRepository.findByUsername(username).ifPresentOrElse(investor -> {
			// **情況一：找到 Investor 資料，設置詳細 Session 屬性**
			session.setAttribute("investor_id", investor.getId());

			// 假設 Watchs 集合不為空，設定第一個 watch_id
			if (investor.getWatchs() != null && !investor.getWatchs().isEmpty()) {
				session.setAttribute("watch_id", investor.getWatchs().iterator().next().getId());
			}

			System.out.println("Session 已設置: investor_id=" + investor.getId() +
					", username=" + investor.getUsername() + " (已找到 Investor 詳細資料)");
		}, () -> {
			// **情況二：未找到 Investor 資料（可能為首次登入），僅設置最少 Session 屬性**
			// 移除嚴重的錯誤提示，改為一般的警告/資訊，因為這可能是預期行為。
			System.out.println(
					"⚠️ 警告：已驗證使用者 (" + username
							+ ") 在 Investor 資料表中找不到對應紀錄。這可能是首次登入或資料尚未完善。Session 中 **未設定** investor_id 和 watch_id。");
			// 此處不設置 investor_id 和 watch_id，讓後續邏輯去處理這些缺失的值（例如導向設定檔頁面）。
		});

		// 2. 根據角色進行導向 (此段邏輯不變)
		String redirectUrl = "/portfolio/page/home"; // 預設頁面

		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			redirectUrl = "/portfolio/page/home";
		} else if (authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
			redirectUrl = "/portfolio/page/home";
		} else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"))) {
			redirectUrl = "/portfolio/page/home"; // GUEST 導向與預設相同
		}

		// **您可能還想新增一個條件：如果未找到 Investor 資料，強制導向到資料設定頁面**
		if (session.getAttribute("investor_id") == null && redirectUrl.equals("/portfolio/page/home")) {
			// 如果用戶沒有 Investor ID 且原本會被導向普通首頁，則將其導向個人資料設定頁面 (例如:
			// /portfolio/page/profile-setup)
			System.out.println("ℹ️ 資訊：由於缺少 Investor ID，將使用者 (" + username + ") 導向至資料設定頁面。");
			// redirectUrl = "/portfolio/page/profile-setup"; // 假設有這個頁面
		}

		// 使用 sendRedirect 執行導向
		response.sendRedirect(redirectUrl);
	}
}