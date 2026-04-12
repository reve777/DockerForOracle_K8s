package com.portfolio.filter;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.portfolio.repository.InvestorRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginFilter extends OncePerRequestFilter {

	@Autowired
	private InvestorRepository investorRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		String investorIdParam = request.getParameter("investor_id");

		if (investorIdParam != null && !investorIdParam.isEmpty()) {
			try {
				// 💡 1. 雖然 ID 目前通常是 Integer，但建議用 Long 解析較安全，或維持 Integer 但捕獲錯誤
				int id = Integer.parseInt(investorIdParam);

				// 💡 2. 使用 JOIN FETCH 的方法，解決 "no Session" 錯誤
				investorRepository.findByIdWithWatchs(id).ifPresent(investor -> {
					// 💡 3. 統一 Session 鍵名，確保與其他 Handler 一致
					session.setAttribute("investor_id", investor.getId());
					session.setAttribute("investor_username", investor.getUsername());

					// 💡 4. 因為用了 JOIN FETCH，這裡存取 watchs 不會再報錯
					if (investor.getWatchs() != null && !investor.getWatchs().isEmpty()) {
						session.setAttribute("watch_id", investor.getWatchs().iterator().next().getId());
					}

					// 💡 注意：由於 investor 物件包含大型集合與懶載入欄位
					// 建議 Session 存 ID 即可，若要存物件，請確保 Entity 有實作 Serializable
					session.setAttribute("investor", investor);

					System.out.println("✅ [Filter] 成功切換身份為: " + investor.getUsername());
				});
			} catch (NumberFormatException e) {
				System.err.println("❌ 投資人 ID 格式錯誤: " + investorIdParam);
			}
		}

		chain.doFilter(request, response);
	}
}