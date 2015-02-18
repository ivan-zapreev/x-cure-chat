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
 */
package com.xcurechat.server.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

public class Configurator {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( Configurator.class );
	
	//The relative path (with the name) of the file storing the site properties
	private static final String RELATIVE_PROPERTY_FILE_PATH_AND_NAME = "WEB-INF/classes/xcure-chat.properties";
	
	//The name of the property storing the site domain names
	public static final String SITE_DOMAIN_PATTERN_PROP_NAME = "site.domain.patterns";
	//The old site domain name
	public static final String OLD_SITE_DOMAIN_PROP_NAME = "old.site.domain.name";
	//The new site domain name
	public static final String NEW_SITE_DOMAIN_PROP_NAME = "new.site.domain.name";
	//The available first-level site domains
	public static final String FIRST_LEVEL_SITE_DOMAINS_PROP_NAME = "available.first.level.domains";

	//The synchronization object
	private static final Object synchObj = new Object();
	
	//Stores the site properties
	private static Properties siteProperties = null;
	
	/**
	 * This method allows to load the site run-time properties from the property file.
	 * Should be invoked only once, the second invocation, even with other parameters,
	 * returns the same property file loaded before. This method is synchronized
	 * @param servletContext the servlet context
	 * @return the set of properties loaded from the file
	 */
	private static Properties loadSitePropertyFile(final ServletContext context) {
		synchronized( synchObj ) {
			if( siteProperties == null ) {
				if(  context != null ) {
					final String fileName = context.getRealPath("/") + RELATIVE_PROPERTY_FILE_PATH_AND_NAME;
					try {
						siteProperties = new Properties();
						logger.info("Loading the site property file from '" + fileName + "'");
						siteProperties.load( new FileInputStream( fileName ) );
				    } catch (IOException e) {
				    	logger.error("The site property file '" + fileName + "' could not be read", e );
				    }
				} else {
					logger.error("Can not load the site property file because the provided servlet context is null");
				}
			}
			return siteProperties;
		}
	}
	
	/**
	 * Allows to get a value of a particular site property, note that before calling
	 * this method one should call the method loading the properties from the file first. 
	 * @param propertyName the name of the property that we want to load
	 * @return the value of the property or null if the property could not be loaded 
	 */
	public static String getProperty( final ServletContext context, final String propertyName ) {
		//Load the property file is needed
		loadSitePropertyFile( context );
		
		//get the property from the file
		String result = null;
		if( siteProperties != null ) {
			result = siteProperties.getProperty( propertyName );
			logger.debug("The value of the site property '" + propertyName + "' is '" + result + "'");
		} else {
			logger.error("The site property '" + propertyName + "' can not be retrieved because the property file is not loaded");
		}
		return result ;
	}

	/**
	 * Allows to get the value of the site domain name pattern
	 * @return the site domain name pattern or null
	 */
	public static String getSiteDomainPattern(final ServletContext context) {
		return getProperty( context, SITE_DOMAIN_PATTERN_PROP_NAME );
	}

	/**
	 * Allows to get the value of the site domain name pattern
	 * @return the site domain name pattern or null
	 */
	public static String getOldSiteDomainName(final ServletContext context) {
		return getProperty( context, OLD_SITE_DOMAIN_PROP_NAME );
	}

	/**
	 * Allows to get the new site domain name or null if there was not change in the site domain name
	 * @return the site domain name pattern or null
	 */
	public static String getNewSiteDomainName(final ServletContext context) {
		return getProperty( context, NEW_SITE_DOMAIN_PROP_NAME );
	}

	/**
	 * Allows to get the | separated list of site's first level domains
	 * @return the site domain name pattern or null
	 */
	public static String getSiteFirstLevelDomains(final ServletContext context) {
		return getProperty( context, FIRST_LEVEL_SITE_DOMAINS_PROP_NAME );
	}
}
