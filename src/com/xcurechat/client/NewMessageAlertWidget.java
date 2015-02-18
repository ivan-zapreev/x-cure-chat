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
package com.xcurechat.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;

import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.Timer;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;


import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.userstatus.UserStatusManager;

import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This composite Widget has only one instance, it is used for notifying
 * the user about the new messages with the sound and also allows to turn
 * the sound notifications on/off. The status of the widget, i.e. on/off
 * is stored in the the cookie for preserving the status between sessions.
 * Allows for a visual notification in the site title.
 */
public final class NewMessageAlertWidget extends Composite implements MouseMoveHandler {
	//This is the length of the time interval since the last time user moved the mouse
	//on site. After this time interval the new chat message notifications will be on 
	private static final int TITLE_NOTIFY_ON_NO_ACTIVITY_TIME_MILLISEC = 10000;
	
	/**
	 * @author zapreevis
	 * This class represents the blinking title of the web-site that notifies the user about some new messages
	 */
	private class TitleBlinkNotifier extends Timer {
		public static final int BLINKING_TIME_PERIOD_MILLISEC = 1000;
		public static final int MAXIMUM_NUMBER_REPETED_BLINKS = 300;
		private String titleBlinkSubstitute = "";
		private String currentTitle = "";
		private int currentlyDoneBlinks = 0;
		private boolean isBlank = false;
		private boolean isBlinking = false;
		
		public TitleBlinkNotifier() {
			super();
			
			//Initiate the title substitution string
			for(int i = 0; i <= titles.siteTitle().length(); i++) {
				titleBlinkSubstitute += "*";
			}
		}
		
		/**
		 * Allows to set the current title and save the currently set value
		 * @param newTitle
		 */
		public void updateTitle(final String newTitle) {
			currentTitle = newTitle;
			setSiteTitle( currentTitle );
		}
		
		@Override
		public void run() {
			if( currentlyDoneBlinks < MAXIMUM_NUMBER_REPETED_BLINKS ) {
				//If we have not done all the blinking yet then
				if( isBlinking ) {
					//Do the blinking
					if( isBlank ) {
						updateTitle( titles.siteTitle() );
					} else {
						updateTitle( titleBlinkSubstitute );
					}
					isBlank = ! isBlank;
					//Increase the blikning count
					currentlyDoneBlinks++;
				}
			} else {
				//Stop the new message blinking
				stopNewMessageTitleNotification();
			}
		}
		
		/**
		 * Schedule repeating blinking
		 */
		public void startBlinking() {
			//Start the timer and set the variables
			if( ! isBlinking ) {
				isBlinking = true;
				scheduleRepeating( BLINKING_TIME_PERIOD_MILLISEC );
				currentlyDoneBlinks = 0;
			}
		}
		
		/**
		 * Stop the blinking, set the title back to normal
		 */
		public void stopBlinking() {
			//Stop the timer and reset the variables
			if( isBlinking ) {
				this.cancel();
				isBlank = false;
				isBlinking = false;
			}
			
			//Check that the currently set title is back to normal
			if( currentTitle.equals( titleBlinkSubstitute ) ) {
				setSiteTitle( titles.siteTitle() );
			}
		}
	}
	
	//The only instance of the sound notifier object
	private static NewMessageAlertWidget instance = null;
	
	//The internationalization object
	private final UITitlesI18N titles = I18NManager.getTitles();
	
	//The title notification title
	private final TitleBlinkNotifier titleNotifier = new TitleBlinkNotifier();
	
	/**
	 * Allows to get the only instance of the sound notifier object.
	 * When called for the first time hookes up the widget to the main sites
	 * focus panel so that it can start receiving the mouse move events.
	 * @return the only instance of the sound notifier object
	 */
	public static NewMessageAlertWidget getInstance() {
		if( instance == null ) {
			instance = new NewMessageAlertWidget( SiteManagerUI.getInstance().getMainFocusPanel() );
		}
		return instance;
	}
	
	//The image button for turning the sound on/off
	private final Image soundImageButton = new Image();
	//The simple panel that stores the image button
	private final SimplePanel mainPanel = new SimplePanel();
	
	//If true then the sound notification are on, otherwise off
	private boolean isSoundOn;
	
	//Indicates if the user was notified about the new chat message during this server update
	private boolean wasNewChatMessageNotified = false;
	
	//The focus panel to be getting the mouse-move event from
	private final FocusPanel userAreaPanel;
	
	//The mouse move handler registration
	private HandlerRegistration mouseModeHandlerReg = null;
	
	/**
	 * The basic constructor
	 */
	private NewMessageAlertWidget( final FocusPanel userAreaPanel ) {
		//Add the tool tip
		soundImageButton.setTitle( titles.newMessageSoundNotificationButtonToolTip() );
		//Add the click handler, for turning the sounds on/off
		soundImageButton.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				setSoundImageButtonStatus( ! isSoundOn );
			}
		});
		
		//Store the panel
		this.userAreaPanel = userAreaPanel;
		
		//Get the on/off status from the cookie and initialize the visual part of the widget
		setSoundImageButtonStatus( SiteManager.isMessageNotifySoundOn() );
		
		//Initialize the composite
		initWidget( mainPanel );
	}
	
	private void addMouseMoveHandler() {
		//Add this to be a mouse move handler if the mouse handler is not set
		if( mouseModeHandlerReg == null ) {
			mouseModeHandlerReg = userAreaPanel.addMouseMoveHandler( this );
		}
	}
	
	private void removeMouseMoveHandler() {
		//Remove the mouse move handler if it is set
		if( mouseModeHandlerReg != null ) {
			mouseModeHandlerReg.removeHandler();
			mouseModeHandlerReg = null;
		}
	}
	
	/**
	 * Allows to start the notifier and to make its widget visible
	 */
	public void start() {
		//Show the sound notifier button
		setSoundButtonVisible(true);
		//There is nothing to be done apart from showing the widget
	}
	
	/**
	 * Allows to stop the notifier and to make its widget invisible
	 */
	public void stop() {
		//Stop the new-message title notifier if there was a new activity
		stopNewMessageTitleNotification();
		//Hide the sound notifier button
		setSoundButtonVisible(false);
	}
	
	/**
	 * Allows to set the new message sound notify button as visible or hidden
	 * @param visible true for making the button visible, false to hide it
	 */
	public void setSoundButtonVisible( final boolean visible ) {
		mainPanel.setStyleName( CommonResourcesContainer.ALERT_WIDGET_IN_VISIBLE_STYLE );
		mainPanel.clear();
		if( visible ) {
			mainPanel.setStyleName( CommonResourcesContainer.ALERT_WIDGET_VISIBLE_STYLE );
			mainPanel.add( soundImageButton );
		}
	}
	
	/**
	 * Should be called every time the new chat-room data update comes
	 * from the server, and before the user gets notified about new messages
	 */
	public void initiateChatMessagesUpdate() {
		wasNewChatMessageNotified = false;
	}
	
	/**
	 * Allows to check if it makes sense to force a new chat message notification.
	 * Returns false if we have already notified the user during this room update
	 */
	public boolean isNewChatMessageNotificationActual() {
		return !wasNewChatMessageNotified;
	}
	
	/**
	 * Allows to start the current title notification for the new messages
	 */
	public final void startNewMessageTitleNotification() {
		addMouseMoveHandler();
		titleNotifier.startBlinking();
	}
	
	/**
	 * Allows to notify the user about the new chat message
	 * Plays sound only ones during this chat-room data update
	 * Only initiates the title notifications if the user was not active on site
	 * for more than BLINKING_ON_NO_ACTIVITY_TIME_MILLISEC milliseconds
	 */
	public final void newChatMessage() {
		//Always notify the user in the site's navigator if the chat section is not selected
		SiteManagerUI.getInstance().alertNonSelectedSiteSection( CommonResourcesContainer.CHAT_SECTION_IDENTIFIER_STRING );
		
		//Get the current time
		final long currentTimeMillisec = System.currentTimeMillis();
		//Ensure delayed loading the this java script code
		final SplitLoad loader = new SplitLoad(){
			@Override
			public void execute() {
				final UserStatusManager statusManager = UserStatusManager.getInstance();
				if( statusManager != null ) {
					final long lastActivityMillisec = statusManager.getLastUserActivityMillisec();
					//Notify the user in the window title and with the sound if there was no on-site activity for too long
					if( currentTimeMillisec - lastActivityMillisec >= TITLE_NOTIFY_ON_NO_ACTIVITY_TIME_MILLISEC ) {
						//Notify in the title
						startNewMessageTitleNotification();
						//Notify with the sound
						if( isSoundOn ) {
							playChatMessageNotification();
						}
					}
					
					//Indicate that the user was notified about the new message within this set of new messages
					wasNewChatMessageNotified = true;
				}
			}
		};
		loader.loadAndExecute();
	}
	
	/**
	 * Allows to notify the user about the new off-line message
	 */
	public final void newOfflineMessage() {
		startNewMessageTitleNotification();
		if( isSoundOn ) {
			playOfflineMessageNotification();
		}
	}
	
	/**
	 * Allows to stop the current title notification for the new messages
	 */
	public final void stopNewMessageTitleNotification() {
		removeMouseMoveHandler();
		titleNotifier.stopBlinking();
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		//Stop the new-message title notifier if there was a new activity
		stopNewMessageTitleNotification();
	}
	
	/**
	 * Allows to set the image button status on/off
	 * @param isSoundOn true to set the status as on, false for off
	 */
	private final void setSoundImageButtonStatus( final boolean isSoundOn ) {
		this.isSoundOn = isSoundOn;
		SiteManager.setMessageNotifySound( isSoundOn );
		soundImageButton.setUrl( ServerSideAccessManager.getSoundNotificationImageURL( isSoundOn ) );
		soundImageButton.setStyleName( isSoundOn ? CommonResourcesContainer.SOUND_ON_IMAGE_STYLE : CommonResourcesContainer.SOUND_OFF_IMAGE_STYLE );
	}
	
	/**
	 * Allows to notify the user about the new chat message
	 */
	private final native void playChatMessageNotification()  /*-{
		$wnd.soundManager.play('chat_message');
	}-*/;
	
	/**
	 * Allows to notify the user about the offline message
	 */
	private final native void playOfflineMessageNotification()  /*-{
		$wnd.soundManager.play('offline_message');
	}-*/;
	
	/**
	 * Allows to set the site title
	 */
	private final native void setSiteTitle(final String title)/*-{
		$doc.title = title
	}-*/;
}
