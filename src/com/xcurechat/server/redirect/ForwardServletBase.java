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
 * The server-side RPC package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server.redirect;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author zapreevis
 * This is a generic servlet base that is responsible for forwarding the request to the appropriate site jsp pages. 
 */
public abstract class ForwardServletBase extends HttpServlet {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ForwardServletBase.class );
	
	/**
	 * Should be implemented by a sub-class and return the corresponding redirect object
	 * @return
	 */
	protected abstract SectionRedirectHelperInt getRedirectHelper();

	/**
	 * Redirects the servlet's get request for the appropriate jsp page. 
	 */
	@Override
	public void doGet( HttpServletRequest request, HttpServletResponse response )
						throws ServletException, IOException {
		//Get the url of the required jsp
		final String relativeJSPURL = getRedirectHelper().getRelativeJSPURL( );
		
		//Forward the request to the jsp page
		logger.debug("Forwarding the servlet request to " + relativeJSPURL);
		
		//Do the actual forwarding
		ServletContext sc = this.getServletContext();
		RequestDispatcher r = sc.getRequestDispatcher( relativeJSPURL );
		r.forward( request,response );
	}
}
