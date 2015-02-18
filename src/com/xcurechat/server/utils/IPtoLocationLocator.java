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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.utils;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.hostip.GeoLocationDataHolder;
import com.xcurechat.server.jdbc.hostip.SelectHostIPExecutor;

/**
 * @author zapreevis
 * This class is supposed to help with mapping ip addresses to geolocations 
 */
public class IPtoLocationLocator {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( IPtoLocationLocator.class );
	
	/**
	 * Allows to retrieve geolocation by an IP address.
	 * @param host the host's IP address
	 * @return the geolocation or an empty string for not-known location: city, state, country
	 */
	public static String getLocationbyIP(final String host){
		//Define needed parts of the IP address 
		String ip40="", ip41="", ip42="";
		StringTokenizer tokenizer = new StringTokenizer(host, "./", false);
		if(tokenizer.hasMoreTokens()) {
			ip40 = tokenizer.nextToken();
			if( tokenizer.hasMoreTokens() ) {
				ip41 = tokenizer.nextToken();
				if( tokenizer.hasMoreTokens() ) {
					ip42 = tokenizer.nextToken();
				}
			}
		}
		logger.debug("Retrieving geolocation for the IPV4 host address: " + host +
					" tokenized as ip40="+ip40+", ip41="+ip41+", ip42="+ip42);
		
		//Retrieve host location from the database by its IPV4 address
		GeoLocationDataHolder data = new GeoLocationDataHolder();
		try {
			SelectHostIPExecutor executor = new SelectHostIPExecutor(ip40, ip41, ip42);
			ConnectionWrapper<GeoLocationDataHolder> getGeoLocationConnWrap = ConnectionWrapper.createConnectionWrapper( executor );
			getGeoLocationConnWrap.executeQuery(data, ConnectionWrapper.XCURE_HOSTIP_DB);
		} catch (Exception e){
			logger.error("Error while retrieving the geolocation for the IP address: "+host, e);
			data.city = "";
			data.state = "";
			data.country = "";
		}
		
		return data.city+(data.state.isEmpty()?"":", "+data.state)+(data.country.isEmpty()?"":", "+data.country);
	}
}
