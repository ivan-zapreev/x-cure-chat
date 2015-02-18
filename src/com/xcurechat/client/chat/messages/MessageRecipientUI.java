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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.chat.messages;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class represents the message's recipient entry
 */
public class MessageRecipientUI extends Composite {
	
	/**
	 * @author zapreevis
	 * This interface contains required method for the panel storing the Message recipient widgets 
	 */
	public interface MessageRecipientPanel {
		/**
		 * Allows to remove the message recipient
		 * @param recepientID the recepient's ID
		 */
		public void removeMessageRecipient( final int recepientID );
		
		/**
		 * Updates the message recipients panel for the cases when there are no recipients set
		 */
		public void updateMessageRecipientsPanel( );
	}
	
	//The localization for the text
	protected static final UITitlesI18N titlesI18N = I18NManager.getTitles();
	
	private final HorizontalPanel horizontalPanel = new HorizontalPanel();
	private final int recepientID;
	
	public MessageRecipientUI( final int recepientID, final String recepientLoginName,
							   final MessageRecipientPanel panel, final DialogBox parentDialog ) {
		this.recepientID = recepientID;
		
		horizontalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		Label recepientLabel = new Label( ShortUserData.getShortLoginName( recepientLoginName ) );
		recepientLabel.setTitle( recepientLoginName );
		recepientLabel.setStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
		recepientLabel.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						//Open the user profile view dialog
						ViewUserProfileDialogUI dialog = new ViewUserProfileDialogUI(recepientID, recepientLoginName, parentDialog, false);
						dialog.show();
						dialog.center();
					}
				};
				executor.loadAndExecute();
			}
		});
		horizontalPanel.add( recepientLabel );

		Image removeRecepientImage = new Image( ServerSideAccessManager.getRemoveImageButtonURL() );
		removeRecepientImage.setStyleName( CommonResourcesContainer.REMOVE_CHAT_MSG_RECEPIENT_IMAGE_STYLE );
		removeRecepientImage.setTitle( titlesI18N.removeRecipientToolTip( recepientLoginName ) );
		removeRecepientImage.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Remove the recipient
				panel.removeMessageRecipient( recepientID );
				
				//Update the list of recipients for the case of no recipients set
				panel.updateMessageRecipientsPanel();
			}
		});
		horizontalPanel.add( removeRecepientImage );
		
		//All composites must call initWidget() in their constructors.
		initWidget( horizontalPanel );		
	}
	
	public int getRecepientID() {
		return recepientID;
	}
}
