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

import com.xcurechat.client.data.MainUserData;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.UserLoginException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to retrieve the user ID but the user login
 */
public class GetUserIDByLoginExecutor extends QueryExecutor<MainUserData> {
	private final String userLoginName;
	
	/**
	 * The basic constructor
	 * @param userLoginName the user login name
	 */
	public GetUserIDByLoginExecutor( final String userLoginName ){
		this.userLoginName = userLoginName;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT " + UID_FIELD_NAME_USERS_TABLE + " FROM " +
									USERS_TABLE + " WHERE " +
									LOGIN_FIELD_NAME_USERS_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setString( 1, userLoginName );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, MainUserData userData) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, MainUserData userData) throws SQLException, SiteException {
		//If there are users with such login name
		if( resultSet.first() ){
			userData.setUID( resultSet.getInt( UID_FIELD_NAME_USERS_TABLE ));
		} else {
			//There are no rows in the result set, the login-password pare is incorrect
			throw new UserLoginException( UserLoginException.INCORRECT_LOGIN_PASSWORD_ERR );
		}
	}

}
