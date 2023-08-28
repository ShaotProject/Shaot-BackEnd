//package com.shaot.security.filter;
//
//import java.io.IOException;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import com.shaot.security.model.ShaotUser;
//
//@Component
//@Order(2)
//public class CompanyFilter implements Filter {
//
//	@Override
//	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
//			throws IOException, ServletException {
//		HttpServletRequest request = (HttpServletRequest) req;
//		HttpServletResponse response = (HttpServletResponse) resp;
//		if (checkEndPoint(request.getServletPath())) {
//			ShaotUser user = (ShaotUser) request.getUserPrincipal();
//			if(!user.getRoles().contains("COMPANY")) {
//				response.sendError(403, "Only manager can access this functionality");
//				return;
//			}
//		}
//		chain.doFilter(request, response);
//	}
//
//	private boolean checkEndPoint(String path) {
//		return path.matches("/shaot/company/.*") && !path.matches("/shaot/company/[0-9]*");
//	}
//}
