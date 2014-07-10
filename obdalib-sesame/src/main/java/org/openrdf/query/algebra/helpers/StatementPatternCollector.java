/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.algebra.helpers;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A QueryModelVisitor that collects StatementPattern's from a query model.
 * StatementPatterns thet are part of filters/constraints are not included in
 * the result.
 */
public class StatementPatternCollector extends QueryModelVisitorBase<RuntimeException> {

	public static List<StatementPattern> process(QueryModelNode node) {
		StatementPatternCollector collector = new StatementPatternCollector();
		node.visit(collector);
		return collector.getStatementPatterns();
	}

	private List<StatementPattern> stPatterns = new ArrayList<StatementPattern>();

	public List<StatementPattern> getStatementPatterns() {
		return stPatterns;
	}

	@Override
	public void meet(Filter node)
	{
		// Skip boolean constraints
		node.getArg().visit(this);
	}

	@Override
	public void meet(StatementPattern node)
	{
		stPatterns.add(node);
	}
}
