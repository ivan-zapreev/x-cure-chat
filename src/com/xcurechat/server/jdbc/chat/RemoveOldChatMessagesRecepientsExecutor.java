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

package com.xcurechat.server.jdbc.chat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for deleting old chat message recipients
 */
public class RemoveOldChatMessagesRecepientsExecutor extends QueryExecutor<Void> {
	//Remove old chat messages every 1 hour, if we do it sooner then
	//Some chat message images might become invisible for the regular users
	public static final int REMOVE_CHAT_MESSAGES_RECIPIENTS_OLDER_THAN_MINUTES = 62;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( RemoveOldChatMessagesRecepientsExecutor.class );
	
	final int minutesOld;
	
	/**
	 * Delete the message recipients that are more than minutesOld minutes old
	 * @param minutesOld the number of minutes back in the past for which we keep messages intact
	 */
	public RemoveOldChatMessagesRecepientsExecutor( final int minutesOld ){
		this.minutesOld = minutesOld;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String updateQuery = "DELETE FROM " + CHAT_MSG_RECEPIENT_TABLE + " WHERE " +
									SENT_DATE_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE + "<?";
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setTimestamp(1 , new Timestamp( System.currentTimeMillis() - minutesOld * 60 * 1000 ) );
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		final int deletedMsgsCount = pstmt.executeUpdate();
		logger.debug("We have removed " + deletedMsgsCount + " old chat message recepients from the database!" );
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
