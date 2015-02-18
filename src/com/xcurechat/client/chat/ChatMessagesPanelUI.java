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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.DOM;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

import com.xcurechat.client.NewMessageAlertWidget;
import com.xcurechat.client.SiteManager;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.rpc.exceptions.RoomAccessException;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;

/**
 * @author zapreevis
 * Represents the chat messages panel
 */
public class ChatMessagesPanelUI extends Composite implements UserAvatarImageWidget.AvatarSpoilerChangeListener {
	//Column indexes
	private static final int FIRST_COLUMN_IDX = 0;
	private static final int SECOND_COLUM_IDX = 1;
	private static final int THIRD_COLUMN_IDX = 2;
	
	//The maximum number of messages to be shown in the chat room
	private static final int MAXIMUM_NUMBER_OF_VISIBLE_ROOM_MESSAGES = 200;
	
	/**
	 * @author zapreevis
	 * The private scroll manager class that helps to manage message scrolling 
	 */
	private class ScrollManager {
		//Scrolling related variables
		private int oldScrollPosition;
		private int oldScrollTop;
		private int oldScrollHeight;
		private int oldScrollClientHeight;
		private boolean isScrollerAtTheBottom;
		
		/**
		 * Remembers the current scrolling position. MUST BE CALLED
		 * before the new chat message is added or appended to the
		 * previous chat-message UI. 
		 */
		public void rememberCurrentScrollPosition() {
			//Store the old height of the message panel
			oldScrollPosition = msgsScrollPanel.getVerticalScrollPosition();
			//Determine if the scroll bar was moved from the bottom position or not
			oldScrollTop = msgsScrollPanel.getVerticalScrollPosition();
			oldScrollHeight = DOM.getElementPropertyInt( msgsScrollPanel.getElement(), "scrollHeight");
			oldScrollClientHeight = DOM.getElementPropertyInt( msgsScrollPanel.getElement(), "clientHeight");
			isScrollerAtTheBottom = ( ( oldScrollClientHeight + oldScrollTop ) == oldScrollHeight ) ;
		}
		
		/**
		 * Scrolls the messages own if needed. MUST BE CALLED
		 * before the new chat message is added or appended to the
		 * previous chat-message UI. 
		 */
		public void updateScrollPosition() {
			//If the scroll is enabled then
			if( ( oldScrollPosition == 0 ) && !isFirstScroll ) {
				//Try to scroll down
				msgsScrollPanel.scrollToBottom();
				if( msgsScrollPanel.getVerticalScrollPosition() > 0 ) {
					//If scrolling was successful then we did the first scroll down
					isFirstScroll = true;
				}
			} else {
				//We scrolled down at least once, now we need to know
				//if the user moved the scroll bar from the bottom or not
				if( isScrollerAtTheBottom ) {
					//If the scroll bar was at the bottom before adding the new 
					//message, we scroll down again to keep the new message visible
					msgsScrollPanel.scrollToBottom();
				}
			}
		}
	}
	
	//The scroll panel that stores the chat messages table
	private final ScrollPanel msgsScrollPanel = new ScrollPanel();
	//The table that stores the chat messages and user avatars
	private final FlexTable table = new FlexTable();
	//The ordered list of visible user avatar widgets that has
	//the same order of widgets as in the messages/avatars table 
	private final List<Widget> avatars = new ArrayList<Widget>();
	//Indicates if the next message will be left or right
	private boolean isLeft = true;
	//True if there was an initial scrolling down to the bottom of the chat msgs
	private boolean isFirstScroll = false;
	//The current number of chat message rows
	private int numberOfRows = 0;
	//The id of the room to which this messages panel belongs
	private final int roomID;
	//The scrolling manager for the chat messages
	private final ScrollManager scrollManager = new ScrollManager();

	/**
	 * The basic constructor
	 * @param roomID the id of the room to which this messages panel belongs
	 */
	public ChatMessagesPanelUI( final int roomID ) {
		//Store the room's ID
		this.roomID = roomID;
		
		//Set interface components
		table.setStyleName( CommonResourcesContainer.CHAT_MESSAGES_TABLE_STYLE_NAME );
		VerticalPanel bottomAlignmentPanel = new VerticalPanel();
		bottomAlignmentPanel.addStyleName( CommonResourcesContainer.CHAT_MESSAGES_VERTICAL_ALIGN_PANEL_STYLE_NAME );
		bottomAlignmentPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		bottomAlignmentPanel.add( table );
		
		msgsScrollPanel.add( bottomAlignmentPanel );
		msgsScrollPanel.setStyleName( CommonResourcesContainer.CHAT_MESSAGES_SCROLL_PANEL_STYLE_NAME );
		
		/*Initialize the composite widget*/
		initWidget( msgsScrollPanel );
	}
	
	/**
	 * Scrolls down the messages
	 */
	public void scrollDownMessages() {
		msgsScrollPanel.scrollToBottom();
	}
	
	/**
	 * Returns true if the next message we add should be a left message otherwise false.
	 * As a side effect this method sets the next message type.
	 * @return true if the next message should be a left message, otherwise false
	 */
	private boolean updateIsLeftMessageStatus() {
		boolean isThisLeft = isLeft;
		isLeft = ! isLeft;
		return isThisLeft;
	}
	
	/**
	 * Removes the old messages is the number room messages exceeds MAXIMUM_NUMBER_OF_VISIBLE_ROOM_MESSAGES
	 */
	public void removeExtraTopMessages() {
		final int extraMsgsCount = numberOfRows - MAXIMUM_NUMBER_OF_VISIBLE_ROOM_MESSAGES; 
		if( extraMsgsCount > 0 ) {
			FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
			for( int row = 0; row < extraMsgsCount; row++ ) {
				//Always remove the top most row in the table but take care
				//about the cells and row spans for the avatar columns
				if( cellFormatter.getRowSpan(0, FIRST_COLUMN_IDX) > 1 ){
					Widget avatar = table.getWidget(0, FIRST_COLUMN_IDX);
					table.insertCell(1, FIRST_COLUMN_IDX);
					cellFormatter.setStyleName( 1, FIRST_COLUMN_IDX, CommonResourcesContainer.CHAT_ENTRY_AVATAR_LEFT_STYLE );
					table.setWidget(1, FIRST_COLUMN_IDX, avatar);
				} else {
					Widget avatar = table.getWidget(0, table.getCellCount(0) - 1 );
					table.insertCell(1, THIRD_COLUMN_IDX);
					cellFormatter.setStyleName( 1, THIRD_COLUMN_IDX, CommonResourcesContainer.CHAT_ENTRY_AVATAR_RIGHT_STYLE );
					table.setWidget(1, THIRD_COLUMN_IDX, avatar);
				}
				table.removeRow( 0 );
				avatars.remove( 0 );
				numberOfRows--;
			}
		}
	}
	
	/**
	 * Allows to add a chat message entry to the flex table.
	 * Does not set any chat message background highlight.
	 * @param table the table to add the message to
	 * @param numberOfRows the current number of rows in the flex table
	 * @param avatarWidget the avatar widget
	 * @param messageWidget the message widget
	 * @param isLeftMessage true if this is a left message, false for the right one
	 * @return the current number of wors in the table
	 */
	public static int addElementsToTable( final FlexTable table, final int numberOfRows,
										  final Widget avatarWidget, final Widget messageWidget,
										  final boolean isLeftMessage ) {
		return addElementsToTable( table, null, numberOfRows, avatarWidget, messageWidget, isLeftMessage );
	}
	
	/**
	 * Allows to add a chat message entry to the flex table
	 * @param table the table to add the message to
	 * @param avatars the array of registered avatar widgets to be appended
	 * 				  with the newly added avatar or null if we do not want
	 * 				  to keep trak of avatars.
	 * @param numberOfRows the current number of rows in the flex table
	 * @param avatarWidget the avatar widget
	 * @param messageWidget the message widget
	 * @param isLeftMessage true if this is a left message, false for the right one
	 * @return the current number of wors in the table
	 */
	private static int addElementsToTable( final FlexTable table, List<Widget> avatars,
										   final int numberOfRows, final Widget avatarWidget,
										   final Widget messageWidget, final boolean isLeftMessage ) {
		//Insert a new row
		final int newRowIndex = numberOfRows;
		table.insertRow( newRowIndex );
		
		//Get the cell formatter
		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		
		//Determine the avatar's row and column index and span 
		//the avatar's cell across the rows, if possible
		final int avatarRowIdx, avatarColIdx, emptyColIdx, messageColIdx;
		if( newRowIndex > 0 ) {
			avatarRowIdx = newRowIndex - 1;
			if( isLeftMessage ) {
				avatarColIdx  = FIRST_COLUMN_IDX;
				messageColIdx = FIRST_COLUMN_IDX;
				emptyColIdx   = SECOND_COLUM_IDX;
				cellFormatter.setRowSpan(avatarRowIdx,  avatarColIdx, 2);
				table.insertCell( newRowIndex, messageColIdx );
				table.insertCell( newRowIndex, emptyColIdx );
			} else {
				avatarColIdx  = ( newRowIndex == 1 ) ? THIRD_COLUMN_IDX : SECOND_COLUM_IDX;
				messageColIdx = SECOND_COLUM_IDX;
				emptyColIdx   = FIRST_COLUMN_IDX;
				table.insertCell( newRowIndex, emptyColIdx );
				table.insertCell( newRowIndex, messageColIdx );
				cellFormatter.setRowSpan(avatarRowIdx, avatarColIdx, 2);
			}
		} else {
			avatarRowIdx = newRowIndex;
			messageColIdx = SECOND_COLUM_IDX;
			if( isLeftMessage ) {
				avatarColIdx  = FIRST_COLUMN_IDX;
				emptyColIdx   = THIRD_COLUMN_IDX;
			} else {
				avatarColIdx  = THIRD_COLUMN_IDX;
				emptyColIdx   = FIRST_COLUMN_IDX;
			}
			table.insertCell( newRowIndex, FIRST_COLUMN_IDX );
			table.insertCell( newRowIndex, SECOND_COLUM_IDX );
			table.insertCell( newRowIndex, THIRD_COLUMN_IDX );
		}
		
		//Insert the avatar and the chat message itself with the proper styles
		final String messageStyle;
		final String avatarStyle;
		if( isLeftMessage ) {
			messageStyle = CommonResourcesContainer.CHAT_ENTRY_MESSAGE_LEFT_STYLE;
			avatarStyle  = CommonResourcesContainer.CHAT_ENTRY_AVATAR_LEFT_STYLE;
		} else {
			messageStyle = CommonResourcesContainer.CHAT_ENTRY_MESSAGE_RIGHT_STYLE;
			avatarStyle  = CommonResourcesContainer.CHAT_ENTRY_AVATAR_RIGHT_STYLE;
		}
		table.setWidget( newRowIndex, emptyColIdx, new HTML("&nbsp;") );
		cellFormatter.setStyleName( newRowIndex, messageColIdx, messageStyle );
		table.setWidget( newRowIndex, messageColIdx, messageWidget );
		cellFormatter.setStyleName( avatarRowIdx, avatarColIdx, avatarStyle );
		table.setWidget( avatarRowIdx, avatarColIdx, avatarWidget );
		
		//Add the new avatar to the avatars list, if it not null
		if( avatars != null ) {
			avatars.add( avatarWidget );
		}
		
		return numberOfRows + 1;
	}
	
	/**
	 * Appends the widget representing a new entry in the chat messages
	 * @param avatarWidget the chat-message avatar widget
	 * @param messageWidget the chat message widget itself
	 * @param isFirst should be true for the first message out of the chunk of new messages
	 * @param isLeftMessage true if this is a left message, false for the right one
	 */
	private void appendChatEntryWidget( final Widget avatarWidget, final Widget messageWidget, final boolean isLeftMessage ){
		//Remember the scroll position before adding the chat message
		scrollManager.rememberCurrentScrollPosition();
		
		//Add the avatar and the message to the panel
		numberOfRows = addElementsToTable( table, avatars, numberOfRows, avatarWidget, messageWidget, isLeftMessage );
		
		//Update the scroller position based on the scroll status
		scrollManager.updateScrollPosition();
	}
	
	/**
	 * Put new room messages into the chat window if they come from a non-blocked user
	 * @param newMsgs the list of new room messages
	 * @param visibleUsers the map storing the mapping between
	 * the visible userIDs and their short data objects
	 */
	public void addNewRoomMessages( List<ChatMessage> newMsgs , final Map<Integer, ShortUserData> visibleUsers ) {
		//Add new room messages into the chat, one by one
		for( int i = 0; i < newMsgs.size(); i++ ) {
			addNewRoomMessage( newMsgs.get( i ), visibleUsers );
		}
		//Remove the extra rows for the old messages
		removeExtraTopMessages();
	}
	
	//The last appended chat room message
	private ChatMessage lastAddedChatMessage = null;
	private ChatMessageUI lastAddedChatMessageUI = null;
	//If true then the last appended message was an error message
	private boolean isErrorMessageAdded = false;
	
	/**
	 * Put a new room message into the chat window if it comes from a non-blocked user
	 * Only adds the message entry if the message ID is strictly larger than the 
	 * last added message ID. Note that messages come in ascending order, by ID.
	 * @param ChatMessage the new room message
	 * @param visibleUsers the map storing the mapping between
	 * the visible userIDs and their short data objects
	 */
	public void addNewRoomMessage( ChatMessage message, final Map<Integer, ShortUserData> visibleUsers ) {
		//If the sender is not blocked by the user then we add this new message
		if( ! SiteManager.isUserBlocked( message.senderID ) &&
			! SiteManager.isUserBlocked( message.infoUserID ) ) {
			//Get the last appended message id
			final int lastAppendedMessageID = ( lastAddedChatMessage == null ? ChatMessage.UNKNOWN_MESSAGE_ID : lastAddedChatMessage.messageID );
			
			//Check that the message we are trying to add has an ID larger
			//than that of the previous one, trying to avoid duplicates
			if( message.messageID > lastAppendedMessageID ) {
				//Notify about the new chat messages addressed to us or to all
				if( NewMessageAlertWidget.getInstance().isNewChatMessageNotificationActual() &&
					message.isFirstRecipient(  SiteManager.getUserID() ) ) {
					NewMessageAlertWidget.getInstance().newChatMessage();
				}
				
				//Check if the new message can be appended to the previously 
				//added message or it has to be added as a new one
				if( ! isErrorMessageAdded && ( lastAddedChatMessage != null ) && ( lastAddedChatMessageUI != null ) &&
					! lastAddedChatMessageUI.isMessageMinimized() && lastAddedChatMessage.isAppendable( message ) ) {
					//Remember the scroll position before appending the chat message content
					scrollManager.rememberCurrentScrollPosition();
					
					//Append the new message data to the last message UI
					lastAddedChatMessageUI.appendMessageContent( message, visibleUsers, roomID );
					
					//Alert the last chat message avatar widget, if this is possible
					if( avatars.size() > 0 ) {
						Widget lastAvatarWidget = avatars.get(  avatars.size() - 1 );
						if( lastAvatarWidget instanceof ChatMessageAvatarUI ) {
							( (ChatMessageAvatarUI) lastAvatarWidget).startTempAvatarWidgetAlert();
						}
					}
					
					//Update the scroller position based on the scroll status
					scrollManager.updateScrollPosition();
				} else {
					//Check if this is the left message and update the internal status
					//WARNING: This call has side effects!!!
					final boolean isLeftMessage = updateIsLeftMessageStatus();
					//Get the new chat message UI and store it as the last appended
					lastAddedChatMessageUI = ChatMessageUI.getChatMessageUI( isLeftMessage, message, visibleUsers, roomID );
					//Append a new message
					appendChatEntryWidget( ChatMessageAvatarUI.getMessageAvatar( message, visibleUsers, roomID ),
										   lastAddedChatMessageUI, isLeftMessage  );
				}
				//Update the last appended message data
				lastAddedChatMessage = message;
				//This method does not append error messages
				isErrorMessageAdded = false;
			}
		}
	}
	
	/**
	 * Allows to set on the alerts on the user avatars, the alerts are set on the
	 * avatars which belong to the messages directly addressing the current user.
	 * @param isOn true if we want to set the avatars constantly on, false for "constantly" off
	 */
	public void setAvatarAlerts( final boolean isOn ) {
		for( int index = 0; index < avatars.size(); index ++ ) {
			Widget lastAvatarWidget = avatars.get(  index );
			if( lastAvatarWidget instanceof ChatMessageAvatarUI ) {
				if( isOn ) {
					( (ChatMessageAvatarUI) lastAvatarWidget).startConstAvatarWidgetAlert();
				} else {
					( (ChatMessageAvatarUI) lastAvatarWidget).stopConstAvatarWidgetAlert();
				}
			}
		}
	}
	
	/**
	 * Place the received error message into to the chat as a system messages 
	 * @param exception the room exception to visualize as a chat message
	 * @param theChatRoomData the chat room's data, for the room we display the error message
	 */
	public void addRoomErrorMessage( RoomAccessException exception, final ChatRoomData theChatRoomData ) {
		final boolean isLeftMessage = updateIsLeftMessageStatus();
		//Add the error message to the chat
		appendChatEntryWidget( ChatMessageAvatarUI.getErrorMessageAvatar(),
							   new ErrorMessageUI( exception, theChatRoomData, isLeftMessage ),
							   isLeftMessage );
		//Remove the extra rows for the old messages
		removeExtraTopMessages();
		//This method does not append error messages
		isErrorMessageAdded = true;
	}
	
	@Override
	public void avatarSpoilerChanged(final int userID, final int spoilerID, final Date spoilerExpDate) {
		for( int row = 0; row < table.getRowCount(); row++ ) {
			Widget leftWidget = table.getWidget(row, FIRST_COLUMN_IDX );
			//There is either two or three columns in the row, the
			//last one should have the avatar or an empty html widget
			Widget rightWidget = table.getWidget(row, table.getCellCount(row) - 1 );
			if( leftWidget instanceof ChatMessageAvatarUI ) {
				(( ChatMessageAvatarUI ) leftWidget).updateThisAvatarSpoiler( userID, spoilerID, spoilerExpDate );
			} else {
				if( rightWidget instanceof ChatMessageAvatarUI ) {
					(( ChatMessageAvatarUI ) rightWidget).updateThisAvatarSpoiler( userID, spoilerID, spoilerExpDate );
				}
			}
		}
	} 
}
