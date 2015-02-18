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
 * The server-side package, core.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.core;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.SiteInfoData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.UserSearchData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.profile.search.CountUserSearchResultsExecutor;

/**
 * @author zapreevis
 * This class is responsible for providing site visitors with the statistics about 
 * the number of registered users, registered users online and total online visitors.
 * This class is a singleton. The class is thread safe.
 */
public class SiteUsersStatisticsManager {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SiteUsersStatisticsManager.class );
	
	//The only instance of the class
	private static final SiteUsersStatisticsManager instance = new SiteUsersStatisticsManager();
	
	/**
	 * Allows to retrieve the only instance of the class
	 * @return the only instance of the class
	 */
	public static SiteUsersStatisticsManager getInstance() {
		return instance;
	}
	
	//The actual search data used to count the registered users, using the default one
	private final UserSearchData userSearchData = new UserSearchData();
	
	//The actual statistical data
	private final SiteInfoData data = new SiteInfoData();
	
	//The time of the last statistics update
	private long lastUpdateTime = System.currentTimeMillis() - SiteInfoData.SERVER_UPDATES_PERIODICITY_MILLISEC;
	
	//Make the constructor private because we have a singleton here
	private SiteUsersStatisticsManager() {
	}
	
	/**
	 * Allows to retrieve the actual statistics which is updated periodically
	 * @param userID is the id of the user retrieving the statistical data
	 * @return  the statistics about the number of registered users, registered users online and total online visitors.
	 */
	public synchronized SiteInfoData getStatistics(final int userID) {
		//If the last update was long ago, it is time to update the actual data
		if( ( System.currentTimeMillis() - SiteInfoData.SERVER_UPDATES_PERIODICITY_MILLISEC ) >= lastUpdateTime ) {
			logger.debug( "Updating the actual site-users statistics, triggered by user " + userID );
			
			//Count the total number of registered users
			OnePageViewData<ShortUserData> foundUsers = new OnePageViewData<ShortUserData>(); 
			ConnectionWrapper<OnePageViewData<ShortUserData>> countFoundUsersConnWrap = ConnectionWrapper.createConnectionWrapper( new CountUserSearchResultsExecutor(userID, userSearchData) );
			try { 
				countFoundUsersConnWrap.executeQuery( foundUsers, ConnectionWrapper.XCURE_CHAT_DB );
				data.totalRegisteredUsers = foundUsers.total_size;
			} catch (SiteException e) {
				logger.error("An unexpected exception when retrieving the number of registered users for the site-users statistics", e);
			}
			
			//Count the number of online users, by checking the users session manager
			data.registeredUsersOnline = UserSessionManager.getOnlineUsersCount();
			
			//Get the total number of currently online users, including the non-logged in ones
			//Basically this is the total number of available session objects
			data.visitorsOnline = HttpUserSessionListener.getCurrentSessionsCount();
			
			logger.debug( "The actual site-users statistics is totalRegisteredUsers:" + data.totalRegisteredUsers +
						  ", registeredUsersOnline:" + data.registeredUsersOnline + ", visitorsOnline" + data.visitorsOnline );
			
			lastUpdateTime = System.currentTimeMillis();
		}
		
		return data.clone();
	}
}
