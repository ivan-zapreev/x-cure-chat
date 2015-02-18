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

package com.xcurechat.server.jdbc.forum;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QuerySetExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for substituting the given sender ID to the DELETED sender.
 * I.e. we do not want to remove he forum messages of the deleted user, but just mark the sender as unknown.
 */
public class DeleteSenderForumMessageExecutor extends QuerySetExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteSenderForumMessageExecutor.class );

	private final int senderID;
	
	public DeleteSenderForumMessageExecutor( final int senderID ){
		this.senderID = senderID;
	}
	
	@Override
	public void executeQuerySet(Connection connection, Void result) throws SQLException, SiteException {
		//Instantiate transaction and its executor
		TransactionExecutor executor = new TransactionExecutor( new Transaction(){
			@Override
			public void execute(Statement sqlStatement, Logger logger) throws SQLException, SiteException {
				//Mark the sender as deleted
				int count = sqlStatement.executeUpdate( "UPDATE " + FORUM_MESSAGES_TABLE + " SET " +
														 SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "= ( SELECT " + UID_FIELD_NAME_USERS_TABLE + " FROM " +
														 USERS_TABLE + " WHERE " + TYPE_FIELD_NAME_USERS_TABLE + "=" + MainUserData.DELETED_USER_TYPE + " ) WHERE "+
														 SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + senderID );
				logger.info("Deleting sender " + senderID + " the sender had " + count + " forum messages" );
				
				//Mark the last reply sender as deleted
				sqlStatement.executeUpdate( "UPDATE " + FORUM_MESSAGES_TABLE + " SET " +
											LAST_SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "= ( SELECT " + UID_FIELD_NAME_USERS_TABLE + " FROM " +
						 					USERS_TABLE + " WHERE " + TYPE_FIELD_NAME_USERS_TABLE + "=" + MainUserData.DELETED_USER_TYPE + " ) WHERE "+
						 					LAST_SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + senderID );
			}
			@Override
			public String getDescription() {
				return "marking user " + senderID + " as deleted";
			}
			
		}, logger );
		executor.execute( connection );		
	}
}
