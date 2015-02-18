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
 * The server-side RPC package, managing connections and connection pool.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Date;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.server.security.bcrypt.BCrypt;

/**
 * @author zapreevis
 * The base class for query executors, contains common methods and constants
 * This classes' children are meant to be used with the ConnectionWrapper
 */
public abstract class ExecutorBase<ParamReturnObjectType> {
	
	//For the table where users are stored 
	public static final String USERS_TABLE = "users";
	public static final String UID_FIELD_NAME_USERS_TABLE = "uid";
	public static final String LOGIN_FIELD_NAME_USERS_TABLE = "login";
	public static final String UNI_LOGIN_FIELD_NAME_USERS_TABLE = "uni_login";
	public static final String PASSWORD_HASH_FIELD_NAME_USERS_TABLE = "password_hash";
	public static final String GENDER_FIELD_NAME_USERS_TABLE = "gender";
	public static final String AGE_FIELD_NAME_USERS_TABLE = "age";
	public static final String FIRST_NAME_FIELD_NAME_USERS_TABLE = "first_name";
	public static final String LAST_NAME_FIELD_NAME_USERS_TABLE = "last_name";
	public static final String COUNTRY_FIELD_NAME_USERS_TABLE = "country";
	public static final String CITY_FIELD_NAME_USERS_TABLE = "city";
	public static final String ABOUT_ME_FIELD_NAME_USERS_TABLE = "aboutMe";
	public static final String TYPE_FIELD_NAME_USERS_TABLE = "type";
	public static final String GOLD_PIECES_FIELD_NAME_USERS_TABLE = "gold_pieces";
	public static final String REG_DATE_FIELD_NAME_USERS_TABLE = "reg_date";
	public static final String LAST_ONLINE_FIELD_NAME_USERS_TABLE = "last_online";
	public static final String IS_ONLINE_FIELD_NAME_USERS_TABLE = "is_online";
	public static final String IS_BOT_FIELD_NAME_USERS_TABLE = "is_bot";
	public static final String SPOILER_ID_FIELD_NAME_USERS_TABLE = "spoiler_id";
	public static final String SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE = "spoiler_exp_date";
	public static final String FORUM_MSGS_COUNT_FIELD_NAME_USERS_TABLE = "num_forum_posts";
	public static final String CHAT_MSGS_COUNT_FIELD_NAME_USERS_TABLE = "num_chat_msgs";
	public static final String TIME_ONLINE_FIELD_NAME_USERS_TABLE = "time_on_site";
	//Is meant to be used for a temporary field in the select statements
	//for retrieving the actual count for the user-forum posts, i.e. not
	//the FORUM_MSGS_COUNT_FIELD_NAME_USERS_TABLE column value, but the
	//result of the COUNT(...) query. 
	public static final String ACTUAL_NUMBER_FORUM_POSTS_FIELD_NAME_USERS_TABLE = "actual_" + FORUM_MSGS_COUNT_FIELD_NAME_USERS_TABLE + "_tmp";
	
	//For the table where profile files are stored 
	public static final String PROFILE_FILES_TABLE = "profile_files"; 
	public static final String FILE_ID_FIELD_PROFILE_FILES_TABLE = "fileID";
	public static final String OWNER_ID_PROFILE_FILES_TABLE = "uid";
	public static final String THUMBNAIL_FIELD_PROFILE_FILES_TABLE = "thumbnail";
	public static final String DATA_FIELD_PROFILE_FILES_TABLE = "data";
	public static final String MIME_TYPE_PROFILE_FILES_TABLE  = "mimeType";
	public static final String FILE_NAME_PROFILE_FILES_TABLE  = "fileName";
	public static final String IMG_WIDTH_PROFILE_FILES_TABLE  = "widthPixels";
	public static final String IMG_HEIGHT_PROFILE_FILES_TABLE  = "heightPixels";
	public static final String UPLOAD_DATE_PROFILE_FILES_TABLE = "uploadDate";
	
	//For the table where profile avatar images are stored 
	public static final String AVATAR_IMAGES_TABLE = "avatar_images"; 
	public static final String USER_ID_AVATAR_IMAGES_TABLE = "uid";
	public static final String IMAGE_FIELD_AVATAR_IMAGES_TABLE = "image";
	public static final String MIME_TYPE_AVATAR_IMAGES_TABLE = "mimeType";

	//For the table where the user login/logout statistics is stored
	public static final String LOGIN_STATS_TABLE = "login_out_statistics";
	public static final String USER_ID_PROFILE_LOGIN_STATS_TABLE = "uid";
	public static final String IS_LOGIN_FIELD_NAME_LOGIN_STATS_TABLE = "is_login";
	public static final String IS_AUTO_FIELD_NAME_LOGIN_STATS_TABLE = "is_auto";
	public static final String DATE_FIELD_NAME_LOGIN_STATS_TABLE = "date";
	public static final String HOST_FIELD_NAME_LOGIN_STATS_TABLE = "host";
	public static final String LOCATION_FIELD_NAME_LOGIN_STATS_TABLE = "location";

	//For the table that contains room descriptions
	public static final String ROOMS_TABLE = "rooms";
	public static final String RID_FIELD_NAME_ROOMS_TABLE = "rid";
	public static final String NAME_FIELD_NAME_ROOMS_TABLE = "name";
	public static final String DESC_FIELD_NAME_ROOMS_TABLE = "description";
	public static final String UID_FIELD_NAME_ROOMS_TABLE = "uid";
	public static final String OWNER_NAME_FIELD_NAME_ROOMS_TABLE = "login";
	public static final String IS_PERM_FIELD_NAME_ROOMS_TABLE = "is_permanent";
	public static final String IS_MAIN_FIELD_NAME_ROOMS_TABLE = "is_main";
	public static final String EXP_DATE_FIELD_NAME_ROOMS_TABLE = "expires";
	public static final String TYPE_FIELD_NAME_ROOMS_TABLE = "type";
	public static final String VISITORS_FIELD_NAME_ROOMS_TABLE = "visitors";
	
	//For the table where the user/room access rights are described
	public static final String ROOM_ACCESS_TABLE = "rooms_access";
	public static final String RAID_FIELD_NAME_ROOM_ACCESS_TABLE = "raid";
	public static final String RID_FIELD_NAME_ROOM_ACCESS_TABLE = "rid";
	public static final String UID_FIELD_NAME_ROOM_ACCESS_TABLE = "uid";
	public static final String LOGIN_FIELD_NAME_ROOM_ACCESS_TABLE = "login";
	public static final String IS_SYSTEM_FIELD_NAME_ROOM_ACCESS_TABLE = "is_system";
	public static final String IS_READ_FIELD_NAME_ROOM_ACCESS_TABLE = "is_read";
	public static final String IS_READ_ALL_FIELD_NAME_ROOM_ACCESS_TABLE = "is_read_all";
	public static final String READ_ALL_EXP_DATE_FIELD_NAME_ROOM_ACCESS_TABLE = "read_all_expires";
	public static final String IS_WRITE_FIELD_NAME_ROOM_ACCESS_TABLE = "is_write";
	
	//For the table where we store the friends relationship
	public static final String FRIENDS_TABLE = "friends";
	public static final String FROM_UID_FIELD_NAME_FRIENDS_TABLE = "uid_from";
	public static final String TO_UID_FIELD_NAME_FRIENDS_TABLE = "uid_to";
	
	//For the table where we store private user messages
	public static final String MESSAGES_TABLE = "private_messages";
	public static final String MSG_ID_FIELD_NAME_MESSAGES_TABLE = "mid";
	public static final String FROM_UID_FIELD_NAME_MESSAGES_TABLE = "uid_from";
	public static final String TO_UID_FIELD_NAME_MESSAGES_TABLE = "uid_to";
	public static final String FROM_HIDDEN_FIELD_NAME_MESSAGES_TABLE = "from_hidden";
	public static final String TO_HIDDEN_FIELD_NAME_MESSAGES_TABLE = "to_hidden";
	public static final String IS_READ_FIELD_NAME_MESSAGES_TABLE = "is_read";
	public static final String SENT_DATE_TIME_FIELD_NAME_MESSAGES_TABLE = "sent_date_time";
	public static final String MSG_TYPE_FIELD_NAME_MESSAGES_TABLE = "msg_type";
	public static final String MSG_TITLE_FIELD_NAME_MESSAGES_TABLE = "msg_title";
	public static final String MSG_BODY_FIELD_NAME_MESSAGES_TABLE = "msg_body";
	public static final String ROOM_ID_FIELD_NAME_MESSAGES_TABLE = "room_id";
	
	//For the chat messages table where we store all chat messages of all rooms
	public static final String CHAT_MESSAGES_TABLE = "chat_messages";
	public static final String MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE = "messageID";
	public static final String SENT_DATE_FIELD_NAME_CHAT_MESSAGES_TABLE = "sentDate";
	public static final String SENDER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE = "senderID";
	public static final String ROOM_ID_FIELD_NAME_CHAT_MESSAGES_TABLE = "roomID";
	public static final String FILE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE = "fileID";
	public static final String MESSAGE_BODY_FIELD_NAME_CHAT_MESSAGES_TABLE = "messageBody";
	public static final String MESSAGE_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE = "messageType";
	public static final String INFO_USER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE = "infoUserID";
	public static final String INFO_USER_LOGIN_FIELD_NAME_CHAT_MESSAGES_TABLE = "infoUserLogin";
	public static final String FONT_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE = "fontType";
	public static final String FONT_SIZE_FIELD_NAME_CHAT_MESSAGES_TABLE = "fontSize";
	public static final String FONT_COLOR_FIELD_NAME_CHAT_MESSAGES_TABLE = "fontColor";
	
	//For the table that stores the chat message files
	public static final String CHAT_FILES_TABLE = "chat_files";
	public static final String FILE_ID_FIELD_NAME_CHAT_FILES_TABLE = "fileID";
	public static final String OWNER_ID_FIELD_NAME_CHAT_FILES_TABLE = "ownerID";
	public static final String UPLOAD_DATE_FIELD_NAME_CHAT_FILES_TABLE = "uploadDate";
	public static final String MIME_TYPE_CHAT_FILES_TABLE  = "mimeType";
	public static final String FILE_NAME_CHAT_FILES_TABLE  = "fileName";
	public static final String THUMBNAIL_FIELD_NAME_CHAT_FILES_TABLE = "thumbnail";
	public static final String DATA_FIELD_NAME_CHAT_FILES_TABLE = "data";
	public static final String IMG_WIDTH_CHAT_FILES_TABLE  = "widthPixels";
	public static final String IMG_HEIGHT_CHAT_FILES_TABLE  = "heightPixels";
	public static final String IS_PUBLIC_MESSAGE_FILE_CHAT_FILES_TABLE  = "is_public";
	public static final String MD5_SUM_CHAT_FILES_TABLE  = "md5";
	
	//For the table that stores private chat message recipients
	public static final String CHAT_MSG_RECEPIENT_TABLE = "chat_msg_recepient";
	public static final String SENT_DATE_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE = "sentDate";
	public static final String ENTRY_ID_CHAT_MSG_RECEPIENT_TABLE = "cmrID";
	public static final String MESSAGE_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE = "messageID";
	public static final String USR_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE = "recepientID";
	
	//For the table that stores the forum messages
	public static final String FORUM_MESSAGES_TABLE = "forum_messages";
	public static final String MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE = "messageID";
	public static final String MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE = "messagePathID";
	public static final String PARENT_MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE = "parentMessageID";
	public static final String SENT_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE = "sentDate";
	public static final String UPDATE_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE = "updateDate";
	public static final String SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE = "senderID";
	public static final String MESSAGE_TITLE_FIELD_NAME_FORUM_MESSAGES_TABLE = "messageTitle";
	public static final String MESSAGE_BODY_FIELD_NAME_FORUM_MESSAGES_TABLE = "messageBody";
	public static final String IS_VISIBLE_FIELD_NAME_FORUM_MESSAGES_TABLE = "is_system";
	public static final String IS_APPROVED_FIELD_NAME_FORUM_MESSAGES_TABLE = "is_approved";
	public static final String NUMBER_OF_VOTES_FIELD_NAME_FORUM_MESSAGES_TABLE = "numVotes";
	public static final String VOTE_VALUE_FIELD_NAME_FORUM_MESSAGES_TABLE = "voteValue";
	public static final String NUM_REPLIES_FIELD_NAME_FORUM_MESSAGES_TABLE = "numReplies";
	public static final String LAST_SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE = "stLastSenderID";
	public static final String LAST_REPLY_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE = "stLastPostDate";
	
	//For the table that stores the forum message votes
	public static final String FORUM_MESSAGE_VOTES_TABLE = "forum_message_votes";
	public static final String MESSAGE_ID_FIELD_FORUM_MESSAGE_VOTES_TABLE = "messageID";
	public static final String SENDER_ID_FIELD_FORUM_MESSAGE_VOTES_TABLE = "senderID";
	public static final String VOTE_VALUE_FIELD_FORUM_MESSAGE_VOTES_TABLE = "voteValue";
	
	
	//For the table of forum files
	public static final String FORUM_FILES_TABLE = "forum_files";
	public static final String FILE_ID_FIELD_NAME_FORUM_FILES_TABLE = "fileID";
	public static final String MESSAGE_ID_FIELD_NAME_FORUM_FILES_TABLE = "messageID";
	public static final String OWNER_ID_FIELD_NAME_FORUM_FILES_TABLE = "ownerID";
	public static final String MIME_TYPE_FIELD_NAME_FORUM_FILES_TABLE = "mimeType";
	public static final String FILE_NAME_FIELD_NAME_FORUM_FILES_TABLE = "fileName";
	public static final String FILE_THUMBNAIL_FIELD_NAME_FORUM_FILES_TABLE = "thumbnail";
	public static final String DATA_FIELD_NAME_FORUM_FILES_TABLE = "data";
	public static final String IMG_WIDTH_PIXELS_NAME_FORUM_FILES_TABLE = "widthPixels";
	public static final String IMG_HEIGHT_PIXELS_NAME_FORUM_FILES_TABLE = "heightPixels";
	public static final String UPLOAD_DATE_FIELD_NAME_FORUM_FILES_TABLE = "uploadDate";
	
	/**
	 * Hashes the user password using BCrypt
	 * @param userPassword the user password
	 * @return the user-password hash
	 */
	protected final static String hashPassword(String userPassword){
		return BCrypt.hashpw( userPassword, BCrypt.gensalt() );
	}

	/**
	 * This method allows to set the time stamp value into the SQL prepared statement  
	 * @param pstmt the prepared statement to set date in
	 * @param index the index at which the parameter has to be binded 
	 * @param timeInHours the time duration in houws starting from now
	 * @return the java.utilData object that contains the expiration Data
	 * @throws SQLException if smth goes wrong when binding the parameter
	 * into the prepared statement.
	 */
	public static Date setTime(PreparedStatement pstmt, final int index, final int timeInHours ) throws SQLException {
		long timeMillisec = System.currentTimeMillis();
		timeMillisec += timeInHours * 60 * 60 * 1000;
		final Timestamp timestamp = new Timestamp(timeMillisec);
		
		pstmt.setTimestamp( index, timestamp );
		
		return new Date( timestamp.getTime() );
	}

	/**
	 * This method allows to set the time stamp value into the SQL prepared statement  
	 * @param pstmt the prepared statement to set date in
	 * @param index the index at which the parameter has to be binded 
	 * @param dateTime the time to set
	 * @throws SQLException if smth goes wrong when binding the parameter
	 * into the prepared statement.
	 */
	public static void setTime(PreparedStatement pstmt, final int index, final Date dateTime ) throws SQLException {
		pstmt.setTimestamp( index, new Timestamp( dateTime.getTime() ) );
	}
	
	/**
	 * This method allows to get the time stamp from the result set and then to convert it into time
	 * @param resultSet the result set to work with
	 * @param entryName the name of the field that is the Timestamp field we want to retrieve
	 * @return the Date corresponding to the retrieved Timestamp, or null if the Timestamp is null
	 * @throws SQLException if smth goes wrong when retrieving the Timestamp
	 * from the result set.
	 */
	public static Date getTime( final ResultSet resultSet, final String entryName ) throws SQLException {
		final Timestamp time_stamp = resultSet.getTimestamp( entryName );
		final Date exp_date;
		if( time_stamp != null ){
			exp_date = new Date( time_stamp.getTime() ); 
		} else {
			exp_date = null;
		}
		return exp_date;
	}
	
	//The constants nedded for retrieving the chat message recipients
	public static final String MESSAGE_RECEPIENT_IDS = "message_recepient_ids";
	public static final String MESSAGE_RECEPIENT_IDS_DELIMITER = ":";
	
	/**
	 * This method allows to get chat message recipients from the result set
	 * @param resultSet the result set to get them from
	 * @param messageID the id of the message for which we get the recipients
	 *                  this parameter is only used for logging purposes
	 * @param logger the logger to write the log info to
	 * @return returns the ordered set of the message recipient ids
	 * @throws SQLException if smth goes wrong when working with the result set
	 */
	public static LinkedHashSet<Integer> getChatMessageRecipients( ResultSet resultSet, final int messageID,
																   final Logger logger ) throws SQLException {
		LinkedHashSet<Integer> recipientIDs = new LinkedHashSet<Integer>();
		//Get and process message recipient IDs
		String messageRecepientIDs = resultSet.getString( MESSAGE_RECEPIENT_IDS );
		logger.debug( "Recepient ID string for message " + messageID + " is: " + messageRecepientIDs );
		if( messageRecepientIDs != null ) {
			String[] ids = messageRecepientIDs.split( MESSAGE_RECEPIENT_IDS_DELIMITER );
			for( int i = 0; i < ids.length; i++ ) {
				try{
					recipientIDs.add( Integer.parseInt( ids[i] ) );
				}catch( NumberFormatException e){
					logger.error("Unable to parse the chat message recepient ID " + ids[i], e);
				}
			}
		}
		logger.debug( "Recepient IDs for message " + messageID + " are: " + recipientIDs );
		return recipientIDs;
	}
	
	/**
	 * Allows to create a string: "IN ( a, b, c)" where a,b,c are the ids from the provided list
	 * @param collectionOfIds the provided list of IDs {a,b,c}
	 * @return the "IN ( a, b, c)" part of the query
	 */
	public static String createINQuerySet( final Collection<Integer> collectionOfIds) {
		String query = " IN (";
		int current_count = 0;
		for( Integer element : collectionOfIds ) {
			query += element.toString();
			if( (++current_count) < collectionOfIds.size() ) {
				query += ",";
			}
		}
		return query += ")";
	}
	
	/**
	 * Allows to get the short user data fields from the result set
	 * @param resultSet the result set to get the results from
	 * @param shortUserData the short user data object that will be filled out with the data
	 * @param forumMsgCountColumnName the name of the column from which we should retrieve the number of user's forum messages
	 * @param isRetrieveLoggedInStatus if true then we retrieve the user logged-on status, if false then we do not.
	 * @throws SQLException an exception if something goes wrong
	 */
	public void retrieveShortUserData( final ResultSet resultSet, final ShortUserData shortUserData,
									   final String forumMsgCountColumnName, final boolean isRetrieveLoggedInStatus ) throws SQLException {
		shortUserData.setUID( resultSet.getInt( UID_FIELD_NAME_USERS_TABLE ) );
		shortUserData.setUserLoginName( resultSet.getString( LOGIN_FIELD_NAME_USERS_TABLE ) );
		shortUserData.setMale( resultSet.getBoolean( GENDER_FIELD_NAME_USERS_TABLE ) );
		shortUserData.setUserRegistrationDate( getTime( resultSet, REG_DATE_FIELD_NAME_USERS_TABLE ) );
		shortUserData.setUserLastOnlineDate( getTime( resultSet, LAST_ONLINE_FIELD_NAME_USERS_TABLE ) );
		shortUserData.setAvatarSpoilerId( resultSet.getInt( SPOILER_ID_FIELD_NAME_USERS_TABLE ) );
		shortUserData.setAvatarSpoilerExpDate( getTime( resultSet, SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE ) );
		
		//Note that, the number of forum messages can be re-calculated and is not taken
		//from this table this is done for the sake of the data freshness
		shortUserData.setSentForumMessagesCount( resultSet.getInt( forumMsgCountColumnName ) );
		shortUserData.setSentChatMessagesCount( resultSet.getInt( CHAT_MSGS_COUNT_FIELD_NAME_USERS_TABLE ) );
		
		shortUserData.setTimeOnline( resultSet.getLong( TIME_ONLINE_FIELD_NAME_USERS_TABLE ) );
		if( isRetrieveLoggedInStatus ) {
			shortUserData.setOnline( resultSet.getBoolean( IS_ONLINE_FIELD_NAME_USERS_TABLE ) );
		}
		shortUserData.setGoldPiecesCount( resultSet.getInt( GOLD_PIECES_FIELD_NAME_USERS_TABLE ) );
	}
	
	/**
	 * Allows to get the short user data fields from the result set
	 * @param resultSet the result set to get the results from
	 * @param forumMsgCountColumnName the name of the column from which we should retrieve the number of user's forum messages
	 * @param isRetrieveLoggedInStatus if true then we retrieve the user logged-on status, if false then we do not.
	 * @return the short user data constructed from the results
	 * @throws SQLException an exception if something goes wrong
	 */
	public ShortUserData getShortUserData( final ResultSet resultSet,
			   final String forumMsgCountColumnName, final boolean isRetrieveLoggedInStatus ) throws SQLException {
		ShortUserData shortUserData = new ShortUserData();
		retrieveShortUserData( resultSet, shortUserData, forumMsgCountColumnName, isRetrieveLoggedInStatus );
		return shortUserData;
	}
	
	/**
	 * Allows to fill in the provided short file descriptor with the data from the result set
	 * @param resultSet the result set to get the results from
	 * @param fileDescr the object that has to be filled in with the data
	 * @return the short user data object provided as an argument, this is done for usability
	 * @throws SQLException an exception if something goes wrong
	 */
	public ShortFileDescriptor getShortFileDescriptorData( final ResultSet resultSet,
														   final ShortFileDescriptor fileDescr ) throws SQLException {
		fileDescr.fileID = resultSet.getInt( FILE_ID_FIELD_PROFILE_FILES_TABLE );
		fileDescr.mimeType = resultSet.getString( MIME_TYPE_PROFILE_FILES_TABLE );
		fileDescr.fileName = resultSet.getString( FILE_NAME_PROFILE_FILES_TABLE );
		fileDescr.widthPixels = resultSet.getInt( IMG_WIDTH_PROFILE_FILES_TABLE );
		fileDescr.heightPixels = resultSet.getInt( IMG_HEIGHT_PROFILE_FILES_TABLE );
		return fileDescr;
	}
}
