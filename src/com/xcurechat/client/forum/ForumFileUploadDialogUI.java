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
 * The user-forum interface package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.forum;

import java.util.List;
import java.util.ArrayList;

import com.google.gwt.user.client.ui.DialogBox;

import com.xcurechat.client.dialogs.FileUploadManagerDialog;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.utils.SupportedFileMimeTypes;

/**
 * @author zapreevis
 * Attach files to the forum message
 */
public class ForumFileUploadDialogUI extends FileUploadManagerDialog {
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
	
	public ForumFileUploadDialogUI(DialogBox parentDialog) {
		super(parentDialog, false);
		
		//Fill dialog with data
		populateDialog();	    
	}
	
	@Override
	protected List<String> getAllowedFileExtensions() {
		return allowedFileExtensions;
	}
	
	@Override
	protected String getDialogTitle() {
		return titlesI18N.forumMessageFileUploadDialog();
	}

	@Override
	protected String getUploadFileServletURL() {
		return ServerSideAccessManager.getForumFileUploadServletURL();
	}
	
	@Override
	protected void onSuccessfulFileUpload( final ShortFileDescriptor fileDesc ) {
		if( parentDialog instanceof ForumFilesManagerUI ) {
			( (ForumFilesManagerUI) parentDialog ).addNewForumMessageFile( fileDesc );
		}
	}

}
