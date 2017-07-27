package it.unibz.inf.ontop.owlrefplatform.core.translator;

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
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogProgram;
import it.unibz.inf.ontop.datalog.MutableQueryModifiers;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.impl.MutableQueryModifiersImpl;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.slf4j.LoggerFactory;

import java.util.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.term.impl.ImmutabilityTools.convertToMutableFunction;

/***
 * Translate a intermediate queries expression into a Datalog program that has the
 * same semantics. We use the built-int predicates Join and Left join. The rules
 * in the program have always 1 or 2 operator atoms, plus (in)equality atoms
 * (due to filters).
 * 
 * 
 * @author mrezk
 */
public class IntermediateQueryToDatalogTranslator {

	private static final String SUBQUERY_PRED_PREFIX = "ansSQ";
	private final IntermediateQueryFactory iqFactory;

	private static class RuleHead {
		public final ImmutableSubstitution<ImmutableTerm> substitution;
		public final DataAtom atom;
		public final Optional<QueryNode> optionalChildNode;

		private RuleHead(ImmutableSubstitution<ImmutableTerm> substitution, DataAtom atom, Optional<QueryNode> optionalChildNode) {
			this.atom = atom;
            this.substitution = substitution;
            this.optionalChildNode = optionalChildNode;
        }
	}
	
	//private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(IntermediateQueryToDatalogTranslator.class);

	// Incremented
	private int subQueryCounter;

	private IntermediateQueryToDatalogTranslator(IntermediateQueryFactory iqFactory) {
		this.iqFactory = iqFactory;
		subQueryCounter = 0;
	}

	private IntermediateQueryToDatalogTranslator(IntermediateQueryFactory iqFactory, int subQueryCounter) {
		this.iqFactory = iqFactory;
		subQueryCounter = subQueryCounter;
	}

	/**
	 * Translate an intermediate query tree into a Datalog program 
	 * 
	 */
	public static DatalogProgram translate(IntermediateQuery query) {
		IntermediateQueryToDatalogTranslator translator = new IntermediateQueryToDatalogTranslator(query.getFactory());
		return translator.translateQuery(query);
	}

	/**
	 * Translate an intermediate query tree into a Datalog program.
	 *
	 * Each (strict) subquery will be translated as a rule with head Pred(var_1, .., var_n),
	 * where the string for Pred is of the form SUBQUERY_PRED_PREFIX + y,
	 * with y > subqueryCounter.
	 */
	public static DatalogProgram translate(IntermediateQuery query, int subQueryCounter) {
		IntermediateQueryToDatalogTranslator translator = new IntermediateQueryToDatalogTranslator(query.getFactory(),
				subQueryCounter);
		return translator.translateQuery(query);
	}

	private DatalogProgram translateQuery(IntermediateQuery query) {
		ConstructionNode root = query.getRootConstructionNode();
		
		Optional<ImmutableQueryModifiers> optionalModifiers =  root.getOptionalModifiers();

        DatalogProgram dProgram;
		if (optionalModifiers.isPresent()){
			QueryModifiers immutableQueryModifiers = optionalModifiers.get();

			// Mutable modifiers (used by the Datalog)
			MutableQueryModifiers mutableModifiers = new MutableQueryModifiersImpl(immutableQueryModifiers);
			// TODO: support GROUP BY (distinct QueryNode)

            dProgram = DATALOG_FACTORY.getDatalogProgram(mutableModifiers);
		}
        else {
            dProgram = DATALOG_FACTORY.getDatalogProgram();
        }

		translate(query,  dProgram, root);
		
	
		
		
		return dProgram;
	}
	
	/**
	 * Translate a given IntermediateQuery query object to datalog program.
	 * 
	 *           
	 * @return Datalog program that represents the construction of the SPARQL
	 *         query.
	 */
	private void translate(IntermediateQuery query, DatalogProgram pr, ConstructionNode root) {

		Queue<RuleHead> heads = new LinkedList<>();
		heads.add(new RuleHead(root.getSubstitution(), query.getProjectionAtom(),query.getFirstChild(root)));

		// Mutable (append-only)
		Map<ConstructionNode, DataAtom> subQueryProjectionAtoms = new HashMap<>();
		subQueryProjectionAtoms.put(root, query.getProjectionAtom());

		//In heads we keep the heads of the sub-rules in the program, e.g. ans5() :- LeftJoin(....)
		while(!heads.isEmpty()) {

			RuleHead head = heads.poll();

			//Applying substitutions in the head.
			ImmutableFunctionalTerm substitutedHeadAtom = head.substitution.applyToFunctionalTerm(
					head.atom);

			List<Function> atoms = new LinkedList<>();

			//Constructing the rule
			CQIE newrule = DATALOG_FACTORY.getCQIE(convertToMutableFunction(substitutedHeadAtom), atoms);

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
									   Map<ConstructionNode, DataAtom> subQueryProjectionAtoms,
									   boolean isNested) {
		
		List<Function> body = new ArrayList<>();
		
		/**
		 * Basic Atoms
		 */
		
		if (node instanceof ConstructionNode) {
			ConstructionNode constructionNode = (ConstructionNode) node;
			DataAtom projectionAtom = Optional.ofNullable(
					subQueryProjectionAtoms.get(constructionNode))
					//.map(atom -> adaptProjectionAtom(atom, constructionNode))
					.orElseGet(() -> generateProjectionAtom(constructionNode.getVariables()));

			heads.add(new RuleHead(constructionNode.getSubstitution(), projectionAtom,te.getFirstChild(constructionNode)));
			subQueryProjectionAtoms.put(constructionNode, projectionAtom);
			Function mutAt = convertToMutableFunction(projectionAtom);
			body.add(mutAt);
			return body;
			
		} else if (node instanceof FilterNode) {
			ImmutableExpression filter = ((FilterNode) node).getFilterCondition();
			List<QueryNode> listnode =  te.getChildren(node);
			body.addAll(getAtomFrom(te, listnode.get(0), heads, subQueryProjectionAtoms, true));

			filter.flattenAND().stream()
					.map(ImmutabilityTools::convertToMutableBooleanExpression)
					.forEach(body::add);

			return body;
			
					
		} else if (node instanceof DataNode) {
			DataAtom atom = ((DataNode)node).getProjectionAtom();
			Function mutAt = convertToMutableFunction(atom);
			body.add(mutAt);
			return body;
				
			
			
		/**
		 * Nested Atoms	
		 */
		} else  if (node instanceof InnerJoinNode) {
			return getAtomsFromJoinNode((InnerJoinNode)node, te, heads, subQueryProjectionAtoms, isNested);
			
		} else if (node instanceof LeftJoinNode) {
			Optional<ImmutableExpression> filter = ((LeftJoinNode)node).getOptionalFilterCondition();
			List<QueryNode> listnode =  te.getChildren(node);

			List<Function> atomsListLeft = getAtomFrom(te, listnode.get(0), heads, subQueryProjectionAtoms, true);
			List<Function> atomsListRight = getAtomFrom(te, listnode.get(1), heads, subQueryProjectionAtoms, true);
				
			if (filter.isPresent()){
				ImmutableExpression filter2 = filter.get();
				Expression mutFilter =  ImmutabilityTools.convertToMutableBooleanExpression(filter2);
				Function newLJAtom = DATALOG_FACTORY.getSPARQLLeftJoin(atomsListLeft, atomsListRight, Optional.of(mutFilter));
				body.add(newLJAtom);
				return body;
			}else{
				Function newLJAtom = DATALOG_FACTORY.getSPARQLLeftJoin(atomsListLeft, atomsListRight, Optional.empty());
				body.add(newLJAtom);
				return body;
			}

		} else if (node instanceof UnionNode) {

			Optional<ConstructionNode> parentNode = te.getParent(node)
					.filter(p -> p instanceof ConstructionNode)
					.map(p -> (ConstructionNode) p);

			DistinctVariableOnlyDataAtom freshHeadAtom;
			if(parentNode.isPresent()) {
				freshHeadAtom = generateProjectionAtom(parentNode.get().getChildVariables());
			}
			else{
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

			Function bodyAtom = convertToMutableFunction(freshHeadAtom);
			body.add(bodyAtom);
			return body;

		} else if (node instanceof TrueNode) {

			/**
			 *
			 * TODO: what should we do when it is the left child of a LJ?
             *
			 * Add a 0-ary atom
			 */
			//DataAtom projectionAtom = generateProjectionAtom(ImmutableSet.of());
			//heads.add(new RuleHead(new ImmutableSubstitutionImpl<>(ImmutableMap.of()), projectionAtom,Optional.empty()));
			//return body;
			body.add(ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getAtomPredicate("dummy", 0), ImmutableList.of()));
			return body;

		} else {
			 throw new UnsupportedOperationException("Type of node in the intermediate tree is unknown!!");
		}

	}

	private List<Function> getAtomsFromJoinNode(InnerJoinNode node, IntermediateQuery te, Queue<RuleHead> heads,
												Map<ConstructionNode, DataAtom> subQueryProjectionAtoms,
												boolean isNested) {
		List<Function> body = new ArrayList<>();
		Optional<ImmutableExpression> filter = node.getOptionalFilterCondition();
		List<Function> atoms = new ArrayList<>();
		List<QueryNode> listnode =  te.getChildren(node);
		for (QueryNode childnode: listnode) {
			List<Function> atomsList = getAtomFrom(te, childnode, heads, subQueryProjectionAtoms, true);
			atoms.addAll(atomsList);
		}

		if (atoms.size() <= 1) {
			throw new IllegalArgumentException("Inconsistent IQ: an InnerJoinNode must have at least two children");
		}

		if (filter.isPresent()){
			if (isNested) {
				ImmutableExpression filter2 = filter.get();
				Function mutFilter = ImmutabilityTools.convertToMutableBooleanExpression(filter2);
				Function newJ = getSPARQLJoin(atoms, Optional.of(mutFilter));
				body.add(newJ);
				return body;
			}
			else {
				body.addAll(atoms);
				filter.get().flattenAND().stream()
						.map(ImmutabilityTools::convertToMutableBooleanExpression)
						.forEach(body::add);
				return body;
			}
		}else{
			Function newJ = getSPARQLJoin(atoms, Optional.empty());
			body.add(newJ);
			return body;
		}
	}

	private DistinctVariableOnlyDataAtom generateProjectionAtom(ImmutableSet<Variable> projectedVariables) {
		AtomPredicate newPredicate = ATOM_FACTORY.getAtomPredicate(SUBQUERY_PRED_PREFIX+ ++subQueryCounter,
				projectedVariables.size());
		return ATOM_FACTORY.getDistinctVariableOnlyDataAtom(newPredicate, ImmutableList.copyOf(projectedVariables));
	}

	private static Function getSPARQLJoin(List<Function> atoms, Optional<Function> optionalCondition) {
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
				? DATALOG_FACTORY.getSPARQLJoin(atoms.get(0), rightTerm, optionalCondition.get())
				: DATALOG_FACTORY.getSPARQLJoin(atoms.get(0), rightTerm);
	}

}
