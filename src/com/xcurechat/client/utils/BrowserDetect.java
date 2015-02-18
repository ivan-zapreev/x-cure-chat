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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author zapreevis
 * This class contains some common GUI utils
 */
public class BrowserDetect extends JavaScriptObject {
	private static final String BROWSER_EXPLORER = "Explorer";
	private static final String BROWSER_OPERA = "Opera";
	private static final String BROWSER_CHROME = "Chrome";
	private static final String BROWSER_SAFARI = "Safari";
	private static final String BROWSER_FIREFOX = "Firefox";
	
	//Stores the mapping from the supported OS to supported browser
	//to supported versions. Stores values in lower case.
	private static final Map<String, Map<String,Set<String>>> supportedOsToBrowserToVersions = new HashMap<String, Map<String,Set<String>>>();
	//Stores the mapping from the supported OS to supported browser
	//to latest checked version. Stores values in lower case.
	private static final Map<String, Map<String,String>> supportedOsToBrowserToLatestVersion = new HashMap<String, Map<String,String>>();
	
	private static final String firefoxLatestVersion = "3.5";
	private static final String[] firefoxVersions={"3","3.0","3.5"};
	private static final String operaLatestVersion = "9.8";
	private static final String[] operaVersions={ "10.00a","10.00b1","Opera Unite","10.00b2","10.00b3","10.00",
												  "9.8","9.64","9.63","9.62","9.61","9.6","9.6b","9.52","9.51","9.5",
												  "9.5b2","9.5b1"};
	private static final String explorerLatestVersion = "8";
	private static final String[] explorerVersions={"8","8.0"};
	private static final String safariLatestVersion = "4";
	private static final String[] safariVersions={"4","4.0"};
	private static final String chromeLatestVersion = "3";
	private static final String[] chromeVersions={"2", "2.0", "3", "3.0"};
	
	/**
	 * Adds a mapping into the list of the tested OS and browsers and their version.
	 * This mappings indicate what was tested and on which OS
	 * @param _os the os string like Linux, Windows and etc
	 * @param _browser the browser name string like Chrome, Firefox and etc
	 * @param version the browser version string like 9.27 or 3
	 */
	private static void addOsToBrowserToVersionMapping( final String _os, final String _browser, final String version ) {
		//To lower case
		final String os = _os.toLowerCase();
		final String browser = _browser.toLowerCase();

		Map<String,Set<String>> supportedBrowserToVersions = supportedOsToBrowserToVersions.get( os );
		if( supportedBrowserToVersions == null ) {
			supportedBrowserToVersions = new HashMap<String, Set<String>>();
		}
		
		//Add the browser to version mapping
		Set<String> versions = supportedBrowserToVersions.get( browser );
		if( versions == null ) {
			versions = new HashSet<String>();
		}
		versions.add( version.toLowerCase() );
		
		//Put the supported versions set back
		supportedBrowserToVersions.put( browser , versions );
		//Put the updated data back
		supportedOsToBrowserToVersions.put(os, supportedBrowserToVersions);
	}
	
	/**
	 * Adds a mapping into the list of the tested OS and browsers and their versions.
	 * This mappings indicate what was tested and on which OS
	 * @param _os the os string like Linux, Windows and etc
	 * @param _browser the browser name string like Chrome, Firefox and etc
	 * @param versions the array of browser version strings like 9.27 or 3
	 * @param _latestVersion the latest tested version
	 */
	private static void addOsToBrowserToVersionMapping( final String _os, final String _browser,
														final String[] versions, final String _latestVersion ) {
		//To lower case
		final String os = _os.toLowerCase();
		final String browser = _browser.toLowerCase();
		final String latestVersion = _latestVersion.toLowerCase();
		
		for( int i = 0; i < versions.length; i++) {
			addOsToBrowserToVersionMapping( os, browser, versions[i] );
		}
		
		Map<String, String> browserToLatestVersion = supportedOsToBrowserToLatestVersion.get( os );
		if( browserToLatestVersion == null ) {
			browserToLatestVersion = new HashMap<String, String>();
		}
		browserToLatestVersion.put( browser, latestVersion );
		supportedOsToBrowserToLatestVersion.put( os, browserToLatestVersion );
	}
	
	static {
		/*Firefox*/
		addOsToBrowserToVersionMapping("Windows", BROWSER_FIREFOX, firefoxVersions, firefoxLatestVersion );
		addOsToBrowserToVersionMapping("Mac", BROWSER_FIREFOX, firefoxVersions, firefoxLatestVersion );
		addOsToBrowserToVersionMapping("Linux", BROWSER_FIREFOX, firefoxVersions, firefoxLatestVersion );
		/*Opera*/
		addOsToBrowserToVersionMapping("Windows", BROWSER_OPERA, operaVersions, operaLatestVersion );
		addOsToBrowserToVersionMapping("Mac", BROWSER_OPERA, operaVersions, operaLatestVersion );
		addOsToBrowserToVersionMapping("Linux", BROWSER_OPERA, operaVersions, operaLatestVersion );
		/*Explorer*/
		addOsToBrowserToVersionMapping("Windows", BROWSER_EXPLORER, explorerVersions, explorerLatestVersion );
		addOsToBrowserToVersionMapping("Mac", BROWSER_EXPLORER, explorerVersions, explorerLatestVersion );
		addOsToBrowserToVersionMapping("Linux", BROWSER_EXPLORER, explorerVersions, explorerLatestVersion );
		/*Safari*/
		addOsToBrowserToVersionMapping("Windows", BROWSER_SAFARI, safariVersions, safariLatestVersion );
		addOsToBrowserToVersionMapping("Mac", BROWSER_SAFARI, safariVersions, safariLatestVersion );
		addOsToBrowserToVersionMapping("Linux", BROWSER_SAFARI, safariVersions, safariLatestVersion );
		/*Chrome*/
		addOsToBrowserToVersionMapping("Windows", BROWSER_CHROME, chromeVersions, chromeLatestVersion );
		addOsToBrowserToVersionMapping("Mac", BROWSER_CHROME, chromeVersions, chromeLatestVersion );
		addOsToBrowserToVersionMapping("Linux", BROWSER_CHROME, chromeVersions, chromeLatestVersion );
	}
	
	protected BrowserDetect() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return true if the given os is supported
	 */
	public final boolean isOSSupported() {
		try{
			return ( supportedOsToBrowserToVersions.get( getOSLowCase() ) != null );
		} catch( Throwable e ) {
			return false;
		}
	}
	
	/**
	 * This method must becalled only if the current OS is supported
	 * @return true if the given browser is supported
	 */
	public final boolean isBrowserSupported() {
		try{
			return ( supportedOsToBrowserToVersions.get( getOSLowCase() ).get( getBrowserLowCase() ) != null );
		} catch( Throwable e ) {
			return false;
		}
	}
	
	/**
	 * This method must becalled only if the current OS and browser are supported
	 * @return true if the given browser version is supported
	 */
	public final boolean isBrowserVersionSupported() {
		try{
			return supportedOsToBrowserToVersions.get( getOSLowCase() ).get( getBrowserLowCase() ).contains( getVersionLowCase() );
		} catch( Throwable e ) {
			return false;
		}
	}
	
	/**
	 * @return the suggested browser version for the given platform or null if the platform is not supported
	 */
	public final String getSuggestedBrowserVersion() {
		try{
			return supportedOsToBrowserToLatestVersion.get( getOSLowCase() ).get( getBrowserLowCase() );
		} catch( Throwable e ) {
			return null;
		}
	}
	
	/**
	 * Allows to detect that the user is using Microsoft Explorer
	 * @return true if the user uses Microsoft Explorer
	 */
	public final boolean isMSExplorer() {
		return getBrowserLowCase().equals( BROWSER_EXPLORER.toLowerCase() );
	}
	
	/**
	 * Allows to detect that the user is using Safari
	 * @return true if the user uses Safari
	 */
	public final boolean isSafari() {
		return getBrowserLowCase().equals( BROWSER_SAFARI.toLowerCase() );
	}
	
	/**
	 * Allows to detect that the user is using Firefox
	 * @return true if the user uses Firefox
	 */
	public final boolean isFirefox() {
		return getBrowserLowCase().equals( BROWSER_FIREFOX.toLowerCase() );
	}
	
	/**
	 * Allows to detect that the user is using Chrome
	 * @return true if the user uses Chrome
	 */
	public final boolean isChrome() {
		return getBrowserLowCase().equals( BROWSER_CHROME.toLowerCase() );
	}
	
	/**
	 * Allows to detect that the user is using Opera
	 * @return true if the user uses Opera
	 */
	public final boolean isOpera() {
		return getBrowserLowCase().equals( BROWSER_OPERA.toLowerCase() );
	}
	
	/**
	 * @return returns the OS used by the user, not null
	 */
	public final native String getOS() /*-{
		return this.OS; 
	}-*/;
	
	/**
	 * @return returns the browser used by the user, not null
	 */
	public final native String getBrowser() /*-{
		return this.browser; 
	}-*/;
	
	/**
	 * The version field is not a string, so we get some object
	 * @return returns the browser version used by the user, not null
	 */
	private final native Object getVersionNative() /*-{
		return this.version; 
	}-*/;
	
	/**
	 * Converts the version object to string
	 * @return returns the browser version used by the user, not null
	 */
	public final String getVersion() {
		return getVersionNative().toString().trim();
	}
	
	/**
	 * @return returns the OS used by the user, not null
	 */
	private final String getOSLowCase() {
		return getOS().toLowerCase();
	}
	
	/**
	 * @return returns the browser used by the user, not null
	 */
	private final String getBrowserLowCase() {
		return getBrowser().toLowerCase();
	}
	
	/**
	 * Converst the version object to a lower case string
	 * @return returns the browser version used by the user, not null
	 */
	private final String getVersionLowCase() {
		return getVersion().toLowerCase();
	}
	
	/**
	 * Use JSNI to grab the JSON object we care about
	 * The JSON object gets its Java type implicitly
	 * based on the method's return type
	 */
	public static final native BrowserDetect getBrowserDetect() /*-{
		//Get the JSON object BrowserDetect
		return $wnd.BrowserDetect; 
	}-*/;
}
