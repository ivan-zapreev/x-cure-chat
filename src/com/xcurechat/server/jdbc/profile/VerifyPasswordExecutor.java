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

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.UserLoginException;

import com.xcurechat.server.jdbc.QueryExecutor;
import com.xcurechat.server.security.bcrypt.BCrypt;

/**
 * @author zapreevis
 * This executor checks that a user with the given login and password exists
 */
public class VerifyPasswordExecutor extends QueryExecutor<Void> {
	
	private final int userID;
	private final String userPassword;
	
	public VerifyPasswordExecutor( final int userID, final String userPassword ){
		this.userID = userID;
		this.userPassword = userPassword;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT "+ PASSWORD_HASH_FIELD_NAME_USERS_TABLE +" FROM " +
									USERS_TABLE + " WHERE " +
									UID_FIELD_NAME_USERS_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, userID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//If there are users with such login name
		if( resultSet.first() ){
			final String passwordHash = resultSet.getString( PASSWORD_HASH_FIELD_NAME_USERS_TABLE );
			//If the password is correct
			if( ! BCrypt.checkpw( userPassword, passwordHash ) ){
				//The login-password do not match
				throw new UserLoginException( UserLoginException.INCORRECT_OLD_PASSWORD_ERR );
			}
		} else {
			//There are no rows in the result set, the login-password pare is incorrect
			throw new UserLoginException( UserLoginException.INCORRECT_LOGIN_PASSWORD_ERR );
		}
	}

}
