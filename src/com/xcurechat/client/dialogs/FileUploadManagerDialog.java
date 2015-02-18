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
package com.xcurechat.client.dialogs;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.dialogs.ActionGridDialog;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.rpc.exceptions.ExceptionsSerializer;
import com.xcurechat.client.rpc.exceptions.UserFileUploadException;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.SupportedFileMimeTypes;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * The base dialog class for dialogs that allow to upload/delete files to/on the server
 */
public abstract class FileUploadManagerDialog extends ActionGridDialog {
	//Contains true if the file upload dialog should be closed after the file was uploaded
	private final boolean closeOnCompleteFileUpload;
	
	//The vertical panel that will store the form's fields
	private VerticalPanel verticalPanel = new VerticalPanel();

	//The form panel for the upload dialog
    private final FormPanel form = new FormPanel();
    
    //The file uploader widget
    private final FileUpload fileUpload = new FileUpload(); 
    
    //if true then we also add a file preview with the deletion buttons and the file 
	//preview/delete and upload parts are placed into the separate disclosure panels
    private final boolean withFilePreview;
    
    //The image widget that shows the uploaded file thumbnail
    private Image fileThumbnail = null;
    
    //The delete button for the file
	private Button deleteButton = new Button( );
	
	//The disclosure panel with the file preview and delete button
	private DisclosurePanel disclPanel = null;
	
	/**
	 * Enable/Disable dialog buttons
	 */
	private void setEnabledButtons( final boolean enabledDelete, final boolean enableCancel, final boolean enableUpload ){
		if( withFilePreview ) {
			if( enabledDelete ) {
				fileThumbnail.setStyleName( CommonResourcesContainer.FILE_UPLOAD_DIALOG_THUMB_IMAGE_STYLE );
				fileThumbnail.addStyleName( CommonResourcesContainer.ZOOME_IN_IMAGE_STYLE );
			} else {
				fileThumbnail.setStyleName( CommonResourcesContainer.FILE_UPLOAD_DIALOG_THUMB_IMAGE_DIS_STYLE );
			}
			deleteButton.setEnabled(enabledDelete);
		}
		setLeftEnabled( enableCancel || true ); //Do not disable the close button
		setRightEnabled( enableUpload );
	}

	/**
	 * A simple constructor for the file upload management dialog
	 * 
	 * WARNIGN: Any subclass of this class must not have smiley selection targets in it!
	 * 
	 * @param parentDialog the parent dialog from which this dialog is opened
	 * @param withFilePreview if true then we also add a file preview with the deletion buttons
	 * and the file preview/delete and upload parts are placed into the separate disclosure panels 
	 */
	public FileUploadManagerDialog( final DialogBox parentDialog, final boolean withFilePreview ){
		this( parentDialog, withFilePreview, true );
	}

	/**
	 * A simple constructor for the file upload management dialog
	 * 
	 * WARNIGN: Any subclass of this class must not have smiley selection targets in it!
	 * 
	 * @param parentDialog the parent dialog from which this dialog is opened
	 * @param withFilePreview if true then we also add a file preview with the deletion buttons
	 * and the file preview/delete and upload parts are placed into the separate disclosure panels 
	 * @param closeOnCompleteFileUpload true if the dialog should be closed after the file was successfully uploaded, otherwise false.
	 */
	public FileUploadManagerDialog( final DialogBox parentDialog, final boolean withFilePreview, final boolean closeOnCompleteFileUpload ){
		super( false, false, true, parentDialog );
		
		this.withFilePreview = withFilePreview;
		this.closeOnCompleteFileUpload = closeOnCompleteFileUpload;
	}
	
	/**
	 * Allows to add the hidden file upload form parameters, which will be send to the server.
	 * Create a TextBox, giving it a name so that it will be submitted. From descendant dialogs,
	 * has to be called only after the dialog is populated, i.e. the populateDialog() is executed.
	 * @param fieldName the name of the parameter to add
	 * @param fieldValue the value of the parameter to add
	 */
	protected void addHiddenUploadFormParameter( final String fieldName, final String fieldValue){
		final TextBox field = new TextBox();
		field.setVisible(false);
		field.setName( fieldName );
		field.setText( fieldValue );
		verticalPanel.add( field );
	}
	
	private void addFileUploadElement( final boolean withDisclosurePanel ){
		//True if there are particular files specified to be valid for uploading
		final boolean hasParticularSupportedFiles = getAllowedFileExtensions() != null;
		
		//ADD THE GRID FOR IMAGE UPLOAD
		if( withDisclosurePanel ) {
			addNewGrid( hasParticularSupportedFiles ? 3 : 2, true, titlesI18N.uploadFilePanelTitle(), isUploadFileViewOpen() );
		} else {
			addNewGrid( hasParticularSupportedFiles ? 3 : 2, false, "", true );
		}
		
		if( hasParticularSupportedFiles ) {
			//Add the supported files label
			HorizontalPanel infoPanel = new HorizontalPanel();
			Label supportedFilesTitleLabel = new Label( titlesI18N.supportedFileTypesFieldLabel() );
			supportedFilesTitleLabel.setStyleName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
			
			String extensions = ""; boolean isFirst = true;
			for( String ext : getAllowedFileExtensions() ) {
				if( isFirst ) {
					extensions += ext;
					isFirst = false;
				} else {
					extensions += ", " + ext;
				}
			}
			Label supportedFilesValuesLabel = new Label( extensions );
			supportedFilesValuesLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME );
			infoPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
			infoPanel.add( supportedFilesTitleLabel );
			infoPanel.add( new HTML("&nbsp;") );
			infoPanel.add( supportedFilesValuesLabel );
			addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, infoPanel, false, false );
		}
		
		//Initialize the form
		form.setSize( "100%", "100%" );
		form.setAction( getUploadFileServletURL() );
		form.setEncoding( FormPanel.ENCODING_MULTIPART );
		form.setMethod( FormPanel.METHOD_POST );
		// Add an event handler to the form.
		form.addSubmitHandler( new SubmitHandler(){
			public void onSubmit( SubmitEvent event ) {
				//This event is fired just before the form is submitted. We can take
				//this opportunity to perform validation.
				String fileName = fileUpload.getFilename();
				if( ( fileName != null ) && ( !fileName.trim().isEmpty() ) ) {
					if( isAllowedFileExtension( fileName ) ) {
						//Disable the controls
						setEnabledButtons(false, false, false);
						//Set the button title for being "Cancel"
						setLeftActionButtonText( titlesI18N.cancelButton() );
						//Start the progress bar
						progressBarUI.startProgressBar();
					} else {
						//The file extension is a non-allowed one, let the user know which files he can upload
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Report the error
								ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().fileUploadSupportedFiles( getAllowedFileExtensions() ) );
							}
						}).loadAndExecute();
						//Disable the form submission
						event.cancel();
					}
				} else {
					(new SplitLoad( true ) {
						@Override
						public void execute() {
							ErrorMessagesDialogUI.openErrorDialog( new UserFileUploadException( UserFileUploadException.FILE_IS_NOT_SELECTED_ERR ) );
						}
					}).loadAndExecute();
					//Disable the form submission
					event.cancel();
				}
			}
		});
		form.addSubmitCompleteHandler( new SubmitCompleteHandler() {
			public void onSubmitComplete( SubmitCompleteEvent event ) {
				// When the form submission is successfully completed, this event is
				// fired. Assuming the service returned a response of type text/html,
				// we can get the result text here (see the FormPanel documentation for
				// further explanation).
				final String response = ( event.getResults() != null ) ? event.getResults().trim() : "";
				//Check if the file upload failed
				if( isFileUploadFailed( response ) ) {
					//Stop the progress bar on error
					progressBarUI.stopProgressBar(true);
					(new SplitLoad( true ) {
						@Override
						public void execute() {
							//Report the error
							ErrorMessagesDialogUI.openErrorDialog( ExceptionsSerializer.restoreExceptionFromString( response ) );
						}
					}).loadAndExecute();
					//Set the button title for being "Close"
					setLeftActionButtonText( titlesI18N.closeButtonTitle() );
				} else {
					//Stop the progress bar on success
					progressBarUI.stopProgressBar(false);
					//Perform an action on the successful file upload
					onSuccessfulFileUpload( getShortFileDescriptor( fileUpload.getFilename(), response ) );
					if( closeOnCompleteFileUpload ) {
						//Hide the current dialog
						hide();
					} else {
						//Just update the image
						updateFileThumbnailImage();
					}
					//Set the button title for being "Attach"
					setLeftActionButtonText( titlesI18N.saveButton() );
				}
				//Enable the controls
				setEnabledButtons(true, true, false);
			}
	    });
		
		//Set the vertical panel to be the form's widget
		verticalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		verticalPanel.setSize( "100%", "100%" );
		form.setWidget(verticalPanel);
		
		//Add the file uplosd to the vertical panel
		fileUpload.setName( ServerSideAccessManager.FILE_UPLOAD_SERVLET_PARAM );
		verticalPanel.add( fileUpload );
		
		//Add some invisible form elements, such as user login name and session id
		// Create a TextBox, giving it a name so that it will be submitted.
		final TextBox loginTB = new TextBox();
		loginTB.setVisible(false);
		loginTB.setName( ServerSideAccessManager.USER_ID_SERVLET_PARAM );
		loginTB.setText( SiteManager.getUserID() + "" );
		verticalPanel.add(loginTB);
		
		final TextBox sessionIdTB = new TextBox();
		sessionIdTB.setVisible(false);
		sessionIdTB.setName( ServerSideAccessManager.SESSION_ID_SERVLET_PARAM );
		sessionIdTB.setText( SiteManager.getUserSessionId() );
		verticalPanel.add(sessionIdTB);
		
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, form, true, false );
		addGridActionElements( true, true, true, true );
	}
	
	/**
	 * Allows to update the local file-thumbnail image, should be
	 * called after a successful file upload or deletion
	 */
	protected void updateFileThumbnailImage() {
		if( fileThumbnail != null ) {
			fileThumbnail.setUrl( getUploadedFileThumbnailURL( true ) );
		}
	}
	
	private void addFileViewAndDeleteSection() {
		//ADD DELETE IMAGE DISCLOSURE PANEL
		disclPanel = addNewGrid( 1, true, titlesI18N.deleteFilePanelTitle(), isDeleteFileViewOpen() );
		
		//Set the url of the image
		fileThumbnail = new Image();
		fileThumbnail.setUrl( getUploadedFileThumbnailURL( false ) );
		fileThumbnail.setStyleName( CommonResourcesContainer.FILE_UPLOAD_DIALOG_THUMB_IMAGE_STYLE );
		fileThumbnail.addStyleName( CommonResourcesContainer.ZOOME_IN_IMAGE_STYLE );
		fileThumbnail.setTitle( titlesI18N.userFileThumbnailManagementTip() );
		fileThumbnail.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Open the image viewing dialog
				 openFileViewDialog();
			}
		});
		addToGrid( FIRST_COLUMN_INDEX, fileThumbnail, false, false );
		
		deleteButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		deleteButton.setText( titlesI18N.deleteButton() );
		deleteButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				(new SplitLoad( true ) {
					@Override
					public void execute() {
						CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
							public void onSuccessAct(Void result) {
								//Do what is needed after the file was deleted
								afterFileDeleteComplete();
								//Just update the image
								updateFileThumbnailImage();
								//Set the button title for being "Close"
								setLeftActionButtonText( titlesI18N.closeButtonTitle() );
								//Enable the buttons
								setEnabledButtons(true, false, true);
							}
							public void onFailureAct(final Throwable caught) {
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Report the error
										ErrorMessagesDialogUI.openErrorDialog( caught );
									}
								}).loadAndExecute();
								//Perform the recovery action
								recover();
							}
						};
						//Perform the image deletion RPC call
						if( ! performFileDeleteRequest( callback ) ) {
							//If the server communication was not performed then stop the progress bar
							progressBarUI.stopProgressBar(false);
						}
					}
					@Override
					public void recover() {
						//Enable the buttons
						setEnabledButtons(true, true, false);
					}
				}).loadAndExecute();
			}
		});
		addToGrid( SECOND_COLUMN_INDEX, deleteButton, false, false );
	}
	
	@Override
	protected void populateDialog() {
		//Set the dialog's title
		this.setText( getDialogTitle() );
		
		//Add the file deletion element
		if( withFilePreview ) {
			addFileViewAndDeleteSection();
		}
		
		//Add the file upload element
		addFileUploadElement( withFilePreview );
	}
		
	/**
	 * Allows to detect whether the file name has an allowed extension.
	 * @param fileName the name of the file that we want to upload
	 * @return true if the file has a correct extension
	 */
	private boolean isAllowedFileExtension( final String fileName ) {
		List<String> allowedFileExtensions = getAllowedFileExtensions();
		if( allowedFileExtensions != null ) {
			final String fileExt = SupportedFileMimeTypes.getFileExtension( fileName );
			if( fileExt != null ) {
				//Check if the given file's extension is in the list of allowed ones
				return allowedFileExtensions.contains( fileExt );
			} else {
				//The file with no file extension is not allowed
				return false;
			}
		} else {
			//If the allowed file extensions are not set then we can upload anything
			return true;
		}
	}
	
	/**
	 * Is called when the file is successfully uploaded to the server, by default it assumes that
	 * the response contains only the DB id of the file, that was just uploaded to the server 
	 * @param fileName the uploaded file name
	 * @param response the server response
	 */
	protected ShortFileDescriptor getShortFileDescriptor( final String fileName, final String response ) {
		final ShortFileDescriptor fileDesc = new ShortFileDescriptor();
		fileDesc.fileID = Integer.parseInt( response );
		fileDesc.fileName = fileName;
		fileDesc.mimeType = SupportedFileMimeTypes.getFileMimeTypeStringByExtension( fileName );
		//NOTE: The default file (image) width and height are set to zero
		
		return fileDesc;
	}
	
	/**
	 * Should be implemented for distinguishing between the successful and failed file upload
	 * By default if the response contains an integer value then it is a successful file upload
	 * with a file id stored in the response.
	 * @param response the server response
	 * @return true if the file upload was successful, otherwise false
	 */
	protected boolean isFileUploadFailed( String response ) {
		//Try to check if the just got an integer response, if yes then this is the uploaded file ID
		try {
			Integer.parseInt( response );
			return false;
		} catch ( NumberFormatException e ) {
			return true;
		}
	}
	
	/**
	 * @return true if the file view and delete disclosure panel
	 * has to be open when the dialog is first displayed, otherwise false.
	 */
	protected boolean isDeleteFileViewOpen() {
		return true;
	}
	
	/**
	 * @return true if the file upload disclosure panel
	 * has to be open when the dialog is first displayed, otherwise false.
	 */
	protected boolean isUploadFileViewOpen() {
		return true;
	}
	
	@Override
	protected void actionLeftButton() {
		hide();
	}
	
	@Override
	protected void actionRightButton() {
		form.submit();
	}
	
	@Override
	protected String getLeftButtonText(){
		return titlesI18N.closeButtonTitle();
	}
	
	@Override
	protected String getRightButtonText(){
		return titlesI18N.uploadButton();
	}
	
	/**
	 * Should open the image view dialog for the uploaded image
	 * NOTE: Does not thing, should be overridden by the sub-class if needed
	 */
	protected void openFileViewDialog() {
	}
	
	/**
	 * Constructed and returns the url from which the image can be retrieved
	 * NOTE: Does not thing, should be overridden by the sub-class if needed
	 * @param update if true then the image has to be updated
	 * @return the required url
	 */
	protected String getUploadedFileThumbnailURL(final boolean update) {
		return "";
	}
	
	/**
	 * After the previously uploaded file gets successfully deleted from the server
	 * NOTE: Does not thing, should be overridden by the sub-class if needed
	 */
	protected void afterFileDeleteComplete() {
	}
	
	/**
	 * The method that is called to perform the request to the server to delete the
	 * previously uploaded file. The method might not do the server call if the
	 * file is not set.
	 * NOTE: Does not thing, should be overridden by the sub-class if needed
	 * @param callback the call back object for the file deletion request
	 * @return true if the server communication was initiated, false otherwise.
	 */
	protected boolean performFileDeleteRequest( final AsyncCallback<Void> callback ) {
		return false;
	}
	
	/**
	 * Allows to open and close the file preview and delete disclosure panel if it is present
	 * @param open
	 */
	protected void openFilePreviewPanel( final boolean open ) {
		if( disclPanel != null ) {
			disclPanel.setOpen( open );
		}
	}
	
	/**
	 * @return an array of strings that are the extensions of the
	 *         files that we are allowed to upload, in case the
     *         method returns null, all files are considered to be allowed
	 */
	protected abstract List<String> getAllowedFileExtensions();
	
	/**
	 * @return the title of the dialog
	 */
	protected abstract String getDialogTitle();
	
	/**
	 * @return the url of the servlet to which the file will be posted
	 */
	protected abstract String getUploadFileServletURL();
	
	/**
	 * Is called when the file is successfully uploaded to the server
	 * @param fileDesc the short file descriptor of the file uploaded to the server
	 */
	protected abstract void onSuccessfulFileUpload( final ShortFileDescriptor fileDesc );
}

