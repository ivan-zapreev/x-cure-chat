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
package com.xcurechat.client.dialogs;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This dialog allows to browse a single image based on its URL.
 * NOTE: This dialog only displays images of the size 640x480!
 */
public class ViewStaticImageDialogUI extends ViewMediaDialogBase {
	
	//These are the standard minimum image sizes for the image view dialog
	protected static final int MIN_IMAGE_VIEW_WIDTH = 640;
	protected static final int MIN_IMAGE_VIEW_HEIGHT = 480;

	//The url of the chat message image
	private final String mediaURL;
	
	//The panel storing the image
	protected final SimplePanel mediaPanel = new SimplePanel();
	
	//Stores the dialog title
	private final String dialogTitle;
	
	public ViewStaticImageDialogUI( final String dialogTitle, final String mediaURL, final DialogBox parentDialog) {
		this( dialogTitle, mediaURL, parentDialog, true);
	}
	
	/**
	 * Allows to create the image view
	 * @param dialogTitle the dialog's title
	 * @param mediaURL the url of the image or another media file or null
	 * @param parentDialog the parent dialog
	 * @param callPopulate if false then the populate method is not called here and should be called in the subclass constructor
	 * this is for the case when the getCurrentMediaFileWidget() method has to be redefined.
	 */
	public ViewStaticImageDialogUI( final String dialogTitle, final String mediaURL, final DialogBox parentDialog, final boolean callPopulate) {
		super( parentDialog );
		
		//Store the data
		this.mediaURL = mediaURL;
		this.dialogTitle = dialogTitle;
		
		if( callPopulate ) {
			//Fill dialog with data
			populateDialog();
		}
	}
	
	/**
	 * Set the current image to the image panel, if the image is
	 * not created yet, then we create a new one.
	 */
	@Override
	protected Widget getMediaFileWidget( final int index ) {
		//Create the image
		Image image = new Image( mediaURL == null ? "" : mediaURL );
		image.setWidth( MIN_IMAGE_VIEW_WIDTH + "px" );
		image.setHeight( MIN_IMAGE_VIEW_HEIGHT + "px" );
		image.setStyleName( CommonResourcesContainer.IMAGE_MEDIA_FILE_SHOW_STYLE );
		return image;
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
