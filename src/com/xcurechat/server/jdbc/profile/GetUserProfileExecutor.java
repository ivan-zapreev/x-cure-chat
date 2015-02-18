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
package com.xcurechat.server.jdbc.profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.UserData;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.UserStateException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor retrieves the user profile data from the database
 */
public class GetUserProfileExecutor extends QueryExecutor<Void> {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( GetUserProfileExecutor.class );
	
	private final UserData userData;
	
	public GetUserProfileExecutor( final UserData userData ){
		this.userData = userData;
	}
	
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT *, COUNT(" + MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + ") AS " + ACTUAL_NUMBER_FORUM_POSTS_FIELD_NAME_USERS_TABLE +
								   " FROM " + USERS_TABLE + ", " + FORUM_MESSAGES_TABLE + " WHERE " +
								   UID_FIELD_NAME_USERS_TABLE + "=? AND " +
								   SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + UID_FIELD_NAME_USERS_TABLE ;
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, userData.getUID() );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//If there are users with such UID
		if( resultSet.first() ){
			logger.debug("The DB reports that the user " + userData.getUID() + " is present");
			//First get the short user data
			retrieveShortUserData( resultSet, userData, ACTUAL_NUMBER_FORUM_POSTS_FIELD_NAME_USERS_TABLE, true );
			//Then get the rest
			userData.setCityName( resultSet.getString( CITY_FIELD_NAME_USERS_TABLE ) );
			userData.setCountryName( resultSet.getString( COUNTRY_FIELD_NAME_USERS_TABLE ) );
			userData.setAboutMe( resultSet.getString( ABOUT_ME_FIELD_NAME_USERS_TABLE ) );
			userData.setFirstName( resultSet.getString( FIRST_NAME_FIELD_NAME_USERS_TABLE ) );
			userData.setLastName( resultSet.getString( LAST_NAME_FIELD_NAME_USERS_TABLE ) );
			userData.setUserAge( resultSet.getInt( AGE_FIELD_NAME_USERS_TABLE ) );
			userData.setBot( resultSet.getBoolean( IS_BOT_FIELD_NAME_USERS_TABLE ) );
			userData.setUserProfileType( resultSet.getInt( TYPE_FIELD_NAME_USERS_TABLE ) );
		} else {
			logger.warn("The DB reports that the user " + userData.getUID() + " is NOT present!");
			//There are no rows in the result set, the login-password pare is incorrect
			throw new UserStateException( UserStateException.USER_DOES_NOT_EXIST );
		}
	}

}
