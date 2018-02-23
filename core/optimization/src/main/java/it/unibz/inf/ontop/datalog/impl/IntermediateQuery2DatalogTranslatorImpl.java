package it.unibz.inf.ontop.datalog.impl;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.exception.DatalogConversionException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.RootConstructionNodeEnforcer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.impl.MutableQueryModifiersImpl;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

/***
 * Translate a intermediate queries expression into a Datalog program that has the
 * same semantics. We use the built-int predicates Join and Left join. The rules
 * in the program have always 1 or 2 operator atoms, plus (in)equality atoms
 * (due to filters).
 * 
 * 
 * @author mrezk
 */
public class IntermediateQuery2DatalogTranslatorImpl implements IntermediateQuery2DatalogTranslator {

	private final IntermediateQueryFactory iqFactory;
	private final RootConstructionNodeEnforcer rootCnEnforcer;
	private final AtomFactory atomFactory;
	private final SubstitutionFactory substitutionFactory;
	private final DatalogFactory datalogFactory;
	private final ImmutabilityTools immutabilityTools;

	private static class RuleHead {
		private final ImmutableSubstitution<ImmutableTerm> substitution;
		private final DataAtom atom;
		private final Optional<QueryNode> optionalChildNode;

		private RuleHead(ImmutableSubstitution<ImmutableTerm> substitution, DataAtom atom, Optional<QueryNode> optionalChildNode) {
			this.atom = atom;
			this.substitution = substitution;
			this.optionalChildNode = optionalChildNode;
		}
	}


	private static final Logger LOG = LoggerFactory.getLogger(IntermediateQuery2DatalogTranslatorImpl.class);

	// Incremented
	private int subQueryCounter;
	private int dummyPredCounter;

	@Inject
	private IntermediateQuery2DatalogTranslatorImpl(IntermediateQueryFactory iqFactory, RootConstructionNodeEnforcer rootCnEnforcer,
                                                    AtomFactory atomFactory,
													SubstitutionFactory substitutionFactory, DatalogFactory datalogFactory,
													ImmutabilityTools immutabilityTools) {
		this.iqFactory = iqFactory;
		this.rootCnEnforcer = rootCnEnforcer;
		this.atomFactory = atomFactory;
		this.substitutionFactory = substitutionFactory;
		this.datalogFactory = datalogFactory;
		this.immutabilityTools = immutabilityTools;
		this.subQueryCounter = 0;
		this.dummyPredCounter = 0;
	}

	/**
	 * Translate an intermediate query tree into a Datalog program.
	 * <p>
	 * Each (strict) subquery will be translated as a rule with head Pred(var_1, .., var_n),
	 * where the string for Pred is of the form SUBQUERY_PRED_PREFIX + y,
	 * with y > subqueryCounter.
	 */
	@Override
	public DatalogProgram translate(IntermediateQuery query) {

		Optional<ImmutableQueryModifiers> optionalModifiers =  Optional.of(query.getRootNode())
				.filter(r -> r instanceof ConstructionNode)
				.map(r -> (ConstructionNode)r)
				.flatMap(ConstructionNode::getOptionalModifiers);

		// Mutable
        DatalogProgram dProgram;
		if (optionalModifiers.isPresent()) {
			MutableQueryModifiers mutableModifiers = new MutableQueryModifiersImpl(optionalModifiers.get());
			dProgram = datalogFactory.getDatalogProgram(mutableModifiers);
		} else {
			dProgram = datalogFactory.getDatalogProgram();
		}

		normalizeIQ(query)
				.forEach(q -> translate(q, dProgram));

		return dProgram;
	}

	/**
	 * Translate a given IntermediateQuery query object to datalog program.
	 * Note that (the object ref of the) datalog program is passed as argument
	 * <p>
	 * Assumption: the root is a construction node
	 */
	private void translate(IntermediateQuery query, DatalogProgram pr) {
		QueryNode root = query.getRootNode();
		if (!(root instanceof ConstructionNode)) {
			throw new DatalogConversionException("the root is expected to be a Construction Node");
		}

		ConstructionNode rootCn = (ConstructionNode) root;
		Queue<RuleHead> heads = new LinkedList<>();

		ImmutableSubstitution<ImmutableTerm> topSubstitution = Optional.of(rootCn)
				.map(ConstructionNode::getSubstitution)
				.orElseGet(substitutionFactory::getSubstitution);

		heads.add(new RuleHead(topSubstitution, query.getProjectionAtom(), query.getFirstChild(root)));

		// Mutable (append-only)
		Map<QueryNode, DataAtom> subQueryProjectionAtoms = new HashMap<>();
		subQueryProjectionAtoms.put(root, query.getProjectionAtom());

		//In heads we keep the heads of the sub-rules in the program, e.g. ans5() :- LeftJoin(....)
		while (!heads.isEmpty()) {

			RuleHead head = heads.poll();

			//Applying substitutions in the head.
			ImmutableFunctionalTerm substitutedHeadAtom = head.substitution.applyToFunctionalTerm(
					head.atom);

			List<Function> atoms = new LinkedList<>();

			//Constructing the rule
			CQIE newrule = datalogFactory.getCQIE(immutabilityTools.convertToMutableFunction(substitutedHeadAtom), atoms);

			pr.appendRule(newrule);

			head.optionalChildNode.ifPresent(node -> {
				List<Function> uAtoms = getAtomFrom(query, node, heads, subQueryProjectionAtoms, false);
				newrule.getBody().addAll(uAtoms);
			});

		}
	}


	/**
	 * This is the MAIN recursive method in this class!!
	 * Takes a node and return the list of functions (atoms) that it represents.
	 * Usually it will be a single atom, but it is different for the filter case.
	 */
	private List<Function> getAtomFrom(IntermediateQuery te, QueryNode node, Queue<RuleHead> heads,
									   Map<QueryNode, DataAtom> subQueryProjectionAtoms,
									   boolean isNested) {

		List<Function> body = new ArrayList<>();

		 /*
		  Basic Atoms
		 */
		if (node instanceof ConstructionNode) {
			ConstructionNode constructionNode = (ConstructionNode) node;
			DataAtom projectionAtom = Optional.ofNullable(
					subQueryProjectionAtoms.get(constructionNode))
					.orElseGet(() -> generateProjectionAtom(constructionNode.getVariables()));

			heads.add(new RuleHead(constructionNode.getSubstitution(), projectionAtom, te.getFirstChild(constructionNode)));
			subQueryProjectionAtoms.put(constructionNode, projectionAtom);
			Function mutAt = immutabilityTools.convertToMutableFunction(projectionAtom);
			body.add(mutAt);
			return body;

		} else if (node instanceof FilterNode) {
			ImmutableExpression filter = ((FilterNode) node).getFilterCondition();
			List<QueryNode> listnode = te.getChildren(node);
			body.addAll(getAtomFrom(te, listnode.get(0), heads, subQueryProjectionAtoms, true));

			filter.flattenAND().stream()
					.map(immutabilityTools::convertToMutableBooleanExpression)
					.forEach(body::add);

			return body;


		} else if (node instanceof DataNode) {
			DataAtom atom = ((DataNode)node).getProjectionAtom();
			Function mutAt = immutabilityTools.convertToMutableFunction(atom);
			body.add(mutAt);
			return body;


			/*
			  Nested Atoms
			 */
		} else if (node instanceof InnerJoinNode) {
			return getAtomsFromJoinNode((InnerJoinNode) node, te, heads, subQueryProjectionAtoms, isNested);

		} else if (node instanceof LeftJoinNode) {
			Optional<ImmutableExpression> filter = ((LeftJoinNode) node).getOptionalFilterCondition();
			List<QueryNode> listnode = te.getChildren(node);

			List<Function> atomsListLeft = getAtomFrom(te, listnode.get(0), heads, subQueryProjectionAtoms, true);
			List<Function> atomsListRight = getAtomFrom(te, listnode.get(1), heads, subQueryProjectionAtoms, true);

			if (filter.isPresent()) {
				ImmutableExpression filter2 = filter.get();
				Expression mutFilter = immutabilityTools.convertToMutableBooleanExpression(filter2);
				Function newLJAtom = datalogFactory.getSPARQLLeftJoin(atomsListLeft, atomsListRight, Optional.of(mutFilter));
				body.add(newLJAtom);
				return body;
			} else {
				Function newLJAtom = datalogFactory.getSPARQLLeftJoin(atomsListLeft, atomsListRight, Optional.empty());
				body.add(newLJAtom);
				return body;
			}

		} else if (node instanceof UnionNode) {

			Optional<ConstructionNode> parentNode = te.getParent(node)
					.filter(p -> p instanceof ConstructionNode)
					.map(p -> (ConstructionNode) p);

			DistinctVariableOnlyDataAtom freshHeadAtom;
			if (parentNode.isPresent()) {
				freshHeadAtom = generateProjectionAtom(parentNode.get().getChildVariables());
			} else {
				freshHeadAtom = generateProjectionAtom(((UnionNode) node).getVariables());
			}


			for (QueryNode child : te.getChildren(node)) {

				if (child instanceof ConstructionNode) {
					ConstructionNode cn = (ConstructionNode) child;
					Optional<QueryNode> grandChild = te.getFirstChild(cn);
					subQueryProjectionAtoms.put(cn, freshHeadAtom);
					heads.add(new RuleHead(cn.getSubstitution(), freshHeadAtom, grandChild));
				} else {
					ConstructionNode cn = iqFactory.createConstructionNode(((UnionNode) node).getVariables());
					subQueryProjectionAtoms.put(cn, freshHeadAtom);
					heads.add(new RuleHead(cn.getSubstitution(), freshHeadAtom, Optional.ofNullable(child)));
				}


			} //end for

			Function bodyAtom = immutabilityTools.convertToMutableFunction(freshHeadAtom);
			body.add(bodyAtom);
			return body;

		} else if (node instanceof TrueNode) {

			/*
			  TODO: what should we do when it is the left child of a LJ?
			  Add a 0-ary atom
			 */
			if (isNested) {
				body.add(atomFactory.getDistinctVariableOnlyDataAtom(
						atomFactory.getAtomPredicate(
								"dummy" + (++dummyPredCounter),
								0
						),
						ImmutableList.of()
				));
			}
			// Otherwise, ignores it
			return body;

		} else {
			throw new UnsupportedOperationException("Unexpected type of node in the intermediate tree: " + node);
		}

	}

	private List<Function> getAtomsFromJoinNode(InnerJoinNode node, IntermediateQuery te, Queue<RuleHead> heads,
												Map<QueryNode, DataAtom> subQueryProjectionAtoms,
												boolean isNested) {
		List<Function> body = new ArrayList<>();
		Optional<ImmutableExpression> filter = node.getOptionalFilterCondition();
		List<Function> atoms = new ArrayList<>();
		List<QueryNode> listnode = te.getChildren(node);
		for (QueryNode childnode : listnode) {
			List<Function> atomsList = getAtomFrom(te, childnode, heads, subQueryProjectionAtoms, true);
			atoms.addAll(atomsList);
		}

		if (atoms.size() <= 1) {
			throw new IllegalArgumentException("Inconsistent IQ: an InnerJoinNode must have at least two children");
		}

		if (filter.isPresent()) {
			if (isNested) {
				ImmutableExpression filter2 = filter.get();
				Function mutFilter = immutabilityTools.convertToMutableBooleanExpression(filter2);
				Function newJ = getSPARQLJoin(atoms, Optional.of(mutFilter));
				body.add(newJ);
				return body;
			} else {
				body.addAll(atoms);
				filter.get().flattenAND().stream()
						.map(immutabilityTools::convertToMutableBooleanExpression)
						.forEach(body::add);
				return body;
			}
		} else {
			Function newJ = getSPARQLJoin(atoms, Optional.empty());
			body.add(newJ);
			return body;
		}
	}

	private DistinctVariableOnlyDataAtom generateProjectionAtom(ImmutableSet<Variable> projectedVariables) {
		AtomPredicate newPredicate = atomFactory.getAtomPredicate(datalogFactory.getSubqueryPredicatePrefix()+ ++subQueryCounter,
				projectedVariables.size());
		return atomFactory.getDistinctVariableOnlyDataAtom(newPredicate, ImmutableList.copyOf(projectedVariables));
	}

	private Function getSPARQLJoin(List<Function> atoms, Optional<Function> optionalCondition) {
		int atomCount = atoms.size();
		Function rightTerm;

		switch (atomCount) {
			case 0:
			case 1:
				throw new IllegalArgumentException("A join requires at least two atoms");
			case 2:
				rightTerm = atoms.get(1);
				break;
			default:
				rightTerm = getSPARQLJoin(atoms.subList(1, atomCount), Optional.empty());
				break;
		}

		return optionalCondition.isPresent()
				? datalogFactory.getSPARQLJoin(atoms.get(0), rightTerm, optionalCondition.get())
				: datalogFactory.getSPARQLJoin(atoms.get(0), rightTerm);
	}

	private ImmutableList<IntermediateQuery> normalizeIQ(IntermediateQuery query) {
		return splitRootUnion(query)
				.map(rootCnEnforcer::enforceRootCn)
				.collect(ImmutableCollectors.toList());
	}

	private Stream<IntermediateQuery> splitRootUnion(IntermediateQuery query) {
	    QueryNode root = query.getRootNode();
		return (root instanceof UnionNode)?
				query.getChildren(root).stream()
						.map(n -> query.getSubquery(
								n,
								query.getProjectionAtom()
						)):
				Stream.of(query);
	}
}

