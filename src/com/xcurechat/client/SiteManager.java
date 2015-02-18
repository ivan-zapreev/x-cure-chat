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
 * The user interface package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * The static site manager class, stores user state within the site
 */
public final class SiteManager {
	//The login name of the logged-on user
	private static String userLoginName = null;
	//The session id of the logged on user
	private static String userSessionId = null;
	//The id of the logged on user
	private static int userID = UserData.UNKNOWN_UID;
	//The profile type of the ogged on user
	private static int userProfileType = MainUserData.UNKNOWN_USER_TYPE;
	//The list of opened room IDs for the logged in user
	private static List<Integer> openedRoomIds = null;
	//The list of blocked user IDs for the logged in user
	private static List<Integer> blockedUserIds = null;
	//The short user data description for the logged in user or null if it is not set yet
	private static ShortUserData logedInUserShortData = null;
	
	/**
	 * Set the logged in user
	 * @param mainUserData the user object with the login name,
	 * user id, session id and profile type
	 */
	public static void setLoggedInUser( final MainUserData mainUserData ) {
		logedInUserShortData = mainUserData;
		userLoginName = mainUserData.getUserLoginName();
		userSessionId = mainUserData.getUserSessionId();
		userProfileType = mainUserData.getUserProfileType();
		userID = mainUserData.getUID();
		storeToCookies();
		retrieveServerFriends();
	}
	
	/**
	 * Allows to set the short user data for the logger in user
	 * @param shortUserData the short user data to be set
	 */
	public static void setShortUserData( final ShortUserData shortUserData ) {
		logedInUserShortData = shortUserData;
	}
	
	/**
	 * Allows to get the short user data of the logged in user
	 * @return the short user data of the logged in user or null if it has not been set yet
	 */
	public static ShortUserData getShortUserData() {
		return logedInUserShortData;
	}
	
	/**
	 * Returns true if the logged in user is an administrator, otherwise false
	 * @return true if the logged in user is an administrator, otherwise false
	 */
	public static boolean isAdministrator() {
		return ( userProfileType == MainUserData.ADMIN_USER_TYPE );
	}
	
	/**
	 * Get the login name of the logged in user 
	 * @return the login name
	 */
	public static String getUserLoginName( ) {
		if( userLoginName == null ){
			loadFromCookies();			
		}
		return userLoginName;
	}
	
	/**
	 * Get the UID of the logged in user 
	 * @return the user unique id
	 */
	public static int getUserID( ) {
		if( userID == UserData.UNKNOWN_UID ){
			loadFromCookies();			
		}
		return userID;
	}
	
	/**
	 * Get the session id for the logged in user 
	 * @return the session id
	 */
	public static String getUserSessionId( ) {
		if( userSessionId == null ){
			loadFromCookies();			
		}
		return userSessionId;
	}
	
	/**
	 * Get the user profile userProfileType
	 * @return the user profile userProfileType
	 */
	public static int getUserProfileType(){
		if( userProfileType == MainUserData.UNKNOWN_USER_TYPE ){
			loadFromCookies();
		}
		return userProfileType;
	}
	
	/**
	 * Removes the logged in user data 
	 */
	public static void removeLoggedInUser() {
		removeCookies();
		logedInUserShortData = null;
		userLoginName = null;
		userSessionId = null;
		userID = UserData.UNKNOWN_UID;
		userProfileType = MainUserData.UNKNOWN_USER_TYPE;
		openedRoomIds = null;
		blockedUserIds = null;
		removeAllFriends();
	}
	
	/**
	 * Allows to detect if the site manager has a logged in user set
	 * @return true if there is a logged in user set
	 */
	public static boolean isUserLoggedIn() {
		//NOTE: We do not user variables but call methods because the values 
		//could have not been loaded from the cookies yet
		return  ( SiteManager.getUserLoginName() != null ) && ( SiteManager.getUserSessionId() != null ) &&
				( SiteManager.getUserProfileType() != MainUserData.UNKNOWN_USER_TYPE ) && 
				( SiteManager.getUserID() != UserData.UNKNOWN_UID );
	}
	
	/**
	 * Allows to add the user ID for the list of users from which
	 * we do not want to receive chat messages
	 * @param userId the user ID to block
	 */
	public static void addBlockedUserId( final int userId ) {
		if( blockedUserIds == null ) {
			loadBlockedUsersFromCookie();
		}
		//if the room is not yet marked as opened
		if( ! blockedUserIds.contains( userId ) ) {
			blockedUserIds.add( userId );
		}
		storeBlockedUsersToCookie();
	}
	
	/**
	 * Update the list of blocked users, i.e. remove the user that we unblocked.
	 * Store the list of blocked users in a cookie
	 * @param userId the user to remove from that list
	 */
	public static void removeBlockedUserId( final int userId ) {
		if( blockedUserIds == null ) {
			loadBlockedUsersFromCookie();
		}
		for( int i = 0; i < blockedUserIds.size(); i++ ) {
			if( blockedUserIds.get( i ) == userId ) {
				blockedUserIds.remove( i );
				break;
			}
		}
		storeBlockedUsersToCookie();
	}
	
	/**
	 * Allows to check if the user if blocked
	 * @param userId the user which we test for being blocked
	 * @return true if the user is blocked otherwise false
	 */
	public static boolean isUserBlocked( final int userId ) {
		boolean result = false;
		if( userId != ShortUserData.UNKNOWN_UID &&
			userId != ShortUserData.DEFAULT_UID	) {
			if( blockedUserIds == null ) {
				loadBlockedUsersFromCookie();
			}
			result = blockedUserIds.contains( userId );
		}
		return result;
	}
	
	/**
	 * Update the list of opened rooms, i.e. add a room we just opened.
	 * Store the list of opened rooms in a coockie
	 * @param roomId the room to add to that list
	 */
	public static void addOpenedRoomId( final int roomId ) {
		if( openedRoomIds == null ) {
			loadOpenedRoomsFromCookie();
		}
		//if the room is not yet marked as opened
		if( ! openedRoomIds.contains( roomId ) ) {
			openedRoomIds.add( roomId );
		}
		storeOpenedRoomsToCookie();
	}
	
	/**
	 * Update the list of opened rooms, i.e. remove a room we just closed.
	 * Store the list of opened rooms in a coockie
	 * @param roomId the room to remove from that list
	 */
	public static void removeOpenedRoomId( final int roomId ) {
		if( openedRoomIds == null ) {
			loadOpenedRoomsFromCookie();
		}
		for( int i = 0; i < openedRoomIds.size(); i++ ) {
			if( openedRoomIds.get( i ) == roomId ) {
				openedRoomIds.remove( i );
				break;
			}
		}
		storeOpenedRoomsToCookie();
	}
	
	/**
	 * Alows to retrieve the list of opened rooms that is stored in the coockies
	 * @return the list of opened rooms that is stored in the coockies
	 */
	public static List<Integer> getOpenedRoomIds( ) {
		if( openedRoomIds == null ) {
			loadOpenedRoomsFromCookie();
		}
		return openedRoomIds;
	}
	
	
	private static final String NOTIFY_SOUND_IS_ON_OFF_COOKIE_NAME = "xcure.client.notify.sound.status";
	private static final String FIRST_TIME_HELP_SHOWN_COOKIE_NAME = "xcure.client.first.time.help.is.shown";
	private static final String OS_BROWSER_VERSION_WARNED_COOKIE_NAME = "xcure.os.browser.version.warned";
	private static final String USER_LOGIN_COOKIE_NAME = "xcure.user.login";
	private static final String USER_ID_COOKIE_NAME = "xcure.user.id";
	private static final String USER_SESSION_COOKIE_NAME = "xcure.session.id";
	private static final String USER_PROFILE_TYPE_COOKIE_NAME = "xcure.user.profile.type";
	private static final String OPENED_ROOM_IDS_COOKIE_NAME = "xcure.opened.room.ids";
	private static final String BLOCKED_USER_IDS_COOKIE_NAME = "xcure.blocked.user.ids";
	
	/**
	 * Allows to read the cookie indicating if new message notification sound is on/off
	 * If the cookie is not set then returns true.
	 * @return true if the sound shuold be on
	 */
	public static boolean isMessageNotifySoundOn() {
		String value = Cookies.getCookie( NOTIFY_SOUND_IS_ON_OFF_COOKIE_NAME );
		return (value == null) || Boolean.parseBoolean( value );
	}
	
	/**
	 * Allows to set the cookie indicating that new message notification sound is on/off
	 */
	public static void setMessageNotifySound( final boolean isOn ) {
		Cookies.setCookie( NOTIFY_SOUND_IS_ON_OFF_COOKIE_NAME, (new Boolean(isOn)).toString() );
	}
	
	/**
	 * Allows to test if the first time help info message was shown
	 * @return true if the help message was shown
	 */
	public static boolean isFirstTimeHelpShown() {
		return Boolean.parseBoolean( Cookies.getCookie( FIRST_TIME_HELP_SHOWN_COOKIE_NAME ) );
	}
	
	/**
	 * Allows to set the cookie indicating the first time help
	 * info message was shown to the user.
	 */
	public static void setFirstTimeHelpShown() {
		Cookies.setCookie( FIRST_TIME_HELP_SHOWN_COOKIE_NAME, (new Boolean(true)).toString() );
	}
	
	/**
	 * Allows to test if the user was warned about the unsupported OS, Browser, Browser version
	 * @return true if the user was warned, otherwise false 
	 */
	public static boolean isUnsupportedOSBrowserVersionWarned() {
		return Boolean.parseBoolean( Cookies.getCookie( OS_BROWSER_VERSION_WARNED_COOKIE_NAME ) );
	}
	
	/**
	 * Allows to set the cookie indicating the user was warned about
	 * the unsupported OS, Browser, Browser version
	 */
	public static void setMSIEWarned() {
		Cookies.setCookie( OS_BROWSER_VERSION_WARNED_COOKIE_NAME, (new Boolean(true)).toString() );
	}
	
	private static void storeListOfIdsToCookie( final List<Integer> listIds, final String cookieName ) {
		if( listIds != null ) {
			String data = "";
			for( int i = 0; i < listIds.size(); i ++) {
				data += listIds.get(i)+",";
			}
			Cookies.setCookie( cookieName, data );
		}
	}

	private static void loadListOfIdsFromCookie( final List<Integer> listIds, final String cookieName ) {
		String data = Cookies.getCookie( cookieName );
		if( data != null ) {
			String[] roomIds = data.split("[,]");
			for(int i = 0; i < roomIds.length; i++){
				try{
					listIds.add( Integer.parseInt( roomIds[i] ) );
				} catch( NumberFormatException e ){
					//Do Nothing, simply try to read further
				}
			}
		}		
	}
	
	/**
	 * This method saves the logged-in user's list of blocked users
	 */
	private static void storeBlockedUsersToCookie() {
		storeListOfIdsToCookie( blockedUserIds, BLOCKED_USER_IDS_COOKIE_NAME + "." + getUserLoginName( ) );
	}
	
	/**
	 * This method loads the logged-in user's list of blocked users
	 */
	private static void loadBlockedUsersFromCookie() {
		blockedUserIds = new ArrayList<Integer>();
		loadListOfIdsFromCookie( blockedUserIds, BLOCKED_USER_IDS_COOKIE_NAME + "." + getUserLoginName( ) );
	}
	
	private static void storeOpenedRoomsToCookie() {
		storeListOfIdsToCookie( openedRoomIds, OPENED_ROOM_IDS_COOKIE_NAME );
	}
	
	private static void loadOpenedRoomsFromCookie() {
		openedRoomIds = new ArrayList<Integer>();
		loadListOfIdsFromCookie( openedRoomIds, OPENED_ROOM_IDS_COOKIE_NAME );
	}
	
	private static void storeToCookies( ) {
		if( ( userLoginName != null ) && ( userSessionId != null ) &&
				( userProfileType != MainUserData.UNKNOWN_USER_TYPE ) &&
				( userID != UserData.UNKNOWN_UID ) ) {
			Cookies.setCookie( USER_LOGIN_COOKIE_NAME, userLoginName );
			Cookies.setCookie( USER_SESSION_COOKIE_NAME, userSessionId );
			Cookies.setCookie( USER_ID_COOKIE_NAME, (new Integer(userID)).toString() );
			Cookies.setCookie( USER_PROFILE_TYPE_COOKIE_NAME, (new Integer(userProfileType)).toString() );
		}
	}
	
	private static void loadFromCookies() {
		userLoginName = Cookies.getCookie( USER_LOGIN_COOKIE_NAME );
		userSessionId = Cookies.getCookie( USER_SESSION_COOKIE_NAME );
		try{
			userProfileType = Integer.parseInt( Cookies.getCookie( USER_PROFILE_TYPE_COOKIE_NAME ) );
		} catch( NumberFormatException e ){
			userProfileType = MainUserData.SIMPLE_USER_TYPE;
		}
		try{
			userID = Integer.parseInt( Cookies.getCookie( USER_ID_COOKIE_NAME ) );
		} catch( NumberFormatException e ){
			userID = UserData.UNKNOWN_UID;
		}
	}

	private static void removeCookies() {
		Cookies.removeCookie( USER_LOGIN_COOKIE_NAME );
		Cookies.removeCookie( USER_SESSION_COOKIE_NAME );
		Cookies.removeCookie( USER_ID_COOKIE_NAME );
		Cookies.removeCookie( USER_PROFILE_TYPE_COOKIE_NAME );
		Cookies.removeCookie( OPENED_ROOM_IDS_COOKIE_NAME );
		//Do not remove the blocked users IDs because those can be reused
	}
	
	//The local list of friends
	private static Set<Integer> userFriendIds = new HashSet<Integer>();
	
	/**
	 * Retrieves the list of friends from the server.
	 * This should be calles once, upon the user log-on
	 */
	public static void retrieveServerFriends() {
		if( userFriendIds == null ) {
			userFriendIds = new HashSet<Integer>();
		}
		
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				AsyncCallback<Set<Integer>> callback = new AsyncCallback<Set<Integer>>() {
					public void onSuccess(Set<Integer> result) {
						userFriendIds.addAll( result );
					}
					public void onFailure(final Throwable caught) {
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Report the error
								ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().unableToRetrieveTheListOfFriends() );
							}
						}).loadAndExecute();
					}
				};
				
				//Perform the server call to retrieve user's friends
				UserManagerAsync userManager = RPCAccessManager.getUserManagerAsync();
				userManager.getAllFriends( SiteManager.getUserID(), SiteManager.getUserSessionId(), callback );
			}
		}).loadAndExecute();
	}
	
	/**
	 * Resets the local storage of friends to an empty set.
	 * This should be calles once, upon the user log-out
	 */
	public static void removeAllFriends() {
		if( userFriendIds != null ) {
			userFriendIds.clear();
		}
	}
	
	/**
	 * Allows to check if the given user is a friend
	 * @param userID the user id for checking to be a friend
	 * @return true if the user is a friend
	 */
	public static boolean isFriend( final int userID ) {
		return (userFriendIds != null) ? userFriendIds.contains( userID ) : false;
	}
	
	/**
	 * Adds a friend to the loacal list of friends
	 * @param userID the id of the user which is a friend
	 */
	public static void addFriend( final int userID ){
		if( userFriendIds != null ) {
			userFriendIds.add( userID );
		}
	}
	
	/**
	 * Removes a friend from the loacal list of friends
	 * @param userID the id of the user which is to be removed
	 */
	public static void removeFriend( final int userID ){
		if( userFriendIds != null ) {
			userFriendIds.remove( userID );
		}
	}
}
