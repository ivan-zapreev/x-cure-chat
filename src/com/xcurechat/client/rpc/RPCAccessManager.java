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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author zapreevis
 * This class is used for instantiating and providing the RPC remote interfaces for the client
 */
public class RPCAccessManager {
	//The name of the user-manager (RPC) servlet context
	public static final String USER_MANAGER_SERVLET_CONTEXT = "users";
	//The name of the room-manager (RPC) servlet context
	public static final String ROOM_MANAGER_SERVLET_CONTEXT = "rooms";
	//The name of the user-statistics (RPC) servlet context
	public static final String USER_STATISTICS_SERVLET_CONTEXT = "statistics";
	//The name of the private messages (RPC) servlet context
	public static final String PRIVATE_MESSAGES_SERVLET_CONTEXT = "messages";
	//The name of the forum manager (RPC) servlet context
	public static final String FORUM_MANAGER_SERVLET_CONTEXT = "forummanager";
	
	//The user-management service object
	private static final UserManagerAsync userMNGService;
	//The room-management service object
	private static final RoomManagerAsync roomMNGService;
	//The user-statistics service object
	private static final UserStatisticsAsync userStatService;
	//The user-messages service object
	private static final MessageManagerAsync messagesService;
	//The forum-manager service object
	private static final ForumManagerAsync forumManagerService;
	
	static {
		userMNGService = getRemoteServieAsyncObject( (UserManagerAsync) GWT.create( UserManager.class), USER_MANAGER_SERVLET_CONTEXT );
		roomMNGService = getRemoteServieAsyncObject( (RoomManagerAsync) GWT.create( RoomManager.class), ROOM_MANAGER_SERVLET_CONTEXT );
		userStatService = getRemoteServieAsyncObject( (UserStatisticsAsync) GWT.create( UserStatistics.class), USER_STATISTICS_SERVLET_CONTEXT );
		messagesService = getRemoteServieAsyncObject( (MessageManagerAsync) GWT.create( MessageManager.class), PRIVATE_MESSAGES_SERVLET_CONTEXT );
		forumManagerService = getRemoteServieAsyncObject( (ForumManagerAsync) GWT.create( ForumManager.class), FORUM_MANAGER_SERVLET_CONTEXT );
	}
	
	/**
	 * Allows to allocate the remote service object
	 * @param classObj the service class object for instantiation
	 * @param URL_SUFFIX the url-suffix on the server t send the requests to
	 * @return returns the remote service object
	 */
	public static <R> R getRemoteServieAsyncObject( R remoteService, final String URL_SUFFIX ) {
		ServiceDefTarget endpoint = (ServiceDefTarget) remoteService;
		String moduleRelativeURL = GWT.getModuleBaseURL() + URL_SUFFIX;
		endpoint.setServiceEntryPoint( moduleRelativeURL );
		
		return remoteService;
	}

	/**
	 * @return returns the forum-manager service object
	 */
	public static ForumManagerAsync getForumManagerAsync() {
		return forumManagerService;
	}

	/**
	 * @return returns the user-management service object
	 */
	public static UserManagerAsync getUserManagerAsync() {
		return userMNGService;
	}

	/**
	 * @return returns the room-management service object
	 */
	public static RoomManagerAsync getRoomManagerAsync() {
		return roomMNGService;
	}

	/**
	 * @return returns the user-statistics service object
	 */
	public static UserStatisticsAsync getUserStatisticsAsync() {
		return userStatService;
	}

	/**
	 * @return returns the user-messages service object
	 */
	public static MessageManagerAsync getMessageManagerAsync(){
		return messagesService;
	}

}
