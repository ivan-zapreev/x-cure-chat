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

import java.util.List;
import java.util.Date;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.MessageFontData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.exceptions.RoomAccessException;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class represents the UI interface for the error message posted into the chat
 */
public class ErrorMessageUI extends ChatMessageBaseUI {
	/**
	 * The exception that occurred while we were working with the given room
	 * @param exception the site exception
	 * @param theChatRoomData the chat room we are in
	 * @param isLeft if true then we apply the left message style, otherwise right
	 */
	public ErrorMessageUI( final RoomAccessException exception, final ChatRoomData theChatRoomData, final boolean isLeft ) {
		super( CommonResourcesContainer.ERROR_MESSAGE_LEFT_UI_STYLE,
			   CommonResourcesContainer.ERROR_MESSAGE_RIGHT_UI_STYLE, isLeft,
			   ChatRoomData.UNKNOWN_ROOM_ID, ShortUserData.UNKNOWN_UID, null, false );
		
		//Add default font size and font type
		addStyleNameToContent( MessageFontData.getFontTypeStyle(  MessageFontData.DEFAULT_FONT_FAMILY ) );
		addStyleNameToContent( MessageFontData.getFontSizeStyle( MessageFontData.DEFAULT_FONT_SIZE ) );
		
		//Add the message title and spacing
		FocusPanel resultPanel = addMessageTitlePanel( new Date(), i18nTitles.chatMessageTypeError(), false, null );
		resultPanel.addStyleName( CommonResourcesContainer.ERROR_MESSAGE_TITLE_STYLE );
		
		//Add error message text
		exception.setRoomName( ChatRoomData.getRoomName( theChatRoomData ) );
		exception.processErrorCodes( I18NManager.getErrors() );
		List<String> errorMsgs = exception.getErrorMessages();
		String longErrorMessage = "";
		for(int i=0; i< errorMsgs.size(); i++){
			longErrorMessage +=  errorMsgs.get( i ) + " ";
		}
		addMessageContentWidget( new HTML( longErrorMessage ) );
	}
}
