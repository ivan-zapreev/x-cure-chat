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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ScrollPanel;

import com.xcurechat.client.SiteBodySectionContent;
import com.xcurechat.client.chat.ChatMessagesPanelUI;

import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.MessageFontData;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIInfoMessages;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;


/**
 * @author zapreevis
 * This class is responsible for showing the site description on the title page of the web-site.
 * This class is a singleton
 */
public class IntroductionPanelUI extends Composite implements SiteBodySectionContent {
	
	//The main decoration panel
	private final DecoratorPanel decorationPanel = new DecoratorPanel();
    //The table storing the site description, for the front page
    private FlexTable mainSiteDescriptionTable = new FlexTable();
    //The scroll panel used for fitting in all of the introduction messages
	private ScrollPanel scroll = new ScrollPanel();
	//The list of introduction messages
	private List<FakeMessage> introMsgs = new ArrayList<FakeMessage>();	
	//The instance of the internationalized info messages
	private final UIInfoMessages infoMessages = I18NManager.getInfoMessages();
	//The global variable used for marking info messages as left or right
	private boolean isLeftMessage = true;
	//The site section history prefix
	private final String siteSectionPrefix; 

	/**
	 * The basic constructor provided with the site section url prefix
	 * @param siteSectionPrefix the history token site section prefix
	 */
	public IntroductionPanelUI( final String siteSectionPrefix ) {
		super();
		
		//Store the data
		this.siteSectionPrefix = siteSectionPrefix;
		
		//Initialize the intro. messages
		initializeIntroMessages();
		//Initialize and add the site description table to the panel
		initializeMainSiteDescriptionTable();
		
		//Add the scroll panel enclosing the messages
		scroll.setStyleName( CommonResourcesContainer.CHAT_MESSAGES_SCROLL_PANEL_STYLE_NAME );
		scroll.add( mainSiteDescriptionTable );

		//Set up the decoration panel
		decorationPanel.add( scroll );
		decorationPanel.setStyleName( CommonResourcesContainer.COMMON_DECORATION_PANEL_STYLE );
		decorationPanel.addStyleName( CommonResourcesContainer.INTRODUCTION_PANEL_STYLE );
		
		//All composites must call initWidget() in their constructors.
		initWidget( decorationPanel );
	}
	
	/**
	 * Allows to update the panel's height on window resize
	 */
	@Override
	public void updateUIElements() {
		scroll.setHeight( InterfaceUtils.suggestMainViewHeight( scroll, 80) + "px");		
	}
	
	/**
	 * Allows to create and add a new chat message to the list of intro messages
	 * @param messageType the message type
	 * @param messageBody the message content
	 */
	private void addNewIntroMessage( final ChatMessage.Types messageType, final String messageBody ) {
		addNewIntroMessage( messageType, messageBody, null, null);
	}
		
	/**
	 * Allows to create and add a new chat message to the list of intro messages
	 * @param messageType the message type
	 * @param messageBody the message content
	 * @param msgImageURL the url to the intro image or null
	 * @param msgImageThumbURL the url to the intro image thumbnail or null
	 */
	private void addNewIntroMessage(final ChatMessage.Types messageType, final String messageBody,
									final String msgImageURL, final String msgImageThumbURL) {
		FakeMessage message = new FakeMessage();
		message.sentDate = new Date(); //The sent date has to be set
		message.messageType = messageType;
		message.messageBody = messageBody;
		message.originalURL = msgImageURL;
		message.thumbnailURL = msgImageThumbURL;
		message.isLeftMessage = isLeftMessage;
		message.fontSize = MessageFontData.LARGE_FONT_SIZE;
		message.fontType = MessageFontData.DEFAULT_FONT_FAMILY;
		switch( messageType  ) {
			case USER_ROOM_ENTER_INFO_MESSAGE_TYPE:
				message.fontColor = MessageFontData.FONT_COLOR_FOUR;
				message.avatar = FakeChatMessageAvatarUI.getUserEnterMessageAvatar( );
				break;
			case USER_ROOM_LEAVE_INFO_MESSAGE_TYPE:
				message.fontColor = MessageFontData.FONT_COLOR_FOUR;
				message.avatar = FakeChatMessageAvatarUI.getUserLeaveMessageAvatar( );
				break;
			case ROOM_IS_CLOSING_INFO_MESSAGE_TYPE:
				message.fontColor = MessageFontData.FONT_COLOR_FOUR;
				message.avatar = FakeChatMessageAvatarUI.getRoomClosingMessageAvatar( );
				break;
			case FAKE_ERROR_MESSAGE_TYPE:
				message.fontColor = MessageFontData.FONT_COLOR_THREE;
				message.avatar = FakeChatMessageAvatarUI.getErrorMessageAvatar( );
				break;
			case SIMPLE_MESSAGE_TYPE:
				message.fontColor = MessageFontData.FONT_COLOR_TWO;
				message.avatar = FakeChatMessageAvatarUI.getSimpleMessageAvatar( message.isLeftMessage );
				break;
			case PRIVATE_MESSAGE_TYPE:
				message.fontColor = MessageFontData.FONT_COLOR_FIVE;
				message.avatar = FakeChatMessageAvatarUI.getPrivateMessageAvatar( message.isLeftMessage );
				break;
			default:
				message.fontColor = MessageFontData.FONT_COLOR_ONE;
				message.avatar = FakeChatMessageAvatarUI.getSimpleMessageAvatar( message.isLeftMessage );
		}
		introMsgs.add( message );
		isLeftMessage = ! isLeftMessage;
	}
	
	/**
	 * Just initialized the chat messages
	 */
	private void initializeIntroMessages() {
		final String locale = InterfaceUtils.getCurrentLocale();
		final String introImgsBase = ServerSideAccessManager.getIntroductionImagesLocation();
		
		addNewIntroMessage( ChatMessage.Types.USER_ROOM_ENTER_INFO_MESSAGE_TYPE, infoMessages.introMessage01Text() );
		addNewIntroMessage( ChatMessage.Types.FAKE_ERROR_MESSAGE_TYPE, infoMessages.introMessage02Text() );
		addNewIntroMessage( ChatMessage.Types.PRIVATE_MESSAGE_TYPE, infoMessages.introMessage03Text() );
		addNewIntroMessage( ChatMessage.Types.SIMPLE_MESSAGE_TYPE, infoMessages.introMessage04Text(),
							introImgsBase + "chat_message_image.png",
							introImgsBase + "chat_message_image_thumb.png");
		addNewIntroMessage( ChatMessage.Types.PRIVATE_MESSAGE_TYPE, infoMessages.introMessage05Text() );
		addNewIntroMessage( ChatMessage.Types.SIMPLE_MESSAGE_TYPE, infoMessages.introMessage06Text(),
							introImgsBase + "transliteration.png",
							introImgsBase + "transliteration_thumb.png");
		addNewIntroMessage( ChatMessage.Types.PRIVATE_MESSAGE_TYPE, infoMessages.introMessage07Text(),
							introImgsBase + "message_flows_" +locale+".png",
							introImgsBase + "message_flows_thumb_" +locale+".png" );
		addNewIntroMessage( ChatMessage.Types.SIMPLE_MESSAGE_TYPE, infoMessages.introMessage08Text(),
							introImgsBase + "set_up_profile_" +locale+".png",
							introImgsBase + "set_up_profile_thumb_" +locale+".png" );
		addNewIntroMessage( ChatMessage.Types.PRIVATE_MESSAGE_TYPE, infoMessages.introMessage09Text(),
							introImgsBase + "user_search_" +locale+".png",
							introImgsBase + "user_search_thumb_" +locale+".png" );
		addNewIntroMessage( ChatMessage.Types.SIMPLE_MESSAGE_TYPE, infoMessages.introMessage10Text(),
							introImgsBase + "create_chat_room_" +locale+".png",
							introImgsBase + "create_chat_room_thumb_" +locale+".png" );
		addNewIntroMessage( ChatMessage.Types.PRIVATE_MESSAGE_TYPE, infoMessages.introMessage11Text(),
							introImgsBase + "offline_message_" +locale+".png",
							introImgsBase + "offline_message_thumb_" +locale+".png" );
		addNewIntroMessage( ChatMessage.Types.SIMPLE_MESSAGE_TYPE, infoMessages.introMessage12Text() );
		addNewIntroMessage( ChatMessage.Types.PRIVATE_MESSAGE_TYPE, infoMessages.introMessage13Text(),
							introImgsBase + "moderation_" +locale+".png",
							introImgsBase + "moderation_thumb_" +locale+".png" );
		addNewIntroMessage( ChatMessage.Types.USER_ROOM_LEAVE_INFO_MESSAGE_TYPE, infoMessages.introMessage14Text() );
	}
	
	@Override
	public void setUserLoggedIn() {
		//NOTE: Does nothing since this is a static component
	}
	
	@Override
	public void setUserLoggedOut() {
		//NOTE: Does nothing since this is a static component
	}
	
	/**
	 * Initializes the main site description table
	 */
	private void initializeMainSiteDescriptionTable() {
		int numberOfRows = 0;
		Iterator<FakeMessage> msgIter = introMsgs.iterator();
		while( msgIter.hasNext() ) {
			FakeMessage message = msgIter.next();
			numberOfRows = ChatMessagesPanelUI.addElementsToTable( mainSiteDescriptionTable, numberOfRows, message.avatar,
																   FakeChatMessageUI.getChatMessageUI(message), message.isLeftMessage);
		}
	}
	
	@Override
	public void onAfterComponentIsAdded() {
		//Update the current history item with the selected section item
		History.newItem( siteSectionPrefix, false );
	}

	@Override
	public void onBeforeComponentIsAdded() {
		//Does nothing
	}
	
	@Override
	public void onBeforeComponentIsRemoved() {
		//Does nothing
	}
	
	@Override
	public void processHistoryToken(String historyToken) {
		//NOTE: Nothing to be done here
	}

	@Override
	public void updateTargetHistoryToken(Anchor anchorLink) {
		anchorLink.setHref( CommonResourcesContainer.URI_HASH_SYMBOL + siteSectionPrefix );
	}

	@Override
	public void setEnabled(boolean enabled) {
		//NOTE: Nothing to be done here
	}
}
