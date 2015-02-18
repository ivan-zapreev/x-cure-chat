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
 * The server core package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server.core;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;

/**
 * @author zapreevis
 * This is a chat bot manager. This class is synchronized
 */
public class ChatBotManager {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ChatBotManager.class );
	
	//The only instance of the chat bot manager
	private static final ChatBotManager instance = new ChatBotManager();
	
	static {
		//TODO: Get all the bot users from the DB and initiate them
	}
	
	private ChatBotManager(){}
	
	/**
	 * Allows to get the only instance of the class
	 * @return the only instance of the class
	 */
	public static ChatBotManager getInstance() {
		return instance;
	}
	
	//The ID of the current chat bot
	private int chatBotID = MainUserData.UNKNOWN_UID;
	
	/**
	 * Allows to retrieve the current chat bot profile ID or MainUserData.UNKNOWN_UID
	 * if the current chat bot is not set.
	 */
	public synchronized int getCurrentChatBotID() {
		return chatBotID;
	}
	
	/**
	 * @return true if the current chat bot is set otherwise false
	 */
	public synchronized boolean isCurrentChatBotSet() {
		return chatBotID != MainUserData.UNKNOWN_UID;
	}
	
	/**
	 * Allows to enable/disable the given user as a bot
	 * @param botUserID the id of the user who is a bot that will be a human or is a human that will be a bot
	 * @param isBot true to mane the user a bot, false to make the user a human
	 */
	public synchronized void enableBot( final int botUserID, final boolean isBot ) {
		logger.info("Marking the user " + botUserID + " as a " + ( isBot ? "BOT" : "HUMAN") );
		
		//TODO: Implement
	}
}
