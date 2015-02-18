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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This class is used for executing a set of queries
 * Its children are meant to be used with the ConnectionWrapper
 * These class's children are supposed to manage everything but
 * the DB connection.
 */
public abstract class QuerySetExecutor<ParamReturnObjectType> extends ExecutorBase<ParamReturnObjectType> {
	
	/**
	 * @author zapreevis
	 * The tranzaction executor class.
	 */
	public class TransactionExecutor {
		private final Transaction transaction;
		private final Logger logger;
		public TransactionExecutor( final Transaction transaction, final Logger logger ) {
			this.transaction = transaction;
			this.logger = logger;
		}
		public void execute( Connection connection ) throws SQLException, SiteException {
			//Create the statement for executing the DB queries
			Statement sqlStatement = null;
			//Create the SQL statement object
			sqlStatement = connection.createStatement();
			
			//Execute queries
			try {
				//Begin the transaction
				sqlStatement.execute( "START TRANSACTION");
				
				//Execute the transaction
				transaction.execute( sqlStatement, logger );
				
				//Try to commit the transaction
				closeTransaction( sqlStatement , "COMMIT", transaction.getDescription() );				
			} catch ( SQLException e ){
				logger.error( "An SQL exception while moving a forum message", e );
				//Try to rollback the transaction
				closeTransaction( sqlStatement , "ROLLBACK", transaction.getDescription() );
				//Throw an exception
				throw new InternalSiteException( InternalSiteException.DATABASE_EXCEPTION_ERR );
			}
		}
		
		/**
		 * Allows to close the transaction with a COMMIT or ROLLBACK statements
		 * @param sqlStatement the sql statement to work with
		 * @param closeSQL the type of closing action: ROLLBACK or COMMIT
		 */
		protected void closeTransaction( final Statement sqlStatement, final String closeSQL,
										 final String message ) {
			if( sqlStatement != null ) {
				try {
					sqlStatement.execute( closeSQL );
				} catch ( SQLException e ) {
					logger.error( "An SQL exception while '" + closeSQL + "' the transaction after" + message, e );
				} finally {
					//Close the statement
					try {
						sqlStatement.close();
					} catch ( SQLException e ) {
						logger.error( "An exception while closing the SQL statement", e);
					}
				}
			}
		}
	}
	
	/**
	 * @author zapreevis
	 * Represents the transaction body
	 */
	public interface Transaction {
		public void execute( final Statement sqlStatement, final Logger logger ) throws SQLException, SiteException;
		public String getDescription();
	}
	
	/**
	 * This method is supposed to execute a set of DB queries
	 * and then to clean up everything, e.g. the prepared statements and the result sets
	 * @param connection the DB connection
	 * @param result the return parameter, this is used for returning the data if any
	 * @throws SQLException if smth goes wrong
	 * @throws SiteException if smth goes wrong
	 */
	public abstract void executeQuerySet( Connection connection, ParamReturnObjectType result ) throws SQLException, SiteException;
}
