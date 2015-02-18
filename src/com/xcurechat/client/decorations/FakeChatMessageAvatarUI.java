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
package com.xcurechat.client.decorations;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;

import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class is responsible for providing the fake avatars for the
 * chat messages displayed at the title page of the web site
 */
public class FakeChatMessageAvatarUI extends Composite {
	
	private final Label avatarTitleLabel = new Label();
	private final Image avatarImage = new Image( );
	//The panel storing the avatar image and the avatar title
	private VerticalPanel mainVerticalPanel = new VerticalPanel();
	
	/**
	 * The basic constructor
	 */
	private FakeChatMessageAvatarUI( ) {
		mainVerticalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainVerticalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		mainVerticalPanel.add( avatarImage );
		mainVerticalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		avatarTitleLabel.setWordWrap( false );
		mainVerticalPanel.add( avatarTitleLabel );
		
		//All composites must call initWidget() in their constructors.
		initWidget( mainVerticalPanel );		
	}
	
	/**
	 * Allows to get the fake info/public/private message's user avatar
	 * @param messageType the chat message type
	 * @param isLeftMessage is only valid for the the message types:
	 * Message.SIMPLE_MESSAGE_TYPE and Message.PRIVATE_MESSAGE_TYPE
	 * This parameter allows to get the left and right admin avatars
	 * @return the avatar for the message of the given type
	 */
	private static FakeChatMessageAvatarUI getMessageAvatar( final ChatMessage.Types messageType, final boolean isLeftMessage ) {
		final FakeChatMessageAvatarUI avatar = new FakeChatMessageAvatarUI( );
		if( ( messageType == ChatMessage.Types.SIMPLE_MESSAGE_TYPE ) ||
			( messageType == ChatMessage.Types.PRIVATE_MESSAGE_TYPE ) ) {
			//Set the avatar image border depending on message type
			if( messageType == ChatMessage.Types.SIMPLE_MESSAGE_TYPE ) {
				avatar.avatarImage.addStyleName( CommonResourcesContainer.SIMPLE_MESSAGE_AVATAR_STYLE );
			} else {
				avatar.avatarImage.addStyleName( CommonResourcesContainer.PRIVATE_MESSAGE_AVATAR_STYLE );
			}
			//Set the face user avatar
			avatar.avatarImage.setUrl( ServerSideAccessManager.getAdministratorAvatarImageURL( isLeftMessage ) );
			setUserLoginName( avatar, I18NManager.getTitles().administratorAvatarTitle(), messageType );
		} else {
			//This must be an info message avatar
			switch( messageType ) {
				case USER_ROOM_ENTER_INFO_MESSAGE_TYPE:
					avatar.avatarImage.setUrl( ServerSideAccessManager.getUserEnterMessageAvatarImageURL( ) );
					break;
				case USER_ROOM_LEAVE_INFO_MESSAGE_TYPE:
					avatar.avatarImage.setUrl( ServerSideAccessManager.getUserLeaveMessageAvatarImageURL( ) );
					break;
				default:
					avatar.avatarImage.setUrl( ServerSideAccessManager.getDefaultInfoMessageAvatarImageURL( ) );
			}
			avatar.avatarImage.setStyleName( CommonResourcesContainer.INFO_MESSAGE_AVATAR_STYLE );
			updateSystemMessageAvatarTitle( avatar, false );
		}
		return avatar;
	}
	
	public static FakeChatMessageAvatarUI getUserEnterMessageAvatar( ) {
		return getMessageAvatar( ChatMessage.Types.USER_ROOM_ENTER_INFO_MESSAGE_TYPE, false );
	}
	
	public static FakeChatMessageAvatarUI getUserLeaveMessageAvatar( ) {
		return getMessageAvatar( ChatMessage.Types.USER_ROOM_LEAVE_INFO_MESSAGE_TYPE, false );
	}
	
	public static FakeChatMessageAvatarUI getSimpleMessageAvatar( final boolean isLeftMessage ) {
		return getMessageAvatar( ChatMessage.Types.SIMPLE_MESSAGE_TYPE, isLeftMessage );
	}
	
	public static FakeChatMessageAvatarUI getPrivateMessageAvatar( final boolean isLeftMessage ) {
		return getMessageAvatar( ChatMessage.Types.PRIVATE_MESSAGE_TYPE, isLeftMessage );
	}
	
	public static FakeChatMessageAvatarUI getRoomClosingMessageAvatar( ) {
		return getMessageAvatar( ChatMessage.Types.ROOM_IS_CLOSING_INFO_MESSAGE_TYPE, false );
	}
	
	/**
	 * @return an avatar for the chat error message
	 */
	public static FakeChatMessageAvatarUI getErrorMessageAvatar() {
		//Here we are adding the error message for which it is not important to set the chat room ID
		FakeChatMessageAvatarUI avatar = new FakeChatMessageAvatarUI( );
		avatar.avatarImage.setUrl( ServerSideAccessManager.getErrorMessageAvatarImageURL( ) );
		avatar.avatarImage.setStyleName( CommonResourcesContainer.ERROR_MESSAGE_AVATAR_STYLE );
		updateSystemMessageAvatarTitle( avatar, true );
		return avatar;
	}
	
	/**
	 * Updates the avatar with the user login name as a title
	 * @param avatar the avatar to work with
	 * @param loginName the user login name to user
	 * @param messageType the type of the user message: private or simple
	 */
	private static void setUserLoginName( final FakeChatMessageAvatarUI avatar, final String userLoginName, final ChatMessage.Types messageType ) {
		if( messageType == ChatMessage.Types.PRIVATE_MESSAGE_TYPE ) {
			avatar.mainVerticalPanel.setStyleName( CommonResourcesContainer.PRIVATE_MESSAGE_FAKE_AVATAR_PANEL_STYLE );
		} else {
			avatar.mainVerticalPanel.setStyleName( CommonResourcesContainer.SIMPLE_MESSAGE_FAKE_AVATAR_PANEL_STYLE );
		}
		avatar.avatarTitleLabel.setText( UserData.getShortLoginName( userLoginName ) );
		avatar.mainVerticalPanel.setTitle( userLoginName );
	}
	
	/**
	 * Updates the system message avatar with the system string name
	 * @param avatar the avatar to update
	 * @param isError true for the error message false for the info message
	 */
	private static void updateSystemMessageAvatarTitle( FakeChatMessageAvatarUI avatar, final boolean isError ) {
		final String avatarTitle = I18NManager.getTitles().systemChatMessageAvatarTitle();
		avatar.avatarImage.setTitle( avatarTitle );
		avatar.avatarTitleLabel.setText( ShortUserData.getShortLoginName( avatarTitle ) );
		avatar.avatarTitleLabel.setTitle( avatarTitle );
		avatar.mainVerticalPanel.setStyleName( ( isError ? CommonResourcesContainer.ERROR_MESSAGE_AVATAR_PANEL_STYLE : CommonResourcesContainer.INFO_MESSAGE_AVATAR_PANEL_STYLE ) );
	}
}
