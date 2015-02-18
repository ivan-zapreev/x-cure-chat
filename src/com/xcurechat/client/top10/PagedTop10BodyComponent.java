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
 * The top10 site section user interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.top10;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteBodyComponent;
import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.Top10SearchData;

import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.PageIndexesGenerator;
import com.xcurechat.client.utils.SplitLoad;

import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.NavigationButtonPanel;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;
import com.xcurechat.client.utils.widgets.UserAvatarWidget;

/**
 * @author zapreevis
 * This is a generic version of the paged top10 component that allows to browse paged data of various ratigs
 */
public abstract class PagedTop10BodyComponent<ResultType> extends Composite implements SiteBodyComponent, UserAvatarImageWidget.AvatarSpoilerChangeListener {
	//The height of the widget in percent relative to the view area of the page
	private static final int WIDGET_HEIGHT_IN_PERCENT = 60;
	//The height of the decoration elements around the scroll panel in pixels
	private static final int DECORATION_ELEMENTS_HEIGHT = 40;
	
	//The localization for the text
	protected static final UITitlesI18N titlesI18N = I18NManager.getTitles();

	/**
	 * @author zapreevis
	 * The local extension of the navigation button
	 */
	private class TopBottomButtons extends NavigationButtonPanel {
		public TopBottomButtons(int buttonType, boolean isEnabled, boolean isActive) {
			super( buttonType, isEnabled, isActive, true, null, null, null, null );
		}
		@Override
		protected void moveToPage(boolean isNext) {
			moveToPageIndex( current_page_index + (isNext ? 1 : -1));
		}
	}
	
	//The list that stores the user avatar widgets for the top10 components showing widgets
	public List<SiteBodyComponent> resultWidgets = new ArrayList<SiteBodyComponent>();
	//The decorated panel storing its elements.
	private final SimplePanel decoratedPanel;
	//The main scroll panel of this composite
	private final ScrollPanel scrollPanel;
	//The main vertical panel storing the navigation buttons and the scroll panel for the search results
	private final VerticalPanel mainPanel;
	//The panel that will store the search results
	private final VerticalPanel resultsPanel;
	//The loading progress bar
	protected final ServerCommStatusPanel progressBarUI = new ServerCommStatusPanel();
	
	//The navigation button panels
	private final NavigationButtonPanel topNavButton;
	private final NavigationButtonPanel bottomNavButton;
	
	//The minimum page index
	private final int minimum_page_index = 1;
	//The maximum page index
	private int maximum_page_index = minimum_page_index;
	//Current page index
	private int current_page_index = minimum_page_index;
	//Stores true if we are currently in the search
	private boolean isInSearch = false;
	//Stores true if the site section is selected
	protected boolean isSiteSectionSelected = false;
	
	/**
	 * The basic constructor
	 */
	public PagedTop10BodyComponent(final String title, final String help) {
		//Initialize the main decorated panel
		decoratedPanel = new DecoratorPanel();
		decoratedPanel.setStyleName( CommonResourcesContainer.GRAY_ROUNDED_CORNER_PANEL_STYLE );
		
		//Initialize the main panel storing the buttons and the scroll panel
		mainPanel = new VerticalPanel();
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		mainPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		decoratedPanel.add( mainPanel );
		
		//Set up the title panel
		final HorizontalPanel horizPanel = new HorizontalPanel();
		horizPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		horizPanel.setStyleName( CommonResourcesContainer.TOP10_STAT_RESULTS_COMPONENT_TITLE_LABEL_STYLE );
		
		//Add the title label
		if( title != null ) {
			final Label titleLabel = new Label( title );
			titleLabel.setWordWrap( false );
			horizPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			horizPanel.add( titleLabel );
		}
		
		//The subsequent elements will be aligned to the top/right
		horizPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		horizPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		
		//Add the to-the-beginning image button
		horizPanel.add(  new HTML( "&nbsp;" ) );
		final Image moveToTopImage = new Image( ServerSideAccessManager.USER_INFO_RELATED_IMAGES_LOCATION + "home.png" );
		moveToTopImage.setTitle( titlesI18N.moveToTopToolTip() );
		moveToTopImage.setStyleName( CommonResourcesContainer.TITLE_IMAGE_BUTTON_STYLE );
		moveToTopImage.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if( current_page_index != minimum_page_index ) {
					moveToPageIndex( minimum_page_index );
				}
			}
		} );
		horizPanel.add( moveToTopImage );
		
		//Add the refresh image button
		horizPanel.add(  new HTML( "&nbsp;" ) );
		final Image refreshImage = new Image( ServerSideAccessManager.USER_INFO_RELATED_IMAGES_LOCATION + "refresh.png" );
		refreshImage.setTitle( titlesI18N.refreshToolTip() );
		refreshImage.setStyleName( CommonResourcesContainer.TITLE_IMAGE_BUTTON_STYLE );
		refreshImage.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateCurrentPage();
			}
		} );
		horizPanel.add( refreshImage );
		
		//Add the help image button if help is present
		if( help != null ) {
			horizPanel.add(  new HTML( "&nbsp;" ) );
			final Image helpImage = new Image( ServerSideAccessManager.USER_INFO_RELATED_IMAGES_LOCATION + "info.png" );
			helpImage.setTitle( titlesI18N.helpToolTip() );
			helpImage.setStyleName( CommonResourcesContainer.TITLE_IMAGE_BUTTON_STYLE );
			helpImage.addClickHandler( new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//Open the info dialog
					(new SplitLoad( true ) {
						@Override
						public void execute() {
							InfoMessageDialogUI.openInfoDialog( help, true );
						}
					}).loadAndExecute();
					//Stop te event from being propagated
					event.stopPropagation();
					event.preventDefault();
				}
			} );
			horizPanel.add( helpImage );
		}
		
		//Add the title component
		mainPanel.add( horizPanel );
		
		//Initialize the top navigation button
		topNavButton = new TopBottomButtons( CommonResourcesContainer.NAV_TOP_IMG_BUTTON, false, false );
		mainPanel.add( topNavButton );
		
		//Initialize the search results panel
		resultsPanel = new VerticalPanel();
		resultsPanel.setWidth("100%");
		resultsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		resultsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		
		//Initialize the scroll panel
		scrollPanel = new ScrollPanel();
		scrollPanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		scrollPanel.add( resultsPanel );
		mainPanel.add( scrollPanel );
		
		//Initialize the bottom navigation button
		bottomNavButton = new TopBottomButtons( CommonResourcesContainer.NAV_BOTTOM_IMG_BUTTON, false, false );
		mainPanel.add( bottomNavButton );
		
		//Initialize the Widget
		initWidget( decoratedPanel );
	}
	
	/**
	 * Allows to prepare the widget for the search and recalculate the next page index.
	 * The latter is done in order to keep the page index in the right bounds
	 * @param nextPageIndexProposal the proposed next page index
	 * @return the next page index that is within the minimum and the maximum page index
	 */
	private int prepareSearch( final int nextPageIndexProposal ) {
		final int nextPageIndex;
		if( ! isInSearch ) {
			//Indicate that the search is started
			isInSearch = true;
			//Disable the components, e.g. buttons and the widgets
			setEnabled( false );
			//Prepare the results page, clear its content and add the progress bar
			resultsPanel.clear();
			resultsPanel.add( progressBarUI );
			//Clear the avatar widgets
			resultWidgets.clear();
			
			//Set the current page index
			if( ( nextPageIndexProposal < minimum_page_index ) ||
				( nextPageIndexProposal > maximum_page_index ) ) {
				//If the new page index is bad then reset it to the minimum
				nextPageIndex = minimum_page_index;
			} else {
				//If the new page index is good then set it to be the current page index
				nextPageIndex = nextPageIndexProposal; 
			}
		} else {
			//We are in search already, thus the next page index stays the same as the current page index
			nextPageIndex = current_page_index;
		}
		
		return nextPageIndex;
	}
	
	/**
	 * Allows to navigate to the next/previous page, also allows to stay at the current page and just refresh it
	 * @param nextPageIndexProposal the desired index of the next page
	 */
	private void moveToPageIndex( final int nextPageIndexProposal ) {
		if( ! isInSearch ) {
			//Prepare for the search
			final int nextPageIndex = prepareSearch( nextPageIndexProposal );
			
			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Retrieve the user data from the server 
					CommStatusAsyncCallback<OnePageViewData<?>> callBack = new CommStatusAsyncCallback<OnePageViewData<?>>(progressBarUI) {
						@SuppressWarnings("unchecked")
						public void onSuccessAct(OnePageViewData<?> results) {
							if( isInSearch ) {
								//Process the search results
								processSearchResults( (OnePageViewData<ResultType> ) results );
								//Finish the search
								finishSearch( nextPageIndex, false );
							}
						}
						public void onFailureAct(Throwable caught) {
							//Use the recovery method
							recover();
						}
					};
					UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
					userManagerObject.searchTop10( SiteManager.getUserID(), SiteManager.getUserSessionId(),
												 new Top10SearchData( nextPageIndexProposal, getStatisticsSearchType() ), callBack );
				}
				@Override
				public void recover() {
					if( isInSearch ) {
						//Finish the search
						finishSearch( nextPageIndex, false );
					}
				}
			}).loadAndExecute();
		}
	}
	
	/**
	 * Allows to update the current page results
	 */
	protected void updateCurrentPage() {
		//Update the current page search results
		moveToPageIndex( current_page_index );
	}
	
	/**
	 * Allows to add the new result widget to the list of site body components and the results panel
	 * @param widget the widget to add
	 */
	private void addResultWidget(Widget widget) {
		if( widget != null ) {
			//If it is a body component then add it to the list
			if( widget instanceof SiteBodyComponent ) {
				resultWidgets.add( (SiteBodyComponent) widget );
			}
			//Put it into the results panel
			resultsPanel.add( widget );
		}
	}
	
	
	/**
	 * Allows to process the search results
	 * @param results the search results
	 */
	private void processSearchResults(OnePageViewData<ResultType> results) {
		if( results != null ) {
			//Compute and update the local maximum page index
			maximum_page_index = PageIndexesGenerator.getNumberOfPages( results.total_size, Top10SearchData.MAX_NUMBER_OF_RESULTS_PER_PAGE );
			//Fill out he results panel
			if( ( results.entries != null ) && ( results.entries.size() > 0 ) ) {
				//Clear the results panel from the progress bar widget
				resultsPanel.clear();
				//Process the result widgets
				for( int index = 0; index < results.entries.size(); index++ ) {
					addResultWidget( constructResultWidget( results.entries, index ) );
				}
				//Scroll to the top
				scrollPanel.scrollToTop();
			}
		}
	}
	
	/**
	 * This method must be implemented by a child class and it should
	 * provide for a particular data entry a corresponding widget
	 * @param results the entire set of results
	 * @param the index of the results entry for which we want to have a widget
	 * @return the widget corresponding to the given data entry
	 */
	protected abstract Widget constructResultWidget( final List<ResultType> results, final int index );
	
	/**
	 * Should be called by the end of the search, after the search results
	 * have been added to the results panel in order to complete the search. 
	 * @param new_page_index the page index at which we arrived
	 * @param isForced if true then the method was called to force the search results stop
	 */
	private void finishSearch( final int new_page_index, final boolean isForced ) {
		//Stop the search if it is on
		if( isInSearch ) {
			//Update the current page index
			current_page_index = new_page_index;
			//Mark that we are not in search any more
			isInSearch = false;
			//Disable progress bar if this is a forced stop
			if( isForced ) {
				progressBarUI.stopProgressBar( true );
			}
			//Update page indexes
			topNavButton.setRemainingPageCount( minimum_page_index, current_page_index, maximum_page_index );
			bottomNavButton.setRemainingPageCount( minimum_page_index, current_page_index, maximum_page_index );
			//Enable/disable the buttons
			topNavButton.setAllowed( current_page_index > minimum_page_index );
			bottomNavButton.setAllowed( current_page_index < maximum_page_index );
			//Enable the components
			setEnabled( true );
		}
	}
	
	/**
	 * Allows to add the style for the scroll panel storing the content of the search results
	 * This style can define e.g. the width of the panel 
	 * @param styleName the style name
	 */
	protected void addResultsPanelStyle( final String styleName ) {
		if( styleName != null ) {
			scrollPanel.addStyleName( styleName );
		}
	} 
	
	/**
	 * Allows to update the height of this UI
	 */
	public void updateUIElements() {
		//Adjust the height of the scroll panel with the content
		scrollPanel.setHeight( ( InterfaceUtils.suggestMainViewHeight( scrollPanel, WIDGET_HEIGHT_IN_PERCENT) - DECORATION_ELEMENTS_HEIGHT ) + "px");
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onAfterComponentIsAdded()
	 */
	@Override
	public void onAfterComponentIsAdded() {
		//Mark the site section as selected
		isSiteSectionSelected = true;
	}
	
	/**
	 * Allows to get the type, one of Top10SearchData, of the statistics that the given widget searches for
	 * @return TOP_MONEY_SEARH_TYPE, TOP_FORUM_POSTS_SEARH_TYPE, TOP_CHAT_MESSAGES_SEARH_TYPE,
 	 * 		   TOP_TIME_ON_SITE_SEARH_TYPE, TOP_LAST_PROFILE_FILES_SEARH_TYPE, or TOP_IS_ONLINE_SEARH_TYPE.
	 */
	protected abstract Top10SearchData.SearchTypes getStatisticsSearchType();

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onBeforeComponentIsAdded()
	 */
	@Override
	public void onBeforeComponentIsAdded() {
		//Update the current page search results
		updateCurrentPage();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onBeforeComponentIsRemoved()
	 */
	@Override
	public void onBeforeComponentIsRemoved() {
		//Force the search to be stopped
		finishSearch( current_page_index, true );
		//Mark the site section as not selected
		isSiteSectionSelected = false;
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		//Enable/Disable the content of the widget, e.g. buttons 
		topNavButton.setEnabled( enabled );
		bottomNavButton.setEnabled( enabled );
		//Enable/disable the elements depending on whether the user is logged in or not
		for( SiteBodyComponent userAvatar : resultWidgets ) {
			userAvatar.setEnabled( enabled && SiteManager.isUserLoggedIn() );
		}
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setUserLoggedIn()
	 */
	@Override
	public void setUserLoggedIn() {
		//Set the user logged in for the content of the widget, e.g. avatars
		for( SiteBodyComponent userAvatar : resultWidgets ) {
			userAvatar.setUserLoggedIn();
		}
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setUserLoggedOut()
	 */
	@Override
	public final void setUserLoggedOut() {
		if( isSiteSectionSelected ) {
			//If the site section is selected update the current page search results
			updateCurrentPage();
		} else {
			//NOTE: Do nothing because the results will be updated on the site section selection
		}
	}

	@Override
	public void avatarSpoilerChanged(final int userID, final int spoilerID, final Date spoilerExpDate) {
		//Update the avatar spoilers in the avatars
		//Set the user logged in for the content of the widget, e.g. avatars
		for( SiteBodyComponent widget : resultWidgets ) {
			if( widget instanceof UserAvatarWidget ) {
				((UserAvatarWidget)widget).updateThisAvatarSpoiler( userID, spoilerID, spoilerExpDate );
			}
		}
	}
}
