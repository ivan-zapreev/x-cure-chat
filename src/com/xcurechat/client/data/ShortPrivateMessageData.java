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
 */
package com.xcurechat.client.data;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ShortPrivateMessageData implements IsSerializable {
	
	//The maximum length of the message subject (Title)
	public static final int MAXIMUM_MESSAGE_SUBJECT_LENGTH = 128;
	
	//The unknown message ID constant 
	public static final int UNKNOWN_MESSAGE_ID = -1;
	
	//Various message types:
	//An unknown message
	public static final int UNKNOWN_MESSAGE_TYPE = -1;				
	//A regular private message
	public static final int SIMPLE_MESSAGE_TYPE = 0;				
	//A request for entering the room
	public static final int ROOM_ACCESS_REQUEST_MESSAGE_TYPE = 1;	
	//A notification that the room access is granted
	public static final int ROOM_ACCESS_GRANTED_MESSAGE_TYPE = 2;
	//A forum reply notification message
	public static final int FORUM_REPLY_NOTIFICATION_MESSAGE_TYPE = 3;

	protected int msgID = UNKNOWN_MESSAGE_ID;
	protected int fromUID = UserData.UNKNOWN_UID;
	protected String fromUserName = null;
	protected int toUID = UserData.UNKNOWN_UID;
	protected String toUserName = null;
	protected int messageType = UNKNOWN_MESSAGE_TYPE;
	protected boolean isRead = false;
	protected Date sendReceiveDate = null;
	//The room name for the room access related messages
	private String roomName = null;
	//The message title string
	protected String messageTitle = null;

	public ShortPrivateMessageData() {
		super();
	}

	/**
	 * @param messageTitle the messageTitle to set
	 */
	public void setMessageTitle(String messageTitle) {
		this.messageTitle = messageTitle;
	}

	/**
	 * @return the messageTitle
	 */
	public String getMessageTitle() {
		return messageTitle;
	}
	
	/**
	 * @param msgID the msgID to set
	 */
	public void setMsgID(int msgID) {
		this.msgID = msgID;
	}
	
	/**
	 * @return the msgID
	 */
	public int getMsgID() {
		return msgID;
	}
	
	/**
	 * @param fromUID the fromUID to set
	 */
	public void setFromUID(int fromUID) {
		this.fromUID = fromUID;
	}
	
	/**
	 * @return the fromUID
	 */
	public int getFromUID() {
		return fromUID;
	}

	/**
	 * @param fromUserName the fromUserName to set
	 */
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	/**
	 * @return the fromUserName
	 */
	public String getFromUserName() {
		return fromUserName;
	}

	/**
	 * @param toUID the toUID to set
	 */
	public void setToUID(int toUID) {
		this.toUID = toUID;
	}

	/**
	 * @return the toUID
	 */
	public int getToUID() {
		return toUID;
	}

	/**
	 * @param toUserName the toUserName to set
	 */
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	/**
	 * @return the toUserName
	 */
	public String getToUserName() {
		return toUserName;
	}

	/**
	 * @param messageType the messageType to set
	 */
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	/**
	 * @return the messageType
	 */
	public int getMessageType() {
		return messageType;
	}

	/**
	 * @param isRead the isRead to set
	 */
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	/**
	 * @return the isRead
	 */
	public boolean isRead() {
		return isRead;
	}

	/**
	 * @param sendReceiveDate the sendReceiveDate to set
	 */
	public void setSendReceiveDate(Date sendReceiveDate) {
		this.sendReceiveDate = sendReceiveDate;
	}

	/**
	 * @return the sendReceiveDate
	 */
	public Date getSendReceiveDate() {
		return sendReceiveDate;
	}
	
	/**
	 * @param roomName the roomName to set
	 */
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	/**
	 * @return the roomName
	 */
	public String getRoomName() {
		return roomName;
	}
}