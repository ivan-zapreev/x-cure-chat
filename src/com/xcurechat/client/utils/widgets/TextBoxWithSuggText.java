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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils.widgets;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.FocusEvent;

import com.google.gwt.dom.client.Element;

import com.google.gwt.user.client.ui.TextBox;

/**
 * @author zapreevis
 * This class extends the regular text box and allows
 * to set the text box's help message visible in the
 * text box in gray color and disappearing as soon
 * and the text box is focused.
 */
public class TextBoxWithSuggText extends TextBox {
	//Stores the helper text set in the text box when its true value
	//is not set, not null, if not set then is an empty string
	private final String helperText;
	
	/**
	 * The simple constructor 
	 */
	public TextBoxWithSuggText( final String helperText ) {
		super();
		//Store the helper text
		this.helperText = ( helperText == null ? "" : helperText.trim() );
		//Initialize the text box and the listeners;
		initialize();
	}

	/**
	 * This constructor may be used by subclasses to explicitly use an existing
	 * element. This element must be an &lt;input&gt; element whose type is 'text'.
	 * @param element the element to be used
	 */
	public TextBoxWithSuggText( final Element element, final String helperText) {
		super(element);
		//Store the helper text
		this.helperText =  ( helperText == null ? "" : helperText.trim() );
		//Initialize the text box and the listeners;
		initialize();
	}
	
	/**
	 * Allows to initialize the text box by setting up its listeners and styles.
	 */
	private void initialize() {
		//Set the base values and styles
		super.setStyleName( CommonResourcesContainer.GWT_TEXT_BOX_STYLE );
		this.addStyleName( CommonResourcesContainer.USER_DIALOG_SUGG_TEXT_BOX_STYLE );
		this.setText( helperText );
		//On gaining the focus
		addFocusHandler(new FocusHandler(){
			public void onFocus(FocusEvent event) {
				//If the focus is obtained and the text box value is set to empty if
				//the user text was not set, i.e. he have the helper message there
				if( TextBoxWithSuggText.super.getText().trim().equals( helperText ) ){
					TextBoxWithSuggText.super.setText( "" );
				}
				//Remove the suggestion style making the text be in another color
				removeStyleName( CommonResourcesContainer.USER_DIALOG_SUGG_TEXT_BOX_STYLE );
			}
		});
		//On loosing the focus
		addBlurHandler(new BlurHandler(){
			public void onBlur(BlurEvent e) {
				//If the text box looses the focus and the text is not set
				//then we set the helper text and the corresponding style 
				if( TextBoxWithSuggText.super.getText().trim().isEmpty() ){
					TextBoxWithSuggText.this.setText( null );
				}
			}
		});
	}
	
	/**
	 * This is forbidden because it can mess up the helper message style
	 * WARNING: It is forbidden to set style to the TextBoxWithSuggText class object, use addStyleName instead!
	 * This method does nothing!
	 */
	@Override
	public void setStyleName( final String styleName ) {
		//WARNING: It is forbidden to set style to the TextBoxWithSuggText class object, use addStyleName instead!
	}
	
	/**
	 * @return the text set by the user or an empty string if the text box contains the helper message
	 */
	@Override
	public String getText() {
		final String text = super.getText();
		if( text.trim().equals( helperText ) ){
			return "";
		} else {
			return text;
		}
	}
	
	/**
	 * @return the text set by the user or an empty string if the text box contains the helper message
	 */
	@Override
	public String getValue() {
		return getText();
	}
	
	/**
	 * Allows to set the text into the text box
	 * @param text the text to be set, if null or an empty string or a helper
	 * text then we set the helper message on and the proper helper style
	 */
	@Override
	public void setText( final String text ) {
		//If some one sets a null, empty, or helper text string,
		//then the text box is reset to be "empty"
		if( ( text == null ) || text.trim().equals("") || text.trim().equals( helperText ) ) {
			addStyleName( CommonResourcesContainer.USER_DIALOG_SUGG_TEXT_BOX_STYLE );
			super.setText( helperText );
		} else {
			removeStyleName( CommonResourcesContainer.USER_DIALOG_SUGG_TEXT_BOX_STYLE );			
			super.setText( text );
		}
	}
	
	/**
	 * Allows to set the text into the text box
	 * @param text the text to be set, if null or an empty string or a helper
	 * text then we set the helper message on and the proper helper style
	 */
	@Override
	public void setValue( final String text ) {
		setText( text );
	}
}
