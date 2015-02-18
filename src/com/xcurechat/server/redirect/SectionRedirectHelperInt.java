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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.server.utils.WebUtilities;

/**
 * @author zapreevis
 * This abstract class should be implemented by classes responsible
 * for the processing the redirect servlet parameters and then
 * creating proper JSP or GWT site version URLs from them
 */
public abstract class SectionRedirectHelperInt {
	//The jsp page extension suffix
	public static final String JSP_PAGE_EXT = ".jsp";
	
	//Get the Log4j logger object
	public static final Logger logger = Logger.getLogger( SectionRedirectHelperInt.class );

	//The name for the servlet parameter that stores the site section for the SEO redirect servlet
	public static final String SITE_SECTION_SERVLET_PARAM = "section";
	
	/**
	 * Allows to complete the site GWT url, including setting up the locale.
	 * @param req the http request to get the site url base from
	 * @param paramString the specific site section parameters
	 * @return the complete url string
	 */
	@SuppressWarnings("unchecked")
	public String completeGWTUrl( final HttpServletRequest req, final String paramString ) {
		final String baseUrl = WebUtilities.getSiteURL( req ); 
		final Map<String, String[]> params = req.getParameterMap();
		return baseUrl + ServerSideAccessManager.URL_QUERY_DELIMITER + WebUtilities.getLocaleParamString( baseUrl, params ) + "#" +
		       getSectionName() + CommonResourcesContainer.SITE_SECTION_HISTORY_DELIMITER +
		       ( ( paramString != null ) ? paramString : "" );
	}
	
	/**
	 * Allows to complete the site's jsp-servlet url, including setting up the locale.
	 * @param req the http request to get the site url base from
	 * @param paramString the specific site section parameters
	 * @return the complete url string
	 */
	@SuppressWarnings("unchecked")
	public String completeServletUrl( final HttpServletRequest req, final String paramString ) {
		final String baseUrl = WebUtilities.getSiteURL( req ); 
		final Map<String, String[]> params = req.getParameterMap();
		return baseUrl + getSectionName() + ServerSideAccessManager.URL_QUERY_DELIMITER +
			   WebUtilities.getLocaleParamString( baseUrl, params ) +
			   ( ( paramString != null ) ? (ServerSideAccessManager.SERVLET_PARAMETERS_DELIMITER + paramString) : "" );
	}
	
	/**
	 * Allows to get the URL of the jsp page that processes he requests for this site section in case JavaScrip is off.
	 * @param req the http request to get the site url base from
	 * @return the required URL to the jsp
	 */
	public String getJSPURL( final HttpServletRequest req ) {
		final String baseUrl = WebUtilities.getSiteURL( req ); 
		return baseUrl + getSectionName() + JSP_PAGE_EXT;
	}
	
	/**
	 * Allows to get the relative URL of the jsp page that processes he requests for this site section in case JavaScrip is off.
	 * @return the required URL to the jsp
	 */
	public String getRelativeJSPURL( ) {
		return ServerSideAccessManager.SERVER_CONTEXT_DELIMITER + getSectionName() + JSP_PAGE_EXT;
	}

	/**
	 * Allows to construct the dispatcher servlet url from the components
	 * @param req the http request to get the site url base from
	 * @param redirectHelperClass the class instance inherited from SectionRedirectHelperInt
	 *                            and corresponding to the site section we want to address
	 * @param urlSuffix the url suffix with the required parameters
	 * @return the complete url or null if something goes wrong or the
	 *         provided class is not a sublass of SectionRedirectHelperInt
	 */
	public static String getSiteSectionServletURL( final HttpServletRequest req,
												   final Class<?> redirectHelperClass,
												   final String urlSuffix ) {
		String result = null;
		try {
			Object object = redirectHelperClass.newInstance();
			if( object instanceof SectionRedirectHelperInt ) {
				result = ((SectionRedirectHelperInt)object).completeServletUrl( req, urlSuffix );
			}
		} catch (Exception e) {
			logger.error("Unable to construct a dispatcher URL", e);
		}
		return result;
	}	
	
	/**
	 * @return The value for the ServerSideAccessManager.SITE_SECTION_SERVLET_PARAM
	 *         servlet parameter corresponding to this site section and its helper
	 */
	public abstract String getSectionName();
	
	/**
	 * Get the GWT version of the site URL
	 * @param req the http request to get the site url base from
	 * @return the complete site URL for the GWT version
	 */
	public abstract String getSiteGWTURL(final HttpServletRequest req);
	
	/**
	 * Get the GWT version of the site URL
	 * @param req the http request to get the site url base from
	 * @return the complete site URL for the jsp-servlet version
	 */
	public abstract String getSiteServletURL(final HttpServletRequest req);
}
