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
package com.xcurechat.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HTML;


import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.XCureUncaughtExceptionHandler;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.client.i18n.I18NManager;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class XCureChatMain implements EntryPoint {
	
	/**
	 * Makes the "on-page-load" screen hidden
	 */
	public static native void removeOnLoadScreen() /*-{
        var loadingscreen = $wnd.document.getElementById('loadingScreen');
        if( loadingscreen ) {
        	loadingscreen.style.display = 'none';
        	loadingscreen.style.visibility='hidden';
		}
	}-*/;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		//NOTE: Commented this check out because it confuses people
		//Make sure that we warn the user about the unsupported platform or the browser, browser version
    	//if( ! doOSBrowserVersionWarning() ) {
	    	initializeSite();
	    //}

		//Remove the loading screen after the site started to execute
		removeOnLoadScreen();
	}
	
	/**
	 * Check that the given OS, Browser and Browser versions are supported, if not gives a warning message.
	 * The fact that the warning was displayed is stored in a cookie, so that the next time user
	 * opens the web site the warnin panel will not be diplayed any more.
	 * @return true if the OS-Browser-BrowserVersion warning is displayed otherwise false.
	 */
	protected boolean doOSBrowserVersionWarning() {
		boolean isWarned = false;
		
		if( ! SiteManager.isUnsupportedOSBrowserVersionWarned() ) {
			final BrowserDetect browserDetector = BrowserDetect.getBrowserDetect();
			if( browserDetector.isOSSupported() ) {
				if( browserDetector.isBrowserSupported() ) {
					if( browserDetector.isBrowserVersionSupported() ) {
						//The OS, Browser, and Browser version are supported!
						//Things are fine, we initialize the web site
						isWarned = false;
					} else {
						//Warn about the Browser Version;
						isWarned = true;
						initializeWarning( I18NManager.getErrors().theBrowserVersionIsNotSupported( browserDetector.getBrowser(),
																									browserDetector.getVersion(),
																									browserDetector.getSuggestedBrowserVersion() ) );
					}
				} else {
					//Warn about the Browser;
					isWarned = true;
					initializeWarning( I18NManager.getErrors().theBrowserIsNotSupported( browserDetector.getBrowser() ) );
				}
			} else {
				//Warn about the OS;
				isWarned = true;
				initializeWarning( I18NManager.getErrors().theOSIsNotSupported() );
			}
		}
		return isWarned;
	}
	
	public void initializeWarning( final String warningString ) {
    	//For the MS IE we add an error/warning message
    	//about that we do not really suport it
    	VerticalPanel warningPanel = new VerticalPanel();
    	warningPanel.setStyleName( CommonResourcesContainer.IE_ERROR_PANEL_STYLE_NAME );
    	warningPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
    	warningPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
    	//Add the error/warning message
    	HTML errorMessage = new HTML( warningString );
    	warningPanel.add( errorMessage );
   	
    	//Add the horizontal panel with the continue button and the localization list box
    	HorizontalPanel horizPanel = new HorizontalPanel();
    	horizPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
    	horizPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
    	//Add the continue button
    	Button continueButon = new Button();
    	continueButon.setText( I18NManager.getTitles().continueButtonTitle() );
    	continueButon.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
    	continueButon.addClickHandler( new ClickHandler() {
    		public void onClick( ClickEvent e) {
    	    	//Remember that the user was warned about using MS IE
    	    	SiteManager.setMSIEWarned();
    	    	//Initialize the web site
    	    	initializeSite();
    		}
    	});
    	horizPanel.add( continueButon );
    	//Add spacing
    	horizPanel.add( new HTML("&nbsp;") );
    	//Add the localization list box
    	horizPanel.add( InterfaceUtils.getLocaleSelectionPanel() );
    	
    	warningPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
    	warningPanel.add( horizPanel );
    	RootPanel.get().add( warningPanel );
	}
	
	/**
	 * Allows to initialize the entire site. First we clean the root panel,
	 * to remove what ever it is there, then we set the SiteManagaerUI instance
	 */
	private void initializeSite() {
		//Clean the root panel, just in case
		RootPanel.get().clear();
    	//Add the uncaught exception handler for production site version
    	GWT.setUncaughtExceptionHandler( new XCureUncaughtExceptionHandler() );
    	//Add the main site UI
    	RootPanel.get().add( SiteManagerUI.getInstance() );
	}
}
