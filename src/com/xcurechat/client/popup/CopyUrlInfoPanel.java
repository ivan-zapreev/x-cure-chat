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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.popup;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author zapreevis
 * THis pop-up allows to display an url in a non-editable text box
 */
public class CopyUrlInfoPanel extends InfoPopupPanel {
	//The maximum view of the text box
	private final static int MAX_VISIBLE_URL_LENGTH = 60;
	
	//The text box with the url
	private TextBox urlTextBox = new TextBox();
	
	/**
	 * @param autoHide
	 * @param modal
	 */
	private CopyUrlInfoPanel(boolean autoHide, boolean modal, final String url) {
		super(autoHide, modal);
		
		final String localUrl = ( url != null ) ? url : "";
		urlTextBox.setText( localUrl );
		urlTextBox.setVisibleLength( ( localUrl.length() <= MAX_VISIBLE_URL_LENGTH ) ? localUrl.length() : MAX_VISIBLE_URL_LENGTH );
		urlTextBox.setReadOnly(true );
		this.addContentWidget( urlTextBox );
	}
	
	/**
	 * Allows to open a new popup with the url link
	 * @param opener the widget from which we open this pop-up
	 * @param url the url to be displayed
	 */
	public static void showPopup( final Widget opener, final String url ) {
		//Create the popup panel object
		final CopyUrlInfoPanel panel = new CopyUrlInfoPanel( true, true, url );
		//Do not do animation because otherwise Firefox will remove url text selection
		panel.setAnimationEnabled(false);
		//Show the pop-up panel at some proper position, in such a way that
		//it does not go outside the window area, also make the popup modal
		panel.setPopupPositionAndShow( panel.new InfoPopUpPositionCallback( opener ) );
		//Set the text in the text field to be selected, also add focus
		//Do it deferred in order to  make sure that the selectAll is
		//called after the pop-up is shown and visible
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			@Override
			public void execute() {
				panel.urlTextBox.setFocus(true);
				panel.urlTextBox.selectAll();
			}
		});
	}

}
