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

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.widgets.ActionLinkPanel;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.client.dialogs.ActionGridDialog;

/**
 * @author zapreevis
 * The generic base class for the media files view dialog
 */
public abstract class ViewMediaDialogBase extends ActionGridDialog {
	
	/**
	 * @author zapreevis
	 * This interface has to be implemented by all file widgets that
	 * should allow for their content to be downloaded by the user.
	 */
	protected interface DownloadableFileWidget {
		public String getFileURL();
	}
	
	/**
	 * @author zapreevis
	 * This is a marker interface that has to be implemented by all widgets returned by the 
	 * children dialogs for which we want to enable image rotation.
	 */
	protected interface RotateableImageWidget extends DownloadableFileWidget {}
	
	//Stores the number of files we are going to view using this dialog
	private final int numberOfFiles;
	
	//The index of the shown image
	private int currentFileIndex = 0;
	
	//The list of widgets, we store them all loaded to avoid unneeded server requests
	private final HashMap<Integer, Widget> fileWidgetList = new HashMap<Integer, Widget>();
	
	//The panel storing the image
	protected final SimplePanel widgetPanel = new SimplePanel();
	
	//The simple panel storing the image control link
	private SimplePanel imageRotationActionLinkHolder = new SimplePanel();
	//The image rotation action link panel
	private ActionLinkPanel imageRotationActionLink = new ActionLinkPanel( ServerSideAccessManager.getRotateImageIconURL(true),
																		   titlesI18N.imageRotateToolTip(),
																		   ServerSideAccessManager.getRotateImageIconURL(false),
																		   titlesI18N.imageRotateToolTip(),
																		   titlesI18N.imageRotateTitle(),
																		   new ClickHandler() {
																			@Override
																			public void onClick( ClickEvent event) {
																				rotateImageRight( currentFileIndex );
																			}
																		   },
																		   true, true );
	//The simple panel storing the loading progress bar
	private SimplePanel imageLoadingBarUIHolder = new SimplePanel();
	//The loading progress bar
	protected final ServerCommStatusPanel imageLoadingBarUI = new ServerCommStatusPanel();
	
	//The simple panel storing the file download link
	private SimplePanel downloadLinkHolder = new SimplePanel();

	//The navigation buttons
	private Button previousButton = null;
	private Button nextButton = null;

	/**
	 * The generic constructor, for the case of viewing one file
	 * @param parentDialog the parent dialog
	 */
	public ViewMediaDialogBase( final DialogBox parentDialog ) {
		this( parentDialog, 1, 0 );
	}
	
	/**
	 * The generic constructor
	 * 
	 * WARNING: Any subclass using this constructor must not have smiley selection target!
	 * 
	 * @param parentDialog the parent dialog
	 * @param numberOfFiles the number of files that we are going to view
	 * @param initialFileIndex the index of the initial file to view
	 */
	public ViewMediaDialogBase( final DialogBox parentDialog, final int numberOfFiles, final int initialFileIndex ) {
		this( false, true, true, parentDialog, numberOfFiles, initialFileIndex );
	}
	
	/**
	 * The generic constructor
	 * @param hasSmileyTarget must be true if the dialog contains smiley selection targets, instances. 
	 * @param autoHide true for auto hide
	 * @param modal true for a modal dialog
	 * @param parentDialog the parent dialog
	 * @param numberOfFiles the number of files that we are going to view
	 * @param initialFileIndex the index of the initial file to view
	 */
	public ViewMediaDialogBase( final boolean hasSmileyTarget, final boolean autoHide,
			 					final boolean modal, final DialogBox parentDialog,
			 					final int numberOfFiles, final int initialFileIndex ) {
		super( hasSmileyTarget, autoHide, modal, parentDialog);
		
		//Store the parameters
		this.numberOfFiles = numberOfFiles;
		this.currentFileIndex = initialFileIndex;
		
		//Enable dialog's animation
		this.setAnimationEnabled(true);
		
		//Enable the action buttons and hot key, even though we will not be adding these buttons
		//to the grid, we will use the hot keys associated with them to close this dialog
		setLeftEnabled( true );
		setRightEnabled( true );
	}
	
	/**
	 * Set the current file widget to the widget panel, if the widget is
	 * not created yet, then we create a new one.
	 */
	private void setCurrentWidgetToThePanel() {
		if( numberOfFiles > 0 ) {
			if( currentFileIndex < 0 ){
				currentFileIndex = numberOfFiles - 1;
			} else {
				if( currentFileIndex >= numberOfFiles ){
					currentFileIndex = 0;
				}
			}
			
			Widget fileWidget = fileWidgetList.get( currentFileIndex ); 
			if( fileWidget == null ){
				fileWidget = getMediaFileWidget( currentFileIndex );
				fileWidget.addStyleName( CommonResourcesContainer.MEDIA_FILE_WIDGET_BORDER_STYLE );
				fileWidgetList.put( currentFileIndex, fileWidget );
			}
			
			//Manage the image file widget rotation controls
			if( fileWidget instanceof RotateableImageWidget ) {
				//Allow image-rotation action link to be shown
				setImageControlsVisible( true );
			} else {
				//Hide the image-rotation action link to be shown
				setImageControlsVisible( false );
			}
			
			//Manage the file downloading link
			if( fileWidget instanceof DownloadableFileWidget ) {
				setWidgetDownloadLinkWidget( (DownloadableFileWidget) fileWidget );
			}
			
			//Set the widget to the panel
			widgetPanel.setWidget( fileWidget );
			
			//Update the dialog title
			setText( getDialogTitle( currentFileIndex ) );
			
			//Re-center the dialog
			this.center();
		} else {
			Window.alert("The provided list of files is empty!");
		}
	}
	
	/**
	 * Allows to set up the download file link widget
	 * @param fileWidget the widget that represents a downloadable file
	 */
	private void setWidgetDownloadLinkWidget( DownloadableFileWidget fileWidget ) {
		downloadLinkHolder.clear();
		downloadLinkHolder.add( InterfaceUtils.getDownloadLinkWidget( fileWidget.getFileURL(), true ) );
	}
	
	/**
	 * Allows to get the file widget for the given index, if the file
	 * widget with the given index was already shown 
	 * @param fileWidgetIndex the file widget index
	 * @return the file widget or null if the file widget with the given index was not yet shown 
	 */
	protected Widget getFileWidget( final int fileWidgetIndex ) {
		return fileWidgetList.get( fileWidgetIndex );
	}
	
	/**
	 * Fills the main grid data
	 */
	protected void populateDialog(){
		final int MAX_NUMBER_OF_ROWS = 2;
		final int MAX_NUMBER_OF_COLUMNS = 1;
		final boolean isWithNavigationButtons = ( numberOfFiles > 1 );
		
		addNewGrid( MAX_NUMBER_OF_ROWS, MAX_NUMBER_OF_COLUMNS, false, "", false);

		//Add the widget panel to the dialog
		this.addToGrid(this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, MAX_NUMBER_OF_COLUMNS, widgetPanel, false, false);

		//Set the current file widget to the panel 
		setCurrentWidgetToThePanel();
		
		//Add controls centering panel
		final HorizontalPanel controlsCenteringPanel = new HorizontalPanel();
		controlsCenteringPanel.setWidth("100%");
		controlsCenteringPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		this.addToGrid( FIRST_COLUMN_INDEX, controlsCenteringPanel, true, false );
		
		//Add the controls panel
		final HorizontalPanel controlsPanel = new HorizontalPanel();
		controlsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		controlsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		controlsCenteringPanel.add( controlsPanel );
		
		//Create navigation button "Close", and put it to be the first button
		final Button closeButton = new Button();
		closeButton.setText( titlesI18N.closeButtonTitle() );
		closeButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		closeButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Close the dialog
				hide();
			}
		} );
		controlsPanel.add( closeButton );
		
		//Add a delimiter
		controlsPanel.add( new HTML("&nbsp;&nbsp;") );
		
		//Add an invisible image loading progress bar
		controlsPanel.add( imageLoadingBarUIHolder );
		
		//Decide whether or not to add the Previous/Next buttons
		if( isWithNavigationButtons ) {
			//Add a delimiter
			controlsPanel.add( new HTML("&nbsp;&nbsp;") );
			
			//Add navigation button "Previous"
			previousButton = new Button();
			previousButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
			previousButton.setText( titlesI18N.userProfileImageShowPreviousButton() );
			previousButton.addClickHandler( new ClickHandler() {
				public void onClick(ClickEvent e) {
					currentFileIndex--;
					//Update the image and the title
					setCurrentWidgetToThePanel();
				}
			} );
			controlsPanel.add( previousButton );
			
			//Add a delimiter
			controlsPanel.add( new HTML("&nbsp;") );

			//Add navigation button "Next"
			nextButton = new Button();
			nextButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
			nextButton.setText( titlesI18N.userProfileImageShowNextButton() );
			nextButton.addClickHandler( new ClickHandler() {
				public void onClick(ClickEvent e) {
					currentFileIndex++;
					//Update the image and the title
					setCurrentWidgetToThePanel();
				}
			} );
			controlsPanel.add( nextButton );
		}
		
		//Add a delimiter
		controlsPanel.add( new HTML("&nbsp;&nbsp;&nbsp;") );
		
		//Add the image download link in order to enable file's download 
		controlsPanel.add( downloadLinkHolder );
		
		//Add a delimiter
		controlsPanel.add( new HTML("&nbsp;&nbsp;&nbsp;") );
		
		//Add the invisible action button for rotating the files, should be visible by default
		controlsPanel.add( imageRotationActionLinkHolder );
	} 
	
	/**
	 * Allows to set the image rotation action link and the image loading progress
	 * bar as visible. These are needed for image rotation. 
	 * @param visible true to set it visible false to hide it
	 */
	private void setImageControlsVisible( final boolean visible ) {
		if( visible ) {
			if( imageRotationActionLinkHolder.getWidget() == null ) {
				imageRotationActionLinkHolder.add( imageRotationActionLink );
			}
			if( imageLoadingBarUIHolder.getWidget() == null ) {
				imageLoadingBarUIHolder.add( imageLoadingBarUI );
			}
		} else {
			imageRotationActionLinkHolder.clear();
			imageLoadingBarUIHolder.clear();
		}
	}
	
	/**
	 * Allows to set the navigation buttons and the image rotation action link into enabled/disable mode
	 * @param enabled true for enabled, otherwise false
	 */
	protected void setEnabled( final boolean enabled ) {
		if( previousButton != null ) {
			previousButton.setEnabled( enabled );
		}
		if( nextButton != null ) {
			nextButton.setEnabled( enabled );
		}
		imageRotationActionLink.setEnabled( enabled );
	}
	
	/**
	 * Allows to indicate whether the image file is being loaded
	 * @param isLoading true is loading false is not
	 */
	protected void markImageLoading( final boolean isLoading ) {
		if( isLoading ) {
			imageLoadingBarUI.startProgressBar();
		} else {
			imageLoadingBarUI.stopProgressBar( false );
		}
	}
	
	/**
	 * The left button's action
	 */
	protected void actionLeftButton() {
		hide();
	}

	/**
	 * The right button's action
	 */
	protected void actionRightButton() {
		hide();
	}
	
	/**
	 * Allows to get the dialog title, this method is called every to update
	 * the dialog title each time we move to the next/previous file.
	 * @param index is the index of the file we are currently viewing
	 */
	public abstract String getDialogTitle( final int index );
	
	/**
	 * Allows to retrieve the UI widget corresponding to the given media file.
	 * @param index is the index of the file we are currently viewing
	 */
	protected abstract Widget getMediaFileWidget( final int index );
	
	/**
	 * In case the displayed file is an image, then this method will be
	 * called in order to rotate the image by 90 degrees to the right 
	 * @param index the index of the image file to be rotated 90 degrees to the right
	 */
	protected abstract void rotateImageRight( final int index );
}
