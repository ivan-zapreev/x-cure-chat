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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.forum.messages;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.xcurechat.client.SiteManager;
import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.rpc.ForumManagerAsync;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.exceptions.MessageException;
import com.xcurechat.client.rpc.exceptions.UserStateException;
import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.ActionLinkPanel;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This is a voting widget for the forum messages
 */
public class ForumMessageVoteWidget extends Composite {
	
	/**
	 * @author zapreevis
	 * Represents a single image in the rating
	 */
	private class RatingImageComposite extends Composite {
		 private final double leftBorder;
		 private final double rightBorder;
		 private final double median;
		 final Image image = new Image();
		
		/**
		 * Defines the values interval for this image
		 * @param leftBorder the left border <= which the image is an empty star 
		 * @param rightBorder the right border >= ( rightBorder + leftBorder) the image is a full star, otherwise it is a half star
		 */
		public RatingImageComposite( final double leftBorder, final double rightBorder ) {
			this.leftBorder = leftBorder;
			this.rightBorder = rightBorder;
			this.median = ( this.leftBorder + this.rightBorder ) / 2.0;
			initWidget( image );
		}
		
		public void updateImage( final double voteValue ) {
			if( voteValue <= leftBorder ) {
				image.setUrl( ServerSideAccessManager.getForumMessageVotingStarImage( ServerSideAccessManager.EMPTY_STAR_IMAGE ) );
			} else {
				if( voteValue < median ) {
					image.setUrl( ServerSideAccessManager.getForumMessageVotingStarImage( ServerSideAccessManager.HALF_STAR_IMAGE ) );
				} else {
					image.setUrl( ServerSideAccessManager.getForumMessageVotingStarImage( ServerSideAccessManager.FULL_STAR_IMAGE ) );
				}
			}
		} 
	}
	
	public static final int NUMBER_INDICATOR_IMAGES = 5;
	public static final int VOTE_VALUE_GOOD = 1;
	public static final int VOTE_VALUE_BAD = 0;
	
	//Voting composite images
	private final ActionLinkPanel voteAgainst;
	private final ActionLinkPanel voteFor;
	
	//The main voting panel
	private final HorizontalPanel mainPanel = new HorizontalPanel();
	//The panel with "do vote" action links
	private final HorizontalPanel doVotePanel = new HorizontalPanel();
	//The list of indicator images
	private final List<RatingImageComposite> imagesList = new ArrayList<RatingImageComposite>();
	//The label for storing the total number of votes
	private final Label numberOfVotes = new Label();
	
	//Contains the message for which the widget should be configured
	private final ForumMessageData messageData;
	
	//The approval progress bar
	private ServerCommStatusPanel progressBarUI = null;
	
	/**
	 * The basic constructor
	 * @param messageData the message for which the widget should be configured 
	 */
	public ForumMessageVoteWidget( final ForumMessageData messageData ) {
		this.messageData = messageData;
		
		//Set the vertical alignment
		mainPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		doVotePanel.setHeight("100%");
		doVotePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		
		//Initialize the panel
		
		//Add the total number of votes
		numberOfVotes.setStyleName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
		numberOfVotes.setWordWrap( false );
		mainPanel.add( numberOfVotes );
		mainPanel.add( new HTML("&nbsp;") );
		
		//Add the indicator images
		double intervalWidth = (VOTE_VALUE_GOOD - VOTE_VALUE_BAD) / (double) NUMBER_INDICATOR_IMAGES;
		for( int i = 0; i < NUMBER_INDICATOR_IMAGES; i++ ){
			RatingImageComposite image = new RatingImageComposite( i * intervalWidth, (i + 1) * intervalWidth );
			imagesList.add( image );
			mainPanel.add( image );
		}
		mainPanel.add( new HTML("&nbsp;") );
		
		//Add the My opinion panel
		mainPanel.add( doVotePanel );
		voteAgainst = new ActionLinkPanel( ServerSideAccessManager.getForumMessageVotingImage( false, true ), "",
				ServerSideAccessManager.getForumMessageVotingImage( false, false ), "",
				I18NManager.getTitles().badForumMessageLinkTitle(),
				new ClickHandler(){
					public void onClick(ClickEvent e) {
						//Give a positive vote
						voteForMessage(false);
						//Stop the event from being propagated, prevent default
						e.stopPropagation(); e.preventDefault();
					}
				}, false, true );
		
		voteFor = new ActionLinkPanel( ServerSideAccessManager.getForumMessageVotingImage( true, true ), "",
				ServerSideAccessManager.getForumMessageVotingImage( true, false ), "",
				I18NManager.getTitles().coolForumMessageLinkTitle(),
				new ClickHandler(){
					public void onClick(ClickEvent e) {
						//Give a positive vote
						voteForMessage(true);
						//Stop the event from being propagated, prevent default
						e.stopPropagation(); e.preventDefault();
					}
				}, false, true );
		populateMyVotePanel();
		
		//Set the current number of votes
		setTheVotes( messageData.numVotes, messageData.voteValue );
		
		//Initialize the composite
		initWidget( mainPanel );
	}
	
	//Populates the my vote panel
	private void populateMyVotePanel() {
		//Clean the panel just in case
		doVotePanel.clear();
		//Populate the panel
		Label myVote = new Label();
		myVote.setText( I18NManager.getTitles().myForumMessageVote() );
		myVote.setStyleName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
		doVotePanel.add( myVote );
		doVotePanel.add( new HTML("&nbsp;") );
		doVotePanel.add( voteAgainst );
		doVotePanel.add( new HTML("&nbsp;") );
		doVotePanel.add( voteFor );
	}

	private void setTheVotes( final int numVotes, final int voteValue ) {
		numberOfVotes.setText( I18NManager.getTitles().numberOfForumMessageVotes( numVotes ) );
		final double voteValueDouble = ( numVotes == 0 ) ? 0.0 : (double) voteValue / (double) numVotes;
		for( RatingImageComposite image : imagesList ) {
			image.updateImage( voteValueDouble );
		}
	}
	
	/**
	 * Allows to vote for the forum message, does the server request
	 * @param voteFor true if we vote for the message, false - against it
	 */
	private void voteForMessage( final boolean voteFor ) {
		//Check if the progress bar was instantiated
		if( progressBarUI == null ) {
			progressBarUI = new ServerCommStatusPanel();
		}
		
		//Add the progress bar to the panel
		mainPanel.add( progressBarUI );
		
		//Do the server call
		setEnabled( false );
		
		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				//Create the server collback object
				CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
					@Override
					public void onSuccessAct(Void result) {
						//Update the vote with the current data, no additional info or requests from the server
						messageData.hasVoted = true;
						messageData.numVotes++;
						messageData.voteValue += ( voteFor ? 1 : 0 );
						setTheVotes( messageData.numVotes, messageData.voteValue );
						//Remove the progress widget
						mainPanel.remove( progressBarUI );
						//enable the vote controls
						setEnabled( true );
						//Re-populate the panel
						populateMyVotePanel();
					}
					@Override
					public void onFailureAct( final Throwable caught ) {
						//If there was an exception saying that the user is not
						//logged in or that we have voted for this message or
						//that it does not exist then mark the message is being voted for
						//Here we do a rough check for it, just based on the type of the exception caught
						if( caught instanceof UserStateException || caught instanceof MessageException ) {
							messageData.hasVoted = true;
						}
						
						//NOTE: With the forum cache that forgets the voting results
						//every now and again the error visualization becomes annoying
						//so visualize it only in case it is not the "already voted" exception 
						if( caught instanceof MessageException ) {
							MessageException ex = (MessageException) caught;
							List<Integer> errorCodes = ex.getErrorCodes();
							if( errorCodes != null && errorCodes.contains( MessageException.YOU_HAVE_ALREADY_VOTED_FOR_THIS_MESSAGE ) ) {
								//DO NOTHING
							} else {
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Report the error
										ErrorMessagesDialogUI.openErrorDialog( caught );
									}
								}).loadAndExecute();
							}
						} else {
							(new SplitLoad( true ) {
								@Override
								public void execute() {
									//Report the error
									ErrorMessagesDialogUI.openErrorDialog( caught );
								}
							}).loadAndExecute();
						}
						//Use the recovery method
						recover();
					}
				};
				ForumManagerAsync forumManagerObj = RPCAccessManager.getForumManagerAsync();
				forumManagerObj.voteForForumMessage(SiteManager.getUserID(), SiteManager.getUserSessionId(), messageData.messageID, voteFor, callback );			
			}
			@Override
			public void recover() {
				//Remove the progress widget
				mainPanel.remove( progressBarUI );
				//Re-populate the panel
				populateMyVotePanel();
				//enable the vote controls
				setEnabled( true );
			}
		}).loadAndExecute();
	}
	
	/**
	 * Allows to enable/disable the widget
	 * @param enabled true to attempt enabling the voting action
	 */
	public void setEnabled( boolean enabled ) {
		final boolean isEnabled = enabled && ( ! messageData.hasVoted );
		doVotePanel.setVisible( isEnabled );
		voteAgainst.setEnabled( isEnabled );
		voteFor.setEnabled(isEnabled  );
	}
}
