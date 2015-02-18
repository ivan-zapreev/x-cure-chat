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
 * The client-side utilities package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Window;

import com.xcurechat.client.SiteManagerUI;

import com.xcurechat.client.i18n.I18NManager;

/**
 * @author zapreevis
 * This class is used to provide delayed site components loading to speed up the site loading performance.
 */
public abstract class SplitLoad implements RunAsyncCallback {
	//If true then the glass panel will be displayed, otherwise not
	private final boolean isWithGlassPanel;

	/**
	 * The basic constructor, the glass panel is will not be displayed
	 */
	public SplitLoad() {
		this( false );
	}
	
	/**
	 * The basic constructor
	 * @param isWithGlassPanel if true then the glass panel will be displayed, otherwise not
	 */
	public SplitLoad(final boolean isWithGlassPanel) {
		this.isWithGlassPanel = isWithGlassPanel;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.core.client.RunAsyncCallback#onFailure(java.lang.Throwable)
	 */
	@Override
	public final void onFailure(Throwable reason) {
		//Remove the progress bar
		if( isWithGlassPanel ) {
			SiteManagerUI.setGlassPanelVisible( false );
		}
		
		//Provide a proper error dialog with a localized error
		Window.alert( I18NManager.getErrors().serverDataLoadingFalied() );
		
		//Call the business of recovery
		recover();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.core.client.RunAsyncCallback#onSuccess()
	 */
	@Override
	public final void onSuccess() {
		//Remove the progress bar
		if( isWithGlassPanel ) {
			SiteManagerUI.setGlassPanelVisible( false );
		}
		
		//Call the business logic
		execute();
	}

	/**
	 * Allows to execute the delay-loaded code specified in the onSuccess method implementation
	 */
	public final void loadAndExecute() {
		//Start the progress bar
		if( isWithGlassPanel ) {
			SiteManagerUI.setGlassPanelVisible( true );
		}
		
		//Start loading of the java script
		GWT.runAsync( this );
	}
	
	/**
	 * Is called when the java script is successfully loaded and the business 
	 * logic specified in the implementation of this method can be executed. 
	 */
	public abstract void execute();
	
	/**
	 * Is called when the java script is failed to loaded. If overridden this method can contain the recovery logic 
	 */
	public void recover() {}
}
