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
package com.xcurechat.client.utils.widgets;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.FlashEmbeddedObject;

/**
 * @author zapreevis
 * This class stores only static style constants
 */
public class CommonResourcesContainer {
	//The symbol that is added to the end of the field title
	public static final String FIELD_LABEL_SUFFIX = ":";

	//The history delimiter token hash symbol
	public static final String URI_HASH_SYMBOL = "#";
	
	//The site section history delimiter
	public static final String SITE_SECTION_HISTORY_DELIMITER = "/";

	//The identifier strings for the site components, needed for enabling the browser history
	public static final String NEWS_SECTION_IDENTIFIER_STRING = ServerSideAccessManager.MAIN_SECTION_SERVLET_CONTEXT;
	public static final String CHAT_SECTION_IDENTIFIER_STRING = ServerSideAccessManager.CHAT_SECTION_SERVLET_CONTEXT;
	public static final String INFO_SECTION_IDENTIFIER_STRING = ServerSideAccessManager.INFO_SECTION_SERVLET_CONTEXT;
	public static final String FORUM_SECTION_IDENTIFIER_STRING = ServerSideAccessManager.FORUM_SECTION_SERVLET_CONTEXT;
	public static final String TOP10_SECTION_IDENTIFIER_STRING = ServerSideAccessManager.TOP10_SECTION_SERVLET_CONTEXT;

	//The active Chat rooms tree update period in milliseconds
	public static final int ACTIVE_ROOMS_TREE_UPDATE_INTERVAL_MILLISEC = 20000;

	//The width of the decorated pane decorations in puxels
	public static final int DECORATIONS_WIDTH  = 14;

	public static final int MAXIMUM_FORUM_MESSAGEBODY_HEIGHT = FlashEmbeddedObject.MAX_ALLOWED_HEIGHT + 50;

	//A reply message body line prefix
	public static final String SINGLE_PREFIX_SYMBOL = ">";

	public static final String REPLY_LINE_PREFIX = SINGLE_PREFIX_SYMBOL + SINGLE_PREFIX_SYMBOL;

	//The possible positions of the navigation buttons
	public static final int NAV_LEFT_IMG_BUTTON = 1;
	public static final int NAV_RIGHT_IMG_BUTTON = 2;
	public static final int NAV_TOP_IMG_BUTTON = 3;
	public static final int NAV_BOTTOM_IMG_BUTTON = 4;

	public static final String USER_AVATAR_CONTROL_IMAGES_LOCATION_PREFIX = ServerSideAccessManager.USERAVATAR_RELATED_IMAGES_LOCATION;
	
	/***********************************************************************************************************************/
	/****************************************************VARIOUS STYLES*****************************************************/
	/***********************************************************************************************************************/
	
	public static final String USER_DIALOG_STYLE_NAME = "xcure-UserDialog";
	
	public static final String USER_DIALOG_COMPULSORY_FIELD_STYLE = "xcure-UserDialog-CompulsoryField";
	
	public static final String USER_DIALOG_REGULAR_FIELD_STYLE = "xcure-UserDialog-RegularField";
	
	public static final String CONST_FIELD_VALUE_DEFAULT_STYLE_NAME = "xcure-UserProfileDialogUI-Value";
	
	public static final String CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME = "xcure-UserProfileDialogUI-Value-Important";

	public static final String DIALOG_LINK_BLUE_STYLE = "xcure-UserDialog-Link";
	
	public static final String DIALOG_LINK_RED_STYLE = "xcure-UserDialog-Link-Red";
	
	public static final String DIALOG_LINK_IMP_STYLE = "xcure-UserDialog-Link-Important";
	
	public static final String ACTION_IMAGE_LINK_STYLE = "xcure-ActionImage-Link";
	
	public static final String ACTION_IMAGE_DISABLED_LINK_STYLE = "xcure-ActionImage-Link-Disabled";
	
	public static final String FORCE_INLINE_DISPLAY_STYLE = "xcure-Chat-Inline-Display";
	
	public static final String CAPTCHA_IMAGE_STYLE = "xcure-CaptchaImage";
	
	public static final String USER_DIALOG_ACTION_BUTTON_STYLE = "xcure-UserDialog-ActionButton";
	
	public static final String MESSAGE_BODY_TEXT_BASE_DIALOG_STYLE = "xcure-Chat-Message-Body-TextBase-Dialog";
	
	public static final String MESSAGE_BODY_TEXT_BASE_PANEL_STYLE = "xcure-Chat-Message-Body-TextBase-Panel";
	
	public static final String CLICKABLE_STYLE = "xcure-Chat-Clickable-Style";
	
	public static final String ALL_TOPICS_SEARCH_RESULTS_STYLE = "xcure-Chat-Forum-Message-Stack-All-Topics";
	
	public static final String CUSTOM_SEARCH_RESULTS_STYLE = "xcure-Chat-Forum-Message-Stack-Custom-Search";
	
	public static final String MSG_REPLIES_STYLE = "xcure-Chat-Forum-Message-Stack-Message-Replies";
	
	public static final String MSG_REPLIES_LAST_STACK_ELEMENT_STYLE = "xcure-Chat-Forum-Message-Stack-Message-Replies-Last-Stack-Element";

	public static final String NEW_OFFLINE_MESSAGES_MENU_ELEMENT_STYLE = "xcure-Chat-New-Offline-Message-Menu-Item";

	public static final String RADIO_MENU_ITEM_SYTLE = "xcure-Chat-Radio-Menu-Item";

	public static final String SOUND_ON_IMAGE_STYLE = "xcure-Chat-Sound-On-Image";

	public static final String SOUND_OFF_IMAGE_STYLE = "xcure-Chat-Sound-Off-Image";

	public static final String ALERT_WIDGET_VISIBLE_STYLE = "xcure-Char-Sound-Button-Panel-Visible";

	public static final String ALERT_WIDGET_IN_VISIBLE_STYLE = "xcure-Char-Sound-Button-Panel-In-Visible";

	public static final String STATISTICS_PANEL_FIELD_STYLE = "xcure-Chat-Main-Visitors-Statistics-Field";

	public static final String STATISTICS_PANEL_VALUE_STYLE = "xcure-Chat-Main-Visitors-Statistics-Value";

	public static final String SITE_LOADING_COMPONENT_GLASS_PANEL_STYLE = "xcure-Chat-Loading-Component-Glass-Panel-Style";

	public static final String INTERNAL_ROUNDED_CORNER_PANEL_STYLE = "xcure-Info-PopUpPanel";

	public static final String INTERNAL_ROUNDED_CORNER_PANEL_CONTENT_STYLE = "xcure-Info-PopUpPanel-Content";

	public static final String LOADING_LABEL_STYLE = "xcure-UserProfileDialogUI-Value-Important";

	public static final String MAIN_SITE_TITLE_MENU_PANEL_STYLE = "xcure-Chat-Main-Title-Panel-Style";

	public static final String NAVIGATION_IMAGE_ENTRY_STYLE = "xcure-Chat-Site-Navigation-Title-Image";

	public static final String NAVIGATION_IMAGE_ENTRY_SELECTED_STYLE = "xcure-Chat-Site-Navigation-Title-Image-Selected";

	public static final String SITE_SECTION_LINK_STYLE = "xcure-Chat-Site-Section-Link";

	public static final String ALERT_IMAGE_STYLE = "xcure-Chat-Site-Section-Alert-Image";

	//The additional style for the user status widget 
	public static final String USER_STATUS_WIDGET_EXTRA_STYLE = "xcure-User-Status-Widget-Extra-Style";

	//The style name for the help item in the help panel
	public static final String HELP_ITEM_STYLE_STYLE = "xcure-Help-Menu-Item-Link";

	public static final String IE_ERROR_PANEL_STYLE_NAME = "xcure-Chat-IE-Error-Panel";

	public static final String INFO_MESSAGE_AVATAR_STYLE = "xcure-Chat-Info-Message-Avatar";

	public static final String ERROR_MESSAGE_AVATAR_STYLE = "xcure-Chat-Error-Message-Avatar";

	public static final String SIMPLE_MESSAGE_AVATAR_STYLE = "xcure-Chat-Simple-Message-Avatar";

	public static final String PRIVATE_MESSAGE_AVATAR_STYLE = "xcure-Chat-Private-Message-Avatar";

	public static final String SIMPLE_MESSAGE_AVATAR_PANEL_STYLE = "xcure-Chat-Simple-Message-Avatar-Panel";

	public static final String PRIVATE_MESSAGE_AVATAR_PANEL_STYLE = "xcure-Chat-Private-Message-Avatar-Panel";

	public static final String INFO_MESSAGE_AVATAR_PANEL_STYLE = "xcure-Chat-Info-Message-Avatar-Panel";

	public static final String ERROR_MESSAGE_AVATAR_PANEL_STYLE = "xcure-Chat-Error-Message-Avatar-Panel";

	public static final String USER_NAME_AVATAR_STYLE = "xcure-Chat-User-Name-Avatar";

	public static final String CHAT_MESSAGE_BODY_CONTENT_STYLE = "xcure-Chat-Message-Content";

	public static final String CHAT_MESSAGE_BODY_CONTENT_FOCUS_STYLE = "xcure-Chat-Message-Content-Focus";

	public static final String INFO_MESSAGE_TITLE_STYLE = "xcure-Chat-Info-Message-Title";

	public static final String ERROR_MESSAGE_TITLE_STYLE = "xcure-Chat-Error-Message-Title";

	public static final String SIMPLE_MESSAGE_TITLE_STYLE = "xcure-Chat-Simple-Message-Title";

	public static final String PRIVATE_MESSAGE_TITLE_STYLE = "xcure-Chat-Private-Message-Title";

	public static final String USER_STATUS_LABEL_STYLE_NAME = "xcure-Chat-Info-Message-User-Status";

	public static final String CHAT_MESSAGE_TITLE_STYLE = "xcure-Chat-Message-Title";

	public static final String CLICKABLE_PANEL_STYLE = "xcure-Chat-Clickable-Panel";

	public static final String ZOOME_IN_IMAGE_STYLE = "xcure-Chat-Zoom-In-Cursor";

	public static final String CHAT_MESSAGES_TABLE_STYLE_NAME = "xcure-Chat-Chat-Messages-Table";

	public static final String CHAT_MESSAGES_SCROLL_PANEL_STYLE_NAME = "xcure-Chat-Messages-ScrollPanel";

	public static final String CHAT_MESSAGES_VERTICAL_ALIGN_PANEL_STYLE_NAME = "xcure-Chat-Chat-Messages-Vertical-Align-Panel";

	public static final String CHAT_MESSAGES_FOCUS_PANEL_STYLE_NAME = "xcure-Chat-Chat-Messages-FocusPanel";

	public static final String CHAT_ENTRY_AVATAR_LEFT_STYLE = "xcure-Chat-Chat-Messages-Table-Avatar-Left";

	public static final String CHAT_ENTRY_AVATAR_RIGHT_STYLE = "xcure-Chat-Chat-Messages-Table-Avatar-Right";

	public static final String CHAT_ENTRY_MESSAGE_LEFT_STYLE = "xcure-Chat-Chat-Messages-Table-Message-Left";

	public static final String CHAT_ENTRY_MESSAGE_RIGHT_STYLE = "xcure-Chat-Chat-Messages-Table-Message-Right";

	public static final String SIMPLE_MESSAGE_STYLE_LEFT = "xcure-Chat-Simple-Message-Left";

	public static final String SIMPLE_MESSAGE_STYLE_RIGHT = "xcure-Chat-Simple-Message-Right";

	public static final String PRIVATE_MESSAGE_STYLE_LEFT = "xcure-Chat-Private-Message-Left";

	public static final String PRIVATE_MESSAGE_STYLE_RIGHT = "xcure-Chat-Private-Message-Right";

	public static final String USER_ROOM_ENTER_INFO_MESSAGE_STYLE_LEFT = "xcure-Chat-Info-Message-Left";

	public static final String USER_ROOM_ENTER_INFO_MESSAGE_STYLE_RIGHT = "xcure-Chat-Info-Message-Right";

	public static final String USR_ROOM_LEAVE_INFO_MESSAGE_STYLE_LEFT = "xcure-Chat-Info-Message-Left";

	public static final String USR_ROOM_LEAVE_INFO_MESSAGE_STYLE_RIGHT = "xcure-Chat-Info-Message-Right";

	public static final String ROOM_IS_CLOSING_INFO_MESSAGE_STYLE_LEFT = "xcure-Chat-Info-Message-Left";

	public static final String ROOM_IS_CLOSING_INFO_MESSAGE_STYLE_RIGHT = "xcure-Chat-Info-Message-Right";

	public static final String CHAT_ROOM_COMPOSITE_STYLE_NAME = "xcure-ChatRoomUI";

	public static final String MAIN_CHAT_ROOM_TABLE_PANEL = "xcure-Chat-Chat-Room-Table";

	//The style for the scrollable panel
	public static final String SCROLLABLE_SIMPLE_PANEL = "xcure-Chat-Scrollable-Panel";

	public static final String ROOMS_VS_USERS_DECORATED_STACK_PANEL_STYLE = "xcure-Chat-Rooms-vs-Users-Panel";

	public static final String CHAT_MESSAGES_PANEL_BOTTOM_LINE = "xcure-Chat-Messages-Panel-Bottom-Line";

	public static final String ERROR_MESSAGE_LEFT_UI_STYLE = "xcure-Chat-Error-Message-Left";

	public static final String ERROR_MESSAGE_RIGHT_UI_STYLE = "xcure-Chat-Error-Message-Right";

	public static final String FONT_SELECTION_PANEL_STYLE = "xcure-Chat-Font-Selector-Panel";

	public static final String FONT_SAMPLE_LABEL_STYLE = "xcure-Chat-Font-Selector-Sample-Label";

	//The leave room (close button) image styles
	public static final String TITLE_ACTION_IMAGE_STYLE = "xcure-RoomsManagerUI-TitleActionImage";

	public static final String OPEN_CHAT_ROOM_STYLE_NAME = "xcure-RoomsManager";

	public static final String RECEPIENTS_PANEL_STYLE = "xcure-Chat-Recipients-Panel";

	public static final String SEND_CHAT_MESSAGE_DIALOG_EXTRA_STYLE = "xcure-Chat-Send-Chat-Message-Dialog";

	public static final String REMOVE_CHAT_MSG_RECEPIENT_IMAGE_STYLE = "xcure-Chat-Remove-Image-Button";

	public static final String ADD_CHAT_MSG_RECEPIENT_IMAGE_STYLE = "xcure-Chat-Message-Add-Recepient-Image";

	public static final String RECEPIENTS_SCROLL_PANEL_STYLE = "xcure-Chat-Chat-Message-Recepient-ScrollPanel";

	public static final String SEND_CHAT_MESSAGE_PANEL_STYLE = "xcure-Chat-Send-Chat-Message-Panel";

	public static final String FLOAT_NONE_LEMENT_STYLE = "xcure-Chat-Float-None";

	public static final String SIMPLE_MESSAGE_FAKE_AVATAR_PANEL_STYLE = "xcure-Chat-Simple-Message-Fake-Avatar-Panel";

	public static final String PRIVATE_MESSAGE_FAKE_AVATAR_PANEL_STYLE = "xcure-Chat-Private-Message-Fake-Avatar-Panel";

	public static final String COMMON_DECORATION_PANEL_STYLE = "xcure-Chat-Gray-Decorated-Panel";

	public static final String INTRODUCTION_PANEL_STYLE = "xcure-Chat-Introduction-Panel";

	public static final String FILE_UPLOAD_DIALOG_THUMB_IMAGE_STYLE = "xcure-Chat-File-Upload-ImageThumnbail";

	public static final String FILE_UPLOAD_DIALOG_THUMB_IMAGE_DIS_STYLE = "xcure-Chat-File-Upload-ImageThumnbail-Dis";

	public static final String PAGED_DIALOG_TABLE_STYLE_NAME = "xcure-PagedActionGreedDialogUI-DataTable";

	public static final String PAGED_DIALOG_TABLE_LABEL_STYLE = "xcure-PagedActionGreedDialogUI-TableLabel";

	public static final String PAGED_DIALOG_STATUS_IMAGE_STYLE_NAME = "xcure-Status-Image";

	public static final String USER_SEARCH_DIALOG_STATUS_IMAGE_STYLE_NAME = "xcure-UserSearch-Status-Image";

	public static final String SEARCH_QUERY_TEXT_BOX_STYLE_NAME = "xcure-Search-Query-TexBox";

	public static final String IMAGE_MEDIA_FILE_SHOW_STYLE = "xcure-Chat-Image-Media-File-View-Style";

	public static final String MEDIA_FILE_WIDGET_BORDER_STYLE = "xcure-Chat-Media-File-Widget-Border-Style";

	public static final String PROFILE_IMAGE_SHOW_STYLE = "xcure-UserDialog-ShowImage";

	public static final String DELETED_MESSAGE_SENDER_RECEPIENT_LINK_STYLE = "xcure-SendMessage-deleted-Sender-Recipient-Link";

	public static final String MESSAGE_TITLE_READ_LINK_STYLE = "xcure-MessagesManager-Message-Title-Read-Link";

	public static final String MESSAGE_TITLE_UNREAD_LINK_STYLE = "xcure-MessagesManager-Message-Title-Unread-Link";

	public static final String UNDEFINED_MESSAGE_RECEPIENT_LINK = "xcure-SendMessage-Undefined-Recipient-Link";

	public static final String DEFINED_MESSAGE_RECEPIENT_LINK = "xcure-SendMessage-Defined-Recipient-Link";

	public static final String MESSAGE_TITLE_TEXT_BOX_STYLE_NAME = "xcure-SendMessage-Dialog-Title-TextBox";

	public static final String MESSAGE_BODY_STYLE = "xcure-Offline-Message-Body-TextArea";

	//The style for the private-message title's scroll panel
	public static final String PRIVATE_MESSAGE_TITLE_SCROLL_PANEL_STYLE = "xcure-Chat-Private-Message-Title-Scroll-Panel";

	//The style for the private-message body's scroll panel
	public static final String PRIVATE_MESSAGE_BODY_SCROLL_PANEL_STYLE = "xcure-Chat-Private-Message-Body-Scroll-Panel";

	//The style for the private-message body's scroll panel
	public static final String PRIVATE_MESSAGE_BODY_SCROLL_PANEL_CONTENT_STYLE = "xcure-Chat-Private-Message-Body-Scroll-Panel-Content";

	//Loading message text status styles
	public static final String VIEW_MESSAGE_DIALOG_IMPORTANT_VALUE_STYLE_NAME = "xcure-ViewMessageDialogUI-Value-Important";

	public static final String AVATAR_IMAGE_CHOICE_DEFAULT_STYLE = "xcure-Avatar-Image-Choice";

	public static final String AVATAR_IMAGE_IN_LIST_STYLE = "xcure-Avatar-Widget-In-List";

	public static final String CHOOSE_AVATAR_PANEL_STYLE = "xcure-Choose-Avatar-Panel";

	public static final String USER_PROFILE_ABOUT_ME_TEXT_AREA_STYLE = "xcure-View-User-Profile-About-Me-Text-Box";

	//This button is not centered but is aligned to the right
	public static final String ROOM_DIALOG_ACTION_BUTTON_STYLE = "xcure-RoomDialogUI-ActionButton";

	public static final String ROOM_DIALOG_MANAGEMENT_LINK = "xcure-UserDialog-Link";

	public static final String USER_AGREEMENT_TEXT_AREA_STYLE = "xcure-User-Agreement-Dialog-Text-Area";

	public static final String USER_STATS_DIALOG_IMAGE_STYLE = "xcure-UserStatsDialog-Image";

	public static final String USER_STATS_CLEAR_BUTTON_STYLE = "xcure-UserStatsDialog-ClearButton";

	public static final String USER_STATS_COMPULSORY_FIELD_LABEL = "xcure-UserDialog-CompulsoryField";

	public static final String ERROR_MESSAGES_DIALOG_STYLE = "xcure-ErrorMessagesDialogUI";

	public static final String ERROR_MESSAGES_DIALOG_BUTTON_STYLE = "xcure-ErrorMessagesDialogUI-Button";

	public static final String INFO_MESSAGE_DIALOG_STYLE = "xcure-InfoMessageDialogUI";

	public static final String INFO_MESSAGE_DIALOG_BUTTON_STYLE = "xcure-InfoMessageDialogUI-Button";

	public static final String ACTION_BUTTON_STYLE = "xcure-UserDialog-ActionButton";

	public static final String COMPULSORY_FIELD_STYLE = "xcure-UserDialog-CompulsoryField";

	public static final String REGULAR_FIELD_STYLE = "xcure-UserDialog-RegularField";

	public static final String DISABLED_FIELD_STYLE = "xcure-UserDialog-DisabledField";

	public static final String LINK_DISABLED_STYLE = "xcure-UserDialog-Link-Disabled";

	public static final String LINK_BLUE_STYLE = "xcure-UserDialog-Link";

	public static final String FORUM_TITLE_COMPONENT_STYLE = "xcure-Chat-Forum-Title-Component";

	public static final String GRAY_ROUNDED_CORNER_PANEL_STYLE = "xcure-Chat-Gray-Decorated-Panel";

	public static final String REMOVE_AUTHOR_BUTTON_IMAGE_STYLE = "xcure-Chat-Remove-Image-Button";

	public static final String REMOVE_AUTHOR_BUTTON_DIS_IMAGE_STYLE = "xcure-Chat-Remove-Image-Button-Disabled";

	public static final String FLOAT_LEFT_INSTEAD_OF_INLINE_STYLE = "xcure-Chat-Float-Left-Style";

	public static final String ACTION_PANEL_BUTTON_EXTRA_STYLE = "xcure-Chat-Forum-Action-Panel-Button-Style";

	public static final String CREATE_NEW_FORUM_MSG_IMAGE_STYLE = "xcure-Chat-Forum-New-Message-Arrow-Image-Style";

	public static final String FORUM_SEARCH_PANEL_DELIMITER_STYLE = "xcure-Chat-Search-Panel-Delimiter";

	public static final String FORUM_MESSAGE_STACK_PRIMARY_STYLE = "xcure-Chat-Forum-Stack-Panel-PriaryStyle";

	public static final String VERTICAL_PANEL_STYLE = "xcure-Chat-Forum-Stack-Panel-Vert-Panel-PriaryStyle";

	public static final String STACK_NAVIGATOR_DEC_PANEL_ELEMENT_STYLE = "xcure-Chat-Forum-Stack-Nav-Dec-Panel";

	public static final String STACK_NAVIGATOR_SCROLL_PANEL_ELEMENT_STYLE = "xcure-Chat-Forum-Stack-Nav-Scroll-Panel";

	public static final String MESSAGE_TITLE_STYLE = "xcure-Forum-Message-Title-TextBox";

	public static final String FORUM_MESSAGE_BODY_STYLE = "xcure-Forum-Message-Body-TextArea";

	public static final String MAXIMUM_SIZE_FOR_MESSAGE_VIEW_STYLE = "xcure-Chat-Max-Forum-Message-View-Dialog";

	public static final String MAXIMUM_WIDTH_FOR_MESSAGE_VIEW_STYLE = "xcure-Chat-Max-Width-Forum-Message-View-Dialog";

	public static final String MAXIMUM_WIDTH_MESSAGE_TITLE_STYLE = "xcure-Chat-Max-Width-Forum-Message-Title";

	public static final String MAXIMUM_WIDTH_MESSAGE_BODY_SCROLL_STYLE = "xcure-Chat-Max-Width-Forum-Message-Body-Scroll";

	public static final String FORUM_TITLE_EXTRA_STYLE = "xcure-Chat-Forum-Topic-Title";

	public static final String ATTACHED_IMAGE_PREVIEW_STYLE = "xcure-Chat-Forum-Message-Attached-Img-Preview";

	public static final String FORUM_MESSAGE_SUBJECT_DELIM_PANEL_STYLE = "xcure-Chat-ForumMessage-Subject-Panel-Delim";

	public static final String FORUM_MESSAGE_TITLE_DELIMITER_PANEL_STYLE = "xcure-Chat-ForumMessage-Title-Delimiter-Panel";

	public static final String FORUM_MESSAGE_UI_EVEN_STYLE = "xcure-Chat-ForumMessage-Widget-EVEN";

	public static final String FORUM_MESSAGE_UI_ODD_STYLE = "xcure-Chat-ForumMessage-Widget-ODD";

	public static final String FORUM_MESSAGE_BODY_CONTENT_STYLE = "xcure-Chat-ForumMessage-Content";

	public static final String FORUM_MESSAGE_TITLE_STYLE = "xcure-Chat-Forum-Message-Title";

	public static final String FORUM_MESSAGE_CLICKABLE_TITLE_STYLE = "xcure-Chat-Forum-Message-Clickable-Title";

	public static final String MESSAGE_AVATAR_TITLE_PANEL_STYLE = "xcure-Chat-Forum-Message-Title-Avatar-Panel";

	public static final String SIMPLE_FORUM_MESSAGE_SUBJECT_STYLE = "xcure-Chat-Forum-Simple-Message-Subject";

	public static final String SHARE_FORUM_MESSAGE_LINK_STYLE = "xcure-Share-Forum-Message-Link";

	public static final String SHARE_FORUM_MESSAGE_LINK_IMAGE_STYLE = "xcure-Share-Forum-Message-Link-Image";

	public static final String NEWS_PANEL_STYLE = "xcure-Chat-News-Panel";

	public static final String INFO_POPUP_STYLE_NAME = "xcure-Info-PopUpPanel";

	public static final String INFO_POPUP_PANEL_CONTENT_STYLE = "xcure-Info-PopUpPanel-Content";

	public static final String INFO_POPUP_FIELD_NAME_STYLE_NAME = USER_DIALOG_COMPULSORY_FIELD_STYLE;

	public static final String INFO_POPUP_VALUE_STYLE_NAME = CONST_FIELD_VALUE_DEFAULT_STYLE_NAME;

	public static final String INFO_POPUP_VALUE_IMP_STYLE_NAME = CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME;

	public static final String INFO_POPUP_VALUE_LINK_STYLE_NAME = DIALOG_LINK_BLUE_STYLE;

	public static final String CHAT_ROOM_POPUP_TITLE_STYLE_NAME = "xcure-ChatRoom-PopUpPanel-Title";

	public static final String CHAT_ROOM_POPUP_IMAGE_STYLE_NAME = "xcure-ChatRoom-PopUpPanel-Image";

	public static final String CHAT_ROOM_POPUP_BUTTON_STYLE_NAME = "xcure-ChatRoom-PopUpPanel-Button";

	//The style name for the panel containing images that are action we can take: send message and alike
	public static final String ACTION_IMAGE_LINKS_PANEL_STYLE = "xcure-ActionImage-Panel";

	//The style for the view full user profile link panel
	public static final String VIEW_USER_PROFILE_LINK_PANEL_STYLE = "xcure-Chat-View-Full-Profile-Link-Panel";

	public static final String TOP10_STAT_RESULTS_COMPONENT_TITLE_LABEL_STYLE = "xcure-Chat-Top10-Rating-Title-Panel";

	public static final String TITLE_IMAGE_BUTTON_STYLE = "xcure-Chat-Top10-Title-Image-Button";

	public static final String STATISTICS_SCROLL_PANEL_STYLE = "xcure-Chat-Top10-Stat-Panel";

	public static final String WIDE_STATISTICS_SCROLL_PANEL_STYLE = "xcure-Chat-Top10-Wide-Stat-Panel";

	public static final String USER_DIALOG_USER_IMAGE_STYLE = "xcure-UserDialog-UserImage";

	public static final String TOP_TEN_USER_FILE_UPLOAD_DATE_STYLE = "xcure-Top10-User-File-Upload-Date-Label";

	public static final String CHAT_ROOM_TREE_ITEM_STYLE_NAME = "xcure-ChatRoomUI-TreeItem-Room";

	public static final String CHAT_ROOM_IMAGE_STYLE_NAME = "xcure-Rooms-Tree-Image";

	public static final String CHAT_ROOM_HIDDEN_TREE_ITEM_STYLE_NAME = "xcure-ChatRoomUI-TreeItem-Hidden-User";

	public static final String CHAT_ROOM_USER_ITEM_STYLE_NAME = "xcure-ChatRoomUI-TreeItem-User";

	public static final String CHAT_ROOM_BLOCKED_USER_ITEM_STYLE_NAME = "xcure-ChatRoomUI-TreeItem-Blocked-User";

	public static final String CHAT_USER_IMAGE_STYLE_NAME = "xcure-Users-Tree-Image";

	public static final String USER_SITE_AGE_IMAGE_STYLE_NAME = "xcure-Users-Site-Age-Tree-Image";

	public static final String USER_FORUM_ACTIVITY_IMAGE_STYLE_NAME = "xcure-Users-Forum-Activity-Tree-Image";

	public static final String CHAT_ROOM_IMAGE_PLACE_HOLDER_STYLE = "xcure-Users-Tree-Image-Place-Holder";

	public static final String USER_NAME_LABEL_STYLE = "xcure-Chat-User-Status-Widget-Label";

	//This is the style name for the user status image
	public static final String USER_STATUS_IMAGE_STYLE_NAME = "xcure-Chat-User-Status-Bar-Image";

	//This is the style name for the user-status list box
	public static final String USER_STATUS_LIST_BOX_STYLE_NAME = "xcure-Chat-List-Box";

	public static final String LOCALIZATION_IMAGE_STYLE = "xcure-Chat-Locale-Image";

	public static final String LOCALE_PANEL_STYLE = "xcure-Locale-Panel-Style";

	public  static final String LOCALE_PARAMETER_NAME = "locale";

	public  static final String DISABLED_DOWNLOAD_LINK_STYLE = "xcure-UserDialog-Link-Disabled";

	public static final String QUOTE_MESSAGE_TEXT_STYLE = "xcure-Chat-Quote-Message-Text-Style";

	public static final String BASIC_MESSAGE_TEXT_STYLE = "xcure-Chat-Basic-Message-Text-Style";

	public static final String DIALOG_LINK_DISABLED_STYLE = "xcure-UserDialog-Link-Disabled";

	public static final String ACTION_LINK_IMP_STYLE = "xcure-Action-Link-Important";

	public static final String NO_TABLE_SPACING_STYLE = "xcure-Chat-No-Spacing";

	//The default image style for the avatar image
	public static final String AVATAR_IMAGE_CHOICE_STYLE = "xcure-Avatar-Image-Choice";

	public static final String AVATAR_WIDGET_IN_LIST_STYLE = "xcure-Avatar-Widget-In-List";

	public static final String CHOOSE_AVATARS_PANEL_STYLE = "xcure-Choose-Avatar-Panel";

	//The style of the entire flash player widget
	public static final String FLASH_PLAYER_WIDGET_STYLE = "xcure-Chat-Flash-Player-Widget-Style";

	//The style of the panel into which the flash player will be placed
	public static final String FLASH_PLAYER_PANEL_STYLE = "xcure-Chat-Flash-Player-Panel-Style";

	//The style for the blocking flash player panel
	public static final String FLASH_PLAYER_BLOCKED_PANEL_STYLE = "xcure-Chat-Flash-Player-Blocked-Panel-Style";

	//Additional centering of the download link for chrome
	public static final String DOWNLOAD_LINK_CHROME_CENTERING_STYLE = "xcure-Chat-Flash-Player-Widget-Download-Link-Centering-Chrome-Style";

	public static final String FORUM_MESSAGES_PANEL_STYLE = "xcure-Chat-Forum-Messages-Panel";

	public static final String NAVIGATION_BUTTON_LEFT_STYLE = "xcure-Chat-Nav-Left-Button-Panel";

	public static final String NAVIGATION_BUTTON_RIGHT_STYLE = "xcure-Chat-Nav-Right-Button-Panel";

	public static final String NAVIGATION_BUTTON_TOP_STYLE = "xcure-Chat-Nav-Top-Button-Panel";

	public static final String NAVIGATION_BUTTON_BOTTOM_STYLE = "xcure-Chat-Nav-Bottom-Button-Panel";

	public static final String NAVIGATION_BUTTON_ON_STYLE = "xcure-Chat-Nav-Button-Panel-On";

	public static final String NAVIGATION_BUTTON_OFF_STYLE = "xcure-Chat-Nav-Button-Panel-Off";

	public static final String NAVIGATION_BUTTON_IMAGE_STYLE = "xcure-Chat-Nav-Button-Panel-Image";

	public static final String FIELD_VALUE_DEFAULT_IMP_STYLE_NAME = "xcure-Chat-Nav-Button-Panel-Label";

	public static final String ENABLED_PAGE_INDEX_STYLE = "xcure-Chat-Page-Idx-Enabled";

	public static final String DISABLED_PAGE_INDEX_STYLE = "xcure-Chat-Page-Idx-Disabled";

	public static final String CURRENT_PAGE_INDEX_STYLE = "xcure-Chat-Page-Idx-Current";

	public static final String TOP_INDEX_WIDGETS_LIST_STYLE = "xcure-Chat-Top-Page-Indexes-List";

	public static final String BOTTOM_INDEX_WIDGETS_LIST_STYLE = "xcure-Chat-Bottom-Page-Indexes-List";

	public static final String PRICE_TAG_WIDGET_ENABLED_STYLE = "xcure-Chat-Price-Tag-Widget";

	public static final String PRICE_TAG_WIDGET_CLICKABLE_STYLE = "xcure-Chat-Price-Tag-Widget-Clickable";

	public static final String PRICE_TAG_WIDGET_DISABLED_STYLE = "xcure-Chat-Price-Tag-Widget-Disabled";

	public static final String LOADING_STATUS_VALUE_STYLE_NAME = "xcure-ViewMessageDialogUI-Value-Important";

	public static final String SERVER_COMMUNICATION_STATUS_PANEL_STYLE = "xcure-Chat-Server-Communication-Status-Panel";

	public static final String SMILE_PANEL_ENTRY_WIDGET_STYLE = "xcure-Chat-Smile-Selection-Dialog-Smile-Entry";

	public static final String SMILEY_LIST_SCROLL_PANEL_STYLE = "xcure-Chat-Smile-Selection-Dialog-ScrollPanel";
	
	public static final String TRANSLITERATION_IS_ON_STYLE = "xcure-Chat-TextObject-Translit-On";

	public static final String TRANSLITERATION_IS_OFF_STYLE = "xcure-Chat-TextObject-Translit-Off";

	public static final String TRANSLITERATION_IS_PLACE_HOLDER_STYLE = "xcure-Chat-TextObject-Translit-Place-Holder";

	public static final String USER_DIALOG_SUGG_TEXT_BOX_STYLE = "xcure-UserDialog-SuggestionTextBox";

	public static final String GWT_TEXT_BOX_STYLE = "gwt-TextBox";

	public static final String PROGRESS_BAR_STYLE_NAME = "xcure-UserDialog-Text-Progress-Bar";

	public static final String PROGRESS_BAR_PLACE_HOLDER_STYLE_NAME = "xcure-UserDialog-Text-Progress-Bar-Place-Holder";

	public static final String USER_AVATAR_SPOILER_IMAGE_STYLE = "xcure-Chat-Avatar-Spoiler-Image";

	public static final String USER_AVATAR_PRANK_ACTION_PANEL_STYLE = "xcure-Chat-Avatar-Spoiler-Action-Panel-Style";

	public static final String USER_AVATAR_NOPRANK_ACTION_PANEL_STYLE = "xcure-Chat-Avatar-NoSpoiler-Action-Panel-Style";

	public static final String AVATAR_IMAGE_STYLE = "xcure-Avatar-Image";

	public static final String AVATAR_IMAGE_WIDGET_STYLE = "xcure-Avatar-Image-Widget";

	//Used for chat message image preview and visualization
	public static final String ATTACHED_USER_IMAGE_THUMBNAIL_STYLE = "xcure-Chat-Message-Image-Thumbnail";

	public static final String AVATAR_PANEL_STYLE = "xcure-Chat-Avatar-Panel";

	public static final String AVATAR_PANEL_DIS_STYLE = "xcure-Chat-Avatar-Panel-Disabled";

	public static final String FILE_TUMBNAILS_PANEL_STYLE = "xcure-Chat-Profile-Files-Thumbnail-Panel";

	public static final String USER_TREASURE_WIDGET_STYLE = "xcure-Chat-User-Treasure-Widget";

	private CommonResourcesContainer() {}

}
