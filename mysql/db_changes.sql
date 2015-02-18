--
--						DATE: 25.09.2010 -- ON THE SERVER
--
--Adding aditional user statistics for the TOP10 site section
ALTER TABLE users ADD COLUMN num_forum_posts INTEGER UNSIGNED DEFAULT 0 COMMENT 'The number of forum messages';
ALTER TABLE users ADD INDEX( num_forum_posts );
ALTER TABLE users ADD COLUMN num_chat_msgs INTEGER UNSIGNED DEFAULT 0 COMMENT 'The number of sent chat messages';
ALTER TABLE users ADD INDEX( num_chat_msgs );
ALTER TABLE users ADD COLUMN time_on_site BIGINT UNSIGNED DEFAULT 0 COMMENT 'The time spent on site in milliseconds';
ALTER TABLE users ADD INDEX( time_on_site );

--
--						DATE: 19.09.2010 -- ON THE SERVER
--
--Adding the unlimited number of files to the user profile, not only pictures
RENAME TABLE profile_images TO profile_files;
ALTER TABLE profile_files CHANGE COLUMN thumbnail thumbnail BLOB COMMENT 'File preview thumbnail';
ALTER TABLE profile_files CHANGE COLUMN image data LONGBLOB NOT NULL COMMENT 'The data of the uploaded file';
--ALTER TABLE profile_files DROP INDEX idx ;
ALTER TABLE profile_files DROP COLUMN idx;
ALTER TABLE profile_files ADD COLUMN fileID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The id of the file';
ALTER TABLE profile_files ADD INDEX( fileID );
ALTER TABLE profile_files ADD COLUMN uploadDate DATETIME NOT NULL COMMENT 'The date-time when the file was uploaded';
UPDATE profile_files SET uploadDate=NOW() WHERE TRUE;

--
--						DATE: 6.06.2010 -- ON THE SERVER
--
--Adding avatar spoilers
ALTER TABLE users ADD COLUMN spoiler_id INTEGER UNSIGNED DEFAULT 0 COMMENT 'The id of the spoiler used for the avatar, zero for no spoiler';
ALTER TABLE users ADD COLUMN spoiler_exp_date DATETIME DEFAULT NULL COMMENT 'The date-time when the user-avatar spoiler expires, can be null if no spoiler is set';

--
--						DATE: 14.05.2010 -- ON THE SERVER
--
-- Speeding up the forum by introducing the new fields to the forum message
ALTER TABLE forum_messages ADD COLUMN numReplies INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'The number of message replies';
ALTER TABLE forum_messages ADD COLUMN stLastSenderID INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'The id of the user who made the last post in the tree rooted to this node';
ALTER TABLE forum_messages ADD COLUMN stLastPostDate DATETIME NOT NULL COMMENT 'The date of the last post in the tree rooted to this node';
ALTER TABLE forum_messages ADD INDEX( stLastPostDate );
--TODO: Run the programm for setting the initial values of the fields added above

--
--						DATE: 14.05.2010 -- ON THE SERVER
--
-- Adding the gold pieces management
ALTER TABLE users ADD COLUMN gold_pieces INTEGER UNSIGNED DEFAULT 0 COMMENT 'Indicates the amount of gold pieces the user has';
ALTER TABLE users ADD INDEX( gold_pieces );
UPDATE users SET gold_pieces=9 WHERE TRUE;
ALTER TABLE avatar_images ADD COLUMN mimeType VARCHAR(256) CHARSET utf8 DEFAULT 'image/jpeg' NOT NULL COMMENT 'The MIME type of the file';

--
--						DATE: 26.03.2010 -- ON THE SERVER
--
-- Misceleneous
ALTER TABLE forum_messages CHANGE COLUMN messageTitle messageTitle VARCHAR(256) CHARSET utf8 DEFAULT '' COMMENT 'The message title';
ALTER TABLE forum_messages CHANGE COLUMN messageBody messageBody TEXT CHARSET utf8 DEFAULT '' COMMENT 'The message body';

--
--						DATE: 19.03.2010 -- ON THE SERVER
--
-- Adding the support for the avoidance of the simular login names
ALTER TABLE users ADD COLUMN uni_login VARCHAR(20) CHARSET utf8 COMMENT 'The unified user login';
ALTER TABLE users ADD INDEX( uni_login );

--TODO: start the website, open the site page and login. Make sure no one new is registered.
--Remove the users with the dupliate unif_logins, then do:

ALTER TABLE users MODIFY COLUMN uni_login VARCHAR(20) CHARSET utf8 NOT NULL UNIQUE COMMENT 'The unified user login';

--NOTE: Now the update is complete

--
--						DATE: 03.02.2010 -- ON THE SERVER
--
-- Adding the chat bot that works with the old chat files from the public messages
DELETE FROM chat_files WHERE true;
ALTER TABLE chat_files ADD COLUMN is_public BOOLEAN DEFAULT FALSE COMMENT 'True if this chat files is attached to a public chat message';
ALTER TABLE chat_files ADD INDEX( is_public );
ALTER TABLE chat_files ADD COLUMN md5 CHAR(32) CHARSET utf8 DEFAULT '' COMMENT 'The MD5 sum for the file data';
ALTER TABLE chat_files ADD INDEX( md5 );
ALTER TABLE users ADD COLUMN is_bot BOOLEAN DEFAULT FALSE COMMENT 'True if this user is a bot';

--
--						DATE: 13.01.2010 -- ON THE SERVER
--
-- Adding the additional file data to the profile images
ALTER TABLE profile_images ADD COLUMN mimeType VARCHAR(256) CHARSET utf8 DEFAULT 'image/jpeg' NOT NULL COMMENT 'The MIME type of the file';
ALTER TABLE profile_images ADD COLUMN fileName VARCHAR(128) CHARSET utf8 DEFAULT 'unknown' NOT NULL COMMENT 'The uploaded file name';
ALTER TABLE profile_images ADD COLUMN widthPixels INTEGER UNSIGNED DEFAULT 640 COMMENT 'The width of the image file, not the thimbnail';
ALTER TABLE profile_images ADD COLUMN heightPixels INTEGER UNSIGNED DEFAULT 480 COMMENT 'The height of the image file, not the thimbnail';

--
--						DATE: 07.01.2010 -- ON THE SERVER
--
-- Adding the possibility to send files to the chat, other than images, but also music and video
DROP TABLE chat_images;
ALTER TABLE chat_messages CHANGE COLUMN imageID fileID INTEGER UNSIGNED DEFAULT 0 COMMENT 'The id of the file associated with this message or null if no file';

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
    INDEX( fileID, ownerID, uploadDate )
) ENGINE = INNODB;

ALTER TABLE forum_files ADD INDEX( mimeType );

--
--						DATE: 29.12.2009 -- ON THE SERVER
--
-- Adding votes for the forum messages
ALTER TABLE forum_messages ADD COLUMN numVotes INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'The number of votes for this post';
ALTER TABLE forum_messages ADD COLUMN voteValue INTEGER NOT NULL DEFAULT 0 COMMENT 'The current vote value';

-- Create the forum votes table
CREATE TABLE forum_message_votes (
    messageID INTEGER UNSIGNED NOT NULL COMMENT 'The id of the forum message to which this vote was done',
    FOREIGN KEY (messageID) REFERENCES forum_messages (messageID) ON DELETE CASCADE,
    senderID INTEGER UNSIGNED NOT NULL COMMENT 'The vote was done by the user with this id',
    FOREIGN KEY (senderID) REFERENCES users (uid) ON DELETE CASCADE,
	voteValue INTEGER NOT NULL DEFAULT 0 COMMENT 'The vote made by this user'
) ENGINE = INNODB;

--
--						DATE: 26.12.2009 -- ON THE SERVER
--
-- Minor alternations of the forum messages table

ALTER TABLE forum_messages ADD COLUMN is_approved BOOLEAN DEFAULT FALSE COMMENT 'True if the forum post was approved to be a news';
ALTER TABLE forum_messages ADD INDEX( is_approved );

--
--						DATE: 03.11.2009 -- ON THE SERVER
--
-- Minor alternations of the forum tables

ALTER TABLE forum_files ADD COLUMN uploadDate DATETIME NOT NULL COMMENT 'The date-time when the file was uploaded';
UPDATE forum_files SET uploadDate=NOW() WHERE TRUE;

--
--						DATE: 19.09.2009 -- ON THE SERVER
--
-- Adding the message and file tables for the forum

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
    messageTitle VARCHAR(128) CHARSET utf8 DEFAULT '' COMMENT 'The message title',
    messageBody VARCHAR(4096) CHARSET utf8 DEFAULT '' COMMENT 'The message body',
	is_system BOOLEAN DEFAULT FALSE COMMENT 'The primary node of the forum is marked as true, because it should not be visible',
    INDEX( messageID, sentDate, senderID, parentMessageID, messageTitle, messageBody, is_system, updateDate, messagePathID )
) ENGINE = INNODB;

-- Create forum files table
CREATE TABLE forum_files (
    fileID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'The id of the file',
    messageID INTEGER UNSIGNED DEFAULT 0 COMMENT 'The id of the forum message to which this file blongs',
    FOREIGN KEY (messageID) REFERENCES forum_messages (messageID) ON DELETE CASCADE,
    ownerID INTEGER UNSIGNED NOT NULL COMMENT 'The id of the user who uploaded the file',
    FOREIGN KEY (ownerID) REFERENCES users (uid) ON DELETE CASCADE,
	mimeType VARCHAR(256) CHARSET utf8 NOT NULL COMMENT 'The MIME type of the file',
    fileName VARCHAR(128) CHARSET utf8 NOT NULL COMMENT 'The uploaded file name',
    thumbnail BLOB COMMENT 'File preview thumbnail',
    data LONGBLOB NOT NULL COMMENT 'The data of the uploaded file',
	widthPixels INTEGER UNSIGNED DEFAULT 0 COMMENT 'The width of the image file, not the thimbnail',
	heightPixels INTEGER UNSIGNED DEFAULT 0 COMMENT 'The height of the image file, not the thimbnail',
    INDEX( fileID, messageID, ownerID )
) ENGINE = INNODB;

--Create the root of the forum, this is an invisible, i.e. system, root node
INSERT forum_messages SET messageID=1, parentMessageID=1, sentDate=NOW(), updateDate=NOW(), senderID=1, messageTitle='The root topic of the forum', is_system=TRUE;

--
--						DATE: 06.09.2009 -- ON THE SERVER
--
-- Fixing the problem with that the chat message recepients are not deleted   
-- from the DB. Since ENGINE=MEMORY does not support key constrains
ALTER TABLE chat_messages ADD INDEX( sentDate );
DELETE FROM chat_msg_recepient WHERE TRUE;
ALTER TABLE chat_msg_recepient ADD COLUMN sentDate DATETIME NOT NULL COMMENT 'The date-time when the message-recepient was added';
ALTER TABLE chat_msg_recepient ADD INDEX( sentDate );
