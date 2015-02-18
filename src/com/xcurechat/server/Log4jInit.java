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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server;

import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;
import java.io.File;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xcurechat.server.utils.HTTPUtils;

/**
 * @author zapreevis
 * This class is used to initialize the log4j
 */
public class Log4jInit extends HttpServlet {
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	@Override
	public void init() {
		System.out.println("INFO: Trying to initialize Log4j properties for XCure-Chat server.");
		String prefix =  getServletContext().getRealPath("/");
		String file = getInitParameter("log4j-init-file");
		//If the log4j-init-file is not set, then no point in trying
		if( file != null ) {
    		PropertyConfigurator.configure( prefix + file );
    		File log = new File(".");
    		System.out.println("INFO: Log4j properties for XCure-Chat server were initialized.");
    		System.out.println("INFO: The location of the log file will be relative to the path: " + log.getAbsolutePath() );
    	} else {
    		System.err.println("ERROR: Log4j properties could not be found, the initialization is skipped.");
    	}
	}
	
	/**
	 * Push DNC context for proper log4j logging of different threads
	 * @param servletSessionId the user session id
	 * @param userSessionId the user session id
	 * @param userID the unique user id
	 */
	public static void pushDNC( final HttpServletRequest request, final String userSessionId, final int userID ) {
		NDC.push( "IP: " + HTTPUtils.getTrueRemoteAddr(userID, request) );
		NDC.push( "(U)ID: " + userID );
		final String id = ( userSessionId != null ? userSessionId : "null");
		NDC.push( "(U)session: " + id );		
	}

	/**
	 * Cleanes DNC context does pop and remove
	 */
	public static void cleanDNC() {
		NDC.pop();
		NDC.pop();
		NDC.pop();
		NDC.remove();
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res){
	}
}
