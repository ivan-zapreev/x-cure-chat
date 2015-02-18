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
 * The server-side utilities package.
 */
package com.xcurechat.server.utils;

import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author zapreevis
 * This class is supposed to contain http request related
 * utilities common to the server side RPC, servlets and etc. 
 */
public class HTTPUtils {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( HTTPUtils.class );
	
	//The name of the HTTP request header that can contain IP forwarding information
	public static final String X_FORWARDED_FOR_HEADER = "x-forwarded-for";
	
	/**
     * Read the remote IP of a servlet request. This may be in one of two
     * places. If we are behind an Apache server with mod_proxy_http, we get the
     * remote IP address from the request header x-forwarded-for. In that case
     * the remote ip of the requestor is always that of the Apache server, since
     * that is the last proxy, as per spec. If this header is missing, we're
	 * probably running locally for testing. In that case we can just use the
	 * remote IP from the request object itself.
	 * @param userID the unique user ID, just for logging purposes
	 * @param request the request passed to the servlet.
	 * @return the deduced IP address or an empty string if the address can not
	 * be resolved.
	 */
	public static String getTrueRemoteAddr(final int userID, HttpServletRequest request) {
		String hostAddress = "";
		try {
			final String headerValue = request.getHeader("X_FORWARDED_FOR_HEADER"); 
	        if ( headerValue != null) {
	        	hostAddress = InetAddress.getByName( headerValue).getHostAddress();
	        } else {
	        	hostAddress = InetAddress.getByName( request.getRemoteAddr() ).getHostAddress(); 
	        }
		} catch ( Exception ex ){
			//This is likely to be printed due to catching java.net.UnknownHostException
			logger.error("Can not resolve IP address for user '"+userID+"'", ex);
		}
        return hostAddress;
	}
}
