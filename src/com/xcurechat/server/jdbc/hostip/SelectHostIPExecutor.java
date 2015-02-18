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
 * The server-side RPC package, managing DB queries.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.jdbc.hostip;

import java.net.URLDecoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;
import com.xcurechat.server.jdbc.hostip.GeoLocationDataHolder;

/**
 * @author zapreevis
 * This executor selects user location by the IPV4 adress
 */
public class SelectHostIPExecutor extends QueryExecutor<GeoLocationDataHolder> {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SelectHostIPExecutor.class );
	
	private static final String IP0_TBL_PREFIX = "ip4_";

	private final Integer ip40;
	private final Integer ip41;
	private final Integer ip42;

	/**
	 * Constructor by the elements of the IP address: ip40.ip41.ip42.ip43
	 * @throws NumberFormatException if the IP address components are not proper integers
	 * 			this might happen if some one is trying to make SQL penetration or
	 * 			it is an IPV6 protocol
	 */
	public SelectHostIPExecutor( final String ip40, final String ip41, final String ip42) throws NumberFormatException {
		this.ip40 = Integer.parseInt( ip40 );
		this.ip41 = Integer.parseInt( ip41 );
		this.ip42 = Integer.parseInt( ip42 );
	}
	
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String tableA = IP0_TBL_PREFIX + ip40; 
		final String selectQuery = "SELECT cityByCountry.name, cityByCountry.state, countries.name FROM " +
	    							tableA + ", countries, cityByCountry WHERE " + 
	    							tableA + ".city = cityByCountry.city AND " +
	    							tableA + ".country = cityByCountry.country AND " + 
	    							tableA + ".country = countries.id AND b=? AND c=?";
		return connection.prepareStatement( selectQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, ip41 );
		pstmt.setInt( 2, ip42 );
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, GeoLocationDataHolder result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}
	
	public void processResultSet(ResultSet resultSet, GeoLocationDataHolder result) throws SQLException, SiteException {
		GeoLocationDataHolder geolocObj = (GeoLocationDataHolder) result;
		//Set defaults first
		geolocObj.city = "";
		geolocObj.state = "";
		geolocObj.country = "";
		//Get the actual values if any
		if( resultSet.first() ) {
			try{
				//The not-so-smart people from HOSTIP.INFO did the database entries in a strange locale
				geolocObj.city = URLDecoder.decode( resultSet.getString(1), "iso-8859-1");
				geolocObj.state = URLDecoder.decode( resultSet.getString(2), "iso-8859-1");
				geolocObj.country = URLDecoder.decode( resultSet.getString(3), "iso-8859-1");
			} catch(Exception e){
				logger.error("Unexpected exception while decoding user data", e);
			}
		}
	}

}

