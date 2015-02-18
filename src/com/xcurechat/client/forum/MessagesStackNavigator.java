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

import java.util.List;
import java.util.ArrayList;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;

import com.xcurechat.client.utils.ClientEncoder;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.MessagesPanel;

/**
 * @author zapreevis
 * This class is used to visualize the list of stack of messages, i.e. the user's position in the tree of forum messages
 */
class MessagesStackNavigator extends ForumBodyComponent {
	
	//The encoder that we should use in the client
	private final ClientEncoder encoder = new ClientEncoder();
	
	//This is the main scroll panel of the message stack
	private final ScrollPanel scrollPanel = new ScrollPanel();
	//This is the VerticalPanel that will contain all of the stack elements
	private final HorizontalPanel elementPanel = new HorizontalPanel();
	
	//This list stores the message stack components;
	private final List<MessageStackElement> messageStack = new ArrayList<MessageStackElement>();
	
	//This variable indicates the fact that the site section is selected
	private boolean isSiteSetionSelected = false;
	
	//The messages panel associated with this stack navigator
	private final MessagesPanel messagesPanel;
	
	//The search panel associated with this stack navigator
	private final ForumSearchPanel searchPanel;

	//The site section prefix
	private final String siteSectionPrefix;
	
	/**
	 * This constructor has to be used for all the body components.
	 * @param addDecorations if true then we use the decorated panel with the rounded corners around this widget
	 * @param messagesPanel the messages panel associated with this stack navigator
	 * @param siteSectionPrefix the history token site section prefix
	 */
	public MessagesStackNavigator( final boolean addDecorations, final MessagesPanel messagesPanel,
								   final ForumSearchPanel searchPanel, final String siteSectionPrefix ) {
		//Initialize the super class
		super( addDecorations );
		
		//Store the data
		this.messagesPanel = messagesPanel;
		this.searchPanel   = searchPanel;
		this.siteSectionPrefix = siteSectionPrefix;
		
		//Set the component widgets
		populate();
	}

	private void populate() {
		//Add the scroll panel
		elementPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		elementPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		scrollPanel.add( elementPanel );
		
		//Initialize the widget
		scrollPanel.setWidth("100%");
		scrollPanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		scrollPanel.addStyleName( CommonResourcesContainer.STACK_NAVIGATOR_SCROLL_PANEL_ELEMENT_STYLE );
		
		//Set the simple panel
		setComponentWidget( scrollPanel );
		
		//Set the height of the panel to be 40 pixels
		addDecPanelStyle(CommonResourcesContainer.STACK_NAVIGATOR_DEC_PANEL_ELEMENT_STYLE);
	}

	@Override
	public void onBeforeComponentIsAdded() {
		//Mark the forum site section as added
		isSiteSetionSelected = true;
		//Ensure that the proper history token is set
		ensureTopStackElementIsCurrent();
	}
	
	@Override
	public void onBeforeComponentIsRemoved() {
		isSiteSetionSelected = false;
		for( MessageStackElement element : messageStack) {
			element.onBeforeComponentIsRemoved();
		}
	}
	
	@Override
	public void setEnabled( boolean enabled ) {
		for( MessageStackElement element : messageStack) {
			element.setEnabled( enabled );
		}
	}
	
	/**
	 * Allows to retrieve the the top-most stack element, in case the stack element does not exist,
	 * we create an initial stack element and return it to the user, because the case of no elements
	 * on the stack is only possible when the initial search has not been done yet
	 * @return the top-most stack element or the initial search element if the stack is empty
	 */
	public MessageStackElement getCurrentMessageStackElement() {
		//The message stack is never empty it should at least contain the initial topic search elements
		final int currentStackElementIndex = messageStack.size() - 1;
		if( currentStackElementIndex >= 0 ) {
			return messageStack.get( currentStackElementIndex );
		} else {
			return getInitialSearchStackElement();
		}
	}
	
	/**
	 * Allows to check if the new search is needed. I.e. if the current message stack element
	 * is different from the provided message stack elements
	 * @param element the complete message search element
	 * @return true if the new search is needed
	 */
	public boolean isNewSearchNeeded( final MessageStackElement element ) {
		final int currentStackElementIndex = messageStack.size() - 1;
		if( currentStackElementIndex >= 0 ) {
			return !messageStack.get( currentStackElementIndex ).isEqualStackElements( element );
		} else {
			return true;
		}
	}
	
	/**
	 * Constructs and returns the initial search stack element
	 * @return the initial seach stack element
	 */
	public static MessageStackElement getInitialSearchStackElement() {
		ForumSearchData searchData = new ForumSearchData();
		ForumSearchData.initWithBrowseSectionsParams( searchData );
		return new MessageStackElement( searchData, null );
	}
	
	/**
	 * Allows to add a new stack element to the panel. The message checks if
	 * the current message is already on the stack if yes then we navigate to
	 * it otherwise add a new messge to the stack. Makes sure that the proper
	 * history token is set.
	 * @param element the element to add
	 */
	public void addStackMessageElement( MessageStackElement element ) {
		//Check if this search element is already on the stack, then navigate to it
		if( isMessageStackElementInTheStack( element ) ) {
			//Then pop till this element is at the top of the stack,
			//make sure that the proper history token is set.
			removeAllAfter( element );
		} else {
			if( ! element.isMessageReply() ) {
				//If this is not a message reply we want to place on the stack, then it should be a
				//custom search element because the initial search element would have been detected 
				//and navigated to already. Remove all stack elements except for the initial search
				//Makes sure that the proper history token is set.
				removeAllAfter( null );
			} else {
				//This is a a new reply message search, remove all search results that are the same except for the page index
				removeAllPagedSearchResults( element );
			}
			//Add the element to the stack, make sure that the proper history token is set
			addMsgsElement( element );
		}
	}
	
	/**
	 * Allows to check if the provided message stack element is on the stack, checks all stack elements.
	 * @param e the element to pop up until
	 * @return true if the provided message stack element was found on the stack, otherwise false
	 */
	public boolean isMessageStackElementInTheStack( final MessageStackElement e ) {
		for( int index = ( messageStack.size() - 1 ); index >= 0 ; index-- ) {
			MessageStackElement element = messageStack.get( index );
			//if the elements are the same object or store the same messages
			if( element.isEqualStackElements( e ) ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Remove all search results that are the same as the given one, except for the page index
	 * @param element the stack element to compare to
	 */
	private void removeAllPagedSearchResults( final MessageStackElement element ) {
		for( int index = ( messageStack.size() - 1 ); index >= 0 ; index-- ) {
			MessageStackElement previousElement = messageStack.get( index );
			//If the elements are the same object or store the same messages
			if( previousElement.isEqualStackElements( element, true ) ) {
				removeLastStackMessageElement( );
			}
		}
	}
	
	/**
	 * Allows to pop all the elements of the stack until the given one or the last stack element.
	 * Ensures that the proper history token is set.
	 * @param e the element to pop up until
	 */
	private void removeAllAfter( final MessageStackElement e ) {
		final int stackSize = messageStack.size();
		if( stackSize > 1 ) {
			//If there is more than one element on the stack
			for( int index = ( stackSize - 1 ); index > 0 ; index-- ) {
				MessageStackElement element = messageStack.get( index );
				//if the elements are the same object or store the same messages
				if( element.isEqualStackElements( e ) ) {
					//In case this was already the top most element on the stack
					//Stop iterations on meeting the required message. Make  
					//sure that the proper history token is set
					ensureTopStackElementIsCurrent( );
					break;
				} else {
					//Pop the latest element from the stack Make  
					//sure that the proper history token is set
					removeLastStackMessageElement( );
				}
			}
		} else {
			if( stackSize == 1 ) {
				//If there is just one element on the stack. Make  
				//sure that the proper history token is set
				ensureTopStackElementIsCurrent( );
			} else {
				//This should not be happening, in this case we do nothing
			}
		}
	}
	
	/**
	 * Makes sure that the forum manager knows about the current stack element
	 * and also that the currently set history token is up to date.
	 */
	private void ensureTopStackElementIsCurrent() {
		//Make sure that the proper history token is set, just for
		//safety in case we need to set the history token, so if
		//doNotUpdateHistry==false i.e. we want to update history
		//then we will update it any ways, if doNotUpdateHistry==true
		//i.e. we do not want to update history, but the current stop
		//stack element is not the one in the current history token
		//then we force the history to be updated, to avoid problems!
		notifyAboutCurrentStackElement( );
	}
	
	/**
	 * Allows to notify the ForumManager about the current latest stack element.
	 * This is needed for managing the "New topic" <=> "Reply here" button handling
	 * Also, this method updates the browser history. NOTE: does not update the
	 * history token if the site section is not selected and the new set search
	 * item is the same as in the history token! When the history token is set
	 * the history-change event is suppressed!
	 */
	private void notifyAboutCurrentStackElement( ) {
		if( messageStack.size() > 0 ) {
			final MessageStackElement element = messageStack.get( messageStack.size() - 1 );
			//Set the appropriate action
			searchPanel.setNewActionType( element.getMessageData() );
			//Mark sure the proper topic ID is set
			searchPanel.setCurrTopicMessageID( ShortForumMessageData.getTopicMessageID( element.getMessageData() ) );
			final String currentHistoryToken = History.getToken();
			if( ( currentHistoryToken == null || ! currentHistoryToken.equals( serializeTopStackElement( false ) ) ) && isSiteSetionSelected ) {
				//Since we were added here, this means that the forum search
				//was performed and a new element was set, here we update History
				History.newItem( serializeTopStackElement( false ), false );
			}
		}
		//Scroll to the last stack element
		scrollToLastStackElement();
	}
	
	/**
	 * Shows the last stack element in the scroll panel
	 */
	private void scrollToLastStackElement() {
		//Make it into the deferred command because the element might be shown later,
		//such as if this method is called before the forum section is selected
		if( messageStack.size() > 0 ) {
			final MessageStackElement element = messageStack.get( messageStack.size() - 1 );
			Scheduler.get().scheduleDeferred( new ScheduledCommand(){
				@Override
				public void execute() {
					//Ensure that the top stack element is visible in the stack
					if( element.isAttached() ) {
						scrollPanel.ensureVisible( element );
					}
				}
			});
		}
	}
	
	/**
	 * Allows to remove the last message stack element, only works
	 * if the number of the stack elements is > 1, the first element
	 * should be the initial search. Also, this method updates the
	 * browser history by setting the current stack message element
	 * data, if needed.
	 */
	public void removeLastStackMessageElement( ) {
		final int removeIndex = messageStack.size() - 1;
		if( removeIndex >= 1 ) {
			messageStack.remove( removeIndex );
			elementPanel.remove( removeIndex );
			MessageStackElement previousElement = messageStack.get( removeIndex - 1 );
			if( previousElement.isMessageReply() ) {
				previousElement.setLastStackElement();
			}
		}
		//Notify the ForumManager about the current stack element
		notifyAboutCurrentStackElement( );
		//Scroll to the last stack element
		scrollToLastStackElement();
	}
	
	/**
	 * This method just adds a message stack element without doing any checks.
	 * Makes sure that the proper history token is set.
	 * @param element the element to ass
	 * @param doNotUpdateHistry if true then the history is not updated, no matter what
	 */
	private void addMsgsElement( MessageStackElement element ) {
		//Get the last message stack element
		final int lastElementIndex = messageStack.size() - 1;
		if( lastElementIndex >= 0 ) {
			//Check if there is a last message stack element that corresponds to browsing message replies
			MessageStackElement lastStackElement = messageStack.get( lastElementIndex );
			//In case there is then we mark the element as a non-last
			if( lastStackElement.isMessageReply() ) {
				lastStackElement.setNotLastStackElement();
			}
		} else {
			//Here we are adding a first stack element but what if it is not 
			//the default search element? We should check on this and if it is
			//not then add the default initial search element first
			MessageStackElement initialElement = getInitialSearchStackElement();
			if( !initialElement.isEqualStackElements( element ) ) {
				//We add this element we use the same history
				//update options as for the main adding element
				addMsgsElement( initialElement );
			}
		}
		//Add the element, visualize it
		messageStack.add( element );
		elementPanel.add( element );
		element.setLastStackElement();
		//Notify the ForumManager about the current stack element
		notifyAboutCurrentStackElement( );
		//Scroll to the last stack element
		scrollToLastStackElement();
	}
	
	/**
	 * Allows to get the serialization string of the top element at the stack.
	 * The serialization is URL safe.
	 * @param createNew if true then in case the stack is empty we create a
	 *                  new element for the initial search and serialize it
	 *                  this element is not put on the stack though.
	 * @return the serialization string of the top element at the stack
	 */
	String serializeTopStackElement( final boolean createNew ) {
		String serializationResult = siteSectionPrefix;
		if( messageStack.size() > 0 || createNew) {
			//NOTE: the getCurrentMessageStackElement() creates the initial search element in case the stack is empty
			serializationResult += getCurrentMessageStackElement().serialize();
		}
		return serializationResult;
	}
	
	/**
	 * Allows to deserialize and complete the stack element given by the history token
	 * @param historyToken the history token
	 * @return the constructed MessageStackElement
	 */
	MessageStackElement deserializeStackElement( final String historyToken ) {
		ForumSearchData searchData = ForumSearchData.deserialize( encoder, historyToken );
		ForumMessageData messageData = null;
		//We have obtained a forum search data, this might be a message replies view
		//If so then we should try to get the corresponding message, if not then we 
		//do it as a custom search, because there is no other better way to do it
		
		if( searchData.isForumNavigation() ) {
			//For the case of navigating BACK in the message history stack this
			//message should be somewhere on the stack, but not the first stack element
			for( int index = ( messageStack.size() - 1 ); index > 0 ; index-- ) {
				MessageStackElement element = messageStack.get( index );
				if( element.isMessageReply() ) {
					if( element.getMessageData().messageID == searchData.baseMessageID ) {
						messageData = element.getMessageData();
						break;
					}
				}
			}
			
			//If we could not navigate backwards, then try to navigate forward
			if( ( messageData == null ) && ( messagesPanel != null ) ) {
				//For navigating FORTH in the forum messages tree, this message
				//should be somewhere in the list of current messages
				messageData = messagesPanel.getCurrentViewMessage( searchData.baseMessageID );
			}
		}
		return new MessageStackElement( searchData, messageData );
	}
	
	@Override
	public void setUserLoggedOut(){
		//Nothing to be done here, the initial search is performed in the ForumManager
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
			scrollPanel.setWidth( scrollPanelWidth + "px" );
			//Scroll to the last stack element
			scrollToLastStackElement();
		}
	}

	@Override
	public void setUserLoggedIn() {
		//Nothing to be done here
	}
}
