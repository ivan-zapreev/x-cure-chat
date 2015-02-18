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

import java.util.Locale;
import java.io.IOException;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xcurechat.server.security.captcha.CaptchaServiceManager;

import com.xcurechat.client.data.UserData;

/**
 * @author zapreevis
 * 
 * NOTE: This class should not do session access validation using
 * {\ref StatisticsSecurityManager } because then the CAPTHCA images
 * in the user login dialog are blocked. The latter looks ugly.  
 * 
 * This servlet is responsible for retrieving the CAPTCHA problem image
 * Once the image is retrieved, it is removed from the hash and can not
 * be tetrieved again.
 */
public class CaptchaImageServlet extends HttpServlet {
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( CaptchaImageServlet.class );
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		Log4jInit.pushDNC( request, null, UserData.UNKNOWN_UID );
		logger.info( "Retrieving captcha problem by the servlet session." );
				
		try{
			ServletOutputStream responseOutputStream = response.getOutputStream();
			String sessionId = request.getSession(true).getId();
			Locale clientLocale = request.getLocale();
			
			byte[] theCaptchaProblem = CaptchaServiceManager.getCaptchaProblem( sessionId, clientLocale );
			
			if( theCaptchaProblem != null ){
				response.setHeader( "Cache-Control", "no-store" );
				response.setHeader( "Pragma", "no-cache" );
				response.setDateHeader( "Expires", 0 );
				response.setContentType( CaptchaServiceManager.CAPTCHA_IMAGE_MIME_FORMAT.getMainMimeType() );
				
				responseOutputStream.write(theCaptchaProblem);
				responseOutputStream.flush();
				responseOutputStream.close();
			} else {
				logger.error( "The servlet could not retrieve/generate CAPTCHA problem!" );
				throw new ServletException("Could not generate CAPTCHA problem!\n");
			}
		} finally {
			Log4jInit.cleanDNC();
		}
	}
}
