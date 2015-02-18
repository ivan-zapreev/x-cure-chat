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
package com.xcurechat.client.rpc;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.SiteInfoData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.Top10SearchData;
import com.xcurechat.client.data.search.UserSearchData;

import com.xcurechat.client.userstatus.UserStatusType;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author zapreevis
 * The Async RPC interface that allows for users to login, logout,
 * register, delete profile, change personal information
 * (the password etc).
 */
public interface UserManagerAsync {
	
	/**
	 * This method should be user for registering a new user
	 */
	public void register(final MainUserData userData, final String userPassword,
							final String captchaResponse, AsyncCallback<MainUserData> callback);
	
	/**
	 * This method processes user login
	 */
	public void login( final String userLoginName, final String userPassword,
						AsyncCallback<MainUserData> callback );
	
	/**
	 * Validates if the user is logged in by checking the login name and the session
	 */
	public void validate( final int userID, final String userSessionId, AsyncCallback<Void> callback );
	
	/**
	 * This method should be user to perform user logout
	 */
	public void logout( final int userID, final String userSessionId, AsyncCallback<Void> callback);
	
	/**
	 * This method allows the user to delete his avatar
	 */
	public void deleteAvatar( final int userID, final String userSessionId, AsyncCallback<Void> callback );
	
	/**
	 * This method allws user to choose an avatar from by index
	 */
	public void chooseAvatar( final int userID, final String userSessionId, final int index, AsyncCallback<Void> callback );
	
	/**
	 * This method allows to update user-profile 
	 */
	public void update(final MainUserData userData, final String oldUserPassword,
						final String newUserPassword, final String userSessionId,
						AsyncCallback<Void> callback);
	
	/**
	 * This method allows to get user profile data based on session Id
	 */
	public void profile( final int userID, final String userSessionId, AsyncCallback<MainUserData> callback );
	
	/**
	 * This method allows to get user profile data based on user Id
	 */
	public void profile( final int userID, final String userSessionId,
						final int profileForUserID, AsyncCallback<UserData> callback );
	
	/**
	 * This method allows to check if user with ID: friendUserID is a friend of the user with ID: userID   
	 */
	public void isFriend( final int userID, final String userSessionId,
							final int friendUserID, AsyncCallback<Boolean> callback );
	
	/**
	 * This method allows to retrieve the set of user friend IDs
	 */
	public void getAllFriends( final int userID, final String userSessionId,
								AsyncCallback<Set<Integer>> callback );
	
	/**
	 * This method allows to add/remove friend of the user with ID: userID
	 */
	public void manageFriend( final int userID, final String userSessionId,
								final int friendUserID, final boolean doRemove,
								AsyncCallback<Boolean> callback );

	/**
	 * This method allows to delete user profile.
	 */
	public void delete( final int userID, final String userPassword,
						final String userSessionId, AsyncCallback<Void> callback);

	/**
	 * This method allows to delete user profile files.
	 */
	public void deleteProfileFiles( final int userID, final String userSessionId,
									final List<Integer> fileIDs, AsyncCallback<Void> callback);

	/**
	 * This method allows count the users satisfying the user-search parameters
	 */
	public void count( final int userID, final String userSessionId,
						final UserSearchData userSearchData, AsyncCallback<Integer> callback );

	/**
	 * This method allows to browse users that satisfy the search query
	 */
	public void browse( final int userID, final String userSessionId,
						final UserSearchData userSearchData, final int offset,
						final int size, AsyncCallback<OnePageViewData<ShortUserData>> callback );

	/**
	 * Allows to set the user status to the server
	 */
	public void setUserStatus( final int userID, final String userSessionId,
							   final UserStatusType userStatus, AsyncCallback<Void> callback );
	/**
	 * Allows to get the user status from the server
	 */
	public void getUserStatus( final int userID, final String userSessionId, AsyncCallback<UserStatusType> callback );
	
	/**
	 * Allows to get the statistics for the number of registered users, online users and guests
	 */
	public void getSiteUsersStatistics( final int userID, final String userSessionId, AsyncCallback<SiteInfoData> callback );
	
	
	/**
	 * This method allows to enable/disable bot for the user with the ID being equal to botID
	 */
	public void enableBot( final int userID, final String userSessionId,
							final int botID, final boolean isBot, AsyncCallback<Void> callback );
	
	/**
	 * Allows to set/clean the user avatar prank
	 */
	public void setAvatarPrank( final int userID, final String userSessionId,
								final int prankedUserId, final int prankID,
								final boolean isRemove, AsyncCallback<Date> callback );
	
	/**
	 * This method allows to perform search for user top10 site section even if the
	 * user is not logged in, thus the userId 	and userSessionId can be undefined
	 */
	public void searchTop10( final int userID, final String userSessionId,
							 final Top10SearchData searchParams,
							 AsyncCallback<OnePageViewData<?>> callback );
}
