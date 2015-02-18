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
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.userstatus;

import java.util.Set;
import java.util.HashSet;

/**
 * @author zapreevis
 * This class represents possible user statuses.
 * I.e. the user is definitely online but it's status
 * specifies his mood and or the things he wants or
 * does at the moment.
 */
public enum UserStatusType {
	CHATTING(0),				//I am chatting
	DO_NOT_DISTURB(1),			//Do not disturb me
	FEEL_HAPPY(2),				//I feel happy
	FEEL_SAD(3),				//I feel sad
	FREE_FOR_CHAT(4),			//I am free for chat
	IN_LOVE(5),				//I am in love
	MUSIC_LISTENING(6),		//I am listening to a music
	NEED_A_MAN(7),				//I am looking for a man
	NEED_A_WOMAN(8),			//I am looking for a woman
	ON_FIRE(9),				//I feel horny
	TRAVELLING(10),				//I am travelling
	AWAY(11),					//I am away
	SLEEPING(12),				//I am sleeping
	ON_THE_PHONE(13),			//I am on the phone
	WATCHING_TV(14),			//I am watching TV
	WORKING(15),				//I am working
	IN_PRIVATE_SESSION(16),		//Private session
	SEARCHING(17),				//Searching
	EATING(18),					//Eating
	GAMING(19),					//Gaming
	SURFING(20),				//Surfing
	ANGRY(21),					//Angry
	FUCK_OFF(22),				//Fuck off
	ANOTHER_SITE_SECTION(23),	//In another site section
	SMOKING(24),				//Went for a smoke
	STUDYING(25),				//I am studying status
	RESTING(26);				//I am resting status
	
	//The id of the status
	private final int id;
	
	/**
	 * THe basic constructor
	 */
	private UserStatusType(final int id) {
		this.id = id;
	}
	
	/**
	 * Allows to get the unique if of the given status
	 * @return the status id
	 */
	public int getId() {
		return id;
	}
	
	//This set contains the user statuses that are equivalent to the away status but AWAY is not there!
	public static final Set<UserStatusType> AWAY_EQUIVALENT_STATUS_SET = new HashSet<UserStatusType>();
	
	static {
		AWAY_EQUIVALENT_STATUS_SET.add( SLEEPING );
		AWAY_EQUIVALENT_STATUS_SET.add( ON_THE_PHONE );
		AWAY_EQUIVALENT_STATUS_SET.add( WATCHING_TV );
		AWAY_EQUIVALENT_STATUS_SET.add( WORKING );
		AWAY_EQUIVALENT_STATUS_SET.add( STUDYING );
		AWAY_EQUIVALENT_STATUS_SET.add( GAMING );
		AWAY_EQUIVALENT_STATUS_SET.add( EATING );
		AWAY_EQUIVALENT_STATUS_SET.add( SURFING );
		AWAY_EQUIVALENT_STATUS_SET.add( TRAVELLING );
		AWAY_EQUIVALENT_STATUS_SET.add( SMOKING );
	}
}
