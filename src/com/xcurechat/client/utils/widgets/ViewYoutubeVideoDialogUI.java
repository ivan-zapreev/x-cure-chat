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
 * The user interface util widgets package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils.widgets;

import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.dialogs.ViewMediaDialogBase;

import com.xcurechat.client.utils.YoutubeLinksHandlerUtils;

/**
 * @author zapreevis
 * This dialog allows to show the youtube video in it.
 */
public class ViewYoutubeVideoDialogUI extends ViewMediaDialogBase {

	//The url of the youtube media URL
	private final String youtubeMediaURL;
	//The original youtube link URL
	private final String youtubeURL;
	
	//Stores the dialog title
	private final String dialogTitle = titlesI18N.viewYoutubeVideoDialogTitle();
	
	/**
	 * Allows to create the youtube video
	 * @param youtubeMediaURL the youtube media URL
	 * @param youtubeURL the original youtube link URL
	 */
	public ViewYoutubeVideoDialogUI( final String youtubeMediaURL, final String youtubeURL ) {
		super( false, false, false, null, 1, 0 );
		
		//Store the data
		this.youtubeMediaURL = youtubeMediaURL;
		this.youtubeURL      = youtubeURL;
		
		populateDialog();
	}
	
	/**
	 * Set the current image to the image panel, if the image is
	 * not created yet, then we create a new one.
	 */
	@Override
	protected Widget getMediaFileWidget( final int index ) {
		return YoutubeLinksHandlerUtils.getYoutubeEmbeddedFlashObject( youtubeMediaURL, youtubeURL, false );
	}

	@Override
	public String getDialogTitle(int index) {
		return dialogTitle;
	}

	@Override
	protected void rotateImageRight(int index) {
		//NOTE: For the time being, we do not rotate the static images, as it is not needed
	}

}
