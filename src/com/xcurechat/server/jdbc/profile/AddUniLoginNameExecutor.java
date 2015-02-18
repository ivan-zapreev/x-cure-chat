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
import java.sql.SQLIntegrityConstraintViolationException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.IncorrectUserDataException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;
import com.xcurechat.server.utils.LoginUnifier;

/**
 * @author zapreevis
 * This executor class is responsible for updating user profile with the unified login name.
 * If the same name already exists, then the IncorrectUserDataException exception is thrown.
 */
public class AddUniLoginNameExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( AddUniLoginNameExecutor.class ); 
	
	private final int userID;
	private final String uniLoginName;
	
	public AddUniLoginNameExecutor( final int userID, final String loginName ){
		this.userID = userID;
		this.uniLoginName = LoginUnifier.getUnifiedLogin( loginName );
	}
	
	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + USERS_TABLE + " SET " +
							 UNI_LOGIN_FIELD_NAME_USERS_TABLE + "=? " +
							 "WHERE "+ UID_FIELD_NAME_USERS_TABLE + "=?";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setString( counter++, uniLoginName );
		pstmt.setInt( counter++, userID );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		try{
			pstmt.executeUpdate();
		}catch( SQLException e ){
			if( e instanceof SQLIntegrityConstraintViolationException ){
				logger.warn( "Trying to assign the unified login name '" + uniLoginName + "' to the user " + userID + ", but the name is already in use." );
				throw new IncorrectUserDataException( IncorrectUserDataException.LOGIN_NAME_IN_USE_ERR );
			} else {
				logger.error( "An unhandled exception when adding a new user to the database." , e);
				throw e;
			}
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
