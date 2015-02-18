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
 * (C) Ivan S. Zapreev, 2010
 */

package com.xcurechat.server.jdbc.profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.utils.AvatarSpoilersHelper;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class allows to set the user attributes such as the user's treasures and its profile type
 */
public class UpdateUserAttributesExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( UpdateUserAttributesExecutor.class );
	
	//The user data to update from
	private final UserData userData;
	//For updating the online time
	private final long newOnlineTime;
	private final boolean updateOnlineTime;

	/**
	 * The basic constructor.
	 * @param userData the user data to update from
	 * @param updateOnlineTime if true then we update the user online time
	 */
	private UpdateUserAttributesExecutor( final UserData userData, final boolean updateOnlineTime, final long newOnlineTime ) {
		this.userData = userData;
		this.updateOnlineTime = updateOnlineTime;
		this.newOnlineTime = newOnlineTime;
		logger.info( "Updating the attributed of user " + userData.getUID() +
					 " in the DB, setting type = " + userData.getUserProfileType() +
					 ", gold = " + userData.getGoldPiecesCount() +
					 ", spoiler id = " + userData.getAvatarSpoilerId() +
					 ", spoiler exp date = " + userData.getAvatarSpoilerExpDate() +
					 ", #forum posts = " + userData.getSentForumMessagesCount() +
					 ", #chat msgs = " + userData.getSentChatMessagesCount() +
					 ", update online time = " + updateOnlineTime +
					 ", new online time = " + newOnlineTime + " millisec." );
	}

	/**
	 * The basic constructor.
	 * 
	 * WARNING: The online time must only be updated when the user is fully logged out
	 * 			and only with the use of the user data stored in the session manager!
	 * 
	 * @param mainUserData the main user data to update from, the data that was stored in the session manager
	 * @param updateOnlineTime if true then we update the user online time
	 */
	public UpdateUserAttributesExecutor( final MainUserData mainUserData, final boolean updateOnlineTime ) {
		this( mainUserData, updateOnlineTime,
			  ( updateOnlineTime ? ( mainUserData.getTimeOnline() + ( System.currentTimeMillis() - mainUserData.getLoginTime() ) ) : 0  ) );
	}
	
	/**
	 * The basic constructor, update the statistics but not the user online time
	 * @param userData the user data to update from
	 */
	public UpdateUserAttributesExecutor( final UserData userData ) {
		this( userData, false, 0 );
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + USERS_TABLE + " SET " +
							 TYPE_FIELD_NAME_USERS_TABLE + "=?, " +
							 GOLD_PIECES_FIELD_NAME_USERS_TABLE + "=?, " +
							 SPOILER_ID_FIELD_NAME_USERS_TABLE + "=?, " +
							 SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE + "=?, " +
							 FORUM_MSGS_COUNT_FIELD_NAME_USERS_TABLE + "=?, " +
							 CHAT_MSGS_COUNT_FIELD_NAME_USERS_TABLE + "=?" +
							 ( updateOnlineTime ? ", " + TIME_ONLINE_FIELD_NAME_USERS_TABLE + "=? " : " " ) +
							 "WHERE " + UID_FIELD_NAME_USERS_TABLE + "=?";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, userData.getUserProfileType() );
		pstmt.setInt( counter++, userData.getGoldPiecesCount() );
		//Manage the avatar spoiler
		if( AvatarSpoilersHelper.isAvatarSpoilerActive( userData ) ) {
			pstmt.setInt( counter++, userData.getAvatarSpoilerId() );
			pstmt.setTimestamp( counter++, new Timestamp( userData.getAvatarSpoilerExpDate().getTime() ) );
		} else {
			pstmt.setInt( counter++, AvatarSpoilersHelper.UNDEFILED_AVATAR_SPOILER_ID );
			pstmt.setTimestamp( counter++, null );
		}
		//Update the number of sent chat messages and the time the user spent online
		//Note that here we do update the number of forum messages the user has.
		//This is done for the sake of optimizing the top40-section search for
		//the number-forum-posts related search.
		pstmt.setInt( counter++, userData.getSentForumMessagesCount() );
		pstmt.setInt( counter++, userData.getSentChatMessagesCount() );
		if( updateOnlineTime ) {
			pstmt.setLong( counter++, newOnlineTime );
		}
		
		pstmt.setInt( counter++, userData.getUID() );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		pstmt.executeUpdate();
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
