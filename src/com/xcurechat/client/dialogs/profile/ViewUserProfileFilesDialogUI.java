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
package com.xcurechat.client.dialogs.profile;

import java.util.List;

import com.google.gwt.user.client.ui.DialogBox;

import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.dialogs.ViewMediaFilesDialogUI;

import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * The dialog for viewing user profile files
 */
public class ViewUserProfileFilesDialogUI extends ViewMediaFilesDialogUI<ShortFileDescriptor> {
	
	//The user name for which the images are shown
	private final String userLoginName;
	//The UID of the user for which the images are shown
	private final int userID;
	
	/**
	 * Allows to view one media file in the dialog
	 * @param parentDialog the parent dialog
	 * @param fileList the list of media file descriptors, not NULL
	 * @param currentIndex the index of the file to show first
	 */
	public ViewUserProfileFilesDialogUI( final DialogBox parentDialog, final ShortFileDescriptor fileDescr,
								   		 final String userLoginName, final int userID ) {
		super( parentDialog, fileDescr );
		
		//Save supplementary data
		this.userLoginName = userLoginName;
		this.userID = userID;
		
		//Fill dialog with data
		populateDialog();
	}
	
	public ViewUserProfileFilesDialogUI( final DialogBox parentDialog, final List<ShortFileDescriptor> fileList,
										  final int currentIndex, final String userLoginName, final int userID ) {
		super( parentDialog, fileList, currentIndex);
		
		//Save supplementary data
		this.userLoginName = userLoginName;
		this.userID = userID;
		
		//Fill dialog with data
		populateDialog();
	}

	@Override
	public String getMediaFileServerURL(ShortFileDescriptor fileDescr) {
		return ServerSideAccessManager.getProfileFileURL( userID, fileDescr, false );
	}

	@Override
	public String getDialogTitle(int index) {
		final String shortName = ShortFileDescriptor.getShortFileName( this.getMediaFileDescriptor(index).fileName );
		return titlesI18N.userProfileFilesShowDialogTitle( userLoginName, shortName );
	}

}
