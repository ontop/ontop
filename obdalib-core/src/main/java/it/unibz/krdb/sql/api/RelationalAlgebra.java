package it.unibz.krdb.sql.api;

/*
 * #%L
 * ontop-obdalib-core
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

import java.io.Serializable;

/**
 * An abstract base class for each term in relational algebra.
 */
public abstract class RelationalAlgebra implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 6661455497184268723L;

	/**
	 * The SELECT statement.
	 */
	protected Projection projection;
	
	/**
	 * The WHERE statement.
	 */
	protected Selection selection;
	
	/**
	 * The GROUP BY statement.
	 */
	protected Aggregation aggregation;

	public void setProjection(Projection projection) {
		this.projection = projection;
	}

	public Projection getProjection() {
		return projection;
	}

	public void setSelection(Selection selection) {
		this.selection = selection;
	}

	public Selection getSelection() {
		return selection;
	}

	public void setAggregation(Aggregation aggregation) {
		this.aggregation = aggregation;
	}

	public Aggregation getAggregation() {
		return aggregation;
	}
	
	public abstract RelationalAlgebra clone();
}
