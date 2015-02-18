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
 * The server-side utilities package.
 */
package com.xcurechat.server.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.ConnectionWrapper;

import com.xcurechat.server.jdbc.profile.AddUniLoginNameExecutor;
import com.xcurechat.server.jdbc.profile.GetUsersWithNoUniLoginExecutor;

/**
 * @author zapreevis
 * This class is responsible for adding the unified Id to the user profiles.
 * This class should not be really used more that once, due to the one time DB change.
 */
public class AddUnifiedLoginThread extends Thread {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( AddUnifiedLoginThread.class );
	
	@Override
    public void run() {
		logger.info("Starting the set up of the unified user login names");
		
		List<ShortUserData> users = new ArrayList<ShortUserData>();
		try{
			ConnectionWrapper<List<ShortUserData>> registerUserConnWrap = ConnectionWrapper.createConnectionWrapper( new GetUsersWithNoUniLoginExecutor() );
			registerUserConnWrap.executeQuery( users, ConnectionWrapper.XCURE_CHAT_DB );
			logger.info("Found " + users.size() + " user profiles which do not have unified login names assiged");
			
			for( ShortUserData userData : users ) {
				logger.debug("Seting the unified login name for the user " + userData.getUID() + " with the login name " + userData.getUserLoginName() );
				try{
					ConnectionWrapper<Void> updateUserConnWrap = ConnectionWrapper.createConnectionWrapper( new AddUniLoginNameExecutor( userData.getUID(), userData.getUserLoginName()) );
					updateUserConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				} catch( SiteException e ) {
					logger.error("An unexpected internal site exception while assigning the unified user login for user " + userData.getUID() + ", the user is skipped", e);
				}
			}
		} catch( SiteException e ) {
			logger.error("An unexpected internal site exception while assigning the unified user logins", e);
		}
		
		logger.info("Setting the unified login names is finished");
    }
}
