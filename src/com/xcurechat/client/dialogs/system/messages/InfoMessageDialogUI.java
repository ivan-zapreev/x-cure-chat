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
package com.xcurechat.client.dialogs.system.messages;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HTML;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.i18n.I18NManager;

/**
 * @author zapreevis
 * Displays info messages
 */
public class InfoMessageDialogUI extends ActionGridDialog {
	//The message
	private final String message;
	//Treat the message as a label or as an HTML code
	private final boolean asHTML;

	private InfoMessageDialogUI( String message, final boolean asHTML ) {
		//Autohide and modal
		super( false, true, true, null );
		
		//Store the message
		this.message = message;
		//Store the message type
		this.asHTML = asHTML;
		
		this.setText( I18NManager.getTitles().infoMessageDialog() );
		this.setStyleName(CommonResourcesContainer.INFO_MESSAGE_DIALOG_STYLE);
		
		setLeftEnabled(true);
		setRightEnabled(true);
		
		//Fill dialog with data
		populateDialog();
	}
	
	@Override
	protected void populateDialog(){
		addNewGrid( 2, false, "", false);
		
		//Add the info message
		final Widget messageLabel;
		if( asHTML ) {
			messageLabel = new HTML( message );
		} else {
			messageLabel = new Label( message );
		}
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, messageLabel, false, false );
		
		//Add the close button
		final Button closeButton = new Button(titlesI18N.closeButtonTitle());
		closeButton.setStyleName(CommonResourcesContainer.INFO_MESSAGE_DIALOG_BUTTON_STYLE);
		closeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent e){
				hide();
			}
		});
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, closeButton, true, true );
	}
	
	protected void actionLeftButton() {
		hide();
	}

	protected void actionRightButton() {
		hide();
	}
	/**
	 * Allows to open a new info message dialog with the message displayed as a label
	 * @param message the info message to display
	 */
	public static void openInfoDialog( final String message ){
		openInfoDialog( message, false );
	}
	
	/**
	 * Allows to open a new info message dialog with the
	 * message displayed as a label or an HTML
	 * @param message the info message to display
	 * @param asHTML if true then the message is displayed as an HTML
	 */
	public static void openInfoDialog( final String message, final boolean asHTML ){
		(new SplitLoad( true ) {
			@Override
			public void execute() {
				InfoMessageDialogUI errMsgDialog = new InfoMessageDialogUI(message, asHTML );
				errMsgDialog.show();
				errMsgDialog.center();
			}
		}).loadAndExecute();
	}

}
