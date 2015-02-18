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
 * The client utilities package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils.widgets;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteBodyComponent;

import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.SplitLoad;

public class UserAvatarWidget extends Composite implements SiteBodyComponent {
	/*The flags that indicate what extra data to display*/
	public static final int DISPLAY_NO_EXTRA_DATA      = 0;  	//         000...00000000 (empty mask)
	public static final int DISPLAY_MONEY_DATA         = 1;    	// 2^^0    000...00000001
	public static final int DISPLAY_FORUM_POSTS_DATA   = 2;    	// 2^^1    000...00000010
	public static final int DISPLAY_CHAT_MESSAGES_DATA = 4;    	// 2^^2    000...00000100
	public static final int DISPLAY_TIME_ON_SITE_DATA  = 8;    	// 2^^3    000...00001000
	public static final int DISPLAY_REGISTRATION_DATA  = 16;   	// 2^^4    000...00010000
	public static final int DISPLAY_AVATAR_IMAGE_DATA  = 32;   	// 2^^5    000...00100000
	public static final int DISPLAY_USER_NAME_DATA     = 64;    // 2^^6    000...01000000
	public static final int DISPLAY_USER_VISIT_DATA    = 128;   // 2^^7    000...10000000
	
	//The internationalization class
	protected static final UITitlesI18N i18nTitles = I18NManager.getTitles();
	
	//The stored user data
	private final ShortUserData userData;

	private final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_SHORT );
	
	//Avatar related UI elements
	private final Label avatarTitleLabel = new Label();
	private final FocusPanel avatarFocusPanel = new FocusPanel();
	private HandlerRegistration avatarClickHandlerReg = null;
	private final UserAvatarImageWidget avatarImage = new UserAvatarImageWidget();
	//Contains true if the message sender is an existing user, false if it is a deleted one
	private final boolean doesSenderExist;
	
	/**
	 * The basic constructor, creates an avatar panel with the user name only
	 * @param userID the id of the user
	 * @param userLoginName the login name of the user
	 */
	public UserAvatarWidget( final int userID, final String userLoginName ) {
		this( new ShortUserData( userID, userLoginName ), DISPLAY_USER_NAME_DATA );
	}
	
	/**
	 * The basic constructor, creates an avatar panel with the image and with the user name
	 * @param userData the short user data for which the avatar is created
	 */
	public UserAvatarWidget( final ShortUserData userData ) {
		this( userData, DISPLAY_AVATAR_IMAGE_DATA | DISPLAY_USER_NAME_DATA );
	}
	
	/**
	 * The basic constructor
	 * @param userData the short user data for which the avatar is created
	 * @param flags the flags that indicate what kind of data to display
	 * 			note that, the avatar image and the user name can be displayed always,
	 * 			other data can only be displayed for an existing user.
	 */
	public UserAvatarWidget( final ShortUserData userData, final int flags ) {
		//Store the user data
		this.userData = userData;
		
		//Check on whether the user of this avatar still exists
		doesSenderExist =  ( userData.getUID() != ShortUserData.UNKNOWN_UID ) &&
						   ! ShortUserData.DELETED_USER_LOGIN_NAME.equals( userData.getUserLoginName() );
		
		//Add the sub-panel with the avatar
		final VerticalPanel avatarVerticalPanel = new VerticalPanel();
		avatarVerticalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		avatarVerticalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		
		//Add the avatar image, if needed
		if( ( flags & DISPLAY_AVATAR_IMAGE_DATA ) > 0 ) {
			displayAvatarImage( avatarVerticalPanel );
		}
		
		//The additional user data is only displayed if the user exists
		if( doesSenderExist ) {
			//Add the user money count, if needed
			if( ( flags & DISPLAY_MONEY_DATA ) > 0 ) {
				displayMoney( avatarVerticalPanel );
			}
			
			//Add the user forum messages count, if needed
			if( ( flags & DISPLAY_FORUM_POSTS_DATA ) > 0 ) {
				displayForumPosts( avatarVerticalPanel );
			}
			
			//Add the user chat messages count, if needed
			if( ( flags & DISPLAY_CHAT_MESSAGES_DATA ) > 0 ) {
				displayChatMessages( avatarVerticalPanel );
			}
			
			//Add the user time on site, if needed
			if( ( flags & DISPLAY_TIME_ON_SITE_DATA ) > 0 ) {
				displayTimeOnSite( avatarVerticalPanel );
			}
			
			//Add the is-online status, if needed
			if( ( flags & DISPLAY_REGISTRATION_DATA ) > 0 ) {
				displayRegTime( avatarVerticalPanel );
			}
			
			//Add the user's last visit date, if needed
			if( ( flags & DISPLAY_USER_VISIT_DATA ) > 0 ) {
				displayLastVisitDate( avatarVerticalPanel );
			}
		}
		
		//Add the user name, if needed
		if( ( flags & DISPLAY_USER_NAME_DATA ) > 0 ) {
			displayUserName( avatarVerticalPanel );
		}
		
		//Add the avatar fields to the focus panel and set its title
		avatarFocusPanel.setTitle( userData.getUserLoginName() );
		avatarFocusPanel.add( avatarVerticalPanel );
		
		//Initialize the widget
		initWidget( avatarFocusPanel );
	}
	
	/**
	 * Allows to display the statistics entry based on the statistical data text and the image
	 * @param avatarVerticalPanel the vertical panel to add the statistical widget to
	 * @param widget the widget that described the value 
	 * @param image_name the name of the image describing the statistics 
	 */
	private void displayStatisticsElement( final VerticalPanel avatarVerticalPanel,
										   final Widget widget, final String image_name) {
		final HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		panel.add( new Image( ServerSideAccessManager.USER_INFO_RELATED_IMAGES_LOCATION +
							  ServerSideAccessManager.SERVER_CONTEXT_DELIMITER + image_name + ".png" ) );
		panel.add( new HTML("&nbsp;") );
		panel.add( widget );
		avatarVerticalPanel.add( panel );
	}
	
	/**
	 * Allows to create an html string for the value-name pair
	 * @param value the value
	 * @param name the name
	 * @return the resulting html string
	 */
	public String createValueNameHTMLString( final String value, final String name ) {
		final boolean isValue = (value != null);
		final boolean isName  = (name  != null);
		return ( isValue ? "<div class=\"gwt-Label xcure-Chat-Avatar-Info-Value xcure-Chat-Inline-Display\">" + value + "</div>" : "" )
				+ ( ( isValue && isName ) ? "&nbsp;" : "" ) +
				( isName ? "<div class=\"gwt-Label xcure-Chat-Avatar-Info-Name xcure-Chat-Inline-Display\">" + name + "</div>" : "" );
	}
	
	/**
	 * Allows to create an html string for the value-name pair
	 * @param value the value
	 * @param name the name
	 * @return the resulting html string
	 */
	public String createValueNameHTMLString( final int value, final String name ) {
		return createValueNameHTMLString( value + "", name );
	}
	
	/**
	 * Allows to create an html string for the value-name pair
	 * @param value the value
	 * @param name the name
	 * @return the resulting html string
	 */
	public String createValueNameHTMLString( final long value, final String name ) {
		return createValueNameHTMLString( value + "", name );
	}
	
	/**
	 * Allows to create an html string for the value- (empty)name pair
	 * @param value the value
	 * @return the resulting html string
	 */
	public String createValueNameHTMLString( final String value ) {
		return createValueNameHTMLString( value , null );
	}
	
	/**
	 * Allows to display the user's money
	 * @param avatarVerticalPanel the panel to add the content to
	 */
	private void displayMoney( final VerticalPanel avatarVerticalPanel ) {
		final String dataString = createValueNameHTMLString( userData.getGoldPiecesCount(), i18nTitles.avatarInfoMoneyCount() );
		displayStatisticsElement( avatarVerticalPanel, new HTML( dataString ), "money" );
	}
	
	/**
	 * Allows to display the number of user's forum messages
	 * @param avatarVerticalPanel the panel to add the content to
	 */
	private void displayForumPosts( final VerticalPanel avatarVerticalPanel ) {
		final String dataString = createValueNameHTMLString( userData.getSentForumMessagesCount(), i18nTitles.avatarInfoForumMsgsCount() );
		displayStatisticsElement( avatarVerticalPanel, new HTML( dataString ), "forum" );
	}
	
	/**
	 * Allows to display the number of user's chat messages
	 * @param avatarVerticalPanel the panel to add the content to
	 */
	private void displayChatMessages( final VerticalPanel avatarVerticalPanel ) {
		final String dataString = createValueNameHTMLString( userData.getSentChatMessagesCount(), i18nTitles.avatarInfoChatMsgsCount() );
		displayStatisticsElement( avatarVerticalPanel, new HTML( dataString ), "chat" );
	}
	
	/**
	 * Allows to display the time this user spent online
	 * @param avatarVerticalPanel the panel to add the content to
	 */
	private void displayTimeOnSite( final VerticalPanel avatarVerticalPanel ) {
		final long total_minutes = userData.getTimeOnline() / ( 60 * 1000 );
		final long hours = total_minutes / 60;
		final long minutes = total_minutes % 60 ;
		final String dataString = createValueNameHTMLString( hours , i18nTitles.avatarInfoTimeHoursOnline() )
								  + "&nbsp;" +
								  createValueNameHTMLString( minutes , i18nTitles.avatarInfoTimeMinutesOnline() );
		displayStatisticsElement( avatarVerticalPanel, new HTML( dataString ), "time" );
	}
	
	/**
	 * Allows to display the user's registration time
	 * @param avatarVerticalPanel the panel to add the content to
	 */
	private void displayRegTime( final VerticalPanel avatarVerticalPanel ) {
		final String dataString = createValueNameHTMLString( dateTimeFormat.format( userData.getUserRegistrationDate() ) );
		displayStatisticsElement( avatarVerticalPanel, new HTML( dataString ), "registration" );
	}
	
	/**
	 * Allows to display the user's last visit date
	 * @param avatarVerticalPanel the panel to add the content to
	 */
	private void displayLastVisitDate( final VerticalPanel avatarVerticalPanel ) {
		if( userData.isOnline() ) {
			displayStatisticsElement( avatarVerticalPanel, new HTML( createValueNameHTMLString( i18nTitles.userOnlineStatus() ) ), "online" );
		} else {
			final String dataString = createValueNameHTMLString( dateTimeFormat.format( userData.getUserLastOnlineDate( ) ) );
			displayStatisticsElement( avatarVerticalPanel, new HTML( dataString ), "offline" );
		}
	}
	
	/**
	 * Allows to display the user login name
	 * @param avatarVerticalPanel the panel to add the content to
	 */
	private void displayUserName( final VerticalPanel avatarVerticalPanel ) {
		avatarVerticalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		avatarTitleLabel.setWordWrap( false );
		avatarVerticalPanel.add( avatarTitleLabel );
			
		//Set the avatar elements data
		if( doesSenderExist ) {
			avatarTitleLabel.setText( userData.getShortLoginName() );
			avatarVerticalPanel.setTitle( userData.getUserLoginName() );
		} else {
			avatarTitleLabel.setText( i18nTitles.unknownMsgSenderReceiver() );
			avatarVerticalPanel.setTitle( i18nTitles.senderHasDeletedHisProfile() );
		}
	}
	
	/**
	 * Allows to display the avatar image
	 * @param avatarVerticalPanel the panel to add the content to
	 */
	private void displayAvatarImage( final VerticalPanel avatarVerticalPanel ) {
		//Update the avatar with the user data
		avatarImage.updateAvatarData( userData );
		//Enable the prank controls only if the user exists
		avatarImage.enablePrankControls( doesSenderExist );
		//Add the avatar image
		avatarVerticalPanel.add( avatarImage );
	}
	
	//Allows to update the avatar's UI
	public void updateThisAvatarSpoiler( final int userID, final int spoilerID, final Date spoilerExpDate) {
		if( ( avatarImage != null ) && doesSenderExist && ( userID == avatarImage.getUserID() ) ) {
			avatarImage.updateThisAvatarSpoiler( spoilerID, spoilerExpDate);
		}
	}
	
	@Override
	public void onAfterComponentIsAdded() {
		//NOTE: There is nothing to be done
	}

	@Override
	public void onBeforeComponentIsAdded() {
		//NOTE: There is nothing to be done
	}

	@Override
	public void onBeforeComponentIsRemoved() {
		//NOTE: There is nothing to be done
	}

	@Override
	public void setEnabled(boolean enabled) {
		//First enable/disbaled the avatar image
		avatarImage.enablePrankControls( enabled && doesSenderExist );
		//Then enable/disable the other stuff
		if( enabled ) {
			avatarFocusPanel.removeStyleName( CommonResourcesContainer.AVATAR_PANEL_DIS_STYLE  );
			if( doesSenderExist ) {
				//If the sender exist
				avatarFocusPanel.addStyleName( CommonResourcesContainer.AVATAR_PANEL_STYLE );
				avatarTitleLabel.addStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
				//Add the click handler if needed
				if( avatarClickHandlerReg == null ) {
					avatarClickHandlerReg = avatarFocusPanel.addClickHandler( new ClickHandler(){
						@Override
						public void onClick(ClickEvent event) {
							//Ensure lazy loading
							final SplitLoad executor = new SplitLoad( true ) {
								@Override
								public void execute() {
									//Open the user profile view
									ViewUserProfileDialogUI dialog = new ViewUserProfileDialogUI( userData.getUID(), userData.getUserLoginName(), null, false );
									dialog.show();
									dialog.center();
								}
							};
							executor.loadAndExecute();
							//Stop the event from being propagated, prevent default
							event.stopPropagation(); event.preventDefault();
						}
					} );
				}
			} else {
				avatarFocusPanel.addStyleName( CommonResourcesContainer.AVATAR_PANEL_DIS_STYLE );
				avatarTitleLabel.addStyleName( CommonResourcesContainer.DIALOG_LINK_DISABLED_STYLE );
			}
		} else {
			//Manage the avatar's click handler
			avatarFocusPanel.removeStyleName( CommonResourcesContainer.AVATAR_PANEL_STYLE );
			avatarFocusPanel.addStyleName( CommonResourcesContainer.AVATAR_PANEL_DIS_STYLE );
			avatarTitleLabel.removeStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
			if( doesSenderExist ) {
				//Remove the click handler if needed
				if( avatarClickHandlerReg != null ) {
					avatarClickHandlerReg.removeHandler();
					avatarClickHandlerReg = null;
				}
			} else {
				avatarTitleLabel.addStyleName( CommonResourcesContainer.DIALOG_LINK_DISABLED_STYLE );				
			}
		}
	}
	
	@Override
	public void setUserLoggedIn() {
		setEnabled( true );
	}

	@Override
	public void setUserLoggedOut() {
		setEnabled( false );
	}

}
