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
package com.xcurechat.client.utils.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
//import com.google.gwt.user.client.ui.Label;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * This is the status composit for indicating the loading status
 */
public class ServerCommStatusPanel extends Composite {
	//The horizontal panel that will store the message body loading status
	private final HorizontalPanel loadingStatusPanel = new HorizontalPanel(); 
	
	//The message body loading status image
	private final Image loadingStatusImg = new Image();

	//The body loading status text
	//private final Label loadingStatusText = new Label();
	
	//Contains the number of currently active server communications
	private int loadingCount = 0;
	
	//The localization for the text
	private final UITitlesI18N titlesI18N = I18NManager.getTitles();
	
	/**
	 * The simple constructor
	 */
	public ServerCommStatusPanel() {
		//Set alignments and style
		loadingStatusPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		loadingStatusPanel.setStyleName( CommonResourcesContainer.SERVER_COMMUNICATION_STATUS_PANEL_STYLE );
		loadingStatusPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		loadingStatusPanel.setTitle( titlesI18N.successWhileCommunicatingToolTipText() );
		loadingStatusImg.setUrl( ServerSideAccessManager.getSuccessImageURL() );
		loadingStatusPanel.add( loadingStatusImg );
		//Initialize the widget
		initWidget( loadingStatusPanel );
	}
	
	/**
	 * Starts the loading progress bar
	 */
	public synchronized void startProgressBar() {
		if( loadingCount == 0 ) {
			//Set the sitle and the image
			loadingStatusPanel.setTitle( titlesI18N.communicatingToolTipText() );
			loadingStatusImg.setUrl( ServerSideAccessManager.getActivityImageURL() );
		}
		//Increase the number of server communications
		loadingCount++;
	}
	
	/**
	 * Allows to stop the progress bar and indicate the loading error if any.
	 * The change in the status is done only if the internal counter shows that
	 * there are active server communications (the non-finished ones)
	 * TODO: In the future we have to somehow store the history of communications
	 * because otherwise if two are done at the same moment and one fails before
	 * the second ends with success then the failure is not indicated in the status bar
	 * TODO: May be add a status for a successfull server communication
	 * @param isError if true then the error should be indicated
	 */
	public synchronized void stopProgressBar( final boolean isError ) {
		//If there are no server communications left, then report
		if( loadingCount > 0 ) {
			//Decrease the number of server communications
			loadingCount--;
			if( isError ) {
				//Set the error status icons
				loadingStatusPanel.setTitle( titlesI18N.errorWhileCommunicatingToolTipText() );
				loadingStatusImg.setUrl( ServerSideAccessManager.getErrorImageURL() );
			} else {
				//The last loading action was successful
				loadingStatusPanel.setTitle( titlesI18N.successWhileCommunicatingToolTipText() );
				loadingStatusImg.setUrl( ServerSideAccessManager.getSuccessImageURL() );
			}
			//Just in case set the counter to zero
			loadingCount = 0;
		}
	}
	
	/**
	 * Allows to reset the progress bar to the successful state,
	 * plus resets the internal counter of active server
	 * connections (the non-finished ones) to zero.
	 */
	public synchronized void cleanProgressBar( ) {
		//Reset the counter
		loadingCount = 0;
		//The last loading action was successful
		loadingStatusPanel.setTitle( titlesI18N.successWhileCommunicatingToolTipText() );
		loadingStatusImg.setUrl( ServerSideAccessManager.getSuccessImageURL() );		
	}
}
