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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;

import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.Transliterator;

/**
 * @author zapreevis
 * This is a wrapper that allows to put the provided text box object above
 * the progress bar that reflect how many more symbols one can input.
 * Also it automatically binds the text object to the smiley selection dialog
 */
public class TextBaseTranslitAndProgressBar extends VerticalPanel implements Focusable, SmileSelectionDialogUI.SmileySelectionTarget{

	/**************************************************************************************/
	/********************THE STATIC METHODS FOR BINDING THE TRANSLITERATOR*****************/
	/**************************************************************************************/
	
	//The translit on/off clickable panel, is global
	private static FocusPanel translitOnOffPanel = new FocusPanel();
	//The click handler registration for the translit on/off panel button
	private static HandlerRegistration clickHandler = null;
	//The text box object we binded the transliterator to
	private static TextBoxBase bindedTransTextObject = null;
	//The place holder stored for the translit on/off panel
	private static SimplePanel bindedTransPanlePlaceHolder = null;

	//If true then the transliteration is on, otherwise it is off
	//This constant is global so every transliterator uses it
	private static boolean isTranslitOn = false;
	
	//The old panel for storing translitOnOffPanel
	private static HorizontalPanel translitPanel = null;
	
	/**
	 * Set the transliterator status button and tool tips
	 * based on the value stored in isTranslitOn
	 */
	private static void updateTranslitStatusAndToolTips() {
		//Set translit info and status
		if( isTranslitOn ) {
			translitOnOffPanel.removeStyleName( CommonResourcesContainer.TRANSLITERATION_IS_OFF_STYLE );
			translitOnOffPanel.addStyleName( CommonResourcesContainer.TRANSLITERATION_IS_ON_STYLE );
			translitOnOffPanel.setTitle( I18NManager.getTitles().translitIsOnButtonTip() );
			bindedTransTextObject.setTitle( I18NManager.getTitles().translitIsOnToolTip() );
		} else {
			translitOnOffPanel.removeStyleName( CommonResourcesContainer.TRANSLITERATION_IS_ON_STYLE );
			translitOnOffPanel.addStyleName( CommonResourcesContainer.TRANSLITERATION_IS_OFF_STYLE );
			translitOnOffPanel.setTitle( I18NManager.getTitles().translitIsOffButtonTip() );
			bindedTransTextObject.setTitle( I18NManager.getTitles().translitIsOffToolTip() );
		}
	}
	
	/**
	 * Allows to bind the transliterator and its visual objects
	 * @param bind true to bind things otherwise just to unbind the currently binded ones
	 * @param textObject
	 * @param newTranslitPanel
	 */
	private static void bindTranslitObjects( boolean bind, final TextBoxBase newTextObject,
											 final HorizontalPanel newTranslitPanel,
											 final SimplePanel placeHolder ) {
		//First un-bind any old bindings if any
		if( translitPanel != null ) {
			translitPanel.remove( translitOnOffPanel );
			translitPanel.add( bindedTransPanlePlaceHolder );
			translitPanel = null;
		}
		if( clickHandler != null ) {
			clickHandler.removeHandler();
			clickHandler = null;
		}
		//Remove the tool tip to avoid it being incorrect
		if( bindedTransTextObject != null ){
			bindedTransTextObject.setTitle( "" );
			bindedTransTextObject = null;
		}
		
		if( bind ) {
			//Store the new text object
			bindedTransTextObject = newTextObject;
			//Re-itialize the transliterator bar, the size is set here to account for the new 
			//TextBoxBase. We reduce the height by 2 pixels to account for the bar's borders
			translitOnOffPanel.setHeight( ( newTextObject.getOffsetHeight() - 6 )+"px");
			//Then bind the new things, first the visual objects
			if( newTranslitPanel != null ) {
				newTranslitPanel.remove( placeHolder );
				newTranslitPanel.add( translitOnOffPanel );
				/*Remove the panel from the tabulation sequence*/
				translitOnOffPanel.setTabIndex(-1);
			}
			translitPanel = newTranslitPanel;
			bindedTransPanlePlaceHolder = placeHolder;
			//Then the new click handler
			clickHandler = translitOnOffPanel.addClickHandler( new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					//Turn transliteration on and off
					isTranslitOn = ! isTranslitOn;
					//Un-bind or bind the transliterator if the transliteration is off/on
					Transliterator.bindTransliterator( isTranslitOn, newTextObject, progressBar );
					//Set the proper tool tips and the status of the translit button
					updateTranslitStatusAndToolTips();
					//Return focus to the text box base
					newTextObject.setFocus(true);
				}
			});
			//Set the proper tool tips and the status of the translit button
			updateTranslitStatusAndToolTips();
		}
		//Then the transliterator itself, NOTE: the bind parameter is
		//propagated so the next call should NOT be inside of the previous 
		//if clause, here we either finish binding or complete unbinding 
		Transliterator.bindTransliterator( bind && isTranslitOn, newTextObject, progressBar );
	}
	
	/**************************************************************************************/
	/******************THE STATIC METHODS FOR BINDING THE PROGRESS BAR*********************/
	/**************************************************************************************/
	
	//The reusable progress bar object
	private static final TextMaximumSizeProgress progressBar = new TextMaximumSizeProgress();
	//The vertical panel currently storing the progressBar
	private static VerticalPanel bondedProgressBarPanel = null;
	//The binded place holder for the progress bar
	private static SimplePanel bindedProgBarPlaceHolder = null;
	
	/**
	 * Allows to bind and unbind the progress bar from the text box base objects
	 * @param bind if true then first unbinds the old object and then binds the new one
	 * 			   if false then just unbinds the old object.
	 * @param textObject the new text box base to bind the progress bar with or null for bind==false
	 * @param maxObjectCapacity the maximum capacity of the provided text box base or 0 for bind==false
	 * @param progressBarPanel the panel that will store the progress bar or null for bind==false
	 * @param placeHolder the place holder that will be substituted with the progress bar or null for bind==false
	 */
	private static void bindProgressBar( final boolean bind, final TextBoxBase textObject,
										 final int maxObjectCapacity,
										 final VerticalPanel progressBarPanel,
										 final SimplePanel placeHolder ) {
		//First remove the old bindings if any
		if( bondedProgressBarPanel != null ) {
			bondedProgressBarPanel.remove( progressBar );
			bondedProgressBarPanel.add( bindedProgBarPlaceHolder );
			bondedProgressBarPanel = null;
			bindedProgBarPlaceHolder = null;
		}
		
		//Do new bindings
		if( bind ) {
			if( progressBarPanel != null ) {
				progressBarPanel.remove( placeHolder );
				progressBarPanel.add( progressBar );
				/*Remove the panel from the tabulation sequence*/
				progressBar.setTabIndex(-1);
			} 
			bindedProgBarPlaceHolder = placeHolder;
			bondedProgressBarPanel = progressBarPanel;
		}
		
		//Then bind the new object or finish un-binding the old one
		progressBar.bindProgressBar(bind, textObject, maxObjectCapacity);
	}
	
	/**************************************************************************************/
	/***********************THE NON STATIC METHODS OF THE CLASS****************************/
	/**************************************************************************************/
	
	//The transliteration panel with the text object and transliteration on/off panel button
	private HorizontalPanel textObjectTranslitButtonPanel = new HorizontalPanel();
	
	//The text object we provide a progress bar and transliterator for
	private final TextBoxBase textObject;
	
	//The place holder stored for the translit on/off panel
	private final SimplePanel transPanlePlaceHolder;
	
	//The place holder for the progress bar
	private final SimplePanel progressBarPlaceHolder = new SimplePanel();
	
	//The maximum text box base capacity
	private final int maxObjectCapacity;
	//True if the transliteration is allowed for this text box base
	private final boolean allowTranslit;
	
	/**
	 * The simple constructor. Sets the maximum length to the text box base if it is a TextBox.
	 * @param textObject the object we want to add the progress bar and transliteration for
	 * @param maxObjectCapacity the maximum text input object capacity in symbols
	 * @param allowTranslit if true then the transliteration is allowed, otherwise it is not
	 */
	public TextBaseTranslitAndProgressBar(final TextBoxBase textObject,
										  final int maxObjectCapacity,
										  final boolean allowTranslit){
		//Call the super constructor
		super();
		
		//Set the maximum capacity of the text base object
		//in case it is an instance of the TextBox
		if( textObject instanceof TextBox ) {
			( (TextBox) textObject).setMaxLength( maxObjectCapacity );
		}
		
		//Store the params
		this.textObject = textObject;
		this.maxObjectCapacity = maxObjectCapacity;
		this.allowTranslit = allowTranslit;
		
		//Initialize the panel
		this.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		this.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		if( allowTranslit ) {
			//Set alignments
			textObjectTranslitButtonPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
			textObjectTranslitButtonPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
			//Add the text object inside this panel
			textObjectTranslitButtonPanel.add( textObject );
			//Add the place holder
			transPanlePlaceHolder = new SimplePanel();
			transPanlePlaceHolder.setStyleName( CommonResourcesContainer.TRANSLITERATION_IS_PLACE_HOLDER_STYLE );
			textObjectTranslitButtonPanel.add( transPanlePlaceHolder );
			this.add( textObjectTranslitButtonPanel );
		} else {
			transPanlePlaceHolder = null;
			this.add( textObject );
		}
		progressBarPlaceHolder.setStyleName( CommonResourcesContainer.PROGRESS_BAR_PLACE_HOLDER_STYLE_NAME );
		this.add( progressBarPlaceHolder );
		
		//Add the focus handler to the text object
		textObject.addFocusHandler( new FocusHandler(){
			@Override
			public void onFocus(FocusEvent event) {
				bindComponents();
			}
		});
		//For Chrome and Safari we have a problem if the box is placed on a FocusPanel
		//then the focus can be lost forever and the text box base will become unusable
		if( BrowserDetect.getBrowserDetect().isChrome() || BrowserDetect.getBrowserDetect().isSafari() ) {
			textObject.addClickHandler( new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					//Therefore, for Chrome and Safari we stop the Click event propagation!
					event.stopPropagation();
					event.preventDefault();
					//And restore the focus
					setFocus(true);
				}
			} );
		}
	}
	
	/**
	 * Allows to bind components to the currently set text box base object
	 */
	private void bindComponents() {
		//If the transliteration on then we make the translit bar to appear
		bindTranslitObjects( allowTranslit, textObject,
							 textObjectTranslitButtonPanel,
							 transPanlePlaceHolder );
		//Bind the text box base object to the progress bar
		bindProgressBar( true, textObject, maxObjectCapacity,
						 TextBaseTranslitAndProgressBar.this,
						 progressBarPlaceHolder );
		//Bind the smiley selection dialog to the new text base
		SmileSelectionDialogUI.bind( TextBaseTranslitAndProgressBar.this );
	}
	
	/**
	 * When called sets the focus to the progress bar and forces the progress bar to update
	 */
	public void setFocusAndUpdate() {
		//Set the focus back to the text box base to 1. set bindings if
		//it is not binded 2. remain the cursor in the text box base element
		setFocus( true );
		//In fact we force the update on whatever progress bar object it is,
		//but the idea is that there is just one progress bar present in the
		//whole system and it is accessible through here
		progressBar.forceProgressUpdate();
	}
	
	/**
	 * The simple constructor. The transliteration is enabled if this constructor is used.
	 * Sets the macximum length to the text box base if it is a TextBox.
	 * @param textObject the object we want to add the progress bar and transliteration for
	 * @param maxObjectCapacity the maximum text input object capacity in symbols
	 */
	public TextBaseTranslitAndProgressBar(final TextBoxBase textObject, final int maxObjectCapacity){
		this( textObject, maxObjectCapacity, true );
	}
	
	@Override
	public void setFocus(boolean focused) {
		//Pass focus to the wrapped text object
		textObject.setFocus(focused);
	}
	
	/**
	 * Allows to get the TextBoxBase object wrapped inside this object
	 * @return
	 */
	public TextBoxBase getWrappedTextBoxBaseObj() {
		return textObject;
	}

	@Override
	public int getTabIndex() {
		//NOTE: Is not needed so is not implemented
		return 0;
	}

	@Override
	public void setAccessKey(char key) {
		//NOTE: Is not needed so is not implemented
	}

	@Override
	public void setTabIndex(int index) {
		//NOTE: Is not needed so is not implemented
	}

	/**
	 * This method should be basically called from the void addSmileStringToMessage( final String ); method of SmileSelectionDialogUI.SmileySelectionTarget
	 * @param stringToInsert the string to insert
	 * @param wrapper the wrapper object containing the text box base object into which we insert the smiley code
	 */
	public static void insertStringIntoTextBoxWrapper( final String stringToInsert, final TextBaseTranslitAndProgressBar wrapper ) {
		final TextBoxBase textObj = wrapper.getWrappedTextBoxBaseObj();
		//If the text box base is not null, is enabled and is not read-only
		if( textObj != null && textObj.isEnabled() && ! textObj.isReadOnly() ) {
			if( BrowserDetect.getBrowserDetect().isMSExplorer() ) {
				//In IE8 if we work with the TextBox then the cursor position
				//is set wrongly, unless the text object has focus in it. 
				textObj.setFocus(true);
			}
			final int currentCursorPos = textObj.getCursorPos();
			final String currentMessageText = textObj.getText();
			textObj.setText( currentMessageText.substring(0, currentCursorPos) + stringToInsert +
										 currentMessageText.substring(currentCursorPos, currentMessageText.length() ) );
			textObj.setCursorPos( currentCursorPos + stringToInsert.length() );
			//Force the progress bar update
			wrapper.setFocusAndUpdate();
		}
	}

	@Override
	public void addSmileStringToMessage(String smileyInternalCodeString) {
		TextBaseTranslitAndProgressBar.insertStringIntoTextBoxWrapper(smileyInternalCodeString, this);
	}
}
