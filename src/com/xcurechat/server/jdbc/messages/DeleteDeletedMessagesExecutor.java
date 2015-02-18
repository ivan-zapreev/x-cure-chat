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

package com.xcurechat.server.jdbc.messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for deleting private (personal) messages
 * That are either (hiden for the sender or routed for the deleted sender)
 * and (hiden for the receiver or routed for the deleted receiver)
 * In other words they can not be read by anyone any more.
 */
public class DeleteDeletedMessagesExecutor extends QueryExecutor<Void> {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteDeletedMessagesExecutor.class ); 
			
	//The selector query for the 'deleted' user profile
	private static final String DELETED_USER_PROFILE_UID_SELECT = "( SELECT " + UID_FIELD_NAME_USERS_TABLE + " FROM " +
																	USERS_TABLE + " WHERE " + TYPE_FIELD_NAME_USERS_TABLE + "=" +
																	MainUserData.DELETED_USER_TYPE + " )";

	public DeleteDeletedMessagesExecutor( ){
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String updateQuery = "DELETE FROM " + MESSAGES_TABLE + " WHERE ( "+
										FROM_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=true OR "+
										FROM_UID_FIELD_NAME_MESSAGES_TABLE + " = " + DELETED_USER_PROFILE_UID_SELECT +
									" ) AND ( " +
										TO_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=true OR "+
										TO_UID_FIELD_NAME_MESSAGES_TABLE + " = " + DELETED_USER_PROFILE_UID_SELECT +
									" )";
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		//THERE IS NOTHING TO BIND
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		int numberDeleted = pstmt.executeUpdate();
		logger.debug("Deleted " + numberDeleted + " unreachable private (personal) messages");
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
