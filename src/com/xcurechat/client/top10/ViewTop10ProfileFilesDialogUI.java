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
package com.xcurechat.client.top10;

import java.util.List;

import com.google.gwt.user.client.ui.DialogBox;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.ShortUserFileDescriptor;
import com.xcurechat.client.dialogs.ViewMediaFilesDialogUI;
import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * This dialog allows to view files from the TOP10 new user files
 */
public class ViewTop10ProfileFilesDialogUI extends ViewMediaFilesDialogUI<ShortUserFileDescriptor> {

	/**
	 * The basic constructor for viewing one file
	 * @param parentDialog the parent dialog
	 * @param fileDescr the file descriptor
	 */
	public ViewTop10ProfileFilesDialogUI(DialogBox parentDialog, ShortUserFileDescriptor fileDescr) {
		super(parentDialog, fileDescr);
		
		//Fill dialog with data
		populateDialog();
	}

	/**
	 * Allows to view one media file in the dialog
	 * @param parentDialog the parent dialog
	 * @param fileList the list of media file descriptors, not NULL
	 * @param currentIndex the index of the file to show first
	 */
	public ViewTop10ProfileFilesDialogUI(DialogBox parentDialog, List<ShortUserFileDescriptor> fileList, int currentIndex) {
		super(parentDialog, fileList, currentIndex);
		
		//Fill dialog with data
		populateDialog();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ViewMediaFilesDialogUI#getMediaFileServerURL(com.xcurechat.client.data.ShortFileDescriptor)
	 */
	@Override
	public String getMediaFileServerURL(ShortUserFileDescriptor fileDescr) {
		return ServerSideAccessManager.getProfileFileURL( fileDescr.ownerID, fileDescr, false );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ViewMediaDialogBase#getDialogTitle(int)
	 */
	@Override
	public String getDialogTitle(int index) {
		final ShortUserFileDescriptor fileDescr = this.getMediaFileDescriptor(index);
		return titlesI18N.userProfileFilesShowDialogTitle( ShortUserData.getShortLoginName( fileDescr.ownerLoginName ),
														   ShortFileDescriptor.getShortFileName( fileDescr.fileName ) );
	}

}
