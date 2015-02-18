-- Create a new database
CREATE DATABASE XCURE_CHAT_DB;

-- Switch to the database
USE XCURE_CHAT_DB;

-- Create users-table
CREATE TABLE users (
	uid INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'User id',
	login VARCHAR(20) CHARSET utf8 NOT NULL UNIQUE COMMENT 'User login',
	uni_login VARCHAR(20) CHARSET utf8 NOT NULL UNIQUE COMMENT 'The unified user login',
	password_hash VARCHAR(120) CHARSET utf8 NOT NULL COMMENT 'Hash of the user password',
	gender BOOLEAN DEFAULT TRUE COMMENT 'True if the user is male, otherwise false',
	type INTEGER UNSIGNED DEFAULT 1 COMMENT 'Defined the user profile type and his access rights',
	gold_pieces INTEGER UNSIGNED DEFAULT 0 COMMENT 'Indicates the amount of gold pieces the user has',
	age INTEGER DEFAULT -1 COMMENT 'Users age',
	first_name VARCHAR(30) CHARSET utf8 DEFAULT '' COMMENT 'User first name',
	last_name VARCHAR(30) CHARSET utf8 DEFAULT '' COMMENT 'User last name',
	country VARCHAR(30) CHARSET utf8 DEFAULT '' COMMENT 'User home country',
	city VARCHAR(30) CHARSET utf8 DEFAULT '' COMMENT 'User home city',
	aboutMe VARCHAR(1024) CHARSET utf8 DEFAULT '' COMMENT 'A user description about himself',
	reg_date DATETIME NOT NULL COMMENT 'The date-time when the user registered',
	is_online BOOLEAN DEFAULT FALSE COMMENT 'True if this user is currently online',
	is_bot BOOLEAN DEFAULT FALSE COMMENT 'True if this user is a bot',
	last_online DATETIME COMMENT 'The last time user was online (logged out)',
	spoiler_id INTEGER UNSIGNED DEFAULT 0 COMMENT 'The id of the spoiler used for the avatar, zero for no spoiler',
	spoiler_exp_date DATETIME DEFAULT NULL COMMENT 'The date-time when the user-avatar spoiler expires, can be null if no spoiler is set',
    num_forum_posts INTEGER UNSIGNED DEFAULT 0 COMMENT 'The number of forum messages',
    num_chat_msgs INTEGER UNSIGNED DEFAULT 0 COMMENT 'The number of sent chat messages',
    time_on_site BIGINT UNSIGNED DEFAULT 0 COMMENT 'The time spent on site in milliseconds',
	INDEX( uid, login, uni_login, first_name, last_name, country, city, aboutMe, age, gender, is_online, last_online, gold_pieces, num_forum_posts, num_chat_msgs, time_on_site )
) ENGINE = INNODB;

-- Create profile files table
CREATE TABLE profile_files (
	fileID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The id of the file',
	uid INTEGER UNSIGNED NOT NULL COMMENT 'User id',
	FOREIGN KEY (uid) REFERENCES users (uid) ON DELETE CASCADE,
	mimeType VARCHAR(256) CHARSET utf8 NOT NULL COMMENT 'The MIME type of the file',
	fileName VARCHAR(128) CHARSET utf8 NOT NULL COMMENT 'The uploaded file name',
	thumbnail BLOB COMMENT 'File preview thumbnail',
	data LONGBLOB NOT NULL COMMENT 'The data of the uploaded file',
	widthPixels INTEGER UNSIGNED DEFAULT 0 COMMENT 'The width of the data file, if applickable, not the thimbnail',
	heightPixels INTEGER UNSIGNED DEFAULT 0 COMMENT 'The height of the data file, if applickable, not the thimbnail',
	uploadDate DATETIME NOT NULL COMMENT 'The date-time when the file was uploaded',
	INDEX( uid, fileID )
) ENGINE = INNODB;

-- Create user avatar image table
CREATE TABLE avatar_images (
	uid INTEGER UNSIGNED NOT NULL UNIQUE COMMENT 'User id',
	FOREIGN KEY (uid) REFERENCES users (uid) ON DELETE CASCADE,
	image MEDIUMBLOB NOT NULL COMMENT 'Avatar image itself',
	mimeType VARCHAR(256) CHARSET utf8 NOT NULL COMMENT 'The MIME type of the file',
	INDEX( uid )
) ENGINE = INNODB;

-- Create log in/out statistics table 
CREATE TABLE login_out_statistics (
	uid INTEGER UNSIGNED NOT NULL COMMENT 'User id',
	FOREIGN KEY (uid) REFERENCES users (uid) ON DELETE CASCADE,
	is_login BOOLEAN COMMENT 'True if the user logged in, otherwise false',
	is_auto BOOLEAN COMMENT 'True if it was an automatic logout, otherwise false',
	date DATETIME NOT NULL COMMENT 'The time login/logout occured',
	host VARCHAR(128) CHARSET utf8 COMMENT 'The host machine from which login/logout occured',
	location TEXT CHARSET utf8 COMMENT 'The geolocation for the given host',
	INDEX( uid, date )
) ENGINE = INNODB;

-- Create chat rooms table
CREATE TABLE rooms (
	rid INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Room id',
	name VARCHAR(30) CHARSET utf8 DEFAULT 'Unknown' COMMENT 'The name of the room',
	description VARCHAR(128) CHARSET utf8 COMMENT 'The description of the room',
	uid INTEGER UNSIGNED NOT NULL COMMENT 'User id',
	FOREIGN KEY (uid) REFERENCES users (uid) ON DELETE CASCADE,
	login VARCHAR(20) CHARSET utf8 NOT NULL COMMENT 'User login',
	is_permanent BOOLEAN DEFAULT TRUE COMMENT 'True if this is a permanent room, otherwise false',
	is_main BOOLEAN DEFAULT FALSE COMMENT 'True if this is the main room, otherwise false',
	expires DATETIME COMMENT 'The date-time when the room closes',
	type INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'This is the room type, 0 stand for public',
	visitors INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'The number of users residing in the room',
	INDEX( rid, is_permanent, is_main, expires, visitors )
) ENGINE = INNODB;

-- Create rooms access table
CREATE TABLE rooms_access (
	raid INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The room access entry id',
	rid INTEGER UNSIGNED NOT NULL COMMENT 'Room id',
	FOREIGN KEY (rid) REFERENCES rooms (rid) ON DELETE CASCADE,
	uid INTEGER UNSIGNED NOT NULL COMMENT 'User id',
	FOREIGN KEY (uid) REFERENCES users (uid) ON DELETE CASCADE,
	login VARCHAR(20) CHARSET utf8 NOT NULL COMMENT 'User login',
	is_system BOOLEAN DEFAULT FALSE COMMENT 'True if this access right can be only visible by admin, otherwise false',
	is_read BOOLEAN DEFAULT TRUE COMMENT 'True if the user can read from the room, otherwise false',
	is_read_all BOOLEAN DEFAULT FALSE COMMENT 'True if the user can read all private messages from the room, otherwise false',
	read_all_expires DATETIME COMMENT 'The date-time when the read all access to the room expires',
	is_write BOOLEAN DEFAULT TRUE COMMENT 'True if the user can write to the room, otherwise false',
	INDEX( raid, rid, uid )
) ENGINE = INNODB;

-- Create friends table
CREATE TABLE friends (
	uid_from INTEGER UNSIGNED NOT NULL COMMENT 'User id of a person which has a friend',
	FOREIGN KEY (uid_from) REFERENCES users (uid) ON DELETE CASCADE,
	uid_to INTEGER UNSIGNED NOT NULL COMMENT 'User id of the person whi is the friend',
	FOREIGN KEY (uid_to) REFERENCES users (uid) ON DELETE CASCADE,
	INDEX( uid_from, uid_to )
) ENGINE = INNODB;

-- Create private messages table
CREATE TABLE private_messages (
	mid INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The message entry id',
	uid_from INTEGER UNSIGNED NOT NULL COMMENT 'From user id',
	FOREIGN KEY (uid_from) REFERENCES users (uid) ON DELETE CASCADE,
	uid_to INTEGER UNSIGNED NOT NULL COMMENT 'To user id',
	FOREIGN KEY (uid_to) REFERENCES users (uid) ON DELETE CASCADE,
	from_hidden BOOLEAN DEFAULT FALSE COMMENT 'True if the user who sent the message can not see it, i.e. deleted it',
	to_hidden BOOLEAN DEFAULT FALSE COMMENT 'True if the user who received the message can not see it, i.e. deleted it',
	is_read BOOLEAN DEFAULT FALSE COMMENT 'True if the message is yet unread by the recepient',
	sent_date_time DATETIME NOT NULL COMMENT 'The date/time when the message was sent',
	msg_type INTEGER UNSIGNED DEFAULT 0 COMMENT '0 stands for a regular personal message',
	msg_title VARCHAR(128) CHARSET utf8 DEFAULT '' COMMENT 'The message title',
	msg_body VARCHAR(2048) CHARSET utf8 DEFAULT '' COMMENT 'The message budy',
	room_id INTEGER UNSIGNED COMMENT 'Room id',
	FOREIGN KEY (room_id) REFERENCES rooms (rid) ON DELETE CASCADE,
	INDEX( mid, uid_from, uid_to, from_hidden, to_hidden, sent_date_time )
) ENGINE = INNODB;

-- Create chat message files table
CREATE TABLE chat_files (
	fileID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The id of the file',
	ownerID INTEGER UNSIGNED NOT NULL COMMENT 'The id of the user who uploaded the image',
	FOREIGN KEY (ownerID) REFERENCES users (uid) ON DELETE CASCADE,
	uploadDate DATETIME NOT NULL COMMENT 'The date-time when the file was uploaded',
	mimeType VARCHAR(256) CHARSET utf8 NOT NULL COMMENT 'The MIME type of the file',
	fileName VARCHAR(128) CHARSET utf8 NOT NULL COMMENT 'The uploaded file name',
	thumbnail BLOB COMMENT 'File preview thumbnail',
	data LONGBLOB NOT NULL COMMENT 'The data of the uploaded file',
	widthPixels INTEGER UNSIGNED DEFAULT 0 COMMENT 'The width of the image file, not the thimbnail',
	heightPixels INTEGER UNSIGNED DEFAULT 0 COMMENT 'The height of the image file, not the thimbnail',
	is_public BOOLEAN DEFAULT FALSE COMMENT 'True if this chat files is attached to a public chat message',
	md5 CHAR(32) CHARSET utf8 DEFAULT '' COMMENT 'The MD5 sum for the file data',
	INDEX( fileID, ownerID, uploadDate )
) ENGINE = INNODB;

-- Create the table storing the chat messages
-- WARNING: ENGINE = MEMORY DOES NOT SUPPORT FOREING KEY CONSTRAINTS 
CREATE TABLE chat_messages (
	messageID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The chat message id',
	sentDate DATETIME NOT NULL COMMENT 'The date-time when the message was sent',
	senderID INTEGER UNSIGNED NOT NULL COMMENT 'The message was sent by the user with this id',
	FOREIGN KEY (senderID) REFERENCES users (uid) ON DELETE CASCADE,
	roomID INTEGER UNSIGNED NOT NULL COMMENT 'The id of the room this message was sent in',
	FOREIGN KEY (roomID) REFERENCES rooms (rid) ON DELETE CASCADE,
	--NOTE: We must not have fileID as a foreing key here, because of storing the files for being re-used by the bot
	fileID INTEGER UNSIGNED DEFAULT 0 COMMENT 'The id of the file associated with this message or null if no file',
	messageBody VARCHAR(1024) CHARSET utf8 DEFAULT '' COMMENT 'The message text',
	messageType INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'This is the message type, 0 stand for simple message',
	infoUserID INTEGER UNSIGNED DEFAULT 0 COMMENT 'The id of the user, in case this about a user event',
	FOREIGN KEY (roomID) REFERENCES rooms (rid) ON DELETE CASCADE,
	infoUserLogin VARCHAR(20) CHARSET utf8 DEFAULT '' COMMENT 'The login of the user, in case this about a user event',
	fontType INTEGER UNSIGNED DEFAULT 0 COMMENT 'The font type to be used when visualizing this message',
	fontSize INTEGER UNSIGNED DEFAULT 2 COMMENT 'The font size to be used when visualizing this message',
	fontColor INTEGER UNSIGNED DEFAULT 0 COMMENT 'The font color to be used when visualizing this message',
	INDEX( messageID, senderID, roomID, messageType, sentDate )
) ENGINE = MEMORY;

-- Create the table storing the mapping from the private chat message ID to the recepient ID
-- WARNING: ENGINE = MEMORY DOES NOT SUPPORT FOREING KEY CONSTRAINTS 
CREATE TABLE chat_msg_recepient (
	cmrID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The chat message recipient entry id',
	sentDate DATETIME NOT NULL COMMENT 'The date-time when the message-recepient was added',
	messageID INTEGER UNSIGNED NOT NULL COMMENT 'The chat message id',
	FOREIGN KEY (messageID) REFERENCES chat_messages (messageID) ON DELETE CASCADE,
	recepientID INTEGER UNSIGNED NOT NULL COMMENT 'The private chat message recepient id',
	FOREIGN KEY (recepientID) REFERENCES users (uid) ON DELETE CASCADE,
	INDEX( cmrID, messageID, recepientID, sentDate )
) ENGINE = MEMORY;

-- Create the forum messages table
CREATE TABLE forum_messages (
	messageID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The forum message id',
	messagePathID VARCHAR(50000) NOT NULL DEFAULT "" COMMENT 'The path to the parent node of this node starting from the root, it is node ids on the path suffixed with dots, like: 1.23.54.60.',
	parentMessageID INTEGER UNSIGNED DEFAULT 0 COMMENT 'The id of the forum message that is a parent of this message',
	FOREIGN KEY (parentMessageID) REFERENCES forum_messages (messageID) ON DELETE CASCADE,
	sentDate DATETIME NOT NULL COMMENT 'The date-time when the message was sent',
	updateDate DATETIME NOT NULL COMMENT 'The date-time when the message was updated',
	senderID INTEGER UNSIGNED NOT NULL COMMENT 'The message was sent by the user with this id',
	FOREIGN KEY (senderID) REFERENCES users (uid) ON DELETE CASCADE,
	messageTitle VARCHAR(256) CHARSET utf8 DEFAULT '' COMMENT 'The message title',
	messageBody TEXT CHARSET utf8 DEFAULT '' COMMENT 'The message body',
	is_system BOOLEAN DEFAULT FALSE COMMENT 'The primary node of the forum is marked as true, because it should not be visible',
	is_approved BOOLEAN DEFAULT FALSE COMMENT 'True if the forum post was approved to be a news',
	numVotes INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'The number of votes for this post',
	voteValue INTEGER NOT NULL DEFAULT 0 COMMENT 'The current vote value',
	numReplies INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'The number of message replies',
	stLastSenderID INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'The id of the user who made the last post in the tree rooted to this node',
	stLastPostDate DATETIME NOT NULL COMMENT 'The date of the last post in the tree rooted to this node',
	INDEX( messageID, sentDate, senderID, parentMessageID, messageTitle, messageBody, is_system, updateDate, messagePathID, is_approved, stLastPostDate )
) ENGINE = INNODB;

-- Create the forum votes table
CREATE TABLE forum_message_votes (
	messageID INTEGER UNSIGNED NOT NULL COMMENT 'The id of the forum message to which this vote was done',
	FOREIGN KEY (messageID) REFERENCES forum_messages (messageID) ON DELETE CASCADE,
	senderID INTEGER UNSIGNED NOT NULL COMMENT 'The vote was done by the user with this id',
	FOREIGN KEY (senderID) REFERENCES users (uid) ON DELETE CASCADE,
	voteValue INTEGER NOT NULL DEFAULT 0 COMMENT 'The vote made by this user'
) ENGINE = INNODB;

-- Create forum files table
CREATE TABLE forum_files (
	fileID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The id of the file',
	messageID INTEGER UNSIGNED DEFAULT 0 COMMENT 'The id of the forum message to which this file blongs',
	FOREIGN KEY (messageID) REFERENCES forum_messages (messageID) ON DELETE CASCADE,
	ownerID INTEGER UNSIGNED NOT NULL COMMENT 'The id of the user who uploaded the file',
	FOREIGN KEY (ownerID) REFERENCES users (uid) ON DELETE CASCADE,
	uploadDate DATETIME NOT NULL COMMENT 'The date-time when the file was uploaded',
	mimeType VARCHAR(256) CHARSET utf8 NOT NULL COMMENT 'The MIME type of the file',
	fileName VARCHAR(128) CHARSET utf8 NOT NULL COMMENT 'The uploaded file name',
	thumbnail BLOB COMMENT 'File preview thumbnail',
	data LONGBLOB NOT NULL COMMENT 'The data of the uploaded file',
	widthPixels INTEGER UNSIGNED DEFAULT 0 COMMENT 'The width of the image file, not the thimbnail',
	heightPixels INTEGER UNSIGNED DEFAULT 0 COMMENT 'The height of the image file, not the thimbnail',
	INDEX( fileID, messageID, ownerID, mimeType )
) ENGINE = INNODB;

-- Create a new user for the server side of the chat
CREATE USER 'xcure-server'@'localhost' IDENTIFIED BY 'sjf#n@df0';

--Grant access rights for the database tables
GRANT INSERT, SELECT, UPDATE, DELETE ON XCURE_CHAT_DB.* TO 'xcure-server'@'localhost' IDENTIFIED BY 'sjf#n@df0';
--The locking tables capability should be done on the DB or global level, here DB is enough
GRANT LOCK TABLES ON XCURE_CHAT_DB.* TO 'xcure-server'@'localhost' IDENTIFIED BY 'sjf#n@df0';

-- Create an Administrator, password = 'sec53zis' :
INSERT users SET login='SuperUser', password_hash='$2a$10$dlIgOUZGwsCeidZf30//XOC5PzxSvbHkNAIUJLByLTnctNYgHILOW', type='0', reg_date=NOW();

-- Create a 'Deleted' user profile, the password is unknown since no one should 
-- use this profile. Note that there should be just one user of this type(=2).
INSERT users SET login='<Unknown>', password_hash='????', type='2', reg_date=NOW();

-- Create the default main room:
INSERT rooms SET name='Main', description='The main chat room', uid='1', login='SuperUser', is_permanent=true, is_main=true, type='0';

-- Add access rights for the main room to the Admin
INSERT rooms_access SET rid=1, login='SuperUser', uid=1, is_system=true, is_read=true, is_read_all=true, read_all_expires=NULL, is_write=false;

--Create the root of the forum, this is an invisible, i.e. system, root node
INSERT forum_messages SET messageID=1, parentMessageID=1, sentDate=NOW(), updateDate=NOW(), senderID=1, messageTitle='The root topic of the forum', is_system=TRUE;

-- ************SUPPLEMENTARY*************

-- Delete tables:
DROP DATABASE XCURE_CHAT_DB;

-- Add a pesronal message from one user to another 
--INSERT private_messages SET uid_from=2, uid_to=1, sent_date_time=NOW();

