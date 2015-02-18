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

import java.util.Map;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.popup.RoomInfoPopupPanel;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;


/**
 * @author zapreevis
 * This class visualizes the room item in the tree
 */
public class RoomTreeItem extends Composite {
	private final FocusPanel theItemPanel = new FocusPanel(); 
	private final HorizontalPanel theMainHorizontalPanel = new HorizontalPanel(); 
	private ChatRoomData roomDataObject;
	//This lael stores the number of room visitors
	private Label roomVisitors = new Label();
	
	private final RoomTreeItem thisItem = this;
	
	public RoomTreeItem( ChatRoomData roomData, Map<Integer, Integer> activeRoomVisitors, final RoomsManagerUI roomsManager ) {
		this.roomDataObject = roomData;
		
		theItemPanel.setStyleName( CommonResourcesContainer.CHAT_ROOM_TREE_ITEM_STYLE_NAME );
		theItemPanel.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent event) {
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						RoomInfoPopupPanel.openRoomViewPopup( roomDataObject, thisItem, true, roomsManager );
					}
				};
				executor.loadAndExecute();
				//In Safari clicking inside a scroll panel makes is scroll up
				//(to the top) this is what we want to prevent here
				event.stopPropagation();
				event.preventDefault();
			}
		} );
		final Label roomNameLabel = new Label( );
		final String longToolTipMsg = InterfaceUtils.populateRoomNameLabel( roomData, false, roomNameLabel );
		theItemPanel.setTitle( longToolTipMsg );
		
		Image roomImage = new Image( ChatRoomData.getRoomTypeImageURL( roomData ) );
		roomImage.setStyleName( CommonResourcesContainer.CHAT_ROOM_IMAGE_STYLE_NAME );
		
		theMainHorizontalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		theMainHorizontalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		//Add the room image
		theMainHorizontalPanel.add( roomImage );
		theMainHorizontalPanel.add( new HTML("&nbsp;" ) );
		//Set the number of visitors
		setRoomVisitors( activeRoomVisitors );
		theMainHorizontalPanel.add( roomVisitors );
		theMainHorizontalPanel.add( new HTML("&nbsp;" ) );
		//Add the room name
		theMainHorizontalPanel.add( roomNameLabel );
		
		theItemPanel.add( theMainHorizontalPanel );
		initWidget( theItemPanel );
	}
	
	/**
	 * Updates the number of room visitors
	 * @param availableRooms the mapping between the room IDs and the number of room visitors
	 */
	public void setRoomVisitors( Map<Integer, Integer> activeRoomVisitors ) {
		Integer visitors = activeRoomVisitors.get( roomDataObject.getRoomID() );
		if( visitors == null ) {
			visitors = 0;
		}
		roomVisitors.setText( "(" + visitors + ")" );
		roomVisitors.setTitle( I18NManager.getTitles().currentNumberOfRoomVisitors( visitors ) );
	}
} 
