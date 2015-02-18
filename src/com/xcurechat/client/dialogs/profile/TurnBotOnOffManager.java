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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.dialogs.profile;

import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class is not an UI element but it contains several of them.
 * First the image and second the label for making the user a bot and not a bot
 */
public class TurnBotOnOffManager implements ClickHandler {
	private boolean isBot;
	private final int userID;
	
	//The objects storing the current status
	private final Image isBotImg = new Image();
	private final Label isBotLabel = new Label();
	
	//Handler registrations
	HandlerRegistration imageHandlerReg = null;
	HandlerRegistration labelHandlerReg = null;

	//The localization for the text
	private final UITitlesI18N titlesI18N = I18NManager.getTitles();
	
	/**
	 * Simple constructor
	 * @param userID the Id of the user that can be added/removed as a Friend
	 */
	public TurnBotOnOffManager( final int userID ) {
		this.userID = userID;
		isBotLabel.setWordWrap( false );
		isBotImg.setStyleName( CommonResourcesContainer.ACTION_IMAGE_LINK_STYLE );
	}
	
	/**
	 * @return the image
	 */
	public Image getIsBotImage() {
		return isBotImg;
	}
	
	/**
	 * @return the label
	 */
	public Label getIsBotLinkLabel() {
		return isBotLabel;
	}
	
	/**
	 * Set if the action should be to enable the bot or to disable it
	 * @param isBot if true then the user is a bot if false then he is not
	 */
	public void initializeIsBot(final boolean isBot ) {
		setFriendStatus( isBot, false, false );
	}
	
	public void onClick(ClickEvent e) {
		setFriendStatus(false, true, false);
		
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				//Set the user to be a bot or not on the server
				AsyncCallback<Void> setBotCallBack = new AsyncCallback<Void>() {
					public void onSuccess( Void result ) {
						//Set the "is friend" profile status 
						setFriendStatus( ! isBot, false, false );
					}
					public void onFailure(final Throwable caught) {
						//Report the error
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								ErrorMessagesDialogUI.openErrorDialog(caught);
							}
						}).loadAndExecute();
						//Do the recovery
						recover();
					}
				};
				UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
				userManagerObject.enableBot( SiteManager.getUserID(), SiteManager.getUserSessionId(), userID, ! isBot, setBotCallBack );
			}
			@Override
			public void recover() {
				setFriendStatus( isBot, false, true );
			}
		}).loadAndExecute();
	}
	
	private void setFriendStatus(final boolean isBot, final boolean startProgress, final boolean isError ){
		if( startProgress ) {
			isBotImg.setUrl( ServerSideAccessManager.getActivityImageURL() );
			isBotImg.setTitle( titlesI18N.communicatingToolTipText() );
			isBotLabel.setText( titlesI18N.communicatingText() );
			isBotLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
			//Remove adding/removing friend click listener
			if( imageHandlerReg != null ) {
				imageHandlerReg.removeHandler();
				imageHandlerReg = null;
			}
			if( labelHandlerReg != null ){
				labelHandlerReg.removeHandler();
				labelHandlerReg = null;
			}
		} else {
			if( isError ) {
				isBotImg.setUrl( ServerSideAccessManager.getErrorImageURL() );
				isBotImg.setTitle( titlesI18N.errorWhileCommunicatingToolTipText() );
				isBotLabel.setText( titlesI18N.errorText() );
				isBotLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
			} else {
				if( isBot ) {
					isBotImg.setUrl( ServerSideAccessManager.getBotActionLinkImageIconURL(true) );
					isBotImg.setTitle( titlesI18N.disableBotText() );
					isBotLabel.setText( titlesI18N.disableBotText() );
					isBotLabel.setStyleName( CommonResourcesContainer.DIALOG_LINK_RED_STYLE );
				} else {
					isBotImg.setUrl( ServerSideAccessManager.getBotActionLinkImageIconURL(false) );
					isBotImg.setTitle( titlesI18N.enableBotText() );
					isBotLabel.setText( titlesI18N.enableBotText() );
					isBotLabel.setStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
				}
				//Remember if the user is a bot or not
				this.isBot = isBot;
				//Adding adding/removing friend click listener 
				imageHandlerReg = isBotImg.addClickHandler( this ); 
				labelHandlerReg = isBotLabel.addClickHandler( this ); 
			}
		}
	}
}
