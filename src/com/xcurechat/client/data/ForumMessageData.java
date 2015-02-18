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

/**
 * @author zapreevis
 * This class extends the short forum message data with the extra data that
 * is needed when the message is retrieved from the database. Basically this
 * class is used for storing the forum search results.
 */
public class ForumMessageData extends ShortForumMessageData {
	
	//Stores the short description of the user who is the author of this message
	public ShortUserData senderData = null;
	
	//The number of replies to this message
	public int numberOfReplies = 0;
	
	//The date of the last reply, in the entire sub-tree of this message, rooted to this message
	public Date lastReplyDate = null;
	
	//The short user description for the user who was the last to reply in
	//the sub-branch of the forum tree rooted to this message
	public ShortUserData lastReplyUser = null;
	
	/**
	 * Allows to check if the sender of the last reply to this message is still registered on the site
	 * @return true if there are replies, and the last reply sender is registered on the site
	 */
	public boolean isLastReplySenderRegistered() {
		return (numberOfReplies > 0) && ( lastReplyUser != null ) ? !lastReplyUser.getUserLoginName().equals( ShortUserData.DELETED_USER_LOGIN_NAME ) : false;
	}
}
