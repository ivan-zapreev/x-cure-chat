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
 * The forum interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.forum.sharing;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This is the super class for the classes responsible for sharing the forum messages
 */
public abstract class ShareMessageLinkBase extends Composite {
	protected boolean clickAction;
	protected final String messageURL;
	protected final String messageTitle;
	protected final String linkTitle;
	private final Anchor link = new Anchor();
	private final Image image = new Image();
	
	/**
	 * The basic constructor.
	 * No click action for the link
	 */
	public ShareMessageLinkBase( final String messageURL, final String messageTitle,
								 final String linkTitle ) {
		this( messageURL, messageTitle, linkTitle, false );
	}
	
	/**
	 * The basic constructor
	 */
	public ShareMessageLinkBase( final String messageURL, final String messageTitle,
								 final String linkTitle, final boolean clickAction ) {
		this.clickAction = clickAction;
		this.messageURL = messageURL;
		this.messageTitle = messageTitle != null ? messageTitle : "";
		this.linkTitle = linkTitle;
	}
	
	/**
	 * Must be called in the constructor of the subclass in order to initialize the widget  
	 */
	protected void initialize() {
		//Set the image
		image.setUrl( ServerSideAccessManager.MESSAGES_RELATED_IMAGES_LOCATION + getLinkImageFileName() + ".png" );
		image.addStyleName( CommonResourcesContainer.SHARE_FORUM_MESSAGE_LINK_IMAGE_STYLE );
		
		//Set the hyperlink
		link.setHref( getLinkURL( messageURL, URL.encodeQueryString( messageTitle ) ) );
		link.setTarget( "_blank" );
		link.setTitle( linkTitle );
		link.setHTML( image.getElement().getString() );
		link.setStyleName( CommonResourcesContainer.SHARE_FORUM_MESSAGE_LINK_STYLE );
		
		//Add the click handler if needed
		if( clickAction  ) {
			link.addClickHandler( new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onClickAction(messageURL, event);
				}
			} );
		}
		//Initialize the composite
		initWidget( link );
	}
	
	/**
	 * In case the object is constructed with the clickAction==true
	 * argument then this method will be invoked whenever the link
	 * is clicked. It does nothing by default, should be overridden.
	 * @param messageURL the url of the corresponding message
	 * @param event the click event
	 */
	protected void onClickAction(final String messageURL, final ClickEvent event) {}

	
	/**
	 * Should be implemented by the subclass and is supposed to return the proper URL
	 * for the link, e.g. a properly formatted URL for posting the message link to Twitter.
	 * The messageTitle is passed into that method after being encoded as a URL component. 
	 * @param messageURL the url of the given forum message 
	 * @param messageTitle the title of the given forum message
	 * @return the properly formatted URL for re-posting the link to the forum message elsewhere
	 */
	public abstract String getLinkURL( final String messageURL, final String messageTitle );
	
	/**
	 * The file location is expected to be in ServerSideAccessManager.MESSAGES_RELATED_IMAGES_LOCATION
	 * on the server, and also the file type is expected to be .png. Note that, the file extension is
     * not a part of the returned file name.
	 * @return a URL of the image hat represents this message sharing link
	 */
	public abstract String getLinkImageFileName();
}
