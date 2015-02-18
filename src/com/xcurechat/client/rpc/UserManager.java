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
 * The server side (RPC, servlet) access package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.rpc;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.SiteInfoData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.Top10SearchData;
import com.xcurechat.client.data.search.UserSearchData;

import com.xcurechat.client.userstatus.UserStatusType;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * The RPC interface that allows for users to login, logout,
 * register, delete profile, change personal information
 * (the password etc).
 */
public interface UserManager extends RemoteService {
	
	/**
	 * This method should be user for registering a new user
	 * @param userData the user data object with no password hash and user object hash 
	 * @param userPassword the user password 
	 * @param captchaResponse the CAPTCHA verification string
	 * @return the user user data object with session and etc.
	 */
	public MainUserData register( final MainUserData userData, final String userPassword,
									final String captchaResponse) throws SiteException;
	
	/**
	 * This method processes user login
	 * @param userLoginName the user name
	 * @param userPassword the user password
	 * @return the user-data object for the logged in user
	 */
	public MainUserData login( final String userLoginName, final String userPassword ) throws SiteException;
	
	/**
	 * Validates if the user is logged in by checking the login name and the session
	 * @param userID the user unique ID
	 * @param userSessionId the user's server side session id
	 * @throws SiteException if the user is not logged in or we try to validate the session too often!
	 */
	public void validate( final int userID, final String userSessionId ) throws SiteException;
	
	/**
	 * This method should be user to perform user logout
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 */
	public void logout( final int userID, final String userSessionId ) throws SiteException;
	
	/**
	 * This method allows the user to delete his avatar
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 */
	public void deleteAvatar( final int userID, final String userSessionId ) throws SiteException;
	
	/**
	 * This method allws user to choose an avatar from by index
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param index the avatar's index
	 */
	public void chooseAvatar( final int userID, final String userSessionId, final int index ) throws SiteException;
	
	/**
	 * This method allows to usdate user-profile 
	 * @param userData the user profile, with the new data
	 * @param oldUserPassword the old user password
	 * @param newUserPassword the new user password
	 * @param userSessionId the id of the user session
	 */
	public void update(final MainUserData userData, final String oldUserPassword,
						final String newUserPassword, final String userSessionId ) throws SiteException;
	
	/**
	 * This method allows to get main user profile data based on user Id
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @return the user data 
	 */
	public MainUserData profile( final int userID, final String userSessionId ) throws SiteException;
	
	/**
	 * This method allows to get user profile data based on user Id
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param profileForUserID the unique ID user of the user we are getting profile for
	 * @return the user data 
	 */
	public UserData profile( final int userID, final String userSessionId, final int profileForUserID ) throws SiteException;
	
	/**
	 * This method allows to check if user with ID: friendUserID is a friend of the user with ID: userID   
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param friendUserID the unique ID user of the user we what to check that is a friend
	 * @return true if the user is a friend 
	 */
	public Boolean isFriend( final int userID, final String userSessionId, final int friendUserID ) throws SiteException;
	
	/**
	 * This method allows to retrieve the set of user friend IDs
	 * @param userID the user unique ID
	 * @param userSessionId the user's server side session id
	 * @return the set of friend of the user userID 
	 */
	public Set<Integer> getAllFriends( final int userID, final String userSessionId ) throws SiteException;
	
	/**
	 * This method allows to add/remove friend of the user with ID: userID   
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param friendUserID the unique ID user of the user we what add/remove as a friend
	 * @param doRemove true if we want to remove a friend, false if we want to add one
	 * @return the true if the user was added as a friend, otherwise false
	 */
	public Boolean manageFriend( final int userID, final String userSessionId,
								final int friendUserID, final boolean doRemove ) throws SiteException;
	
	/**
	 * This method allows to delete user profile.
	 * @param userID the user unique ID
	 * @param userPassword the user password
	 * @param userSessionId the id of the user session
	 */
	public void delete( final int userID, final String userPassword, final String userSessionId ) throws SiteException;
	
	/**
	 * This method allows to delete user profile files.
	 * @param userID the unique user ID
	 * @param userSessionId the id of the user session
	 * @param fileIDs the list of ids of the files to delete
	 */
	public void deleteProfileFiles( final int userID, final String userSessionId, final List<Integer> fileIDs ) throws SiteException;

	/**
	 * This method allows count the users satisfying the user-search parameters
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param userSearchData the user search data object
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public Integer count( final int userID, final String userSessionId, final UserSearchData userSearchData ) throws SiteException;

	/**
	 * This method allows to browse users that satisfy the search query
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param userSearchData the user search data object
	 * @param offset the offset for the first statistical entry
	 * @param size the max number of entries to retrieve 
	 * @return an object containing requested statistical data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public OnePageViewData<ShortUserData> browse( final int userID, final String userSessionId,
													final UserSearchData userSearchData,
													final int offset, final int size ) throws SiteException;
	
	/**
	 * Allows to set the user status to the server
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param userStatus the new user status
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void setUserStatus( final int userID, final String userSessionId, final UserStatusType userStatus ) throws SiteException;
	
	/**
	 * Allows to get the user status from the server
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @return the current user status
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public UserStatusType getUserStatus( final int userID, final String userSessionId ) throws SiteException;
	
	/**
	 * Allows to get the statistics for the number of registered users, online users and guests
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @return the current statistics for the number of registered users, online users and guests
	 * @throws SiteException if something goes wrong
	 */
	public SiteInfoData getSiteUsersStatistics( final int userID, final String userSessionId ) throws SiteException;
	
	/**
	 * This method allows to enable/disable bot for the user with the ID being equal to botID   
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param botID the unique ID of the user who will be set/unset as a bot
	 * @param isBot true if we want to set the user as a bot, otherwise false
	 * @throws SiteException if something goes wrong
	 */
	public void enableBot( final int userID, final String userSessionId,
								final int botID, final boolean isBot ) throws SiteException;
	
	/**
	 * Allows to set/clean the user avatar prank
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param prankedUserId the id of the pranked user 
	 * @param prankID the id of the prank
	 * @param isRemove true if we want to remove the prank, false if we want to set a new one
	 * @param the prank expiration date or null if we cleaned the prank
	 * @throws SiteException if something goes wrong
	 */
	public Date setAvatarPrank( final int userID, final String userSessionId,
								final int prankedUserId, final int prankID,
								final boolean isRemove ) throws SiteException;
	
	/**
	 * This method allows to perform search for user top10 site section even if the
	 * user is not logged in, thus the userId 	and userSessionId can be undefined
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param searchParams the object with the search parameters
	 * @return the search results
	 * @throws SiteException in case something goes wrong on the server
	 */
	public OnePageViewData<?> searchTop10( final int userID, final String userSessionId,
												final Top10SearchData searchParams ) throws SiteException;
}
