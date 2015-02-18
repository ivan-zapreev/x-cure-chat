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
 */
package com.xcurechat.client.dialogs.room;

import java.util.List;

import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.CloseEvent;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PopupPanel;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.search.NonRoomUserSearchData;
import com.xcurechat.client.data.search.UserSearchData;

import com.xcurechat.client.dialogs.UserSearchDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * Allows to search for users and to add user-room access rihts for them
 */
public class AddRoomUserDialogUI extends UserSearchDialog {
	//The number of result rows per page
	private static final int NUMBER_OF_ROWS_PER_PAGE = UserSearchData.MAX_NUMBER_OF_USERS_PER_PAGE;

	//The reference to the parent dialog
	private final DialogBox parentDialog;
	
	//The ID of the room we want to add users to
	private final int roomID;
	//The room name we add users to
	private final String roomName;
	
	public AddRoomUserDialogUI( final int roomID, final String roomName, final DialogBox parentDialog ){
		super( NUMBER_OF_ROWS_PER_PAGE, parentDialog);
		
		this.parentDialog = parentDialog;
		this.roomID = roomID;
		this.roomName = roomName;
		
		//Set the dialog's caption.
		updateDialogTitle();
		
		//Populate the dialog with elements
		populateDialog();
	}
	
	/**
	 * Allows to update data in the given dialog and in the parent
	 * dialog if it is present and it is a RoomUsersManagerDialogUI
	 */
	protected void updateDialogsData() {
		//Update the current page since we want to remove the added user from the list
		this.updateActualData();
		//Update the parent's dialog page, since we want the user to appear there
		if( ( parentDialog != null ) && ( parentDialog instanceof RoomUsersManagerDialogUI ) ) {
			//We retrieve initial data because if we just update the current
			//page then the number of pages in the dialog title is not updated  
			((RoomUsersManagerDialogUI) parentDialog).updateActualData();
		}
	}
	
	//The local variable needed to store the popup listener for sequential
	//addition of user-access objects in the admin settings
	private CloseHandler<PopupPanel> sequentialDialogShowListener = null;
	
	@Override
	protected void actionButtonAction(final List<Integer> userIDS, final List<String> userLoginNames) {
		if( userIDS.isEmpty() ) {
			//Give an error message that there are no selected users
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					//Report the error
					ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().thereIsNoSelectedUsers() );
				}
			}).loadAndExecute();
		} else { 
			//Disable vontrols
			enableControls( false );
			if( isAdmin) {
				//Ensure lazy loading
				( new SplitLoad( true ) {
					@Override
					public void execute() {
						//Hide this dialog
						final AddRoomUserDialogUI thisDialog = AddRoomUserDialogUI.this;
						thisDialog.setVisible(false);
						//Create the first access dialog in the chain, with no parent dialog passed
						//This way we avoid the user-search dialog appearing and disappearing. 
						final RoomUserAccessDialogUI accessDialog = new RoomUserAccessDialogUI(true, null, null, userIDS.get(0),
																								userLoginNames.get(0), roomID, roomName );
						sequentialDialogShowListener = new CloseHandler<PopupPanel>() {
							private int indexUserID = 0;
							public void onClose(CloseEvent<PopupPanel> e) {
								//Update the index to move to the next ID/Login pare. The dialog 
								//for indexUserID == 0 is shown from outside of this listener class.
								indexUserID++;
								if( indexUserID < userIDS.size() ) {
									//There are still users to add open new dialog
									RoomUserAccessDialogUI accessDialog = new RoomUserAccessDialogUI(true, null, null, userIDS.get(indexUserID),
																									userLoginNames.get(indexUserID), roomID, roomName );
									//Add this popup listener to the new dialog 
									accessDialog.addCloseHandler( this );
									accessDialog.show();
									accessDialog.center();
								} else {
									//There are no more users to add, update the search
									//results and show the user-search dialog
									thisDialog.updateDialogsData();
									thisDialog.setVisible(true);
									thisDialog.center();
									//Enable controls
									enableControls( true );
								}
							}
						};
						//Add the popup listener that on close will open the next dialog or will 
						//show the search dialog if all the user access data objects were set
						accessDialog.addCloseHandler( sequentialDialogShowListener );
						accessDialog.show();
						accessDialog.center();
					}
				}).loadAndExecute();
			} else {
				//Ensure lazy loading
				(new SplitLoad( true ){
					@Override
					public void execute() {
						//Create a new user room access on the server
						CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
							public void onSuccessAct(Void result) {
								//Update the list of users in the search dialog and also in the room's users dialog
								updateDialogsData();
								//Enable controls
								enableControls( true );
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
						//Do the RPC call to add users
						RoomManagerAsync roomManagerObject = RPCAccessManager.getRoomManagerAsync();
						roomManagerObject.createRoomAccess(SiteManager.getUserID(), SiteManager.getUserSessionId(), userIDS, roomID, callback);
					}
					@Override
					public void recover() {
						//Enable controls
						enableControls( true );
					}
				}).loadAndExecute();
			}
		}
	}
	
	@Override
	protected void warnUserNoDataToDisplay(){
		(new SplitLoad( true ) {
			@Override
			public void execute() {
				InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().noNewUsersToAddToTheChatRoom() );
			}
		}).loadAndExecute();
	}

	@Override
	protected String actionButtonText() {
		return titlesI18N.addButtonTitle();
	}

	@Override
	protected UserSearchData getExtraUserSearchData() {
		NonRoomUserSearchData extData = new NonRoomUserSearchData();
		extData.roomID = roomID;
		return extData;
	}

	@Override
	protected void updateDialogTitle( final int numberOfEntries, final int numberOfPages, final int currentPageNumber ) {
		this.setText( titlesI18N.addUsersToRoomDialogTitle( numberOfEntries, currentPageNumber, numberOfPages ) );
	}
}
