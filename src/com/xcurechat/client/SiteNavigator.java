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
package com.xcurechat.client;

import java.util.List;
import java.util.ArrayList;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

import com.xcurechat.client.chat.ChatNavigatorElement;

import com.xcurechat.client.data.MainUserData;

import com.xcurechat.client.decorations.IntroNavigatorElement;

import com.xcurechat.client.forum.ForumNavigatorElement;

import com.xcurechat.client.news.NewNavigatorElement;
import com.xcurechat.client.top10.Top10NavigatorElement;
import com.xcurechat.client.userstatus.UserStatusType;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class represent the main site navigator. It is responsible for
 * displaying the site's title navigation buttons and also for filling
 * the site's content with appropriate components when the navigation
 * buttons are clicked
 */
public class SiteNavigator extends Composite implements SiteTitleComponent, ValueChangeHandler<String> {
	//A wrapper panel for the float panel, used to facilitate alignment
	private final HorizontalPanel wrapperPanel = new HorizontalPanel();
	
	//The panel storing the site section UI elements
	private final FlowPanel flowPanel = new FlowPanel();
	
	//This list stores all of the available site navigation elements, i.e. all of the available site section
	private final List<SiteNavigatorElement<?>> siteNavigatorElements = new ArrayList<SiteNavigatorElement<?>>();
	
	//Store the reference to the default section of the site
	private SiteNavigatorElement<?> siteDefaultSection;
	
	/**
	 * This is a simple constructor that has to be provided with a simple panel that will store the main site body
	 * @param siteBodyPanel the panel that will store the site body
	 */
	public SiteNavigator( final SimplePanel siteBodyPanel ) {
		//Initialize the site navigator with the required elements:
		
		//Add the chat section of the site
		registerNewSiteSection( new ChatNavigatorElement( true, siteBodyPanel, this, UserStatusType.FREE_FOR_CHAT ), false );
		//Add the site forum section
		registerNewSiteSection( new ForumNavigatorElement( false, siteBodyPanel, this, UserStatusType.ANOTHER_SITE_SECTION ), false );
		//Add the site top10 section
		registerNewSiteSection( new Top10NavigatorElement( false, siteBodyPanel, this, UserStatusType.ANOTHER_SITE_SECTION ), false );
		//Add the site introduction section
		registerNewSiteSection( new IntroNavigatorElement( false, siteBodyPanel, this, UserStatusType.ANOTHER_SITE_SECTION ), false );
		//Add the site news section. The main section is the default site section.
		registerNewSiteSection( new NewNavigatorElement( false, siteBodyPanel, this, UserStatusType.ANOTHER_SITE_SECTION ), true );
		
		//Initialize the composite widget
		flowPanel.setWidth("100%");
		wrapperPanel.add( flowPanel );
		initWidget( wrapperPanel );
		
		//Add the history change handler
		History.addValueChangeHandler( this );
	}
	
	/**
	 * Allows to register a new site setion
	 * @param element the site section navigator element
	 * @param isDefault true if this sections should be the defaul site section
	 */
	private void registerNewSiteSection( SiteNavigatorElement<?> element, boolean isDefault ) {
		//Place it into the title widget
		flowPanel.add( element );
		
		//Register the navigator element in the list of all navigator elements
		siteNavigatorElements.add( element );
		
		//Mark the navigator element as the default one
		//(default site section), if needed
		if( isDefault ) {
			siteDefaultSection = element;
		}
	} 
	
	/**
	* Allows to detect if we can freely navigate away from the
	* current site section after the user has logged in
	* @param historyToken the current history token
	* @return true if we can freely navigate away
	*/
	public boolean canMoveFromOnLogin( final String historyToken ) {
		if( historyToken != null && !historyToken.trim().isEmpty() ) {
			//Currently we do not allow to navigate away from any of the site section
			//because now the site sections are hyperlinks and thus if the user
			//tries to open the info page in a new window, we do not get this page opened 
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public HorizontalAlignmentConstant getHorizontalAlignment() {
		return HasHorizontalAlignment.ALIGN_CENTER;
	}

	@Override
	public double getTitlePanelWidthInPercent() {
		return 35.0;
	}

	@Override
	public void onWindowResize() {
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			@Override
			public void execute() {
				//Just in case, update the UI elements of the selected site section
				for( SiteNavigatorElement<?> element : siteNavigatorElements ) {
					if( element.isSelected() ) {
						element.getBodyContentWidget().updateUIElements();
					}
				}
			}
		});
	}
	
	/**
	 * Allows to retrieve the currently selected site section
	 * @return the currently selected site section of null if none is selected
	 */
	private SiteNavigatorElement<?> getSelectedSiteSection() {
		for( SiteNavigatorElement<?> element : siteNavigatorElements ) {
			if( element.isSelected() ) {
				return element;
			}
		}
		return null;
	}
	
	@Override
	public void setLoggedIn(MainUserData mainUserData) {
		//Just notify the site sections that the user has logged in
		for( SiteNavigatorElement<?> element : siteNavigatorElements ) {
			element.setLoggedIn( true );
		}
		
		//Select the site section corresponding to the current history token
		selectSectionWithHistoryToken( History.getToken(), false );
	}
	
	@Override
	public void setLoggedOut() {
		//Notify the site components that the user is logged out
		for( SiteNavigatorElement<?> element : siteNavigatorElements ) {
			element.setLoggedIn( false );
		}
		
		//Select the site section corresponding to the current history token
		selectSectionWithHistoryToken( History.getToken(), false );
	}
	
	/**
	 * Allows to add an alert to the site navigator element for a non-selected site section
	 * @param siteSectionIdentifier the site section identifier
	 */
	public void alertNonSelectedSiteSection( final String siteSectionIdentifier ) {
		if( siteSectionIdentifier != null && !siteSectionIdentifier.trim().isEmpty() ) {
			for( SiteNavigatorElement<?> element : siteNavigatorElements ) {
				if( element.getSiteSectionIdentifier().equals( siteSectionIdentifier ) ) {
					//We found the right section
					if( ! element.isSelected() ) {
						//If it is not selected then do the alert
						element.alertSiteSection( true );
					}
					//now we can stop looking further
					break;
				}
			}
		}
	}
	
	/**
	 * Allows to select the specified site navigator element, i.e. the site section
	 * The provided section gets selected and all other get de-selected, use this method
	 * to process the history chagne evens and o navigate the site sections!
	 * @param theElementToSelect the site navigator element to be selected
	 */
	void setSelectedSiteSection( final SiteNavigatorElement<?> theElementToSelect ) {
		//Select the appropriate site sections
		for( SiteNavigatorElement<?> element : siteNavigatorElements ) {
			element.setSelected( element == theElementToSelect );
		}
		
		//Just in case do the update after the site section is selected
		onWindowResize();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		//Select the site section corresponding to the current history token
		selectSectionWithHistoryToken( event.getValue(), true );
	}
	
	/**
	 * This method should be set to process the currently set history token in order to (re-)select the (current) site section
	 * This method takes the history token and tries to select the site section corresponding to it, if the site section is
	 * disabled or the history token does not correspond to any site section then we try to re-select the currently selected
	 * site section, if the currently selected site section does not exist then we go to the default site section.
	 * @param historyToken the history token to use for the navigation
	 * @param reportDisabled if true then we notify the user that the site section could not be selected as it is disabled
	 * @param historyToken the history token to process.
	 */
	private void selectSectionWithHistoryToken( final String historyToken, final boolean reportDisabled ) {
		//Pre-process the history token
		String actualHistoryToken = (historyToken == null) ? "" : historyToken;
		if( ! actualHistoryToken.contains( CommonResourcesContainer.SITE_SECTION_HISTORY_DELIMITER ) ) {
			//May be some one forgot to put the "/" symbol in the the manual URL input, hen we add it to the 
			//end of the history token because there should be one and if there is none then we try to fix it.
			actualHistoryToken += CommonResourcesContainer.SITE_SECTION_HISTORY_DELIMITER;
		}
		
		//Try to select the site section corresponding to the given history token
		boolean wasSiteSectionSelected = false;
		for( SiteNavigatorElement<?> element : siteNavigatorElements ) {
			wasSiteSectionSelected = element.processHystoryToken( actualHistoryToken, reportDisabled ); 
			if( wasSiteSectionSelected ) {
				//The proper site section has been found and selected
				break;
			}
		}
		
		//If none of the site sections got selected, then try to re-select the
		//currently selected section. If it is set and is not disabled then re-
		//select it. Otherwise navigate to the default site section
		if( ! wasSiteSectionSelected ) {
			SiteNavigatorElement<?> selectedSection = getSelectedSiteSection();
			if( selectedSection == null || selectedSection.isDisabled() ) {
				selectedSection = siteDefaultSection;
			} else {
				//Force this site section to be artificially unselected,
				//this is needed to be able to select it again (re-select)
				//The latter is required to update the history token
				selectedSection.setSelected( false );
			}
			setSelectedSiteSection( selectedSection );
		}
	}
}
