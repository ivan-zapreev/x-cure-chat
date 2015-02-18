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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This class is meant to be used with the ConnectionWrapper
 */
public abstract class QueryExecutor<ParamReturnObjectType> extends ExecutorBase<ParamReturnObjectType> {
	
	/**
	 * Based on the connection we make a prepared statement
	 * @param connection the database connection
	 * @return the resulting prepared statement that has to be executed
	 * @throws SQLException if smth good wrong
	 */
	public abstract PreparedStatement prepareStatement( Connection connection ) throws SQLException;
	
	/**
	 * Bind parameters into the prepared statement
	 * @param pstmt the prepared statement
	 * @throws SQLException if smth good wrong
	 */
	public abstract void bindParameters( PreparedStatement pstmt ) throws SQLException;
	
	/**
	 * Executes SQL query. Note that right after it the JDBC resources
	 * associated with this connection within which the query was executed
	 * are released back into connection pool.
	 * @param pstmt the prepared statement to execute.
	 * @param result is an object that can be used to return results with
	 * @return the ResultSet produced by the execution
	 * @throws SQLException if smth goes wrong
	 */
	public abstract ResultSet executeQuery( PreparedStatement pstmt, ParamReturnObjectType result ) throws SQLException, SiteException;
	
	/**
	 * Process the obtained result set
	 * @param resultSet the result of DB query
	 * @param result is an object that can be used to return results with
	 * @throws SiteLogicException if when processing the
	 * results we found some logical error.
	 */
	public abstract void processResultSet( ResultSet resultSet, ParamReturnObjectType result ) throws SQLException, SiteException;

}
