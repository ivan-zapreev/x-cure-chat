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
package com.xcurechat.client.popup;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.chat.messages.SendChatMessageManager;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.dialogs.messages.SendMessageDialogUI;
import com.xcurechat.client.dialogs.profile.AddRemoveFriendManager;
import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;
import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.AvatarSpoilersHelper;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;

/**
 * @author zapreevis
 * The short user info dialog
 */
public class ShortUserInfoPopupPanel extends InfoPopupPanel {
	
	//The short user info which is for viewing
	private ShortUserData userData;
	
	//The id of the room where from which we opened this user view pop up
	private final int roomID;
	
	private UserAvatarImageWidget avatarImageWidget = null;
	
	//The message image for the send image link
	private final Image messageImg = new Image();

	//The chat message image for the send image link
	private final Image chatMessageImg = new Image();
	
	//The action/add friend/remove friend image for loading/adding/removing friend status
	private final AddRemoveFriendManager friendManager;

	//The user block/unblock image
	private final Image userBlockActionImg = new Image();

	//The main vertical panel here, which is put into the decorator panel
	private final VerticalPanel mainInfoVPanel = new VerticalPanel();
	//The main horizontal panel here, which is put into the decorator panel
	private final HorizontalPanel mainInfoHPanel = new HorizontalPanel();
	//The vertical panel that stores the used description and action buttons panel
	private final VerticalPanel mainInfoActionPanel = new VerticalPanel();
	
	//The handler for showing the user profile
	private final ClickHandler userProfileShowHandler;
	
	/**
	 * The main constructor
	 * @param userData the short user data
	 * @param roomID id of the room where from which we opened this user view pop up
	 */
	private ShortUserInfoPopupPanel( final ShortUserData userData, final int roomID ) {
		//No autohide, and modal
		super( true, true);
		
		//Save the user data
		this.userData = userData;
		this.roomID = roomID;
		this.friendManager = new AddRemoveFriendManager( userData.getUID() );
		
		this.userProfileShowHandler = new ClickHandler(){
			public void onClick( ClickEvent e) {
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						//Close this popup
						hide();
						//Open the profile view dialog
						ViewUserProfileDialogUI dialog = new ViewUserProfileDialogUI( userData.getUID(),
																						userData.getUserLoginName(),
																						null, false );
						dialog.show();
						dialog.center();
					}
				};
				executor.loadAndExecute();
			}
		};
		
		//Add the main horizontal panel to the main vertical panel
		mainInfoVPanel.add( mainInfoHPanel );
		
		//Fill dialog with data
		populate();
	}
	
	/**
	 * Allows to open a short user's view popup with the given data 
	 * @param userData the data we want to show in the user's view popup
	 * @param roomID id of the room where from which we opened this user view pop up
	 * @param opener the ui element relative to which the popup will be opened
	 */
	public static void openShortUserViewPopup( final ShortUserData userData, final int roomID, final Widget opener ) {
		//Ensure lazy loading
		final SplitLoad executor = new SplitLoad( true ) {
			@Override
			public void execute() {
				//Create the popup panel object
				ShortUserInfoPopupPanel panel = new ShortUserInfoPopupPanel( userData, roomID );
				//Show the pop-up panel at some proper position, in such a way that
				//it does not go outside the window area, also make the popup modal
				panel.setPopupPositionAndShow( panel.new InfoPopUpPositionCallback( opener ) );
			}
		};
		executor.loadAndExecute();
	} 
	
	private void addMainDataFields() {		
		mainInfoHPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE);
		mainInfoHPanel.setSize("100%", "100%");
		mainInfoActionPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainInfoActionPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainInfoActionPanel.setSize("100%", "100%");
		
		HorizontalPanel mainFieldAndValuesPanel = new HorizontalPanel();
		mainFieldAndValuesPanel.setSize("100%", "100%");
		mainFieldAndValuesPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainFieldAndValuesPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		
		VerticalPanel mainFieldsPanel = new VerticalPanel();
		mainFieldsPanel.setSize("100%", "100%");
		mainFieldsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainFieldsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT);
		
		VerticalPanel mainValuesPanel = new VerticalPanel();
		mainValuesPanel.setSize("100%", "100%");
		mainValuesPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainValuesPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT);

		//Add the login name field
		Label userLoginNameLabel = new Label();
		userLoginNameLabel.setWordWrap( false );
		userLoginNameLabel.setText( userData.getShortLoginName( ) );
		userLoginNameLabel.setTitle( userData.getUserLoginName() );
		userLoginNameLabel.addClickHandler( userProfileShowHandler );
		InterfaceUtils.addFieldValueToPanels( mainFieldsPanel, true, titlesI18N.userFieldName(),
												mainValuesPanel, true, userLoginNameLabel );
		userLoginNameLabel.setStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
		
		//Add gender field
		Label gender = new Label();
		gender.setWordWrap( false );
		if( userData.isMale() ) {
			gender.setText( titlesI18N.genderMaleValue() );
		} else {
			gender.setText( titlesI18N.genderFemaleValue() );
		}
		InterfaceUtils.addFieldValueToPanels( mainFieldsPanel, true, titlesI18N.genderField(),
												mainValuesPanel, true, gender );

		//Add the online status field
		Label onlineStatus = new Label();
		onlineStatus.setWordWrap( false );
		if( userData.isOnline() ) {
			onlineStatus.setText( titlesI18N.userOnlineStatus() );
		} else {
			onlineStatus.setText( titlesI18N.userOfflineStatus() );
		}
		InterfaceUtils.addFieldValueToPanels( mainFieldsPanel, true, titlesI18N.statusColumnTitle(),
												mainValuesPanel, true, onlineStatus );
		
		mainFieldAndValuesPanel.add( mainFieldsPanel );
		mainFieldAndValuesPanel.add( new HTML("&nbsp;") );
		mainFieldAndValuesPanel.add( mainValuesPanel );
		
		//Set up the avatar's image
		avatarImageWidget = new UserAvatarImageWidget( userData );
		//Here we do not show the avatar's prank ever because it is not
		//always possible to track and update the short user data shown
		//in this pop-up. Thus we simply do not show spoilers. 
		avatarImageWidget.updateThisAvatarSpoiler(AvatarSpoilersHelper.UNDEFILED_AVATAR_SPOILER_ID, null);
		avatarImageWidget.enablePrankControls( false );
		avatarImageWidget.setTitle( titlesI18N.avatarUserFieldName() );
		
		mainInfoActionPanel.add( mainFieldAndValuesPanel );
		mainInfoHPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		mainInfoHPanel.add( mainInfoActionPanel );
		mainInfoHPanel.add( new HTML("&nbsp;") );
		mainInfoHPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		mainInfoHPanel.add( avatarImageWidget );
	}
	
	/**
	 * Allows to add action image buttons such as:
	 * 1. send chat message 2. send private (offline) message
	 * 3. add/remove friend 4. block/unblock chat messages
	 */
	private void addActionImageButtons() {
		//Add actions: Sent message, add friend, write to chat
		HorizontalPanel actionPanel = new HorizontalPanel();
		actionPanel.setStyleName( CommonResourcesContainer.ACTION_IMAGE_LINKS_PANEL_STYLE );
		actionPanel.setSize("100%", "100%");
		actionPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE);
		actionPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER);
		mainInfoActionPanel.add( actionPanel );
		
		//Add the chat message related elements
		chatMessageImg.setUrl( ServerSideAccessManager.getChatMessageImageIconURL() );
		chatMessageImg.setTitle( titlesI18N.sendChatMessageTipText() );
		chatMessageImg.setStyleName( CommonResourcesContainer.ACTION_IMAGE_LINK_STYLE );
		//Add click listeners to open the send message dialog
		chatMessageImg.addClickHandler(  new ClickHandler(){
			public void onClick(ClickEvent e) {
				//Close this pop up
				hide();
				//Open the send message dialog
				SendChatMessageManager.getInstance().writeToChatRoomUser(roomID, userData.getUID(), userData.getUserLoginName());
			}
		} );
		actionPanel.add( chatMessageImg );
		
		//Add the send message related elements
		messageImg.setUrl( ServerSideAccessManager.getSendMessageImageURL() );
		messageImg.setTitle( titlesI18N.sendMessageTipText() );
		messageImg.setStyleName( CommonResourcesContainer.ACTION_IMAGE_LINK_STYLE );
		//Add click listeners to open the send message dialog
		messageImg.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e){
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						//Close this panel
						hide();
						//Open the other dialog
						SendMessageDialogUI sendDialog = new SendMessageDialogUI( null, userData.getUID(),
																				  userData.getUserLoginName(), null);
						sendDialog.show();
						sendDialog.center();
					}
				};
				executor.loadAndExecute();
			}
		} );
		actionPanel.add( messageImg );
		
		//Add the load friend status/ add, remove friend related elements 
		actionPanel.add( friendManager.getFriendImage() );
		
		//Add the block/unblock user image and handler
		userBlockActionImg.setStyleName( CommonResourcesContainer.ACTION_IMAGE_LINK_STYLE );
		userBlockActionImg.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				if( SiteManager.isUserBlocked( userData.getUID() ) ) {
					SiteManager.removeBlockedUserId( userData.getUID() );
				} else {
					SiteManager.addBlockedUserId( userData.getUID() );
				}
				//Update the blocked image
				updateUserBlockedActionImage();
				//Stop the event from being propagated
				e.preventDefault();
				e.stopPropagation();
			}
		});
		//Update the blocked image
		updateUserBlockedActionImage();
		//Add the action image to the panel
		actionPanel.add( userBlockActionImg );
	}
	
	/**
	 * Allows to update the status of the user block/unblock image
	 */
	private void updateUserBlockedActionImage(){
		if( SiteManager.isUserBlocked( userData.getUID() ) ) {
			userBlockActionImg.setUrl( ServerSideAccessManager.getUnBlockUserImageIconURL() );
			userBlockActionImg.setTitle( I18NManager.getTitles().clickToUnblockUserMessages(userData.getUserLoginName()) );
		} else {
			userBlockActionImg.setUrl( ServerSideAccessManager.getBlockUserImageIconURL() );
			userBlockActionImg.setTitle( I18NManager.getTitles().clickToBlockUserMessages(userData.getUserLoginName()) );
		}
	}
	
	/**
	 * Allows to add a small panel containing the "View Full Profile" link,
	 * this is made for those who are not smart enough to click on the login
	 * name in this dialog again, to get to a full profile view  
	 */
	private void addViewUserProfileLink() {
		//Initialize the link
		Label userProfileLink = new Label(I18NManager.getTitles().viewFullUserProfileLinkTitle());
		userProfileLink.setWordWrap(false);
		userProfileLink.setStyleName( CommonResourcesContainer.DIALOG_LINK_RED_STYLE );
		userProfileLink.addClickHandler( this.userProfileShowHandler );
		
		//Initialize the panel
		HorizontalPanel userProfileLinkPanel = new HorizontalPanel();
		userProfileLinkPanel.setStyleName( CommonResourcesContainer.VIEW_USER_PROFILE_LINK_PANEL_STYLE );
		userProfileLinkPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		userProfileLinkPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		userProfileLinkPanel.add( userProfileLink );
		
		//Add the profile view link panel to the main panel
		mainInfoVPanel.add( userProfileLinkPanel );
	} 
	
	/**
	 * Fills the main grid data
	 */
	protected void populate() {
		//Set the main panel inside the decorator panel
		mainInfoVPanel.setWidth("100%");
		mainInfoActionPanel.setWidth("100%");
		mainInfoHPanel.setWidth("100%");
		this.addContentWidget( mainInfoVPanel );
		
		//Add the main fields and the avatar image
		addMainDataFields();
		
		//Add the action panel if we are not browsing ourselves
		if( SiteManager.getUserID() != userData.getUID() ) {
			friendManager.populateFriendData( false );
			addActionImageButtons();
		}
		
		//Add the view user profile link
		addViewUserProfileLink();
	}
}
