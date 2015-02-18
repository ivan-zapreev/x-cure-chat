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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.utils.FlashObjectWithJWPlayer;
import com.xcurechat.client.utils.SupportedFileMimeTypes;

/**
 * @author zapreevis
 * This class is responsible for showing the embedded radio player
 */
public class RadioPlayerComposite extends ActionGridDialog {
	//Contains the mapping of the names of the radios to the corresponding urls
	private static final Map<String, String> nameToUrl = new HashMap<String,String>();
	
	private static String getLoveRadioURL(final String name, final String bitrate) {
		return "http://stream.loveradio.ru:8000/"+name+"_"+bitrate+".mp3";
	}
	
	private static String getLoveRadioURLMono(final String name) {
		return getLoveRadioURL( name, "32_mono");
	}
	
	private static String getLoveRadioURLStereo(final String name) {
		return getLoveRadioURL( name, "96_stereo");
	}
	
	static {
		nameToUrl.put( "Love Top 40 (32 Kbps)", getLoveRadioURLMono("Top") );
		nameToUrl.put( "Love Top 40 (96 Kbps)", getLoveRadioURLStereo("Top") );
		nameToUrl.put( "Love Russian (32 Kbps)", getLoveRadioURLMono("Russian") );
		nameToUrl.put( "Love Russian (96 Kbps)", getLoveRadioURLStereo("Russian") );
		nameToUrl.put( "Love Radio (32 Kbps)", getLoveRadioURLMono("Loveradio") );
		nameToUrl.put( "Love Radio (96 Kbps)", getLoveRadioURLStereo("Loveradio") );
		nameToUrl.put( "Love Pride (32 Kbps)", getLoveRadioURLMono("Pride") );
		nameToUrl.put( "Love Pride (96 Kbps)", getLoveRadioURLStereo("Pride") );
		nameToUrl.put( "Love Gold (32 Kbps)", getLoveRadioURLMono("Gold") );
		nameToUrl.put( "Love Gold (96 Kbps)", getLoveRadioURLStereo("Gold") );
		nameToUrl.put( "Love Chill (32 Kbps)", getLoveRadioURLMono("Chill") );
		nameToUrl.put( "Love Chill (96 Kbps)", getLoveRadioURLStereo("Chill") );
		nameToUrl.put( "Love Alternative (32 Kbps)", getLoveRadioURLMono("Alternative") );
		nameToUrl.put( "Love Alternative (96 Kbps)", getLoveRadioURLStereo("Alternative") );
		nameToUrl.put( "Love Dance (32 Kbps)", getLoveRadioURLMono("Dance") );
		nameToUrl.put( "Love Dance (96 Kbps)", getLoveRadioURLStereo("Dance") );
		nameToUrl.put( "Love RNB (32 Kbps)", getLoveRadioURLMono("Rnb") );
		nameToUrl.put( "Love RNB (96 Kbps)", getLoveRadioURLStereo("Rnb") );
	}
	
	/**
	 * @author zapreevis
	 * This is a custom flash object class for the radio
	 */
	private class RadioFlashObject extends FlashObjectWithJWPlayer {
		public RadioFlashObject() {
			super( null, null, GWT.getModuleBaseURL() );
		}
		@Override
		protected String getSkinPluginFileName() {
			return "whotube.zip";
		}
		@Override
		protected String getMinimumPlayerHeight() {
			return "25";
		}
	}
	
	//The radio player html cntainer
	private final HTML radioHTML = new HTML();
	
	/**
	 * The basic constructor
	 */
	public RadioPlayerComposite() {
		super( false, false, false, null);
		
		//Populate the dialog
		populateDialog();
	}
	
	@Override
	protected boolean canCenterInCell( final int collspan, final Widget w ) {
		return w instanceof HTML;
	}

	@Override
	protected void populateDialog() {
		addNewGrid( 2, 3, false, "", false);
		
		//Add the radio-html widget to the panel
		this.addToGrid(this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 3, radioHTML, false, false);
		
		//Construct and add the listbox
		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		panel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		panel.setSize("100%", "100%");
		Label chooseLabel = new Label( titlesI18N.chooseRadioText() );
		chooseLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME );
		final ListBox chooseRadioListBox = new ListBox();
		for( String name : nameToUrl.keySet() ) {
			chooseRadioListBox.addItem( name );
		}
		chooseRadioListBox.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				setRadioStation( chooseRadioListBox.getItemText( chooseRadioListBox.getSelectedIndex() ) );
			}
		});
		final String defaultRadioStation = chooseRadioListBox.getItemText(0);
		panel.add( chooseLabel );
		panel.add( new HTML("&nbsp;"));
		panel.add( chooseRadioListBox );
		this.addToGrid(this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, panel, true, false);
		
		//Set the default radio station
		setRadioStation( defaultRadioStation );
		
		//Create navigation button "Close"
		Button closeButton = new Button();
		closeButton.setText( titlesI18N.closeButtonTitle() );
		closeButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		closeButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Close the dialog
				hide();
			}
		} );
		this.addToGrid( SECOND_COLUMN_INDEX, closeButton, false, true);
	}
	
	/**
	 * Allows to set a new radio station
	 * @param stationName the radio station name
	 */
	private void setRadioStation( final String stationName ) {
		final String radioURL = nameToUrl.get( stationName );
		if( radioURL != null ) {
			RadioFlashObject player = new RadioFlashObject();
			player.setMediaUrl( radioURL, SupportedFileMimeTypes.MP3_FLASH_MIME.getMainMimeType() );
			player.completeEmbedFlash( true );
			radioHTML.setHTML( player.toString() );
			
			this.setText( titlesI18N.radioTitlePrefix( stationName ) );
		}
	}

	@Override
	protected void actionLeftButton() {
		//DO NOTHING, the action buttons are not present
	}

	@Override
	protected void actionRightButton() {
		//DO NOTHING, the action buttons are not present
	}

}
