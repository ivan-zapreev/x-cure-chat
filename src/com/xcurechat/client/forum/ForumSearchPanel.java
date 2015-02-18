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
 * The forum interface package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.forum;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.search.ForumSearchData;

import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.TextBaseTranslitAndProgressBar;
import com.xcurechat.client.utils.widgets.TextBoxWithSuggText;

/**
 * @author zapreevis
 * Represents the forum panel that contains the search bar etc
 */
class ForumSearchPanel extends ForumBodyComponent {
	//The maximum length of the search text box in characters 
	private static final int SEARCH_TEXT_BOX_LENGTH_CHARACTERS = 30;
	
	//The simple wrapper panel
	private final SimplePanel wrapperPanel = new SimplePanel();
	//The main vertical panel
	private HorizontalPanel mainSearchPanel = new HorizontalPanel();
	//The search text box
	private TextBoxWithSuggText searchTextBox = new TextBoxWithSuggText( i18NTitles.searchAnyTextHelperMessage() );
	//The search button
	private final Button searchButton = new Button();
	//The refresh button
	private final Button refreshButton = new Button();
	//Search only in topic descriptions
	private final CheckBox onlyInTopicsCheckBox = new CheckBox();
	//Search only in this topic
	private final CheckBox onlyInThisTopicCheckBox = new CheckBox();
	//The messages posted by the given author
	private final Label authorValue = new Label( );
	//The id of the author who's messages we will search
	private int authorID = ShortUserData.UNKNOWN_UID;
	//The id of the author who's messages we will search
	private String authorLoginName = "";
	//The image button for removing the forum author
	private final Image removeRecepientImage = new Image( );
	//The panel storing the author selection components
	private final HorizontalPanel authorSearchComponents = new HorizontalPanel();
	//This is the ID of the base message, i.e. a message that defines the topic we are browsing
	private int baseMessageID = ShortForumMessageData.UNKNOWN_MESSAGE_ID;
	
	//The loading progress bar
	private final ServerCommStatusPanel progressBarUI = new ServerCommStatusPanel();
	
	//True if the component is enabled, otherwise false
	private boolean enabled = true;
	
	//True if the user is logged in, otherwise false
	private boolean isLoggedIn = false;
	
	/**********************************************************************************************************/
	/******SPECIFIC FOR THE ACTION BUTTON THAT ALLOWS TO CREATE NEW FORUM MESSAGES*****************************/
	/**********************************************************************************************************/
	//The "New Topic" button
	private final Button sendMessageButton = new Button();
	//The hint image URL
	private String hintImageURL;
	//The hint image URL
	private final String disabledHintImageURL = ServerSideAccessManager.getForumDisabledActionHintImageURL();
	
	//True if this action is allowed for the given logged in user
	private boolean isActionAllowed = false;
	
	//The pre-created instance of the send-forum-message dialog
	//WARNING: We should not instantiate the popup here because
	//then it can not register in the pop-up messages stack.
	private SendForumMessageDialogUI sendForumMessageDialog = null;
	
	//Stores the message data for the message we can reply to, or null
	private ForumMessageData messageData = null;
	
	//Stores the list of arrow images, for an easy access
	private List<Image> arrowImages = new ArrayList<Image>();
	
	/**********************************************************************************************************/
	
	/**
	 * This constructor has to be used for all the body components
	 * @param addDecorations if true then we use the decorated panel with the rounded corners around this widget
	 */
	public ForumSearchPanel( final boolean addDecorations ) {
		//Initialize the super class
		super( addDecorations );
		
		//Initialize the remove author button
		removeRecepientImage.setStyleName( CommonResourcesContainer.REMOVE_AUTHOR_BUTTON_IMAGE_STYLE );
		removeRecepientImage.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( enabled ) {
					removeForumAuthor( false );
				}
			}
		});
		
		//Populate the title panel
		populate();
	}
	
	private void populate() {
		if(BrowserDetect.getBrowserDetect().isOpera()) {
			//Otherwise opera makes the panel as wide as the page 
			mainSearchPanel.setWidth("0%");
		}
		mainSearchPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		mainSearchPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		
		/****ADD ELEMENTS RESPONSIBLE FOR POSTING NEW MESSAGES********/
		populateNewMsgElements();
		
		/**Add the delimiter**/
		addPanelDelimiter();
		
		/*****************ADD MAIN SEARCH ELEMENTS********************/
		searchTextBox.addStyleName( CommonResourcesContainer.SEARCH_QUERY_TEXT_BOX_STYLE_NAME );
		searchTextBox.setVisibleLength( SEARCH_TEXT_BOX_LENGTH_CHARACTERS );
		searchTextBox.addKeyDownHandler( new KeyDownHandler(){
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if( enabled && event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					//Activate search on pressing enter in the search string text box
					doNewSearch();
					
					//Prevent the event from being propagated and from its default action
					event.preventDefault();
					event.stopPropagation();
				}
			}
		});
		addPanelElement( new TextBaseTranslitAndProgressBar( searchTextBox, ForumSearchData.MAX_SEARCH_STRING_LENGTH ) );
		
		/*****************ADD ADDITIONAL SEARCH ELEMENTS********************/
		mainSearchPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		
		//Add only search in topics selector
		setCheckBoxEnabled( onlyInTopicsCheckBox, true );
		//Originally we search for the topics only
		onlyInTopicsCheckBox.setValue( true );
		onlyInTopicsCheckBox.setText( i18NTitles.onlyInTopicsCheckBox() );
		onlyInTopicsCheckBox.setTitle( i18NTitles.onlyInTopicsCheckBoxToolTip() );
		onlyInTopicsCheckBox.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e) {
				if( onlyInTopicsCheckBox.isEnabled() ) {
					if( onlyInTopicsCheckBox.getValue() ) {
						//If is checked
						onlyInThisTopicCheckBox.setValue( false );
						setCheckBoxEnabled( onlyInThisTopicCheckBox, false );
					} else {
						//If is unchecked
						if(  isCurrentTopicSet() ) {
							//If the current topic is set
							setCheckBoxEnabled( onlyInThisTopicCheckBox, true );
						}
					}
				}
			}
		});
		addPanelElement( onlyInTopicsCheckBox );
		
		//Add only search in this topic selector
		setCheckBoxEnabled( onlyInThisTopicCheckBox, false ); //Is disabled in the beginning since the current topic is not set
		onlyInThisTopicCheckBox.setText( i18NTitles.onlyInThisTopicCheckBox() );
		onlyInThisTopicCheckBox.setTitle( i18NTitles.onlyInThisTopicCheckBoxToolTip() );
		onlyInThisTopicCheckBox.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e) {
				if( onlyInThisTopicCheckBox.isEnabled() ) {
					if( onlyInThisTopicCheckBox.getValue() ) {
						//If is checked
						onlyInTopicsCheckBox.setValue( false );
						setCheckBoxEnabled( onlyInTopicsCheckBox, false );
					} else {
						//If is unchecked
						setCheckBoxEnabled( onlyInTopicsCheckBox, true );
					}
				}
			}
		});
		addPanelElement( onlyInThisTopicCheckBox );
		
		//Add the author selector
		Label authorTitle = new Label( i18NTitles.postedByAuthor() + ":" );
		authorTitle.setTitle( i18NTitles.postedByAuthorToolTip() );
		authorTitle.setStyleName( CommonResourcesContainer.REGULAR_FIELD_STYLE);
		authorSearchComponents.setHeight("100%");
		authorSearchComponents.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE  );
		authorSearchComponents.add( authorTitle );
		authorSearchComponents.add( new HTML("&nbsp;") );
		authorValue.setStyleName( CommonResourcesContainer.LINK_DISABLED_STYLE );
		authorValue.setWordWrap( false );
		authorValue.setText( i18NTitles.postedByAnyoneValue() );
		authorValue.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//If the component is not disables and we are logged in then
				if( enabled && isLoggedIn ) {
					if( authorID == ShortUserData.UNKNOWN_UID ) {
						//Ensure lazy loading
						final SplitLoad executor = new SplitLoad( true ) {
							@Override
							public void execute() {
								//Open the user selection dialog
								SelectAuthorDialogUI dialog = new SelectAuthorDialogUI( ForumSearchPanel.this );
								dialog.show();
								dialog.center();
							}
						};
						executor.loadAndExecute();
					} else {
						//Ensure lazy loading
						final SplitLoad executor = new SplitLoad( true ) {
							@Override
							public void execute() {
								//View user profile if the author is selected
								ViewUserProfileDialogUI userProfile = new ViewUserProfileDialogUI( authorID, authorValue.getText(), null, true);
								userProfile.show();
								userProfile.center();
							}
						};
						executor.loadAndExecute();
					}
				}
			}
		});
		authorSearchComponents.add( authorValue );
		addPanelElement( authorSearchComponents );
		
		/**Add the delimiter**/
		addPanelDelimiter();
		
		/*****************ADD SEARCH, REFRESH AND PROGRES ELEMENTS********************/
		
		//Adding the refresh button
		refreshButton.setStyleName( CommonResourcesContainer.ACTION_BUTTON_STYLE );
		refreshButton.setText( i18NTitles.refreshForumMessagesButtonTitle() );
		refreshButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent event) {
				ForumSearchManager.doSearch();
			}
		} );
		addPanelElement( refreshButton  );
		
		//Adding the progress bar
		addPanelElement( progressBarUI );

		//Adding the search button
		searchButton.setText( i18NTitles.searchButtonText() );
		searchButton.setStyleName( CommonResourcesContainer.ACTION_BUTTON_STYLE );
		searchButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Activate search 
				doNewSearch();
			}
		});
		addPanelElement( searchButton  );
		
		wrapperPanel.add( mainSearchPanel );
		setComponentWidget( wrapperPanel );
	}
	
	private void populateNewMsgElements() {
		//Add the first arrow
		Image arrowImage = new Image( ServerSideAccessManager.getForumNewTopicImageURL() );
		arrowImage.setStyleName( CommonResourcesContainer.CREATE_NEW_FORUM_MSG_IMAGE_STYLE );
		arrowImages.add( arrowImage );
		addPanelElement( arrowImage );
		
		//Add the new topic button
		sendMessageButton.setStyleName( CommonResourcesContainer.ACTION_BUTTON_STYLE );
		sendMessageButton.addStyleName( CommonResourcesContainer.ACTION_PANEL_BUTTON_EXTRA_STYLE );
		sendMessageButton.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e) {
				//Ensure lazy loading
				( new SplitLoad( true ) {
					@Override
					public void execute() {
						//Close the opened dialogs first, if any
						closeSendForumMessageDialog();
						//Open the "New topic" or "Reply message" dialog
						if( messageData == null ) {
							//Here we are just creating a new section
							final ForumMessageData newSectionMessage = new ForumMessageData();
							newSectionMessage.senderID = SiteManager.getUserID(); //Set the proper sender ID
							newSectionMessage.messageID = ShortForumMessageData.ROOT_FORUM_MESSAGE_ID;
							sendForumMessageDialog = new SendForumMessageDialogUI( newSectionMessage, true, true, false );
						} else {
							//Alternatively we are replying to some message
							sendForumMessageDialog = new SendForumMessageDialogUI( messageData, true, false );
						}
						sendForumMessageDialog.show();
						sendForumMessageDialog.center();
					}
				}).loadAndExecute();
			}
		});
		addPanelElement( sendMessageButton );
		
		//Add the second arrow
		arrowImage = new Image( ServerSideAccessManager.getForumNewTopicImageURL() );
		arrowImage.setStyleName( CommonResourcesContainer.CREATE_NEW_FORUM_MSG_IMAGE_STYLE );
		arrowImages.add( arrowImage );
		addPanelElement( arrowImage );
		
		//Set the initial component type to create new section
		setNewActionType(null);
	}
	
	/**
	 * Allows to set the current status of the action to creating: New Section, New Topic, Write here
	 * The set action is allowed if it is either not creating a new section, i.e. messageData != null
	 * or the user is an administrator.
	 * @param messageData the message data of null if not applicable
	 */
	public void setNewActionType( final ForumMessageData messageData ) {
		this.messageData = messageData; //Store the message data
		
		//Check on the type of allowed action
		if( messageData != null ) {
			if( messageData.isForumSectionMessage() ) {
				//This is the forum section message thus we are allowed to create new topics
				this.hintImageURL = ServerSideAccessManager.getForumNewTopicImageURL();
				sendMessageButton.setText( i18NTitles.newForumTopicButtonTitle() );
			} else {
				//This is the forum topic or simple message thus we are allowed write here
				this.hintImageURL = ServerSideAccessManager.getForumWriteHereImageURL();
				sendMessageButton.setText( i18NTitles.postForumMessageHereButtonTitle() );
			}
			isActionAllowed = true; //Everyone can make a new topic or write into an old one
		} else {
			//It is a create "new forum section" action, is only allowed for the admin
			this.hintImageURL = ServerSideAccessManager.getForumNewSectionImageURL();
			sendMessageButton.setText( i18NTitles.newForumSectionButtonTitle() );
			isActionAllowed = SiteManager.isAdministrator();
		}
		
		//Update the hint images
		updateHintImages();
	}
	
	//Allows to update the hint images according to the current component status
	private void updateHintImages() {
		final String url = ( this.isActionAllowed && this.enabled && this.isLoggedIn ) ? hintImageURL : disabledHintImageURL;
		for( Image image : arrowImages ) {
			image.setUrl( url );
		}
	}
	
	/**
	 * Allows to check if the currently set action is allowed
	 * @return true if the user is logged int, the component is enabled and the action is allowed for the logged in user
	 */
	private boolean isAllowedAction() {
		return this.isActionAllowed && this.enabled && this.isLoggedIn;
	}
	
	/**
	 * Adds a widget to the main panel
	 * @param widget the widget to add
	 */
	private void addPanelElement(Widget widget) {
		//Add widget
		mainSearchPanel.add( widget );
		//Add delimiter
		mainSearchPanel.add(  new HTML("&nbsp;") );
	}
	
	/**
	 * Allows to add a delimiter to the panel
	 */
	public void addPanelDelimiter() {
		HTML delimiter = new HTML("&nbsp");
		delimiter.setStyleName( CommonResourcesContainer.FORUM_SEARCH_PANEL_DELIMITER_STYLE );
		addPanelElement(delimiter);
	}
	
	/**
	 * Allows to get the progress bar UI
	 * @return the progress bar UI
	 */
	public ServerCommStatusPanel getProgressBarUI() {
		return progressBarUI;
	}
	
	/**
	 * Allows to check if the current topic, the one we are viewing now, is set
	 * @return true if the current topics is set
	 */
	public boolean isCurrentTopicSet() {
		return  ( baseMessageID != ShortForumMessageData.UNKNOWN_MESSAGE_ID ) &&
				( baseMessageID != ShortForumMessageData.ROOT_FORUM_MESSAGE_ID );
	}
	
	/**
	 * Allows to set the base message ID that identifies the currently browsed topic
	 * @param baseMessageID the ID of the message that identifies the currently browsed topic.
	 */
	public void setCurrTopicMessageID( final int baseMessageID ) {
		this.baseMessageID = baseMessageID;
		if( isCurrentTopicSet() ) {
			//If the base message is defined then we can enable the check box,
			if( enabled && !onlyInTopicsCheckBox.getValue() ) {
				//This should be done only if the components (controls) are enabled
				//and we are not looking searching for the topics only
				setCheckBoxEnabled( onlyInThisTopicCheckBox, true );
			}
		} else {
			//Disable the check box
			setCheckBoxEnabled( onlyInThisTopicCheckBox, false );
			//Set the check box unchecked
			onlyInThisTopicCheckBox.setValue( false );
		}
	}
	
	/**
	 * Allows to initiate the search
	 */
	private void doNewSearch() {
		//Create the search data object
		ForumSearchData searchData = new ForumSearchData();
		//Collect the search parameters
		searchData.byUserID = authorID;
		searchData.byUserLoginName = authorLoginName;
		searchData.isOnlyTopics = onlyInTopicsCheckBox.getValue();
		searchData.searchString = searchTextBox.getValue();
		searchData.isOnlyInCurrentTopic = onlyInThisTopicCheckBox.getValue();
		//Set the base message only if it is needed for the current topic search
		if( searchData.isOnlyInCurrentTopic ) {
			searchData.baseMessageID = baseMessageID;
		}
		//Initiate the search procedure
		ForumSearchManager.doSearch( new MessageStackElement( searchData, null ) );
	}
	
	@Override
	public void setEnabled( boolean enabled ) {
		searchButton.setEnabled(enabled);
		refreshButton.setEnabled(enabled);
		searchTextBox.setEnabled(enabled);
		if( ! onlyInThisTopicCheckBox.getValue() && enabled ) {
			//If we are not searching in the current topic
			setCheckBoxEnabled( onlyInTopicsCheckBox, true );
		} else {
			setCheckBoxEnabled( onlyInTopicsCheckBox, false );
		}
		if( ! onlyInTopicsCheckBox.getValue() && enabled && isCurrentTopicSet() ) {
			//If we are not searching for topics
			setCheckBoxEnabled( onlyInThisTopicCheckBox, true );
		} else {
			setCheckBoxEnabled( onlyInThisTopicCheckBox, false );
		}
		this.enabled = enabled;
		//If we are enabling elements
		if( enabled ) {
			//If the user is logged in
			if( isLoggedIn ) {
				//The search by the user is enabled
				authorValue.setStyleName( CommonResourcesContainer.LINK_BLUE_STYLE );
				//Set the remove author image button
				removeRecepientImage.setUrl( ServerSideAccessManager.getRemoveImageButtonURL() );
				removeRecepientImage.setStyleName( CommonResourcesContainer.REMOVE_AUTHOR_BUTTON_IMAGE_STYLE );
				removeRecepientImage.setTitle( i18NTitles.removeAuthorToolTip() );
			}
		} else {
			//The search by the user is disabled
			authorValue.setStyleName( CommonResourcesContainer.LINK_DISABLED_STYLE );
			//Set the remove author image button
			removeRecepientImage.setUrl( ServerSideAccessManager.getRemoveImageButtonDisabledURL() );
			removeRecepientImage.setStyleName( CommonResourcesContainer.REMOVE_AUTHOR_BUTTON_DIS_IMAGE_STYLE );
			removeRecepientImage.setTitle( "" );
		}
		if( BrowserDetect.getBrowserDetect().isOpera() ) {
			//For Opera the buttons get miss aligned, this is a workaround
			setComponentWidget( wrapperPanel );
		}
		setEnabledCreate( enabled );
	}
	
	private void setEnabledCreate( boolean enabled ) {
		this.enabled = enabled;
		
		//Update the button
		sendMessageButton.setEnabled( isAllowedAction() );
		if( BrowserDetect.getBrowserDetect().isOpera() ) {
			//For Opera the buttons get miss aligned, this is a workaround
			setComponentWidget( wrapperPanel );
		}
		
		//Update the hint images
		updateHintImages();
	}

	private void setCheckBoxEnabled(final CheckBox checkBox, final boolean enabled ) {
		if( enabled ) {
			checkBox.setStyleName( CommonResourcesContainer.REGULAR_FIELD_STYLE);
		} else {
			checkBox.setStyleName( CommonResourcesContainer.DISABLED_FIELD_STYLE);
		}
		checkBox.setEnabled(enabled);
	}
	
	/**
	 * Allows to set the selected author for the forum message search
	 * @param authorID the id of the author
	 * @param authorLoginName the login name of the authro
	 */
	public void setForumAuthor( final int authorID, final String authorLoginName ) {
		authorValue.setText( ShortUserData.getShortLoginName( authorLoginName ) );
		authorValue.setTitle( authorLoginName );
		this.authorID = authorID;
		this.authorLoginName = authorLoginName;
		
		//Set and add the remove author image button
		removeRecepientImage.setUrl( ServerSideAccessManager.getRemoveImageButtonURL() );
		removeRecepientImage.setTitle( i18NTitles.removeAuthorToolTip() );
		authorSearchComponents.add( removeRecepientImage );
	}
	
	/**
	 * Allows to remove the author wen searching for messages
	 * @param makeDisabled if true then we make the author selection link disabled
	 */
	private void removeForumAuthor(final boolean makeDisabled) {
		if( makeDisabled ) {
			authorValue.setStyleName( CommonResourcesContainer.LINK_DISABLED_STYLE );
		}
		authorValue.setText( i18NTitles.postedByAnyoneValue() );
		authorID = ShortUserData.UNKNOWN_UID;
		authorLoginName = "";
		
		//Remove the remove-author image button
		authorSearchComponents.remove( removeRecepientImage );
	}
	
	@Override
	public void setUserLoggedIn() {
		//If the component is currently enabled then we enable the new topic button
		if( enabled ) {
			//The search by the user is enabled
			authorValue.setStyleName( CommonResourcesContainer.LINK_BLUE_STYLE );
			//Set the remove author image button
			removeRecepientImage.setUrl( ServerSideAccessManager.getRemoveImageButtonURL() );
			removeRecepientImage.setStyleName( CommonResourcesContainer.REMOVE_AUTHOR_BUTTON_IMAGE_STYLE );
			removeRecepientImage.setTitle( i18NTitles.removeAuthorToolTip() );
		}
		isLoggedIn = true;
	}
	
	/**
	 * Allows to forse the search parameters into the search interface
	 * In case the user is logged out, we do not force the message author
	 * @param searchData the search parameters
	 */
	public void enforceSearchParams( final ForumSearchData searchData ) {
		//Allows to set the search parameters into the interfacce
		searchTextBox.setValue( searchData.searchString );
		
		//Set the base message ID and check boxes
		setCurrTopicMessageID( searchData.baseMessageID );
		onlyInThisTopicCheckBox.setValue( searchData.isOnlyInCurrentTopic );
		onlyInTopicsCheckBox.setValue( searchData.isOnlyTopics );
		
		//Set the user search parameters
		removeForumAuthor( !isLoggedIn );
		if( isLoggedIn && searchData.byUserID != ShortUserData.UNKNOWN_UID ) {
			setForumAuthor( searchData.byUserID,  searchData.byUserLoginName );
		}
	}
	
	/**
	 * Allows to close the new topic or reply message dialog if they are open
	 */
	private void closeSendForumMessageDialog() {
		//Close the create new forum topic dialog
		if( sendForumMessageDialog != null ) {
			sendForumMessageDialog.hide();
			sendForumMessageDialog = null;
		}
	}
	
	@Override
	public void onBeforeComponentIsRemoved() {
		//Close the opened dialog, if any
		closeSendForumMessageDialog();
	}
	
	@Override
	public void setUserLoggedOut() {
		isLoggedIn = false;
		//The search by the user is disabled
		removeForumAuthor( true );
	}

	@Override
	public void onBeforeComponentIsAdded() {
		//Does nothing
	}
	
	@Override
	public void updateUIElements( final boolean forceScrolling, final boolean adjustWidth, final int percentWidth ) {
		if( adjustWidth ) {
			//NOTE: Setting the width of the decorated panel alone does not work!!!
			//Thus we also set the width of the scroll panel
			final int decPanelWidth = (int) ( Window.getClientWidth() / 100.0 * percentWidth ); 
			setDecPanelWidth( decPanelWidth );
			//Here DECORATIONS_WIDTH is the width of the decorations we have around the decorated panel
			final int scrollPanelWidth = decPanelWidth - CommonResourcesContainer.DECORATIONS_WIDTH;
			wrapperPanel.setWidth( scrollPanelWidth + "px" );
		}
	}
}
