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
package com.xcurechat.client.dialogs.room;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.data.ChatRoomData;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.dialogs.system.messages.QuestionMessageDialogUI;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.utils.SplitLoad;

/** 
 * @author zapreevis
 * Allows to ask the user if he want to send the room-access request to the room's owner
 */
public class AskForRoomAccessDialogUI extends QuestionMessageDialogUI {

	//The chat room data
	private final ChatRoomData roomData;
	
	public AskForRoomAccessDialogUI( final ChatRoomData roomData ){
		super();
		
		this.roomData = roomData;
		
		//Fill dialog with data
		populateDialog();
	}
	
	@Override
	protected String getDialogQuestion() {
		return titlesI18N.roomAccessRequestDialogQuestion( ChatRoomData.getRoomName( roomData ) );
	}

	@Override
	protected String getDialogTitle() {
		return titlesI18N.roomAccessRequestDialogTitle( );
	}

	@Override
	protected void negativeAnswerAction() {
		hide();
	}

	@Override
	protected void positiveAnswerAction() {
		setControlsEnabled(true, false);
		
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				//Update an old room data on the server
				AsyncCallback<Void> callback = new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						//Hide the dialog window
						hide();
					}
					public void onFailure(final Throwable caught) {
						//Report the error
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								ErrorMessagesDialogUI.openErrorDialog( caught );
							}
						}).loadAndExecute();
						//Do the recovery
						recover();
					}
				};
				RoomManagerAsync roomManagerObject = RPCAccessManager.getRoomManagerAsync();
				roomManagerObject.sendRoomAccessRequest( SiteManager.getUserID(), SiteManager.getUserSessionId(), roomData.getRoomID(), callback);
			}
			@Override
			public void recover() {
				setControlsEnabled(true, true);
			}
		}).loadAndExecute();
	}
}
