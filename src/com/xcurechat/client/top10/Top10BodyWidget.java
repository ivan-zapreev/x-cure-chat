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

import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.SiteBodySectionContent;

import com.xcurechat.client.decorations.SiteDynamicDecorations;

import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;

/**
 * @author zapreevis
 * This is the site-section body widget corresponding to the top10 site section 
 */
public class Top10BodyWidget extends Composite implements SiteBodySectionContent, UserAvatarImageWidget.AvatarSpoilerChangeListener {
	//The width of the main widget's scroll panel, when the background is on
	private static final int BG_ON_WIDGET_WIDTH_IN_PERCENT = 59;
	//The width of the main widget's scroll panel, when the background is off
	private static final int BG_OFF_WIDGET_WIDTH_IN_PERCENT = 98;
	//The minimum view area width after which we force the background to be removed
	private static final int MIN_BG_ON_VIEW_AREA_WIDTH = 1540; //NOTE: This is an experimentally obtained value
	//The height of the main widget's scroll panel
	private static final int WIDGET_HEIGHT_IN_PERCENT = 70;
	
	//THe list of the top10 widgets that are placed in the body of the top10 section
	private final List<PagedTop10BodyComponent<?>> top10Widgets = new ArrayList<PagedTop10BodyComponent<?>>();
	
	//The decorated panel storing its elements.
	private final SimplePanel decoratedPanel;
	
	//The main scroll panel of this composite
	private final SimplePanel scrollPanel;
	
	//The main vertical panel storing all the top10 section elements
	private final VerticalPanel widgetPanel;
	
	//The main horizontal panel storing all the statistics widgets
	private final HorizontalPanel statWidgetsPanel;
	
	//Is true if the site section is currently selected
	private boolean isSiteSectionSelected = false;
	
	//The site section history prefix
	private final String siteSectionPrefix; 

	/**
	 * The basic constructor provided with the site section url prefix
	 * @param siteSectionPrefix the history token site section prefix
	 */
	public Top10BodyWidget(  final String siteSectionPrefix ) {
		super();
		
		//Store the data
		this.siteSectionPrefix = siteSectionPrefix;
		
		//Register as the avatar spoiler change listener 
		UserAvatarImageWidget.addAvatarSpoilerChangeListener( this );
		
		//Initialize the main decorated panel
		decoratedPanel = new DecoratorPanel();
		decoratedPanel.setStyleName( CommonResourcesContainer.GRAY_ROUNDED_CORNER_PANEL_STYLE );
		
		//Initialize the vertical
		widgetPanel = new VerticalPanel();
		widgetPanel.setWidth("100%");
		widgetPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		widgetPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		decoratedPanel.add( widgetPanel );
		
		//Initialize the scroll panel
		scrollPanel = new SimplePanel();
		scrollPanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		widgetPanel.add( scrollPanel );
		
		//Initialize the panel storing the statistics widgets
		statWidgetsPanel = new HorizontalPanel();
		statWidgetsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		statWidgetsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		statWidgetsPanel.setWidth("100%");
		scrollPanel.add( statWidgetsPanel );
		
		//Add top10 widgets here
		addTop10Widget( new MoneyTop10StatWidget() );
		addTop10Widget( new TimeOnSiteTop10StatWidget() );
		addTop10Widget( new ChatMsgsTop10StatWidget() );
		addTop10Widget( new ForumPostsTop10StatWidget() );
		addTop10Widget( new RegistrationsTop10StatWidget() );
		addTop10Widget( new LastProfileFileTop10StatWidget() );
		addTop10Widget( new VisitsTop10StatWidget() );
		
		//Initialize the composite
		initWidget( decoratedPanel );
	}
	
	/**
	 * Allows to add a new top10 widget to the top10 section body
	 * @param top10Widget
	 */
	private void addTop10Widget( PagedTop10BodyComponent<?> top10Widget ) {
		top10Widgets.add( top10Widget );
		statWidgetsPanel.add( top10Widget );
	}

	@Override
	public void updateTargetHistoryToken(Anchor anchorLink) {
		//Construct the history token from the current search data
		anchorLink.setHref( CommonResourcesContainer.URI_HASH_SYMBOL + siteSectionPrefix );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onAfterComponentIsAdded()
	 */
	@Override
	public void onAfterComponentIsAdded() {
		for(PagedTop10BodyComponent<?> top10Widget : top10Widgets) {
			top10Widget.onAfterComponentIsAdded();
		}
		isSiteSectionSelected = true;
		//Update the UI elements
		updateUIElements();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onBeforeComponentIsAdded()
	 */
	@Override
	public void onBeforeComponentIsAdded() {
		for(PagedTop10BodyComponent<?> top10Widget : top10Widgets) {
			top10Widget.onBeforeComponentIsAdded();
		}
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#onBeforeComponentIsRemoved()
	 */
	@Override
	public void onBeforeComponentIsRemoved() {
		for(PagedTop10BodyComponent<?> top10Widget : top10Widgets) {
			top10Widget.onBeforeComponentIsRemoved();
		}
		isSiteSectionSelected = false;
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#processHistoryToken(java.lang.String)
	 */
	@Override
	public void processHistoryToken(final String historyToken) {
		//NOTE: Nothing to be done here
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setUserLoggedIn()
	 */
	@Override
	public void setUserLoggedIn() {
		for(PagedTop10BodyComponent<?> top10Widget : top10Widgets) {
			top10Widget.setUserLoggedIn();
		}
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#setUserLoggedOut()
	 */
	@Override
	public void setUserLoggedOut() {
		for(PagedTop10BodyComponent<?> top10Widget : top10Widgets) {
			top10Widget.setUserLoggedOut();
		}
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#updateUIElements()
	 */
	@Override
	public void updateUIElements() {
		//Compute the desired width in percent and also turn the background on/off
		final int WORK_WIDGET_WIDTH_IN_PERCENT;
		final boolean isShowBackground = ( Window.getClientWidth() > MIN_BG_ON_VIEW_AREA_WIDTH );
		if( isShowBackground ) {
			WORK_WIDGET_WIDTH_IN_PERCENT = BG_ON_WIDGET_WIDTH_IN_PERCENT;
		} else {
			WORK_WIDGET_WIDTH_IN_PERCENT = BG_OFF_WIDGET_WIDTH_IN_PERCENT;
		}
		SiteDynamicDecorations.showSiteBackground( isShowBackground );
		
		//Adjust the height andthe width of the scroll panel with the content
		final int width = (int) ( Window.getClientWidth() / 100.0 * WORK_WIDGET_WIDTH_IN_PERCENT ); 
		scrollPanel.setWidth( width + "px" );
		scrollPanel.setHeight( InterfaceUtils.suggestMainViewHeight( statWidgetsPanel, WIDGET_HEIGHT_IN_PERCENT)+ "px" );
		for(PagedTop10BodyComponent<?> top10Widget : top10Widgets) {
			top10Widget.updateUIElements();
		}
	}

	@Override
	public void avatarSpoilerChanged( final int userID, final int spoilerID, final Date spoilerExpDate) {
		if( isSiteSectionSelected ) {
			//Only Update avatars if the site section is currently selected
			for(PagedTop10BodyComponent<?> top10Widget : top10Widgets) {
				top10Widget.avatarSpoilerChanged( userID, spoilerID, spoilerExpDate );
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		//NOTE: Nothing to be done here
	}	
}
