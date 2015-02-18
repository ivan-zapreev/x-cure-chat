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
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.SiteBodySectionContent;

import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.MessagesPanel;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;

/**
 * @author zapreevis
 * This class represents the forum manager container, should have only one instance
 */
public class ForumBodyWidget extends Composite implements SiteBodySectionContent, UserAvatarImageWidget.AvatarSpoilerChangeListener {
	//The main vertical panel storing all the forum elements
	private final VerticalPanel forumPanel = new VerticalPanel();
	
	//The forum's search panel
	private final ForumSearchPanel searchPanel;
	//The forum's message stack navigator
	private final MessagesStackNavigator stackNavigator;
	//The forum's messages panel
	private final MessagesPanel messagesPanel;
	
	//The list of the forum's components
	private final List<ForumBodyComponent> forumBodyComponents = new ArrayList<ForumBodyComponent>();
	
	/**
	 * The basic constructor provided with the site section url prefix
	 * @param siteSectionPrefix the history token site section prefix
	 */
	ForumBodyWidget( final String siteSectionPrefix ) {
		super();
		
		//The forum's search panel
		this.searchPanel = new ForumSearchPanel( false );
		forumBodyComponents.add( searchPanel );
		//The forum's messages panel
		this.messagesPanel = new ForumMessagesPanel( true, true, true, siteSectionPrefix );
		forumBodyComponents.add( messagesPanel );
		//The forum's message stack navigator
		this.stackNavigator = new MessagesStackNavigator( false, messagesPanel, searchPanel, siteSectionPrefix );
		forumBodyComponents.add( stackNavigator );
		
		//Set up the forum manager
		ForumSearchManager.setForumUIComponents( searchPanel, stackNavigator, messagesPanel, this );
		
		//Register the avatar image spoiler listener
		UserAvatarImageWidget.addAvatarSpoilerChangeListener( this );
		
		//Initialize the main vetical panel
		forumPanel.setWidth("100%");
		forumPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		
		//Add the search and navigator panels to a wrapping decorated panel
		final VerticalPanel searchNavPanel = new VerticalPanel();
		searchNavPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		final DecoratorPanel decPanel = new DecoratorPanel();
		decPanel.setStyleName( CommonResourcesContainer.GRAY_ROUNDED_CORNER_PANEL_STYLE );
		decPanel.add( searchNavPanel );
		forumPanel.add( decPanel );
		
		//Add the search panel
		searchPanel.setWidth("100%");
		searchNavPanel.add( searchPanel );
		
		//Add the messages stack navigator
		stackNavigator.setWidth("100%");
		searchNavPanel.add( stackNavigator );
		
		//Add Messages panel
		forumPanel.add( messagesPanel );
		
		//Initialize the composite
		initWidget( forumPanel );
	}

	@Override
	public void setEnabled( final boolean enabled ) {
		for( ForumBodyComponent comp : forumBodyComponents){
			comp.setEnabled( enabled );
		}
	}
	
	@Override
	public void updateUIElements() {
		//Opera and IE do not like to set the 100% (desired) width, via css, of an empty panel, 
		//and in Windows it somehow needs more space thus here in case of IE and Opera we enforce   
		//set 98% width. Now we always enforce the width, just for uniform with of all the elements.
		final int WIDTH_PERCENT = 98;
		final boolean enforceWidth = true;
		for( ForumBodyComponent comp : forumBodyComponents){
			comp.updateUIElements( false, enforceWidth, WIDTH_PERCENT );
		}
	}

	@Override
	public void setUserLoggedIn() {
		//WARNING: DO NOT PUT ANY SEARCH HERE, it will mess up he results due to the asynchronous server calls
		
		//Notify the forum elements that the user can write/read the forum
		for( ForumBodyComponent comp : forumBodyComponents){
			comp.setUserLoggedIn();
		}
	}

	@Override
	public void setUserLoggedOut() {
		//Notify the forum elements that the user can only read the forum
		for( ForumBodyComponent comp : forumBodyComponents){
			comp.setUserLoggedOut();
		}
	}

	//The first time the forum is shown, make the messages panel a bit
	//smaller to prevent vertical scroll bar which then disappears on re-select
	private boolean isFirstTime = true;
	
	@Override
	public void onAfterComponentIsAdded() {
		//Re-add the panel size, here we have to account for the
		//browsers because they do not render the first view correctly
		//if the person comes directly to the forum page
		BrowserDetect detect = BrowserDetect.getBrowserDetect();
		if( isFirstTime && ( detect.isChrome() || detect.isSafari() || detect.isFirefox() ) ) {
			//We do the update in the deferred command because only this way 
			//the browser does it after the rendering of the view is complete
			Scheduler.get().scheduleDeferred( new ScheduledCommand(){
				@Override
				public void execute() {
					updateUIElements();
				}
			});
			isFirstTime = false;
		}
	}
	
	@Override
	public void onBeforeComponentIsAdded() {
		//WARNING: Is called when we navigated here with the
		//history token, should not do any forum search here
		for( ForumBodyComponent comp : forumBodyComponents){
			comp.onBeforeComponentIsAdded();
		}
	}
	
	@Override
	public void onBeforeComponentIsRemoved() {
		for( ForumBodyComponent comp : forumBodyComponents){
			comp.onBeforeComponentIsRemoved();
		}
	}
	
	@Override
	public void processHistoryToken(String historyToken) {
		//Process the history token, i.e. perform proper search, 
		//refresh the view but without updating the history
		ForumSearchManager.doSearch( stackNavigator.deserializeStackElement( historyToken ), false );
	}

	@Override
	public void updateTargetHistoryToken(Anchor anchorLink) {
		anchorLink.setHref( CommonResourcesContainer.URI_HASH_SYMBOL + stackNavigator.serializeTopStackElement( true ) );
	}

	@Override
	public void avatarSpoilerChanged(int userID, int spoilerID, Date spoilerExpDate) {
		messagesPanel.updateUserAvatarSpoiler(userID, spoilerID, spoilerExpDate);
	}
}
