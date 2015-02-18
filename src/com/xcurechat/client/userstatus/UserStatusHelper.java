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
package com.xcurechat.client.userstatus;

import java.util.Map;
import java.util.HashMap;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIInfoMessages;

import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * This is a helper class for the user status functionality
 */
public final class UserStatusHelper {
	
	private static final String USER_STATUS_IMAGES_EXT = ".png";
	private static final String FREE_FOR_CHAT_IMAGE = "FreeForChat";
	
	private static final Map<Integer, String> statusIDToStatusString = new HashMap<Integer, String>();
	private static final Map<String, UserStatusType> statusStringToStatusID = new HashMap<String, UserStatusType>();
	private static final Map<UserStatusType, String> statusIDToURL = new HashMap<UserStatusType, String>();
	
	private static void addUserStatusMappings(final UserStatusType userStatus, final String statusString, String imageName ){
		statusIDToStatusString.put(	userStatus.getId(), statusString );
		statusStringToStatusID.put(	statusString, userStatus );
		statusIDToURL.put( userStatus , ServerSideAccessManager.USER_STATUS_IMAGES_BASE_URL+ imageName + USER_STATUS_IMAGES_EXT );
	}
	
	static {
		//Initialize the mapping from the user status constants to the internationalized string values
		UIInfoMessages infoMsgsI18N = I18NManager.getInfoMessages();
		
		addUserStatusMappings( UserStatusType.FREE_FOR_CHAT, infoMsgsI18N.userStatusFreeForChat(), FREE_FOR_CHAT_IMAGE );
		addUserStatusMappings( UserStatusType.CHATTING, infoMsgsI18N.userStatusChatting(), "Chatting" );
		addUserStatusMappings( UserStatusType.DO_NOT_DISTURB, infoMsgsI18N.userStatusDND(), "DoNotDisturb" );
		addUserStatusMappings( UserStatusType.IN_PRIVATE_SESSION, infoMsgsI18N.userStatusInPrivate(), "InPrivate" );
		addUserStatusMappings( UserStatusType.ANOTHER_SITE_SECTION, infoMsgsI18N.userStatusElsewhereHere(), "OtherSiteSection" );

		addUserStatusMappings( UserStatusType.RESTING, infoMsgsI18N.userStatusResting(), "Resting" );
		addUserStatusMappings( UserStatusType.MUSIC_LISTENING, infoMsgsI18N.userStatusMusicListening(), "MusicListening" );
		addUserStatusMappings( UserStatusType.WATCHING_TV, infoMsgsI18N.userStatusWatchingTV(), "WatchingTv" );
		addUserStatusMappings( UserStatusType.AWAY, infoMsgsI18N.userStatusAway(), "Away" );
		addUserStatusMappings( UserStatusType.ON_THE_PHONE, infoMsgsI18N.userStatusOnThePhone(), "OnThePhone" );
		addUserStatusMappings( UserStatusType.SLEEPING, infoMsgsI18N.userStatusSleeping(), "Sleeping" );
		addUserStatusMappings( UserStatusType.SMOKING, infoMsgsI18N.userStatusSmoking(), "Smoking" );
		addUserStatusMappings( UserStatusType.EATING, infoMsgsI18N.userStatusEating(), "Eating" );
		addUserStatusMappings( UserStatusType.GAMING, infoMsgsI18N.userStatusGaming(), "Game" );
		addUserStatusMappings( UserStatusType.SURFING, infoMsgsI18N.userStatusSurfingNet(), "Surfing" );
		addUserStatusMappings( UserStatusType.WORKING, infoMsgsI18N.userStatusWorking(), "Working" );
		addUserStatusMappings( UserStatusType.STUDYING, infoMsgsI18N.userStatusStudying(), "Studying" );

		addUserStatusMappings( UserStatusType.FEEL_HAPPY, infoMsgsI18N.userStatusHappy(), "FeelHappy" );
		addUserStatusMappings( UserStatusType.FEEL_SAD, infoMsgsI18N.userStatusSad(), "FeelSad" );
		addUserStatusMappings( UserStatusType.ANGRY, infoMsgsI18N.userStatusAngry(), "Angry" );
		addUserStatusMappings( UserStatusType.IN_LOVE, infoMsgsI18N.userStatusInLove(), "InLove" );
		addUserStatusMappings( UserStatusType.FUCK_OFF, infoMsgsI18N.userStatusFuckOff(), "FuckOff" );
		addUserStatusMappings( UserStatusType.ON_FIRE, infoMsgsI18N.userStatusOnFire(), "OnFire" );
		
		addUserStatusMappings( UserStatusType.NEED_A_MAN, infoMsgsI18N.userStatusNeedAMan(), "NeedAMan" );
		addUserStatusMappings( UserStatusType.NEED_A_WOMAN, infoMsgsI18N.userStatusNeedAWoman(), "NeedAWoman" );
		addUserStatusMappings( UserStatusType.SEARCHING, infoMsgsI18N.userStatusNeedAPartner(), "Searching" );
		
		addUserStatusMappings( UserStatusType.TRAVELLING, infoMsgsI18N.userStatusTravelling(), "Travelling" );
	}
	
	/**
	 * Allows to get the status name by the user status id encoded in a form of a string
	 * @param statusIDStr the user status id (int) as a string
	 * @return the user status name or null if the name could not be determined 
	 */
	public static String getUserStatusString( final String statusIDStr ) {
		String userStatusSring = null;
		
		try {
			final int statusID = Integer.parseInt( statusIDStr );
			userStatusSring = statusIDToStatusString.get(	statusID );
		} catch(Exception e) {}
		
		return userStatusSring;
	}
	
	/**
	 * @return the user a copy of the status string to ID mapping
	 */
	public static Map<String, UserStatusType> getStatusStringToStatusID() {
		return new HashMap<String, UserStatusType>(statusStringToStatusID);
	}
	
	/*
	 * Allows to get the mapping of the user status id to the user status string
	 */
	public static String getUserStatusMsg( final UserStatusType userStatus ) {
		String userStatusString = statusIDToStatusString.get( userStatus.getId() );
		if( userStatusString != null ) {
			return userStatusString;
		} else {
			//Return the default: free for chat
			return I18NManager.getInfoMessages().userStatusFreeForChat();
		}
	}
	
	/*
	 * Allows to get the mapping of the user status id to the user status image url
	 */
	public static String getUserStatusImgURL( final UserStatusType userStatus ) {
		String userStatusImageURL = statusIDToURL.get( userStatus );
		if( userStatusImageURL != null ) {
			return userStatusImageURL;
		} else {
			//Return the default: free for chat
			return ServerSideAccessManager.USER_STATUS_IMAGES_BASE_URL+ FREE_FOR_CHAT_IMAGE + USER_STATUS_IMAGES_EXT;
		}
	}
	
	/**
	 * Allows to retrieve the user status by the user status string (message)
	 * @param statusMessage the user status message
	 * @return the corresponding user status
	 */
	public static UserStatusType getUserStatus( final String statusMessage ) {
		UserStatusType userStatus = statusStringToStatusID.get( statusMessage );
		if( userStatus != null ) {
			return userStatus;
		} else {
			//Return the default: free for chat
			return UserStatusType.FREE_FOR_CHAT;
		}
	}
	
}
