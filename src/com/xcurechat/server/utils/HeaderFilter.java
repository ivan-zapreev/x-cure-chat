/**
 * X-Cure-Chat
 * Copyright (C) 2013  Dr. Ivan S. Zapreev
 * www: https://nl.linkedin.com/in/zapreevis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.#
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The server-side Utils package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server.utils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author zapreevis
 * Allows to add header values for the particular type of files.
 * 
 * Adapted from: 
 * 		http://juliusdev.blogspot.com/2008/06/tomcat-add-expires-header.html
 * One of the Yahoo bests practices for Speeding Up Your Web Site is to  add an Expires
 * or a Cache-Control Header. If you use Tomcat as web-server (Tomcat 6 + NIO have good
 * performances), you can use a Servlet filter to set a far future Expires header for
 * static components like images. 
 */
public class HeaderFilter implements Filter {  
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( HeaderFilter.class );
	
	private Map<String, String> headersMap;  
	
	public void init(FilterConfig filterConfig) throws ServletException {  
		String headerParam = filterConfig.getInitParameter("header");  
		if (headerParam == null) {  
			logger.warn("No headers were found in the web.xml (init-param) for the HeaderFilter !");  
			return;  
		}  
	
		//Init the header list :  
		headersMap = new LinkedHashMap<String, String>();  
		
		if (headerParam.contains("|")) {  
			String[] headers = headerParam.split("|");  
			for (String header : headers) {  
				parseHeader(header);  
			} 	
		} else {  
			parseHeader(headerParam);  
		}  
		
		// Log configured headers .  
		if (logger.isInfoEnabled()) {  
			logger.info("The following headers were registered in the HeaderFilter :");  
			Set<Entry<String, String>> headers = headersMap.entrySet();  
			for (Entry<String, String> item : headers) {  
				logger.info(item.getKey() + ':' + item.getValue());  
			}  
		}
	}  
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {  
		if (headersMap != null) {  
			// Add the header to the response  
			Set<Entry<String, String>> headers = headersMap.entrySet();  
			for (Entry<String, String> header : headers) {  
				((HttpServletResponse) response).setHeader(header.getKey(), header.getValue());  
			}
		}  
		// Continue  
		chain.doFilter(request, response);  
	}  
	
	public void destroy() {  
		this.headersMap = null;  
	}  
	
	private void parseHeader(String header) {
		String headerName = header.substring(0, header.indexOf(":"));  
		if (!headersMap.containsKey(headerName)) {  
			headersMap.put(headerName, header.substring(header.indexOf(":") + 1));  
		}  
	}  
} 