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

import com.google.gwt.user.client.ui.Composite;
//import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;


import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.decorations.SiteDynamicDecorations;

/**
 * @author zapreevis
 * This class represents the site title, the one with the site icon and the site title
 */
public class SiteTitle extends Composite implements SiteTitleComponent {

	public SiteTitle() {
		//Add the widgets to the main panel
		HorizontalPanel titlePanel = new HorizontalPanel();
		titlePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		titlePanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		titlePanel.add( SiteDynamicDecorations.getBackgroundTimeImage() );
		//titlePanel.add( new HTML("&nbsp;") ); //Add some spacing
		//titlePanel.add( SiteDynamicDecorations.getSiteTitleLabel() );
		
		//Initialize the composite
		initWidget( titlePanel );
	}

	public HorizontalAlignmentConstant getHorizontalAlignment() {
		if( SiteManager.isUserLoggedIn() ) {
			return HasHorizontalAlignment.ALIGN_CENTER;
		} else {
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}

	public double getTitlePanelWidthInPercent() {
		return 30.0;
	}

	public void onWindowResize() {
		//NOTE: Nothing to be done here
	}

	public void setLoggedIn(MainUserData mainUserData) {
		//NOTE: Nothing to be done here
	}

	public void setLoggedOut() {
		//NOTE: Nothing to be done here
	}

}
