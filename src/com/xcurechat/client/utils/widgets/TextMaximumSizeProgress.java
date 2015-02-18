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
package com.xcurechat.client.utils.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.HTML;

import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * This represents a progress bar which is responsible to visually show
 * how much more text we can put into some TextBoxBase inheriting object
 */
public class TextMaximumSizeProgress extends Composite implements KeyPressHandler, KeyDownHandler, KeyUpHandler {
	//The object that will get text input into it 
	private TextBoxBase textObject = null;
	//The maximum text object capacity in cymbols
	private int MAX_TEXT_OBJECT_SYMB_CAPACITY = 0;
	
	//The height of the progress bar in pixels
	private static final int HEIGHT_INNER_IN_PIXELS = 3;
	//The progress bar length in pixels is set relative to the textObject offset width
	private int PROGRESS_BAR_LENGTH_IN_PIXELS = 0;
	//True if we have already initialized the progress bar
	private boolean isProgressBarInitialized = false;
	//Contains the tooltip string, to be shown in the info dialog
	private String infoToolTipMessage = "";
	///Contains true if we exceeded the maximum length
	private boolean isMaximumLengthExceeded = false; 
	
	//The progress bar is made of a panel, we need a focus panel for click listeners
	private FocusPanel progressBarPanel = new FocusPanel();
	private HTML leftBar = new HTML();
	private HTML rightBar = new HTML();
	
	//The list of handler registrations for the currently binded text box base
	private List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();
	
	/**
	 * This constructor should be only visible inside of the package.
	 * Note that to initialize the progress bar one has to bind it to the text base object.
	 * This should be done using the method bindProgressBar(...).
	 */
	TextMaximumSizeProgress( ) {
		//Initialize the progress bar content first
		HorizontalPanel innerProgressBarPanel = new HorizontalPanel(); 
		innerProgressBarPanel.setStyleName( CommonResourcesContainer.PROGRESS_BAR_STYLE_NAME );
		innerProgressBarPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		innerProgressBarPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		innerProgressBarPanel.add( leftBar );
		innerProgressBarPanel.add( rightBar );
		progressBarPanel.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e) {
				if( isMaximumLengthExceeded ) {
					(new SplitLoad( true ) {
						@Override
						public void execute() {
							//Report the error
							ErrorMessagesDialogUI.openErrorDialog( infoToolTipMessage );
						}
					}).loadAndExecute();
				} else {
					(new SplitLoad( true ) {
						@Override
						public void execute() {
							//Open the info dialog
							InfoMessageDialogUI.openInfoDialog( infoToolTipMessage );
						}
					}).loadAndExecute();
				}
			}
		});
		progressBarPanel.add( innerProgressBarPanel );
		
		//Initialize the composite
		initWidget( progressBarPanel );
	}
	
	/**
	 * Allows to bind and un-bind the progress bar to the
	 * text box base with the provided maximum text capacity.
	 * @param bind if true the un-binds the currently binded text box and binds
	 *			   the new one, if false the only unbinds the text object.
	 * @param textObject the text object to bind or null
	 * @param maxObjectCapacity the maximum capacity of the binded object or 0
	 */
	public void bindProgressBar(final boolean bind, final TextBoxBase textObject, final int maxObjectCapacity) {
		//Un-bind the old text box base object if any
		Iterator<HandlerRegistration> iter = handlerRegistrations.iterator();
		while( iter.hasNext() ) {
			iter.next().removeHandler();
		}
		handlerRegistrations.clear();
		if( this.textObject != null ) {
			this.textObject = null;
		}
		MAX_TEXT_OBJECT_SYMB_CAPACITY = 0;
		
		//Bind the parameters
		if( bind ) {
			//Bind the new elements
			this.textObject = textObject;
			//Register handlers and store their registrations
			handlerRegistrations.add( textObject.addKeyPressHandler( this ) );
			handlerRegistrations.add( textObject.addKeyUpHandler( this ) );
			handlerRegistrations.add( textObject.addKeyDownHandler( this ) );
			//Store the maximum capacity object
			MAX_TEXT_OBJECT_SYMB_CAPACITY = maxObjectCapacity;
			//Re-initialize the progress bar
			isProgressBarInitialized = false;
			forceProgressUpdate();
		}
	}
	
	/**
	 * Allows to set the tabulation sequence index for the enclosed FocusPanel
	 * @param index the index to be set
	 */
	public void setTabIndex(final int index) {
		progressBarPanel.setTabIndex(index);
	}
	
	private String getBar(double percent, int red, int green, int blue, boolean isTopInt) {
		double width_double = ( ( (double) (percent * PROGRESS_BAR_LENGTH_IN_PIXELS) ) / 100.0 );
		int width;
		if( isTopInt ) {
			width = (int) Math.ceil( width_double );
		} else {
			width = (int) Math.floor( width_double );
		}
		return "<div style=\"width:" + width + "px; height: " + HEIGHT_INNER_IN_PIXELS +
				"px; background-color: RGB(" + red + ","+ green + "," + blue + ");\"></div>";
	}
	
	private String getLeftBar(double percent){
		return getBar(percent, 122, 0, (int) ( 255.0 * ( (double) (100 - percent) ) / 100.0 ), true);
	}
	
	private String getRightBar(double percent){
		return getBar(percent, 122, 0, 255, false);
	}
	
	//The old text length value
	private int oldLength = -1;
	
	private void onChange() {
		//Try to initialize the progress bar
		initializeProgressBar();
		
		//if the progress bar is initialized then
		if( isProgressBarInitialized ) {
			int length = ( textObject.getText() == null ? 0 : textObject.getText().length() );
			if( oldLength != length ) {
				if( length > MAX_TEXT_OBJECT_SYMB_CAPACITY ) {
					infoToolTipMessage = I18NManager.getTitles().exceedingTextLength( length - MAX_TEXT_OBJECT_SYMB_CAPACITY );
					length = MAX_TEXT_OBJECT_SYMB_CAPACITY;
					isMaximumLengthExceeded = true;
				} else {
					infoToolTipMessage = I18NManager.getTitles().remainingTextLength( MAX_TEXT_OBJECT_SYMB_CAPACITY - length );
					isMaximumLengthExceeded = false;
				}
				progressBarPanel.setTitle( infoToolTipMessage );
				final double percent = ( (double) 100.0 * length ) / MAX_TEXT_OBJECT_SYMB_CAPACITY ; 
				leftBar.setHTML( getLeftBar( percent ) );
				rightBar.setHTML( getRightBar( 100.0 - percent ) );
				oldLength = length;
			}
		}
	}

	/**
	 * When called forced the progress bar update
	 */
	public void forceProgressUpdate() {
		onChange();
	}
	
	/**
	 * Does the progress bar initialization, if not initialized earlier
	 */
	private void initializeProgressBar() {
		if( !isProgressBarInitialized && ( textObject != null ) ) {
			//Reset the old length value, because the progress
			//bar is reused for different text box base objects
			oldLength = -1;
			//Initialize the progress bar
			PROGRESS_BAR_LENGTH_IN_PIXELS = textObject.getOffsetWidth();
			//First make sure that the width is not zero, if it is then the
			//object is not yet visible so we can not initialize
			if( PROGRESS_BAR_LENGTH_IN_PIXELS != 0 ) {
				progressBarPanel.setWidth( PROGRESS_BAR_LENGTH_IN_PIXELS + "px");
				isProgressBarInitialized = true;
			}
		}
	}
	
    public void onKeyDown(KeyDownEvent e) {
		onChange();
    }

    public void onKeyPress(KeyPressEvent e) {
		onChange();
    }

    public void onKeyUp(KeyUpEvent e) {
		onChange();
    }
}
