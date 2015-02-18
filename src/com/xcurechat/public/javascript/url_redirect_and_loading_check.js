/*
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
 */

//This method is called after a time-out and checks if the page was successfully loadeed, if not it refreshes it
function onBlockedLoadRefresh() {
    var loadingscreen = window.document.getElementById('loadingScreen');
    if( loadingscreen ) {
		//The loading screen was found
		var display = loadingscreen.style.display.toString();
		var visibility = loadingscreen.style.visibility.toString();
		if( ! display.match('none') || ! visibility.match('hidden') ) {
			//In case the loading screen is not hidden, we assume that the web-site could not load. 
			//This some times happens on Opera, for the unknown reason. In this case we reload the page.
			window.location.reload();
		}
	}
}

//Redirects users from the xcure-chat domains to the tainichok domain (this is the new one)
var hostname = window.location.hostname;
if( ! hostname.match(".*localhost.*" ) ) {
	if( hostname.match(".*xcure-chat\.com.*" ) || hostname.match(".*xcure-chat\.org.*" ) || hostname.match(".*xcure-chat\.net.*" )  ) {
		window.location = "http://www.tainichok.com/?locale=en"
	} else {
		if( hostname.match(".*xcure-chat\.ru.*" ) ) {
			window.location = "http://www.tainichok.ru/?locale=ru"
		}
	}
}

//If the site URL is OK, wait for 60 seconds for the site to load and if it is not there yet, then reload the page
setTimeout( 'onBlockedLoadRefresh()', 60000 );
