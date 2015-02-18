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
package com.xcurechat.client.utils;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.google.gwt.i18n.client.LocaleInfo;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.SiteManagerUI;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.widgets.ActionLinkPanel;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.PriceTagWidget;

/**
 * @author zapreevis
 * This class contains some common GUI utils
 */
public class InterfaceUtils {
	
	//The length of the maximum visible part of the room name
	public static final int MAX_ROOM_NAME_TITLE_LENGTH = 20;

	private InterfaceUtils(){}
	
	/**
	 * Allows to get the locale selection Panel
	 * @return
	 */
	public static HorizontalPanel getLocaleSelectionPanel() {
		final HorizontalPanel localeSelectionPanel = new HorizontalPanel();
		localeSelectionPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		localeSelectionPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		localeSelectionPanel.setStyleName( CommonResourcesContainer.LOCALE_PANEL_STYLE );
		
		String[] localeNames = LocaleInfo.getAvailableLocaleNames();
		for( String localeValue : localeNames ) {
			if( !localeValue.equals( InterfaceUtils.DEFAULT_LOCALE_VALUE ) ) {
				String nativeName = LocaleInfo.getLocaleNativeDisplayName( localeValue );
				Image localeImage = new Image();
				localeImage.addStyleName( CommonResourcesContainer.LOCALIZATION_IMAGE_STYLE );
				localeImage.setUrl( ServerSideAccessManager.SITE_IMAGES_LOCATION + "locale_small_" + localeValue + ".png");
				localeImage.setTitle( nativeName );
				final String currLocaleValue = localeValue;
				localeImage.addClickHandler( new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						reloadWebSitePages( currLocaleValue );
					}
				});
				localeSelectionPanel.add( new HTML("&nbsp") );
				localeSelectionPanel.add( localeImage );
			}
		}
		
		return localeSelectionPanel;
	}
	
	/**
	 * Allows to reload the web-site pages with the given locale preserving the on-site position
	 * @param localeName the locale we want t reload with
	 */
	public static void reloadWebSitePages( final String localeName ) {
		//Get the current History token if any
		String CURRENT_HISTORY_TOKEN = History.getToken();
		if( ( CURRENT_HISTORY_TOKEN != null ) &&  !CURRENT_HISTORY_TOKEN.trim().isEmpty() ) {
			CURRENT_HISTORY_TOKEN =  "#" + CURRENT_HISTORY_TOKEN;
		} else {
			CURRENT_HISTORY_TOKEN = "";
		}
		Window.open( getHostPageLocation() + "?" + CommonResourcesContainer.LOCALE_PARAMETER_NAME + "=" + localeName + CURRENT_HISTORY_TOKEN , "_self", "");
	}
	
	/**
	 * Allows to reload the web-site pages with the current locale
	 */
	public static void reloadWebSitePages( ) {
		reloadWebSitePages( InterfaceUtils.getCurrentLocale() );
	}
		
	/**
	 * Get the URL of the page, without a hash or query string.
	* @return the location of the page
	*/
	public static native String getHostPageLocation()
	  /*-{
			var s = $doc.location.href;
			
			//Pull off any hash.
			var i = s.indexOf('#');
			if (i != -1){
				s = s.substring(0, i);
			}
			
			//Pull off any query string.
			i = s.indexOf('?');
			if (i != -1){
				s = s.substring(0, i);
			}
			
			//Ensure a final slash if non-empty.
			return s;
	}-*/;
	
	/**
	 * Allows to detect the Internet Explorer by Microsoft
	 * @return true if it is IE or if the user agent string is null
	 */
	public static boolean isMicrosoftIE() {
		return BrowserDetect.getBrowserDetect().isMSExplorer();
	}
	
	public static final String DEFAULT_LOCALE_VALUE = "default";

	/**
	 * Gets the current locale we are using, the identifier sting
	 * The "default" locale is russian and for this we return "ru"
	 * instead of "default".
	 */
	public static String getCurrentLocale() {
		String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
		if( currentLocale.equals( DEFAULT_LOCALE_VALUE ) ) {
			currentLocale = "ru";
		}
		return currentLocale;
	}

	/**
	 * Allows to suggest the default maximum height of the
	 * main view of the web-site such as the chat room or
	 * the introduction panel. Note that this method should
	 * be called after the widget is attached to the dom
	 * tree and is visualized.
	 * @param element the widget we want to suggest the height for
	 * @param defaultHeightInPercent if the height can not be determined
	 * relative to the absolute top of the provided element then this is
	 * the % of the browser's client view height that we recommend the
	 * element to use for its own height
	 * @return the suggested height of the element in pixels
	 */
	public static int suggestMainViewHeight(final Widget element, final int defaultHeightInPercent ) {
		//Update the height
		if( element.getAbsoluteTop() != 0 ) {
			//If we can detect the absolute top of the panel then we maximize the space 
			//used for the split panel (minus some pixels px for margins and decorations)
			return Window.getClientHeight() - element.getAbsoluteTop() - SiteManagerUI.CLIENT_AREA_HEIGHT_MARGIN_BOTTOM;
		} else {
			//TODO: This actually should not be happening, but in Opera 9.64 it does
			//If the absolute top is not detected then just use 95% of the visible height
			//In principle this might not be enough, but only on very big screens, up to
			//1680x1050 this works fine, may be one can set the height to 100% to cover all
			//cases I am not doing this right now because the issue seems to be with either
			//Opera or GWT 1.6 or both. Because 1. we do not get the absolute top value, and
			//second the split bar of the panel overflows, and I guess the split panel itself
			//too. Strangely enough all of the other elements placed inside the split panel
			//have proper positions and sizes at least in Opera 9.64 and Firefox 3.0.11
			//NOTE: The overflow problem for opera is fixed in CSS by forcing overflow:hidden
			//for the .gwt-HorizontalSplitPanel class in rooms_manager.css
			return (int) ( Window.getClientHeight() * ( ( double) defaultHeightInPercent ) / 100.0 );
		}
	}
	
	/**
	 * @author zapreevis
	 * Should be implemented by every tab content manager
	 */
	public interface TabContentManager {
		public void tabFillOut( final int index, final DecoratedTabPanel tabPanel );
		public void tabCleanUp( final int index, final DecoratedTabPanel tabPanel );
	}
	
	/**
	 * Allows to add tab selection listeners for preventing the user 
	 * accessing the priced tabs if he does not have enough money.
	 * This method also supportes re-setting the tab content on tab
	 * selection and cleaning up the tab when it gets un-selected.
	 * @param tabPanel the tab panel with the priced tab categories
	 * @param pricedTabTitles the mapping from the tab indexes to the price tag objects
	 * @param tabManager must contain the tab content manager instance 
	 */
	public static void addTabSectionsListener( final DecoratedTabPanel tabPanel,
											   final Map<Integer, PriceTagWidget> pricedTabTitles,
											   final TabContentManager tabManager ) {
		//Add the before selection handler
		tabPanel.addBeforeSelectionHandler( new BeforeSelectionHandler<Integer>(){
			@Override
			public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
				//Fill out the tab that gets selected with the new content 
				final int nextTabIndex = event.getItem();
				if( tabManager != null ) {
					//First do the clean-up, just in case
					tabManager.tabCleanUp( nextTabIndex, tabPanel );
					//Now do the fill-out
					tabManager.tabFillOut( nextTabIndex, tabPanel );
				} else {
					Window.alert("The tab with index " + nextTabIndex + " does not have content manager!");
				}
			}
		});
		
		//Add the selection handler
		tabPanel.addSelectionHandler( new SelectionHandler<Integer>(){
			@Override
			public void onSelection( SelectionEvent<Integer> event) {
				//Get the currently selected element
				final int newTabIndex = event.getSelectedItem();
				
				//Enable the current price title, if any
				final PriceTagWidget currentPriceTitle = pricedTabTitles.get( newTabIndex );
				if( currentPriceTitle != null ) {
					currentPriceTitle.setEnabled( true );
				}
				
				//Go over all available tabs
				for( int index = 0; index < tabPanel.getWidgetCount(); index ++) {
					//For all the tabs, other than the selected one
					if( index != newTabIndex ) {
						//Clean their content
						tabManager.tabCleanUp( index, tabPanel );
						//All the price tab titles should be disabled
						final PriceTagWidget otherPriceTitle = pricedTabTitles.get( index );
						if( otherPriceTitle != null ) {
							otherPriceTitle.setEnabled( false );
						}
					}
				}
			}
		} );
	}
	
	/**
	 * Allows to get a URL widget for a file download. Note that, this widget automatically
	 * adds a file-download-on server parameter to the provided file URL
	 * @param fileUrl the url of the file that we want to be downloadable.
	 * @param isOnlyLoggedIn if true then the link will be only enabled if the user is currently logged in
	 * @return the download url widget
	 */
	public static final Widget getDownloadLinkWidget( final String fileUrl, final boolean isOnlyLoggedIn ) {
		final UITitlesI18N i18nTitles = I18NManager.getTitles();
		return new ActionLinkPanel( ServerSideAccessManager.getDownloadIconURL(true),
								    i18nTitles.fileDownloadLinkTitle(),
								    ServerSideAccessManager.getDownloadIconURL(false),
								    i18nTitles.downloadOnlyIfLoggedInToolTip(),
								    i18nTitles.fileDownloadLinkTitle(),
								    new ClickHandler() {
										@Override
										public void onClick( ClickEvent event) {
											final String downloadURL = fileUrl + ServerSideAccessManager.getDownloadFileURLSuffix( true );
											final BrowserDetect browserDetector = BrowserDetect.getBrowserDetect();
											if( browserDetector.isOpera() || browserDetector.isSafari() ) {
												Window.open( downloadURL, "_blank", "" );
											} else {
												Window.open( downloadURL, "", "" );
											}
										}
								  	}, SiteManager.isUserLoggedIn() || !isOnlyLoggedIn, true );
	}

	/**
	 * Populates the label with the room name and sets a short 
	 * tip message to it. If the room's name is too long then  
	 * it is shortened but the rip contains the complete name.
	 * @param fullRoomName the full room name
	 */
	public static void populateRoomNameLabel( final String fullRoomName, final Label roomNameLabel) {
		//If the name is too long, then we cut it and add the hint
		if( fullRoomName.length() > MAX_ROOM_NAME_TITLE_LENGTH ) {
			roomNameLabel.setText( fullRoomName.substring(0, MAX_ROOM_NAME_TITLE_LENGTH - 3) + "..." );
		} else {
			roomNameLabel.setText( fullRoomName );
		}
		roomNameLabel.setTitle( fullRoomName );
	}

	/**
	 * Constructs the field label 
	 * @param fieldName the field name
	 * @param isCompulsory true if the field is compulsory
	 * @return the new label object
	 */
	public static Label getNewFieldLabel(String fieldName, boolean isCompulsory) {
		Label fieldLabel = new Label( fieldName +CommonResourcesContainer.FIELD_LABEL_SUFFIX);
		if( isCompulsory ){
			fieldLabel.setStyleName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
		} else {
			fieldLabel.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		}
		return fieldLabel;
	}

	/**
	 * Fills in the two parallel columns of fields and value labels 
	 * @return the field label
	 */
	public static Label addFieldValueToPanels( final VerticalPanel fieldPanel, final boolean isImportant,
							final String fieldName, final VerticalPanel valuePanel,
							final boolean isConstant, final Label valueLabel ) {
		Label fieldLabel = getNewFieldLabel( fieldName, isImportant );
		fieldPanel.add( fieldLabel );
		if( isConstant ) {
			valueLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME );
		} else {
			valueLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
		}
		valuePanel.add( valueLabel );
		return fieldLabel;
	}

	/**
	 * Populates the label with the room name and if needed sets a
	 * short tip message to it. If needed mesans that the room's name
	 * is too long, and so we truncate it.
	 * @param roomData
	 * @param isShortTipMsg if true then the short tip message with 
	 * the complete room name is generated and set to the label Title.
	 * @return the long room tip message if isShortTipMsg is false, else null
	 */
	public static String populateRoomNameLabel( final ChatRoomData roomData, final boolean isShortTipMsg, final Label roomNameLabel) {
		String longTipMsg = null;
		final String fullRoomName = ChatRoomData.getRoomName( roomData );
		//If the name is too long, then we cut it and add the hint
		if( fullRoomName.length() > MAX_ROOM_NAME_TITLE_LENGTH ) {
			roomNameLabel.setText( fullRoomName.substring(0, MAX_ROOM_NAME_TITLE_LENGTH - 3) + "..." );
		} else {
			roomNameLabel.setText( fullRoomName );
		}
		if( isShortTipMsg ) {
			roomNameLabel.setTitle( ChatRoomData.getRoomShortTipMsg( roomData ) );
		} else {
			longTipMsg = ChatRoomData.getRoomLongTipMsg( roomData );
		}
		return longTipMsg;
	}
}
