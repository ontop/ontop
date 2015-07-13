package org.semanticweb.ontop.owlrefplatform.core.translator;

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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.DataTypePredicate;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.DatatypeFactory;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import org.semanticweb.ontop.model.Substitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.SubstitutionUtilities;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.DataNode;
import org.semanticweb.ontop.pivotalrepr.FilterNode;
import org.semanticweb.ontop.pivotalrepr.GroupNode;
import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.JoinLikeNode;
import org.semanticweb.ontop.pivotalrepr.LeftJoinNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.UnionNode;
import org.semanticweb.ontop.pivotalrepr.impl.VariableCollector;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

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
	
	private final static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();



	

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(IntermediateQueryToDatalogTranslator.class);

	public static DatalogProgram translate(IntermediateQuery te) {
		

		//log.debug("SPARQL algebra: \n{}", te);
		DatalogProgram result = ofac.getDatalogProgram();

		// Render the variable names in the signature into Variable object
		
		ConstructionNode root = te.getRootConstructionNode();
		List<ConstructionNode> rulesToDO = new LinkedList<ConstructionNode>();
		translate(te, root, result, rulesToDO);
		
		for (ConstructionNode node: rulesToDO){
			translate(te,  node, result, rulesToDO);
		}
		
		return result;
	}
	
	/**
	 * Translate a given IntermediateQuery query object to datalog program.
	 * 
	 *           
	 * @return Datalog program that represents the construction of the SPARQL
	 *         query.
	 */
	private static void translate(IntermediateQuery te, ConstructionNode root ,  DatalogProgram pr, List<ConstructionNode> rulesToDO  ) {
		
		DataAtom head= root.getProjectionAtom();
		List<QueryNode> listNodes=  te.getCurrentSubNodesOf(root);
		
		List<Function> atoms = new LinkedList<Function>();
		
		CQIE newrule = ofac.getCQIE(head, atoms);
		pr.appendRule(newrule);
		
		for (QueryNode node: listNodes){
			
			
			if (node instanceof ConstructionNode) {
				((ConstructionNode) node).getProjectionAtom();
			}

			if (node instanceof DataNode) {

			}
			
			if (node instanceof FilterNode) {
				translate(te, (FilterNode) node, newrule, pr, rulesToDO);
						
			}
	
			if (node instanceof InnerJoinNode) {
				translate(te, ((InnerJoinNode) node), newrule, pr, rulesToDO);
			}
			
			if (node instanceof LeftJoinNode) {
				translate(te, ((LeftJoinNode) node), newrule, pr, rulesToDO);
			}
			
			if (node instanceof UnionNode) {
				//TODO
			}
			if (node instanceof GroupNode) {
				//TODO
			}	
			

		}
		
		
	}

	/**
	 * Translates Filter atoms and add them to the rule of the program
	 * 
	 * @param te
	 * @param node
	 * @param newrule
	 * @param pr
	 * @param rulesToDO
	 */
	private static void translate(IntermediateQuery te, FilterNode node, CQIE newrule, DatalogProgram pr , List<ConstructionNode> rulesToDO  ) {
		Function filter = ((FilterNode) node).getFilterCondition();
		List<QueryNode> listnode =  te.getCurrentSubNodesOf(node);
		Function atom = getAtomFrom(te, listnode.get(0), rulesToDO);
		Function newJ = ofac.getSPARQLJoin(atom,filter);
		newrule.getBody().add(newJ);	
	}
		
	/**
	 * Translates LJ atoms and add them to the rule of the program
	 * 
	 * @param te
	 * @param node
	 * @param newrule
	 * @param pr
	 * @param rulesToDO
	 */
	private static void translate(IntermediateQuery te, LeftJoinNode node, CQIE newrule, DatalogProgram pr , List<ConstructionNode> rulesToDO  ) {

		Optional<ImmutableBooleanExpression> filter = node.getOptionalFilterCondition();
		List<QueryNode> listnode =  te.getCurrentSubNodesOf(node);
		List<Function> atoms = new LinkedList<Function>();
		
		for (QueryNode childnode: listnode) {
			Function atom = getAtomFrom(te, childnode, rulesToDO);
			atoms.add(atom);
		}
		
		if (filter.isPresent()){
			Function newLJAtom = ofac.getSPARQLLeftJoin(atoms.get(0), atoms.get(1), filter.get());
			newrule.getBody().add(newLJAtom);	
		}else{
			Function newLJAtom = ofac.getSPARQLLeftJoin(atoms.get(0), atoms.get(1));
			newrule.getBody().add(newLJAtom);	
		}
	
	}

	/**
	 * Translates Join atoms and add them to the rule of the program
	 * 
	 * @param te
	 * @param node
	 * @param newrule
	 * @param pr
	 * @param rulesToDO
	 */
	private static void translate(IntermediateQuery te, InnerJoinNode node, CQIE newrule,DatalogProgram pr, List<ConstructionNode> rulesToDO  ) {
		
		Optional<ImmutableBooleanExpression> filter = node.getOptionalFilterCondition();
		
		List<QueryNode> listnode =  te.getCurrentSubNodesOf(node);
		
		List<Function> atoms = new LinkedList<Function>();
		
		for (QueryNode childnode: listnode) {
			Function atom = getAtomFrom(te, childnode,rulesToDO);
			atoms.add(atom);
		}
		
		if (filter.isPresent()){
			Function newJAtom =  ofac.getSPARQLJoin(atoms, filter.get());
			newrule.getBody().add(newJAtom);
		}else{
			Function newJAtom =  ofac.getSPARQLJoin(atoms);
			newrule.getBody().add(newJAtom);
		}
		
		
		
	}

	private static Function getAtomFrom(IntermediateQuery te, QueryNode node,  List<ConstructionNode> rulesToDO  ) {
		
		if (node instanceof ConstructionNode) {
			Function newAns = ((ConstructionNode) node).getProjectionAtom();
			rulesToDO.add((ConstructionNode)node);
			return newAns;
		}

		if (node instanceof FilterNode) {
			Function filter = ((FilterNode) node).getFilterCondition();
			List<QueryNode> listnode =  te.getCurrentSubNodesOf(node);
			Function atom = getAtomFrom(te, listnode.get(0), rulesToDO);
			Function newJ = ofac.getSPARQLJoin(atom,filter);
			return newJ;
					
		}
		
		if (node instanceof DataNode) {
			return ((DataNode)node).getAtom();
		}
		
		if (node instanceof InnerJoinNode) {
			Optional<ImmutableBooleanExpression> filter = ((InnerJoinNode)node).getOptionalFilterCondition();
			List<Function> atoms = new LinkedList<Function>();
			List<QueryNode> listnode =  te.getCurrentSubNodesOf(node);
			for (QueryNode childnode: listnode) {
				Function atom = getAtomFrom(te, childnode, rulesToDO);
				atoms.add(atom);
			}
			
			
		if (filter.isPresent()){
			Function newJ = ofac.getSPARQLJoin(atoms, filter.get());
			return newJ;
		}else{
			Function newJ = ofac.getSPARQLJoin(atoms);
			return newJ;	
		}
		
		
		}
		
		if (node instanceof LeftJoinNode) {
			Optional<ImmutableBooleanExpression> filter = ((LeftJoinNode)node).getOptionalFilterCondition();
			List<Function> atoms = new LinkedList<Function>();
			List<QueryNode> listnode =  te.getCurrentSubNodesOf(node);
			for (QueryNode childnode: listnode) {
				Function atom = getAtomFrom(te, childnode, rulesToDO);
				atoms.add(atom);
			}
			
			if (filter.isPresent()){
				Function newLJ = ofac.getSPARQLLeftJoin(atoms.get(0), atoms.get(1), filter.get());
				return newLJ;
			}else{
				Function newLJ = ofac.getSPARQLLeftJoin(atoms.get(0), atoms.get(1));
				return newLJ;	
			}
			
			
		}
		
		if (node instanceof UnionNode) {
			
		}
	
		return null;
	}

	
	
}
