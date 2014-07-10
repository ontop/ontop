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
package org.openrdf.query.algebra.evaluation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that embeds {@link Filter}s with {@link SameTerm} operators
 * in statement patterns as much as possible. Operators like sameTerm(X, Y) are
 * processed by renaming X to Y (or vice versa). Operators like sameTerm(X,
 * <someURI>) are processed by assigning the URI to all occurring variables with
 * name X.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class SameTermFilterOptimizer implements QueryOptimizer {

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new SameTermFilterVisitor());
	}

	protected static class SameTermFilterVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Filter filter) {
			super.meet(filter);

			if (filter.getCondition() instanceof SameTerm) {
				// SameTerm applies to the filter's argument
				SameTerm sameTerm = (SameTerm)filter.getCondition();
				TupleExpr filterArg = filter.getArg();

				ValueExpr leftArg = sameTerm.getLeftArg();
				ValueExpr rightArg = sameTerm.getRightArg();

				// Verify that vars are (potentially) bound by filterArg
				Set<String> bindingNames = filterArg.getBindingNames();
				if (isUnboundVar(leftArg, bindingNames) || isUnboundVar(rightArg, bindingNames)) {
					// One or both var(s) are unbound, this expression will never
					// return any results
					filter.replaceWith(new EmptySet());
					return;
				}

				Set<String> assuredBindingNames = filterArg.getAssuredBindingNames();
				if (isUnboundVar(leftArg, assuredBindingNames) || isUnboundVar(rightArg, assuredBindingNames)) {
					// One or both var(s) are potentially unbound, inlining could
					// invalidate the result e.g. in case of left joins
					return;
				}

				if (leftArg instanceof Var || rightArg instanceof Var) {

					BindingSetAssignmentCollector collector = new BindingSetAssignmentCollector();
					filterArg.visit(collector);

					for (BindingSetAssignment bsa : collector.getBindingSetAssignments()) {
						// check if the VALUES clause / bindingsetassignment contains
						// one of the arguments of the sameTerm.
						// if so, we can not inline.
						Set<String> names = bsa.getAssuredBindingNames();
						if (leftArg instanceof Var) {
							if (names.contains(((Var)leftArg).getName())) {
								return;
							}
						}
						if (rightArg instanceof Var) {
							if (names.contains(((Var)rightArg).getName())) {
								return;
							}
						}
					}
				}

				Value leftValue = getValue(leftArg);
				Value rightValue = getValue(rightArg);

				if (leftValue != null && rightValue != null) {
					// ConstantOptimizer should have taken care of this
				}
				else if (leftValue != null && rightArg instanceof Var) {
					bindVar((Var)rightArg, leftValue, filter);
				}
				else if (rightValue != null && leftArg instanceof Var) {
					bindVar((Var)leftArg, rightValue, filter);
				}
				else if (leftArg instanceof Var && rightArg instanceof Var) {
					// Two unbound variables, rename rightArg to leftArg
					renameVar((Var)rightArg, (Var)leftArg, filter);
				}
			}
		}

		private boolean isUnboundVar(ValueExpr valueExpr, Set<String> bindingNames) {
			if (valueExpr instanceof Var) {
				Var var = (Var)valueExpr;
				return !var.hasValue() && !bindingNames.contains(var.getName());
			}
			return false;
		}

		private Value getValue(ValueExpr valueExpr) {
			if (valueExpr instanceof ValueConstant) {
				return ((ValueConstant)valueExpr).getValue();
			}
			else if (valueExpr instanceof Var) {
				return ((Var)valueExpr).getValue();
			}
			else {
				return null;
			}
		}

		private void renameVar(Var oldVar, Var newVar, Filter filter) {
			filter.getArg().visit(new VarRenamer(oldVar, newVar));

			// TODO: skip this step if old variable name is not used
			// Replace SameTerm-filter with an Extension, the old variable name
			// might still be relevant to nodes higher in the tree
			Extension extension = new Extension(filter.getArg());
			extension.addElement(new ExtensionElem(new Var(newVar.getName()), oldVar.getName()));
			filter.replaceWith(extension);
		}

		private void bindVar(Var var, Value value, Filter filter) {
			// Set the value on all occurences of the variable
			filter.getArg().visit(new VarBinder(var.getName(), value));
		}
	}

	protected static class VarRenamer extends QueryModelVisitorBase<RuntimeException> {
		private final Var oldVar;

		private final Var newVar;

		public VarRenamer(Var oldVar, Var newVar) {
			this.oldVar = oldVar;
			this.newVar = newVar;
		}

		@Override
		public void meet(Var var) {
			if (var.equals(oldVar)) {
				var.replaceWith(newVar.clone());
			}
		}

		@Override
		public void meet(ProjectionElem projElem)
			throws RuntimeException
		{
			if (projElem.getSourceName().equals(oldVar.getName())) {
				projElem.setSourceName(newVar.getName());
			}
		}
	}

	protected static class BindingSetAssignmentCollector extends QueryModelVisitorBase<RuntimeException> {

		private List<BindingSetAssignment> assignments = new ArrayList<BindingSetAssignment>();

		@Override
		public void meet(BindingSetAssignment bsa) {
			assignments.add(bsa);
		}

		public List<BindingSetAssignment> getBindingSetAssignments() {
			return assignments;
		}
	}

	protected static class VarBinder extends QueryModelVisitorBase<RuntimeException> {

		private final String varName;

		private final Value value;

		public VarBinder(String varName, Value value) {
			this.varName = varName;
			this.value = value;
		}

		@Override
		public void meet(Var var) {
			if (var.getName().equals(varName)) {
				var.setValue(value);
			}
		}
	}
}
