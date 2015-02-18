In order to make the XCureChat run we need to:

1. Download and install Tomcat

    http://tomcat.apache.org/download-55.cgi

   Optionally set up the automacti start-up and shut-down scripts

    http://www.howtogeek.com/howto/linux/installing-tomcat-6-on-ubuntu/

   NOTE: Use https://visualvm.dev.java.net/ for the application profiling
   Use: export CATALINA_OPTS="-Dcom.sun.management.jmxremote
                              -Dcom.sun.management.jmxremote.port=8086
                              -Dcom.sun.management.jmxremote.authenticate=false
                              -Dcom.sun.management.jmxremote.ssl=false
                              -Dcom.sun.management.jmxremote.hostname=hostname/IP"
   before starting up Tomcat, to be able to connect to it.

2. Download the packages:

    http://dev.mysql.com/downloads/connector/j/5.1.html
    http://commons.apache.org/downloads/download_collections.cgi

   Place the jar archives from these packages into the Tomcat installation

    /usr/local/tomcat/common/lib (Tomcat5.5)
    /usr/local/tomcat/lib (Tomcat6.0)
	
    Also, we use the html parser that is included into the war file:
    http://htmlparser.sourceforge.net
    We use: htmlparser.jar and htmllexer.jar
	
3. Install MySQL database, and create the site's database and user using
    
    /XCureChat/mysql/create_user_and_database.sql
    
   Also, we need to create and initialize the Geolocation database, see

    /XCureChat/mysql/create_hostip_database.sql

   Increase the max_allowed_packet to be 32 Mb, to allow larger file uploads.
   		sudo pico /etc/mysql/my.cnf
   and change 
		max_allowed_packet = 16M
   into 
		max_allowed_packet = 32M
	
   YOU MIGHT WANT TO DOUBLE OTHER VALUES THE SAME WAY!
   BECAUSE: "You can also get strange problems with large packets if you are using
   large BLOB values but have not given mysqld access to enough memory to handle
   the query. If you suspect this is the case, try adding ulimit -d 256000 to the
   beginning of the mysqld_safe script and restarting mysqld."
   
   Also enable query caching by setting
        query_cache_type=1
   in the cnf file.

4. For other required packages, such as log4j and etc, see

    /XCureChat/build.properties
    
5. Compilation and instalation is done by running ant in /XCureChat forlder note that
   the correct tomcat directory has to be specified in /XCureChat/build.properties

6. For the tomcat that was installed via apt-get modify:

    /etc/tomcat5.5/policy.d/03catalina.policy (Tomcat5.5)
    /etc/tomcat6/policy.d/03catalina.policy (Tomcat6.0)

    Add:
    
        permission java.io.FilePermission "${catalina.base}${file.separator}webapps${file.separator}chat${file.separator}WEB-INF${file.separator}classes${file.separator}logging.properties", "read";
    
    To:
    
        // These permissions apply to JULI
        grant codeBase "file:${catalina.home}/bin/tomcat-juli.jar" {
            ...
        }
    
    and add
        
    //Grant the permissions for logging of X-Cure chat
    grant codeBase "file:${catalina.home}/webapps/chat/-" {
            permission java.security.AllPermission;
    };

    NOTE: In case we set deployment.context = ROOT in build.properties
    file then in the above we should change "chat" into ROOT.
    
    Also, use:
        service tomcat5.5 stop
    AND
        service tomcat5.5 start

7. To change the Tomcat port from 8080 to 80 (standard http) either modify
        /usr/share/tomcat5.5/conf/server.xml (if Tomcat is run under root)
    OR
        run Tomcat + Apache (if tomcat is run under non-root)

8. Make the server to use the data compression to decrease the loading times, in
		/usr/share/tomcat5.5/conf/server.xml
   find the HTTP Connector element for the HTTP/1.1 protocol, e.g.:
		<Connector port="8080" protocol="HTTP/1.1" ... />
   and add a new attribute to it: 
		compression="on"
   Tobe more specific, we need to set:
        compression="on"
        compressionMinSize="1024"
        noCompressionUserAgents="gozilla, traviata"
        compressableMimeType="text/html,text/xml,text/plain,text/css,text/javascript"

9. To run jsp pages efficiently make sure that the following setup
   is present in conf/web.xml:
        <init-param>
            <param-name>development</param-name>
            <param-value>false</param-value>
        </init-param>
        <init-param>
            <param-name>fork</param-name>
            <param-value>true</param-value>
        </init-param>

10. If you want to make the chat application to be the default one then 
    set deployment.context = ROOT in build.properties, see point 6.

