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
package com.xcurechat.client.tree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.userstatus.UserStatusHelper;
import com.xcurechat.client.userstatus.UserStatusManager;
import com.xcurechat.client.utils.SiteUserAge;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.UserTreasureLevel;
import com.xcurechat.client.utils.UserForumActivity;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.popup.ShortUserInfoPopupPanel;
import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * This class visualizes the user item in the tree
 * NOTE: We do not just inherit from the FocusPanel because
 * this some times causes GWT to crash since when one calls
 * super() in the constructor of the child. This is somehow
 * related to the focus panel being imediately registered to
 * event handling or smth like that. Immediately, i.e. before
 * it is fully constructed.
 */
public class UserTreeItem extends Composite {
	private static final UITitlesI18N i18nTitles = I18NManager.getTitles();
	
	private final FocusPanel theItemPanel = new FocusPanel(); 
	//The main horizontal panel that stores the user tree entry
	private final HorizontalPanel theMainHorizontalPanel = new HorizontalPanel();
	//The horizontal panel for user icon and blocking/un-blicking the user
	private final HorizontalPanel userBlockPanel = new HorizontalPanel();
	//The reference to this object, is needed for the anonymous classes
	private final UserTreeItem thisItem = this;
	//The user name label
	private final Label userNameLabel = new Label( );
	//The user image gender/blocked status icon
	private final Image userImage = new Image( );
	//The status image icon
	private final Image userStatusImage = new Image( );
	//The user block image
	private final Image userBlockImage = new Image( );
	//The user site age image
	private final Image userSiteAgeImage = new Image( );
	//The user chat activity image
	private final Image userChatActivityImage = new Image( );
	//The user forum activity image
	private final Image userForumActivityImage = new Image( );
	//The place holder for the user block Image
	private final SimplePanel blockImagePlaceHolder = new SimplePanel();
	//The short user Data
	private final ShortUserData userData;
	
	/**
	 * To create an entry for this user who is hidden in the given room
	 * @param userData
	 */
	public UserTreeItem( final String userLoginName ) {
		this.userData = null;
		
		final Label userNameLabel = new Label( );
		userNameLabel.setText( ShortUserData.getShortLoginName( userLoginName ) );
		userNameLabel.setTitle( userLoginName );
		Image userImage = new Image( ServerSideAccessManager.getHiddenUserImageIconURL() );
		userImage.setStyleName( CommonResourcesContainer.CHAT_USER_IMAGE_STYLE_NAME );
		blockImagePlaceHolder.setStyleName( CommonResourcesContainer.CHAT_ROOM_IMAGE_PLACE_HOLDER_STYLE );
		
		theMainHorizontalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		theMainHorizontalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		theMainHorizontalPanel.add( blockImagePlaceHolder );
		theMainHorizontalPanel.add( userImage );
		userStatusImage.setStyleName( CommonResourcesContainer.CHAT_USER_IMAGE_STYLE_NAME );
		userStatusImage.setUrl( UserStatusHelper.getUserStatusImgURL( UserStatusManager.getUserStatusQueue().getCurrentUserStatus() ) );
		theMainHorizontalPanel.add( userStatusImage );
		theMainHorizontalPanel.add( userNameLabel );
		theMainHorizontalPanel.setTitle( i18nTitles.hiddenUserTip() );
		
		theItemPanel.setStyleName( CommonResourcesContainer.CHAT_ROOM_HIDDEN_TREE_ITEM_STYLE_NAME );
		theItemPanel.add( theMainHorizontalPanel );
		
		initWidget(theItemPanel);
	}
	
	/**
	 * To create an entry for a non-hidden user
	 * @param userData the user data to display
	 * @param roomID the id of the room this item will be displayed in
	 */
	public UserTreeItem( final ShortUserData userData, final int roomID ) {
		//Store the user data object 
		this.userData = userData;
		
		//Initialize the image styles
		userImage.setStyleName( CommonResourcesContainer.CHAT_USER_IMAGE_STYLE_NAME );
		userStatusImage.setStyleName( CommonResourcesContainer.CHAT_USER_IMAGE_STYLE_NAME );
		userBlockImage.setStyleName( CommonResourcesContainer.CHAT_USER_IMAGE_STYLE_NAME );
		
		userSiteAgeImage.setStyleName( CommonResourcesContainer.USER_SITE_AGE_IMAGE_STYLE_NAME );
		final SiteUserAge siteUserAge = userData.getSiteUserAge();
		userSiteAgeImage.setUrl( GWT.getModuleBaseURL() + siteUserAge.getAgeImageURL() ); 
		userSiteAgeImage.setTitle( siteUserAge.getAgeImageTooltip( i18nTitles ) );
		
		userChatActivityImage.setStyleName( CommonResourcesContainer.CHAT_USER_IMAGE_STYLE_NAME );
		final UserTreasureLevel userChatActivity = userData.getUserTreasureLevel();
		userChatActivityImage.setUrl( GWT.getModuleBaseURL() + userChatActivity.getUserTreasureLevelImageURL() );
		userChatActivityImage.setTitle( userChatActivity.getUserTreasureImageTooltip( i18nTitles, userData.getGoldPiecesCount() ) );
		
		userForumActivityImage.setStyleName( CommonResourcesContainer.USER_FORUM_ACTIVITY_IMAGE_STYLE_NAME );
		final UserForumActivity userForumActivity = userData.getUserForumActivity();
		userForumActivityImage.setUrl( GWT.getModuleBaseURL() + userForumActivity.getForumActivityImageURL() );
		userForumActivityImage.setTitle( userForumActivity.getForumActivityImageTooltip( i18nTitles, userData.getSentForumMessagesCount() ) );
		
		blockImagePlaceHolder.setStyleName( CommonResourcesContainer.CHAT_ROOM_IMAGE_PLACE_HOLDER_STYLE );
		
		//Create and initialize the user-block panel and focus panel
		FocusPanel userBlockFocusPanel = new FocusPanel();
		userBlockPanel.add( blockImagePlaceHolder );
		userBlockPanel.add( userImage );
		userBlockFocusPanel.add(  userBlockPanel );
		if( userData.getUID() != SiteManager.getUserID() ) {
			//If this is not us then allow blocking of this user
			userBlockFocusPanel.addClickHandler( new ClickHandler() {
				public void onClick(ClickEvent e) {
					if( SiteManager.isUserBlocked( userData.getUID() ) ) {
						SiteManager.removeBlockedUserId( userData.getUID() );
					} else {
						SiteManager.addBlockedUserId( userData.getUID() );
					}
					//Update the blocked image
					updateUserTreeItemStatus();
					//Stop the event from being propagated
					e.preventDefault();
					e.stopPropagation();
				}
			});
			//When the mouse is over, add the user block/unblock image
			theItemPanel.addMouseOverHandler( new MouseOverHandler(){
				@Override
				public void onMouseOver(MouseOverEvent event) {
					//First remove the image, just in case, it should not have been there
					blockImagePlaceHolder.remove( userBlockImage );
					//Now add the image again
					blockImagePlaceHolder.add( userBlockImage );
				}});
			//When the mouse is our remove the user block/unblock image
			theItemPanel.addMouseOutHandler( new MouseOutHandler(){
				@Override
				public void onMouseOut(MouseOutEvent event) {
					blockImagePlaceHolder.remove( userBlockImage );
				}});
		}
		//Set the current blocked image status
		updateUserTreeItemStatus();
		
		//Set the user login name label
		userNameLabel.setText( userData.getShortLoginName() );
		
		//Add the click handler for the entire node
		theItemPanel.setTitle( i18nTitles.clickToViewShortUserInfoToolTip( userData.getUserLoginName() ) );
		theItemPanel.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent event) {
				//Ensure lazy loading
				( new SplitLoad( true ) {
					@Override
					public void execute() {
						ShortUserInfoPopupPanel.openShortUserViewPopup(userData, roomID, thisItem);
					}
				}).loadAndExecute();
				//In Safari clicking inside a scroll panel makes is scroll up
				//(to the top) this is what we want to prevent here
				event.stopPropagation();
				event.preventDefault();
			}
		});
		
		theMainHorizontalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		theMainHorizontalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		theMainHorizontalPanel.add( userBlockFocusPanel );
		theMainHorizontalPanel.add( userStatusImage );
		theMainHorizontalPanel.add( userSiteAgeImage );
		theMainHorizontalPanel.add( userChatActivityImage );
		theMainHorizontalPanel.add( userForumActivityImage );
		theMainHorizontalPanel.add( userNameLabel );
		
		theItemPanel.add( theMainHorizontalPanel );
		
		initWidget(theItemPanel);
	}
	
	/**
	 * Allows to update the current user blocked status
	 */
	private void updateUserTreeItemStatus() {
		if( userData.getUID() != SiteManager.getUserID() ) {
			//If this is not us then set blocking related image urls and titles
			if( SiteManager.isUserBlocked( userData.getUID() ) ) {
				if( userData.isMale() ) {
					userImage.setUrl( ServerSideAccessManager.getMaleBlockOnImageIconURL() );
				} else {
					userImage.setUrl( ServerSideAccessManager.getFemaleBlockOnImageIconURL() );
				}
				userBlockPanel.setTitle( i18nTitles.clickToUnblockUserMessages(userData.getUserLoginName()) );
				userBlockImage.setUrl( ServerSideAccessManager.getUnBlockUserImageIconURL() );
				theItemPanel.setStyleName( CommonResourcesContainer.CHAT_ROOM_BLOCKED_USER_ITEM_STYLE_NAME );
			} else {
				if( userData.isMale() ) {
					userImage.setUrl( ServerSideAccessManager.getMaleBlockOffImageIconURL() );
				} else {
					userImage.setUrl( ServerSideAccessManager.getFemaleBlockOffImageIconURL() );
				}
				userBlockPanel.setTitle( i18nTitles.clickToBlockUserMessages(userData.getUserLoginName()) );
				userBlockImage.setUrl( ServerSideAccessManager.getBlockUserImageIconURL() );
				theItemPanel.setStyleName( CommonResourcesContainer.CHAT_ROOM_USER_ITEM_STYLE_NAME );
			}
		} else {
			if( userData.isMale() ) {
				userImage.setUrl( ServerSideAccessManager.getMaleBlockOffImageIconURL() );
			} else {
				userImage.setUrl( ServerSideAccessManager.getFemaleBlockOffImageIconURL() );
			}
			theItemPanel.setStyleName( CommonResourcesContainer.CHAT_ROOM_USER_ITEM_STYLE_NAME );
		}
		//Set user status image
		userStatusImage.setUrl( UserStatusHelper.getUserStatusImgURL( userData.getUserStatus() ) );
		userStatusImage.setTitle( i18nTitles.userStatusFieldTitle() + " " + UserStatusHelper.getUserStatusMsg( userData.getUserStatus() ) );
	}
} 
