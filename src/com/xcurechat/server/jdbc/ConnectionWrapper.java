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
 * The server-side RPC package, managing connections and connection pool.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;

/**
 * @author zapreevis
 * This class implements a factory distributing the connection objects
 * The resources get released after the connection is executed, i.e
 * the execute method is called.
 */
public class ConnectionWrapper<ParamReturnObjectType> {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ConnectionWrapper.class );
	
	//The variables that define the database to which we make the query
	public static final byte XCURE_CHAT_DB = 0;
	public static final byte XCURE_HOSTIP_DB = 1;
	
	//The Data source stored in the context
	private static DataSource xcureChatDataSource = null;
	private static DataSource xcureHostipDataSource = null;

	static{
		try{
			Context dbContext = new InitialContext();
			xcureChatDataSource = (DataSource) dbContext.lookup( "java:comp/env/jdbc/XCURE_CHAT_DB" );
			xcureHostipDataSource = (DataSource) dbContext.lookup( "java:comp/env/jdbc/XCURE_HOSTIP_DB" );
		}  catch ( NamingException e ) {
			//TODO: Add special exceptions for this case, like an internal system error or smth
			logger.error( "Can not lookup the naming context with the DB connection pool", e);
		}
	}
	
	/**
	 * When the application is run in Tomcat this method MUST NOT be used, because then the data source is
	 * automatically retrieved from the context. If we run the applications as stand alone, then we have to
	 * initialize the data source ourselves. In the latter case this method should be used.
	 * @param jdbcURL the database url
	 * @param dbLoginName the login for the database
	 * @param dbUserPassword the pasword for the database
	 * @param db the database XCURE_CHAT_DB or XCURE_HOSTIP_DB
	 * @throws Exception if the database is not recognized.
	 */
	public static void initializeDataSource( final String jdbcURL, final String dbLoginName,
											 final String dbUserPassword, final byte db ) throws Exception {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUrl ( jdbcURL );
		dataSource.setUser( dbLoginName );
		dataSource.setPassword( dbUserPassword );
		switch( db ) {
			case XCURE_CHAT_DB: xcureChatDataSource = dataSource; break;
			case XCURE_HOSTIP_DB: xcureHostipDataSource = dataSource; break;
			default:
				throw new Exception("Unknown DB = " + db + " should be either XCURE_CHAT_DB("+XCURE_CHAT_DB+") or XCURE_HOSTIP_DB("+XCURE_HOSTIP_DB+")");
		}
	}
	
	//This stores the user object that executed the
	//query within the provided connection.
	private ExecutorBase<ParamReturnObjectType> executor = null;
	
	private ConnectionWrapper() {
		super();
	}
	
	/**
	 * A private constructor, since we do not want to let people
	 * allocate this object on their own, just in case. 
	 * @param queryExecutor
	 */
	private ConnectionWrapper( ExecutorBase<ParamReturnObjectType> executor ){
		this();
		this.executor = executor;
	}
	
	/**
	 * Get a new instance of the ConnectionWrapper
	 * @param executor the object that will execute the query step by step
	 * @return the provided connection wrapper
	 */
	static public <ParamReturnObjectType> ConnectionWrapper<ParamReturnObjectType> createConnectionWrapper( ExecutorBase<ParamReturnObjectType> executor ){
		return new ConnectionWrapper<ParamReturnObjectType>( executor );
	}
	
	/**
	 * Get the DB connection from the JDBC connection pool
	 * return the resources to the pool after everything is done
	 * @param db the database index, for which the query is targeted
	 * @throws SiteException if the retrieved data showed some logical problem.
	 */
	public void executeQuery(final byte db) throws SiteException {
		executeQuery( null, db );
	}
	
	/**
	 * Get the DB connection from the JDBC connection pool
	 * return the resources to the pool after everything is done
	 * @param result is an object that can be used to return results with
	 * @param db the database index, for which the query is targeted
	 * @throws SiteException if the retrieved data showed some logical problem.
	 */
	public void executeQuery( ParamReturnObjectType result, final byte db ) throws SiteException {
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try{
			//Get the query
			switch(db){
				case XCURE_CHAT_DB:
					connection = xcureChatDataSource.getConnection();
					break;
				case XCURE_HOSTIP_DB:
					connection = xcureHostipDataSource.getConnection();
					break;
				default:
					logger.error("Trying to execute query targeted to an unknown database with index: "+db);
			}
			
			if( executor instanceof QueryExecutor<?> ) {
				QueryExecutor<ParamReturnObjectType> queryExecutor = (QueryExecutor<ParamReturnObjectType>) executor;
				//Do the query and etc
				pstmt = queryExecutor.prepareStatement( connection );
				queryExecutor.bindParameters( pstmt );
				resultSet = queryExecutor.executeQuery( pstmt, result );
				if( resultSet != null )	{
					//If it was a select statement or another one which
					//results in a ResultSet then process it
					queryExecutor.processResultSet( resultSet, result );
				}
			} else {
				if( executor instanceof QuerySetExecutor<?> ) {
					QuerySetExecutor<ParamReturnObjectType> querySetExecutor = (QuerySetExecutor<ParamReturnObjectType>) executor;
					querySetExecutor.executeQuerySet(connection, result);
				} else {
					logger.error("Unsupported executor class type "+ executor.getClass().getSimpleName());
				}
			}
		} catch ( SQLException e ) {
			logger.error( "An SQL exception while working with the database", e);
			throw new InternalSiteException( InternalSiteException.DATABASE_EXCEPTION_ERR );
		} finally {
			if( resultSet != null ){
				try{
					resultSet.close();
				}catch(SQLException e){
					logger.error( "An SQL exception while closing the result set", e);
				}
				resultSet = null;
			}
			if( pstmt != null ){
				try{
					pstmt.close();
				}catch(SQLException e){
					logger.error( "An SQL exception while closing the prepared statement", e);
				}
				pstmt = null;
			}
			if( connection != null ) {
				try{
					connection.close();
				}catch(SQLException e){
					logger.error( "An SQL exception while closing the connection", e);
				}
				connection = null;
			}
		}
	}
}
