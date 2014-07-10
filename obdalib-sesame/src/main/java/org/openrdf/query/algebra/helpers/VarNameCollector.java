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

import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Var;

/**
 * A QueryModelVisitor that collects the names of (non-constant) variables that
 * are used in a query model.
 */
public class VarNameCollector extends QueryModelVisitorBase<RuntimeException> {

	public static Set<String> process(QueryModelNode node) {
		VarNameCollector collector = new VarNameCollector();
		node.visit(collector);
		return collector.getVarNames();
	}

	private Set<String> varNames = new LinkedHashSet<String>();

	public Set<String> getVarNames() {
		return varNames;
	}

	@Override
	public void meet(Var var) {
		if (!var.hasValue()) {
			varNames.add(var.getName());
		}
	}
}
