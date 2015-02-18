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

import java.util.Date;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.FocusPanel;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.popup.ShortUserInfoPopupPanel;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;

/**
 * @author zapreevis
 * This class is responsible for providing the chat message avatar
 */
public class ChatMessageAvatarUI extends Composite {
	
	//Alert the avatar on the append for some seconds
	private static final int AVATAR_TEMP_ALERT_DURATION_MILLISEC = 10000;
	//If true then the global constant alert is on
	private static boolean isGlobalConstantAlertOn = false;
	
	/**
	 * @author zapreevis
	 * Represents the types of the chat message avatar alerts
	 */
	private enum ChatMessageAvatarAlertType {
		NO_CHAT_MESSAGE_AVATAR_ALERT( null, null ),
		SIMPLE_CHAT_MESSAGE_AVATAR_ALERT( "xcure-Chat-Simple-Message-Aatar-Alert", "alert_message_simple" ),
		PRIVATE_CHAT_MESSAGE_AVATAR_ALERT( "xcure-Chat-Private-Message-Aatar-Alert", "alert_message_private" );
		
		private final String avatarAlertStyle;
		private final String avatarAlertImageName;
		
		ChatMessageAvatarAlertType( final String avatarAlertStyle, final String avatarAlertImageName ) {
			this.avatarAlertStyle     = avatarAlertStyle;
			this.avatarAlertImageName = avatarAlertImageName;
		}
		
		/**
		 * Allows to get avatar alert widget
		 * @return the avatar alert widget
		 */
		public Image getAvatarAlertImage() {
			Image result = null;
			if( ( avatarAlertImageName != null ) && ( avatarAlertStyle != null ) ) {
				result = new Image( ServerSideAccessManager.getPresetAvatarImagesBase() + avatarAlertImageName + ".gif" );
				result.setStyleName( avatarAlertStyle );
			}
			return result;
		}
		
		/**
		 * Allows to get a type of a chat message avatar alert
		 * @param messageType the chat message type
		 * @return the corresponding chat message avatar alert type
		 */
		public static ChatMessageAvatarAlertType getAvatarAlertType( final ChatMessage.Types messageType ) {
			ChatMessageAvatarAlertType highlight = null;
			switch( messageType ) {
				case SIMPLE_MESSAGE_TYPE:
					highlight = SIMPLE_CHAT_MESSAGE_AVATAR_ALERT;
					break;
				case PRIVATE_MESSAGE_TYPE:
					highlight = PRIVATE_CHAT_MESSAGE_AVATAR_ALERT;
					break;
				default:
					highlight = NO_CHAT_MESSAGE_AVATAR_ALERT;
			}
			return highlight;
		}
	}
	
	//The user name label
	private final Label avatarTitleLabel = new Label();
	//The user avatar widget
	private final UserAvatarImageWidget avatarImageWidget = new UserAvatarImageWidget( );
	//Avatar alert image
	private Image alertImage = null;
	//The panel storing the avatar/avatar alert widget
	private final SimplePanel avatarWrapperPanel = new SimplePanel();
	//Indicates that the temporary avatar alert is on
	private boolean isTempAvatarAlertOn = false;
	//Indicates that the constant avatar alert is on
	private boolean isConstAvatarAlertOn = false;
	//This is the avatar alert stop timer
	private final Timer avatarAlertStopTimer = new Timer() {
        public void run() {
        	//Turn the alert off
        	turnAvatarWidgetAlertOff( true );
        }
      };
	
	//The focus panel for enabling the clicking on user avatar panel
	private final FocusPanel mainFocusPanel = new FocusPanel();
	//The pabel storing the avatar image and the avatar title
	private final VerticalPanel mainVerticalPanel = new VerticalPanel();
	//The id of the room in which this avatar is shown
	private final int roomID;
	//Stored user data;
	private ShortUserData userData = null;
	
	/**
	 * The basic constructor
	 * @param roomID the id of the room in which this avatar is shown
	 */
	private ChatMessageAvatarUI( final int roomID ) {
		mainVerticalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainVerticalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		avatarWrapperPanel.add( avatarImageWidget );
		mainVerticalPanel.add( avatarWrapperPanel );
		mainVerticalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		avatarTitleLabel.setWordWrap( false );
		avatarTitleLabel.setStyleName( CommonResourcesContainer.USER_NAME_AVATAR_STYLE );
		mainVerticalPanel.add( avatarTitleLabel );
		mainFocusPanel.add( mainVerticalPanel );
		
		this.roomID = roomID;
		
		//All composites must call initWidget() in their constructors.
		initWidget( mainFocusPanel );		
	}
	
	/**
	 * Turns the avatar widget alert on
	 */
	private void turnAvatarWidgetAlertOn( final boolean isTempAlert ) {
		if( alertImage != null ) {
			//If none of the alerts is currently on then enable an alert
			if( ! isTempAvatarAlertOn && ! isConstAvatarAlertOn ) {
				avatarWrapperPanel.clear();
				avatarWrapperPanel.add( alertImage );
			}
			//Remember which alert was turned on
			if( isTempAlert ) {
				isTempAvatarAlertOn  = true;
			} else {
				isConstAvatarAlertOn = true;
			}
		}
	}
	
	/**
	 * Turns the avatar widget alert off
	 */
	private void turnAvatarWidgetAlertOff( final boolean isTempAlert ) {
		if( alertImage != null ) {
			//If only one of the alerts is on then remove the alert
			if( (   isTempAvatarAlertOn && ! isConstAvatarAlertOn ) ||
				( ! isTempAvatarAlertOn &&   isConstAvatarAlertOn ) ) {
	        	avatarWrapperPanel.clear();
	        	avatarWrapperPanel.add( avatarImageWidget );
			}
			//Remember which alert was turned off
			if( isTempAlert ) {
				isTempAvatarAlertOn  = false;
			} else {
				isConstAvatarAlertOn = false;
			}
		}
	}
	
	/**
	 * In case the avatar widget alert is set, this method 
	 * will cause it the avatar to be put constantly on.
	 */
	public void startConstAvatarWidgetAlert() {
		//Set the global constant alert on
		isGlobalConstantAlertOn = true;
		//Set the alert image
		turnAvatarWidgetAlertOn( false );
	}
	
	/**
	 * In case the avatar widget alert is set, this method 
	 * will cause it the avatar to be put constantly off.
	 */
	public void stopConstAvatarWidgetAlert() {
		//Set the global constant alert off
		isGlobalConstantAlertOn = false;
		//Turn the alert off
		turnAvatarWidgetAlertOff( false );
	}
	
	/**
	 * In case the avatar widget alert is set, this method will cause it
	 * the avatar to be alerted for some predefined period of time.
	 */
	public void startTempAvatarWidgetAlert() {
		if( alertImage != null ) {
			//Set the alert image
			turnAvatarWidgetAlertOn( true );
			//Manage the timer
			avatarAlertStopTimer.cancel();
			avatarAlertStopTimer.schedule( AVATAR_TEMP_ALERT_DURATION_MILLISEC );
		}
	}
	
	/**
	 * Allows to get the info message or user message avatar
	 * @param message the chat message 
	 * @param visibleUsers the list of users that are currently visible in the room
	 * @param roomID the id of the room in which this avatar is shown
	 * @return the avatar for the given message
	 */
	public static ChatMessageAvatarUI getMessageAvatar( final ChatMessage message, final Map<Integer, ShortUserData> visibleUsers, final int roomID ) {
		final ChatMessageAvatarUI avatar = new ChatMessageAvatarUI( roomID );
		if( ( message.messageType == ChatMessage.Types.SIMPLE_MESSAGE_TYPE ) ||
			( message.messageType == ChatMessage.Types.PRIVATE_MESSAGE_TYPE ) ) {
			//Set the avatar image border depending on message type
			if( message.messageType == ChatMessage.Types.SIMPLE_MESSAGE_TYPE ) {
				avatar.avatarImageWidget.addStyleName( CommonResourcesContainer.SIMPLE_MESSAGE_AVATAR_STYLE );
			} else {
				avatar.avatarImageWidget.addStyleName( CommonResourcesContainer.PRIVATE_MESSAGE_AVATAR_STYLE );
			}
			//Determine the user avatar alert type for the messages that were sent to this user
			if( message.isUserMessage() && message.isFirstRecipient(  SiteManager.getUserID() ) ) {
				avatar.alertImage = ChatMessageAvatarAlertType.getAvatarAlertType( message.messageType ).getAvatarAlertImage();
				if( isGlobalConstantAlertOn ) {
					//If the global alert is on then start the new avatar alert as well
					avatar.startConstAvatarWidgetAlert();
				} else {
					//Otherwise start a temporary alert
					avatar.startTempAvatarWidgetAlert();
				}
			}
			//Get the user related data
			avatar.userData = visibleUsers.get( message.senderID );
			if( avatar.userData == null ) {
				//Ensure lazy loading
				(new SplitLoad(){
					@Override
					public void execute() {
						//The user is not in the room!!! Retrieve the user data from the server!!!
						AsyncCallback<UserData> getProfileCallBack = new AsyncCallback<UserData>() {
							public void onSuccess(UserData result) {
								avatar.userData = result;
								avatar.avatarImageWidget.updateAvatarData( avatar.userData );
								setUserLoginName( avatar, message.messageType );
							}
							public void onFailure(Throwable caught) {
								//Do the recovery
								recover();
							}
						};
						UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
						userManagerObject.profile( SiteManager.getUserID(), SiteManager.getUserSessionId(), message.senderID, getProfileCallBack );
					}
					@Override
					public void recover() {
						//The user is gone or some server error, put some dummy avatar
						avatar.userData = null;
						avatar.avatarImageWidget.updateAvatarImage( message.senderID, true, false );
						setUserLoginName( avatar, message.messageType );
					}
				}).loadAndExecute();
			} else {
				//The user is in the room show his avatar
				avatar.avatarImageWidget.updateAvatarData( avatar.userData );
				setUserLoginName( avatar, message.messageType );
			}
		} else {
			//Disable the prank action buttons as they are irrelevant for the info messages
			avatar.avatarImageWidget.enablePrankControls(false);
			//This must be an info message avatar
			switch( message.messageType ) {
				case USER_ROOM_ENTER_INFO_MESSAGE_TYPE:
					avatar.avatarImageWidget.updateAvatarImage( ServerSideAccessManager.getUserEnterMessageAvatarImageURL( ) );
					break;
				case USER_ROOM_LEAVE_INFO_MESSAGE_TYPE:
					avatar.avatarImageWidget.updateAvatarImage( ServerSideAccessManager.getUserLeaveMessageAvatarImageURL( ) );
					break;
				default:
					avatar.avatarImageWidget.updateAvatarImage( ServerSideAccessManager.getDefaultInfoMessageAvatarImageURL( ) );
			}
			avatar.avatarImageWidget.setStyleName( CommonResourcesContainer.INFO_MESSAGE_AVATAR_STYLE );
			updateSystemMessageAvatarTitle( avatar, false );
		}
		return avatar;
	}
	
	/**
	 * Updates the avatar with the user login name as a title
	 * @param avatar the avatar to work with
	 * @param messageType the type of the user message: private or simple
	 */
	private static void setUserLoginName( final ChatMessageAvatarUI avatar, final ChatMessage.Types messageType ) {
		if( messageType == ChatMessage.Types.PRIVATE_MESSAGE_TYPE ) {
			avatar.mainVerticalPanel.setStyleName( CommonResourcesContainer.PRIVATE_MESSAGE_AVATAR_PANEL_STYLE );
		} else {
			avatar.mainVerticalPanel.setStyleName( CommonResourcesContainer.SIMPLE_MESSAGE_AVATAR_PANEL_STYLE );
		}
		
		if( avatar.userData != null ) {
			avatar.avatarTitleLabel.setText( avatar.userData.getShortLoginName() );
			avatar.mainVerticalPanel.setTitle( avatar.userData.getUserLoginName() );
			//Add the user info panel popup for the user avatar panel
			avatar.mainFocusPanel.addClickHandler( new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					//Ensure lazy loading
					final SplitLoad executor = new SplitLoad( true ) {
						@Override
						public void execute() {
							//Open the short user view
							ShortUserInfoPopupPanel.openShortUserViewPopup( avatar.userData, avatar.roomID, avatar );
						}
					};
					executor.loadAndExecute();
					//Stop the event from being propagated
					event.stopPropagation();
				}
			});
		} else {
			//This happens if the user does not exist
			final String unknownUserName = I18NManager.getTitles().unknownUserLoginName();
			avatar.avatarTitleLabel.setText( ShortUserData.getShortLoginName( unknownUserName ) );
			avatar.mainVerticalPanel.setTitle( I18NManager.getErrors().userDoesNotExistError() );
		}
	}
	
	/**
	 * @return an avatar for the chat error message
	 */
	public static ChatMessageAvatarUI getErrorMessageAvatar() {
		//Here we are adding the error message for which it is not important to set the chat room ID
		ChatMessageAvatarUI avatar = new ChatMessageAvatarUI( ChatRoomData.UNKNOWN_ROOM_ID );
		//Disable the prank action buttons as they are irrelevant for the error messages
		avatar.avatarImageWidget.enablePrankControls(false);
		avatar.avatarImageWidget.updateAvatarImage( ServerSideAccessManager.getErrorMessageAvatarImageURL( ) );
		avatar.avatarImageWidget.setStyleName( CommonResourcesContainer.ERROR_MESSAGE_AVATAR_STYLE );
		updateSystemMessageAvatarTitle( avatar, true );
		return avatar;
	}
	
	/**
	 * Allows to update the avatar spoiler based on the spoiler id and the expiration date
	 * If the avatar does not belong to the specified user, then the spoiler update is skipped
	 * @param userID the if of the user to whoes this avatar must belong
	 * @param avatarSpoilerId the avatar spoiler id
	 * @param avatarSpoilerExpDate the avatar expiration date
	 */
	public void updateThisAvatarSpoiler( final int userID, final int spoilerID, final Date spoilerExpDate ) {
		if( ( ( userData != null ) && ( userID == userData.getUID() ) ) ) {
			//Update the cached user data
			userData.setAvatarSpoilerId( spoilerID );
			userData.setAvatarSpoilerExpDate( spoilerExpDate );
			//Update the avatar image
			avatarImageWidget.updateThisAvatarSpoiler( spoilerID, spoilerExpDate );
		} else {
			if( userID == avatarImageWidget.getUserID() ) {
				//Just update the image if it is our user
				avatarImageWidget.updateThisAvatarSpoiler(spoilerID, spoilerExpDate);
			}
		}
	}
	
	/**
	 * Updates the system message avatar with the system string name
	 * @param avatar the avatar to update
	 * @param isError true for the error message false for the info message
	 */
	private static void updateSystemMessageAvatarTitle( ChatMessageAvatarUI avatar, final boolean isError ) {
		final String avatarTitle = I18NManager.getTitles().systemChatMessageAvatarTitle();
		avatar.avatarImageWidget.setTitle( avatarTitle );
		avatar.avatarTitleLabel.setText( ShortUserData.getShortLoginName( avatarTitle ) );
		avatar.avatarTitleLabel.setTitle( avatarTitle );
		avatar.mainVerticalPanel.setStyleName( ( isError ? CommonResourcesContainer.ERROR_MESSAGE_AVATAR_PANEL_STYLE : CommonResourcesContainer.INFO_MESSAGE_AVATAR_PANEL_STYLE ) );
	}
}
