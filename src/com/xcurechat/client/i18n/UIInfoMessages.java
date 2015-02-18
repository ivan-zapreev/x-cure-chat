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
 * The user interface internationalization package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.i18n;

import com.google.gwt.i18n.client.Messages;

/**
 * @author zapreevis
 * This interface is needed to provide internationalization for the main  
 * information messages that come from the server side and also the client.
 */
public interface UIInfoMessages extends Messages {
	
	/**
	 * @return the new site version notification message
	 */
	@Key("xcure.client.new.site.version.alert")
	String alertNewSiteVersionInfo(final String newSiteVersion );
	
	/**
	 * @return the "A new non-public room has been created, please add room users" message
	 */
	@Key("xcure.client.room.new.non.public.add.users.msg")
	String roomCreatedAddUsers();

	/**
	 * @return the "There are no search results for this query." message
	 */
	@Key("xcure.client.search.no.results.msg")
	String noSearchResultsForTheQuery();
	
	/**
	 * @return the "There are no new users can be added to the chat room" message
	 */
	@Key("xcure.client.room.access.search.no.results.msg")
	String noNewUsersToAddToTheChatRoom();
	
	/**
	 * @return the "Dear {0},\n\n Could you please add me to your private (protected) room \"{1}\"?\n\n {2}" message
	 */
	@Key("xcure.client.room.access.request.body.msg")
	String pleaseAddMeToYourRoomRequest( String receiverName, String roomName, String senderName );
	
	/**
	 * @return the "Dear {0},\n\n I've added you to my private (protected) room \"{1}\"!\n\n {2}" message
	 */
	@Key("xcure.client.room.access.granted.body.msg")
	String youWereAddedToTheRoom( String receiverName, String roomName, String senderName );
	
	/**
	 * @return the "This room is about to be closed!" message
	 */
	@Key("xcure.client.room.is.closing.info.msg")
	String chatInfoMsgRoomIsClosing();
	
	/**
	 * @return the " has entered the room" message
	 */
	@Key("xcure.client.user.room.enter.info.msg")
	String chatInfoMsgUserRoomEnter();

	/**
	 * @return the " has left the room" message
	 */
	@Key("xcure.client.user.room.leave.info.msg")
	String chatInfoMsgUserRoomLeave();
	
	/**
	 * @return contains text for 01 introduction message
	 */
	@Key("xcure.client.introduction.message.01.text")
	String introMessage01Text();
	
	/**
	 * @return contains text for 02 introduction message
	 */
	@Key("xcure.client.introduction.message.02.text")
	String introMessage02Text();
	
	/**
	 * @return contains text for 03 introduction message
	 */
	@Key("xcure.client.introduction.message.03.text")	
	String introMessage03Text();
	
	/**
	 * @return contains text for 04 introduction message
	 */
	@Key("xcure.client.introduction.message.04.text")
	String introMessage04Text();
	
	/**
	 * @return contains text for 05 introduction message
	 */
	@Key("xcure.client.introduction.message.05.text")
	String introMessage05Text();
	
	/**
	 * @return contains text for 06 introduction message
	 */
	@Key("xcure.client.introduction.message.06.text")
	String introMessage06Text();
	
	/**
	 * @return contains text for 07 introduction message
	 */
	@Key("xcure.client.introduction.message.07.text")
	String introMessage07Text();
	
	/**
	 * @return contains text for 08 introduction message
	 */
	@Key("xcure.client.introduction.message.08.text")
	String introMessage08Text();
	
	/**
	 * @return contains text for 09 introduction message
	 */
	@Key("xcure.client.introduction.message.09.text")
	String introMessage09Text();
	
	/**
	 * @return contains text for 10 introduction message
	 */
	@Key("xcure.client.introduction.message.10.text")
	String introMessage10Text();
	
	/**
	 * @return contains text for 11 introduction message
	 */
	@Key("xcure.client.introduction.message.11.text")
	String introMessage11Text();
	
	/**
	 * @return contains text for 12 introduction message
	 */
	@Key("xcure.client.introduction.message.12.text")
	String introMessage12Text();
	
	/**
	 * @return contains text for 13 introduction message
	 */
	@Key("xcure.client.introduction.message.13.text")
	String introMessage13Text();
	
	/**
	 * @return contains text for 14 introduction message
	 */
	@Key("xcure.client.introduction.message.14.text")
	String introMessage14Text();
	
	/**
	 * @return the text of the user agreement
	 */
	@Key("xcure.client.user.agreement.text")
	String userAgreementText();
	
	/**
	 * @return I am away
	 */
	@Key("xcure.client.user.status.away")
	String userStatusAway();
	
	/**
	 * @return I am chatting
	 */
	@Key("xcure.client.user.status.chatting")
	String userStatusChatting();
	
	/**
	 * @return Do not disturb me
	 */
	@Key("xcure.client.user.status.dnd")
	String userStatusDND();
	
	/**
	 * @return I feel happy
	 */
	@Key("xcure.client.user.status.happy")
	String userStatusHappy();
	
	/**
	 * @return I feel sad
	 */
	@Key("xcure.client.user.status.sad")
	String userStatusSad();
	
	/**
	 * @return I am free for chat
	 */
	@Key("xcure.client.user.status.free.for.chat")
	String userStatusFreeForChat();
	
	/**
	 * @return I am in love
	 */
	@Key("xcure.client.user.status.in.love")
	String userStatusInLove();
	
	/**
	 * @return I am listening to a music
	 */
	@Key("xcure.client.user.status.music.listening")
	String userStatusMusicListening();
	
	/**
	 * @return I am looking for a man
	 */
	@Key("xcure.client.user.status.need.a.man")
	String userStatusNeedAMan();
	
	/**
	 * @return I am looking for a woman
	 */
	@Key("xcure.client.user.status.need.a.woman")
	String userStatusNeedAWoman();
	
	/**
	 * @return I feel horny
	 */
	@Key("xcure.client.user.status.on.fire")
	String userStatusOnFire();
	
	/**
	 * @return I am on the phone
	 */
	@Key("xcure.client.user.status.on.the.phone")
	String userStatusOnThePhone();
	
	/**
	 * @return I am sleeping
	 */
	@Key("xcure.client.user.status.sleeping")
	String userStatusSleeping();
	
	/**
	 * @return I am travelling
	 */
	@Key("xcure.client.user.status.travelling")
	String userStatusTravelling();
	
	/**
	 * @return I am watching TV
	 */
	@Key("xcure.client.user.status.watching.tv")
	String userStatusWatchingTV();
	
	/**
	 * @return I am working
	 */
	@Key("xcure.client.user.status.working")
	String userStatusWorking();

	/**
	 * @return Private session
	 */
	@Key("xcure.client.user.status.private.session")
	String userStatusInPrivate();

	/**
	 * @return Need a partner
	 */
	@Key("xcure.client.user.status.need.a.partner")
	String userStatusNeedAPartner();

	/**
	 * @return Eating
	 */
	@Key("xcure.client.user.status.eating")
	String userStatusEating();

	/**
	 * @return Gaming
	 */
	@Key("xcure.client.user.status.gaming")
	String userStatusGaming();

	/**
	 * @return Surfing Net
	 */
	@Key("xcure.client.user.status.surfing.net")
	String userStatusSurfingNet();

	/**
	 * @return Angry
	 */
	@Key("xcure.client.user.status.angry")
	String userStatusAngry();

	/**
	 * @return Fuck off
	 */
	@Key("xcure.client.user.status.fuck.off")
	String userStatusFuckOff();

	/**
	 * @return Elsewhere on site
	 */
	@Key("xcure.client.user.status.elsewhere.here")
	String userStatusElsewhereHere();

	/**
	 * @return resting 
	 */
	@Key("xcure.client.user.status.resting")
	String userStatusResting();

	/**
	 * @return smoking 
	 */
	@Key("xcure.client.user.status.smoking")
	String userStatusSmoking();

	/**
	 * @return studying
	 */
	@Key("xcure.client.user.status.studying")
	String userStatusStudying();

	/**
	 * @return has changed his status
	 */
	@Key("xcure.client.user.status.change.unknown")
	String getUserStatusChangedInfoMessage();

	/**
	 * @return has changed his status to:
	 */
	@Key("xcure.client.user.status.change.known")
	String getUserStatusChangedToInfoMessage( );
	
	/**
	 * @return Do you really want to follow the URL: {0}?
	 */
	@Key("xcure.client.user.want.to.follow.the.url")
	String doYouWantToFollowTheURL(String url);
	
	/**
	 * @return the help message html text
	 */
	@Key("xcure.client.user.help.message.html")
	String helpMessageHTML();
	
	/**
	 * @return The forum topic has no posts in it
	 */
	@Key("xcure.client.forum.topic.has.no.posts")
	String noPostsInThisForumTopic();
	
	/**
	 * @return The given forum section does not contain any topics
	 */
	@Key("xcure.client.forum.section.has.no.topics")
	String noTopicsInThisForumSection();
	
	/**
	 * @return The forum message has no replies
	 */
	@Key("xcure.client.forum.message.has.no.replies")
	String noRepliesToThisForumMessage();
	
	/**
	 * @return the user treasure wallet help information
	 */
	@Key("xcure.client.user.treasure.wallet.help")
	String userTreasureWalletHelp();

	/**
	 * @return "I just replied to your forum message {0}"
	 */
	@Key("xcure.client.user.forum.reply.private.msg.notification")
	String repliedToForumMessage( final String forumMessageURL );
	
	/**
	 * @return "Do you want to clean this prank for {0} gold pieces?"
	 */
	@Key("xcure.client.user.clean.prank.question")
	String cleanPrankDialogQuestion( final int priceInGoldPieces );
	
	/**
	 * @return "Remove prank"
	 */
	@Key("xcure.client.user.clean.prank.dialog.title")
	String cleanPrankDialogTitle();
}
