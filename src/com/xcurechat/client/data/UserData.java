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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.i18n.UIErrorMessages;
import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.exceptions.IncorrectUserDataException;
import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * The client-side user-data object. Contains non-private user data
 * 
 * For more details on the Cyrillic (+ extensions) URF-8 (HEX) codes see
 * http://tlt.its.psu.edu/suggestions/international/bylanguage/cyrchart.html
 */
public class UserData extends ShortUserData {
	//The undefined user profile types
	public static final short UNKNOWN_USER_TYPE = -1;
	//This is an administrator's profile type
	public static final short ADMIN_USER_TYPE = 0;
	//This is a regular user profile type
	public static final short SIMPLE_USER_TYPE = 1;
	//There should be just one user with this profile type
	public static final short DELETED_USER_TYPE = 2;
	
	//The maximum width and height of the user profile image in pixels
	public static final int MAX_USER_PROFILE_IMAGE_PIXEL_WIDTH = 1024;
	public static final int MAX_USER_PROFILE_IMAGE_PIXEL_HEIGHT = 768;
	
	//The maximum number of the profile files
	public static final int MAXIMUM_NUMBER_OF_FILES = 20;

	//Any english or any cyrillic letter (plus some cyrillic extensions)
	private static final String ANY_LETTER = "[(\u0020)a-zA-Z\u0400-\u045F\u048A-\u04F9]";
	//private static final String ANY_LATIN_LETTER = "[(\u0020)a-zA-Z]";
	
	//The max/min lengths of user login
	public static final int MIN_LOGIN_LENGTH = 4;
	public static final int MAX_LOGIN_LENGTH = 20;
	//Format for the userName
	public static final String USER_LOGIN_PATTERN = "(" + ANY_LETTER + "|[0-9]){"+MIN_LOGIN_LENGTH+","+MAX_LOGIN_LENGTH+"}";
	//Format for the userName
	public static final String USER_REG_LOGIN_PATTERN = "(" + ANY_LETTER + "|[0-9]){"+MIN_LOGIN_LENGTH+","+MAX_LOGIN_LENGTH+"}";

	//The max/min lengths of user login
	public static final int MIN_PASSWORD_LENGTH = 4;
	public static final int MAX_PASSWORD_LENGTH = 10;
	//Format for the userPassword
	//WARNING: Opera 10.1 does not work with this pattern matching, it does not allow for digits! 
	//public static final String USER_PASSWORD_PATTERN = ".{"+MIN_PASSWORD_LENGTH+","+MAX_PASSWORD_LENGTH+"}";
	
	//The max/min lengths of optional fields
	public static final int MIN_OPTIONAL_LENGTH = 0;
	public static final int MAX_OPTIONAL_LENGTH = 30;
	//The pattern for the optional data field
	public static final String OPTIONAL_FIELDS_PATTERN = "(" + ANY_LETTER + "|[0-9]){"+MIN_OPTIONAL_LENGTH+","+MAX_OPTIONAL_LENGTH+"}";
	
	//The maximum length of the about me field
	public static final int MAX_ABOUT_ME_LENGTH = 1024;
	
	//If true then the given user is a bot
	private boolean isBot;
	//Indicates if the user is a friend of the main user
	private boolean isFriend;
	//User's age
	private Integer userAge;
	//First name
	private String firstName;
	//Last name
	private String lastName;
	//Country name
	private String countryName;
	//City name
	private String cityName;
	//The about me description provided by the user
	private String aboutMe;
	//The list with the user profile image descriptors
	private HashMap<Integer, ShortFileDescriptor> userProfileFiles = new LinkedHashMap<Integer, ShortFileDescriptor>( );
	//The logged in user type
	private int userProfileType = SIMPLE_USER_TYPE;

	//Various age categories
	public static final String AGE_UNKNOWN_STR = "???";
	public static final String AGE_UNDER_TO_18_STR = "< 18";
	public static final String AGE_18_TO_21_STR = "18--21";
	public static final String AGE_21_TO_25_STR = "21--25";
	public static final String AGE_25_TO_30_STR = "25--30";
	public static final String AGE_30_TO_35_STR = "30--35";
	public static final String AGE_35_TO_45_STR = "35--45";
	public static final String AGE_45_TO_55_STR = "45--55";
	public static final String AGE_ABOVE_55_STR = "> 55";

	public static final Integer AGE_UNKNOWN = new Integer(-1);
	public static final Integer AGE_UNDER_TO_18 = new Integer(0);
	public static final Integer AGE_18_TO_21 = new Integer(18);
	public static final Integer AGE_21_TO_25 = new Integer(21);
	public static final Integer AGE_25_TO_30 = new Integer(25);
	public static final Integer AGE_30_TO_35 = new Integer(30);
	public static final Integer AGE_35_TO_45 = new Integer(35);
	public static final Integer AGE_45_TO_55 = new Integer(45);
	public static final Integer AGE_ABOVE_55 = new Integer(55);
	
	//Age string to integer Mapping
	private static final HashMap<String,Integer> ageMappingTable = new HashMap<String,Integer>();
	
	static {
		ageMappingTable.put( AGE_UNKNOWN_STR, AGE_UNKNOWN );
		ageMappingTable.put( AGE_UNDER_TO_18_STR, AGE_UNDER_TO_18 );
		ageMappingTable.put( AGE_18_TO_21_STR, AGE_18_TO_21 );
		ageMappingTable.put( AGE_21_TO_25_STR, AGE_21_TO_25 );
		ageMappingTable.put( AGE_25_TO_30_STR, AGE_25_TO_30 );
		ageMappingTable.put( AGE_30_TO_35_STR, AGE_30_TO_35 );
		ageMappingTable.put( AGE_35_TO_45_STR, AGE_35_TO_45 );
		ageMappingTable.put( AGE_45_TO_55_STR, AGE_45_TO_55 );
		ageMappingTable.put( AGE_ABOVE_55_STR, AGE_ABOVE_55 );
	}
	
	/**
	 * Allows to get the number of use files in the profile
	 * @return the number of user-profile files
	 */
	public int getNumberOfFiles() {
		return userProfileFiles.size();
	}
	
	/**
	 * Allows to get the user profile file descriptors. 
	 * @return the current ordered hash map with the user profile file descriptors.
	 */
	public HashMap<Integer, ShortFileDescriptor> getFileDescriptors() {
		return userProfileFiles;
	}
	
	/**
	 * Allows to get the copy of the current ordered hash map with the user profile file descriptors. 
	 * @return the current ordered hash map with the user profile file descriptors.
	 */
	public HashMap<Integer, ShortFileDescriptor> cloneFileDescriptors() {
		return new LinkedHashMap<Integer, ShortFileDescriptor>( userProfileFiles );
	}
	
	/**
	 * Allows to a user file descriptor for a specified file index
	 * @param fileID the user file ID
	 * @return the descriptor, if there is no file with such index, then it returns null
	 */
	public ShortFileDescriptor getUserProfileFileDescr( final int fileID ) {
		return userProfileFiles.get( fileID );
	}
	
	/**
	 * Allows to remove the user-profile file with the given fileID.
	 * @param fileID the fileID of the file attached to the user profile
	 */
	public void removeUserProfileFileDescr( final int fileID ) {
		userProfileFiles.remove( fileID );
	}
	
	/**
	 * Allows to add a new profile-file descriptor to the user profile
	 * @param fileDescr the file descriptor with the properly set fileID
	 * @throws Exception 
	 */
	public void addUserProfileFileDescr( final ShortFileDescriptor fileDescr ) {
		if( fileDescr.fileID != ShortFileDescriptor.UNKNOWN_FILE_ID ) {
			userProfileFiles.put( fileDescr.fileID, fileDescr );
		} else {
			throw new IndexOutOfBoundsException("Undefined user-profile file id: " + fileDescr.fileID );
		}
	}
	
	/**
	 * Copies the user-visible data from the provided object to this one.
	 * Note that we only copy the data that can be changed. I.e. the user
	 * login name, the user ID and user registration date are not copied.
	 * We also do not copy any system information, such as is in MainUserData.
	 * Also we do not copy the user-images descriptors, they must be handled separately.
	 * @param another the user data object to copy information from
	 */
	public void setUserManagedData( UserData another ) {
		this.setMale( another.isMale() );
		this.setUserAge( another.getUserAge() );
		this.setFirstName( another.getFirstName() );
		this.setLastName( another.getLastName() );
		this.setCityName( another.getCityName() );
		this.setCountryName( another.getCountryName() );
		this.setAboutMe( another.getAboutMe() );
		this.setUserStatus( another.getUserStatus() );
		//NOTE: Here we must not copy the fields that
		//are not editable by the user i.e. the online
		//status the registration date, the last online date
	}
	
	/**
	 * @return the user age string label or an empty string
	 * if the age int value has no string representation.
	 */
	public String getAgeString() {
		String ageStr = "";
		Iterator<String> iter = ageMappingTable.keySet().iterator();
		while( iter.hasNext() ) {
			String tmpStr = iter.next();
			if( ( (Integer) ageMappingTable.get( tmpStr ) ).intValue() == userAge.intValue() ){
				ageStr = tmpStr;
				break;
			}
		}
		return ageStr;
	}

	/**
	 * The default constructor, is needed for serialization
	 */
	public UserData(){
		super();
		this.isFriend = false;
		this.firstName = "";
		this.lastName = "";
		this.cityName = "";
		this.countryName = "";
		this.aboutMe = "";
		this.userAge = new Integer(0);
		this.isBot = false;
	}
	
	/**
	 * Allows to detect if the given user is a bot
	 * @return true if the user is a bot
	 */
	public boolean isBot() {
		return this.isBot;
	}
	
	/**
	 * Allows to set the given user to be a bot or a normal user
	 * @param isBot true for a bot, otherwise false
	 */
	public void setBot( final boolean isBot ) {
		this.isBot = isBot;
	}
	
	/**
	 * The friend relationship is always related to the MainUserData
	 * object for which we retrieved this UserData object. 
	 * @return true if the user is known to be a friend.
	 */
	public boolean isFriend(){
		return this.isFriend;
	}
	
	/**
	 * @param isFriend true if the user is a friend
	 */
	public void setFriend( final boolean isFriend){
		this.isFriend = isFriend;
	}
	
	public Integer getUserAge(){
		return this.userAge;
	}
	
	public static Integer getAgeFromString( String userAgeStr ){
		return ageMappingTable.get( userAgeStr );
	}
	
	public static Integer stringToUserAge( final String userAgeStr ) {
		Integer ageVal = ageMappingTable.get( userAgeStr );
		if( ageVal != null ){
			return ageVal;
		} else {
			return new Integer( AGE_UNKNOWN );
		}
	}
	
	public void setUserAge( final String userAgeStr ){
		this.userAge = stringToUserAge( userAgeStr );
	}
	
	public void setUserAge( final Integer userAge ){
		if( ageMappingTable.values().contains( userAge ) ){
			this.userAge = userAge;
		} else {
			this.userAge = new Integer(AGE_UNKNOWN);
		}
	}
	
	public String getFirstName() {
		return this.firstName;
	}
	
	public void setFirstName( final String firstName ) {
		this.firstName = firstName.trim();
	}
	
	public String getLastName() {
		return this.lastName;
	}
	
	public void setLastName( final String lastName ) {
		this.lastName = lastName.trim();
	}

	public String getCountryName() {
		return this.countryName;
	}
	
	public void setCountryName( final String countryName ) {
		this.countryName = countryName.trim();
	}

	public String getCityName() {
		return this.cityName;
	}
	
	public void setCityName( final String cityName ) {
		this.cityName = cityName.trim();
	}
	
	public String getAboutMe() {
		return this.aboutMe;
	}
	
	public void setAboutMe( final String aboutMe ) {
		this.aboutMe = aboutMe;
	}
	
	/**
	 * Validates the user data, before sending it to the server
	 * @param password the user password
	 * @param isOld true is it is a message about the old password
	 */
	public static void validatePassword( String password, boolean isOld ) throws SiteException {
		UIErrorMessages errorsI18N = I18NManager.getErrors();
		if( password.isEmpty() ){
			if( isOld ) {
				throw new IncorrectUserDataException( errorsI18N.emptyOldUserPasswordError() );
			} else {
				throw new IncorrectUserDataException( errorsI18N.emptyUserPasswordError() );
			}
		}
	}
	
	/**
	 * Validates the updated user data, before sending it to the server
	 * @param newPassword the new password
	 * @param newPasswordRep the repetition of the new password
	 */
	public static void validatePasswords( final String newPassword, final String newPasswordRep ) throws SiteException {
		UIErrorMessages errorsI18N = I18NManager.getErrors();
		
		if( ( newPassword.length() < MIN_PASSWORD_LENGTH ) || ( newPassword.length() > MAX_PASSWORD_LENGTH ) ){
			throw new IncorrectUserDataException( errorsI18N.newPasswordFormatError( UserData.MIN_PASSWORD_LENGTH, UserData.MAX_PASSWORD_LENGTH) );
		}else{
			if(!newPasswordRep.equals( newPassword )){
				throw new IncorrectUserDataException( errorsI18N.newPasswordRepNotEqualError() );
			}
		}
	}
	
	/**
	 * This method validates user data object, it is meant to be
	 * used before sending the data to the server. Also every client
	 * can run this method, to validate 
	 */
	public void validate(final boolean isRegistration) throws SiteException {
		IncorrectUserDataException exception = new IncorrectUserDataException();
		UIErrorMessages errorsI18N = I18NManager.getErrors();
		UITitlesI18N titlesI18N = I18NManager.getTitles();
		
		if( !userName.trim().isEmpty() ){
			if( isRegistration ) {
				if( !userName.matches( USER_REG_LOGIN_PATTERN ) ) {
					exception.addErrorMessage(errorsI18N.incorrectUserNameFormat(MIN_LOGIN_LENGTH, MAX_LOGIN_LENGTH));
				}
			} else {
				if( !userName.matches( USER_LOGIN_PATTERN ) ) {
					exception.addErrorMessage(errorsI18N.incorrectUserNameFormat(MIN_LOGIN_LENGTH, MAX_LOGIN_LENGTH));
				}
			}
		} else {
			exception.addErrorMessage(errorsI18N.incorrectUserNameFormat(MIN_LOGIN_LENGTH, MAX_LOGIN_LENGTH));
		}
		
		if( !firstName.matches(OPTIONAL_FIELDS_PATTERN) ){
			exception.addErrorMessage( errorsI18N.incorrectTextFieldFormat( titlesI18N.firstNameField(), MAX_OPTIONAL_LENGTH ) );
		}
		
		if( !lastName.matches(OPTIONAL_FIELDS_PATTERN) ){
			exception.addErrorMessage( errorsI18N.incorrectTextFieldFormat( titlesI18N.lastNameField(), MAX_OPTIONAL_LENGTH ) );
		}
		
		if( !cityName.matches(OPTIONAL_FIELDS_PATTERN) ){
			exception.addErrorMessage( errorsI18N.incorrectTextFieldFormat( titlesI18N.cityField(), MAX_OPTIONAL_LENGTH ) );
		}
		
		if( !countryName.matches(OPTIONAL_FIELDS_PATTERN) ){
			exception.addErrorMessage( errorsI18N.incorrectTextFieldFormat( titlesI18N.countryField(), MAX_OPTIONAL_LENGTH ) );
		}
		
		if( aboutMe.length() > MAX_ABOUT_ME_LENGTH ) {
			exception.addErrorMessage( errorsI18N.incorrectTextFieldLength( titlesI18N.aboutMeUserProfilePanel(), MAX_ABOUT_ME_LENGTH ) );
		}
		
		//Check if there are error, if yes, then throw an exception
		if( exception.containsErrors() ){
			throw exception;
		}
	}
	
	/**
	 * @param userProfileType the user account type
	 */
	public void setUserProfileType(final int userProfileType){
		this.userProfileType = userProfileType;
	}

	/**
	 * @return the user account type
	 */
	public int getUserProfileType(){
		return this.userProfileType;
	}
	
	/**
	 * @return true if the user is admin
	 */
	public boolean isAdmin(){
		return ( this.userProfileType == ADMIN_USER_TYPE ); 
	}
}
