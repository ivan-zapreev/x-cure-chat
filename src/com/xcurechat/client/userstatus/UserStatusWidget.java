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
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.userstatus;

import java.util.Iterator;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;

/**
 * @author zapreevis
 * This class represents a user status selector class for the UI
 */
public final class UserStatusWidget extends Composite {
	
	//The main horizontal panel that is the widget's base
	private final HorizontalPanel mainHPanel = new HorizontalPanel();
	//The list box that contains possible user statuses
	private final ListBox userStatusesBox = new ListBox();
	//The simple panel holding the status panel or the progress bar
	private final SimplePanel statusImagePanel = new SimplePanel();
	//The image that reflects the user status
	private final Image userStatusImage = new Image();
	//The loading progress bar
	private final ServerCommStatusPanel progressBarUI = new ServerCommStatusPanel();
	//The label that will store the user name
	private final Label userNameLabel = new Label( );
	
	/**
	 * The simple constructor
	 */
	public UserStatusWidget() {
		//Set the alignment
		mainHPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		//Set the user label style
		userNameLabel.addStyleName( CommonResourcesContainer.USER_NAME_LABEL_STYLE );
		//Construct the widget components
		mainHPanel.add( userNameLabel );
		mainHPanel.add( new HTML("&nbsp;") );
		//Add the status image to the panel
		userStatusImage.setStyleName( CommonResourcesContainer.USER_STATUS_IMAGE_STYLE_NAME );
		statusImagePanel.add( userStatusImage );
		mainHPanel.add( statusImagePanel );
		userStatusesBox.setEnabled( true );
		//Fill out the status list box
		Iterator<String> iter = UserStatusHelper.getStatusStringToStatusID().keySet().iterator();
		while( iter.hasNext() ) {
			String statusString = iter.next();
			userStatusesBox.addItem( statusString, statusString );
		}
		//Add the change handler
		userStatusesBox.addChangeHandler( new ChangeHandler(){
			public void onChange(ChangeEvent e){
				//Get the selected user status and set it on the server and here
				UserStatusManager.getUserStatusQueue().setCurrentUserStatus( getNewlySelectedUserStatus() );
			}
		});
		//Add the status list box to the panel
		userStatusesBox.setStyleName( CommonResourcesContainer.USER_STATUS_LIST_BOX_STYLE_NAME );
		mainHPanel.add( userStatusesBox );
				
		//Initialize the widget
		initWidget( mainHPanel );
	}
	
	/**
	 * Allows to set the user name into the status widget.
	 * I.e. when the widget will shows the status of the given logged in user.
	 * @param userLoginName the user name to set
	 */
	public void setUserLoginName( final String userLoginName ) {
		userNameLabel.setText( userLoginName +":" );
	}
	
	/**
	 * Allows to get the progress bar associated with this status widget 
	 */
	ServerCommStatusPanel getProgressBarUI() {
		return progressBarUI;
	}
	
	/**
	 * This method removes everything from the status
	 * image panel and adds the progress bar panel into it
	 */
	void prepareForServerCommunication() {
		//Disable the status list box
		userStatusesBox.setEnabled( false );
		//Set the loading image
		statusImagePanel.clear();
		statusImagePanel.add( progressBarUI );
	}
	
	/**
	 * This method restores the set user status
	 */
	void finishServerCommunication() {
		//Set the currently set status image back
		statusImagePanel.clear();
		statusImagePanel.add( userStatusImage );
		//Enable the list box
		userStatusesBox.setEnabled(true);		
	}
	
	/**
	 * Allows to set the user-status type in the user-status list box
	 * Note that, this method makes sure that the previous user status can never be one of the away statuses
	 * @param userStatus the new user status to be set
	 */
	void setCurrentUserStatus( final UserStatusType userStatus ) {
		//Set the user-status list box selected value 
		final String statusString = UserStatusHelper.getUserStatusMsg( userStatus );
		for( int index = 0; index < userStatusesBox.getItemCount(); index ++) {
			if( userStatusesBox.getItemText(index).equals( statusString ) ) {
				userStatusesBox.setSelectedIndex( index );
				break;
			}
		}
		//Set the status image url
		statusImagePanel.clear();
		userStatusImage.setUrl( UserStatusHelper.getUserStatusImgURL( userStatus ) );
		statusImagePanel.add( userStatusImage );
	}
	
	/**
	 * Allows to retrieve the user status type set in the user-status list box
	 * @return
	 */
	private UserStatusType getNewlySelectedUserStatus() {
		return UserStatusHelper.getUserStatus( userStatusesBox.getValue( userStatusesBox.getSelectedIndex() ) );
	}
}
