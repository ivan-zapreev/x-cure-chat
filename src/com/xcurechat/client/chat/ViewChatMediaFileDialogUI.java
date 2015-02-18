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
 * The chat site section interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.chat;

import com.google.gwt.user.client.ui.DialogBox;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.dialogs.ViewMediaFilesDialogUI;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.SupportedFileMimeTypes;

/**
 * @author zapreevis
 *
 */
public class ViewChatMediaFileDialogUI extends ViewMediaFilesDialogUI<ShortFileDescriptor> {
	//True if the file is already attached to the chat message, otherwise false 
	private final boolean isMsgAtt;
	//Fhe id of the room the message file comes from
	private final int roomID;
	
	/**
	 * @param roomID the id of the room the message file comes from
	 * @param fileDescr the descriptor of the file we want to view
	 * @param isMsgAtt if the file is already attached to the chat message 
	 * @param parentDialog the parent dialog we call this 
	 */
	public ViewChatMediaFileDialogUI( final int roomID, final ShortFileDescriptor fileDescr,
										final boolean isMsgAtt, final DialogBox parentDialog ) {
		super( parentDialog, fileDescr );
		
		this.isMsgAtt = isMsgAtt;
		this.roomID = roomID;
		
		//In case the file we are viewing comes from the chat, and is not the file the user
		//is adding to the chat message right now, then if this is a playable file we make
		//the dialog non-modal and do not let it auto-close
		if( isMsgAtt && SupportedFileMimeTypes.isPlayableMimeType( fileDescr.mimeType ) ) {
			this.setModal(false);
			this.setAutoHideEnabled(false);
		}
		
		//Fill dialog with data
		this.populateDialog();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ViewMediaFileDialogUI#getMediaFileServerURL(com.xcurechat.client.data.ShortFileDescriptor)
	 */
	@Override
	public String getMediaFileServerURL(ShortFileDescriptor fileDescr) {
		return ServerSideAccessManager.getChatMessageFileURL(roomID, fileDescr, false, isMsgAtt);
	}

	@Override
	public String getDialogTitle(int index) {
		return titlesI18N.chatMessageFileViewDialogTitle();
	}
}
