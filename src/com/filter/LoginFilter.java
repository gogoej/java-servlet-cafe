package com.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cafe.admin.main.MainServlet;
import com.cafe.auth.SessionAuthInfo;

public class LoginFilter implements Filter {

	private FilterConfig filterConfig;
	//������ �α��ν��Ѿ� �ϴ� ������ ���͸� INCLUDE_URI�� �켱������ �� ����
	private static final String[] INCLUDE_URIS = {"/auth/mypage.do"};
	private static final String[] EXCLUDE_URIS = { "/main/**", "/resource/**", "/auth/**"
			, "/news/notice/list.do", "/news/notice/view.do"
			,"/news/event/list.do", "/news/event/view.do"
			,"/store/**", "/menu/**"
			, "/admin/**"//������ �α����������� AdminAuthFilter���� ���͸�
	};

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req;
		HttpServletResponse resp;
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			req = (HttpServletRequest) request;
			resp = (HttpServletResponse) response;
			
			//�ʼ��� �α����� �ʿ��� �������� ���ܰ� �ƴ� �������� �⺻������ �α����� �䱸�ؾ� �Ѵ�.
			if (isIncludeUri(req) || isExcludeUri(req) == false) {
				// �α��� Ȯ��
				SessionAuthInfo info = (SessionAuthInfo) req.getSession().getAttribute(MainServlet.SESSION_INFO);
//				System.out.println(info);
				if (info == null) {
					resp.sendRedirect(req.getContextPath() + "/auth/login.do");
					return;
				}
			}
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		filterConfig = null;
	}
	
	private boolean isIncludeUri(HttpServletRequest req) {
		String uri = req.getRequestURI();
		String cp = req.getContextPath();
		uri = uri.substring(cp.length());
		if(uri.substring(uri.length()-1).equals("/")) {
			uri = uri.substring(0,-1);
		}

		if (uri.length() <= 1 || uri.equals("/")) {
			return false;
		}

		for (String s : INCLUDE_URIS) {
			if (s.lastIndexOf("/**") != -1) {
				s = s.substring(0, s.lastIndexOf("/**"));
				if (uri.indexOf(s) == 0 || s.contains(uri)) {// ex: s=/auth/** , uri=/auth/abc.do �̸�  /auth/�� ��ġ�Ƿ� 0�� ���´�. 
					return true;
				}
			} else if (uri.equals(s)) {
				return true;
			}
		}
		return false;
	}

	private boolean isExcludeUri(HttpServletRequest req) {
		String uri = req.getRequestURI();
		String cp = req.getContextPath();
		uri = uri.substring(cp.length());

		if (uri.length() <= 1 || uri.equals("/")) {
			return true;
		}

		for (String s : EXCLUDE_URIS) {
			if (s.lastIndexOf("/**") != -1) {
				s = s.substring(0, s.lastIndexOf("/**"));
				if (uri.indexOf(s) == 0 || s.contains(uri)) {
					return true;
				}
			} else if (uri.equals(s)) {
				return true;
			}
		}
		return false;
	}

}
