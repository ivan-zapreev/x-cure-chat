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
 * The forum interface package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.forum.messages;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.forum.ForumSearchManager;

import com.xcurechat.client.rpc.ForumManagerAsync;
import com.xcurechat.client.rpc.RPCAccessManager;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * This is an Admin dialog that allows to move the provided message
 * (identified by its id) to be a child of a selected message (id) 
 */
public class MoveMessageDialogUI extends ActionGridDialog {
	
	//The if of the message that we want to move
	private final int messageID;
	
	//The text box that should contain the id of the new parent message
	private final TextBox moveTextBox = new TextBox();
	
	/**
	 * @param messageID the ID of the message that we want to move
	 * @param parentDialog the id of the parent dialog or null if none
	 */
	public MoveMessageDialogUI(final int messageID, DialogBox parentDialog) {
		super( false, false, true, parentDialog );
		
		//Store the message ID
		this.messageID = messageID;
		
		//Set the dialog's caption.
		this.setText( titlesI18N.moveForumMessageDialogTitle( messageID ) );
		
		//Enable the action buttons and hot key
		setLeftEnabled( true );
		setRightEnabled( true );
		
		//Fill dialog with data
		populateDialog();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ActionGridDialog#populateDialog()
	 */
	@Override
	protected void populateDialog() {
		addNewGrid( 2, false, "", false);
		
		Label moveFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.moveForumMessageToParent(), true );
		moveFieldTitle.setWordWrap( false );
		addToGrid( FIRST_COLUMN_INDEX, moveFieldTitle, false, false );
		addToGrid( SECOND_COLUMN_INDEX, moveTextBox, false, true );
		
		this.addGridActionElements( true, true, true, true );
	}
	
	/**
	 * Allows to set the main control elements enabled and or disabled
	 * @param enabled if true the elements are enabled, otherwise disabled
	 */
	private void setEnabled( final boolean enabled ) {
		setLeftEnabled( enabled );
		setRightEnabled( enabled );
		moveTextBox.setEnabled( enabled );
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ActionGridDialog#actionLeftButton()
	 */
	@Override
	protected void actionLeftButton() {
		hide();
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ActionGridDialog#actionRightButton()
	 */
	@Override
	protected void actionRightButton() {
		//Disable the control elements
		setEnabled( false );
		
		try {
			//Try to get the parent message ID
			final int newParentMessageID = Integer.parseInt( moveTextBox.getText().trim() );
			
			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Perform the server call in order to search for forum messages
					CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>( progressBarUI ) {
						public void onSuccessAct(Void result) {
							//Close the dialog
							hide();
							//Enable control elements
							setEnabled( true );
							//Refresh the current forum page, but do not update the history
							ForumSearchManager.doSearch();
						}
						public void onFailureAct(final Throwable caught) {
							(new SplitLoad( true ) {
								@Override
								public void execute() {
									//Report the error
									ErrorMessagesDialogUI.openErrorDialog( caught );
								}
							}).loadAndExecute();
							//Use the recovery method
							recover();
						}
					};
					
					//Perform the server call
					ForumManagerAsync forumManagerObj = RPCAccessManager.getForumManagerAsync();
					forumManagerObj.moveForumMessage(SiteManager.getUserID(), SiteManager.getUserSessionId(), messageID, newParentMessageID, callback );
				}
				@Override
				public void recover() {
					//Enable control elements
					setEnabled( true );
				}
			}).loadAndExecute();
		} catch( NumberFormatException e ) {
			//NOTE: Do not report any error
			//Clean up the text field
			moveTextBox.setText("");
			//Enable the controls
			setEnabled( true );
		}
	}
	
	@Override
	protected String getLeftButtonText(){
		return titlesI18N.cancelButton();
	}

	@Override
	protected String getRightButtonText(){
		return titlesI18N.moveButton();
	}

}
