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
package com.xcurechat.server.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * This class contains several useful web-utility methods.
 */
public class WebUtilities {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( WebUtilities.class );
	
	//The type of encoding to use for the url component encoding
	public static final String ENCODING_TYPE = "UTF-8";

	public static final String LOCALE_SERVLET_PARAMETER_NAME = "locale";

	public static final String LOCALE_PARAMETER_VALUE_ENGLISH = "en";

	public static final String LOCALE_PARAMETER_VALUE_RUSSIAN = "ru";
	
	//Ending with "/" because it is for local checks only
	private static final String URL_FIRST_LEVEL_DOMAIN_RUSSIAN = ".ru" + ServerSideAccessManager.SERVER_CONTEXT_DELIMITER;
	
	/*Use as static only*/
	private WebUtilities() {}
	
	/**
 	 * This methods is made to mimic the URL.encodeComponent(String) of GWT.
 	 * When we do encoding we code it the same way as GWT does, first into
	 * UTF-8 and then each "%" is replaced by its Percent encoding I.e.
	 * String->UTF-8->"All % into %25". See also http://en.wikipedia.org/wiki/Percent-encoding.
	 */
	public static String encodeGWTURLComponent(final String value) {
		String result = null;
		try {
			result = URLEncoder.encode( value, ENCODING_TYPE ).replaceAll("%", "%25");
		} catch (Exception e) {
			logger.error("Unable to encode the URL component value '"+value+"'", e);
		}
		logger.debug("Encoded '"+value+"' into '"+result + "'");
		return result;
	}
		
	/**
 	 * This methods is made to mimic the URL.decodeComponent(String) of GWT.
	 * When decoding values we do not need to replace "%25" with "%" because
	 * this is done when parsing the URL so we have to decode the string which
	 * is purely UTF-8. 
	 */
	public static String decodeGWTURLComponent(final String value) {
		String result = null;
		try {
			//The following line is commented out, see the comments why.
			//result = URLDecoder.decode( value.replaceAll("%25", "%"), ENCODING_TYPE );
			result = URLDecoder.decode( value, ENCODING_TYPE );
		} catch (Exception e) {
			logger.error("Unable to decode the URL component value '"+value+"'", e);
		}
		logger.debug("Decoded '"+value+"' into '"+result + "'");
		return result;
	}		

	/**
	 * Allows to reconstruct the site URL from the request parameters
	 * @param req the request
	 * @return the name of the website ending with the slash "/"
	 */
	public static String getSiteURL( final HttpServletRequest req ) {
		//Example: http://hostname.com:80/mywebapp/servlet/MyServlet/a/b;c=123?d=789 
		String scheme = req.getScheme();             // http
		String serverName = req.getServerName();     // hostname.com
		int serverPort = req.getServerPort();        // 80
		String contextPath = req.getContextPath();   // /mywebapp
		
		//One could get other parameters as well, but here we do not need them:
		//String servletPath = req.getServletPath(); // /servlet/MyServlet 
		//String pathInfo = req.getPathInfo();       // /a/b;c=123 
		//String queryString = req.getQueryString(); // d=789 
		
		//Manage the old and new domain names if defined.
		serverName = reqriteServerName( req.getSession().getServletContext(), serverName );
		
		//To not put the port in case it is the default 80 port for http
		return scheme+"://" + serverName + (serverPort != 80 ? ":" + serverPort : "" ) +
		       contextPath + ServerSideAccessManager.SERVER_CONTEXT_DELIMITER;
	}
	
	/**
	 * In case the server domain name was changed, one can ensure that the old
     * domain name gets rewritten into a proper new one by just calling this mehtod
	 * @param context the servlet context
	 * @param oldServerName the current server name, the one in the servlet request
	 * @return the rewritten name into the new domain, in case the old and new
	 *         domain names are specified in the site property file.  
	 */
	private static String reqriteServerName( final ServletContext context, final String oldServerName ) {
		final String oldDomainName = Configurator.getOldSiteDomainName( context );
		final String newDomainName = Configurator.getNewSiteDomainName( context );
		final String firstLevelDomainName = Configurator.getSiteFirstLevelDomains( context );
		
		String newServerName = oldServerName;
		
		if( oldDomainName != null && newDomainName != null && firstLevelDomainName != null ) {
			//We need to check the domain name and rewrite it if needed
			if( newServerName.contains( oldDomainName ) ) {
				//In case we have an old domain name in the request, rewrite it into the new one
				newServerName = newServerName.replaceAll( oldDomainName , newDomainName );
				
				//Then check that the first level domain we have is allowed, if not then rewrite it
				String[] firstLevelDomains = firstLevelDomainName.split("\\|");
				if( firstLevelDomains.length > 0 ) {
					boolean isValidFirstLevelDomain = false; int index = 0; 
					while( (index < firstLevelDomains.length ) && !isValidFirstLevelDomain ) {
						isValidFirstLevelDomain = newServerName.endsWith( firstLevelDomains[index] );
						index++;
					}
					//Now if the first level domain is not recognized as a valid one
					if( ! isValidFirstLevelDomain ) {
						//Replace the domain name
						String[] domainNameSequence = newServerName.split("\\.");
						newServerName = "";
						for( index=0; index < ( domainNameSequence.length - 1 ); index++ ) {
							newServerName +=  domainNameSequence[index] + ( index != (domainNameSequence.length - 2) ? "." : "" );
						}
						//We do not know which domain will be better, we could guess but actually
						//it does not harm if we just use the first one in the sequence.
						newServerName += firstLevelDomains[1];
					}
				} else {
					logger.warn("The number of the first-level site-domains array is zerro, keeping the old first level domain!");
				}
			}
			logger.debug("The server name '" + oldServerName + "' was rewritten into '" + newServerName + "'");
		} else {
			logger.debug("The old and new server domain names or the first level domain names are not set, no server name rewriting will be done");
		}
		
		return newServerName;
	}
	
	/**
	 * Allows to get the locale from the servlet request parameters
	 * @param params the servlet request parameters
	 * @return the locale if it is present or an empty string if it is not
	 */
	private static String getLocaleValue( final Map<String, String[]> params ) {
		String[] values = params.get( LOCALE_SERVLET_PARAMETER_NAME );
		if( values != null ) {
			return values[0];
		} else {
			return "";
		}
	}

	/**
	 * Allows to get the locale parameter value
	 * @param req the request
	 * @return the locale value
	 */
	@SuppressWarnings("unchecked")
	public static String getLocaleValue( final HttpServletRequest req ) {
		return getLocaleValue( getSiteURL( req ), req.getParameterMap() );
	}
	
	/**
	 * Allows to get the locale parameter value
	 * @param baseUrl the url of the website we are at
	 * @param params the servlet request parameters
	 * @return the locale value
	 */
	public static String getLocaleValue( final String baseUrl, final Map<String, String[]> params ) {
		String localeValue = getLocaleValue( params );
		if( localeValue.trim().isEmpty() ) {
			//If the locale is not set
			if( baseUrl.endsWith( URL_FIRST_LEVEL_DOMAIN_RUSSIAN ) ) {
				//If we are on the .ru version of the website
				localeValue = LOCALE_PARAMETER_VALUE_RUSSIAN ;
			} else {
				//If we are on the non-.ru version of the website
				localeValue = LOCALE_PARAMETER_VALUE_ENGLISH ;
			}
		}
		return localeValue;
	}

	/**
	 * Allows to construct the proper locale parameter for the URL
	 * @param req the request
	 * @return the proper locale string
	 */
	@SuppressWarnings("unchecked")
	public static String getLocaleParamString( final HttpServletRequest req ) {
		return getLocaleParamString( getSiteURL( req ), req.getParameterMap() );
	}
	
	/**
	 * Allows to construct the proper locale parameter for the URL
	 * @param baseUrl the url of the website we are at
	 * @param params the servlet request parameters
	 * @return the proper locale string
	 */
	public static String getLocaleParamString( final String baseUrl, final Map<String, String[]> params ) {
		return LOCALE_SERVLET_PARAMETER_NAME +
			   ServerSideAccessManager.SERVER_PARAM_NAME_VAL_DELIM +
			   getLocaleValue(baseUrl, params );
	}

}
