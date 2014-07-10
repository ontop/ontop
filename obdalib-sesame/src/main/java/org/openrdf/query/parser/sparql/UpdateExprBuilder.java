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
package org.openrdf.query.parser.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.Add;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.Clear;
import org.openrdf.query.algebra.Copy;
import org.openrdf.query.algebra.Create;
import org.openrdf.query.algebra.DeleteData;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.InsertData;
import org.openrdf.query.algebra.Load;
import org.openrdf.query.algebra.Modify;
import org.openrdf.query.algebra.Move;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.parser.sparql.ast.ASTAdd;
import org.openrdf.query.parser.sparql.ast.ASTClear;
import org.openrdf.query.parser.sparql.ast.ASTCopy;
import org.openrdf.query.parser.sparql.ast.ASTCreate;
import org.openrdf.query.parser.sparql.ast.ASTDeleteClause;
import org.openrdf.query.parser.sparql.ast.ASTDeleteData;
import org.openrdf.query.parser.sparql.ast.ASTDeleteWhere;
import org.openrdf.query.parser.sparql.ast.ASTDrop;
import org.openrdf.query.parser.sparql.ast.ASTGraphOrDefault;
import org.openrdf.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.openrdf.query.parser.sparql.ast.ASTGraphRefAll;
import org.openrdf.query.parser.sparql.ast.ASTIRI;
import org.openrdf.query.parser.sparql.ast.ASTInsertClause;
import org.openrdf.query.parser.sparql.ast.ASTInsertData;
import org.openrdf.query.parser.sparql.ast.ASTLoad;
import org.openrdf.query.parser.sparql.ast.ASTModify;
import org.openrdf.query.parser.sparql.ast.ASTMove;
import org.openrdf.query.parser.sparql.ast.ASTQuadsNotTriples;
import org.openrdf.query.parser.sparql.ast.ASTUpdate;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * Extension of TupleExprBuilder that builds Update Expressions.
 * 
 * @author Jeen Broekstra
 */
public class UpdateExprBuilder extends TupleExprBuilder {

	/**
	 * @param valueFactory
	 */
	public UpdateExprBuilder(ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public UpdateExpr visit(ASTUpdate node, Object data)
		throws VisitorException
	{
		if (node instanceof ASTModify) {
			return this.visit((ASTModify)node, data);
		}
		else if (node instanceof ASTInsertData) {
			return this.visit((ASTInsertData)node, data);
		}

		return null;
	}

	@Override
	public InsertData visit(ASTInsertData node, Object data)
		throws VisitorException
	{

		TupleExpr result = new SingletonSet();

		// Collect insert triples
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern();

		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());

		if (node.jjtGetNumChildren() > 0) {

			Object algebraExpr = node.jjtGetChild(0).jjtAccept(this, data);

			if (algebraExpr instanceof ValueExpr) { // named graph identifier
				Var contextVar = mapValueExprToVar((ValueExpr)algebraExpr);
				graphPattern.setContextVar(contextVar);
				graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);
			}

			for (int i = 1; i < node.jjtGetNumChildren(); i++) {
				node.jjtGetChild(i).jjtAccept(this, data);
			}

		}
		TupleExpr insertExpr = graphPattern.buildTupleExpr();

		VarCollector collector = new VarCollector();
		insertExpr.visit(collector);
		for (Var var : collector.getCollectedVars()) {
			if (!var.hasValue() && !var.isAnonymous()) {
				// non-anonymous var in insert data not allowed
				throw new VisitorException("INSERT DATA may not contain variables");
			}
		}

		graphPattern = parentGP;

		// Retrieve all StatementPatterns from the insert expression
		List<StatementPattern> statementPatterns = StatementPatternCollector.process(insertExpr);

		Set<Var> projectionVars = getProjectionVars(statementPatterns);

		// Create BNodeGenerators for all anonymous variables
		Map<Var, ExtensionElem> extElemMap = new HashMap<Var, ExtensionElem>();

		for (Var var : projectionVars) {
			if (var.isAnonymous() && !extElemMap.containsKey(var)) {
				ValueExpr valueExpr;

				if (var.hasValue()) {
					valueExpr = new ValueConstant(var.getValue());
				}
				else {
					valueExpr = new BNodeGenerator();
				}

				extElemMap.put(var, new ExtensionElem(valueExpr, var.getName()));
			}
		}

		if (!extElemMap.isEmpty()) {
			result = new Extension(result, extElemMap.values());
		}

		// Create a Projection for each StatementPattern in the clause
		List<ProjectionElemList> projList = new ArrayList<ProjectionElemList>();

		for (StatementPattern sp : statementPatterns) {
			ProjectionElemList projElemList = new ProjectionElemList();

			projElemList.addElement(new ProjectionElem(sp.getSubjectVar().getName(), "subject"));
			projElemList.addElement(new ProjectionElem(sp.getPredicateVar().getName(), "predicate"));
			projElemList.addElement(new ProjectionElem(sp.getObjectVar().getName(), "object"));

			if (sp.getContextVar() != null) {
				projElemList.addElement(new ProjectionElem(sp.getContextVar().getName(), "context"));
			}

			projList.add(projElemList);
		}

		if (projList.size() == 1) {
			result = new Projection(result, projList.get(0));
		}
		else if (projList.size() > 1) {
			result = new MultiProjection(result, projList);
		}
		else {
			// Empty constructor
			result = new EmptySet();
		}

		result = new Reduced(result);

		return new InsertData(result);
	}

	@Override
	public DeleteData visit(ASTDeleteData node, Object data)
		throws VisitorException
	{

		TupleExpr result = new SingletonSet();

		// Collect construct triples
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern();

		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());

		Object algebraExpr = node.jjtGetChild(0).jjtAccept(this, data);

		if (algebraExpr instanceof ValueExpr) { // named graph identifier
			Var contextVar = mapValueExprToVar((ValueExpr)algebraExpr);
			graphPattern.setContextVar(contextVar);
			graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);
		}

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		TupleExpr deleteExpr = graphPattern.buildTupleExpr();

		VarCollector collector = new VarCollector();
		deleteExpr.visit(collector);
		for (Var var : collector.getCollectedVars()) {
			if (!var.hasValue()) {
				if (var.isAnonymous()) {
					// blank node in delete data not allowed
					throw new VisitorException("DELETE DATA may not contain blank nodes");
				}
				else {
					// var in delete data not allowed
					throw new VisitorException("DELETE DATA may not contain variables");
				}
			}
		}

		graphPattern = parentGP;

		// Retrieve all StatementPatterns from the insert expression
		List<StatementPattern> statementPatterns = StatementPatternCollector.process(deleteExpr);

		Set<Var> projectionVars = getProjectionVars(statementPatterns);

		// Create BNodeGenerators for all anonymous variables
		Map<Var, ExtensionElem> extElemMap = new HashMap<Var, ExtensionElem>();

		for (Var var : projectionVars) {
			if (var.isAnonymous() && !extElemMap.containsKey(var)) {
				ValueExpr valueExpr;

				if (var.hasValue()) {
					valueExpr = new ValueConstant(var.getValue());
				}
				else {
					valueExpr = new BNodeGenerator();
				}

				extElemMap.put(var, new ExtensionElem(valueExpr, var.getName()));
			}
		}

		if (!extElemMap.isEmpty()) {
			result = new Extension(result, extElemMap.values());
		}

		// Create a Projection for each StatementPattern in the clause
		List<ProjectionElemList> projList = new ArrayList<ProjectionElemList>();

		for (StatementPattern sp : statementPatterns) {
			ProjectionElemList projElemList = new ProjectionElemList();

			projElemList.addElement(new ProjectionElem(sp.getSubjectVar().getName(), "subject"));
			projElemList.addElement(new ProjectionElem(sp.getPredicateVar().getName(), "predicate"));
			projElemList.addElement(new ProjectionElem(sp.getObjectVar().getName(), "object"));

			if (sp.getContextVar() != null) {
				projElemList.addElement(new ProjectionElem(sp.getContextVar().getName(), "context"));
			}

			projList.add(projElemList);
		}

		if (projList.size() == 1) {
			result = new Projection(result, projList.get(0));
		}
		else if (projList.size() > 1) {
			result = new MultiProjection(result, projList);
		}
		else {
			// Empty constructor
			result = new EmptySet();
		}

		result = new Reduced(result);

		return new DeleteData(result);
	}

	@Override
	public TupleExpr visit(ASTQuadsNotTriples node, Object data)
		throws VisitorException
	{
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern();

		ValueExpr contextNode = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);

		Var contextVar = mapValueExprToVar(contextNode);
		graphPattern.setContextVar(contextVar);
		graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		TupleExpr result = graphPattern.buildTupleExpr();
		parentGP.addRequiredTE(result);

		graphPattern = parentGP;

		return result;
	}

	@Override
	public Modify visit(ASTDeleteWhere node, Object data)
		throws VisitorException
	{
		// Collect delete clause triples
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern();

		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		TupleExpr whereExpr = graphPattern.buildTupleExpr();
		graphPattern = parentGP;

		TupleExpr deleteExpr = whereExpr.clone();

		// FIXME we should adapt the grammar so we can avoid doing this
		// post-processing.
		VarCollector collector = new VarCollector();
		deleteExpr.visit(collector);
		for (Var var : collector.getCollectedVars()) {
			if (var.isAnonymous() && !var.hasValue()) {
				throw new VisitorException("DELETE WHERE may not contain blank nodes");
			}
		}

		Modify modify = new Modify(deleteExpr, null, whereExpr);

		return modify;
	}

	@Override
	public Load visit(ASTLoad node, Object data)
		throws VisitorException
	{

		ValueConstant source = (ValueConstant)node.jjtGetChild(0).jjtAccept(this, data);

		Load load = new Load(source);
		load.setSilent(node.isSilent());
		if (node.jjtGetNumChildren() > 1) {
			ValueConstant graph = (ValueConstant)node.jjtGetChild(1).jjtAccept(this, data);
			load.setGraph(graph);
		}

		return load;
	}

	@Override
	public Clear visit(ASTClear node, Object data)
		throws VisitorException
	{
		Clear clear = new Clear();
		clear.setSilent(node.isSilent());

		ASTGraphRefAll graphRef = node.jjtGetChild(ASTGraphRefAll.class);

		if (graphRef.jjtGetNumChildren() > 0) {
			ValueConstant graph = (ValueConstant)graphRef.jjtGetChild(0).jjtAccept(this, data);
			clear.setGraph(graph);
		}
		else {
			if (graphRef.isDefault()) {
				clear.setScope(Scope.DEFAULT_CONTEXTS);
			}
			else if (graphRef.isNamed()) {
				clear.setScope(Scope.NAMED_CONTEXTS);
			}
		}
		return clear;
	}

	@Override
	public Clear visit(ASTDrop node, Object data)
		throws VisitorException
	{
		// implementing drop as a synonym of clear, in Sesame this is really the
		// same thing, as empty
		// graphs are not recorded.

		Clear clear = new Clear();
		clear.setSilent(node.isSilent());

		ASTGraphRefAll graphRef = node.jjtGetChild(ASTGraphRefAll.class);

		if (graphRef.jjtGetNumChildren() > 0) {
			ValueConstant graph = (ValueConstant)graphRef.jjtGetChild(0).jjtAccept(this, data);
			clear.setGraph(graph);
		}
		else {
			if (graphRef.isDefault()) {
				clear.setScope(Scope.DEFAULT_CONTEXTS);
			}
			else if (graphRef.isNamed()) {
				clear.setScope(Scope.NAMED_CONTEXTS);
			}
		}
		return clear;
	}

	@Override
	public Create visit(ASTCreate node, Object data)
		throws VisitorException
	{
		ValueConstant graph = (ValueConstant)node.jjtGetChild(0).jjtAccept(this, data);

		Create create = new Create(graph);
		create.setSilent(node.isSilent());
		return create;
	}

	@Override
	public Copy visit(ASTCopy node, Object data)
		throws VisitorException
	{
		Copy copy = new Copy();
		copy.setSilent(node.isSilent());

		ASTGraphOrDefault sourceNode = (ASTGraphOrDefault)node.jjtGetChild(0);
		if (sourceNode.jjtGetNumChildren() > 0) {
			ValueConstant sourceGraph = (ValueConstant)sourceNode.jjtGetChild(0).jjtAccept(this, data);
			copy.setSourceGraph(sourceGraph);
		}

		ASTGraphOrDefault destinationNode = (ASTGraphOrDefault)node.jjtGetChild(1);
		if (destinationNode.jjtGetNumChildren() > 0) {
			ValueConstant destinationGraph = (ValueConstant)destinationNode.jjtGetChild(0).jjtAccept(this, data);
			copy.setDestinationGraph(destinationGraph);
		}
		return copy;
	}

	@Override
	public Move visit(ASTMove node, Object data)
		throws VisitorException
	{
		Move move = new Move();
		move.setSilent(node.isSilent());

		ASTGraphOrDefault sourceNode = (ASTGraphOrDefault)node.jjtGetChild(0);
		if (sourceNode.jjtGetNumChildren() > 0) {
			ValueConstant sourceGraph = (ValueConstant)sourceNode.jjtGetChild(0).jjtAccept(this, data);
			move.setSourceGraph(sourceGraph);
		}

		ASTGraphOrDefault destinationNode = (ASTGraphOrDefault)node.jjtGetChild(1);
		if (destinationNode.jjtGetNumChildren() > 0) {
			ValueConstant destinationGraph = (ValueConstant)destinationNode.jjtGetChild(0).jjtAccept(this, data);
			move.setDestinationGraph(destinationGraph);
		}
		return move;
	}

	@Override
	public Add visit(ASTAdd node, Object data)
		throws VisitorException
	{
		Add add = new Add();
		add.setSilent(node.isSilent());

		ASTGraphOrDefault sourceNode = (ASTGraphOrDefault)node.jjtGetChild(0);
		if (sourceNode.jjtGetNumChildren() > 0) {
			ValueConstant sourceGraph = (ValueConstant)sourceNode.jjtGetChild(0).jjtAccept(this, data);
			add.setSourceGraph(sourceGraph);
		}

		ASTGraphOrDefault destinationNode = (ASTGraphOrDefault)node.jjtGetChild(1);
		if (destinationNode.jjtGetNumChildren() > 0) {
			ValueConstant destinationGraph = (ValueConstant)destinationNode.jjtGetChild(0).jjtAccept(this, data);
			add.setDestinationGraph(destinationGraph);
		}
		return add;
	}

	@Override
	public Modify visit(ASTModify node, Object data)
		throws VisitorException
	{

		ValueConstant with = null;
		ASTIRI withNode = node.getWithClause();
		if (withNode != null) {
			with = (ValueConstant)withNode.jjtAccept(this, data);
		}

		if (with != null) {
			graphPattern.setContextVar(mapValueExprToVar(with));
			graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);
		}

		ASTGraphPatternGroup whereClause = node.getWhereClause();

		TupleExpr where = null;
		if (whereClause != null) {
			where = (TupleExpr)whereClause.jjtAccept(this, data);
		}

		TupleExpr delete = null;
		ASTDeleteClause deleteNode = node.getDeleteClause();
		if (deleteNode != null) {
			delete = (TupleExpr)deleteNode.jjtAccept(this, data);
		}

		TupleExpr insert = null;
		ASTInsertClause insertNode = node.getInsertClause();
		if (insertNode != null) {
			insert = (TupleExpr)insertNode.jjtAccept(this, data);
		}

		Modify modifyExpr = new Modify(delete, insert, where);

		return modifyExpr;
	}

	@Override
	public TupleExpr visit(ASTDeleteClause node, Object data)
		throws VisitorException
	{
		TupleExpr result = (TupleExpr)data;

		// Collect construct triples
		GraphPattern parentGP = graphPattern;

		graphPattern = new GraphPattern();

		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		TupleExpr deleteExpr = graphPattern.buildTupleExpr();

		// FIXME we should adapt the grammar so we can avoid doing this in
		// post-processing.
		VarCollector collector = new VarCollector();
		deleteExpr.visit(collector);
		for (Var var : collector.getCollectedVars()) {
			if (var.isAnonymous() && !var.hasValue()) {
				// blank node in delete pattern, not allowed by SPARQL spec.
				throw new VisitorException("DELETE clause may not contain blank nodes");
			}
		}

		graphPattern = parentGP;

		return deleteExpr;

	}

	@Override
	public TupleExpr visit(ASTInsertClause node, Object data)
		throws VisitorException
	{
		TupleExpr result = (TupleExpr)data;

		// Collect insert clause triples
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern();

		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		TupleExpr insertExpr = graphPattern.buildTupleExpr();

		graphPattern = parentGP;

		return insertExpr;

	}

	private Set<Var> getProjectionVars(Collection<StatementPattern> statementPatterns) {
		Set<Var> vars = new LinkedHashSet<Var>(statementPatterns.size() * 2);

		for (StatementPattern sp : statementPatterns) {
			vars.add(sp.getSubjectVar());
			vars.add(sp.getPredicateVar());
			vars.add(sp.getObjectVar());
			if (sp.getContextVar() != null) {
				vars.add(sp.getContextVar());
			}
		}

		return vars;
	}
}