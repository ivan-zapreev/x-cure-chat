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
 * interface elements of the system. Error messages are defined elsewhere.
 */
public interface UITitlesI18N extends Messages {
	
	/**
	 * @return "Site version:"
	 */
	@Key("xcure.client.site.version.field.title")
	String siteVersionFieldLabel();
	
	/**
	 * @return "registered users:"
	 */
	@Key("xcure.client.registered.users.field.title")
	String totalUsersFieldLabel();
	
	/**
	 * @return "Users online:"
	 */
	@Key("xcure.client.users.online.field.title")
	String onlineUsersFieldLabel();
	
	/**
	 * @return "Guests online:"
	 */
	@Key("xcure.client.guests.online.field.title")
	String onlineGuestsFieldLabel();
	
	/**
	 * @return the title of the website
	 */
	@Key("xcure.client.title")
	String siteTitle();
	
	/**
	 * @return Click to get help
	 */
	@Key("xcure.client.help.tool.tip")
	String helpToolTip();
	
	/**
	 * @return Click to refresh
	 */
	@Key("xcure.client.refresh.tool.tip")
	String refreshToolTip();
	
	/**
	 * @return Click to go to the top
	 */
	@Key("xcure.client.go.to.top.tool.tip")
	String moveToTopToolTip();
	
	/**
	 * @return the 'continue' button title
	 */
	@Key("xcure.client.continue.button.title")
	String continueButtonTitle();
	
	/**
	 * @return the 'Locate' button title
	 */
	@Key("xcure.client.locate.button.title")
	String locateButtonTitle();
	
	/**
	 * @return the title of the main chat room
	 */
	@Key("xcure.client.main.room.title")
	String mainRoomTitle();
	
	/**
	 * @return the description of the main chat room
	 */
	@Key("xcure.client.main.room.description")
	String mainRoomDescription();
	
	/**
	 * @return the title of the main menu
	 */
	@Key("xcure.client.main.menu.title")
	String mainMenuTitle();
	
	/**
	 * @return the localized "Language" title
	 */
	@Key("xcure.client.site.language.field.title")
	String languageFieldTitle();
	
	/**
	 * @return the localized "Status" title
	 */
	@Key("xcure.client.user.status.field.title")
	String userStatusFieldTitle();
	
	/**
	 * @return the label for the "private massage" check box
	 */
	@Key("xcure.client.private.msg.check.box")
	String privateMessageCB();
	
	/**
	 * @return the label for the "translit massage" check box
	 */
	@Key("xcure.client.translit.msg.check.box")
	String translitMessageCB();
	
	/**
	 * @return the "send message" buttom label
	 */
	@Key("xcure.client.send.msg.button")
	String sendMessageButtonTitle();
	
	/**
	 * @return the "Hot key: Escape (Esc)" button title
	 */
	@Key("xcure.client.left.action.button.title")
	String defaultLeftActionButtonTitle();
	
	/**
	 * @return the "Hot key: Ctrl-Enter" button title
	 */
	@Key("xcure.client.right.action.button.title")
	String defaultRightActionButtonTitle();
	
	/**
	 * @return the "Hot key: Enter" button title
	 */
	@Key("xcure.client.right.action.button.hot.key.enter.title")
	String hotKeyEnterButtonToolTip();
	
	/**
	 * @return the "Reply" button label
	 */
	@Key("xcure.client.reply.button")
	String replyButton();

	/**
	 * @return the "close" button label
	 */
	@Key("xcure.client.close.button")
	String closeButtonTitle();

	/**
	 * @return the "No" button label
	 */
	@Key("xcure.client.no.button.title")
	String noButtonTitle();

	/**
	 * @return the "Yes" button label
	 */
	@Key("xcure.client.yes.button.title")
	String yesButtonTitle();

	/**
	 * @return the "add" button label
	 */
	@Key("xcure.client.add.button")
	String addButtonTitle();
	
	/**
	 * @return the "Select" button label also used in check boxes
	 */
	@Key("xcure.client.select.button")
	String selectButtonTitle();
	
	/**
	 * @return the "Reset" button label
	 */
	@Key("xcure.client.user.reset.button")
	String resetButtonTitle();
	
	/**
	 * @return the "rooms" panel title
	 */
	@Key("xcure.client.rooms.panel.title")
	String roomsPanelTitle(final int myRooms, final int otherRooms);
	
	/**
	 * @return the "My rooms" tree title
	 */
	@Key("xcure.client.my.rooms.tree.title")
	String myRoomsTreeTitle();
	
	/**
	 * @return the "Other rooms" tree title
	 */
	@Key("xcure.client.other.rooms.tree.title")
	String otherRoomsTreeTitle();
	
	/**
	 * @return the "people" tree title
	 */
	@Key("xcure.client.people.panel.title")
	String peoplePanelTitle(final int friends, final int others);
	
	/**
	 * @return the "friends" sub-tree title
	 */
	@Key("xcure.client.friends.subtree.title")
	String friendsSubTreeTitle();
	
	/**
	 * @return the "Users" title
	 */
	@Key("xcure.client.users.title")
	String usersTitle();
	
	/**
	 * @return the "User" field name
	 */
	@Key("xcure.client.user.field.name")
	String userFieldName();
	
	/**
	 * @return the "Edit" button's title
	 */
	@Key("xcure.client.edit.button")
	String editTitle();
	
	/**
	 * @return the "others" sub-tree title
	 */
	@Key("xcure.client.others.subtree.title")
	String othersSubTreeTitle();
	
	/**
	 * @return the "Radios" item of the main site menu
	 */
	@Key("xcure.client.menu.radios.item")
	String radiosMenuItem();
	
	/**
	 * @return the "Help" item of the main site menu
	 */
	@Key("xcure.client.menu.help.item")
	String helpMenuItem();
	
	/**
	 * @return the "Register" item of the main site menu
	 */
	@Key("xcure.client.menu.register.item")
	String registerMenuItem();
	
	/**
	 * @return the "Login" item of the main site menu
	 */
	@Key("xcure.client.menu.login.item")
	String loginMenuItem();
	
	/**
	 * @return the "Logout" item of the main site menu
	 */
	@Key("xcure.client.menu.logout.item")
	String logoutMenuItem();

	/**
	 * @return the "Preferences" item of the main site menu
	 */
	@Key("xcure.client.menu.preferences.item")
	String prefsMenuItem();

	/**
	 * @return the "Statistics" item of the main site menu
	 */
	@Key("xcure.client.menu.statistics.item")
	String statisticsMenuItem();
	
	/**
	 * @return the "User Manager" item of the main site menu
	 */
	@Key("xcure.client.menu.user.manager.item")
	String usersMenuItem();
	
	/**
	 * This one is used when there are no new messages
	 * @return the "Messages" item of the main site menu
	 */
	@Key("xcure.client.menu.private.messages.item")
	String messagesMenuItem();
	
	/**
	 * This one is used when there are new messages 
	 * @return the "Messages ({0})" item of the main site menu
	 */
	@Key("xcure.client.menu.private.messages.new.item")
	String messagesNewMenuItem(int numberNewMsgs);
	
	/**
	 * @return the "Room Manager" item of the main site menu
	 */
	@Key("xcure.client.menu.room.manager.item")
	String roomsMenuItem();
	
	/**
	 * @return the "Click outside the dialog to close it" tool tip
	 */
	@Key("xcure.client.close.dialog.tool.tip")
	String closeDialogTextTip();
	
	/**
	 * @return the title of the "User registration" dialog
	 */
	@Key("xcure.client.user.registration.dialog.title")
	String userRegistrationDialogTitle();

	/**
	 * @return the title of the "Edit user profile" dialog
	 */
	@Key("xcure.client.user.profile.dialog.edit.title")
	String userProfileDialogTitle();
	
	/**
	 * @return the title of the "View user profile" dialog
	 */
	@Key("xcure.client.user.profile.dialog.view.title")
	String viewUserProfileDialogTitle();
	
	/**
	 * @return the link title: "View full profile"
	 */
	@Key("xcure.client.user.view.profile.link.title")
	String viewFullUserProfileLinkTitle();
	
	/**
	 * @return the title of the "View user profile" dialog
	 */
	@Key("xcure.client.short.user.info.dialog.title")
	String shortUserInfoDialogTitle();
	
	/**
	 * @return the title of the main panel for the user profile
	 */
	@Key("xcure.client.user.profile.dialog.main.panel.title")
	String mainDataUserProfilePanel();
	
	/**
	 * @return the title of the about me panel for the user profile
	 */
	@Key("xcure.client.user.profile.dialog.about.me.panel.title")
	String aboutMeUserProfilePanel();
	
	/**
	 * @return the title of the avatar panel for the user profile
	 */
	@Key("xcure.client.user.profile.dialog.avatar.panel.title")
	String avatarUserProfilePanel();
	
	/**
	 * @return the title of the avatar field for the user profile
	 */
	@Key("xcure.client.user.profile.avatar.field.title")
	String avatarUserFieldName();
	
	/**
	 * @return the title of the password panel for the user profile
	 */
	@Key("xcure.client.user.profile.dialog.password.panel.title")
	String passwordUserProfilePanel();
	
	/**
	 * @return the title of the images panel for the user profile
	 */
	@Key("xcure.client.user.profile.dialog.images.panel.title")
	String imagesUserProfilePanel();
	
	/**
	 * @return the "Click to choose" image tip
	 */
	@Key("xcure.client.image.click.to.choose.tip")
	String clickToChooseToolTip();
	
	/**
	 * @return Add/remove profile files
	 */
	@Key("xcure.client.user.profile.add.remove.file")
	String addRemoveProfileFilesLink();
	
	/**
	 * @return Upload a file to the profile
	 */
	@Key("xcure.client.user.profile.file.upload.dialog.title")
	String profileFileUploadDialog();
	
	/**
	 * @return Profile files {0} (page {1} out of {2})
	 */
	@Key("xcure.client.user.profile.add.remove.file.dialog.title")
	String addRemoveProfileFilesDialogTitle( final int numberOfEntries, final int currentPageNumber, final int numberOfPages );
	
	/**
	 * @return the title of the "delete user" panel for the user profile
	 */
	@Key("xcure.client.user.profile.dialog.delete.panel.title")
	String deleteUserProfilePanel();

	/**
	 * @return the question to confirm that user wants to delete the profile
	 */
	@Key("xcure.client.user.profile.delete.profile.question")
	String deleteUserProfileQuestion();
	
	/**
	 * @return delete file title for the file management dialog 
	 */
	@Key("xcure.client.user.profile.delete.file.title")
	String deleteFilePanelTitle();
	
	/**
	 * @return upload file title for the file management dialog 
	 */
	@Key("xcure.client.user.profile.upload.file.title")
	String uploadFilePanelTitle();
	
	/**
	 * @param fileName the file name
	 * @param user login name
	 * @return profile's file show tialog title
	 */
	@Key("xcure.client.user.profile.image.show.dialog.title")
	String userProfileFilesShowDialogTitle( final String userLoginName, final String fileName );
	
	/**
	 * @return file management "Click to view" tip
	 */
	@Key("xcure.client.user.profile.file.enlarge.tip")
	String userFileThumbnailManagementTip();
	
	/**
	 * @return image viewing "next image" button
	 */
	@Key("xcure.client.user.profile.image.show.next.button")
	String userProfileImageShowNextButton();
	
	/**
	 * @return image viewing "previous image" button
	 */
	@Key("xcure.client.user.profile.image.show.previous.button")
	String userProfileImageShowPreviousButton();
	
	/**
	 * @return the registration button of the "User registration" dialog
	 */
	@Key("xcure.client.user.register.button")
	String registerButton();

	/**
	 * @return the save button title for the "User profile" dialog
	 */
	@Key("xcure.client.user.profile.save.button")
	String saveButton();
	
	/**
	 * @return the Cancel button title 
	 */
	@Key("xcure.client.user.cancel.button")
	String cancelButton(); 
	
	/**
	 * @return the Move button title 
	 */
	@Key("xcure.client.user.move.button")
	String moveButton();
	
	/**
	 * @return the DELETE button title 
	 */
	@Key("xcure.client.delete.button")
	String deleteButton();
	
	/**
	 * @return the CREATE button title 
	 */
	@Key("xcure.client.create.button")
	String createButton();
	
	/**
	 * @return the upload button title
	 */
	@Key("xcure.client.user.upload.button")
	String uploadButton();
	
	/**
	 * @return the supported file ext label
	 */
	@Key("xcure.client.supported.upload.file.ext.label")
	String supportedFileTypesFieldLabel();
	
	/**
	 * @return the send button title
	 */
	@Key("xcure.client.user.send.button")
	String sendButton();
	
	/**
	 * @return the Open dialog button title
	 */
	@Key("xcure.client.user.dialog.button")
	String dialogButton();
	
	/**
	 * @return the login field title
	 */
	@Key("xcure.client.user.login.name.field")
	String loginNameField();
	
	/**
	 * @return password field title
	 */
	@Key("xcure.client.user.password.field")
	String passwordField();
	
	/**
	 * @return password repetition field title
	 */
	@Key("xcure.client.user.password.rep.field")
	String passwordRepeatField();
	
	/**
	 * @return "current password" field title
	 */
	@Key("xcure.client.user.current.password.field")
	String currentPasswordField();
	
	/**
	 * @return "new password" field title
	 */
	@Key("xcure.client.user.new.password.field")
	String newPasswordField();
	
	/**
	 * @return "new password repetition" field title
	 */
	@Key("xcure.client.user.new.password.rep.field")
	String newPasswordRepeatField();
	
	/**
	 * @return first-name field title
	 */
	@Key("xcure.client.user.first.name.field")
	String firstNameField();
	
	/**
	 * @return last-name field title
	 */
	@Key("xcure.client.user.last.name.field")
	String lastNameField();
	
	/**
	 * @return user's gender
	 */
	@Key("xcure.client.user.gender.field")
	String genderField();
	
	/**
	 * @return user's female gender
	 */
	@Key("xcure.client.user.gender.female.value")
	String genderFemaleValue();
	
	/**
	 * @return user's male gender
	 */
	@Key("xcure.client.user.gender.male.value")
	String genderMaleValue();
	
	/**
	 * @return user's unknonw gender
	 */
	@Key("xcure.client.user.gender.unknown.value")
	String genderUnknownValue();
	
	/**
	 * @return age-field title
	 */
	@Key("xcure.client.user.age.field")
	String ageField();
	
	/**
	 * @return country-field title 
	 */
	@Key("xcure.client.user.country.field")
	String countryField();
	
	/**
	 * @return about-myself field title
	 */
	@Key("xcure.client.user.about.myself.field")
	String aboutMeField();
	
	/**
	 * @return city-field title
	 */
	@Key("xcure.client.user.city.field")
	String cityField();
	
	/**
	 * @return the title for the optional user registration form fields panel
	 */
	@Key("xcure.client.user.optional.fields")
	String optionalUserDataFields();
	
	/**
	 * @return captcha image title
	 */
	@Key("xcure.client.user.captcha.image.field")
	String captchaImage();
	
	/**
	 * @return captcha-answer field title  
	 */
	@Key("xcure.client.user.captcha.answer.field")
	String captchaAnswerField();
	
	/**
	 * @return captcha-image help information
	 */
	@Key("xcure.client.user.captcha.image.help")
	String captchaImageHelpMsg();

	/**
	 * @return the title of error messages dialos
	 */
	@Key("xcure.client.error.messages.dialog.title")
	String errorMessagesDialog();
	
	/**
	 * @return the title of the user-login dialo
	 */
	@Key("xcure.client.user.login.dialog.title")
	String userLoginDialogTitle();
	
	/**
	 * @return the text of the login button 
	 */
	@Key("xcure.client.user.login.dialog.login.button")
	String loginButton();
	
	/**
	 * @return the title for the user statistics dialog 
	 */
	@Key("xcure.client.user.stats.dialog.title")
	String userStatisticsDialogTitle(final String userLogin, final int currentPage, final int numberPages);
	
	/**
	 * @return the tooltip for the login image 
	 */
	@Key("xcure.client.user.stats.login.image.tooltip")
	String userStatisticsLogInImageTip();

	/**
	 * @return the tooltip for the logout image 
	 */
	@Key("xcure.client.user.stats.logout.image.tooltip")
	String userStatisticsLogOutImageTip();

	/**
	 * @return the tooltip for the auto-logout image 
	 */
	@Key("xcure.client.user.stats.auto.logout.image.tooltip")
	String userStatisticsLogOutAutoImageTip();
	
	/**
	 * @return the previous button title
	 */
	@Key("xcure.client.previous.button")
	String previousButton();
	
	/**
	 * @return the navigator button title
	 */
	@Key("xcure.client.navigate.button")
	String navigateButton();
	
	/**
	 * @return the next button title
	 */
	@Key("xcure.client.next.button")
	String nextButton();
	
	/**
	 * @return the search button text for the search dialogs 
	 */
	@Key("xcure.client.search.button")
	String searchButtonText();
	
	/**
	 * @return the "friend" check box title for the user search fialog 
	 */
	@Key("xcure.client.search.friend.checkbox.title")
	String friendCheckBoxTitleName();
	
	/**
	 * @return the "pictures" check box title for the user search fialog 
	 */
	@Key("xcure.client.search.pictures.checkbox.title")
	String picturesCheckBoxTitleName();
	
	/**
	 * @return the "online" check box title for the user search fialog 
	 */
	@Key("xcure.client.search.online.checkbox.title")
	String onlineCheckBoxTitleName();
	
	/**
	 * @return the clear user statistics title  
	 */
	@Key("xcure.client.user.stats.clear.title")
	String clearUserStatisticsTitle();
	
	/**
	 * @return the clear button title  
	 */
	@Key("xcure.client.user.stats.clear.button")
	String userStatsClearButton();
	
	/**
	 * @return the type of statistical entry  
	 */
	@Key("xcure.client.user.stats.table.header.type")
	String userStatsEntryType();
	
	/**
	 * @return the date title for the statistical entry  
	 */
	@Key("xcure.client.user.stats.table.header.date")
	String userStatsDateTitle();
	
	/**
	 * @return the host title for the statistical entry  
	 */
	@Key("xcure.client.user.stats.table.header.host")
	String userStatsHostTitle();
	
	/**
	 * @return the geo-locaiton title for the statistical entry  
	 */
	@Key("xcure.client.user.stats.table.header.location")
	String userStatsLocationTitle();
	
	/**
	 * @return the "Main" room type name  
	 */
	@Key("xcure.client.room.type.main")
	String roomTypeNameMain();
	
	/**
	 * @return the "Public" room type name  
	 */
	@Key("xcure.client.room.type.public")
	String roomTypeNamePublic();
	
	/**
	 * @return the "Protected" room type name  
	 */
	@Key("xcure.client.room.type.protected")
	String roomTypeNameProtected();
	
	/**
	 * @return the "Private" room type name  
	 */
	@Key("xcure.client.room.type.private")
	String roomTypeNamePrivate();
	
	/**
	 * @return the "Description" label from the room info popup
	 */
	@Key("xcure.client.room.info.popup.description")
	String roomDescFieldName();
	
	/**
	 * @return the "Closing time" label from the room info popup
	 */
	@Key("xcure.client.room.info.popup.closing.time")
	String roomClosingTimeFieldName();
	
	/**
	 * @return the "Access" label from the room info popup
	 */
	@Key("xcure.client.room.info.popup.access")
	String roomAccessFieldName();
	
	/**
	 * @return the "Owner" label from the room info popup
	 */
	@Key("xcure.client.room.info.popup.owner")
	String roomOwnerFieldName();
	
	/**
	 * @return the "Type" label from the room info popup
	 */
	@Key("xcure.client.room.info.popup.type")
	String roomTypeFieldName();
	
	/**
	 * @return the "Permanent" label from the room info popup
	 */
	@Key("xcure.client.room.type.permanent")
	String roomPermanentType();
	
	/**
	 * @return the "Temporary" label from the room info popup
	 */
	@Key("xcure.client.room.type.temporary")
	String roomTemporaryType();
	
	/**
	 * @return the "Enter" label from the room enter button
	 */
	@Key("xcure.client.room.enter.button")
	String roomEnterButton();
	
	/**
	 * @return the room manager dialog title
	 */
	@Key("xcure.client.room.manager.for.user.dialog.title")
	String roomManagerDialogTitle(String userLoginName, int num_rooms, int curr_page, int total_pages);
	
	/**
	 * @return the room manager dialog title
	 */
	@Key("xcure.client.room.manager.dialog.title")
	String roomManagerDialogTitle(  final int numb_rooms, int curr_page, int total_pages);
	
	/**
	 * @return the "number" column name for the a table
	 */
	@Key("xcure.client.table.col.number")
	String indexColumnTitle();
	
	/**
	 * @return the "File type" column name for the a table
	 */
	@Key("xcure.client.table.col.file.type")
	String fileTypeColumnTitle();
	
	/**
	 * @return the "File name" column name for the a table
	 */
	@Key("xcure.client.table.col.file.name")
	String fileNameColumnTitle();
	
	/**
	 * @return the "select" column name for the a table
	 */
	@Key("xcure.client.table.col.select")
	String selectorColumnTitle();
	
	/**
	 * @return the "User Login Name" column name for the a table
	 */
	@Key("xcure.client.table.col.user.login.name")
	String userLoginColumnTitle();
	
	/**
	 * @return the "Room name" column name for the a table
	 */
	@Key("xcure.client.table.col.room.name")
	String roomNameColumnTitle();
	
	/**
	 * @return the "type" column name for the a table
	 */
	@Key("xcure.client.table.col.type")
	String typeColumnTitle();
	
	/**
	 * @return the "Access/Type" column name for the a table
	 */
	@Key("xcure.client.table.col.access.type")
	String accessOrTypeColumnTitle();
	
	/**
	 * @return the "gender" column name for the a table
	 */
	@Key("xcure.client.table.col.gender")
	String genderColumnTitle();
	
	/**
	 * @return the "status" column name for the a table
	 */
	@Key("xcure.client.table.col.status")
	String statusColumnTitle();
	
	/**
	 * @return the "open" status name for the room table
	 */
	@Key("xcure.client.room.table.status.open")
	String roomStatusOpen();
	
	/**
	 * @return the "closed" status name for the room table
	 */
	@Key("xcure.client.room.table.status.closed")
	String roomStatusClosed();
	
	/**
	 * @return the "create new room" dialog title
	 */
	@Key("xcure.client.room.create.dialog.title")
	String createRoomDialogTitle();
	
	/**
	 * @return the "update room" dialog title
	 */
	@Key("xcure.client.room.update.dialog.title")
	String updateRoomDialogTitle( );

	/**
	 * @return the '' field name for the new/update room dialog
	 */
	@Key("xcure.client.room.name.field")
	String roomNameFieldTitle();

	/**
	 * @return the '' field name for the new/update room dialog
	 */
	@Key("xcure.client.room.description.field")
	String roomDescriptionFieldTitle();

	/**
	 * @return the '' field name for the new/update room dialog
	 */
	@Key("xcure.client.room.type.field")
	String roomTypeFieldTitle();

	/**
	 * @return the '' field name for the new/update room dialog
	 */
	@Key("xcure.client.room.duration.field")
	String roomDurationFieldTitle();

	/**
	 * @return the '' field name for the new/update room dialog
	 */
	@Key("xcure.client.room.is.permanent.field")
	String roomPermanentFieldTitle();

	/**
	 * @return the '' field name for the new/update room dialog
	 */
	@Key("xcure.client.room.is.main.field")
	String roomMainFieldTitle();
	
	/**
	 * @return the 'hour' form for the room duration period
	 */
	@Key("xcure.client.room.duration.hour")
	String roomTimeDurationHour();
	
	/**
	 * @return the 'hours' form for the room duration period
	 */
	@Key("xcure.client.room.duration.hours")
	String roomTimeDurationHours();
	
	/**
	 * @return the 'hours' form for the room duration period
	 */
	@Key("xcure.client.room.duration.hourss")
	String roomTimeDurationHourss();
	
	/**
	 * @return the 'Room users: {0} (page {1} out of {2})' dialog title
	 */
	@Key("xcure.client.room.users.manager.dialog.title")
	String roomUsersManagerDialogTitle( int num_entries, int curr_page, int total_pages);
	
	/**
	 * @return the 'Unknown' text value
	 */
	@Key("xcure.client.unknown.text")
	String unknownTextValue();
	
	/**
	 * @return the 'Clean duration' text value
	 */
	@Key("xcure.client.clean.duration.text")
	String cleanDurationTextValue();

	/**
	 * @return the 'Undefined' text value
	 */
	@Key("xcure.client.undefined.text")
	String undefinedTextValue();

	/**
	 * @return the 'Profile' string of the user menu in user room access dialog
	 */
	@Key("xcure.client.room.user.menu.profile")
	String profileMenuItemUserRoomAccess();
	
	/**
	 * @return the 'Access' string of the user menu in user room access dialog
	 */
	@Key("xcure.client.room.user.menu.access")
	String accessMenuItemUserRoomAccess();

	/**
	 * @return the 'management' string of the client side
	 */
	@Key("xcure.client.management.title")
	String managementTitle();
	
	/**
	 * @return the 'read all expires' string title for the user room access dialog
	 */
	@Key("xcure.client.room.user.access.read.all.expires.title")
	String readAllExpiresFieldName();
	
	/**
	 * @return the 'read' access check box title for the user room access dialog
	 */
	@Key("xcure.client.room.user.access.read.title")
	String readAccessFieldTitle();
	
	/**
	 * @return the 'read all' access check box title for the user room access dialog
	 */
	@Key("xcure.client.room.user.access.read.all.title")
	String readAllAccessFieldTitle();
	
	/**
	 * @return the 'write' access check box title for the user room access dialog
	 */
	@Key("xcure.client.room.user.access.write.title")
	String writeAccessFieldTitle();
	
	/**
	 * @return the 'system' access check box title for the user room access dialog
	 */
	@Key("xcure.client.room.user.access.system.title")
	String systemAccessFieldTitle();
	
	/**
	 * @return the title for the user room access dialog
	 */
	@Key("xcure.client.room.user.access.dialog.title")
	String userRoomAccessDialogTitle();
	
	/**
	 * @return the title for the information-message dialog
	 */
	@Key("xcure.client.information.dialog.title")
	String infoMessageDialog();
	
	/**
	 * @return the title of the "room-user read all expred" column
	 */
	@Key("xcure.client.room.user.read.all.expired.hint.title")
	String readAllExpiredHintTitle();
	
	/**
	 * @return the title of the "room-user read all is active" column
	 */
	@Key("xcure.client.room.user.read.all.active.hint.title")
	String readAllActiveHintTitle();
	
	/**
	 * @return the title of the "room-user read all is not on" column
	 */
	@Key("xcure.client.room.user.read.all.not.on.hint.title")
	String readAllNotOnHintTitle();
	
	/**
	 * @return The "Query" search field title
	 */
	@Key("xcure.client.search.query.field.title")
	String queryStringFieldTitle();
	
	/**
	 * @return The "Search and add users to room: {0} (page {1} out of {2})" search field title
	 */
	@Key("xcure.client.search.add.room.user.dialog.title")
	String addUsersToRoomDialogTitle( final int numberOfEntries, final int currentPageNumber, final int numberOfPages );
	
	/**
	 * @return The "online" status title
	 */
	@Key("xcure.client.user.online.status")
	String userOnlineStatus();
	
	/**
	 * @return The "offline" status title
	 */
	@Key("xcure.client.user.offline.status")
	String userOfflineStatus();
	
	/**
	 * @return The "now" status title
	 */
	@Key("xcure.client.user.online.status.now")
	String nowOnlineText();
	
	/**
	 * @return The "Last online" field title
	 */
	@Key("xcure.client.user.last.online.field.title")
	String lastOnlineFieldTitle();
	
	/**
	 * @return The "First online" field title
	 */
	@Key("xcure.client.user.first.online.field.title")
	String registrationDateFieldName();
	
	/**
	 * @return The "Send personal message" image tip
	 */
	@Key("xcure.client.user.send.message.image.tip")
	String sendMessageTipText();
	
	/**
	 * @return The "Sending message is disabled because another profile view dialog is opened, but might be invisible." image tip
	 */
	@Key("xcure.client.user.send.message.disabled.image.tip")
	String sendMessageDisabledTipText();

	/**
	 * @return The "Send message" text
	 */
	@Key("xcure.client.user.send.message.text")
	String sendMessageText();
	
	/**
	 * @return The "Send chat message" image tip
	 */
	@Key("xcure.client.user.send.chat.message.image.tip")
	String sendChatMessageTipText();

	/**
	 * @return The "Write to chat" text
	 */
	@Key("xcure.client.user.send.chat.message.text")
	String sendChatMessageText();
	
	/**
	 * @return The "Communicating with the server" image tip
	 */
	@Key("xcure.client.user.loading.data.image.tip")
	String communicatingToolTipText();
	
	/**
	 * @return The "Communicating..." text
	 */
	@Key("xcure.client.user.loading.data.text")
	String communicatingText();
	
	/**
	 * @return The "Error while communicating with the server" image tip
	 */
	@Key("xcure.client.user.error.loading.data.image.tip")
	String errorWhileCommunicatingToolTipText();
	
	/**
	 * @return The "The communication to the server was successful" image tip
	 */
	@Key("xcure.client.user.success.loading.data.image.tip")
	String successWhileCommunicatingToolTipText();
	
	/**
	 * @return The "Error" text
	 */
	@Key("xcure.client.user.error.text")
	String errorText();
	
	/**
	 * @return The "Add friend" text
	 */
	@Key("xcure.client.user.add.friend.text")
	String addFriendText();
	
	/**
	 * @return The "Remove friend" text
	 */
	@Key("xcure.client.user.remove.friend.text")
	String removeFriendText();
	
	/**
	 * @return The title for the user-personal messages dialog 
	 */
	@Key("xcure.client.user.messages.dialog.title")
	String messagesManagerDialogTitle( final int numberOfEntries, final int currentPageNumber, final int numberOfPages);
	
	/**
	 * @return The "Write" button title
	 */
	@Key("xcure.client.user.messages.trite.button.title")
	String writeButton();
	
	/**
	 * @return The "From/To" column title
	 */
	@Key("xcure.client.user.messages.from.to.column.title")
	String fromToColumnTitle();

	/**
	 * @return The "User name" column title
	 */
	@Key("xcure.client.user.messages.user.name.column.title")
	String fromToUserColumnTitle();
	
	/**
	 * @return The "From" column title
	 */
	@Key("xcure.client.user.messages.from.user.column.title")
	String fromUserColumnTitle();

	/**
	 * @return The "To" column title
	 */
	@Key("xcure.client.user.messages.to.user.column.title")
	String toUserColumnTitle();
	
	/**
	 * @return The "Message title" column title
	 */
	@Key("xcure.client.user.messages.message.title.column.title")
	String messageTitleColumnTitle();
	
	/**
	 * @return The "Date/Time" column title
	 */
	@Key("xcure.client.date.time.column.title")
	String dateTimeColumnTitle();
	
	/**
	 * @return The "Sent message read by the recepient" image tip
	 */
	@Key("xcure.client.sent.read.message.image.tip")
	String sentReadMessageImageTip();
	
	/**
	 * @return The "Sent message not read by the recepient" image tip
	 */
	@Key("xcure.client.sent.unread.message.image.tip")
	String sentUnreadMessageImageTip();
	
	/**
	 * @return The "Received and read message" image tip
	 */
	@Key("xcure.client.received.read.message.image.tip")
	String receivedReadMessageImageTip();
	
	/**
	 * @return The "Received and unread message" image tip
	 */
	@Key("xcure.client.received.unread.message.image.tip")
	String receivedUnreadMessageImageTip();
	
	/**
	 * @return The "Message titile: {0}" image tip
	 */
	@Key("xcure.client.message.full.title.tip")
	String fullMessageTitleTip(String title);
	
	/**
	 * @return The "Send personal message" dialog title
	 */
	@Key("xcure.client.send.personal.message.dialog.title")
	String sendPersonalMessageDialogTitle();
	
	/**
	 * @return the "A request for accessing the room \"{0}\"" message subject title
	 */
	@Key("xcure.client.room.access.request.message.subject.title")
	String roomAccessRequestMessageTitle(String roomName);
	
	/**
	 * @return the "The access to the room \"{0}\" is granted!" message subject title
	 */
	@Key("xcure.client.room.access.granted.message.subject.title")
	String roomAccessGrantedMessageTitle(String roomName);
	
	/**
	 * @return the "<Undefined title>" message subject title
	 */
	@Key("xcure.client.undefined.message.subject.title")
	String undefinedMessageTitleText();
	
	/**
	 * @return the "Subject" field text
	 */
	@Key("xcure.client.message.subject.field.text")
	String messageSubjectFieldTitle();
	
	/**
	 * @return the "Message body:" field text
	 */
	@Key("xcure.client.message.body.field.text")
	String messageBodyFieldTitle();
	
	/**
	 * @return the "Recepient" message field title
	 */
	@Key("xcure.client.recepient.message.field.title")
	String recepientFieldTitle();
	
	/**
	 * @return the "Sender" message field title
	 */
	@Key("xcure.client.sender.message.field.title")
	String senderFieldTitle();
	
	/**
	 * @return the "Undefined recepient" message field value
	 */
	@Key("xcure.client.undefined.recepient.message.field.value")
	String undefinedRecepientFieldValue();
	
	/**
	 * @return the "Click to select the recepient" message field value
	 */
	@Key("xcure.client.click.to.select.recepient.message.field.tip")
	String clickToSelectTheRecepientTip();
	
	/**
	 * @return the "Click to view the profile of the message recepient" message field value
	 */
	@Key("xcure.client.click.to.view.profile.of.message.recepient.field.tip")
	String clickToViewRecepientProfileTip();
	
	/**
	 * @return the "Unknown" user name for the deleted message sender/receiver
	 */
	@Key("xcure.client.unknown.message.sender.receiver.value")
	String unknownMsgSenderReceiver();
	
	/**
	 * @return the "The recepient of the message has deleted his/her profile" tip message
	 */
	@Key("xcure.client.unknown.message.recepient.tip")
	String recepientHasDeletedHisProfile();
	
	/**
	 * @return the "The sender of the message has deleted his/her profile" tip message
	 */
	@Key("xcure.client.unknown.message.sender.tip")
	String senderHasDeletedHisProfile();
	
	/**
	 * @return the "Select message recepient: {0} (page {1} of {2})" dialog title
	 */
	@Key("xcure.client.select.message.recepient.dialog.title")
	String selectMessageRecepientDialogTitle( int numberOfEntries, int currentPageNumber, int numberOfPages );
	
	/**
	 * @return the "Sender: {0}" profile link tip
	 */
	@Key("xcure.client.message.sender.profile.link.tip")
	String messageSenderProfileTip( String userLoginName );
	
	/**
	 * @return the "Recepient: {0}" profile link tip
	 */
	@Key("xcure.client.message.recepient.profile.link.tip")
	String messageRecepientProfileTip( String userLoginName );
	
	/**
	 * @return the "Sent message" dialog title
	 */
	@Key("xcure.client.view.sent.simple.message.dialog.title")
	String viewSimplePersonalMessageOutDialogTitle();
	
	/**
	 * @return the "Received message" dialog title
	 */
	@Key("xcure.client.view.received.simple.message.dialog.title")
	String viewSimplePersonalMessageInDialogTitle();
	
	/**
	 * @return the "Sent room access request" dialog title
	 */
	@Key("xcure.client.view.sent.room.access.request.message.dialog.title")
	String viewRoomAccessRequestOutDialogTitle();
	
	/**
	 * @return the "Received room access request" dialog title
	 */
	@Key("xcure.client.view.received.room.access.request.message.dialog.title")
	String viewRoomAccessRequestInDialogTitle();
	
	/**
	 * @return the "Room-access notification" dialog title
	 */
	@Key("xcure.client.view.room.access.notification.message.dialog.title")
	String viewRoomAccessNotificationDialogTitle();
	
	/**
	 * @return the "Sent at" field title
	 */
	@Key("xcure.client.view.message.sent.at.field.title")
	String sentAtFieldTitle();
	
	/**
	 * @return the "Received at" field title
	 */
	@Key("xcure.client.view.message.received.at.field.title")
	String receivedAtFieldTitle();
	
	/**
	 * @return the "Message title" field title
	 */
	@Key("xcure.client.view.message.long.subject.field.title")
	String messageSubjectFieldLongTitle();
	
	/**
	 * @return the "View messages" value
	 */
	@Key("xcure.client.view.message.type.field.name")
	String viewMessagesTypeField();
	
	/**
	 * @return the "Received" value
	 */
	@Key("xcure.client.view.received.messages.check.list.item")
	String receivedMessagesListBoxItem();
	
	/**
	 * @return the "Sent" value
	 */
	@Key("xcure.client.view.sent.messages.check.list.item")
	String sentMessagesListBoxItem();
	
	/**
	 * @return the "All" value
	 */
	@Key("xcure.client.view.all.messages.check.list.item")
	String allMessagesListBoxItem();
	
	/**
	 * @return the "Re:" prefix text
	 */
	@Key("xcure.client.reply.message.title.prefix")
	String replyMessageTitlePrefix();
	
	/**
	 * @return the "The maximum text length is exceded by {0} symbols" text
	 */
	@Key("xcure.client.text.progress.exceeding.maximum.text.length.msg")
	String exceedingTextLength(int extraLength);
	
	/**
	 * @return the "You can still input up to {0} symbols" text
	 */
	@Key("xcure.client.text.progress.remaining.text.length.msg")
	String remainingTextLength(int remainingLength);
	
	/**
	 * @return the "Close room: {0}" image tip
	 */
	@Key("xcure.client.close.room.image.button.tip")
	String getCloseRoomImageTip( String roomName );
	
	/**
	 * @return the "New chat room activity" image tip
	 */
	@Key("xcure.client.new.chat.room.activity.image.button.tip")
	String newChatActivityImageTip( );
	
	/**
	 * @return the "Click to view the chat room data" image tip
	 */
	@Key("xcure.client.click.to.view.chat.room.data.image.button.tip")
	String openRoomInfoImageTip( );
	
	/**
	 * @return the "Site users: {0} (page {1} of {2})" dialog title
	 */
	@Key("xcure.client.site.users.dialog.title")
	String userSearchDialogTitle(  int numberOfUsers, int currentPageNumber, int numberOfPages );
	
	/**
	 * @return the "Delete" link title for file management
	 */
	@Key("xcure.client.delete.link.title")
	String deleteLinkTitle();
	
	/**
	 * @return the "Upload" link title for file management
	 */
	@Key("xcure.client.upload.link.title")
	String uploadLinkTitle();
	
	/**
	 * @return the "Choose" link title for file management
	 */
	@Key("xcure.client.choose.link.title")
	String chooseLinkTitle();
	
	/**
	 * @return the "User avatar upload" dialog title
	 */
	@Key("xcure.client.user.avatar.upload.dialog.title")
	String uploadProfileAvatarDialogTitle();
	
	/**
	 * @return the "Choose an avatar" dialog title
	 */
	@Key("xcure.client.user.avatar.choose.dialog.title")
	String chooseUserAvatarDialogTitle();
	
	/**
	 * @return the "Hidden user" tip message
	 */
	@Key("xcure.client.user.hidden.tip")
	String hiddenUserTip(); 
	
	/**
	 * @return "Already closed" value for the room expiration field
	 */
	@Key("xcure.client.room.expired.field.value")
	String alreadyClosedRoomFieldValue();
	
	/**
	 * @return The numer of room visitors is: {0}
	 */
	@Key("xcure.client.room.number.of.visitors")
	String currentNumberOfRoomVisitors( int numberOfVisitors);
	
	/**
	 * @return Would you like to request access to room \"{0}\" from its owner?
	 */
	@Key("xcure.client.room.access.request.dialog.question")
	String roomAccessRequestDialogQuestion( String roomName );
	
	/**
	 * @return "Request room access?" dialog title
	 */
	@Key("xcure.client.room.access.request.dialog.title")
	String roomAccessRequestDialogTitle();
	
	/**
	 * @return "Simple" chat message type
	 */
	@Key("xcure.client.chat.room.simple.message.type")
	String chatMessageTypeSimple();
	
	/**
	 * @return "Private" chat message type
	 */
	@Key("xcure.client.chat.room.private.message.type")
	String chatMessageTypePrivate();
	
	/**
	 * @return "Information" chat message type
	 */
	@Key("xcure.client.chat.room.information.message.type")
	String chatMessageTypeInformation();
	
	/**
	 * @return "Error" chat message type
	 */
	@Key("xcure.client.chat.room.error.message.type")
	String chatMessageTypeError();
	
	/**
	 * @return "System message" avatar title
	 */
	@Key("xcure.client.chat.room.system.message.avatar.title")
	String systemChatMessageAvatarTitle();
	
	/**
	 * @return "Administrator" avatar title
	 */
	@Key("xcure.client.chat.room.administrator.avatar.title")
	String administratorAvatarTitle();
	
	/**
	 * @return "Click to unblock user messages" tool tip
	 */
	@Key("xcure.client.chat.room.unblock.user.messages.tip")
	String clickToUnblockUserMessages( final String userLoginName );
	
	/**
	 * @return "Click to block user messages" tool tip
	 */
	@Key("xcure.client.chat.room.block.user.messages.tip")
	String clickToBlockUserMessages( final String userLoginName );
	
	/**
	 * @return "Click to view short user info" tool tip
	 */
	@Key("xcure.client.chat.room.click.to.view.short.user.info")
	String clickToViewShortUserInfoToolTip( String loginName );
	
	/**
	 * @return "Unknown user"
	 */
	@Key("xcure.client.chat.room.message.unknown.user.login.name")
	String unknownUserLoginName();
	
	/**
	 * @return "Add message recipient" tool tip text
	 */
	@Key("xcure.client.chat.room.message.add.recipient.tip")
	String addRecipientToolTip();
	
	/**
	 * @return "Remove message recipient {0}" tool tip text
	 */
	@Key("xcure.client.chat.room.message.remove.recipient.tip")
	String removeRecipientToolTip( String recipientLoginName );
	
	/**
	 * @return "Image" title
	 */
	@Key("xcure.client.chat.message.add.file.link.title")
	String chatMessageAddFileTitle();
	
	/**
	 * @return "Click to attach image to the chat message" title
	 */
	@Key("xcure.client.chat.message.add.file.link.tool.tip")
	String chatMessageAddFileToolTip();
	
	/**
	 * @return "Smiles" link title
	 */
	@Key("xcure.client.smiles.link.title")
	String smilesLinkTitle();
	
	/**
	 * @return "Click to open the dialog with all available smiles" link title
	 */
	@Key("xcure.client.smiles.link.tool.tip")
	String smilesLinkToolTip();
	
	/**
	 * @return "Attachments" link title
	 */
	@Key("xcure.client.files.link.title")
	String filesLinkTitle();
	
	/**
	 * @return "Click to open the dialog for managing the attached files" link title
	 */
	@Key("xcure.client.files.link.tool.tip")
	String filesLinkToolTip();
	
	/**
	 * @return "Keep the dialog open" title
	 */
	@Key("xcure.client.keep.dialog.open.title")
	String keepDialogOpenTitle();
	
	/**
	 * @return "Message flows" title
	 */
	@Key("xcure.client.use.message.flows.title")
	String messageFlowsTitle();
	
	/**
	 * @return "Flows" title
	 */
	@Key("xcure.client.use.message.flows.short.title")
	String messageFlowsTitleShort();
	
	/**
	 * @return "When enabled, allow to derive font settings when replying to a chat message" title
	 */
	@Key("xcure.client.use.message.flows.tool.tip")
	String messageFlowsToolTip();
	
	/**
	 * @return "Message alert" title
	 */
	@Key("xcure.client.use.message.alert.title")
	String messageAlertTitle();
	
	/**
	 * @return "Alert" title
	 */
	@Key("xcure.client.use.message.alert.short.title")
	String messageAlertTitleShort();
	
	/**
	 * @return "When enabled, allows to see the avatar alerts for the chat message addressed to you" title
	 */
	@Key("xcure.client.use.message.alert.tool.tip")
	String messageAlertToolTip();
	
	/**
	 * @return "Send message to room {0}" dialog title
	 */
	@Key("xcure.client.chat.message.send.to.current.room.dialog.title")
	String sendMessageToCurrentRoomDialogTitle( String roomTitle );
	
	/**
	 * @return "Private message" check box text
	 */
	@Key("xcure.client.chat.message.private.message.check.box.title")
	String privateMessageCheckBoxTitle();
	
	/**
	 * @return "The private message is only visible to its recipients" check box text
	 */
	@Key("xcure.client.chat.message.private.message.check.box.tip")
	String privateMessageCheckBoxToolTip();
	
	@Key("xcure.client.chat.message.text.font")
	String fontTitile();
	@Key("xcure.client.chat.message.text.font.serif")
	String fontSerifTitile();
	@Key("xcure.client.chat.message.text.font.sans.serif")
	String fontSansSerifTitile();
	@Key("xcure.client.chat.message.text.font.fantasy")
	String fontFantasyTitile();
	@Key("xcure.client.chat.message.text.font.cursive")
	String fontCursiveTitile();
	@Key("xcure.client.chat.message.text.font.monospace")
	String fontMonospaceTitile();

	@Key("xcure.client.chat.message.text.size")
	String fontSizeTitile();
	@Key("xcure.client.chat.message.text.size.x.small")
	String fontXSmallTitile();
	@Key("xcure.client.chat.message.text.size.small")
	String fontSmallTitile();
	@Key("xcure.client.chat.message.text.size.medium")
	String fontMediumTitile();
	@Key("xcure.client.chat.message.text.size.large")
	String fontLargeTitile();
	@Key("xcure.client.chat.message.text.size.x.large")
	String fontXLargeTitile();

	@Key("xcure.client.chat.message.text.color")
	String fontColorTitile();
	@Key("xcure.client.chat.message.text.color.three.name")
	String fontColorThreeTitile();
	@Key("xcure.client.chat.message.text.color.four.name")
	String fontColorFourTitile();
	@Key("xcure.client.chat.message.text.color.two.name")
	String fontColorTwoTitile();
	@Key("xcure.client.chat.message.text.color.six.name")
	String fontColorSixTitile();
	@Key("xcure.client.chat.message.text.color.one.name")
	String fontColorOneTitile();
	@Key("xcure.client.chat.message.text.color.five.name")
	String fontColorFiveTitile();
	@Key("xcure.client.chat.message.text.color.seven.name")
	String fontColorSevenTitile();

	@Key("xcure.client.chat.message.text.sample.field")
	String fontSampleField();
	@Key("xcure.client.chat.message.text.sample.text")
	String fontSampleText();
	@Key("xcure.client.chat.message.text.sample.tool.tip")
	String fontSampleToolTip();
	
	/**
	 * @return "Message (minimize/maximize)" panel title
	 */
	@Key("xcure.client.send.chat.message.dialog.message.panel")
	String disclosurePanelSendChatMessageDialogTitle();
	
	/**
	 * @return "All room visitors"
	 */
	@Key("xcure.client.send.chat.message.recipient.everyone")
	String recepientsAreEveryone();
	
	/**
	 * @return "No on in the room"
	 */
	@Key("xcure.client.send.chat.message.recipient.noone")
	String recepientsAreNoone();
	
	/**
	 * @return "Recipients" field title
	 */
	@Key("xcure.client.chat.message.recipients.field.title")
	String recipientsFieldTitle();
	
	/**
	 * @return "Click here to reply to the chat message" tool tip
	 */
	@Key("xcure.client.chat.message.click.here.to.reply")
	String clickHereToReplyToTheMessage();
	
	/**
	 * @return "Click here to send new message" tool tip
	 */
	@Key("xcure.client.chat.message.click.here.to.send.new.message")
	String clickHereToSendNewMessage();
	
	/**
	 * @return "Attach/delete file" dialog title
	 */
	@Key("xcure.client.chat.message.attach.delete.file.dialog.title")
	String attachChatMessageFileDialogTitle();
	
	/**
	 * @return "Click to view" tool tip
	 */
	@Key("xcure.client.click.to.view.tool.tip")
	String clickToViewToolTip();
	
	/**
	 * @return "Chat file view" dialog title
	 */
	@Key("xcure.client.chat.message.file.view.dialog.title")
	String chatMessageFileViewDialogTitle();
	
	/**
	 * @return "Favorites" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.favorites")
	String favoriteSmileCagtegoryTitle();
	
	/**
	 * @return "Presents" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.presents")
	String presentsSmileCagtegoryTitle();
	
	/**
	 * @return "funny" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.funny")
	String funnySmileCagtegoryTitle();
	
	/**
	 * @return "Teasing" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.teasing")
	String teasingSmileCagtegoryTitle();
	
	/**
	 * @return "Talking" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.talking")
	String talkingSmileCagtegoryTitle();
	
	/**
	 * @return "Relaxing" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.relaxing")
	String relaxingSmileCagtegoryTitle();
	
	/**
	 * @return "Emotions" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.emotions")
	String emotionsSmileCagtegoryTitle();
	
	/**
	 * @return "Love" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.love")
	String loveSmileCagtegoryTitle();
	
	/**
	 * @return "Sound" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.sound")
	String soundSmileCagtegoryTitle();
	
	/**
	 * @return "Miscellaneous" smiles category name
	 */
	@Key("xcure.client.chat.message.smiles.category.miscellaneous")
	String miscSmileCagtegoryTitle();
	
	/**
	 * @return "Smiles selection" dialog title
	 */
	@Key("xcure.client.chat.message.smiles.selection.dialog.title")
	String smilesSelectionDialogTitle();
	
	/**
	 * @return "Click to put the smile code into the chat message" tool tip
	 */
	@Key("xcure.client.chat.message.smiles.selection.smile.tool.tip")
	String clickToPutTheSmileCodeIntoTheChatMessage();
	
	/**
	 * @return "Smiles (minimize/maximize)" panel title
	 */
	@Key("xcure.client.send.chat.message.dialog.smiles.panel")
	String disclosurePanelSmilesSelectionDialogTitle();
	
	/**
	 * @return "Transliteration is ON! Click on the bar at the left to turn it OFF" tool tip
	 */
	@Key("xcure.client.text.object.transliteration.is.on.tool.tip")
	String translitIsOnToolTip();
	
	/**
	 * @return "Transliteration is OFF! Click on the bar at the left to turn it ON" tool tip
	 */
	@Key("xcure.client.text.object.transliteration.is.off.tool.tip")
	String translitIsOffToolTip();
	
	/**
	 * @return "Click to turn transliteration ON" tool tip
	 */
	@Key("xcure.client.text.object.transliteration.is.on.button.tip")
	String translitIsOnButtonTip();
	
	/**
	 * @return "Click to turn transliteration OFF" tool tip
	 */
	@Key("xcure.client.text.object.transliteration.is.off.button.tip")
	String translitIsOffButtonTip();
	
	/**
	 * @return the "Click on the message title to show the message content" message
	 */
	@Key("xcure.client.click.message.title.to.show.message.content")
	String clickMessageTitleToShowTheMessage();
	
	/**
	 * @return the "Ha ha ha I am a funny guy" message
	 */
	@Key("xcure.client.chat.message.ha.i.am.funny.guy")
	String hahahaIamAFunnyGuyMsg();
	
	/**
	 * @return the "Click here to show/hide the message content" message
	 */
	@Key("xcure.client.click.message.title.to.show.hide.message.content.tool.tip")
	String clickMessageTitleToShowHideMessageContentToolTip();
	
	/**
	 * @return the "from" text title
	 */
	@Key("xcure.client.chat.message.from.text")
	String chatMessageFromTextTitle();
	
	/**
	 * @return the "I accept" check box text
	 */
	@Key("xcure.client.i.accept.check.box.text")
	String iAcceptCheckBoxText();
	
	/**
	 * @return the "the user agreement" text
	 */
	@Key("xcure.client.user.agreement.link.text")
	String userAgreementText();
	
	/**
	 * @return the "Reject" button text
	 */
	@Key("xcure.client.user.agreement.reject.button.text")
	String rejectUserAgreement();
	
	/**
	 * @return the "Accept" button text
	 */
	@Key("xcure.client.user.agreement.accept.button.text")
	String acceptUserAgreement();
	
	/**
	 * @return the User Agreement dialog title
	 */
	@Key("xcure.client.user.agreement.dialog.title")
	String userAdreementDialogTitle();
	
	/**
	 * @return the external URL open confirmation dialog title
	 */
	@Key("xcure.client.external.link.dialog.confirm.dialog.title")
	String urlOpenRequestDialogTitle();
	
	/**
	 * @return New topic
	 */
	@Key("xcure.client.forum.new.forum.topic.button.title")
	String newForumTopicButtonTitle();
	
	/**
	 * @return New section
	 */
	@Key("xcure.client.forum.new.forum.section.button.title")
	String newForumSectionButtonTitle();
	
	/**
	 * @return Post here
	 */
	@Key("xcure.client.forum.post.here.button.title")
	String postForumMessageHereButtonTitle();
	
	/**
	 * @return Refresh
	 */
	@Key("xcure.client.forum.refresh.messages.button.title")
	String refreshForumMessagesButtonTitle();
	
	/**
	 * @return Search the forum
	 */
	@Key("xcure.client.forum.search.field.title")
	String forumSearchFieldTitle();
	
	/**
	 * @return Only topics
	 */
	@Key("xcure.client.forum.search.only.topics")
	String onlyInTopicsCheckBox();
	
	/**
	 * @return Search only for forum topics but not in the regular messages
	 */
	@Key("xcure.client.forum.search.only.topics.tooltip")
	String onlyInTopicsCheckBoxToolTip();
	
	/**
	 * @return Only in this topic
	 */
	@Key("xcure.client.forum.search.only.this.topic")
	String onlyInThisTopicCheckBox();
	
	/**
	 * @return Search only for messages in the current topic
	 */
	@Key("xcure.client.forum.search.only.topics.tooltip")
	String onlyInThisTopicCheckBoxToolTip();
	
	/**
	 * @return Posted by
	 */
	@Key("xcure.client.forum.search.posted.by.author")
	String postedByAuthor();
	
	/**
	 * @return Search for messages posted by the selected author
	 */
	@Key("xcure.client.forum.search.posted.by.author.tooltip")
	String postedByAuthorToolTip();
	
	/**
	 * @return <Anyone>
	 */
	@Key("xcure.client.forum.search.posted.by.anyone.value")
	String postedByAnyoneValue();
	
	/**
	 * @return the "Selecting messages author: {0} (page {1} of {2})" dialog title
	 */
	@Key("xcure.client.forum.search.select.author.dialog.title")
	String selectAuthorDialogTitle( int numberOfEntries, int currentPageNumber, int numberOfPages );
	
	/**
	 * @return the "Remove author" image button tooltip
	 */
	@Key("xcure.client.forum.search.remove.author.tooltip")
	String removeAuthorToolTip();
	
	/**
	 * @return "Page" string
	 */
	@Key("xcure.client.forum.search.page.string")
	String pageString();
	
	/**
	 * @return "out of" string
	 */
	@Key("xcure.client.forum.search.out.of.string")
	String outOfString();
	
	/**
	 * @return "Forum-message reply" string
	 */
	@Key("xcure.client.forum.message.reply.dialog.title")
	String replyForumMessageDialogTitle();
	
	/**
	 * @return "New forum topic" string
	 */
	@Key("xcure.client.new.forum.topic.dialog.title")
	String newForumTopicDialogTitle();
	
	/**
	 * @return "New forum section" string
	 */
	@Key("xcure.client.new.forum.section.dialog.title")
	String newForumSectionDialogTitle();
	
	/**
	 * @return "Editing a forum message" string
	 */
	@Key("xcure.client.forum.message.edit.dialog.title")
	String editForumMessageDialogTitle();
	
	/**
	 * @return "Forum-message files" string
	 */
	@Key("xcure.client.forum.message.files.dialog.title")
	String forumMessageFilesDialogTitle( int numberOfEntries, int currentPageNumber, int numberOfPages );
	
	/**
	 * @return "Attach file to the message" string
	 */
	@Key("xcure.client.forum.message.file.upload.dialog.title")
	String forumMessageFileUploadDialog();
	
	/**
	 * @return "<Search for all, taking care of extra search modifiers>" string
	 */
	@Key("xcure.client.search.any.text.helper.message")
	String searchAnyTextHelperMessage();
	
	/**
	 * @return the "Section" field text
	 */
	@Key("xcure.client.forum.section.field.text")
	String forumSectionNameTitle();
	
	/**
	 * @return the "Topic" field text
	 */
	@Key("xcure.client.forum.topic.field.text")
	String forumTopicFieldTitle();
	
	/**
	 * @return the "Title" field text
	 */
	@Key("xcure.client.forum.message.field.text")
	String forumMessageSubjectTitle();
	
	/**
	 * @return the "Created on:" text
	 */
	@Key("xcure.client.forum.message.created.date.field")
	String forumMessageCreatedOn();
		
	/**
	 * @return the "Updated on:" text
	 */
	@Key("xcure.client.forum.message.updated.date.field")
	String forumMessageUpdatedOn();
		
	/**
	 * @return the "Number of replies:" text
	 */
	@Key("xcure.client.forum.message.number.of.replies.field")
	String forumMessageNumberOfReplies();
	
	/**
	 * @return the "Number of posts:" text
	 */
	@Key("xcure.client.forum.topic.number.of.posts.field")
	String forumTopicNumberOfPosts();

	/**
	 * @return the "Last reply on" text
	 */
	@Key("xcure.client.forum.message.last.reply.on.field")
	String forumMessageLastReplyOn();

	/**
	 * @return the "Last post on" text
	 */
	@Key("xcure.client.forum.topic.last.post.on.field")
	String forumTopicLastPostOn();
	
	/**
	 * @return the "by" text
	 */
	@Key("xcure.client.forum.message.last.reply.by.field")
	String forumMessageLastReplyBy();
		
	/**
	 * @return the "Topic description" text
	 */
	@Key("xcure.client.forum.topic.description.disclosure.panel")
	String forumMessageTopicDescription();

	/**
	 * @return the "Message body" text
	 */
	@Key("xcure.client.forum.message.body.disclosure.panel")
	String forumMessageMessageBody();
	
	/**
	 * @return the "Delete" text
	 */
	@Key("xcure.client.forum.message.delete.button")
	String forumMessageDeleteButton();
	
	/**
	 * @return the "Move" text
	 */
	@Key("xcure.client.forum.message.move.button")
	String forumMessageMoveButton();
	
	/**
	 * @return the "Approve" text
	 */
	@Key("xcure.client.forum.message.approve.button")
	String forumMessageApproveButton();
	
	/**
	 * @return the "Disapprove" text
	 */
	@Key("xcure.client.forum.message.disapprove.button")
	String forumMessageDisApproveButton();
	
	/**
	 * @return the "Edit" text
	 */
	@Key("xcure.client.forum.message.edit.button")
	String forumMessageEditButton();
	
	/**
	 * @return the "Reply" text
	 */
	@Key("xcure.client.forum.message.reply.button")
	String forumMessageReplyButton();
	
	/**
	 * @return the "View replies" text
	 */
	@Key("xcure.client.forum.message.view.replies.button")
	String forumMessageViewRepliesButton();
	
	/**
	 * @return the "Enter topic" text
	 */
	@Key("xcure.client.forum.enter.topic.button")
	String forumEnterTopicButton();
	
	/**
	 * @return the "Enter section" text
	 */
	@Key("xcure.client.forum.enter.section.button")
	String forumEnterSectionButton();
	
	/**
	 * @return the "View topic" text
	 */
	@Key("xcure.client.forum.message.view.topic.button")
	String forumMessageViewTopicButton();
	
	/**
	 * @return "Forum image view" dialog title
	 */
	@Key("xcure.client.forum.media.view.dialog.title")
	String forumMessageMediaViewDialogTitle( String fileName );
	
	/**
	 * @return "Topic by" field title
	 */
	@Key("xcure.client.forum.topic.by.field.title")
	String topicCreatedByFieldTitle();
	
	/**
	 * @return "Deleting forum message/topic" dialog title
	 */
	@Key("xcure.client.forum.delete.topic.message.dialog.title")
	String deleteForumTopicMessageWithRepliesDialogTitle();
	
	/**
	 * @return "This forum message/topic has replies, proceed with deletion?" question
	 */
	@Key("xcure.client.forum.delete.topic.message.question")
	String doYouWantToDeleteForumTopicMessageWithReplies();
	
	/**
	 * @return "Click to view forum topics"
	 */
	@Key("xcure.client.forum.message.stack.click.to.view.forum.topics")
	String forumMsgStackClickToViewForumTopics();
	
	/**
	 * @return "Click to view the custom search results"
	 */
	@Key("xcure.client.forum.message.stack.click.to.view.custom.search.results")
	String forumMsgStackClickToViewCustomSearchResults();
	
	/**
	 * @return "<<All forum topics, page {0}>>"
	 */
	@Key("xcure.client.forum.message.stack.all.forum.topics")
	String forumMsgStackAllTopicsLabel(final int pageNumber);
	
	/**
	 * @return "<<Custom search results, page {0}>>"
	 */
	@Key("xcure.client.forum.message.stack.custom.search")
	String forumMsgStackCustomSearchLabel(final int pageNumber);
	
	/**
	 * @return "Post replies, page {0}:"
	 */
	@Key("xcure.client.forum.message.stack.message")
	String forumMsgStackRepliesToPost(final int pageNumber);
	
	/**
	 * @return "Topic posts, page {0}:"
	 */
	@Key("xcure.client.forum.message.stack.topic")
	String forumMsgStackRepliesToTopic(final int pageNumber);
	
	/**
	 * @return "Section topics, page {0}:"
	 */
	@Key("xcure.client.forum.message.stack.section")
	String forumMsgStackTopicsOfSection(final int pageNumber);
	
	/**
	 * @return "Click to preview this message"
	 */
	@Key("xcure.client.forum.message.stack.click.to.preview.this.message")
	String forumMsgStackClickToViewTheMessage();
	
	/**
	 * @return "Click to view the replies to this message"
	 */
	@Key("xcure.client.forum.message.stack.click.to.view.this.message.replies")
	String forumMsgStackClickToViewReplies();
	
	/**
	 * @return "Viewing the forum message data"
	 */
	@Key("xcure.client.forum.message.view.dialog.title")
	String viewForumMessageDialogTitle();
	
	/**
	 * @return "Viewing the forum topic description"
	 */
	@Key("xcure.client.forum.topic.view.dialog.title")
	String viewForumTopicDialogTitle();
	
	/**
	 * @return "Viewing the forum section description"
	 */
	@Key("xcure.client.forum.section.view.dialog.title")
	String viewForumSectionDialogTitle();
	
	/**
	 * @return "Message ID:"
	 */
	@Key("xcure.client.forum.message.id.field")
	String forumMessageID();
	
	/**
	 * @return "Cool"
	 */
	@Key("xcure.client.forum.message.vote.good.message")
	String coolForumMessageLinkTitle();
	
	/**
	 * @return "Sucks"
	 */
	@Key("xcure.client.forum.message.vote.bad.message")
	String badForumMessageLinkTitle();
	
	/**
	 * @return "Total votes: {0}"
	 */
	@Key("xcure.client.forum.message.vote.total.votes")
	String numberOfForumMessageVotes( final int numVotes );
	
	/**
	 * @return "My vote:"
	 */
	@Key("xcure.client.forum.message.vote.my.vote")
	String myForumMessageVote();
	
	/**
	 * @return "Moving forum message with ID: {0}"
	 */
	@Key("xcure.client.forum.move.message.dialog.title")
	String moveForumMessageDialogTitle( final int messageID );
	
	/**
	 * @return "New parent ID:"
	 */
	@Key("xcure.client.forum.move.message.new.parent.id.field")
	String moveForumMessageToParent();
	
	/**
	 * @return "Web link"
	 */
	@Key("xcure.client.forum.message.web.link.title")
	String forumPostLinkTitle();
	
	/**
	 * @return "Share in twitter"
	 */
	@Key("xcure.client.forum.message.share.twitter.link.title")
	String shareInTwitterLinkTitle();
	
	/**
	 * @return "Share in vkontakte"
	 */
	@Key("xcure.client.forum.message.share.vkontakte.link.title")
	String shareInVKontakteLinkTitle();
	
	/**
	 * @return "Share in my world"
	 */
	@Key("xcure.client.forum.message.share.mailru.link.title")
	String shareInMyWorldLinkTitle();
	
	/**
	 * @return "Share in Facebook"
	 */
	@Key("xcure.client.forum.message.share.facebook.link.title")
	String shareInInFacebookLinkTitle();
	
	/**
	 * @return "<<Download>>"
	 */
	@Key("xcure.client.media.file.download.link.title")
	String fileDownloadLinkTitle();
	
	/**
	 * @return "You can download the file if you are logged in"
	 */
	@Key("xcure.client.media.file.download.if.logged.in.tooltip")
	String downloadOnlyIfLoggedInToolTip();
	
	/**
	 * @return "Click to turn the new-message notification sound on/off"
	 */
	@Key("xcure.client.new.message.notification.sound.button.tool.tip")
	String newMessageSoundNotificationButtonToolTip();
	
	/**
	 * @return New
	 */
	@Key("xcure.client.site.user.age.new.tool.tip")
	String newSiteUserAge();
	
	/**
	 * @return Beginner
	 */
	@Key("xcure.client.site.user.age.beginner.tool.tip")
	String beginnerSiteUserAge();
	
	/**
	 * @return Normal
	 */
	@Key("xcure.client.site.user.age.normal.tool.tip")
	String normalSiteUserAge();
	
	/**
	 * @return Experienced
	 */
	@Key("xcure.client.site.user.age.experiened.tool.tip")
	String experiencedSiteUserAge();
	
	/**
	 * @return Old
	 */
	@Key("xcure.client.site.user.age.old.tool.tip")
	String oldSiteUserAge();
	
	/**
	 * @return Permanent
	 */
	@Key("xcure.client.site.user.age.permanent.tool.tip")
	String permanentSiteUserAge();
	
	/**
	 * @return Send more than {0} chat messages
	 */
	@Key("xcure.client.user.gold.pieces.tool.tip")
	String goldPiecesInTheWallet(final int goldPieces);
	
	/**
	 * @return Has {0} forum messages, the next level is after {1} 
	 */
	@Key("xcure.client.user.has.from.to.num.forum.messages.tool.tip")
	String hasForumMessages(final int currCount, final int nextLevelNum);
	
	/**
	 * @return Has {0} forum messages, the top level!
	 */
	@Key("xcure.client.user.has.more.than.num.forum.messages.tool.tip")
	String hasForumMessages(final int currCount);
	
	/**
	 * @return Disable bot
	 */
	@Key("xcure.client.user.disable.bot.action")
	String disableBotText();
	
	/**
	 * @return Enable bo
	 */
	@Key("xcure.client.user.enable.bot.action")
	String enableBotText();
	
	/**
	 * @return "Radio: {0}"
	 */
	@Key("xcure.client.radio.widget.title.prefix")
	String radioTitlePrefix(final String name);
	
	/**
	 * @return "Choose radio:"
	 */
	@Key("xcure.client.radio.widget.choose.radio.text")
	String chooseRadioText();
	
	/**
	 * @return "You must have at least {0} gold pieces"
	 */
	@Key("xcure.client.access.starts.from.num.gold.pieces")
	String accessStartsFromNumGoldPieces( final int priceInGoldPieces );
	
	/**
	 * @return "The price is {0} gold pieces"
	 */
	@Key("xcure.client.price.is.num.gold.pieces")
	String priceIsNumGoldPieces( final int priceInGoldPieces );
	
	/**
	 * @return "Click to prank the user"
	 */
	@Key("xcure.client.avatar.click.to.prank.tooltip")
	String clickToPrankTheUserToolTip();
	
	/**
	 * @return "Click to remove the prank"
	 */
	@Key("xcure.client.avatar.click.clear.prank.tooltip")
	String clickToClearThePrankToolTip();
	
	/**
	 * @return "Choose avatar prank"
	 */
	@Key("xcure.client.choose.avatar.prank.dialog.title")
	String chooseAvatarAvatarDialogTitle();
	
	/**
	 * @return "Registrations"
	 */
	@Key("xcure.client.top10.section.stat.registrations.title")
	String top10RegistrationsTitle();
	
	/**
	 * @return Description for the "Registrations" statistics
	 */
	@Key("xcure.client.top10.section.stat.registrations.desc")
	String top10RegistrationsDesc();
	
	/**
	 * @return "Forum posts"
	 */
	@Key("xcure.client.top10.section.stat.forum.posts.title")
	String top10ForumPostsTitle();
	
	/**
	 * @return Description for the "Forum posts" statistics
	 */
	@Key("xcure.client.top10.section.stat.forum.posts.desc")
	String top10ForumPostsDesc();
	
	/**
	 * @return "Chat msgs"
	 */
	@Key("xcure.client.top10.section.stat.chat.messages.title")
	String top10ChatMsgsTitle();
	
	/**
	 * @return Description for the "Chat msgs" statistics
	 */
	@Key("xcure.client.top10.section.stat.chat.messages.desc")
	String top10ChatMsgsDesc();
	
	/**
	 * @return "Profile update"
	 */
	@Key("xcure.client.top10.section.stat.profile.file.update.title")
	String top10LastProfileFileTitle();
	
	/**
	 * @return Description for the "Profile update" statistics
	 */
	@Key("xcure.client.top10.section.stat.profile.file.update.desc")
	String top10LastProfileFileDesc();
	
	/**
	 * @return "Money"
	 */
	@Key("xcure.client.top10.section.stat.money.title")
	String top10MoneyTitle();
	
	/**
	 * @return Description for the "Money" statistics
	 */
	@Key("xcure.client.top10.section.stat.money.desc")
	String top10MoneyDesc();
	
	/**
	 * @return "Visits"
	 */
	@Key("xcure.client.top10.section.stat.visits.title")
	String top10UserVisitsTitle();
	
	/**
	 * @return Description for the "Visits" statistics
	 */
	@Key("xcure.client.top10.section.stat.visits.desc")
	String top10UserVisitsDesc();
	
	/**
	 * @return "Time one site"
	 */
	@Key("xcure.client.top10.section.stat.time.on.site.title")
	String top10TimeOnSiteTitle();
	
	/**
	 * @return Description for the "Time one site" statistics
	 */
	@Key("xcure.client.top10.section.stat.time.on.site.desc")
	String top10TimeOnSiteDesc();
	
	/**
	 * @return "hours"
	 */
	@Key("xcure.client.avatar.stat.time.on.site.hours")
	String avatarInfoTimeHoursOnline();
	
	/**
	 * @return "minutes"
	 */
	@Key("xcure.client.avatar.stat.time.on.site.minutes")
	String avatarInfoTimeMinutesOnline();
	
	/**
	 * @return "messages"
	 */
	@Key("xcure.client.avatar.stat.chat")
	String avatarInfoChatMsgsCount();
	
	/**
	 * @return "messages"
	 */
	@Key("xcure.client.avatar.stat.forum")
	String avatarInfoForumMsgsCount();
	
	/**
	 * @return "pieces"
	 */
	@Key("xcure.client.avatar.stat.money")
	String avatarInfoMoneyCount();
	
	/**
	 * @return "Click to rotate the image"
	 */
	@Key("xcure.client.image.view.rotate.tool.tip")
	String imageRotateToolTip();
	
	/**
	 * @return "Rotate"
	 */
	@Key("xcure.client.image.view.rotate.title")
	String imageRotateTitle();
	
	/**
	 * @return "View Yutube video"
	 */
	@Key("xcure.client.view.youtube.video.dialog.title")
	String viewYoutubeVideoDialogTitle();
}