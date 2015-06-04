
package com.efficio.fieldbook.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.commons.hibernate.DynamicManagerFactoryProviderConcurrency;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class FieldbookHandlerInterceptor implements HandlerInterceptor {

	@Autowired
	private WorkbenchDataManagerImpl workbenchDataManager;

	@Autowired
	private DynamicManagerFactoryProviderConcurrency managerFactoryProvider;

	public FieldbookHandlerInterceptor() {

	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

		this.managerFactoryProvider.close();
	}

}
