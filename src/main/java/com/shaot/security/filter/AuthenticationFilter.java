//package com.shaot.security.filter;
//
//import java.io.IOException;
//import java.security.Principal;
//import java.util.Base64;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.mindrot.jbcrypt.BCrypt;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletRequestWrapper;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpMethod;
//import org.springframework.stereotype.Component;
//
//import com.shaot.model.Company;
//import com.shaot.model.Worker;
//import com.shaot.repository.CompaniesRepository;
//import com.shaot.repository.WorkersRepository;
//import com.shaot.security.context.SecurityContext;
//import com.shaot.security.model.ShaotUser;
//
//import lombok.RequiredArgsConstructor;
//
//@Component
//@RequiredArgsConstructor
//@Order(1)
//public class AuthenticationFilter implements Filter {
//	final CompaniesRepository companiesRepository;
//	final WorkersRepository workersRepository;
//	final SecurityContext securityContext;
//
//	@Override
//	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
//			throws IOException, ServletException {
//		HttpServletRequest request = (HttpServletRequest) req;
//		HttpServletResponse response = (HttpServletResponse) resp;
//		if (!checkEndPoint(request.getMethod(), request.getServletPath())) {
//			String sessionId = request.getSession().getId();
//			ShaotUser user = securityContext.getUserBySession(sessionId);
//			if (user == null) {
//				String[] credentials = null;
//				try {
//					credentials = getCredentials(request.getHeader("Authorization"));
//				} catch (Exception e) {
//					response.sendError(401, "token not valid");
//				}
//				Company company = companiesRepository.findCompanyByMail(credentials[0]).orElse(null);
//				Worker worker = workersRepository.findWorkerByMail(credentials[0]).orElse(null);
//				if ((company == null || !BCrypt.checkpw(credentials[1], company.getPassword()))
//						&& (worker == null || !BCrypt.checkpw(credentials[1], worker.getPassword()))) {
//					response.sendError(401, "Login or password is not valid");
//					return;
//				}
//				Set<String> roles = new HashSet<>();
//				if (company != null) {
//					roles.add("COMPANY");
//					user = new ShaotUser(company.getMail(), roles);
//					securityContext.addUserSession(sessionId, user);
//				} else if (worker != null) {
//					roles.add("WORKER");
//					user = new ShaotUser(worker.getMail(), roles);
//					securityContext.addUserSession(sessionId, user);
//				}
//				request = new WrappedRequest(request, user.getName(), user.getRoles());
//			}
//		}
//		chain.doFilter(request, response);
//	}
//
//	private String[] getCredentials(String header) {
//		String[] encoded = header.split(" ");
//		String decoded = new String(Base64.getDecoder().decode(encoded[1]));
//		return decoded.split(":");
//	}
//
//	public boolean checkEndPoint(String method, String path) {
//		return ((method.equals(HttpMethod.POST.toString())
//					&& (path.equals("/shaot/company") || path.equals("/shaot/worker")))
//				|| method.equals(HttpMethod.GET.toString())
//					&& (path.matches("/shaot/company/[0-9]*") || path.matches("/shaot/worker/[0-9]*")));
//	}
//
//	private static class WrappedRequest extends HttpServletRequestWrapper {
//		String login;
//		Set<String> roles;
//
//		public WrappedRequest(HttpServletRequest request, String login, Set<String> roles) {
//			super(request);
//			this.login = login;
//			this.roles = roles;
//		}
//
//		@Override
//		public Principal getUserPrincipal() {
//			return new ShaotUser(login, roles);
//		}
//
//	}
//}
