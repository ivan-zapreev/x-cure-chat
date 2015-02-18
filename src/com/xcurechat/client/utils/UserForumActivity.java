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
 * The file processing package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.rpc.ServerSideAccessManager;

public enum UserForumActivity implements IsSerializable {
	LEVEL_0(0, SiteForumActivityHelper.SENT_UP_TO_025_FORUM_MESSAGES ),
	LEVEL_1(1, SiteForumActivityHelper.SENT_UP_TO_100_FORUM_MESSAGES ),
	LEVEL_2(2, SiteForumActivityHelper.SENT_UP_TO_200_FORUM_MESSAGES ),
	LEVEL_3(3, SiteForumActivityHelper.SENT_UP_TO_600_CHAT_MESSAGES ),
	LEVEL_4(4, SiteForumActivityHelper.INFTY_FORUM_MESSAGES );
	
	public static final String USER_FORUM_ACTIVITY_IMAGES_LOCATION_FOLDER = "forum/";
	private final String forumActivityImageURL;
	private final int rangeEnd;
	
	private UserForumActivity( final int forumActivityImageIndex, final int rangeEnd ){
		this.forumActivityImageURL = ServerSideAccessManager.USER_STATUS_IMAGES_BASE_URL + USER_FORUM_ACTIVITY_IMAGES_LOCATION_FOLDER + forumActivityImageIndex + ".png";
		this.rangeEnd = rangeEnd; 
	}
	
	public String getForumActivityImageURL() {
		return  forumActivityImageURL;
	}
	
	public int getRangeEnd() {
		return rangeEnd;
	}
	
	public String getForumActivityImageTooltip( final UITitlesI18N i18nTitles, final int msgCount ) {
		if( rangeEnd != SiteForumActivityHelper.INFTY_FORUM_MESSAGES ) {
			return i18nTitles.hasForumMessages(msgCount, rangeEnd);
		} else {
			return i18nTitles.hasForumMessages(msgCount);
		}
	}
	
	/**
	 * @return true if to post to the forum, user needs to pass a CAPTCHA test 
	 */
	public static final boolean isCaptchaNeeded( UserForumActivity userActivitylevel ) {
		return ( userActivitylevel.compareTo( UserForumActivity.LEVEL_1 ) <= 0 );
	}
	
	/**
	 * @author zapreevis
	 * This is a helper class for the user forum activity 
	 */
	public static class SiteForumActivityHelper {
		//The user age statuses and periods
		public static final int SENT_UP_TO_025_FORUM_MESSAGES = 25;
		public static final int SENT_UP_TO_100_FORUM_MESSAGES = 4 * SENT_UP_TO_025_FORUM_MESSAGES;
		public static final int SENT_UP_TO_200_FORUM_MESSAGES = 2 * SENT_UP_TO_100_FORUM_MESSAGES;
		public static final int SENT_UP_TO_600_CHAT_MESSAGES = 3 * SENT_UP_TO_200_FORUM_MESSAGES;
		public static final int INFTY_FORUM_MESSAGES = -1;
		
		public static UserForumActivity getSiteUserForumActivityBySentMsgs( final int numSentMsgs ) {
			if( numSentMsgs < SENT_UP_TO_025_FORUM_MESSAGES ) {
				return LEVEL_0;
			} else {
				if( numSentMsgs < SENT_UP_TO_100_FORUM_MESSAGES ) {
					return LEVEL_1;
				} else {
					if( numSentMsgs < SENT_UP_TO_200_FORUM_MESSAGES ) {
						return LEVEL_2;
					} else {
						if( numSentMsgs < SENT_UP_TO_600_CHAT_MESSAGES ) {
							return LEVEL_3;
						} else {
							return LEVEL_4;
						}
					}
				}
			}
		}
	}
}
