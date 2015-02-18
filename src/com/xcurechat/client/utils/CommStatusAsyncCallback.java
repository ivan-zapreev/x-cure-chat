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
 */
package com.xcurechat.client.utils;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;

/**
 * @author zapreevis
 * Gives a wrapper for the regular call bak object, that allows to start and stop the progress bar
 * @param <T> the type of the object returned from the server
 */
public abstract class CommStatusAsyncCallback<T> implements AsyncCallback<T> {
	//The status panel to which we should indicate the call back status
	private final ServerCommStatusPanel loadingProgressBar;
	
	/**
	 * The call back object constructor. Starts the progress bar right away
	 * @param loadingProgressBar the server communication progress bar, can be null
	 */
	public CommStatusAsyncCallback( final ServerCommStatusPanel loadingProgressBar ) {
		this( loadingProgressBar, true );
	}
	
	/**
	 * The call back object constructor
	 * @param loadingProgressBar the server communication progress bar, can be null
	 * @param startProgress if true then the progress bar is started right away, if false then not
	 */
	public CommStatusAsyncCallback( final ServerCommStatusPanel loadingProgressBar, final boolean startProgress ) {
		this.loadingProgressBar = loadingProgressBar;
		if( startProgress ) {
			startProgressBar();
		}
	}
	
	/**
	 * Allows to start the progress bar
	 */
	public void startProgressBar() {
		if( loadingProgressBar != null ) {
			loadingProgressBar.startProgressBar();
		}
	}
	
	/**
	 * Allows to stop the progress bar
	 * @param isError true if the progress is stopped due to a reported error, otherwise false
	 */
	public void stopProgressBar( final boolean isError ) {
		if( loadingProgressBar != null ) {
			loadingProgressBar.stopProgressBar( isError );
		}
	}

	public final void onFailure(Throwable caught) {
		//Set the status bar to failure
		stopProgressBar( true );
		//Do the on success load action
		onFailureAct( caught );
	}

	public final void onSuccess(T result) {
		//Do the on success load action
		onSuccessAct( result );
		//Set the status bar to success
		stopProgressBar( false );
	}
	
	/**
	 * Has to be implemented to handle the server-reported exception
	 * @param caught the exception that happened while communicating to server
	 */
	public abstract void onFailureAct(Throwable caught);

	/**
	 * Has to be implemented to handle a successful server response
	 * @param result the server response object
	 */
	public abstract void onSuccessAct(T result);
}
