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
 * The client utilities package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils.widgets;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

import com.google.gwt.http.client.URL;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.dialogs.system.messages.QuestionMessageDialogUI;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIInfoMessages;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;

import com.xcurechat.client.utils.AvatarSpoilersHelper;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * This class represents user avatar image composite
 */
public class UserAvatarImageWidget extends Composite implements MouseOverHandler, MouseOutHandler {
	
	private static final String PRANK_ACTION_IMAGE_NAME = "prank";
	private static final String CLEAR_ACTION_IMAGE_NAME = "clear";
	private static final String ACTION_IMAGE_EXT = ".png";
	
	//The internationalization class
	private static final UITitlesI18N i18nTitles = I18NManager.getTitles();
	private static final UIInfoMessages i18nInfo = I18NManager.getInfoMessages();
	
	//The next time the avatar images will we re-requested from the server
	private static long myAvatarUpdateTimeMillisec = System.currentTimeMillis(); 
	private static long otherAvatarsUpdateTimeMillisec = System.currentTimeMillis();
	
	/**
	 * @author zapreevis
	 * This interface should be implemented by the UI entities that sould react on the avatar spoiler change
	 */
	public interface AvatarSpoilerChangeListener {
		/**
		 * Will be invoked if the interface is registered, when the spoiler of the avatar of user is changed.
		 * @param userID the id of the user whoe's avatar spoiler changed
		 * @param spoilerID the new id of the avatar's spoiler
		 * @param spoilerExpDate the new expiration date of the avatar's spoiler
		 */
		public void avatarSpoilerChanged( final int userID, final int spoilerID, final Date spoilerExpDate );
	}
	
	/**
	 * @author zapreevis
	 * THis class is needed to ask the the user if he wants to pay for removing the prank
	 */
	private class CleanPrankQuestionDialogUI extends QuestionMessageDialogUI {
		private final int priceInGoldPieces;
		public CleanPrankQuestionDialogUI( final int priceInGoldPieces ) {
			//Call the super constructor
			super();
			//Store the price
			this.priceInGoldPieces = priceInGoldPieces;
			//Fill dialog with data
			populateDialog();
		}
		
		@Override
		public boolean isHtmlQuestionText() {
			return true;
		}

		@Override
		protected String getDialogQuestion( ) {
			return i18nInfo.cleanPrankDialogQuestion( priceInGoldPieces );
		}

		@Override
		protected String getDialogTitle() {
			return i18nInfo.cleanPrankDialogTitle();
		}

		@Override
		protected void negativeAnswerAction() {
			//Close the dialogs
			hide();
			//Hide the controls
			activateControls( false, false );
		}

		@Override
		protected void positiveAnswerAction() {
			//Close the dialogs
			hide();
			//Set the prank
			setUserPrankedStatus( userID, spoilerID, true );
		}
	}
	
	//The main vertical panel of the avatar
	private VerticalPanel vpanel = new VerticalPanel();
	
	//Stores the avatar image
	private Image avatarImage = null;
	
	//Stores the avatar-spoiler image
	private Image avatarSpoilerImage = null;
	
	//The action link panels for the avatar's prank and clean
	private ActionLinkPanel prankActionPanel = null;
	private ActionLinkPanel clearActionPanel = null;
	
	//The avatar data
	private int userID = ShortUserData.UNKNOWN_UID;
	private boolean isMale = true;
	private int spoilerID = AvatarSpoilersHelper.UNDEFILED_AVATAR_SPOILER_ID;
	private Date spoilerExpDate = null;
	
	//Remembers if the prank controls are enabled/disabled
	private boolean prankControlsEnabled = true;
	
	/**
	 * The basic constructor
	 */
	public UserAvatarImageWidget() {
		//The simple panel here is needed for stupid Opera and later I started using if for the mouse move listeners
		FocusPanel panel = new FocusPanel();
		panel.add( vpanel );
		panel.addMouseOverHandler( this );
		panel.addMouseOutHandler( this );
		
		//Initialize the action images
		String actionImageUrlEnbl = CommonResourcesContainer.USER_AVATAR_CONTROL_IMAGES_LOCATION_PREFIX + PRANK_ACTION_IMAGE_NAME + ACTION_IMAGE_EXT;
		prankActionPanel = new ActionLinkPanel( actionImageUrlEnbl, i18nTitles.clickToPrankTheUserToolTip(),
												ServerSideAccessManager.getActivityImageURL( ), "", null, new ClickHandler() {
													@Override
													public void onClick( ClickEvent event) {
														//Ensure lazy loading
														( new SplitLoad( true ) {
															@Override
															public void execute() {
																//Open the prank selection dialog
																AvatarPrankSelectionDialogUI dialog = new AvatarPrankSelectionDialogUI( UserAvatarImageWidget.this, userID, isMale );
																dialog.show();
																dialog.center();
																//Hide the controls
																activateControls( false, false );
															}
														}).loadAndExecute();
														//Stop the event from being propagated
														event.stopPropagation(); event.preventDefault();
													}
												}, false, true );
		prankActionPanel.addStyleName( CommonResourcesContainer.USER_AVATAR_PRANK_ACTION_PANEL_STYLE );
		actionImageUrlEnbl = CommonResourcesContainer.USER_AVATAR_CONTROL_IMAGES_LOCATION_PREFIX + CLEAR_ACTION_IMAGE_NAME + ACTION_IMAGE_EXT;
		clearActionPanel = new ActionLinkPanel( actionImageUrlEnbl, i18nTitles.clickToClearThePrankToolTip(),
												ServerSideAccessManager.getActivityImageURL( ), "", null, new ClickHandler() {
													@Override
													public void onClick( ClickEvent event) {
														//Ensure lazy loading
														( new SplitLoad( true ) {
															@Override
															public void execute() {
																//Open the confirmation dialog
																final int price = AvatarSpoilersHelper.getSpoilerPrice( spoilerID );
																CleanPrankQuestionDialogUI dialog = UserAvatarImageWidget.this.new CleanPrankQuestionDialogUI( price );
																dialog.show();
																dialog.center();
															}
														}).loadAndExecute();
														//Stop the event from being propagated
														event.stopPropagation(); event.preventDefault();
													}
												}, false, true );
		clearActionPanel.addStyleName( CommonResourcesContainer.USER_AVATAR_NOPRANK_ACTION_PANEL_STYLE );
		//Hide the action buttons
		prankActionPanel.setVisible(false);
		clearActionPanel.setVisible(false);
		
		//Initialize the widget to be a vertical panel
		panel.setStyleName( CommonResourcesContainer.AVATAR_IMAGE_WIDGET_STYLE );
		initWidget( panel );
	}
	
	/**
	 * The basic constructor
	 */
	public UserAvatarImageWidget( final ShortUserData userData ) {
		this();
		
		//Set the user data
		updateAvatarData( userData );
	}
	
	/**
	 * Allows to update the user data for the widget, does not update the avatar
	 * @param userData the user date
	 * @param update true if the avatar should be updated
	 */
	public void updateAvatarData( final ShortUserData userData ) {
		updateAvatarData( userData, false );
	}
	
	/**
	 * Allows to update the user data for the widget
	 * @param userData the user date
	 * @param update true if the avatar should be updated
	 */
	public void updateAvatarData( final ShortUserData userData, final boolean update ) {
		//Update the avatar image itself
		updateAvatarImage( userData.getUID(), userData.isMale(), update  );
		//Update the 
		updateThisAvatarSpoiler( userData.getAvatarSpoilerId(), userData.getAvatarSpoilerExpDate() );
	}
	
	/**
	 * Allows to get the id of the user fow whoem this avatar corresponds
	 * @return the if of the user
	 */
	public int getUserID() {
		return userID;
	}
	
	/**
	 * Allows to update the avatar spoiler based on the spoiler id and the expiration date
	 * @param avatarSpoilerId the avatar spoiler id
	 * @param avatarSpoilerExpDate the avatar expiration date
	 */
	public void updateThisAvatarSpoiler( final int spoilerID, final Date spoilerExpDate ) {
		if( AvatarSpoilersHelper.isAvatarSpoilerActive( spoilerID, spoilerExpDate ) ) {
			//If the avatar spoiler is not expired
			if( avatarSpoilerImage == null ) {
				//If the avatar spoiler image is not present then create it
				avatarSpoilerImage = new Image( );
				avatarSpoilerImage.setStyleName( CommonResourcesContainer.USER_AVATAR_SPOILER_IMAGE_STYLE );
				vpanel.add( avatarSpoilerImage );
			}
			avatarSpoilerImage.setUrl( URL.encode( GWT.getModuleBaseURL() + AvatarSpoilersHelper.getSpoilerRelativeURL(spoilerID) ) );
			this.spoilerID = spoilerID;
			this.spoilerExpDate = spoilerExpDate;
		} else {
			//If the avatar spoiler is expired
			if( avatarSpoilerImage != null ) {
				//If the avatar spoiler image is present then remove it
				vpanel.remove( avatarSpoilerImage );
				avatarSpoilerImage = null;
			}
			this.spoilerID = AvatarSpoilersHelper.UNDEFILED_AVATAR_SPOILER_ID;
			this.spoilerExpDate = null;
		}
	}

	/**
	 * Allows to update the avatar's image
	 * @param userID the id of the user we want a picture of
	 * @param isMale true if this is a male's avatar, it is only
	 * neded if the user's avatar is not set.
	 * @param update true if we want to update the image, in this
	 * case the URL will be changed.
	 */
	public void updateAvatarImage( final int userID, final boolean isMale, final boolean update ) {
		updateAvatarImage( getProfileAvatarURL( userID, isMale, update ) );
		this.userID = userID;
		this.isMale = isMale;
	}

	/**
	 * Allows to update the avatar's image
	 * @param url the avatar's url to be set 
	 */
	public void updateAvatarImage( final String url ) {
		//Check if the avatar image is there
		if( avatarImage == null ) {
			avatarImage = new Image();
			avatarImage.setStyleName( CommonResourcesContainer.AVATAR_IMAGE_STYLE );
			vpanel.add( avatarImage );
			//Add the spoiler management buttons
			vpanel.add( prankActionPanel );
			vpanel.add( clearActionPanel );
		}
		avatarImage.setUrl( url );
	}
	
	/**
	 * Return the URL for the user avatar image
	 * @param userID the id of the user we want a picture of
	 * @param isMale true if this is a male's avatar, it is only
	 * neded if the user's avatar is not set.
	 * @param update true if we want to update the image, in this
	 * case the URL will be changed.
	 * @return the required URL
	 */
	private static String getProfileAvatarURL( final int userID, final boolean isMale, final boolean update ) {
		long time; final boolean isMyAvatar = (userID == SiteManager.getUserID());
		if( isMyAvatar ) {
			if( update || myAvatarUpdateTimeMillisec < System.currentTimeMillis() ) {
				myAvatarUpdateTimeMillisec += 60000;
			}
			time = myAvatarUpdateTimeMillisec;
		} else {
			if( update || otherAvatarsUpdateTimeMillisec < System.currentTimeMillis() ) {
				otherAvatarsUpdateTimeMillisec += 60000;
			}
			time = otherAvatarsUpdateTimeMillisec;
		}
		
		return	URL.encode( GWT.getModuleBaseURL() +
				ServerSideAccessManager.USER_PROFILE_AVATAR_SERVLET_CONTEXT + ServerSideAccessManager.URL_QUERY_DELIMITER +
				ServerSideAccessManager.FOR_USER_ID_AVATAR_SERVLET_PARAM + ServerSideAccessManager.SERVER_PARAM_NAME_VAL_DELIM + userID + ServerSideAccessManager.SERVLET_PARAMETERS_DELIMITER +
				ServerSideAccessManager.FOR_USER_GENDER_AVATAR_SERVLET_PARAM + ServerSideAccessManager.SERVER_PARAM_NAME_VAL_DELIM + ( isMale ? "1" : "0") + ServerSideAccessManager.SERVLET_PARAMETERS_DELIMITER +
				ServerSideAccessManager.DUMMY_TIME_SERVLET_PARAM + ServerSideAccessManager.SERVER_PARAM_NAME_VAL_DELIM + time );
		//NOTE: The last parameter is only used for updating the avatar images
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		if( SiteManager.isUserLoggedIn() ) {
			//If the user is logged in, then
			if( SiteManager.getUserID() == userID ) {
				//If this is the avatar of the user who is logged in
				if( AvatarSpoilersHelper.isAvatarSpoilerActive( spoilerID, spoilerExpDate ) ) {
					//If this user is pranked  he can clean his prank
					activateControls( false , true );
				} else {
					//If this user is NOT pranked then there is nothing to be done
					activateControls( false , false );
				}
			} else {
				//If this is an avatar of some other user
				if( AvatarSpoilersHelper.isAvatarSpoilerActive( spoilerID, spoilerExpDate ) ) {
					//If the other user is pranked then one can clean his prank or set a new one
					activateControls( true , true );
				} else {
					//If the other user is NOT pranked then one can prank him
					activateControls( true , false );
				}
			}
		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		//Hide the action buttons
		activateControls(false, false);
	}

	/**
	 * Allows to enable/set visible and disable/set invisible the controls for the avatar
	 * Set everything to disabled if prankControlsEnabled == false, otherwise works as expected.
	 * @param activatePrank true to activate/show, false to disable/hide
	 * @param activateClear true to activate/show, false to disable/hide
	 */
	private void activateControls(final boolean activatePrank, final boolean activateClear) {
		prankActionPanel.setEnabled( prankControlsEnabled && activatePrank );
		prankActionPanel.setVisible( prankControlsEnabled && activatePrank );
		clearActionPanel.setEnabled( prankControlsEnabled && activateClear );
		clearActionPanel.setVisible( prankControlsEnabled && activateClear );
	}
	
	/**
	 * Allows to set the prank controls on and off
	 * @param enabled true to enable, false to disable
	 */
	public void enablePrankControls( final boolean enabled ) {
		prankControlsEnabled = enabled;
		if( ! prankControlsEnabled ) {
			activateControls( false, false );
		}
	}
	
	/**
	 * Allows to set the prank status for the user, both in the client and on the server
	 * @param prankedUserId the id of the user that is pranked
	 * @param prankID the id of the prank itself
	 * @param isRemove true if we want to remove the prank
	 */
	private void setUserPrankedStatus( final int prankedUserId, final int prankID, final boolean isRemove ) {
		//Just disable the controls but do not hide them
		prankActionPanel.setEnabled( false );
		clearActionPanel.setEnabled( false );
		
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				//Update the prank data on the server 
				AsyncCallback<Date> getProfileCallBack = new AsyncCallback<Date>() {
					public void onSuccess( Date prankExpDate ) {
						//Set the new prank status everywhere in the client
						notifyAvatarChangeListeners( userID, prankID, prankExpDate );
						//Update this avatar widget as well
						updateThisAvatarSpoiler( prankID, prankExpDate );
						
						//Hide the control buttons
						activateControls( false, false);
					}
					public void onFailure(final Throwable caught) {
						//Report the error
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								ErrorMessagesDialogUI.openErrorDialog( caught );
							}
						}).loadAndExecute();
						//Do the recovery
						recover();
					}
				};
				UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
				userManagerObject.setAvatarPrank( SiteManager.getUserID(), SiteManager.getUserSessionId(),
												  prankedUserId, prankID, isRemove, getProfileCallBack );
			}
			@Override
			public void recover() {
				//Hide the control buttons
				activateControls( false, false);
			}
		}).loadAndExecute();
	}
	
	//The list of registered avatar spoilers
	public static Set<AvatarSpoilerChangeListener> avatarSpoilerChangeListeners = new HashSet<AvatarSpoilerChangeListener>();
	
	/**
	 * Allows to a new avatar spoiler change listener
	 * @param listener the new listener to be added
	 */
	public static void addAvatarSpoilerChangeListener( AvatarSpoilerChangeListener listener ) {
		avatarSpoilerChangeListeners.add( listener );
	}
	
	/**
	 * Allows to a remove an old avatar spoiler change listener
	 * @param listener the new listener to be removed
	 */
	public static void removeAvatarSpoilerChangeListener( AvatarSpoilerChangeListener listener ) {
		avatarSpoilerChangeListeners.remove( listener );
	}
	
	/**
	 * Should be invoked when the spoiler of the avatar of user is changed. It notifies all the avatar poiler change listeners
	 * @param userID the id of the user whoe's avatar spoiler changed
	 * @param spoilerID the new id of the avatar's spoiler
	 * @param spoilerExpDate the new expiration date of the avatar's spoiler
	 */
	public static void notifyAvatarChangeListeners(final int userID, final int spoilerID, final Date spoilerExpDate) {
		for( AvatarSpoilerChangeListener listener : avatarSpoilerChangeListeners ) {
			listener.avatarSpoilerChanged(userID, spoilerID, spoilerExpDate);
		}
	} 
}
