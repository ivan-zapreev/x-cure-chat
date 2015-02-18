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
package com.xcurechat.client.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlowPanel;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIInfoMessages;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.popup.ShortUserInfoPopupPanel;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.userstatus.UserStatusHelper;
import com.xcurechat.client.utils.MessageTextToFlowPanel;
import com.xcurechat.client.utils.SmileyHandlerUI;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.dialogs.ViewStaticImageDialogUI;
import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;

/**
 * @author zapreevis
 * This class represents the base UI interface for the chat message
 */
public abstract class ChatMessageBaseUI extends Composite {
	
	//The interval in milliseconds after which the appended message will get a prefix with the new time stamp
	private static final long APPENDED_MESSAGE_DATE_NOTIFICATION_INTERVAL_MILLISEC = 60*1000; //Is set to be one minute
	
	protected static final UITitlesI18N i18nTitles = I18NManager.getTitles();
	protected static final UIInfoMessages i18nInfoMsgs = I18NManager.getInfoMessages();
	
	//The Panel that encloses the message content
	private final FlowPanel content = new FlowPanel();
	//The title panel of the message, stored in the message content
	private FocusPanel titlePanel = null;
	//The decorated panel that adds rounded corners and encloses the the message body
	private final DecoratorPanel decoratedPanel = new DecoratorPanel();
	//The simple or focus panel, the latter allows clicking on the message
	private SimplePanel messageContentPanel = null;
	//The id of the room this message is sent to
	private final int roomID;
	//The id of the user for the info message, is needed for technical reasons
	private final int firstMsgInfoUserId;
	//The list of users visible in the room when the message was received
	private final Map<Integer, ShortUserData> visibleUsers;
	//True if the message is minimized, otherwise false, we only minimize user messages
	private boolean isMessageMinimized = false;
	//Current message sending time
	private Date sentDate = new Date();
	//The click handler registration for the messages that have "click to reply" capadilities or null
	private HandlerRegistration clickHandlerRegistration = null;
	
	protected ChatMessageBaseUI( final String leftMessageStyleName,
								 final String rightMessageStyleName,
								 final boolean isLeft, final int roomID, final int infoUserId,
								 final Map<Integer, ShortUserData> visibleUsers,
								 final boolean isWithFocusPanel ) {
		//Store data
		this.roomID = roomID;
		this.firstMsgInfoUserId = infoUserId;
		this.visibleUsers = visibleUsers;
		
		//Initialize the decorated panel wrapper of this UI element
		decoratedPanel.setStyleName( isLeft? leftMessageStyleName : rightMessageStyleName );
		
		content.setWidth("100%");
		if( isWithFocusPanel ) {
			messageContentPanel = new FocusPanel();
			messageContentPanel.addStyleName( CommonResourcesContainer.CHAT_MESSAGE_BODY_CONTENT_FOCUS_STYLE );
		} else {
			messageContentPanel = new SimplePanel();
		}
		messageContentPanel.setSize("100%", "100%");
		messageContentPanel.add( content );
		messageContentPanel.setTitle( "" );
		messageContentPanel.addStyleName( CommonResourcesContainer.CHAT_MESSAGE_BODY_CONTENT_STYLE );
		decoratedPanel.add( messageContentPanel );
		
		//All composites must call initWidget() in their constructors.
		initWidget( decoratedPanel );		
	}
	
	/**
	 * Allows to set the reply message click handler. This method only works if the
	 * class constructor had the isWithFocusPanel parameter set to true.
	 * @param clickToReplyHandler the click handler
	 */
	public void setReplyMessageClickHandler( final ClickHandler clickToReplyHandler ) {
		if( clickToReplyHandler != null && messageContentPanel instanceof FocusPanel ) {
			messageContentPanel.setTitle( I18NManager.getTitles().clickHereToReplyToTheMessage() );
			clickHandlerRegistration = ( (FocusPanel) messageContentPanel ).addClickHandler( clickToReplyHandler );
			messageContentPanel.addStyleName( CommonResourcesContainer.CLICKABLE_PANEL_STYLE );
		}
	}
	
	/**
	 * Allows to remove message click handler for the "Click to reply" messages.
	 * If the click handler is not present, then this method does nothing
	 */
	protected void removeMessageClickHandler() {
		if( clickHandlerRegistration != null ) {
			messageContentPanel.setTitle( "" );
			clickHandlerRegistration.removeHandler();
			clickHandlerRegistration = null;
			messageContentPanel.removeStyleName( CommonResourcesContainer.CLICKABLE_PANEL_STYLE );
		}
	}
	
	/**
	 * Allows to add style name to the content. Note that, if the
	 * style name is null or an empty string then nothing is done
	 * but no error is reported, this is done on purpose.
	 * @param styleName the style name to add or an empty string
	 */
	public void addStyleNameToContent( final String styleName) {
		if( styleName != null && !styleName.trim().equals("") ) {
			content.addStyleName( styleName );
		}
	}
	
	/**
	 * Creates a label with an inline display style
	 * @param text the text to put into the label
	 * @return the resulting label
	 */
	private static Label getInlineDisplayLabel( final String text){
		Label label = new Label(text);
		label.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
		return label;
	}
	
	/**
	 * Adds the panel containing the message title, stores the message sending date
	 * @param date the date we take
	 * @param the chat message type: Simple message/Private message/Error message/Info message
	 * @param isUserMsg true if this is a message sent by a real user
	 * @param message the user message in case isUserMsg is true otherwise null
	 * @return the chat message title panel
	 */
	public FocusPanel addMessageTitlePanel( final Date date, final String messageType,
											final boolean isUserMsg, final ChatMessage message ) {
		final FocusPanel titleFocusPanel = new FocusPanel();
		final FlowPanel messageTitleContent = new FlowPanel();
		messageTitleContent.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
		titleFocusPanel.add( messageTitleContent );
		//Store the message sending date
		sentDate = date;
		//Allow to minimize the message only if it is a user message
		if( isUserMsg ){
			//Make the message title clickable
			titleFocusPanel.addStyleName( CommonResourcesContainer.CLICKABLE_PANEL_STYLE );
			titleFocusPanel.setTitle( i18nTitles.clickMessageTitleToShowHideMessageContentToolTip() );
			titleFocusPanel.addClickHandler( new ClickHandler(){
				List<Widget> storedMessageContent = null;
				@Override
				public void onClick(ClickEvent event) {
					if( isMessageMinimized ) {
						//Restore the message content
						cleanAndRestoreMessageContent( titlePanel, storedMessageContent );
					} else {
						//Retrieve the message content
						storedMessageContent = saveAndCleanMessageContent( titlePanel );
						//Add the minimized message text
						addContentText( i18nTitles.clickMessageTitleToShowTheMessage() );
					}
					isMessageMinimized = !isMessageMinimized;
					//Prevent the event propagation and its default action
					event.preventDefault();
					event.stopPropagation();
				}});
		}
		
		final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_MEDIUM );
		messageTitleContent.add( getInlineDisplayLabel( "[" + dateTimeFormat.format( date ) +
														(messageType.trim().isEmpty()? "": ", " ) + messageType) );
		//If this is a user message and all data is available then we add the "from ..." section
		if( isUserMsg && ( message != null ) && visibleUsers != null ) {
			ShortUserData userData = visibleUsers.get( message.senderID );
			if( userData != null ) {
				messageTitleContent.add( getInlineDisplayLabel( " " + i18nTitles.chatMessageFromTextTitle()+" " ) );
				messageTitleContent.add( getUserLinkLabel( ShortUserData.UNKNOWN_UID, null, userData, roomID, false, true ) );
			}
		}
		messageTitleContent.add( getInlineDisplayLabel( "]" ) );
		titleFocusPanel.addStyleName( CommonResourcesContainer.CHAT_MESSAGE_TITLE_STYLE );
		
		//Add the message title panel widget
		addMessageTitleContentWidget( titleFocusPanel );
		
		return titleFocusPanel;
	}
	
	/**
	 * Adds the message title panel, stores the message sending date
	 * @param message the chat message we take the date from
	 * @param userData short user data or null for an unknown sender
	 * @return the message title panel
	 */
	public FocusPanel addMessageTitlePanel( final ChatMessage message ){
		final String messageType;
		final String styleName;
		final boolean isUserMessage; //If the message is sent by a user but it is not a system message
		switch( message.messageType ){
			case USER_STATUS_CHAGE_INFO_MESSAGE_TYPE:
			case USER_ROOM_LEAVE_INFO_MESSAGE_TYPE:
			case USER_ROOM_ENTER_INFO_MESSAGE_TYPE:
			case ROOM_IS_CLOSING_INFO_MESSAGE_TYPE:
				messageType = i18nTitles.chatMessageTypeInformation();
				isUserMessage = false;
				styleName = CommonResourcesContainer.INFO_MESSAGE_TITLE_STYLE;
				break;
			case SIMPLE_MESSAGE_TYPE:
				messageType = i18nTitles.chatMessageTypeSimple();
				isUserMessage = true;
				styleName = CommonResourcesContainer.SIMPLE_MESSAGE_TITLE_STYLE;
				break;
			case PRIVATE_MESSAGE_TYPE:
				messageType = i18nTitles.chatMessageTypePrivate();
				isUserMessage = true;
				styleName = CommonResourcesContainer.PRIVATE_MESSAGE_TITLE_STYLE;
				break;
			default:
				//Here we assign the unknown message type
				messageType = "Unknown message type";
				isUserMessage = true;
				styleName = CommonResourcesContainer.SIMPLE_MESSAGE_TITLE_STYLE;
		}
		FocusPanel resultPanel = addMessageTitlePanel( message.sentDate, messageType, isUserMessage, message );
		resultPanel.addStyleName( styleName );
		return resultPanel;
	}
	
	/**
	 * Allows to fill the message with the content, i.e. recipients and the message body only
	 * @param message the chat message 
	 */
	protected void addMessageContent( final ChatMessage message,
									   final Map<Integer, ShortUserData> visibleUsers,
									   final int roomID, final boolean isAddRecepients ) {
		switch( message.messageType ){
			case ROOM_IS_CLOSING_INFO_MESSAGE_TYPE:
				//Just add the room-is-closing info message
				addMessageTextContent( i18nInfoMsgs.chatInfoMsgRoomIsClosing() );
				break;
				
			case USER_STATUS_CHAGE_INFO_MESSAGE_TYPE:
				//Just add the user-status-change info message
				addMessageInfoContent( message, visibleUsers );
				break;
				
			case USER_ROOM_ENTER_INFO_MESSAGE_TYPE:
			case USER_ROOM_LEAVE_INFO_MESSAGE_TYPE:
				//Add the user-room-enter/leave message
				addMessageRoomEnterLeaveContent( message, visibleUsers );
				break;
				
			default:
				//Add message recipients, if needed
				if( isAddRecepients ) {
					addMessageRecepients( message.recipientIDs, visibleUsers );
				}
				//Add message body
				addMessageTextContent( message.messageBody );
				//Add message image, with a hover over enlarge effect
				addMessageFileContent( roomID, message.fileDesc );
				break;
		}
	}
	
	/**
	 * Allows to append the given message data to the current message UI.
	 * WARNING: This method just appends the data, it does not check if this is allowed or not!
	 * WARNING: Do not append a new message data to a minimized message or to a message
	 * of a different type or with different recipients!
	 * @param message the message data to be appended
	 * @param visibleUsers the list of visible users
	 * @param roomID the id of the room
	 */
	public void appendMessageContent( final ChatMessage message,
			   						  final Map<Integer, ShortUserData> visibleUsers,
			   						  final int roomID ) {
		if( message != null ) {
			//If this is a status change message then add a delimiter
			//and remove the click handler in case the status change
			//is for another user
			if( message.messageType == ChatMessage.Types.USER_STATUS_CHAGE_INFO_MESSAGE_TYPE ) {
				addMessageTextContent( "|" );
				if( message.infoUserID != firstMsgInfoUserId ) {
					removeMessageClickHandler();
				}
			}
			//Make sure the message we are appending is not null
			if( ( sentDate == null ) ||
			    ( ( message.sentDate.getTime() - sentDate.getTime() ) > APPENDED_MESSAGE_DATE_NOTIFICATION_INTERVAL_MILLISEC ) ) {
				//Make a note about the additional message time if it is more than a certain period of time older
				final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_MEDIUM );
				final Label timeStampLabel = new Label( "[" + dateTimeFormat.format( message.sentDate ) + "]" );
				timeStampLabel.addStyleName( CommonResourcesContainer.CHAT_MESSAGE_TITLE_STYLE );
				addMessageContentWidget( timeStampLabel );
				//Update the message sending date
				sentDate = message.sentDate;
			}
			//Append the message content without the recipients
			addMessageContent( message, visibleUsers, roomID, false );
		}
	}
	
	/**
	 * Allows to detect if the given message is currently in the minimized mode or not
	 * @return true if it is in the minimized mode.
	 */
	public boolean isMessageMinimized() {
		return isMessageMinimized;
	}
	
	/**
	 * Allows to add the content for the room enter/leave message, i.e.
	 * of type ChatMessage.USER_ROOM_ENTER_INFO_MESSAGE_TYPE
	 * or ChatMessage.USER_ROOM_LEAVE_INFO_MESSAGE_TYPE
	 * @param message the room enter/leave message
	 * @param visibleUsers the list of visible users
	 */
	protected void addMessageRoomEnterLeaveContent( final ChatMessage message,
			   										final Map<Integer, ShortUserData> visibleUsers ) {
		if( ( message.messageType == ChatMessage.Types.USER_ROOM_ENTER_INFO_MESSAGE_TYPE ) ||
			( message.messageType == ChatMessage.Types.USER_ROOM_LEAVE_INFO_MESSAGE_TYPE ) ) {
			addMessageContentWidget( getUserLinkLabel( message.infoUserID, message.infoUserLogin,
										  		visibleUsers.get( message.infoUserID ), roomID, true ) );
			addContentText( message.messageType == ChatMessage.Types.USER_ROOM_ENTER_INFO_MESSAGE_TYPE ?
							i18nInfoMsgs.chatInfoMsgUserRoomEnter( ) : i18nInfoMsgs.chatInfoMsgUserRoomLeave( ) );
		}
	}
	
	/**
	 * Allows to add the content for the info message, i.e. of type ChatMessage.USER_STATUS_CHAGE_INFO_MESSAGE_TYPE
	 * @param message the info message
	 * @param visibleUsers the list of visible users
	 */
	protected void addMessageInfoContent( final ChatMessage message,
			   						  	   final Map<Integer, ShortUserData> visibleUsers ) {
		if( message.messageType == ChatMessage.Types.USER_STATUS_CHAGE_INFO_MESSAGE_TYPE ) {
			addMessageContentWidget( getUserLinkLabel( message.infoUserID, message.infoUserLogin,
							  visibleUsers.get( message.infoUserID ), roomID, true ) );
			final String statusName = UserStatusHelper.getUserStatusString( message.messageBody );
			if( statusName == null ) {
				addContentText( i18nInfoMsgs.getUserStatusChangedInfoMessage( ) );
			} else {
				addContentText( i18nInfoMsgs.getUserStatusChangedToInfoMessage());
				final Label statusLabel = new Label( statusName );
				statusLabel.setStyleName( CommonResourcesContainer.USER_STATUS_LABEL_STYLE_NAME );
				addMessageContentWidget( statusLabel );
			}
		}
	}
	
	/**
	 * Allows to add message recipients to the message body
	 * @param recipientIDs the list of message recipient ids
	 * @param visibleUsers the map of visible users
	 */
	protected void addMessageRecepients( final LinkedHashSet<Integer> recipientIDs,
			   							 final Map<Integer, ShortUserData> visibleUsers ) {
		if( recipientIDs != null ) {
			Iterator<Integer> recepientsIter = recipientIDs.iterator();
			boolean isFirstRecepient = true;
			while( recepientsIter.hasNext() ) {
				ShortUserData userData = visibleUsers.get( recepientsIter.next() );
				if( userData != null ) {
					//If the recipient is present in the room then we add it to the message
					addMessageContentWidget( getUserLinkLabel( ShortUserData.UNKNOWN_UID, null, userData, roomID, isFirstRecepient ) );
					addContentText(",", false);
				}
				isFirstRecepient = false;
			}
		}
	}
	
	/**
	 * Allows to create the file thumbnail with the click listener
	 * @param thumbnailURL the url to the thumbnail of the image
	 * @param originalURL the url of the original image if the chat-message file is an image given by its URL
	 * @param the id of the room this file came from, is needed if the fileDesc is not null
	 * @param fileDesc the chat-message file descriptor or null if we want to show an image given by its URL
	 */
	protected static FocusPanel createChatFileThumbnail( final String thumbnailURL, final String originalURL, final int roomID, final ShortFileDescriptor fileDesc ) {
		FocusPanel actionImageOpenPanel = new FocusPanel();
		actionImageOpenPanel.addStyleName( CommonResourcesContainer.ATTACHED_USER_IMAGE_THUMBNAIL_STYLE );
		actionImageOpenPanel.addStyleName( CommonResourcesContainer.ZOOME_IN_IMAGE_STYLE );
		actionImageOpenPanel.setTitle( i18nTitles.clickToViewToolTip() );
		actionImageOpenPanel.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Ensure lazy loading
				(new SplitLoad( true ) {
					@Override
					public void execute() {
						//Open the file view dialog
						DialogBox dialog;  
						if( originalURL != null ) {
							dialog = new ViewStaticImageDialogUI( i18nTitles.chatMessageFileViewDialogTitle( ), originalURL, null );
						} else {
							dialog = new ViewChatMediaFileDialogUI( roomID, fileDesc, true, null );
						}
						dialog.show();
						dialog.center();
					}
				}).loadAndExecute();
				
				//Stop the event from being propagated
				event.stopPropagation();
			}
		});
		
		Image imageThumbStyled = new Image( thumbnailURL );
		final String imageThumbString = imageThumbStyled.getElement().getString();
		actionImageOpenPanel.getElement().setInnerHTML( imageThumbString + "<span>" + imageThumbString+ "</span>");
		
		return actionImageOpenPanel;
	}
	
	/**
	 * Allows to add the chat message file to the chat message content if the file is set
	 * @param roomID the id of the room the message appears in
	 * @param fileDesc the descriptor of the attached file or null
	 */
	protected void addMessageFileContent( final int roomID, final ShortFileDescriptor fileDesc ) {
		if( fileDesc != null ) {
			addMessageContentWidget( createChatFileThumbnail( ServerSideAccessManager.getChatMessageFileURL(roomID, fileDesc, true, true), null, roomID, fileDesc ) );
		}
	}
	
	/**
	 * Adds a widget to the message content, makes sure that the widget has an inline display
	 * @param w the widget to add
	 * @param addSpacing if true then adds spacing between the old widgets and the new one
	 */
	protected void addContentWidget( final Widget w, final boolean addSpacing ) {
		MessageTextToFlowPanel.addContentWidget( content, w, addSpacing );
	}
	
	/**
	 * Allows to restore the message content from the list of widgets,
	 * note that the title panel is then added in some other place.
	 * @param titlePanel the title panel that should be in the message content
	 * @param contentWidgets the list of widgets to place after the message title panel
	 */
	protected void cleanAndRestoreMessageContent( final FocusPanel titlePanel, List<Widget> contentWidgets ) {
		//First clean the current message content
		content.clear();
		
		//Put back the message title. Do not add spaces,
		//we already  have them in the stored message content.
		if( titlePanel != null ) {
			addContentWidget( titlePanel, false );
		}
		
		//Put back the widgets from the list
		if( contentWidgets != null ) {
			for(int index = 0; index < contentWidgets.size(); index++) {
				content.add( contentWidgets.get( index ) );
			}
		}
	}
	
	/**
	 * Allows to get all the message content widgets except for
	 * the title widget and then clean the message content.
	 * The title panel, if present, remains in the message content.
	 * @param titlePanel the title panel or null if it is not needed
	 */
	protected List<Widget> saveAndCleanMessageContent( final FocusPanel titlePanel ) {
		List<Widget> result = new ArrayList<Widget>();
		
		//If the title panel should be there then try to remove it
		if( titlePanel != null ) {
			content.remove( titlePanel );
		}
		
		//Copy all of the widgets to the list
		for(int index = 0; index < content.getWidgetCount(); index++) {
			result.add( content.getWidget( index ) );
		}
		
		//Clear the message content
		content.clear();
		
		//Put back the message title panel
		if( titlePanel != null ) {
			addMessageContentWidget( titlePanel );
		}
		
		return result;
	}
	
	/**
	 * Adds a widget to the message content, makes sure that the widget has an inline display
	 * @param w the widget to add
	 */
	protected void addMessageContentWidget( final Widget w ) {
		addContentWidget( w, true );
	}
	
	/**
	 * Must be used to add the message title widget to the content panel
	 * @param titlePanel the title panel to add
	 */
	protected void addMessageTitleContentWidget(final FocusPanel titlePanel) {
		this.titlePanel = titlePanel;
		addMessageContentWidget( titlePanel );
	}
	
	/**
	 * Adds a widget to the message content, makes sure that the widget has an inline display
	 * @param w the widget to add
	 * @param addSpacing if true then adds spacing between the old widgets and the new one
	 */
	private void addContentText( final String text, final boolean addSpacing ) {
		addContentWidget( new Label( text ), addSpacing );
	}	
	
	/**
	 * Adds a widget to the message content, makes sure that the widget has an inline display
	 * @param w the widget to add
	 */
	private void addContentText( final String text ) {
		addContentWidget( new Label( text ), true );
	}
	
	/**
	 * Allows to add the message body to the chat message. Here we
	 * parse the message to sort out smiles.
	 * @param messageBody the original chat message body (its text)
	 */
	protected void addMessageTextContent( final String messageBody ) {
		String currentMessageBody = messageBody;
		if( ( currentMessageBody != null ) &&
			( currentMessageBody.trim().equals( i18nTitles.clickMessageTitleToShowTheMessage() ) ) ) {
			//Prevent fraud, call the sender funny :)
			currentMessageBody = i18nTitles.hahahaIamAFunnyGuyMsg();
		}
		List<Widget> messageWidgets = SmileyHandlerUI.getMessageViewObject( currentMessageBody, ChatMessage.MAX_ONE_WORD_LENGTH, false );
		for( int i = 0; i < messageWidgets.size(); i++) {
			addMessageContentWidget( messageWidgets.get( i ) );
		}
	}
	
	/**
	 * Create a user short info or profile link. If userData == null
	 * then we open the user profile but not the short user view pop up
	 * @param userID the id of the user 
	 * @param userLoginName the login name of the user
	 * @param userData the short user data or null.
	 * @param roomID the id of the room we process messages for
	 * @param isImportant if true then the user link has an important link style
	 * @return the label link with the click handler
	 */
	public static Label getUserLinkLabel( final int userID, final String userLoginName,
										  final ShortUserData userData, final int roomID,
										  final boolean isImportant ) {
		return getUserLinkLabel( userID, userLoginName, userData, roomID, isImportant, false );
	}
	
	/**
	 * Create a user short info or profile link. If userData == null
	 * then we open the user profile but not the short user view pop up
	 * @param userID the id of the user 
	 * @param userLoginName the login name of the user
	 * @param userData the short user data or null.
	 * @param roomID the id of the room we process messages for
	 * @param isImportant if true then the user link has an important link style
	 * @param isInlineDisplay true is the message has to be diplayed inline
	 * @return the label link with the click handler
	 */
	public static Label getUserLinkLabel( final int userID, final String userLoginName,
										  final ShortUserData userData, final int roomID,
										  final boolean isImportant, final boolean isInlineDisplay ) {
		final String localUserLoginName = (userData != null ? userData.getUserLoginName() : userLoginName );
		final Label result = new Label( ShortUserData.getShortLoginName( localUserLoginName ) );
		if( isInlineDisplay ) {
			result.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
		}
		result.setTitle( localUserLoginName );
		if( isImportant ) {
			result.addStyleName( CommonResourcesContainer.DIALOG_LINK_IMP_STYLE );
		} else {
			result.addStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
		}
		
		//Add the user login name click handler
		result.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( userData != null ) {
					//Ensure lazy loading
					( new SplitLoad( true ) {
						@Override
						public void execute() {
							ShortUserInfoPopupPanel.openShortUserViewPopup(userData, roomID, result);
						}
					}).loadAndExecute();
				} else {
					//Ensure lazy loading
					( new SplitLoad( true ) {
						@Override
						public void execute() {
							ViewUserProfileDialogUI dialog = new ViewUserProfileDialogUI( userID, userLoginName, null, false );
							dialog.show();
							dialog.center();
						}
					}).loadAndExecute();
				}
				//Prevent the event from being propagated and prevent default
				event.stopPropagation();
				event.preventDefault();
			}
		});
		
		return result;
	}
}
