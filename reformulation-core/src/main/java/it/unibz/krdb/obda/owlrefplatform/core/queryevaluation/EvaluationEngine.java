package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.OBDADataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * An interface representing the Evaluation engine for a Technique wrapper.
 * The main task of such an evaluation engine is to evaluate the rewritten
 * and unfolded data log program over the data source.
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface EvaluationEngine {

	/**
	 * Evalutes the given string over the data source
	 * 
	 * @param sql the query
	 * @return the result set
	 * @throws Exception
	 */
	public ResultSet execute(String sql) throws Exception;
	
	public Statement getStatement() throws SQLException;
	
	/**
	 * Updates the current data source with the given one, i.e. after 
	 * the update queries will be evaluated over the new data source.
	 * 
	 * @param ds the new data source
	 */
	public void update(OBDADataSource ds);
	
	public void closeStatement() throws Exception;
	
	public void isCanceled (boolean bool);
	
	public void dispose();
}
