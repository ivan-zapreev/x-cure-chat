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
package com.xcurechat.client.forum.messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.OpenEvent;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.data.search.ForumSearchData;


import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.FlashObjectWithJWPlayer;
import com.xcurechat.client.utils.MessageTextToFlowPanel;
import com.xcurechat.client.utils.SmileyHandlerUI;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.SupportedFileMimeTypes;

import com.xcurechat.client.utils.widgets.ActionLinkPanel;
import com.xcurechat.client.utils.widgets.DateOldProgressBarUI;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarWidget;

import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.forum.DeleteMessageQuestionDialogUI;
import com.xcurechat.client.forum.ForumSearchManager;
import com.xcurechat.client.forum.MessageStackElement;
import com.xcurechat.client.forum.SendForumMessageDialogUI;
import com.xcurechat.client.forum.ViewForumMediaFilesDialogUI;
import com.xcurechat.client.forum.ViewMessageDialogUI;

import com.xcurechat.client.forum.sharing.ShareMessageFacebookURL;
import com.xcurechat.client.forum.sharing.ShareMessageLinkURL;
import com.xcurechat.client.forum.sharing.ShareMessageMailRuURL;
import com.xcurechat.client.forum.sharing.ShareMessageTwitterURL;
import com.xcurechat.client.forum.sharing.ShareMessageVKontakteURL;

import com.xcurechat.client.rpc.ForumManagerAsync;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * This widget represents the forum message/topic shown in the forum
 */
public abstract class ForumMessageWidget extends Composite {
	//The default width of the progress bar in pixels, the progress bar for how new the post date is
	private static final int DEFAULT_PROGRESS_BAR_WIDTH_PIXELS = 100;
	//The default height of the progress bar in pixels
	private static final int DEFAULT_PROGRESS_BAR_HEIGHT_PIXELS = 3;
	
	/**
	 * @author zapreevis
	 * This click handler allows to open the attached image view dialog
	 */
	private class AttachedImageClickHandler implements ClickHandler {
		private final List<ShortFileDescriptor> fileDescList;
		final int currentIndex;
		public AttachedImageClickHandler( final List<ShortFileDescriptor> fileDescList, final int currentIndex ) {
			this.fileDescList = fileDescList;
			this.currentIndex = currentIndex;
		}
		public void onClick( ClickEvent e) {
			//Ensure lazy loading
			( new SplitLoad( true ) {
				@Override
				public void execute() {
					//Open the image view pop up
					ViewForumMediaFilesDialogUI viewDialog = new ViewForumMediaFilesDialogUI( null, fileDescList, currentIndex );
					viewDialog.show();
					viewDialog.center();
				}
			}).loadAndExecute();
			//Stop the event from being propagated, prevent default
			e.stopPropagation(); e.preventDefault();
		}
	}
	
	//The message title clicking handler registration 
	private HandlerRegistration messageTitleClickHandlerReg = null;
	
	//This is the click handler that is used for viewing the message replies
	private final ClickHandler viewMessageRepliesClickHandler = new ClickHandler() {
		 public void onClick(ClickEvent e) {
				final ForumSearchData viewRepliesData = new ForumSearchData();
				viewRepliesData.baseMessageID = messageData.messageID;
				ForumSearchManager.doSearch( new MessageStackElement( viewRepliesData, messageData ) );
				//Stop the event from being propagated, prevent default
				e.stopPropagation(); e.preventDefault();
			 }
		 };
	
	//Stores true if the user is indicated to be logged in
	private boolean isLoggedIn = false;
	//Stores true if the actions on the message are currently allowed
	private boolean enabled = false;
	
	//The decorated panel that encloses all of the message elements
	private final DecoratorPanel decoratedPanel = new DecoratorPanel();
	//Stores the visualized forum message data
	protected final ForumMessageData messageData;
	//If true then this is an odd message, otherwise it is even, on the given page
	private final boolean oddOrNot;
	//The main vertical panel storing all of the other elements
	private final VerticalPanel mainVerticalPanel = new VerticalPanel();
	//The title panel storing all of the message title information
	private final FlexTable titlePanelTable = new FlexTable();
	
	//The message subject text flow panel
	private final FlowPanel messageSubjectContent = new FlowPanel();
	//This panel is added and instantiated in case the message subject is click-able
	private FocusPanel messageSubjectClickPanel = null;
	//The message info flow panel
	private final FlowPanel titleInfoPanel = new FlowPanel();
	//THe message info flow panel with the last reply info
	private final FlowPanel titleInfoLastRepPanel = new FlowPanel();
	//The horizontal panel with the message body
	private final VerticalPanel msgBodyLeveledPanel = new VerticalPanel();
	//The flow panel of the message body
	private final FlowPanel messageBodyFlow = new FlowPanel();
	//The flow panel of the message body: Images
	private final FlowPanel imagesPanel = new FlowPanel();
	//The flow panel of the message body: Flash
	private final FlowPanel flashPanel = new FlowPanel();
	//The user avatar widget if needed or null if it is not
	private UserAvatarWidget userAvatar = null;
	
	//The internationalization class
	protected static final UITitlesI18N i18nTitles = I18NManager.getTitles();
	
	//The profile link to the last reply sender
	private Label lastSenderProfileLink = null;
	private HandlerRegistration lastReplyClickHandlerReg = null;
	
	//Message body related items
	private DisclosurePanel disclosurePanel;
	private final SimplePanel msgBodyScrollPanel = new SimplePanel();
	
	//The message action links panel elements
	private final SimplePanel approveMsgPanel = new SimplePanel();
	private ActionLinkPanel approveMessageAct = null;
	private ActionLinkPanel disApproveMessageAct = null;
	private ActionLinkPanel moveMessageAct = null;
	private final SimplePanel deleteMsgPanel = new SimplePanel();
	private ActionLinkPanel deleteMessageAct = null;
	private ActionLinkPanel editMessageAct = null;
	private final SimplePanel viewMessageTopicPanel = new SimplePanel();
	private ActionLinkPanel viewMessageTopicAct = null; //Stays null for the topic message
	private ActionLinkPanel viewMessageRepliesAct = null;
	private ActionLinkPanel replyToMessageAct = null;
	
	//The reply or edit message dialog references
	private MoveMessageDialogUI moveMessageDialog = null;
	private SendForumMessageDialogUI replyMessageDialog = null;
	private SendForumMessageDialogUI editMessageDialog = null;
	private DeleteMessageQuestionDialogUI warningDialog = null;
	private DeleteMessageQuestionDialogUI deleteForumMessageDialog = null;
	private ViewMessageDialogUI viewTopicMessageDialog = null;
	
	//Contains true if the last reply sender is an existing user, false if it is a deleted one
	protected final boolean doesLastReplySenderExist;
	
	//If true then the action panel is shown, otherwise not
	private final boolean showActionPanel;
	
	//if true then the content disclosure panel is set open no matter what
	private final boolean forseContentOpen;
	
	//if the message title should be click-abel
	private final boolean isMsgTitleClickable;
	
	//The widget responsible for forum-message voting and visualizing the votes
	private ForumMessageVoteWidget messageVoteWidget = null;
	
	//The loading progress bar
	private ServerCommStatusPanel progressBarUI = null;
	
	/**
	 * Allows to get an instance of the progress bar UI 
	 * @return an instance of the progress bar UI
	 */
	private ServerCommStatusPanel getProgressBarUI() {
		//Check if the progress bar was instantiated
		if( progressBarUI == null ) {
			progressBarUI = new ServerCommStatusPanel();
		}
		return progressBarUI;
	}
	
	/**
	 * The main constructor
	 * @param messageData the message data which this widget will contain
	 * @param isLoggedIn true if the user is logged in
	 * @param oddOrNot if true then this is an odd message, otherwise it is even, on the given page
	 * @param showActionPanel if true then the action panel is shown, otherwise not
	 * @param forseContentOpen if true then the content disclosure panel is set open no matter what
	 * @param isMsgTitleClickable if the message title should be clickable clicking on the title moves us to the message's children
	 */
	public ForumMessageWidget( final ForumMessageData messageData, final boolean isLoggedIn,
								final boolean oddOrNot, final boolean showActionPanel,
								final boolean forseContentOpen, final boolean isMsgTitleClickable ) {
		this.messageData = messageData;
		this.isLoggedIn = isLoggedIn;
		this.oddOrNot = oddOrNot;
		this.showActionPanel = showActionPanel;
		this.forseContentOpen = forseContentOpen;
		this.doesLastReplySenderExist = messageData.isLastReplySenderRegistered();
		this.isMsgTitleClickable = isMsgTitleClickable;
	}
	
	/**
	 * Should be called once in the ocnstructor of every subclass, to initialize the widget
	 */
	protected void initializeWidget() {
		//Populates the widget with the elements
		populateWidget();
		
		//Allows to initialize required click handlers and etc
		setUserLoggedIn( isLoggedIn );
		
		//Initialize the composite.
		initWidget( decoratedPanel );
	}
	
	protected void addNewFieldValuePair( final FlowPanel content, final String fieldName,
			   						   final Date dateValue, final boolean addEndCommaDelimiter ) {
		final Label fieldLabel = new Label( fieldName );
		fieldLabel.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_SHORT );
		final Label dateTimeLabel = new Label( dateTimeFormat.format( dateValue ) );
		dateTimeLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
		final int width = ( dateTimeLabel.getOffsetWidth() == 0 ? DEFAULT_PROGRESS_BAR_WIDTH_PIXELS : dateTimeLabel.getOffsetWidth() );
		DateOldProgressBarUI progressBarUI= new DateOldProgressBarUI( width, DEFAULT_PROGRESS_BAR_HEIGHT_PIXELS, dateValue );
		
		//If there are replies to this message
		VerticalPanel dateTimePanel = new VerticalPanel();
		dateTimePanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		dateTimePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		dateTimePanel.add( dateTimeLabel );
		dateTimePanel.add( progressBarUI );
		
		//Add the widgets to the panel
		addNewFieldValuePair( content, fieldLabel, dateTimePanel, addEndCommaDelimiter );
	}
	
	protected void addNewFieldValuePair( final FlowPanel content, final String fieldName,
									   final String fieldValue, final boolean addEndCommaDelimiter ) {
		Label fieldLabel = new Label( fieldName );
		fieldLabel.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		Label valueLabel = new Label( fieldValue );
		valueLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
		
		//Add the widgets to the panel
		addNewFieldValuePair( content, fieldLabel, valueLabel, addEndCommaDelimiter );
	}
	
	protected void addContentWidgetWebKit( final FlowPanel content, final Widget wid ) {
		Widget widToAdd = wid;
		//Somehow Chrome and Safari do not understand inline in this context, so we use float left
		if( BrowserDetect.getBrowserDetect().isSafari() || BrowserDetect.getBrowserDetect().isChrome() ) {
			SimplePanel panelWrap = new SimplePanel();
			panelWrap.add( wid );
			panelWrap.addStyleName( CommonResourcesContainer.FLOAT_LEFT_INSTEAD_OF_INLINE_STYLE );
			widToAdd = panelWrap;
		}
		MessageTextToFlowPanel.addContentWidget( content, widToAdd, false );
	}
	
	protected void addNewFieldValuePair( final FlowPanel content, final Widget fieldNameWid,
			   						   final Widget fieldValueWid, final boolean addEndCommaDelimiter ) {
		addContentWidgetWebKit( content, fieldNameWid );
		addContentWidgetWebKit( content, new HTML("&nbsp;") );
		addContentWidgetWebKit( content, fieldValueWid );
		HTML delimiter = null;
		if( addEndCommaDelimiter ) {
			delimiter = new HTML(",&nbsp;&nbsp;");
		} else {
			delimiter = new HTML("&nbsp;");
		}
		delimiter.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		addContentWidgetWebKit(content, delimiter );
	}
	
	/**
	 * @return should return true if the avatar vertical panel is needed, for the user name or avatar image, otherwise false
	 */
	public abstract boolean isAvatarTitlePanelNeeded();
	
	/**
	 * Allows to populate the vertical panel with the avatar image or just a user name and etc
	 * @param avatarVerticalPanel the panel to populate
	 * @param senderData the information about the message sender
	 * @return an instance of the added user avatar widget or null if it was not added or was added but in a modewithout the image
	 */
	public abstract UserAvatarWidget populateAvatarTitlePanel( final VerticalPanel avatarVerticalPanel, final ShortUserData senderData  );
	
	/**
	 * Allows to retrieve the proper name for the message subject field
	 * @return the proper name for the message subject field
	 */
	public abstract String getMessageSubjectFieldName();
	
	/**
	 * Allows to set proper styles for the subject field and value widgets
	 * @param messageSubjectField the field label
	 * @param messageSubjectContent the panel with the message title text
	 */
	public abstract void setMessageSubjectFieldValueStyles( final Label messageSubjectField, final FlowPanel messageSubjectContent );
	
	/**
	 * Allows to populate the title info panel with information such as about when the message was created and how many replies it has
	 * @param titleInfoPanel the flow panel to populate
	 */
	public abstract void populateMessageTitleInfoPanel( final FlowPanel titleInfoPanel );
	
	/**
	 * Allows to populate the title info last-reply panel with information such as how many replies it has and who was the last to reply
	 * @param titleInfoLastRepPanel the flow panel to populate
	 * @return the label that is the link for the last reply sender profile, the click listener is not set here or null if not set
	 */
	public abstract Label populateMessageTitleLastReplyInfoPanel( final FlowPanel titleInfoLastRepPanel );
	
	/**
	 * Allows to add information about the last user to reply in this branch
	 * @param thePanel the panel to add the information to
	 * @param lastReplyTitle the title to user, e.g. "Last post" or "Last reply"
	 * @return the label link to the profileof the user who did the last reply, with no action listener
	 */
	protected Label addLastReplyInfoToPanel( FlowPanel thePanel, final String lastReplyTitle) {
		Label lastSenderProfileLink = null;
		if( messageData.numberOfReplies > 0 ) {
			addNewFieldValuePair( thePanel, lastReplyTitle, messageData.lastReplyDate, false );
			addNewFieldValuePair( thePanel, i18nTitles.forumMessageLastReplyBy(), "", false );
			//Add link to the last sender profile
			if( doesLastReplySenderExist ) {
				lastSenderProfileLink = new Label( messageData.lastReplyUser.getShortLoginName() );
				lastSenderProfileLink.setTitle( messageData.lastReplyUser.getUserLoginName() );
			} else {
				lastSenderProfileLink = new Label( i18nTitles.unknownMsgSenderReceiver() );
				lastSenderProfileLink.setTitle( i18nTitles.senderHasDeletedHisProfile() );
			}
			MessageTextToFlowPanel.addContentWidget( thePanel, lastSenderProfileLink, false );
		}
		return lastSenderProfileLink;
	}
	
	/**
	 * Allows to update the avatar of the listed forum messages for the given user with the new spoiler data
	 * @param userID the id of the user for which the spoiler should be updated
	 * @param spoilerID the new spoiler id
	 * @param spoilerExpDate the new expiration date for the spoiler 
	 */
	public void updateUserAvatarSpoiler(int userID, int spoilerID, Date spoilerExpDate) {
		if( userAvatar != null ) {
			userAvatar.updateThisAvatarSpoiler( userID, spoilerID, spoilerExpDate);
		}
	}
	
	private void populateTitlePanel() {
		//Add the avatar panel to the message if needed
		final boolean isAvatarPanelNeeded = isAvatarTitlePanelNeeded();
		if( isAvatarPanelNeeded ) {
			//Add the sub-panel with the avatar
			VerticalPanel avatarTitlePanel = new VerticalPanel();
			avatarTitlePanel.setWidth("100%");
			avatarTitlePanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			avatarTitlePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
			avatarTitlePanel.addStyleName( CommonResourcesContainer.MESSAGE_AVATAR_TITLE_PANEL_STYLE );
			
			//Populate the panel, store the link to the avatar, if needed
			userAvatar = populateAvatarTitlePanel( avatarTitlePanel, messageData.senderData );
			
			//Put it into the message
			titlePanelTable.setWidget(0, 0, avatarTitlePanel );
			titlePanelTable.getCellFormatter().addStyleName(0, 0, CommonResourcesContainer.FORUM_MESSAGE_SUBJECT_DELIM_PANEL_STYLE );
		}
		
		//Add the subpanel with the title and other message info
		VerticalPanel titleVertPanel = new VerticalPanel();
		titleVertPanel.setHeight("100%");
		//In case of the avatar panel NOT needed this is the first cell in the row, otherwise it is the second cell
		titlePanelTable.setWidget(0, ( isAvatarPanelNeeded ? 1 : 0 ), titleVertPanel );
		titleVertPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		titleVertPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		
		final Label messageSubjectField = new Label( getMessageSubjectFieldName() + ":" );
		messageSubjectField.setWordWrap( false );
		MessageTextToFlowPanel.addContentWidget( messageSubjectContent, messageSubjectField, true );
		final List<Widget> messageWidgets = SmileyHandlerUI.getMessageViewObject( messageData.messageTitle,
																				  ChatMessage.MAX_ONE_WORD_LENGTH, false );
		for( Widget w : messageWidgets ) {
			MessageTextToFlowPanel.addContentWidget( messageSubjectContent, w, true );
		}
		
		//Set the proper styles to the field and value labels
		setMessageSubjectFieldValueStyles( messageSubjectField, messageSubjectContent );
		titleVertPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		titleVertPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		//Add message title clicking capability, if needed
		if( isMsgTitleClickable ) {
			//THe clickable version allows to view the forum message replies
			messageSubjectClickPanel = new FocusPanel();
			messageSubjectClickPanel.setTitle( getViewForumMessageRepliesLinkText() );
			messageSubjectClickPanel.addStyleName( CommonResourcesContainer.FORUM_MESSAGE_TITLE_STYLE );
			messageSubjectClickPanel.add( messageSubjectContent );
			titleVertPanel.add( messageSubjectClickPanel );
		} else {
			messageSubjectContent.addStyleName( CommonResourcesContainer.FORUM_MESSAGE_TITLE_STYLE );
			titleVertPanel.add( messageSubjectContent );
		}
		
		lastSenderProfileLink = populateMessageTitleLastReplyInfoPanel( titleInfoLastRepPanel );
		titleVertPanel.add( titleInfoLastRepPanel );
		
		//Add the message title info
		if( SiteManager.isAdministrator() ) {
			//if the user is an administrator, then he is allowed to see the message id of all messages
			addNewFieldValuePair( titleInfoPanel, i18nTitles.forumMessageID(), messageData.messageID + "", true );
		}
		populateMessageTitleInfoPanel( titleInfoPanel );
		titleVertPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		titleVertPanel.add( titleInfoPanel );
		
		//Complete the title panel
		mainVerticalPanel.add( titlePanelTable );

		//Add the delimiter panel
		SimplePanel delimiterPanel = new SimplePanel();
		delimiterPanel.setStyleName( CommonResourcesContainer.FORUM_MESSAGE_TITLE_DELIMITER_PANEL_STYLE );
		mainVerticalPanel.add( delimiterPanel );
	}
	
	/**
	 * Allows to get the title for the message body content
	 * @return the title for the message body content
	 */
	protected abstract String getMessageBodyContentTitle();
	
	/**
	 * Allows to detect if the message body content should be open by default or not
	 * @return true the message body content should be open by default or not
	 */
	protected abstract boolean isMessageBodyContentOpen();
	
	private void populateMessageBody() {
		disclosurePanel = new DisclosurePanel( getMessageBodyContentTitle() );
		disclosurePanel.addOpenHandler( new OpenHandler<DisclosurePanel>() {
			public void onOpen(OpenEvent<DisclosurePanel> event) {
				//Adjust the height of the message body
				adjustHeight();
			}
		});
		disclosurePanel.setWidth("100%");
		disclosurePanel.setAnimationEnabled( true );
		disclosurePanel.setOpen( isMessageBodyContentOpen() || forseContentOpen );
		msgBodyScrollPanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		disclosurePanel.add( msgBodyScrollPanel );
		
		//Take the message, parse out the embedded plash animations,
		//then process split the text line by line and process smileys
		//along with the lines that are ">>" citations 
		messageData.messageBody = ( messageData.messageBody == null ? "" : messageData.messageBody.trim() );
		final boolean isMsgBodyEmpty = messageData.messageBody.isEmpty() && messageData.attachedFileIds.isEmpty();
		final String messageBody = isMsgBodyEmpty ? i18nTitles.undefinedTextValue() : messageData.messageBody;
		MessageTextToFlowPanel converter = new MessageTextToFlowPanel( messageBodyFlow, messageBody, CommonResourcesContainer.REPLY_LINE_PREFIX, true );
		//Process the input, convert the text into widgets and store them in messageBodyFlow
		converter.process();
		
		//Append attached flash and images.
		int currentIndex = 0; //The index of the image file
		List< ShortFileDescriptor > imageFiles = new ArrayList<ShortFileDescriptor>(); //The list of image files
		for( ShortFileDescriptor fileDesc: messageData.attachedFileIds ) {
			if( SupportedFileMimeTypes.isImageMimeType( fileDesc.mimeType ) ) {
				Image image = new Image();
				image.setUrl( ServerSideAccessManager.getForumFileURL( fileDesc, true ) );
				image.setStyleName( CommonResourcesContainer.ATTACHED_IMAGE_PREVIEW_STYLE );
				image.addStyleName( CommonResourcesContainer.ZOOME_IN_IMAGE_STYLE );
				if( BrowserDetect.getBrowserDetect().isOpera() ) {
					//This is specifically for the shitty opera
					image.addStyleName( CommonResourcesContainer.FLOAT_LEFT_INSTEAD_OF_INLINE_STYLE );
				}
				image.setTitle( i18nTitles.clickToViewToolTip() );
				image.addClickHandler( new AttachedImageClickHandler( imageFiles, currentIndex ) );
				MessageTextToFlowPanel.addContentWidget( imagesPanel, image, false );
				MessageTextToFlowPanel.addContentWidget( imagesPanel, new HTML("&nbsp;"), false );
				imageFiles.add( fileDesc ); //Add the image file to the list
				currentIndex++; //Increment the image file index
			} else {
				if( SupportedFileMimeTypes.isPlayableMimeType( fileDesc.mimeType ) ) {
					FlashObjectWithJWPlayer flashObject = new FlashObjectWithJWPlayer( null, getForumMessageURL( messageData ),
																					   GWT.getModuleBaseURL() );
					
					//Construct the embedded flash object
					flashObject.setMediaUrl( ServerSideAccessManager.getForumFileURL( fileDesc, false ), fileDesc.mimeType );
					flashObject.completeEmbedFlash( messageData.isApproved );
					
					//Get he flash object with the file downloading link
					MessageTextToFlowPanel.addContentWidget( flashPanel, flashObject.getEmbeddedObjectWidget( true, true ), false );
					//ChatMessageBaseUI.addContentWidget( flashPanel, new HTML("&nbsp;"), false );
				} else {
					//Should not be happening
					Window.alert("Unknown attached file mime-format: " + fileDesc.mimeType + ", skipping the file!");
				}
			}
		}
		
		msgBodyLeveledPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		msgBodyLeveledPanel.setWidth("100%");
		msgBodyLeveledPanel.add( messageBodyFlow );
		msgBodyLeveledPanel.add( imagesPanel );
		msgBodyLeveledPanel.add( flashPanel );
		
		msgBodyScrollPanel.add( msgBodyLeveledPanel );
		disclosurePanel.addStyleName( CommonResourcesContainer.FORUM_MESSAGE_TITLE_DELIMITER_PANEL_STYLE );
		mainVerticalPanel.add( disclosurePanel );
	}
	
	/**
	 * Allows to populate the footer panel of the message
	 * @param showActionPanel if true then the message action buttons are added to the message
	 */
	private void populateFooterPanel( final boolean showActionPanel ) {
		HorizontalPanel footerHorizontalPanel = new HorizontalPanel();
		footerHorizontalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		footerHorizontalPanel.setWidth("100%");
		
		if( showActionPanel ) {
			HorizontalPanel actionButtonsPanel = new HorizontalPanel();
			actionButtonsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
			actionButtonsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			
			if( isDeleteMessageActionLink() ) {
				deleteMessageAct = new ActionLinkPanel( ServerSideAccessManager.getDeleteMessageURL( true ), "",
						 ServerSideAccessManager.getDeleteMessageURL( false ), "",
						 i18nTitles.forumMessageDeleteButton(),
						 new ClickHandler(){
							 public void onClick(ClickEvent e) {
								if( messageData.numberOfReplies > 0 ) {
									//Ensure lazy loading
									( new SplitLoad( true ) {
										@Override
										public void execute() {
											 warningDialog = new DeleteMessageQuestionDialogUI( ForumMessageWidget.this );
											 warningDialog.show();
											 warningDialog.center();
										}
									}).loadAndExecute();
								} else {
									 deleteMessage();
								}
								//Stop the event from being propagated, prevent default
								e.stopPropagation(); e.preventDefault();
							 }
						 }, false, true, true );
				deleteMsgPanel.add( deleteMessageAct );
				actionButtonsPanel.add( deleteMsgPanel ); actionButtonsPanel.add( new HTML("&nbsp;") );
			}
			
			if( isMoveMessageActionLink() ) {
				moveMessageAct = new ActionLinkPanel( ServerSideAccessManager.getMoveMessageURL( true ), "",
						 ServerSideAccessManager.getMoveMessageURL( false ), "",
						 i18nTitles.forumMessageMoveButton(),
						 new ClickHandler(){
							 public void onClick(ClickEvent e) {
								//Ensure lazy loading of the dialog data
								( new SplitLoad( true ) {
									@Override
									public void execute() {
										moveMessageDialog = new MoveMessageDialogUI( messageData.messageID, null );
										moveMessageDialog.show();
										moveMessageDialog.center();
									}
								}).loadAndExecute();
								//Stop the event from being propagated, prevent default
								e.stopPropagation(); e.preventDefault();
							 }
						 }, false, true, true );
				actionButtonsPanel.add( moveMessageAct ); actionButtonsPanel.add( new HTML("&nbsp;") );
			}
			
			if( isApproveMessageActionLink() ) {
				approveMessageAct =  new ActionLinkPanel( ServerSideAccessManager.getApproveMessageURL( true ), "",
							ServerSideAccessManager.getApproveMessageURL( false ), "",
							i18nTitles.forumMessageApproveButton(),
							new ClickHandler(){
								public void onClick(ClickEvent e) {
									//Disapprove the message
									doMessageApprove(true);
									//Stop the event from being propagated, prevent default
									e.stopPropagation(); e.preventDefault();
								}
							}, false, true, true );
				disApproveMessageAct =  new ActionLinkPanel( ServerSideAccessManager.getDisApproveMessageURL( true ), "",
						ServerSideAccessManager.getDisApproveMessageURL( false ), "",
						i18nTitles.forumMessageDisApproveButton(),
						new ClickHandler(){
							public void onClick(ClickEvent e) {
								//Disapprove the message
								doMessageApprove(false);
								//Stop the event from being propagated, prevent default
								e.stopPropagation(); e.preventDefault();
							}
						}, false, true, true );
				
				//Add the Approve or Disapprove action link depending on whether the message is approved or not
				approveMsgPanel.add( messageData.isApproved ? disApproveMessageAct : approveMessageAct );
				
				actionButtonsPanel.add( approveMsgPanel ); actionButtonsPanel.add( new HTML("&nbsp;") );
			}
			
			if( isEditMessageActionLink() ) {
				editMessageAct  = new ActionLinkPanel( ServerSideAccessManager.getEditMessageURL( true ), "",
						 ServerSideAccessManager.getEditMessageURL( false ), "",
						 i18nTitles.forumMessageEditButton(),
						 new ClickHandler(){
							 public void onClick(ClickEvent e) {
									//Ensure lazy loading
									( new SplitLoad( true ) {
										@Override
										public void execute() {
											editMessageDialog = new SendForumMessageDialogUI( messageData, false, false );
											editMessageDialog.show();
											editMessageDialog.center();
										}
									}).loadAndExecute();
								//Stop the event from being propagated, prevent default
								e.stopPropagation(); e.preventDefault();
							 }
						 }, false, true, true );
				actionButtonsPanel.add( editMessageAct ); actionButtonsPanel.add( new HTML("&nbsp;") );
			}
			
			if( isAddViewMessageTopicActionLink() ) {
				//Add only if this is not the message topic
				viewMessageTopicAct = new ActionLinkPanel( ServerSideAccessManager.getViewMessageTopicURL( true ), "",
						 ServerSideAccessManager.getViewMessageTopicURL( false ), "",
						 i18nTitles.forumMessageViewTopicButton(),
						 new ClickHandler(){
							 public void onClick(ClickEvent e) {
								viewMessageTopic( messageData );
								//Stop the event from being propagated, prevent default
								e.stopPropagation(); e.preventDefault();
							 }
						 }, false, true, true );
				viewMessageTopicPanel.add( viewMessageTopicAct );
				actionButtonsPanel.add( viewMessageTopicPanel ); actionButtonsPanel.add( new HTML("&nbsp;") );
			}
			
			if( isReplyToMessageActionLink() ) {
				replyToMessageAct = new ActionLinkPanel( ServerSideAccessManager.getReplyToMessageURL( true ), "",
						 ServerSideAccessManager.getReplyToMessageURL( false ), "",
						 i18nTitles.forumMessageReplyButton(),
						 new ClickHandler(){
							 public void onClick(ClickEvent e) {
									//Ensure lazy loading
									( new SplitLoad( true ) {
										@Override
										public void execute() {
											replyMessageDialog = new SendForumMessageDialogUI( messageData, true, true );
											replyMessageDialog.show();
											replyMessageDialog.center();
										}
									}).loadAndExecute();
								//Stop the event from being propagated, prevent default
								e.stopPropagation(); e.preventDefault();
							 }
						 }, false, true );
				actionButtonsPanel.add( replyToMessageAct ); actionButtonsPanel.add( new HTML("&nbsp;") );
			}
			
			viewMessageRepliesAct = new ActionLinkPanel( getViewForumMessageRepliesURL( true ), "",
														 getViewForumMessageRepliesURL( false ), "",
														 getViewForumMessageRepliesLinkText(),
														 viewMessageRepliesClickHandler, false, true );
			actionButtonsPanel.add( viewMessageRepliesAct ); actionButtonsPanel.add( new HTML("&nbsp;") );
			
			footerHorizontalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
			footerHorizontalPanel.add( actionButtonsPanel );
		}
		
		//Add the post-voting widget in the center if it is not a topic or section message
		if( ! messageData.isForumSectionMessage() && ! messageData.isForumTopicMessage() ) {
			footerHorizontalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			messageVoteWidget = new ForumMessageVoteWidget( messageData );
			footerHorizontalPanel.add( messageVoteWidget );
		}
		
		//Add the forum message sharing links
		addForumMessageSharingLinks( footerHorizontalPanel );
		
		mainVerticalPanel.add( footerHorizontalPanel );
	}
	
	/**
	 * Allows to add a horizontal panel with various sharing links to the forum messages.
	 * All of the forum messages get a web link to the post.
	 * THe regular posts get also sharing links for Twitter, Mail.ru and Facebook.
	 * @param panel the panel to add the panel to.
	 */
	private void addForumMessageSharingLinks( HorizontalPanel panel ) {
		final String forumMessageURL = getForumMessageURL( messageData );
		final String forumMessageTitle = messageData.messageTitle;
		
		HorizontalPanel linksPanel = new HorizontalPanel();
		linksPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		linksPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		//Add the post link
		linksPanel.add( new ShareMessageLinkURL( forumMessageURL, forumMessageTitle, i18nTitles.forumPostLinkTitle() ) );
		
		//If it is a regular forum post then add the sharing links
		if( ! messageData.isForumSectionMessage() && ! messageData.isForumTopicMessage() ) {
			linksPanel.add( new HTML("&nbsp;") );
			linksPanel.add( new ShareMessageTwitterURL( forumMessageURL, forumMessageTitle, i18nTitles.shareInTwitterLinkTitle() ) );
			linksPanel.add( new HTML("&nbsp;") );
			linksPanel.add( new ShareMessageVKontakteURL( forumMessageURL, forumMessageTitle, i18nTitles.shareInVKontakteLinkTitle() ) );
			linksPanel.add( new HTML("&nbsp;") );
			linksPanel.add( new ShareMessageMailRuURL( forumMessageURL, forumMessageTitle, i18nTitles.shareInMyWorldLinkTitle() ) );
			linksPanel.add( new HTML("&nbsp;") );
			linksPanel.add( new ShareMessageFacebookURL( forumMessageURL, forumMessageTitle, i18nTitles.shareInInFacebookLinkTitle() ) );
		}
		
		panel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		panel.add( linksPanel );
	}
	
	/**
	 * Allows to delete the forum message/topic on the server
	 * and then call the refresh of the messages panel
	 */
	public void deleteMessage( ) {
		//Get the progress bar UI
		final ServerCommStatusPanel progressBarUI = getProgressBarUI();
		
		//Add the progress bar to the panel
		deleteMsgPanel.clear();
		deleteMsgPanel.add( progressBarUI );
		
		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				//Perform the server call in order to delete the message/topic
				CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>( progressBarUI ) {
					public void onSuccessAct(Void result) {
						//NOTE: There is no need to put the delete action link back to the panel
						
						//Update the list of messages, no history update here
						ForumSearchManager.doSearch( );
					}
					public void onFailureAct(final Throwable caught) {
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Report the error
								ErrorMessagesDialogUI.openErrorDialog( caught );
							}
						}).loadAndExecute();
						//Use the recovery method
						recover();
					}
				};
				
				ForumManagerAsync forumManagerObj = RPCAccessManager.getForumManagerAsync();
				forumManagerObj.deleteMessage( SiteManager.getUserID(), SiteManager.getUserSessionId(), messageData.messageID, callback );
			}
			@Override
			public void recover() {
				//Put the delete action link back to the panel
				deleteMsgPanel.clear();
				deleteMsgPanel.add( deleteMessageAct );
			}
		}).loadAndExecute();
	}
	
	/**
	 * Allows to approve and disapprove the message
	 * @param approve true for approving otherwise false
	 */
	private void doMessageApprove( final boolean approve ) {
		//Get the progress bar UI
		final ServerCommStatusPanel progressBarUI = getProgressBarUI();
		
		//Add the progress bar to the panel
		approveMsgPanel.clear();
		approveMsgPanel.add( progressBarUI );
		
		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				//Create the server call back object
				CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>( progressBarUI ) {
					@Override
					public void onSuccessAct(Void result) {
						//Add the Approve or Disapprove action link depending on whether the message is approved or not
						approveMsgPanel.clear();
						approveMsgPanel.add( approve ? disApproveMessageAct : approveMessageAct );
					}
					@Override
					public void onFailureAct(final Throwable caught) {
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Report the error
								ErrorMessagesDialogUI.openErrorDialog( caught );
							}
						}).loadAndExecute();
						//Use the recovery method
						recover();
					}
				};
				
				//Do the server call
				ForumManagerAsync forumManagerObj = RPCAccessManager.getForumManagerAsync();
				forumManagerObj.approveForumMessage(SiteManager.getUserID(), SiteManager.getUserSessionId(), messageData.messageID, approve, callback );			
			}
			@Override
			public void recover() {
				//Add the Approve or Disapprove action link depending on whether the message was previously approved or not
				approveMsgPanel.clear();
				approveMsgPanel.add( ! approve ? disApproveMessageAct : approveMessageAct );
			}
		}).loadAndExecute();
	}
	
	/**
	 * This method takes the forum message, gets its topic message ID, retrieves the
	 * topic information from the server and shown it in a dialog view
	 * @param messageData the message for which we want to see the topic
	 */
	public void viewMessageTopic( final ForumMessageData messageData ) {
		final int topicMessageID = ShortForumMessageData.getTopicMessageID( messageData );
		if( topicMessageID != ShortForumMessageData.UNKNOWN_MESSAGE_ID ) {
			
			//Get the progress bar UI
			final ServerCommStatusPanel progressBarUI = getProgressBarUI();
			
			//Add the progress bar to the panel
			viewMessageTopicPanel.clear();
			viewMessageTopicPanel.add( progressBarUI );

			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Perform the server call in order to retrieve the forum message
					CommStatusAsyncCallback<ForumMessageData> callback = new CommStatusAsyncCallback<ForumMessageData>( progressBarUI ) {
						public void onSuccessAct(final ForumMessageData result) {
							//Put the action link back to the panel
							viewMessageTopicPanel.clear();
							viewMessageTopicPanel.add( viewMessageTopicAct );
							
							//Ensure lazy loading, open the topic message viewing dialog
							( new SplitLoad( true ) {
								@Override
								public void execute() {
									//Show the message data
									viewTopicMessageDialog = new ViewMessageDialogUI( result, true );
									viewTopicMessageDialog.show();
									viewTopicMessageDialog.center();
								}
							}).loadAndExecute();
						}
						public void onFailureAct(final Throwable caught) {
							(new SplitLoad( true ) {
								@Override
								public void execute() {
									//Report the error
									ErrorMessagesDialogUI.openErrorDialog( caught );
								}
							}).loadAndExecute();
							//Use the recovery method
							recover();
						}
					};
					
					ForumManagerAsync forumManagerObj = RPCAccessManager.getForumManagerAsync();
					forumManagerObj.getForumMessage(SiteManager.getUserID(), SiteManager.getUserSessionId(), topicMessageID, callback);
				}
				@Override
				public void recover() {
					//Put the action link back to the panel
					viewMessageTopicPanel.clear();
					viewMessageTopicPanel.add( viewMessageTopicAct );
				}
			}).loadAndExecute();
		} else {
			//Should not be happening.
			Window.alert("Could not determine the id of the topic message.");
		}
	}
	
	private void populateWidget() {
		//Initialze the title panel
		populateTitlePanel();
		
		//Populate message body if needed
		if( hasMessageBodyContent() ) {
			populateMessageBody();
		}
		
		//Populate message actions
		populateFooterPanel( showActionPanel );
		
		//Complete the composite
		mainVerticalPanel.setWidth("100%");
		mainVerticalPanel.addStyleName( CommonResourcesContainer.FORUM_MESSAGE_BODY_CONTENT_STYLE );
		decoratedPanel.setWidth( "100%" );
		decoratedPanel.add( mainVerticalPanel );
		decoratedPanel.setStyleName( oddOrNot ? CommonResourcesContainer.FORUM_MESSAGE_UI_ODD_STYLE : CommonResourcesContainer.FORUM_MESSAGE_UI_EVEN_STYLE );
	}
	
	/**
	 * Allows to indicate whether the user is logged in or not, this is needed for enabling/disablig component's actions
	 * @param isLoggedIn true if the user is logged in, otherwise false
	 */
	public void setUserLoggedIn( final boolean isLoggedIn ) {
		this.isLoggedIn = isLoggedIn;
		
		//Manage the avatar panel if it was added to the message
		if( userAvatar != null ) {
			if( isLoggedIn ) {
				userAvatar.setUserLoggedIn();
			} else {
				userAvatar.setUserLoggedOut();
			}
		}
		
		//Enable/Disable last sender profile link
		if( lastSenderProfileLink != null ) {
			final ShortUserData lastReplyUser = messageData.lastReplyUser;
			if( isLoggedIn ) {
				//Remove the disabled link style
				lastSenderProfileLink.removeStyleName( CommonResourcesContainer.DIALOG_LINK_DISABLED_STYLE );
				if( doesLastReplySenderExist ) {
					//If the user exists add the blue link style
					lastSenderProfileLink.addStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
					//Add the click handler if needed
					if( lastReplyClickHandlerReg == null ) {
						lastReplyClickHandlerReg = lastSenderProfileLink.addClickHandler( new ClickHandler() {
							public void onClick( ClickEvent e ) {
								//Ensure lazy loading
								final SplitLoad executor = new SplitLoad( true ) {
									@Override
									public void execute() {
										//Open the user profile view
										ViewUserProfileDialogUI dialog = new ViewUserProfileDialogUI( lastReplyUser.getUID(), lastReplyUser.getUserLoginName(), null, false );
										dialog.show();
										dialog.center();
									}
								};
								executor.loadAndExecute();
								//Stop the event from being propagated, prevent default
								e.stopPropagation(); e.preventDefault();
							}
						  } );
					}
				} else {
					lastSenderProfileLink.addStyleName( CommonResourcesContainer.DIALOG_LINK_DISABLED_STYLE );
				}
			} else {
				//Remove the blue link style, add the disabled link style
				lastSenderProfileLink.removeStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
				lastSenderProfileLink.addStyleName( CommonResourcesContainer.DIALOG_LINK_DISABLED_STYLE );
				if( doesLastReplySenderExist ) {
					//If the user exists remove the click listener if any
					if( lastReplyClickHandlerReg != null ) {
						lastReplyClickHandlerReg.removeHandler();
						lastReplyClickHandlerReg = null;
					}
				}
			}
		}
		
		//Update the status of the Move, Delete, Edit, Reply
		//and other present action link elements
		updateEnabledStatusActionLinks();
	}
	
	/**
	 * Update the enabled state of the Move, Delete, Edit, Reply and other present
	 * action link elements based on the enabled and isLoggedIn class fields and 
	 * also the fact that the current user is allowed to use them or not.
	 */
	private void updateEnabledStatusActionLinks() {
		if( approveMessageAct != null ) {
			approveMessageAct.setEnabled( enabled && isLoggedInAndAdmin() );
		}
		if( disApproveMessageAct != null ) {
			disApproveMessageAct.setEnabled( enabled && isLoggedInAndAdmin() );
		}
		if( moveMessageAct != null ) {
			moveMessageAct.setEnabled( enabled && isLoggedInAndAdmin() );
		}
		if( deleteMessageAct != null ) {
			deleteMessageAct.setEnabled( isUserAllowedToHaveDeleteEditActionsEnabled() );
		}
		if( editMessageAct != null ) {
			editMessageAct.setEnabled( isUserAllowedToHaveDeleteEditActionsEnabled() );
		}
		if( replyToMessageAct != null ) {
			replyToMessageAct.setEnabled( enabled && isLoggedIn );
		}
		if( viewMessageTopicAct != null ) {
			//Allow to view the message topic if the action is present
			viewMessageTopicAct.setEnabled( enabled );
		}
		if( viewMessageRepliesAct != null ) {
			//Allow to view replies for a forum topics even if there are no replies, or for a regular message with replies
			viewMessageRepliesAct.setEnabled( enabled && ( allowForViewingRepliesWhenNote() || ( messageData.numberOfReplies > 0 ) ) );
		}
		
		//Enable/disable the message title clicking
		enableMessageTitleClicking( enabled && ( allowForViewingRepliesWhenNote() || ( messageData.numberOfReplies > 0 ) ) );
		
		//Enable/disable the voting widget
		if( messageVoteWidget != null ) {
			messageVoteWidget.setEnabled( enabled && isLoggedIn );
		}
	}
	
	/**
	 * Allows to enable/disable the message title clicking.
	 * For this method to work we should have the message widget constructed with the message-title clicking enabled
	 * @param enable true to enable, false to disable
	 */
	private void enableMessageTitleClicking( final boolean enable ) {
		if( messageSubjectClickPanel != null ) {
			//If the message title clicking is enabled
			if( enable ) {
				if( messageTitleClickHandlerReg == null ) {
					messageTitleClickHandlerReg = messageSubjectClickPanel.addClickHandler( viewMessageRepliesClickHandler );
					messageSubjectClickPanel.addStyleName( CommonResourcesContainer.FORUM_MESSAGE_CLICKABLE_TITLE_STYLE );
				}
			} else {
				if( messageTitleClickHandlerReg != null ) {
					messageTitleClickHandlerReg.removeHandler();
					messageTitleClickHandlerReg = null;
					messageSubjectClickPanel.removeStyleName( CommonResourcesContainer.FORUM_MESSAGE_CLICKABLE_TITLE_STYLE );
				}
			}
		}
	}
	
	/**
	 * @return returns true if the user is logged in and he is either and admin or the message sender
	 */
	protected boolean isLoggedInAndAdminOrMessageOwner() {
		return isLoggedIn && ( ( SiteManager.getUserID() == messageData.senderID ) || SiteManager.isAdministrator() );		
	}
	
	/**
	 * @return true if the user is logged in and he is an administrator
	 */
	protected boolean isLoggedInAndAdmin() {
		return isLoggedIn && SiteManager.isAdministrator();
	}
	
	/**
	 * Allows to check if the Edit and Delete actions enabled
	 * @return true if the user is logged in, the interface is enabled and
	 * the user is either the sender of the message or is the administrator
	 */
	private boolean isUserAllowedToHaveDeleteEditActionsEnabled() {
		return enabled && isLoggedInAndAdminOrMessageOwner();
	}
	
	/**
	 * Allows to indicate if the user can view the message replies page in case
	 * when there are not replies, this is needed for forum sections and topics
	 * @return true if the user is allowed to view message replies even if there are none
	 */
	public abstract boolean allowForViewingRepliesWhenNote();
	
	/**
	 * Allows to enable-disable message actions
	 * @param enabled true to enable otherwise false
	 */
	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
		
		//Update the status of the Move, Delete, Edit, Reply
		//and other present action link elements
		updateEnabledStatusActionLinks();
	}
	
	/**
	 * Should close all of the dialogs opened from this message
	 */
	public void closeDialogs() {
		//Close the move message dialog if any
		if( moveMessageDialog != null ) {
			moveMessageDialog.hide();
			moveMessageDialog = null;
		}
		//Close the warning message dialog if any
		if( warningDialog != null ) {
			warningDialog.hide();
			warningDialog = null;
		}
		//Close the reply message dialog
		if( replyMessageDialog != null ) {
			replyMessageDialog.hide();
			replyMessageDialog = null;
		}
		//Close the editing message dialog
		if( editMessageDialog != null ) {
			editMessageDialog.hide();
			editMessageDialog = null;
		}
		//Close the delete message warning dialog
		if( deleteForumMessageDialog != null ) {
			deleteForumMessageDialog.hide();
			deleteForumMessageDialog = null;
		}
		//Close the topic message view dialog
		if( viewTopicMessageDialog != null ) {
			viewTopicMessageDialog.hide();
			viewTopicMessageDialog = null;
		}
	}
	
	/**
	 * Allows to adjust the height of the visible part of the message body.
	 * Should be called after the message is displayed, and if message's body
	 * is too long then it adjusts its height with forcing the scroll bar.
	 */
	public void adjustHeight() {
		adjustHeight( CommonResourcesContainer.MAXIMUM_FORUM_MESSAGEBODY_HEIGHT, false );
	}
	
	/**
	 * Allows to adjust the height of the visible part of the message body to the given one.
	 * Should be called after the message is displayed, and if message's body
	 * is too long then it adjusts its height with forcing the scroll bar.
	 * @param maximumHeight the maximum allowed message body height
	 * @param forceIfOpen if true then if the disclosure panel is open, we set
	 * the scroll panel height to be maximumHeight.
	 */
	public void adjustHeight( final int maximumHeight, final boolean forceIfOpen ) {
		if( hasMessageBodyContent() ) {
			if( disclosurePanel.isOpen() ) {
				if( forceIfOpen || ( msgBodyScrollPanel.getOffsetHeight() > maximumHeight ) ) {
					msgBodyScrollPanel.setHeight( maximumHeight + "px");
				}
			}
		}
	}
	
	/**
	 * Allows to adjust the width of the forum message widget, note that his requires adjusting
	 * using a style class, because setting the width of the FlowPanels is not enough, we get an
	 * overflow that has to be somehow fixed, I do it using the overflow:auto; style.
	 * @param widthStyleNameTitle the style name to use for the title flow panels
	 * @param widthStyleNameBody the style to use for the width of the body scroll panel
	 */
	public void adjustWidth( final String widthStyleNameTitle, final String widthStyleNameBody ) {
		messageSubjectContent.addStyleName( widthStyleNameTitle );
		titleInfoPanel.addStyleName( widthStyleNameTitle );
		titleInfoLastRepPanel.addStyleName( widthStyleNameTitle );
		if( hasMessageBodyContent() ) {
			msgBodyScrollPanel.addStyleName( widthStyleNameBody );
		}
	}
	
	/**
	 * Allows to return a proper URL link to the given forum message
	 * @param messageData the forum message to return the link for
	 * @return the properly formed URL for the forum message
	 */
	public static String getForumMessageURL( ForumMessageData messageData ) {
		final ForumSearchData searchData = new ForumSearchData();
		searchData.isOnlyMessage = true;
		searchData.baseMessageID = messageData.messageID;
		return ServerSideAccessManager.getForumMessageServletBasedURL( (new MessageStackElement( searchData, null )).serialize() );
	}
	
	/**
	 * Allows to check if we need to add View message topic action link or not
	 * @return true if we need to add View message topic action link
	 */
	protected abstract boolean isAddViewMessageTopicActionLink();
	
	/**
	 * Allows to retrieve the title for the link for viewing the forum message replies, e.g.: View replies, View topics 
	 * @return the title for the link for viewing the forum message replies
	 */
	protected abstract String getViewForumMessageRepliesLinkText();
	
	/**
	 * Allows to get the proper image for the get forum message replies action link
	 * it depends on whether it is a simple forum message or a topic or section message 
	 */
	protected abstract String getViewForumMessageRepliesURL( final boolean isEnabled );
	
	/**
	 * Allows to detect if we are allowed to have the reply-to action link here
	 * @return true if we are allowed to have the reply-to action link
	 */
	protected abstract boolean isReplyToMessageActionLink();
	
	/**
	 * Allows to detect if we are allowed to have the approve action link here
	 * @return true if we are allowed to have the approve action link
	 */
	protected abstract boolean isApproveMessageActionLink();
	
	/**
	 * Allows to detect if we are allowed to have the delete action link here
	 * @return true if we are allowed to have the delete action link
	 */
	protected abstract boolean isDeleteMessageActionLink();
	
	/**
	 * Allows to detect if we are allowed to have the move action link here
	 * @return true if we are allowed to have the move action link
	 */
	protected abstract boolean isMoveMessageActionLink();
	
	/**
	 * Allows to detect if we are allowed to have the edit action link here
	 * @return true if we are allowed to have the edit action link
	 */
	protected abstract boolean isEditMessageActionLink();
	
	/**
	 * Allows to check if the message has the body content
	 * @return true if the message has the body content that should be displayes
	 */
	protected abstract boolean hasMessageBodyContent();
}
