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
package com.xcurechat.client.chat;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;

//import com.google.gwt.user.client.Window;
//import com.google.gwt.event.logical.shared.ResizeHandler;
//import com.google.gwt.event.logical.shared.ResizeEvent;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedStackPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.chat.messages.SendChatMessageManager;
import com.xcurechat.client.chat.messages.SendChatMessagePanelUI;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.tree.RoomTreeItem;
import com.xcurechat.client.tree.UserTreeItem;
import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.StringUtils;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.RoomAccessException;

/**
 * @author zapreevis
 * The User interface representation of the chat room. This is a composite widget. 
 */
@SuppressWarnings("deprecation")
public class ChatRoomUI extends Composite implements UserAvatarImageWidget.AvatarSpoilerChangeListener {
	
	//The extra margin for the Flex table storing the messages panel and the send-chat-message panel
	private static final int EXTRA_HEIGHT_MARGIN_SAFARI = 10;
	private static final int EXTRA_HEIGHT_MARGIN_CHROME = 10;
	private static final int EXTRA_HEIGHT_MARGIN_FIREFOX = 8;
	private static final int EXTRA_HEIGHT_MARGIN_OTHERS = 2;
	
	//The internationalization object reference
	private static final UITitlesI18N i18NTitles = I18NManager.getTitles();
	
	//70 is approximately the height of the decorations
	//of the stack panel due to 2 (24x24) images +
	//2 (1x7) borders + 8 pixels for margins etc
	private static final int DECORATIONS_OS_STACK_PANEL = 70;
	
	//This tree will contain users and rooms, one for all opened rooms
	private static Tree theRoomsTree = new Tree();
	
	//The current number of my rooms
	private static int numberOfMyRooms = 0;
	//The current number of other's rooms
	private static int numberOfOthersRooms = 0;
	
	//Stores the currently open chat room UI
	public static ChatRoomUI theCurrentChatRoomUI = null;
	
	static {
		//Do some basic initialization for the rooms tree
		theRoomsTree.setAnimationEnabled(true);
	}
	
	//This is the panel that will store the rooms tree in a room instance
	private SimplePanel theRoomsTreePanel = new SimplePanel(); 
	//The simple panel storing the room visitors tree
	private SimplePanel theUsersTreePanel = new SimplePanel();

	//The data object of this room
	private ChatRoomData theChatRoomData;
	
	//This This panel splits the user and room trees
	private DecoratedStackPanel thePeopleRoomsVPanel = new DecoratedStackPanel();
	//Splits the chat messages from the tree with the users/rooms
	private HorizontalSplitPanel theMsgUsrRoomHPanel = new HorizontalSplitPanel();
	//The vertical panel with the chat messages
	private final ChatMessagesPanelUI theMsgsPanel;
	//The table containing the messages panel and the send-chat-message panel UI
	private final FlexTable mainTable = new FlexTable();
	//The room visitor's tree reference
	private Tree theUsersTree = new Tree();
	//The room's tab title widget
	private final OpenedRoomTitlePanel roomTabTitleWidget;
	//The users panel widget index
	private int usersPanelWidgetIntex;
	
	//The instance of the rooms manager
	private final RoomsManagerUI roomsManager;
	
	//The set of visible users residing in the room
	private Map<Integer, ShortUserData> visibleUsers = new HashMap<Integer, ShortUserData>();
	
	/**
	 * The simple constructor that is initialized with the ChatRoomData object.
	 * @param theChatRoomData the initialization data for the chat room
	 * @param roomTitleLabel the title label used in the room tab title
	 * @param roomsManager the instance of the rooms manager
	 */
	public ChatRoomUI(final ChatRoomData theChatRoomData, final OpenedRoomTitlePanel roomTabTitleWidget, final RoomsManagerUI roomsManager ) {
		//Store the rooms manager
		this.roomsManager = roomsManager;
		//Store the room's title widget
		this.roomTabTitleWidget = roomTabTitleWidget;
		//Create the chat messages panel for this chat room UI representation
		this.theMsgsPanel = new ChatMessagesPanelUI( theChatRoomData.getRoomID() );
		
		//Update the room's data, resetting the label value if needed
		updateRoomData( theChatRoomData );
		
		//All composites must call initWidget() in their constructors.
		initWidget(theMsgUsrRoomHPanel);
		
		//Create and relate the UI elements
		constructUIObjects();
	}
	
	/**
	 * Allows to scroll down the messages panel to show the latest messages
	 */
	public void scrollMessagesPanelDown() {
		theMsgsPanel.scrollDownMessages();
	}
	
	/**
	 * Allows to detect whether the tab is selected or not
	 * @return true if the tab is selected, otherwise false
	 */
	public boolean isTabSelected() {
		return this.roomTabTitleWidget.isTabSelected();
	}
	
	/**
	 * Allows to set the chat room's data, on the update from the server
	 * If the room's title is changed then the title label is updated
	 * @param roomData the new chat room data
	 */
	public void updateRoomData( final ChatRoomData roomData ) {
		if( roomData != null ) {
			theChatRoomData = roomData;
			roomTabTitleWidget.setLocalRoomData( theChatRoomData );
		}
	}
	
	/**
	 * @return the chat room data object
	 */
	public ChatRoomData getChatRoomData() {
		return theChatRoomData;
	}
	
	//The indexes for the FlexTable with the messages panel and the send-chat-message panel UI
	private final int CHAT_MESSAGES_ROW_IDX = 0;
	private final int CHAT_MESSAGES_CELL_IDX = 0;
	private final int SEND_CHAT_MESSAGE_PANEL_ROW_IDX = 1;
	private final int SEND_CHAT_MESSAGE_PANEL_IDX = 0;
	
	private void constructUIObjects() {
		thePeopleRoomsVPanel.setStyleName( CommonResourcesContainer.ROOMS_VS_USERS_DECORATED_STACK_PANEL_STYLE );
		theRoomsTreePanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		theUsersTreePanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		
		//theRoomsTreePanel.setSize("100%", "100%");
		thePeopleRoomsVPanel.add(theRoomsTreePanel);
		//thePeopleRoomsVPanel.setSize("100%", "100%");
		thePeopleRoomsVPanel.add( theUsersTreePanel );
		usersPanelWidgetIntex = thePeopleRoomsVPanel.getWidgetIndex( theUsersTreePanel ); 
		
		//Set the people stack open
		thePeopleRoomsVPanel.showStack( thePeopleRoomsVPanel.getWidgetIndex( theUsersTreePanel ) );
		
		//Set the sizes of the horizontal split panel
		theMsgUsrRoomHPanel.setSize( "100%", "100%" );
		//Populate the panels
		mainTable.setSize("100%", "100%");
		mainTable.setStyleName( CommonResourcesContainer.MAIN_CHAT_ROOM_TABLE_PANEL );
		mainTable.insertRow(CHAT_MESSAGES_ROW_IDX);
		mainTable.insertCell(CHAT_MESSAGES_ROW_IDX, CHAT_MESSAGES_CELL_IDX);
		mainTable.getCellFormatter().setAlignment( CHAT_MESSAGES_ROW_IDX,  CHAT_MESSAGES_CELL_IDX,
													HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_BOTTOM);
		mainTable.setWidget(CHAT_MESSAGES_ROW_IDX, CHAT_MESSAGES_CELL_IDX, theMsgsPanel);
		//Add the bottom cell with the send-chat-message widget
		mainTable.insertRow(SEND_CHAT_MESSAGE_PANEL_ROW_IDX);
		mainTable.insertCell(SEND_CHAT_MESSAGE_PANEL_ROW_IDX, SEND_CHAT_MESSAGE_PANEL_IDX);
		mainTable.getCellFormatter().setAlignment( SEND_CHAT_MESSAGE_PANEL_ROW_IDX,  SEND_CHAT_MESSAGE_PANEL_IDX,
													HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		
		theMsgUsrRoomHPanel.setLeftWidget( mainTable );
		theMsgUsrRoomHPanel.setRightWidget( thePeopleRoomsVPanel );
		updateUIElements();
		
		//NOTE: This is now done from the RoomsManagerUI and the SiteNavigator
		//This listener is needed to keep the element's size in synch when the window is resized
		/*Window.addResizeHandler( new ResizeHandler(){
			public void onResize(ResizeEvent e){
				updateUIElements();
			}
		});*/
	}
	
	/**
	 * Tries to update the panels storing the tree in such a way that we get scrolling inside
	 */
	private void updateTeesHeight() {
		//If we manage to get height > 0 then we computed the
		//size for the scroll panel of the tree items correctly
		//If not then there will be no scrolling, just set the
		//heights to 100% there is nothing more we can do here
		final int height = this.getOffsetHeight() - DECORATIONS_OS_STACK_PANEL;
		if( height > 0 ) {
			theRoomsTreePanel.setHeight(height+"px");
			theUsersTreePanel.setHeight(height+"px");
		} else {
			theRoomsTreePanel.setHeight("100%");
			theUsersTreePanel.setHeight("100%");
		}
	}
	
	//This is the minimal height of the cell dedicated for the send-chat-message panel
	//WARNING: IE8 does not like 0-heights this is why we set it to 1 pixel!!!
	final static int MINIMAL_SEND_CHAT_MESSAGE_PANEL_HEIGHT = 1;
	
	/**
	 * Updates some of the UI elements, e.g. resets the position
	 * of the horizontal splitter and scrolls down the messages.
	 */
	public void updateUIElements() {
		//Update the height
		final int suggestedHeight = InterfaceUtils.suggestMainViewHeight( theMsgUsrRoomHPanel, 95);
		theMsgUsrRoomHPanel.setHeight( suggestedHeight + "px");
		//Update the splitter position
		theMsgUsrRoomHPanel.setSplitPosition("82%");
		//Set the tree's heights
		updateTeesHeight();
		//Update the main table cell sizes
		final SendChatMessagePanelUI instance = (SendChatMessagePanelUI) mainTable.getWidget(SEND_CHAT_MESSAGE_PANEL_ROW_IDX, SEND_CHAT_MESSAGE_PANEL_IDX);
		//Here SEND_CHAT_PANEL_DECORATIONS_WIDTH is the width of the decorations around the send-chat-message-panel in pixels
		final int heightSendPanel = (instance != null && instance.isVisible()) ? instance.getOffsetHeight(): MINIMAL_SEND_CHAT_MESSAGE_PANEL_HEIGHT ;
		theMsgsPanel.removeStyleName( CommonResourcesContainer.CHAT_MESSAGES_PANEL_BOTTOM_LINE );
		if( heightSendPanel > MINIMAL_SEND_CHAT_MESSAGE_PANEL_HEIGHT ) {
			theMsgsPanel.addStyleName( CommonResourcesContainer.CHAT_MESSAGES_PANEL_BOTTOM_LINE );
		}
		//Different browsers need different extra margins
		final int extraMargin;
		if( BrowserDetect.getBrowserDetect().isChrome() ) {
			extraMargin = EXTRA_HEIGHT_MARGIN_CHROME;
		} else {
			if( BrowserDetect.getBrowserDetect().isSafari() ) {
				extraMargin = EXTRA_HEIGHT_MARGIN_SAFARI;
			} else {
				if( BrowserDetect.getBrowserDetect().isFirefox() ) {
					extraMargin = EXTRA_HEIGHT_MARGIN_FIREFOX;
				} else {
					extraMargin = EXTRA_HEIGHT_MARGIN_OTHERS;
				}
			}
		}
		mainTable.getCellFormatter().setHeight(CHAT_MESSAGES_ROW_IDX, CHAT_MESSAGES_CELL_IDX, (suggestedHeight - extraMargin - heightSendPanel) + "px" );
		mainTable.getCellFormatter().setHeight(SEND_CHAT_MESSAGE_PANEL_ROW_IDX, SEND_CHAT_MESSAGE_PANEL_IDX, heightSendPanel + "px" );
		//Scroll down the messages
		theMsgsPanel.scrollDownMessages();
	}
	
	/**
	 * @return the currently set list of room's visible users
	 */
	public Map<Integer, ShortUserData> getRoomVisibleUsers() {
		return visibleUsers;
	}
	
	/**
	 * Allows to update the chat room's data with the new data from the server
	 * @param exception the exception that occurs while getting the room update or null 
	 * @param currentVisibleUsers the set of short user data of the currently visible room users
	 * @param newMsgs the list of new messages to be appended to the existing ones
	 */
	public void updateRoomActualData( SiteException exception, Map<Integer, ShortUserData> currentVisibleUsers, List<ChatMessage> newMsgs) {
		if( exception == null ) {
			//If there is a update for users
			if( currentVisibleUsers != null ) {
				//Update the list of visible users
				visibleUsers = currentVisibleUsers;
				updatePeopleTree();
			}
			//If there are new messages
			if( ( newMsgs != null ) && ( newMsgs.size() > 0 ) ) {
				if( ! roomTabTitleWidget.isTabSelected() ) {
					//Set the new message status for the info image
					roomTabTitleWidget.setTitleInfoImageType(false);
				}
				theMsgsPanel.addNewRoomMessages( newMsgs, visibleUsers );
			}
		} else {
			//Report the exception
			appendRoomErrorMessage( exception );
		}
	}
	
	/**
	 * Allows to report an error into the chat room.
	 * If the exception is not an instance of the RoomAccessException
	 * then it opens the error dialog, otherwise it appends a new
	 * error message to the list of the chat room messages
	 * @param exception the exception to report
	 */
	public void appendRoomErrorMessage( final Throwable exception) {
		//If the is an error
		if( exception instanceof RoomAccessException ) {
			if( ! roomTabTitleWidget.isTabSelected() ) {
				//Set the new message status for the info image
				roomTabTitleWidget.setTitleInfoImageType(false);
			}
			//Put an error message inside the chat
			theMsgsPanel.addRoomErrorMessage( (RoomAccessException) exception, theChatRoomData );
		} else {
			(new SplitLoad( ) {
				@Override
				public void execute() {
					//NOTE: This should not be happening, ever
					ErrorMessagesDialogUI.openErrorDialog( exception );
				}
			}).loadAndExecute();
		}
	}
	
	public static final String FRIENDS_TREE_ITEM_TEXT = I18NManager.getTitles().friendsSubTreeTitle();
	public static final String OTHER_USERS_TREE_ITEM_TEXT = I18NManager.getTitles().othersSubTreeTitle();
	
	/**
	 * Updates the tree of visible room visitors. This is done only
	 * if the room's tab is currently visible.
	 */
	private void updatePeopleTree() {
		//Update the tree only if it is visible, because otherwise GWT sometimes breaks down and
		//the user's tree gets messed up every now and again, not too often but still unpleasant.
		if( roomTabTitleWidget.isTabSelected() && theUsersTreePanel.isVisible() ) {
			//Save the old root items state if any
			boolean oldFriendsState = getItemState( theUsersTree, FRIENDS_TREE_ITEM_TEXT );
			boolean oldOthersState = getItemState( theUsersTree, OTHER_USERS_TREE_ITEM_TEXT );
			
			//The tree items that contain user's friends and other users that are inside the chat room
			TreeItem friendsTreeItem = new TreeItem( FRIENDS_TREE_ITEM_TEXT );
			TreeItem otherTreeItem = new TreeItem( OTHER_USERS_TREE_ITEM_TEXT );
			//Add new elements
			Iterator<ShortUserData> usersIter = visibleUsers.values().iterator();
			boolean isThisUserPresent = false;
			final int thisUserID = SiteManager.getUserID();
			int numFriends = 0;
			int numOthers = 0;
			while( usersIter.hasNext() ){
				final ShortUserData userData = usersIter.next();
				final int userID = userData.getUID();
				isThisUserPresent = isThisUserPresent || ( thisUserID == userID );
				if( SiteManager.isFriend( userID ) ) {
					friendsTreeItem.addItem( new UserTreeItem( userData, theChatRoomData.getRoomID() ) );
					numFriends++;
				} else {
					otherTreeItem.addItem( new UserTreeItem( userData, theChatRoomData.getRoomID() ) );
					numOthers++;
				}
			}
			
			//If the user is not visible, i.e. is not in the set: 
			//visibleUsers. Then add hims as an invisible user
			if( !isThisUserPresent ) {
				otherTreeItem.addItem( new UserTreeItem( SiteManager.getUserLoginName() ) );
				numOthers++;
			}
			
			//Restore the item's states
			friendsTreeItem.setState( oldFriendsState, false );
			otherTreeItem.setState( oldOthersState, false );
			
			//Clear the tree elements, this way we reduce 
			//the memory leaks occurring in Firefox 3.0.11
			theUsersTree.clear();
			//Remove the tree from the panel, we do not use
			//theUsersTreePanel.remove(theUsersTree) because
			//in some cases Firefox crashes and the old tree
			//does not get removed, after that nothing works
			theUsersTreePanel.clear();
			
			//Fill the user's tree again
			theUsersTree.setAnimationEnabled( true );
			if( friendsTreeItem.getChildCount() != 0 ) {
				theUsersTree.addItem( friendsTreeItem );
			}
			if( otherTreeItem.getChildCount() != 0 ) {
				theUsersTree.addItem( otherTreeItem );
			}
			//Add the tree back
			theUsersTreePanel.add( theUsersTree );
			
			//Update the title
			updateCurrentUsersTreePanelTitle( numFriends, numOthers );
		}
	}
	
	/**
	 * Allows to update the number of users in the title of the disclosure panel of the currently open chat room
	 * @param friends
	 * @param others
	 */
	private void updateCurrentUsersTreePanelTitle( final int friends, final int others ) {
		String headerHtmlString = StringUtils.getHeaderString( i18NTitles.peoplePanelTitle( friends, others ),
																ServerSideAccessManager.getUsersTreeImageURL() );
		thePeopleRoomsVPanel.setStackText(usersPanelWidgetIntex, headerHtmlString, true);
	}
	
	/**
	 * This method should and is called before the room's tab gets selected.
	 * It enables the room tab title's close button and  adds the static room
	 * tree to the local rooms tree panel. 
	 */
	public void onBeforeTabSelected() {
		//Enable the close button
		roomTabTitleWidget.setRoomTabTitleSelected( true );
		//Just a safety check, because we might be adding the same
		//tree to the panel twice when it is already there, the latter
		//causes a runtime exception in the java script
		if( theRoomsTreePanel.getWidget() != theRoomsTree ) {
			theRoomsTreePanel.add( theRoomsTree );
		}
		//Set the new active chat room UI
		theCurrentChatRoomUI = this;
		//Update the number of rooms in the disclosure panel
		updateCurrentRoomsTreePanelTitle();
		
		//Put the send chat message widget in
		if( mainTable.getWidget(SEND_CHAT_MESSAGE_PANEL_ROW_IDX, SEND_CHAT_MESSAGE_PANEL_IDX) == null ) {
			SendChatMessagePanelUI instance = SendChatMessageManager.getInstance().getSendChatMessagePanelUI( theChatRoomData.getRoomID(), roomsManager );
			mainTable.setWidget(SEND_CHAT_MESSAGE_PANEL_ROW_IDX, SEND_CHAT_MESSAGE_PANEL_IDX, instance);
		}
		//Notify the SendChatMessageManager that we are switching to the new room
		SendChatMessageManager.getInstance().onRoomChangeSendDialogModification( theChatRoomData.getRoomID() );
	}
	
	/**
	 * This method should and is called before the room's tab gets unselected.
	 * It disables the room tab title's close button. 
	 */
	public void onBeforeTabUnselected() {
		//Disable the close button
		roomTabTitleWidget.setRoomTabTitleSelected( false );
		//Set the info status for the info image
		roomTabTitleWidget.setTitleInfoImageType(true);
	}
	
	/**
	 * Allows to retrieve the tree item by name
	 * @param tree the tree to take the item from
	 * @param itemText the text of the item we want to get the sate of
	 * @return the tree item with the given text, or null if there is no such item
	 */
	private static TreeItem getTreeItem( final Tree tree, final String itemText ) {
		if( tree != null ) {
			for( int i = 0; i < tree.getItemCount(); i++ ) {
				TreeItem oldTreeItem = tree.getItem( i );
				if( oldTreeItem.getText().equals( itemText ) ) {
					return oldTreeItem;
				}
			}
		}
		return null;
	}
	
	/**
	 * Allows to retrieve the tree item state
	 * @param tree the tree to take the item from
	 * @param itemText the text of the item we want to get the sate of
	 * @return true if the item does not exist, or it exists and its state is true
	 */
	private static boolean getItemState( final Tree tree, final String itemText ) {
		TreeItem treeItem = getTreeItem( tree, itemText );
		if( treeItem != null ) {
			return treeItem.getState();
		}
		return true;
	}
	
	private static void updateActiveRoomVisitorsInTreeItem( Map<Integer, Integer> activeRoomVisitors, TreeItem setOfActiveRooms ){
		if( setOfActiveRooms != null ) {
			for( int index = 0 ; index < setOfActiveRooms.getChildCount(); index++ ) {
				( (RoomTreeItem) setOfActiveRooms.getChild( index ).getWidget() ).setRoomVisitors( activeRoomVisitors );
			}
		}
	}
	
	//The mapping between room IDs and the number of users in these rooms
	private static Map<Integer, Integer> activeRoomVisitors = new HashMap<Integer,Integer>();
	
	public static final String MY_ROOMS_TREE_ITEM_TEXT = I18NManager.getTitles().myRoomsTreeTitle();
	public static final String OTHERS_ROOMS_TREE_ITEM_TEXT = I18NManager.getTitles().otherRoomsTreeTitle();
	
	/**
	 * Allows to update the static instance of the rooms tree
	 * @param newActiveRoomVisitors the mapping between the room IDs and the number of room visitors
	 */
	public static void updateActiveRoomVisitors( Map<Integer, Integer> newActiveRoomVisitors ) {
		activeRoomVisitors = newActiveRoomVisitors;
		updateActiveRoomVisitorsInTreeItem( activeRoomVisitors, getTreeItem( theRoomsTree, MY_ROOMS_TREE_ITEM_TEXT ) );
		updateActiveRoomVisitorsInTreeItem( activeRoomVisitors, getTreeItem( theRoomsTree, OTHERS_ROOMS_TREE_ITEM_TEXT ) );
	}
	
	/**
	 * Allows to update the static instance of the rooms tree
	 * @param availableRooms the new rooms to update with
	 * @param roomsManager the instance of the rooms manager
	 */
	public static void updateRoomsTree( Map<Integer, ChatRoomData> availableRooms, final RoomsManagerUI roomsManager ){
		Collection<ChatRoomData> rooms = (Collection<ChatRoomData>) availableRooms.values();
		Iterator<ChatRoomData> roomsIter = rooms.iterator();
		
		//Save the old root items state if any
		boolean oldMyRoomsState = getItemState( theRoomsTree, MY_ROOMS_TREE_ITEM_TEXT );
		boolean oldOtherRoomsState = getItemState( theRoomsTree, OTHERS_ROOMS_TREE_ITEM_TEXT );
		
		//Add new elements
		TreeItem rootMyRoomsTreeItem = new TreeItem( MY_ROOMS_TREE_ITEM_TEXT );
		TreeItem rootOtherRoomsTreeItem = new TreeItem( OTHERS_ROOMS_TREE_ITEM_TEXT );
		final int thisUserID = SiteManager.getUserID();
		numberOfMyRooms = 0;
		numberOfOthersRooms = 0;
		while( roomsIter.hasNext() ){
			ChatRoomData roomData = (ChatRoomData) roomsIter.next();
			if( roomData.getOwnerID() == thisUserID ) {
				rootMyRoomsTreeItem.addItem( new RoomTreeItem( roomData, activeRoomVisitors, roomsManager ) );
				numberOfMyRooms++;
			} else {
				rootOtherRoomsTreeItem.addItem( new RoomTreeItem( roomData, activeRoomVisitors, roomsManager ) );
				numberOfOthersRooms++;
			}
		}
		//Restore the root node states
		rootMyRoomsTreeItem.setState( oldMyRoomsState, false );
		rootOtherRoomsTreeItem.setState( oldOtherRoomsState, false );
		
		//Remove all of the old tree elements
		theRoomsTree.removeItems();
		//Add new items
		if( rootMyRoomsTreeItem.getChildCount() != 0 ) {
			theRoomsTree.addItem( rootMyRoomsTreeItem );
		}
		if( rootOtherRoomsTreeItem.getChildCount() != 0 ) {
			theRoomsTree.addItem( rootOtherRoomsTreeItem );
		}
		
		//Update the current room's tree disclosure panel title;
		updateCurrentRoomsTreePanelTitle();
	}
	
	/**
	 * Allows to update the number of rooms in the title of the disclosure panel of the currently open chat room
	 */
	private static void updateCurrentRoomsTreePanelTitle() {
		if( theCurrentChatRoomUI != null ) {
			final int index = theCurrentChatRoomUI.thePeopleRoomsVPanel.getWidgetIndex( theCurrentChatRoomUI.theRoomsTreePanel );
			if( index != -1 ) {
				String headerHtmlString = StringUtils.getHeaderString( i18NTitles.roomsPanelTitle( numberOfMyRooms, numberOfOthersRooms ),
																		ServerSideAccessManager.getRoomsTreeImageURL());
				theCurrentChatRoomUI.thePeopleRoomsVPanel.setStackText(index, headerHtmlString, true);
			}
		}
	}

	@Override
	public void avatarSpoilerChanged(int userID, int spoilerID, Date spoilerExpDate) {
		theMsgsPanel.avatarSpoilerChanged(userID, spoilerID, spoilerExpDate);
	}

	/**
	 * Allows to set on the alerts on the user avatars, the alerts are set on the
	 * avatars which belong to the messages directly addressing the current user.
	 * @param isOn true if we want to set the avatars constantly on, false for "constantly" off
	 */
	public void setAvatarAlerts( final boolean isOn ) {
		theMsgsPanel.setAvatarAlerts( isOn );
	}
}
