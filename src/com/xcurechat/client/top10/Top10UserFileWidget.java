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
 * The client utilities package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.top10;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.SiteBodyComponent;

import com.xcurechat.client.data.ShortUserFileDescriptor;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarWidget;

/**
 * @author zapreevis
 * This class represents a user file preview widget for the TOP10 site section 
 */
public class Top10UserFileWidget extends Composite implements SiteBodyComponent {
	//The localization for the text
	protected static final UITitlesI18N titlesI18N = I18NManager.getTitles();

	//The main vertical panel storing the user's data
	private final VerticalPanel mainPanel;
	//The user file image preview
	private final Image previewImage;
	//The basic user avatar storing only the user name
	private final UserAvatarWidget userNameAvatar;
	//The dialog storing this widget or null if none
	private final DialogBox parentDialog;
	//The given preview file descriptor
	private final ShortUserFileDescriptor fileDescr;
	
	/**
	 * The basic constructor
	 * @param parentDialog the reference to the parent dialog or null if none
	 * @param fileDescriptors the list of the users' profile files to browse through 
	 * @param fileIndex the index of the file we create this preview widget for
	 */
	public Top10UserFileWidget( final DialogBox parentDialog, final List<ShortUserFileDescriptor> fileDescriptors, final int fileIndex ) {
		//Store the data
		this.parentDialog = parentDialog;
		this.fileDescr = fileDescriptors.get( fileIndex );
		
		//Initialize and fill out the main panel
		mainPanel = new VerticalPanel();
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		
		//Initialize the image 
		previewImage = constructThumbnailImage( fileDescriptors, fileIndex );
		
		//Initialize the upload date panel
		final HorizontalPanel uploadDatePanel = new HorizontalPanel();
		uploadDatePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		//Add the image
		uploadDatePanel.add( new Image( ServerSideAccessManager.USER_INFO_RELATED_IMAGES_LOCATION +
				  			 ServerSideAccessManager.SERVER_CONTEXT_DELIMITER + "uploaded.png" ) );
		uploadDatePanel.add( new HTML("&nbsp;") );
		//Add the date label
		{
			final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_SHORT );
			final Label uploadDateLabel = new Label( dateTimeFormat.format( fileDescr.uploadDate ) );
			uploadDateLabel.setStyleName( CommonResourcesContainer.TOP_TEN_USER_FILE_UPLOAD_DATE_STYLE );
			uploadDatePanel.add( uploadDateLabel );
		}
		
		//Initialize the user name label
		userNameAvatar = new UserAvatarWidget( fileDescr.ownerID, fileDescr.ownerLoginName );
		
		mainPanel.add( previewImage );
		mainPanel.add( uploadDatePanel );
		mainPanel.add( userNameAvatar );
		
		//Initialize the composite
		initWidget(mainPanel);
	}
	
	/**
	 * Allows to create an image thumbnail for the given file descriptor
	 * @param fileDescriptors the list of file descriptors
	 * @param index the index of this file descriptor
	 * @return the image
	 */
	private Image constructThumbnailImage( final List<ShortUserFileDescriptor> fileDescriptors, final int index ){
		final ShortUserFileDescriptor fileDescr = fileDescriptors.get( index );
		Image image = new Image( ServerSideAccessManager.getProfileFileURL( fileDescr.ownerID, fileDescr, true ) );
		image.setTitle( titlesI18N.userFileThumbnailManagementTip() );
		image.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Ensure lazy loading
				( new SplitLoad( true ) {
					@Override
					public void execute() {
						ViewTop10ProfileFilesDialogUI dialog = new ViewTop10ProfileFilesDialogUI( parentDialog, fileDescriptors, index );
						dialog.show();
						dialog.center();
					}
				}).loadAndExecute();
			}
		});
		image.setStyleName( CommonResourcesContainer.USER_DIALOG_USER_IMAGE_STYLE );
		return image;
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onAfterComponentIsAdded()
	 */
	@Override
	public void onAfterComponentIsAdded() {
		userNameAvatar.onAfterComponentIsAdded();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onBeforeComponentIsAdded()
	 */
	@Override
	public void onBeforeComponentIsAdded() {
		userNameAvatar.onBeforeComponentIsAdded();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onBeforeComponentIsRemoved()
	 */
	@Override
	public void onBeforeComponentIsRemoved() {
		userNameAvatar.onBeforeComponentIsRemoved();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		userNameAvatar.setEnabled( enabled );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setUserLoggedIn()
	 */
	@Override
	public void setUserLoggedIn() {
		setEnabled( true );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setUserLoggedOut()
	 */
	@Override
	public void setUserLoggedOut() {
		setEnabled( false );
	}

}
