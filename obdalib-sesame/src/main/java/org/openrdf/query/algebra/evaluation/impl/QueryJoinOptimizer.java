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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.ZeroLengthPath;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.algebra.helpers.TupleExprs;

/**
 * A query optimizer that re-orders nested Joins.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class QueryJoinOptimizer implements QueryOptimizer {

	protected final EvaluationStatistics statistics;

	public QueryJoinOptimizer() {
		this(new EvaluationStatistics());
	}

	public QueryJoinOptimizer(EvaluationStatistics statistics) {
		this.statistics = statistics;
	}

	/**
	 * Applies generally applicable optimizations: path expressions are sorted
	 * from more to less specific.
	 * 
	 * @param tupleExpr
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new JoinVisitor());
	}

	protected class JoinVisitor extends QueryModelVisitorBase<RuntimeException> {

		Set<String> boundVars = new HashSet<String>();

		@Override
		public void meet(LeftJoin leftJoin) {
			leftJoin.getLeftArg().visit(this);

			Set<String> origBoundVars = boundVars;
			try {
				boundVars = new HashSet<String>(boundVars);
				boundVars.addAll(leftJoin.getLeftArg().getBindingNames());

				leftJoin.getRightArg().visit(this);
			}
			finally {
				boundVars = origBoundVars;
			}
		}

		@Override
		public void meet(Join node) {

			Set<String> origBoundVars = boundVars;
			try {
				boundVars = new HashSet<String>(boundVars);

				// Recursively get the join arguments
				List<TupleExpr> joinArgs = getJoinArgs(node, new ArrayList<TupleExpr>());

				// Reorder the (recursive) join arguments to a more optimal sequence
				List<TupleExpr> orderedJoinArgs = new ArrayList<TupleExpr>(joinArgs.size());

				// first get all subselects and order them
				List<TupleExpr> orderedSubselects = reorderSubselects(getSubSelects(joinArgs));
				joinArgs.removeAll(orderedSubselects);

				// We order all remaining join arguments based on cardinality and
				// variable frequency statistics
				if (joinArgs.size() > 0) {
					// Build maps of cardinalities and vars per tuple expression
					Map<TupleExpr, Double> cardinalityMap = new HashMap<TupleExpr, Double>();
					Map<TupleExpr, List<Var>> varsMap = new HashMap<TupleExpr, List<Var>>();

					for (TupleExpr tupleExpr : joinArgs) {
						cardinalityMap.put(tupleExpr, statistics.getCardinality(tupleExpr));
						if (tupleExpr instanceof ZeroLengthPath) {
							varsMap.put(tupleExpr, ((ZeroLengthPath)tupleExpr).getVarList());
						}
						else {
							varsMap.put(tupleExpr, getStatementPatternVars(tupleExpr));
						}
					}

					// Build map of var frequences
					Map<Var, Integer> varFreqMap = new HashMap<Var, Integer>();
					for (List<Var> varList : varsMap.values()) {
						getVarFreqMap(varList, varFreqMap);
					}

					// order all other join arguments based on available statistics
					while (!joinArgs.isEmpty()) {
						TupleExpr tupleExpr = selectNextTupleExpr(joinArgs, cardinalityMap, varsMap, varFreqMap,
								boundVars);

						joinArgs.remove(tupleExpr);
						orderedJoinArgs.add(tupleExpr);

						// Recursively optimize join arguments
						tupleExpr.visit(this);

						boundVars.addAll(tupleExpr.getBindingNames());
					}
				}

				// Build new join hierarchy
				TupleExpr subselectJoins = null;
				if (orderedSubselects.size() > 0) {
					subselectJoins = orderedSubselects.get(0);
					for (int i = 1; i < orderedSubselects.size(); i++) {
						subselectJoins = new Join(subselectJoins, orderedSubselects.get(i));
					}
				}

				if (orderedJoinArgs.size() > 0) {
					// Note: generated hierarchy is right-recursive to help the
					// IterativeEvaluationOptimizer to factor out the left-most join
					// argument
					int i = orderedJoinArgs.size() - 1;
					TupleExpr replacement = orderedJoinArgs.get(i);
					for (i--; i >= 0; i--) {
						replacement = new Join(orderedJoinArgs.get(i), replacement);
					}

					if (subselectJoins != null) {
						replacement = new Join(subselectJoins, replacement);
					}

					// Replace old join hierarchy
					node.replaceWith(replacement);

				}
				else {
					// only subselect joins involved in this query.
					node.replaceWith(subselectJoins);
				}
			}
			finally {
				boundVars = origBoundVars;
			}
		}

		protected <L extends List<TupleExpr>> L getJoinArgs(TupleExpr tupleExpr, L joinArgs) {
			if (tupleExpr instanceof Join) {
				Join join = (Join)tupleExpr;
				getJoinArgs(join.getLeftArg(), joinArgs);
				getJoinArgs(join.getRightArg(), joinArgs);
			}
			else {
				joinArgs.add(tupleExpr);
			}

			return joinArgs;
		}

		protected List<Var> getStatementPatternVars(TupleExpr tupleExpr) {
			List<StatementPattern> stPatterns = StatementPatternCollector.process(tupleExpr);
			List<Var> varList = new ArrayList<Var>(stPatterns.size() * 4);
			for (StatementPattern sp : stPatterns) {
				sp.getVars(varList);
			}
			return varList;
		}

		protected <M extends Map<Var, Integer>> M getVarFreqMap(List<Var> varList, M varFreqMap) {
			for (Var var : varList) {
				Integer freq = varFreqMap.get(var);
				freq = (freq == null) ? 1 : freq + 1;
				varFreqMap.put(var, freq);
			}
			return varFreqMap;
		}

		protected List<TupleExpr> getSubSelects(List<TupleExpr> expressions) {
			List<TupleExpr> subselects = new ArrayList<TupleExpr>();

			for (TupleExpr expr : expressions) {
				if (TupleExprs.containsProjection(expr)) {
					subselects.add(expr);
				}
			}
			return subselects;
		}

		
		/**
		 * Determines an optimal ordering of subselect join arguments, based on
		 * variable bindings. An ordering is considered optimal if for each
		 * consecutive element it holds that first of all its shared variables
		 * with all previous elements is maximized, and second, the union of all
		 * its variables with all previous elements is maximized.
		 * <p>
		 * Example: reordering
		 * 
		 * <pre>
		 *   [f] [a b c] [e f] [a d] [b e]
		 * </pre>
		 * 
		 * should result in:
		 * 
		 * <pre>
		 *   [a b c] [a d] [b e] [e f] [f]
		 * </pre>
		 * 
		 * @param subselects
		 *        the original ordering of expressions
		 * @return the optimized ordering of expressions
		 */
		protected List<TupleExpr> reorderSubselects(List<TupleExpr> subselects) {

			if (subselects.size() == 1) {
				return subselects;
			}

			List<TupleExpr> result = new ArrayList<TupleExpr>();
			if (subselects == null || subselects.size() == 0) {
				return result;
			}

			// Step 1: determine size of join for each pair of arguments
			HashMap<Integer, List<TupleExpr[]>> joinSizes = new HashMap<Integer, List<TupleExpr[]>>();

			int maxJoinSize = 0;
			for (int i = 0; i < subselects.size(); i++) {
				TupleExpr firstArg = subselects.get(i);
				for (int j = i + 1; j < subselects.size(); j++) {
					TupleExpr secondArg = subselects.get(j);

					Set<String> names = firstArg.getBindingNames();
					names.retainAll(secondArg.getBindingNames());

					int joinSize = names.size();
					if (joinSize > maxJoinSize) {
						maxJoinSize = joinSize;
					}

					List<TupleExpr[]> l = null;

					if (joinSizes.containsKey(joinSize)) {
						l = joinSizes.get(joinSize);
					}
					else {
						l = new ArrayList<TupleExpr[]>();
					}
					TupleExpr[] tupleTuple = new TupleExpr[] { firstArg, secondArg };
					l.add(tupleTuple);
					joinSizes.put(joinSize, l);
				}
			}

			// Step 2: find the first two elements for the ordered list by
			// selecting the pair with first of all,
			// the highest join size, and second, the highest union size.

			TupleExpr[] maxUnionTupleTuple = null;
			int currentUnionSize = -1;

			// get a list of all argument pairs with the maximum join size
			List<TupleExpr[]> list = joinSizes.get(maxJoinSize);

			// select the pair that has the highest union size.
			for (TupleExpr[] tupleTuple : list) {
				Set<String> names = tupleTuple[0].getBindingNames();
				names.addAll(tupleTuple[1].getBindingNames());
				int unionSize = names.size();

				if (unionSize > currentUnionSize) {
					maxUnionTupleTuple = tupleTuple;
					currentUnionSize = unionSize;
				}
			}

			// add the pair to the result list.
			result.add(maxUnionTupleTuple[0]);
			result.add(maxUnionTupleTuple[1]);

			// Step 3: sort the rest of the list by selecting and adding an element
			// at a time.
			while (result.size() < subselects.size()) {
				result.add(getNextSubselect(result, subselects));
			}

			return result;
		}

		private TupleExpr getNextSubselect(List<TupleExpr> currentList, List<TupleExpr> joinArgs) {

			// determine union of names of all elements currently in the list: this
			// corresponds to the projection resulting from joining all these
			// elements.
			Set<String> currentListNames = new HashSet<String>();
			for (TupleExpr expr : currentList) {
				currentListNames.addAll(expr.getBindingNames());
			}

			// select the next argument from the list, by checking that it has,
			// first, the highest join size with the current list, and second, the
			// highest union size.
			TupleExpr selected = null;
			int currentUnionSize = -1;
			int currentJoinSize = -1;
			for (TupleExpr candidate : joinArgs) {
				if (!currentList.contains(candidate)) {
					Set<String> names = candidate.getBindingNames();
					names.retainAll(currentListNames);
					int joinSize = names.size();

					names = candidate.getBindingNames();
					names.addAll(currentListNames);
					int unionSize = names.size();

					if (joinSize > currentJoinSize) {
						selected = candidate;
						currentJoinSize = joinSize;
						currentUnionSize = unionSize;
					}
					else if (joinSize == currentJoinSize) {
						if (unionSize > currentUnionSize) {
							selected = candidate;
							currentJoinSize = joinSize;
							currentUnionSize = unionSize;
						}
					}
				}
			}

			return selected;
		}

		/**
		 * Selects from a list of tuple expressions the next tuple expression that
		 * should be evaluated. This method selects the tuple expression with
		 * highest number of bound variables, preferring variables that have been
		 * bound in other tuple expressions over variables with a fixed value.
		 */
		protected TupleExpr selectNextTupleExpr(List<TupleExpr> expressions,
				Map<TupleExpr, Double> cardinalityMap, Map<TupleExpr, List<Var>> varsMap,
				Map<Var, Integer> varFreqMap, Set<String> boundVars)
		{
			TupleExpr result = null;

			double lowestCardinality = Double.MAX_VALUE;
			for (TupleExpr tupleExpr : expressions) {
				// Calculate a score for this tuple expression
				double cardinality = getTupleExprCardinality(tupleExpr, cardinalityMap, varsMap, varFreqMap,
						boundVars);

				if (cardinality < lowestCardinality) {
					// More specific path expression found
					lowestCardinality = cardinality;
					result = tupleExpr;
				}
			}

			return result;
		}

		protected double getTupleExprCardinality(TupleExpr tupleExpr, Map<TupleExpr, Double> cardinalityMap,
				Map<TupleExpr, List<Var>> varsMap, Map<Var, Integer> varFreqMap, Set<String> boundVars)
		{
			double cardinality = cardinalityMap.get(tupleExpr);

			List<Var> vars = varsMap.get(tupleExpr);

			// Compensate for variables that are bound earlier in the evaluation
			List<Var> unboundVars = getUnboundVars(vars);
			List<Var> constantVars = getConstantVars(vars);
			int nonConstantVarCount = vars.size() - constantVars.size();
			if (nonConstantVarCount > 0) {
				double exp = (double)unboundVars.size() / nonConstantVarCount;
				cardinality = Math.pow(cardinality, exp);
			}

			if (unboundVars.isEmpty()) {
				// Prefer patterns with more bound vars
				if (nonConstantVarCount > 0) {
					cardinality /= nonConstantVarCount;
				}
			}
			else {
				// Prefer patterns that bind variables from other tuple expressions
				int foreignVarFreq = getForeignVarFreq(unboundVars, varFreqMap);
				if (foreignVarFreq > 0) {
					cardinality /= foreignVarFreq;
				}
			}

			// Prefer patterns that bind more variables
			// List<Var> distinctUnboundVars = getUnboundVars(new
			// HashSet<Var>(vars));
			// if (distinctUnboundVars.size() >= 2) {
			// cardinality /= distinctUnboundVars.size();
			// }

			return cardinality;
		}

		protected List<Var> getConstantVars(Iterable<Var> vars) {
			List<Var> constantVars = new ArrayList<Var>();

			for (Var var : vars) {
				if (var.hasValue()) {
					constantVars.add(var);
				}
			}

			return constantVars;
		}

		protected List<Var> getUnboundVars(Iterable<Var> vars) {
			List<Var> unboundVars = new ArrayList<Var>();

			for (Var var : vars) {
				if (!var.hasValue() && !this.boundVars.contains(var.getName())) {
					unboundVars.add(var);
				}
			}

			return unboundVars;
		}

		protected int getForeignVarFreq(List<Var> ownUnboundVars, Map<Var, Integer> varFreqMap) {
			int result = 0;

			Map<Var, Integer> ownFreqMap = getVarFreqMap(ownUnboundVars, new HashMap<Var, Integer>());

			for (Map.Entry<Var, Integer> entry : ownFreqMap.entrySet()) {
				Var var = entry.getKey();
				int ownFreq = entry.getValue();
				result += varFreqMap.get(var) - ownFreq;
			}

			return result;
		}
	}
}
