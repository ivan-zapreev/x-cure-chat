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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;

import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;

import com.xcurechat.client.dialogs.profile.ViewUserProfileFilesDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * This widget allows to view user profile images
 */
public class UserProfileFilesView extends Composite {
	//Maximum number of profile files per page
	private static final int MAX_FILES_PER_PAGE = 3;
	
	//The localization for the text
	protected static final UITitlesI18N titlesI18N = I18NManager.getTitles();

	//The main horizontal panel that represents this widget
	private final HorizontalPanel mainHorPanel;
	//The horizontal panel that should store the file thumbnails
	private final HorizontalPanel thumbnailsPanel;
	//The minimum page index
	private final int minimum_page_index = 1;
	//The maximum page index
	private int maximum_page_index = minimum_page_index;
	//Current page index
	private int current_page_index = minimum_page_index;
	//The list of the file descriptors, the index of the map is the file index or null if not set
	private List<ShortFileDescriptor> userFileDescriptors = null;
	//The id of the user for which we browse the files
	private int userID = ShortUserData.UNKNOWN_UID;
	//The login name of the user for which we browse the files
	private String userLoginName = null;
	//The previous and the next buttons
	private final NavigationButtonPanel previousButton, nextButton;
	//The dialog storing this widget or null if none
	private final DialogBox parentDialog;
	
	/**
	 * @author zapreevis
	 * The local extension of the navigation button
	 */
	private class LeftRightButtons extends NavigationButtonPanel {
		public LeftRightButtons(int buttonType, boolean isEnabled, boolean isActive) {
			super( buttonType, isEnabled, isActive, true,
				   "xcure-Chat-Profile-Files-Nav-Button-Panel-Left",
				   "xcure-Chat-Profile-Files-Nav-Button-Panel-Right",
				   null, null );
		}
		@Override
		protected void moveToPage(boolean isNext) {
			moveToPageIndex( current_page_index + (isNext ? 1 : -1));
		}
	}
	
	/**
	 * The basic constructor
	 */
	public UserProfileFilesView( final DialogBox parentDialog, final UserData userData ) {
		//Store the reference to the parent dialog
		this.parentDialog = parentDialog;
		
		//Set the local scroll panel
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.addStyleName( CommonResourcesContainer.FILE_TUMBNAILS_PANEL_STYLE );
		//Set up the images panel
		thumbnailsPanel = new HorizontalPanel();
		thumbnailsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		thumbnailsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		scrollPanel.add( thumbnailsPanel );
		
		//Instantiate the buttons
		previousButton = new LeftRightButtons( CommonResourcesContainer.NAV_LEFT_IMG_BUTTON, false , false );
		nextButton 	   = new LeftRightButtons( CommonResourcesContainer.NAV_RIGHT_IMG_BUTTON, false , false );
		
		//Set up the main panel
		mainHorPanel = new HorizontalPanel();
		mainHorPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );

		mainHorPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainHorPanel.add( previousButton );
		mainHorPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		mainHorPanel.add( scrollPanel );
		mainHorPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainHorPanel.add( nextButton );
		
		//Set the user profile files
		setUserProfile( userData );
		
		//Initialize the widget
		initWidget( mainHorPanel );
	}

	/**
	 * Allows to set the new profile data
	 * @param userProfile the user profile data
	 */
	public void setUserProfile( final UserData userData ) {
		if( ( userData != null ) && ( userData.getFileDescriptors().size() != 0 ) ) {
			this.userID = userData.getUID();
			this.userLoginName = userData.getShortLoginName();
			this.userFileDescriptors = new ArrayList<ShortFileDescriptor>(userData.getFileDescriptors().values());
			//Increase the maximum page index using the 
			maximum_page_index = userFileDescriptors.size() / MAX_FILES_PER_PAGE +
								 ( userFileDescriptors.size() % MAX_FILES_PER_PAGE > 0 ? 1 : 0 );
		} else {
			maximum_page_index = minimum_page_index;
			this.userFileDescriptors = null;
		}
		
		//Move to page index
		moveToPageIndex( current_page_index );
	}
	
	/**
	 * Allows to create an image thumbnail for the given file descriptor
	 * @param fileDescriptors the list of file descriptors
	 * @param index the index of this file descriptor
	 * @return the image
	 */
	private Image constructThumbnailImage( final List<ShortFileDescriptor> fileDescriptors, final int index ){
		final ShortFileDescriptor fileDescr = fileDescriptors.get( index );
		Image image = new Image( ServerSideAccessManager.getProfileFileURL( userID, fileDescr, true ) );
		image.setTitle( titlesI18N.userFileThumbnailManagementTip() );
		image.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//First invert the list to have the desired order of files and the index
				final List<ShortFileDescriptor> invertedFileSequence = new ArrayList<ShortFileDescriptor>();
				final Iterator<ShortFileDescriptor> iter = fileDescriptors.iterator();
				while( iter.hasNext() ) {
					invertedFileSequence.add(0, iter.next());
				}
				final int inverted_index = ( fileDescriptors.size() - 1 ) - index;
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						//Second open the dialog
						ViewUserProfileFilesDialogUI dialog = new ViewUserProfileFilesDialogUI( parentDialog, invertedFileSequence,
																								inverted_index, userLoginName, userID);
						dialog.show();
						dialog.center();
					}
				};
				executor.loadAndExecute();
			}
		});
		image.setStyleName( CommonResourcesContainer.USER_DIALOG_USER_IMAGE_STYLE );
		return image;
	}
	
	/**
	 * Allows to move to the new page index for the given set of profile files
	 * @param newPageIndex the new page index
	 */
	private void moveToPageIndex( final int newPageIndex ) {
		//Turn off the buttons first
		previousButton.setEnabled(false);
		nextButton.setEnabled(false);
		
		//Remove the file thumbnails
		thumbnailsPanel.clear();
		thumbnailsPanel.add( new HTML("&nbsp;") );
		
		if( ( userFileDescriptors != null ) && ! userFileDescriptors.isEmpty() ) {
			//If there are file descriptors present, update the current page
			if( ( newPageIndex < minimum_page_index ) ||
				( newPageIndex > maximum_page_index ) ) {
					//If the new page index is bad then reset it to the minimum
				current_page_index = minimum_page_index;
			} else {
					//If the new page index is good then set it to be the current page index
				current_page_index = newPageIndex; 
			}
			
			//Compute the file indexes which should be in the interval: [0, userFileDescriptors.size() - 1]
			final int max_file_index = userFileDescriptors.size() - 1; 
			final int index_offset = (current_page_index - minimum_page_index) * MAX_FILES_PER_PAGE; 
			int  lastFileIndex = max_file_index - index_offset;
			int firstFileIndex = lastFileIndex - MAX_FILES_PER_PAGE + 1;
			
			//Fill the list of thumbnails in the backwards manner
			for(int index = lastFileIndex ; (index >= firstFileIndex ) && ( index >= 0 ) ; index-- ) {
				thumbnailsPanel.add( constructThumbnailImage( userFileDescriptors, index ) );
				thumbnailsPanel.add( new HTML("&nbsp;") );
			}
			
			//Show the widget if it was hidden
			mainHorPanel.setVisible(true);
		}else {
			//Reset the current page index
			current_page_index = maximum_page_index;
			
			//Hide this widget
			mainHorPanel.setVisible(false);
		}
		
		//Update page indexes
		previousButton.setRemainingPageCount( minimum_page_index, current_page_index, maximum_page_index );
		nextButton.setRemainingPageCount( minimum_page_index, current_page_index, maximum_page_index );
		//Enable/disable the buttons
		previousButton.setAllowed( current_page_index > minimum_page_index );
		nextButton.setAllowed( current_page_index < maximum_page_index );
		//Activate the buttons
		previousButton.setEnabled(true);
		nextButton.setEnabled(true);
	}
}
