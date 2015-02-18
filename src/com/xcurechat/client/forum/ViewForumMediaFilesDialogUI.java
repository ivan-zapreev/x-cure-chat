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
package com.xcurechat.client.forum;

import java.util.List;

import com.google.gwt.user.client.ui.DialogBox;

import com.xcurechat.client.dialogs.ViewMediaFilesDialogUI;

import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * Allows to view the forum-message image
 */
public class ViewForumMediaFilesDialogUI extends ViewMediaFilesDialogUI<ShortFileDescriptor> {
	
	public ViewForumMediaFilesDialogUI( final ShortFileDescriptor fileDescr, final DialogBox parentDialog ) {
		super( parentDialog, fileDescr  );
		
		//Fill dialog with data
		populateDialog();
	}
	
	public ViewForumMediaFilesDialogUI( final DialogBox parentDialog, final List<ShortFileDescriptor> filesList, final int currentIndex ) {
		super( parentDialog, filesList, currentIndex);
		
		//Fill dialog with data
		populateDialog();
	}
	
	@Override
	public String getMediaFileServerURL(ShortFileDescriptor fileDescr) {
		return ServerSideAccessManager.getForumFileURL( fileDescr, false );
	}
	
	@Override
	public String getDialogTitle(int index) {
		return titlesI18N.forumMessageMediaViewDialogTitle( ShortFileDescriptor.getShortFileName( this.getMediaFileDescriptor(index).fileName ) );
	}

}
