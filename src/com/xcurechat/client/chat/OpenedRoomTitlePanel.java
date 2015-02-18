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
package com.xcurechat.client.chat;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.Timer;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import com.xcurechat.client.data.ChatRoomData;


import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.popup.RoomInfoPopupPanel;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class represents the room's tab title panel
 */
class OpenedRoomTitlePanel extends Composite {
	//The delay in millisec after which we open the room info Popup
	private static final int ROOM_INFO_POPUP_OPEN_DELAY_MILLISEC = 1000;
	
	//The label that will store the room's title
	private Label roomNameLabel = new Label();
	//The close room image, if needed
	private Image closeImage = new Image();
	//The close room click listener
	private final ClickHandler closeRoomClickListener;
	private HandlerRegistration closeRoomClickHandlerRegistration = null;
	//Stores true if the close button was clicked
	private boolean isCloseClicked = false;
	
	//The room info image
	private Image infoImage = new Image();
	//The room info click listener
	private final MouseOverHandler roomInfoOpenHandler;
	private HandlerRegistration roomInfoOpenHandlerRegistration = null;
	private Timer roomInfoOpenTimer = new Timer() {
		public void run() {
			//Ensure lazy loading
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					//Just open the room info
					RoomInfoPopupPanel.openRoomViewPopup( localRoomData, roomNameLabel, false, roomsManager );
				}
			};
			executor.loadAndExecute();
		}
	};
	
	//If true then we are in the info mode, otherwise fase
	private boolean isInfo = true;
	//True if the title bar is in the enabled mode
	private boolean isTitleBarEnabled = false;
	//The internationalization object
	private UITitlesI18N i18nTitles = I18NManager.getTitles();
	//The local chat room Data
	private ChatRoomData localRoomData = null;
	//The instance of the rooms manager UI
	private final RoomsManagerUI roomsManager;
	
	public OpenedRoomTitlePanel(final ChatRoomData roomData, final RoomsManagerUI roomsManager) {
		//Set the room data
		setLocalRoomData( roomData );
		
		//Store the reference to the rooms manager
		this.roomsManager = roomsManager;
		
		//Create the close room click listener
		closeRoomClickListener = new ClickHandler(){
			public void onClick(ClickEvent e){
				if( ! isCloseClicked ) {
					isCloseClicked = true;
					setCloseEnabled( false );
					roomsManager.closeRoom( localRoomData.getRoomID() );
				}
			}
		};
		
		//Create the room's open and close handlers
		roomInfoOpenHandler = new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent e) {
				//Reschedule the timer
				roomInfoOpenTimer.cancel();
				roomInfoOpenTimer.schedule( ROOM_INFO_POPUP_OPEN_DELAY_MILLISEC );
			}
		};
		//Add the canceling handler right to the image
		infoImage.addMouseOutHandler( new MouseOutHandler(){
			public void onMouseOut( MouseOutEvent e ) {
				//Just cancel the timer
				roomInfoOpenTimer.cancel();
			}
		});
		
		//Create the room's title panel
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSize("100%", "100%");

		//Manage the room info image
		infoImage.setStyleName( CommonResourcesContainer.TITLE_ACTION_IMAGE_STYLE );
		//Manage the room name
		roomNameLabel.setWordWrap(false); //If not done, the multiple word label will be split into lines
		//Manage the close button image
		closeImage.setStyleName( CommonResourcesContainer.TITLE_ACTION_IMAGE_STYLE );
		
		panel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		panel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		panel.add( infoImage );
		panel.add( new HTML("&nbsp;") );
		panel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		panel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		panel.add( roomNameLabel );
		panel.add( new HTML("&nbsp;") );
		panel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		panel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		panel.add( closeImage );
		
		//All composites must call initWidget() in their constructors.
		initWidget(panel);
	}
	
	/**
	 * Allows to enable/disable the info image button
	 */
	private void setInfoEnabled( final boolean enable ) {
		if( enable ) {
			roomInfoOpenHandlerRegistration = infoImage.addMouseOverHandler( roomInfoOpenHandler );
			if( isInfo ) {
				infoImage.setTitle( i18nTitles.openRoomInfoImageTip() );
				infoImage.setUrl( ServerSideAccessManager.getEnabledRoomInfoButtonImage() );
			} else {
				infoImage.setTitle( i18nTitles.newChatActivityImageTip() + " " + i18nTitles.openRoomInfoImageTip() );
				infoImage.setUrl( ServerSideAccessManager.getEnabledRoomMsgButtonImage() );
			}
		} else {
			if( roomInfoOpenHandlerRegistration != null ) {
				roomInfoOpenHandlerRegistration.removeHandler();
				roomInfoOpenHandlerRegistration = null;
			}
			infoImage.setTitle( "" );
			if( isInfo ) {
				infoImage.setUrl( ServerSideAccessManager.getDisabledRoomInfoButtonImage() );
			} else {
				infoImage.setUrl( ServerSideAccessManager.getDisabledRoomMsgButtonImage() );
			}
		}
	}
	
	/**
	 * Allows to enable and disable the room's close button if any
	 * @param enable if true then enable in case the close button
	 * was not already clicked.
	 */
	private void setCloseEnabled( final boolean enable ) {
		//If there is more than one opened room
		if( enable && roomsManager.hasMoreThanOneRoomOpened() ) {
			if ( ! isCloseClicked ) {
				closeImage.setUrl( ServerSideAccessManager.getEnabledCloseButtonImage() );
				closeImage.setTitle( i18nTitles.getCloseRoomImageTip( roomNameLabel.getText() ) );
				closeRoomClickHandlerRegistration = closeImage.addClickHandler( closeRoomClickListener );
			}
		} else {
			if( closeRoomClickHandlerRegistration != null){
				closeRoomClickHandlerRegistration.removeHandler();
				closeRoomClickHandlerRegistration = null;
			}
			closeImage.setUrl( ServerSideAccessManager.getDisabledCloseButtonImage() );
			closeImage.setTitle( "" );
		}
	}
	
	/**
	 * Allows to indicate that the tab bar is selected so it's state has to change
	 * @param enabled if true then the tab is selected, otherwise false
	 */
	public void setRoomTabTitleSelected( final boolean enable ) {
		//Store the title bar status
		isTitleBarEnabled = enable;
		if( enable == true ) {
			//If the tab is being selected then we remove the new message image from it
			this.isInfo = true;
		}
		//Set the info button on/off
		setInfoEnabled( enable );
		//Set the close button on/off 
		setCloseEnabled( enable );
	}
	
	/**
	 * @return returns true if the tab is visible i.e. it is a selected tab
	 */
	public boolean isTabSelected() {
		return isTitleBarEnabled;
	}
	
	/**
	 * Allows to manage the title's room info image
	 * @param isInfo if true then we show the info image,
	 * otherwise it is the new room's activity image.
	 */
	public void setTitleInfoImageType( final boolean isInfo ){
		this.isInfo = isInfo;
		setInfoEnabled( isTitleBarEnabled );
	}
	
	/**
	 * Allows to set the new room's data
	 * @return roomData the room data object to update from
	 */
	public void setLocalRoomData(final ChatRoomData roomData) {
		localRoomData = roomData;
		InterfaceUtils.populateRoomNameLabel( roomData, true, roomNameLabel );
	}
}  
