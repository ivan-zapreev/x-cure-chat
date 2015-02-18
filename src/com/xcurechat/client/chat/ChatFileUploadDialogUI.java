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
package com.xcurechat.client.chat;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.chat.messages.SendChatMessageManager;

import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.dialogs.FileUploadManagerDialog;
import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;

import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.SupportedFileMimeTypes;

/**
 * @author zapreevis
 * This is the chat message file management dialog
 */
public class ChatFileUploadDialogUI extends FileUploadManagerDialog {
	//The list of file extensions that are allowed to be uploaded to the server by this dialog
	public static final List<String> allowedFileExtensions = new ArrayList<String>();
	
	static {
		allowedFileExtensions.add( SupportedFileMimeTypes.MimeHelper.MP3_FILE_SUFIX );
		allowedFileExtensions.add( SupportedFileMimeTypes.MimeHelper.MP4_FILE_SUFIX );
		allowedFileExtensions.add( SupportedFileMimeTypes.MimeHelper.FLV_FILE_SUFIX );
		allowedFileExtensions.add( SupportedFileMimeTypes.MimeHelper.SWF_FILE_SUFIX );
		allowedFileExtensions.add( SupportedFileMimeTypes.MimeHelper.JPEG_FILE_SUFIX );
		allowedFileExtensions.add( SupportedFileMimeTypes.MimeHelper.JPG_FILE_SUFIX );
		allowedFileExtensions.add( SupportedFileMimeTypes.MimeHelper.PNG_FILE_SUFIX );
		allowedFileExtensions.add( SupportedFileMimeTypes.MimeHelper.GIF_FILE_SUFIX );
	}
	
	//Stores the file descriptor of the uploaded file or null if these is none 
	private ShortFileDescriptor fileDesc = null;
	//Stores the id of the room to which we send a chat message with this image 
	private final int roomID;
	//The send-chat-message manager
	private final SendChatMessageManager sendMsgManager;
	
	/**
	 * Allows to create chat-message file management dialog
	 * @param roomID the ID of the room for which we create a chat message and thus upload this message image
	 * @param fileDesc the file descriptor of the uploaded file or null if these is none 
	 * @param sendMsgManager the send-chat-message manager
	 */
	public ChatFileUploadDialogUI(final int roomID, final ShortFileDescriptor fileDesc, SendChatMessageManager sendMsgManager) {
		//Do not hide the send chat message dialog
		super( null, true, false );
		
		//Store the data
		this.roomID = roomID;
		this.fileDesc = fileDesc;
		this.sendMsgManager = sendMsgManager;
		
		//Make this dialog not modal and without auto close
		this.setAutoHideEnabled(false);
		this.setModal(false);
		
		//Populate the dialog with data
		this.populateDialog();
		
		//Add the servlet parameters, here it is just the room ID
		this.addHiddenUploadFormParameter( ServerSideAccessManager.ROOM_ID_CHAT_FILES_SERVLET_PARAM, roomID + "");
	}

	@Override
	public void afterFileDeleteComplete() {
		//Update the stored image ID
		this.fileDesc = null;
		//NOTE: Do not update the parent widget with the uploaded file yet, this will be done when the dialog gets closed
	}

	@Override
	protected void onSuccessfulFileUpload(ShortFileDescriptor fileDesc) {
		//Just update the image ID
		this.fileDesc = fileDesc;
		
		//Open the disclosure panel with the uploaded file preview
		openFilePreviewPanel( true );
		
		//NOTE: Do not update the parent widget with the uploaded file yet, this will be done when the dialog gets closed
	}
	
	@Override
	protected void actionLeftButton() {
		//Update the parent send chat message dialog with the new file or null if the file was deleted
		sendMsgManager.onChatRoomMessageFileUploadDilalogClose( roomID, this.fileDesc );
		//Hide the file upload dialog
		hide();
	}
	
	@Override
	protected String getDialogTitle() {
		return titlesI18N.attachChatMessageFileDialogTitle();
	}

	@Override
	public String getUploadedFileThumbnailURL(boolean update) {
		return ServerSideAccessManager.getChatMessageFileURL( roomID, fileDesc, true, false );
	}

	@Override
	public void openFileViewDialog() {
		//Open the file view dialog
		final ShortFileDescriptor dummyFileDescr;
		//Check if the file descriptor is null
		if( fileDesc == null ) {
			//Construct a dummy file descriptor for retrieving the default file from the server 
			dummyFileDescr = new ShortFileDescriptor( );
		} else {
			//Use the current file descriptor
			dummyFileDescr = fileDesc;
		}
		//Ensure lazy loading
		(new SplitLoad( true ) {
			@Override
			public void execute() {
				ViewChatMediaFileDialogUI dialog = new ViewChatMediaFileDialogUI( roomID, dummyFileDescr, false, thisDialog );
				dialog.show();
				dialog.center();
			}
		}).loadAndExecute();
	}

	@Override
	public boolean performFileDeleteRequest(final AsyncCallback<Void> callback) {
		if( fileDesc != null ) {
			//Ensure lazy loading
			(new SplitLoad(){
				@Override
				public void execute() {
					//Get the room's data from the server and open the room editing dialog
					RoomManagerAsync roomsManager = RPCAccessManager.getRoomManagerAsync();
					roomsManager.deleteChatMessageImage( SiteManager.getUserID(),
														 SiteManager.getUserSessionId(),
														 roomID, fileDesc.fileID, callback);
				}
				@Override
				public void recover() {
					callback.onFailure( new InternalSiteException( I18NManager.getErrors().serverDataLoadingFalied() ) );
				}
			}).loadAndExecute();
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean isDeleteFileViewOpen() {
		return fileDesc != null;
	}
	
	@Override
	public boolean isUploadFileViewOpen() {
		return true; //(fileDesc == null);
	}

	@Override
	protected List<String> getAllowedFileExtensions() {
		return allowedFileExtensions;
	}

	@Override
	protected String getUploadFileServletURL() {
		return ServerSideAccessManager.getChatMessageFileManagerURL();
	}

}
