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

import java.util.List;

import com.google.gwt.i18n.client.Messages;

/**
 * @author zapreevis
 * This interface is needed to provide internationalization for the main  
 * error messages that come from the server side and also happen in the client.
 */
public interface UIErrorMessages extends Messages {

	/**
	 * @return incorrect user login exception
	 */
	@Key("xcure.client.error.incorrect.user.name.format")
	String incorrectUserNameFormat(int minLoginLength, int maxLoginLength);

	/**
	 * @return poor new user password exception
	 */
	@Key("xcure.client.error.incorrect.password.format")
	String passwordFormatError(int minPasswordLength, int maxPasswordLength);

	/**
	 * @return the new password format hasincorect format
	 */
	@Key("xcure.client.error.incorrect.new.password.format")
	String newPasswordFormatError(int minPasswordLength, int maxPasswordLength);

	/**
	 * @return the incorrect value of the text field, format or length
	 */
	@Key("xcure.client.error.incorrect.text.field.format")
	String incorrectTextFieldFormat(String fieldName, int maxFieldLength);
	
	/**
	 * @return the field {0} must not contain more that {1} symbols
	 */
	@Key("xcure.client.error.incorrect.text.field.length")
	String incorrectTextFieldLength(String fieldName, int maxFieldLength);
	
	/**
	 * @return poor new user password exception
	 */
	@Key("xcure.client.error.user.password.rep.notequal")
	String passwordRepNotEqualError();

	/**
	 * @return the new password and it's repitition fo not match
	 */
	@Key("xcure.client.error.user.new.password.rep.notequal")
	String newPasswordRepNotEqualError();
	
	/**
	 * @return the old password is incorrect
	 */
	@Key("xcure.client.error.wrong.old.password")
	String incorrectOldPassword();
	
	/**
	 * @return incorrect captcha response exception
	 */
	@Key("xcure.client.error.incorrect.captcha.response")
	String captchaTestFailedError();

	/**
	 * @return empty captcha response exception
	 */
	@Key("xcure.client.error.empty.captcha.response")
	String captchaEmptyAnswerError();
	
	/**
	 * @return empty user login exception
	 */
	@Key("xcure.client.error.empty.login")
	String emptyUserLoginError();
	
	/**
	 * @return empty user password exception
	 */
	@Key("xcure.client.error.empty.password")
	String emptyUserPasswordError();
	
	/**
	 * @return empty old user password exception
	 */
	@Key("xcure.client.error.empty.old.password")
	String emptyOldUserPasswordError();

	/**
	 * @return the user login in use exception
	 */
	@Key("xcure.server.error.user.login.in.use")
	String userLoginInUseError( );
	
	/**
	 * @return the user agreement is not accepter exception
	 */
	@Key("xcure.client.user.agreement.is.not.accepted.error")
	String userAgreementIsNotAcceptedError();
	
	/**
	 * @return the user login in use exception
	 */
	@Key("xcure.server.error.wrong.login.password")
	String incorrectLoginPasswordError( );

	/**
	 * @return the user login in use exception
	 */
	@Key("xcure.server.error.internal.database.problem")
	String internalDatabaseError( );
	
	/**
	 * @return the user is not logged in exception
	 */
	@Key("xcure.server.user.state.not.logged.in.error")
	String userIsNotLoggedInError();
	
	/**
	 * @param remainingTimeHours the hours part of the remaining blocking time 
	 * @param remainingTimeMinutes the minutes part of the remaining blocking time 
	 * @return the user does server requests too frequently
	 */
	@Key("xcure.server.error.internal.too.frequent.requests")
	String tooFrequentRequestsError( int remainingTimeHours, int remainingTimeMinutes );
	
	/**
	 * @return "You either type incredibly fast, or send too many messages,
	 *          either way, take a break and let your fingers rest"
	 */
	@Key("xcure.server.error.send.message.abuse.detected.error")
	String sendMessageAbuseDetected();
	
	/**
	 * @param remainingTimeHours the hours part of the remaining blocking time 
	 * @param remainingTimeMinutes the minutes part of the remaining blocking time 
	 * @return the user failed too many logins/ session validations
	 */
	@Key("xcure.server.error.internal.too.many.login.failures")
	String tooManyFailedLoginsError( int remainingTimeHours, int remainingTimeMinutes );
	
	/**
	 * @return there was some unexpected IO exception while
	 * 			processing the file upload request message
	 */
	@Key("xcure.server.error.file.upload.input.output")
	String unexpectedIOFileUploadError();
	
	/**
	 * @return an incorrect file upload request message
	 */
	@Key("xcure.server.error.file.upload.invalid.request")
	String incorrectFileUploadRequestError();

	/**
	 * @return the size of the uploaded file exceeds maxFileSizeMb Mb maxFileSizeKb Kb  
	 */
	@Key("xcure.server.error.file.upload.file.too.large")
	String fileIsTooLargeInError( long maxFileSizeMb, long maxFileSizeKb );
	
	/**
	 * @return the upload file is not selected  
	 */
	@Key("xcure.client.error.file.upload.not.selected")
	String fileIsNotSelectedError();
	
	/**
	 * @return the uploaded file is either corrupted or the format is unsupported by the server
	 */
	@Key("xcure.server.error.file.upload.incorrect.file.format")
	String incorectUploadFileFormat();
	
	/**
	 * @return an unknown server side error
	 */
	@Key("xcure.server.error.unknown.error")
	String unknownInternalSiteError();
	
	/**
	 * @return insufficient access rights error
	 */
	@Key("xcure.server.error.insufficient.access.rights")
	String insufficientAccessRightsError();
	
	/**
	 * @return the "user does not exist" message
	 */
	@Key("xcure.server.error.user.does.not.exist")
	String userDoesNotExistError();
	
	/**
	 * @return the "room does not exist" message
	 */
	@Key("xcure.server.error.room.does.not.exist")
	String roomDoesNotExistError();
	
	/**
	 * @return the "room is closed" message
	 */
	@Key("xcure.server.error.room.is.closed")
	String roomIsClosedError();
	
	/**
	 * @return the "user can not access the room" message
	 */
	@Key("xcure.server.error.room.can.not.be.accessed")
	String canNotEnterTheRoomError();
	
	/**
	 * @return the "the room name is not suitable" message
	 */
	@Key("xcure.server.error.room.wrong.name")
	String incorrectRoomNameFormat(int min_length, int max_length);
	
	/**
	 * @return the "undefined room's life time" message
	 */
	@Key("xcure.server.error.room.unknown.life.time")
	String unknownRoomLifeTime();
		
	/**
	 * @param max_rooms_number the maximum allowed number of rooms
	 * @return the "too many rooms" message
	 */
	@Key("xcure.server.error.room.too.many")
	String tooManyRooms(int max_rooms_number);
	
	/**
	 * @return the "user can not create a room of this type" message
	 */
	@Key("xcure.server.error.room.user.create.room.type")
	String unallowedRoomTypeForUser();
	
	/**
	 * @return the "you are not allowed to update other's rooms" message
	 */
	@Key("xcure.server.error.room.user.can.not.update.others.rooms")
	String canNotUpdateOthersRooms();
	
	/**
	 * @return the "you are not allowed to create rooms for other users" message
	 */
	@Key("xcure.server.error.room.user.can.not.create.room.for.others")
	String canNotCreateRoomForOthers();
	
	/**
	 * @return the "There are no selected rooms" message
	 */
	@Key("xcure.server.error.room.is.not.selected")
	String thereIsNoSelectedRooms();
	
	/**
	 * @return the "There are no selected users" message
	 */
	@Key("xcure.server.error.room.user.is.not.selected")
	String thereIsNoSelectedUsers();
	
	/**
	 * @return the "Only the admin can manage room-user access in details" message
	 */
	@Key("xcure.server.error.room.user.can.not.modify")
	String canNotUpdateRoomUserAccess();
	
	/**
	 * @return the "Only the admin can create the detailed room-user access object" message
	 */
	@Key("xcure.server.error.room.user.can.not.insert.detailed")
	String canNotInsertDetailedRoomUserAccess();
	
	/**
	 * @return the "The system room-user access already exists" message
	 */
	@Key("xcure.server.error.system.room.user.access.exists")
	String systemRoomUserAccessAlreadyExists();
	
	/**
	 * @return the "The regular room-user access already exists" message
	 */
	@Key("xcure.server.error.regular.room.user.access.exists")
	String regularRoomUserAccessAlreadyExists();
	
	/**
	 * @return the "The room-user access already exists" message
	 */
	@Key("xcure.server.error.room.user.access.exists")
	String roomUserAccessAlreadyExists();
	
	/**
	 * @return the "This room-user access rule does not have any enabled access right!" message
	 */
	@Key("xcure.client.error.room.user.access.is.empty")
	String roomUserAccessIsEmpty();
	
	/**
	 * @return the "The are no selected messages" message
	 */
	@Key("xcure.server.error.no.selected.messages")
	String thereIsNoSelectedMessages();
	
	/**
	 * @return the "The message recepient is undefined" message
	 */
	@Key("xcure.client.error.message.recepient.undefined")
	String undefinedMsgRecepient();
	
	/**
	 * @return the "The message's body and title are empty" message
	 */
	@Key("xcure.client.error.message.body.and.title.empty")
	String bothMsgTitleAndBodyAreEmpty(); 
	
	/**
	 * @return the "The message's title should not be longer than {0}" message
	 */
	@Key("xcure.client.error.message.title.too.long")
	String theMsgTitleIsTooLong(int max_length); 
	
	/**
	 * @return the "The message's body should not be longer than {0} symbols" message
	 */
	@Key("xcure.client.error.message.body.too.long")
	String theMsgBodyIsTooLong(int max_length);
	
	/**
	 * @return The message title is empty
	 */
	@Key("xcure.client.message.title.is.empty.error")
	String theMsgTitleIsTooEmpty();
	
	/**
	 * @return The message body is empty
	 */
	@Key("xcure.client.message.body.is.empty.error")
	String theMsgBodyIsTooEmpty();
	
	/**
	 * @return "The search query should not be longer than {0} symbols" message
	 */
	@Key("xcure.client.error.search.query.too.long")
	String searchQueryIsTooLong(int max_length);
	
	/**
	 * @return the "The message can not be found!" message
	 */
	@Key("xcure.server.error.message.can.not.be.found.probably.deleted")
	String messageDeletedOrIsNotUsers();
	
	/**
	 * @return the "The room access request is invalid! The room does not exist!" message
	 */
	@Key("xcure.server.error.room.access.request.is.invalid.room.does.not.exist")
	String roomDeletedAccessRequestIsInvalid();
	
	/**
	 * @return the "You are residing in {0} rooms! Please leave some rooms to enter new ones!" message
	 */
	@Key("xcure.client.error.maximum.allowed.number.opened.rooms.is.reached")
	String maximumAllowedNumberOfOpenRoomsError( int maximumNumberOfOpenedRooms );
	
	/**
	 * @return the "Unable to retrieve the list of user friends from the server!" message
	 */
	@Key("xcure.server.error.unable.to.get.friends")
	String unableToRetrieveTheListOfFriends( );
	
	/**
	 * @return The room {0} is offline and thus is not accessable!
	 */
	@Key("xcure.server.room.is.offline.error")
	String roomIsNotOnline( String roomName );
	
	/**
	 * @return The room {0} is about to be closed, therefore no new users are allowed!
	 */
	@Key("xcure.server.room.is.almost.closed.error")
	String roomIsAboutToBeClosed( String roomName );
	
	/**
	 * @return The room {0} is closed and thus is no longer accessable!
	 */
	@Key("xcure.server.room.is.closed.error")
	String roomWasClosed( String roomName );
	
	/**
	 * @return You do not have access rights to enter the room {0}!
	 */
	@Key("xcure.server.user.does.not.have.room.access.error")
	String userDoesNotHaveRoomAccess( String roomName );
	
	/**
	 * @return You were removed from the list of users of the room {0}!
	 */
	@Key("xcure.server.user.was.removed.from.the.room.error")
	String userWasRemovedFromTheRoom( String roomName );
	
	/**
	 * @return You are not allowed to post messages to the room {0}!
	 */
	@Key("xcure.server.user.can.not.write.to.the.room.error")
	String userIsNotAllowedToWriteIntoTheRoom( String roomName );
	
	/**
	 * @return You have left or were forced to leave the room {0}! \n Please re-enter it, for getting the room updates!
	 */
	@Key("xcure.server.user.is.not.in.the.room.error")
	String userIsNotInsideTheRoom( String roomName );
	
	/**
	 * @return The maximum number of message recipients ({0}) has been reached!
	 */
	@Key("xcure.client.chat.message.maximum.number.message.recipients.error")
	String maximumAllowedNumberOfMessageRecipients( final int maximumNumberMessageRecepients );
	
	/**
	 * @return The chat message is too long, is must not exceed {0} symbols!
	 */
	@Key("xcure.client.chat.message.is.too.long")
	String chatMessageIsTooLong(final int reduceBySizeSize);
	
	/**
	 * @return Neither the message body not the message image are set, the message is empty!
	 */
	@Key("xcure.client.chat.message.is.empty")
	String chatMessageIsEmpty();
	
	/**
	 * @return The user can only send private or public messages!
	 */
	@Key("xcure.client.chat.message.has.wrong.type")
	String chatMessageHasWrongType();
	
	/**
	 * @return The private chat message has to have recipients!
	 */
	@Key("xcure.client.chat.message.is.privat.but.no.recipients")
	String chatMessageIsPrivateButHasNoRecepients();
	
	/**
	 * @return We could not send the chat message in {0} seconds.
	 */
	@Key("xcure.client.chat.message.sending.time.out")
	String chatMessageSendTimeOut(final int timeOutSeconds);
	
	/**
	 * @return The given OS is not supported
	 */
	@Key("xcure.client.os.is.not.supported.error")
	String theOSIsNotSupported();
	
	/**
	 * @return The given Browser is not supported
	 */
	@Key("xcure.client.browser.is.not.supported.error")
	String theBrowserIsNotSupported(final String browser);
	
	/**
	 * @return The given Browser Versionis not supported
	 */
	@Key("xcure.client.browser.version.is.not.supported.error")
	String theBrowserVersionIsNotSupported(final String browser, final String version, final String versionSugg );
	
	/**
	 * @return The user can not access the site section because he is not logged in
	 */
	@Key("xcure.client.no.site.section.access.not.logged.in.error")
	String noAccessToTheSiteSectionNotLoggedIn();
	
	/**
	 * @return The only allowed upload-file extensions are: {0}
	 */
	@Key("xcure.client.the.only.allowed.upload.file.extensions.are.error")
	String fileUploadSupportedFiles( List<String> allowedFileExtensions );
	
	/**
	 * @return You did not select any files
	 */
	@Key("xcure.client.you.did.not.select.any.files.error")
	String thereIsNoSelectedFilesToDelete();
	
	/**
	 * @return The connection to the server has been lost
	 */
	@Key("xcure.client.server.connection.losterror")
	String serverConnectionHasBeenLost();
	
	/**
	 * @return Could not retrieve server data, please repeat the operation!
	 */
	@Key("xcure.client.server.data.loading.error")
	String serverDataLoadingFalied();
	
	/**
	 * @return The forum message you reply to does not exist
	 */
	@Key("xcure.server.forum.message.you.reply.to.does.not.exist")
	String forumMessageParentMessageDoesNotExist();
	
	/**
	 * @return The forum message you want to update does not exit, or you are not its sender!
	 */
	@Key("xcure.server.forum.message.you.reply.to.does.not.exist")
	String forumMessageToUpdateDoesNotExist();
	
	/**
	 * @return The message no longer exists!
	 */
	@Key("xcure.server.forum.message.does.not.exist")
	String theMessageNoLongerExists();
	
	/**
	 * @return You have already voted for this message!
	 */
	@Key("xcure.server.forum.message.you.have.already.voted.for.this.message")
	String youHaveAlreadyVotedForThisMessage();
	
	/**
	 * @return Either the embedded object is not a flash movie or the embedding html code is corrupted!
	 */
	@Key("xcure.server.message.improper.embedded.object")
	String improperEmbeddedObject();
	
	/**
	 * @return The resulting message is too long, probably due to the embedded Flash objects!
	 */
	@Key("xcure.server.message.too.long.embedded.object")
	String resultingForumMessageIsTooLongEmbeddedFlash();
	
	/**
	 * @return You only have {0} gold pieces, you need to have at least {1}!
	 */
	@Key("xcure.client.not.enough.gold")
	String insufficientGold( final int usersGold, final int neededGold );
	
	/**
	 * @return Oooops we lost our focus, where should I put the smiles?
	 */
	@Key("xcure.client.smiley.selection.dialog.focus.lost")
	String smileySelectionDialogFocusLost();
}
