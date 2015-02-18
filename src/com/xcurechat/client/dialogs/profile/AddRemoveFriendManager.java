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
 * This class is not an UI element but ic contains several of the.
 * First the image and second the label for managing friends
 */
public class AddRemoveFriendManager implements ClickHandler {
	private boolean doRemove;
	private final int friedID;
	
	//The action/add friend/remove friend image for loading/adding/removing friend status
	private final Image friendImg = new Image();
	private final Label friendLabel = new Label();
	
	//Handler registrations
	HandlerRegistration imageHandlerReg = null;
	HandlerRegistration labelHandlerReg = null;

	//The localization for the text
	private final UITitlesI18N titlesI18N = I18NManager.getTitles();
	
	/**
	 * Simple constructor
	 * @param friedID the Id of the user that can be added/removed as a Friend
	 */
	public AddRemoveFriendManager( final int friedID ) {
		this.friedID = friedID;
		friendLabel.setWordWrap( false );
		friendImg.setStyleName( CommonResourcesContainer.ACTION_IMAGE_LINK_STYLE );
	}
	
	/**
	 * @return the image with the "Add friend"/"Remove friend" picture
	 */
	public Image getFriendImage() {
		return friendImg;
	}
	
	/**
	 * @return the label with the "Add friend"/"Remove friend" link
	 */
	public Label getFriendLinkLabel() {
		return friendLabel;
	}
	
	/**
	 * Allows to retrieve the data bout being a friend form the server
	 * @param getFromServer if true then we request the server, otherwise
	 * use local data.
	 */
	public void populateFriendData(final boolean getFromServer) {
		if( getFromServer ) {
			setFriendStatus(false, true, false);
			
			//Ensure lazy loading
			(new SplitLoad(){
				@Override
				public void execute() {
					AsyncCallback<Boolean> isFriendCallBack = new AsyncCallback<Boolean>() {
						public void onSuccess(Boolean result) {
							setFriendStatus( result, false, false );
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
					userManagerObject.isFriend( SiteManager.getUserID(), SiteManager.getUserSessionId(), friedID, isFriendCallBack );
				}
				@Override
				public void recover() {
					setFriendStatus(false, false, true );
				}
			}).loadAndExecute();
		} else {
			setFriendStatus( SiteManager.isFriend( friedID ), false, false );
		}
	}
	
	/**
	 * Set if the action should be to remove the friend
	 * @param doRemove true to remove the friend, true to add
	 */
	public void setDoRemove(final boolean doRemove ) {
		this.doRemove = doRemove; 
	}
	
	public void onClick(ClickEvent e) {
		setFriendStatus(false, true, false);
		
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				//Retrieve the user-friend related data from the server 
				AsyncCallback<Boolean> isFriendCallBack = new AsyncCallback<Boolean>() {
					public void onSuccess( Boolean isFriend ) {
						//Update the local friend's list
						if( isFriend ) {
							SiteManager.addFriend(friedID);
						} else {
							SiteManager.removeFriend(friedID);
						}
						//Set the "is friend" profile status 
						setFriendStatus( isFriend, false, false );
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
				userManagerObject.manageFriend( SiteManager.getUserID(), SiteManager.getUserSessionId(), friedID, doRemove, isFriendCallBack );
			}
			@Override
			public void recover() {
				setFriendStatus(false, false, true );
			}
		}).loadAndExecute();
	}
	
	private void setFriendStatus(final boolean isFriend, final boolean startProgress, final boolean isError ){
		if( startProgress ) {
			friendImg.setUrl( ServerSideAccessManager.getActivityImageURL() );
			friendImg.setTitle( titlesI18N.communicatingToolTipText() );
			friendLabel.setText( titlesI18N.communicatingText() );
			friendLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
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
				friendImg.setUrl( ServerSideAccessManager.getErrorImageURL() );
				friendImg.setTitle( titlesI18N.errorWhileCommunicatingToolTipText() );
				friendLabel.setText( titlesI18N.errorText() );
				friendLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
			} else {
				if( isFriend ) {
					friendImg.setUrl( ServerSideAccessManager.getRemoveFriendImageURL() );
					friendImg.setTitle( titlesI18N.removeFriendText() );
					friendLabel.setText( titlesI18N.removeFriendText() );
					friendLabel.setStyleName( CommonResourcesContainer.DIALOG_LINK_RED_STYLE );
				} else {
					friendImg.setUrl( ServerSideAccessManager.getAddFriendImageURL() );
					friendImg.setTitle( titlesI18N.addFriendText() );
					friendLabel.setText( titlesI18N.addFriendText() );
					friendLabel.setStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
				}
				//Adding adding/removing friend click listener 
				this.setDoRemove( isFriend );
				imageHandlerReg = friendImg.addClickHandler( this ); 
				labelHandlerReg = friendLabel.addClickHandler( this ); 
			}
		}
	}
}
