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
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.userstatus.UserStatusType;
import com.xcurechat.client.utils.AvatarSpoilersHelper;
import com.xcurechat.client.utils.SiteUserAge;
import com.xcurechat.client.utils.UserTreasureLevel;
import com.xcurechat.client.utils.UserForumActivity;

/**
 * @author zapreevis
 * The short user data used for search results,
 * it includes the user ID and the user login name
 */
public class ShortUserData implements IsSerializable, Comparable<ShortUserData> {
	//The following number of points, that can be related to the number of sent chat 
	//messages or chat files or smth else, issued to the user by the system should 
	//to one piece of gold in the user treasury (wallet).
	public static final int POINTS_RER_GOLD_PIECE_VALUE = 5;
	
	//Activity points for each sort of activity
	public static final int OFFLINE_MESSAGE_ACTIVITY_POINTS = 1;
	public static final int CHAT_MESSAGE_TEXT_ACTIVITY_POINTS = 1;
	public static final int CHAT_MESSAGE_FILE_ACTIVITY_POINTS = 2;
	public static final int FORUM_MESSAGE_ACTIVITY_POINTS = 2;
	
	//The maximal visible length of the user login name
	public static final int MAX_VISIBLE_LOGIN_NAME_LENGTH = 12;
	
	//The user ID value if it is not defined.
	public static final int UNKNOWN_UID = 0;
	//The default user UD as used during login
	public static final int DEFAULT_UID = UNKNOWN_UID - 1;
	
	//The login name for the 'deleted' user profile
	public static final String DELETED_USER_LOGIN_NAME = "<Unknown>";
	
	//The default number of the user's gold pieces
	public static final int DEFAULT_USER_GOLD_COUNT = 0;
	public static final UserTreasureLevel DEFAULT_USER_TREASURE_LEVEL = UserTreasureLevel.LEVEL_0;
	
	//The user object unique ID
	protected int uid;
	//The user online status, I.e. when the user is online he can specify
	//his mood and or the things he wants or does at the moment.
	//NOTE: The user status is not stored in the DB, thus the search
	//queries return the default user status values
	protected UserStatusType userStatus;
	//The user's login name
	protected String userName;
	//Is user online or not
	protected boolean isOnline;
	//Indicates if the user's gender true if it is male
	private boolean isMale;
	//User profile registration date
	private Date userRegistrationDate;
	//User last-online date
	private Date userLastOnlineDate;
	//The user age status, that is set when the user is logged in based on the user registration date
	protected SiteUserAge userSiteAge;
	//The user chat activity status
	protected UserTreasureLevel userTreasureLevel;
	//The number of gold pieces in the user's treasury (wallet)
	protected int goldPiecesCount;
	//The counter for the user points, once the points get converted into the gold pieces, the counter is reset to zero
	private int userPointsCounter;
	//The user forum activity status
	private UserForumActivity userForumActivity;
	//The sent forum messages count
	private int sentForumMsgsCount;
	//The user-avatar spoiler id
	private int avatarSpoilerId;
	//The user-avatar spoiler expiration date
	private Date avatarSpoilerExpDate;
	//The time which the user spent online
	private long timeOnline;
	//The number of sent chat messages
	private int sentChatMsgsCount;
	
	public ShortUserData(){
		this.uid = UNKNOWN_UID;
		this.userStatus = UserStatusType.FREE_FOR_CHAT;
		this.userName = "";
		this.isMale = true;
		this.isOnline = false;
		this.userRegistrationDate = null;
		this.userLastOnlineDate = null;
		this.userSiteAge = SiteUserAge.NEW;
		this.userPointsCounter = 0;
		this.userForumActivity = UserForumActivity.LEVEL_0;
		this.sentForumMsgsCount = 0;
		//No money set, yet 
		this.goldPiecesCount = DEFAULT_USER_GOLD_COUNT;
		this.userTreasureLevel = DEFAULT_USER_TREASURE_LEVEL;
		this.avatarSpoilerId = AvatarSpoilersHelper.UNDEFILED_AVATAR_SPOILER_ID;
		this.avatarSpoilerExpDate = null;
		this.timeOnline = 0;
		this.sentChatMsgsCount = 0;
	}
	
	/**
	 * A dummy constructor that should be used to construct a data object with only the user ID and the user name set
	 * @param uid the user id
	 * @param userName the user login name
	 */
	public ShortUserData( final int uid, final String userName ){
		this();
		this.uid = uid;
		this.userName = userName;
	}
	
	/**
	 * Allows to copy the user data from this object to the argument object
	 * @param userData the object to copy the data to
	 */
	public void copyTo( ShortUserData userData ) {
		userData.uid = this.uid;
		userData.userStatus = this.userStatus;
		userData.userName = this.userName;
		userData.isMale = this.isMale;
		userData.isOnline = this.isOnline;
		userData.userLastOnlineDate = this.userLastOnlineDate;
		userData.userSiteAge = this.userSiteAge;
		userData.userPointsCounter = this.userPointsCounter;
		userData.userForumActivity = this.userForumActivity;
		userData.sentForumMsgsCount = this.sentForumMsgsCount;
		userData.goldPiecesCount = this.goldPiecesCount;
		userData.userTreasureLevel = this.userTreasureLevel;
		userData.timeOnline = this.timeOnline;
		userData.sentChatMsgsCount = this.sentChatMsgsCount;
		copySpoilerTo( userData );
	}
	
	/**
	 * Allows to copy user prank data from one object to another
	 * @param userData the object to copy the data to
	 */
	public void copySpoilerTo( ShortUserData userData ) {
		userData.avatarSpoilerId = this.avatarSpoilerId;
		userData.avatarSpoilerExpDate = this.avatarSpoilerExpDate;
	}
	
	/**
	 * For all the ShortUserData object the ordering is naturally defined by the user ID
	 */
	public int compareTo(ShortUserData otherData) {
		if( otherData.uid == uid ) {
			return 0;
		} else {
			if( otherData.uid > uid ) {
				return -1;
			} else {
				return +1;
			}
		}
	}
	
	/**
	 * Allows to get the id of the avatar spoiler
	 * @return the id of the avatar spoiler
	 */
	public int getAvatarSpoilerId() {
		return  avatarSpoilerId;
	}
	
	/**
	 * Allows to set the avatar spoiler id
	 * @param avatarSpoilerId the id of the avatar spoiler
	 */
	public void setAvatarSpoilerId( final int  avatarSpoilerId ) {
		this.avatarSpoilerId = avatarSpoilerId;
	}
	
	/**
	 * Allows to get the expiration date of the avatar spoiler
	 * @return the expiration date of the avatar spoiler, can be null
	 */
	public Date getAvatarSpoilerExpDate() {
		return  avatarSpoilerExpDate;
	}
	
	/**
	 * Allows to set the expiration date for the avatar spoiler
	 * @param avatarSpoilerExpDate the expiration date for the avatar spoiler, can be null
	 */
	public void setAvatarSpoilerExpDate( final Date  avatarSpoilerExpDate ) {
		this.avatarSpoilerExpDate = avatarSpoilerExpDate;
	}
	
	/**
	 * Allows to retrieve the current user status.
	 * NOTE: In case of this class instance being created
	 * in the DB search this method might return the default
	 * user status value but not the one set by the user.
	 * This is because the user status is not stored in the DB.
	 * @return the user status.
	 */
	public UserStatusType getUserStatus() {
		return this.userStatus;
	}
	
	/**
	 * Allows to set the user status.
	 * @param userStatus the new user status.
	 */
	public void setUserStatus( final UserStatusType userStatus ) {
		this.userStatus = userStatus;		
	}

	/**
	 * @return user object unique ID or UNKNOWN_UID if the value is not known
	 */
	public int getUID() {
		return this.uid;
	}
	
	/**
	 * @param uid user object unique ID
	 */
	public void setUID(int uid) {
		this.uid = uid;
	}
	
	/**
	 * @return The user's login name
	 */
	public String getUserLoginName(){
		return this.userName;
	}

	/**
	 * @param userLoginName The user's login name
	 */
	public void setUserLoginName( final String userLoginName){
		//Make sure that there are no spaces around the name.
		if( userLoginName != null ) {
			this.userName = userLoginName.trim();
		} else {
			this.userName = userLoginName;
		}
	}

	/**
	 * Note that that online status does not have to be set.
	 * It is used in e.g. user search dialogs but can be undefined
	 * (false) in other applications.
	 * @return true if user is online, otherwise false
	 */
	public boolean isOnline() {
		return isOnline;
	}
	
	/**
	 * Sets the user online/offline status
	 * @param isOnline true if user is online, otherwise false
	 */
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	/**
	 * Sets the user gender
	 * @param isMale true for male, otherwise false
	 */
	public void setMale(boolean isMale){
		this.isMale = isMale;
	}

	/**
	 * @return true if user is male, otherwise false
	 */
	public boolean isMale(){
		return this.isMale;
	}
	
	/**
	 * Allows to get the user registration date
	 * @return the user registration date
	 */
	public Date getUserRegistrationDate( ) {
		return this.userRegistrationDate;
	}
	
	/**
	 * Allows to set the user registration date
	 * @param userRegistrationDate the user registration date
	 */
	public void setUserRegistrationDate( final Date userRegistrationDate ) {
		this.userRegistrationDate = userRegistrationDate;
		//Set the user registration age
		setUserRegistrationAge( this.userRegistrationDate );
	}
	
	/**
	 * Allows to truncate a long login name into a short one
	 * @param loginName the long login name
	 * @return the truncated login name
	 */
	public static String getShortLoginName( final String loginName ) {
		if( loginName.length() > MAX_VISIBLE_LOGIN_NAME_LENGTH ) {
			return loginName.substring(0, MAX_VISIBLE_LOGIN_NAME_LENGTH - 3 ) + "...";
		} else {
			return loginName;
		}
	}
	
	/**
	 * Allows to truncate a long login name into a short one
	 * @return the truncated login name
	 */
	public String getShortLoginName( ) {
		return ShortUserData.getShortLoginName( userName );
	}
	
	public Date getUserLastOnlineDate( ) {
		return this.userLastOnlineDate;
	}
	
	public void setUserLastOnlineDate( final Date userLastOnlineDate ) {
		this.userLastOnlineDate = userLastOnlineDate;
	}
	
	/**
	 * Allows to get the user registration age
	 * @return the user registration age
	 */
	public SiteUserAge getSiteUserAge() {
		return this.userSiteAge;
	}
	
	/**
	 * Set the user registration age based on the user registration date
	 */
	protected void setUserRegistrationAge( final Date userRegistrationDate ) {
		this.userSiteAge = SiteUserAge.SiteUserAgeHelper.getSiteUserAgeByRegDate( userRegistrationDate );
	}
	
	/**
	 * Allows to get the current user's level of treasure
	 */
	public synchronized UserTreasureLevel getUserTreasureLevel() {
		return this.userTreasureLevel;
	}
	
	/**
	 * Allows to indicate that the user sent another offline message
	 */
	public void sentAnotherOfflineMessage() {
		addUserActivityPoints( OFFLINE_MESSAGE_ACTIVITY_POINTS );
	}
	
	/**
	 * Allows to indicate that the user sent another chat message
	 * @param isPublic true if the message is public 
	 * @param isHasText true if the message has a non-empty body
	 * @param isHasFile true if the message has an attached file
	 */
	public void sentAnotherChatMessage( final boolean isPublic, final boolean isHasText, final boolean isHasFile ) {
		if( isHasText ) {
			//In case the message has text add points for that
			addUserActivityPoints( CHAT_MESSAGE_TEXT_ACTIVITY_POINTS );
		}
		if( isHasFile ) {
			//In case the message has a file attached add points for that
			addUserActivityPoints( CHAT_MESSAGE_FILE_ACTIVITY_POINTS );
		}
		//Increment the count of the chat messages
		this.sentChatMsgsCount++;
	}
	
	/**
	 * Allows to subtract the certain amount of the gold pieces from the user's wallet.
	 * Works only if goldPiecesDecrement > 0;
	 * @param decrement the number of gold pieces by which the value will be decremented
	 * @throws InternalSiteException in case the used does not have sufficient gold pieces
	 */
	public synchronized void decrementGoldPiecesCount( final int decrement ) throws InternalSiteException {
		if( decrement != 0 ) {
			final int goldDecrement = Math.abs( decrement );
			final int currentGoldPiecesCount = getGoldPiecesCount();
			if( currentGoldPiecesCount >= goldDecrement ) {
				setGoldPiecesCount( currentGoldPiecesCount - goldDecrement );
			} else {
				throw new InternalSiteException( InternalSiteException.INSUFFICIENT_GOLD_FUNDS,
												 currentGoldPiecesCount, goldDecrement );
			}
		}
	}
	
	/**
	 * Allows to increment the gold-pieces count in the user's wallet by the certain amount of gold.
	 * Prevents setting the gold pieces count for more than the maximum value of integer.
	 * @param goldPiecesIncrement the number of gold pieces by which the gold-pieces count should be incremented
	 */
	public synchronized void incrementGoldenPiecesCount( final int goldPiecesIncrement ) {
		final int currentGoldPiecesCount = getGoldPiecesCount();
		if( currentGoldPiecesCount <= ( Integer.MAX_VALUE - goldPiecesIncrement ) ) {
			setGoldPiecesCount( currentGoldPiecesCount + goldPiecesIncrement );
		}
	}
	
	/**
	 * Allows to set the number of gold pieced in the user's treasury (wallet)
	 * Will not set the gold count that is < 0. If such a value is provided
	 * then the gold count is set to be 0.
	 * @param goldPiecesCount the number of gold pieced in the user's treasury (wallet)
	 */
	public synchronized void setGoldPiecesCount( final int goldPiecesCount ) {
		this.goldPiecesCount = goldPiecesCount;
		if( this.goldPiecesCount < 0 ) {
			this.goldPiecesCount = 0;
		}
		this.userTreasureLevel = UserTreasureLevel.SiteChatActivityHelper.getUserTreasureLevel( this.goldPiecesCount );
	}
	
	/**
	 * Allows to get the number of gold pieced in the user's treasury (wallet)
	 * @return the number of gold pieced in the user's treasury (wallet)
	 */
	public synchronized int getGoldPiecesCount() {
		return goldPiecesCount;
	}
	
	/**
	 * Allows to add more points to the user profile, these points are then
     * converted into the gold pieces of the user treasury (wallet).
     * @param numberOrPoints - the number of points to add, can NOT be negative
	 */
	private synchronized void addUserActivityPoints( final int numberOrPoints ) {
		if( numberOrPoints >= 0 ) {
			//Add activity points
			userPointsCounter += numberOrPoints;
			//If we collected enough points then convert them to gold as many as possible
			if( userPointsCounter >= POINTS_RER_GOLD_PIECE_VALUE ) {
				//Convert into gold
				final int extraGoldPieces = userPointsCounter / POINTS_RER_GOLD_PIECE_VALUE;
				//Store remaining points
				userPointsCounter = userPointsCounter % POINTS_RER_GOLD_PIECE_VALUE;
				//increment the gold count
				incrementGoldenPiecesCount( extraGoldPieces );
			}
		}
	}
	
	/**
	 * Allows to get the current user forum activity
	 */
	public synchronized UserForumActivity getUserForumActivity() {
		return this.userForumActivity;
	}
	
	/**
	 * Allows to account for the forum message sent by the user
	 */
	public synchronized void sentAnotherForumMessage() {
		//Add points for the new forum message
		addUserActivityPoints( FORUM_MESSAGE_ACTIVITY_POINTS );
		
		//In case the number of sent forum messages exceeds the upper border of
		//the range of the current activity level, re-calculate the activity level
		if( ( ++sentForumMsgsCount ) >= this.userForumActivity.getRangeEnd() ) {
			this.userForumActivity = UserForumActivity.SiteForumActivityHelper.getSiteUserForumActivityBySentMsgs( sentForumMsgsCount );
		}
	}
	
	/**
	 * Allows to get the current count for the forum messages
	 * @return count the forum messages count
	 */
	public synchronized int getSentForumMessagesCount() {
		return this.sentForumMsgsCount;
	}
	
	/**
	 * Allows to set the current count for the forum messages
	 * @param count the forum messages count
	 */
	public synchronized void setSentForumMessagesCount( final int count ) {
		this.sentForumMsgsCount = count;
		this.userForumActivity = UserForumActivity.SiteForumActivityHelper.getSiteUserForumActivityBySentMsgs( count );
	}
	
	/**
	 * Allows to get the current count for the chat messages
	 * @return count the chat messages count
	 */
	public synchronized int getSentChatMessagesCount() {
		return this.sentChatMsgsCount;
	}
	
	/**
	 * Allows to set the current count for the chat messages
	 * @param count the chat messages count
	 */
	public synchronized void setSentChatMessagesCount( final int count ) {
		this.sentChatMsgsCount = count;
	}
	
	/**
	 * Allows to get the current time online in milliseconds,
	 * i.e. the time since the given object was created
	 * @return count the current time online in milliseconds
	 */
	public synchronized long getTimeOnline() {
		return this.timeOnline;
	}
	
	/**
	 * Allows to set the current time online in milliseconds
	 * @param timeOnline the current time online in milliseconds
	 */
	public synchronized void setTimeOnline( final long timeOnline ) {
		this.timeOnline = timeOnline;
	}
}
