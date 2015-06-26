/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.workbench.proxy;

import javax.servlet.ServletConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles cookies for proxy servlets.
 */
public class CookieHandler {
	protected static final String COOKIE_AGE_PARAM = "cookie-max-age";

	private final String maxAge;

	protected CookieHandler(final String maxAge) {
   	 this.maxAge = maxAge;
    }
	
	protected CookieHandler(final ServletConfig config) {
		this(config.getInitParameter(COOKIE_AGE_PARAM));
	}
	
	protected String getCookieNullIfEmpty(final HttpServletRequest req, 
			final HttpServletResponse resp, final String name){
		String value = this.getCookie(req, resp, name);
		if (null !=value && value.isEmpty()){
			value = null;
		}
		return value;
	}
	
	protected String getCookie(final HttpServletRequest req, 
			final HttpServletResponse resp, final String name) {
		String value = null;
		final Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (name.equals(cookie.getName())) {
					resp.addHeader("Vary", "Cookie");
					initCookie(cookie, req);
					resp.addCookie(cookie);
					value = cookie.getValue();
					break;
				}
			}
		}
		return value;
	}
	
	private void initCookie(final Cookie cookie, 
			final HttpServletRequest req) {
		final String context = req.getContextPath();
		cookie.setPath(null == context ? "/" : context);
		if (maxAge != null) {
			cookie.setMaxAge(Integer.parseInt(maxAge));
		}
	}
	
	/**
	 * @param req servlet request
	 * @param resp servlet response
	 * @param name cookie name
	 * @param value cookie value
	 */
	protected void addNewCookie(final HttpServletRequest req, final HttpServletResponse resp, final String name, final String value)
	{
		final Cookie cookie = new Cookie(name, value);
		initCookie(cookie, req);
		resp.addCookie(cookie);
	}

	/**
	 * @return the maximum age allowed for cookies
	 */
	public String getMaxAge() {
		return maxAge;
	}
}