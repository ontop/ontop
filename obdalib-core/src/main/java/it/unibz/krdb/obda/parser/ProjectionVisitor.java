package it.unibz.krdb.obda.parser;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.sql.QuotedIDFactory;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import it.unibz.krdb.sql.api.ProjectionJSQL;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor to retrieve the projection of the given select statement. (SELECT... FROM).<br>
 * 
 * BRINGS TABLE NAME / SCHEMA / ALIAS AND COLUMN NAMES in the FROM clause into NORMAL FORM
 *
 * Since the current release does not support Function, we throw a ParserException, when a function is present
 *
 */

public class ProjectionVisitor implements SelectVisitor, SelectItemVisitor, ExpressionVisitor{
	
	private ProjectionJSQL projection;
	private boolean bdistinctOn = false; // true when a SELECT distinct is present
	private boolean unsupported = false;
	
	private final QuotedIDFactory idfac;
	
	public ProjectionVisitor(QuotedIDFactory idfac) {
		this.idfac = idfac;
	}

	/**
	 * Return the list of Projection with the expressions between SELECT and FROM<br>
	 * 
	 * BRINGS TABLE NAME / SCHEMA / ALIAS AND COLUMN NAMES in the FROM clause into NORMAL FORM
	 * 
	 * @param select parsed statement
	 * @return
	 * @throws JSQLParserException 
	 */
	
	public ProjectionJSQL getProjection(Select select, boolean deepParsing) throws JSQLParserException {
		
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) 
				withItem.accept(this);
		}
		select.getSelectBody().accept(this);
		
		if (unsupported && deepParsing) // used to throw exception for the currently unsupported methods
				throw new JSQLParserException(SQLQueryDeepParser.QUERY_NOT_SUPPORTED);
		
		return projection;	
		
	}
	
	/**
	 *  Modify the Select clause using the values of the Projection
	 * @param select parsed statement
	 * @param proj anew projection expression between SELECT and FROM
	 */
	public void setProjection(Select select, final ProjectionJSQL proj) {

		select.getSelectBody().accept(new SelectVisitor() {

			@Override
			public void visit(PlainSelect plainSelect) {
				if (proj.getType().equals("select distinct on")) {
					List<SelectItem> distinctList = new ArrayList<>();
					
					for (SelectExpressionItem seItem : proj.getColumnList()) 
						distinctList.add(seItem);
					
					Distinct distinct = new Distinct();
					distinct.setOnSelectItems(distinctList);
					plainSelect.setDistinct(distinct);
				}
				else if (proj.getType().equals("select distinct")) {
					Distinct distinct = new Distinct();
					plainSelect.setDistinct(distinct);
					
					plainSelect.getSelectItems().clear();
					plainSelect.getSelectItems().addAll(proj.getColumnList());
				}
				else {
					plainSelect.getSelectItems().clear();
					List<SelectExpressionItem> columnList = proj.getColumnList();
					if (!columnList.isEmpty()) {
						plainSelect.getSelectItems().addAll(columnList);
					}
					else {
						plainSelect.getSelectItems().add(new AllColumns());
					}
				}	
			}

			@Override
			public void visit(SetOperationList setOpList) {
				unsupported = true;
				setOpList.getPlainSelects().get(0).accept(this);
			}

			@Override
			public void visit(WithItem withItem) {
				withItem.getSelectBody().accept(this);
			}});
	}

	private void unsupported(Object o) {
		System.out.println(this.getClass() + " DOES NOT SUPPORT " + o);
		unsupported = true;
	}
	
	
	/*
	 * visit PlainSelect, search for the SelectExpressionItems
	 * Stored in ProjectionSQL 
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
	 */
	
	@Override
	public void visit(PlainSelect plainSelect) {
		// visit the SelectItems and distinguish between select distinct,
		// select distinct on, select all 
		
		projection = new ProjectionJSQL();
		Distinct distinct = plainSelect.getDistinct();
	
		if (distinct != null) { // for SELECT DISTINCT [ON (...)]			
			
			if (distinct.getOnSelectItems() != null) {
				bdistinctOn = true;
			
				for (SelectItem item : distinct.getOnSelectItems()) 
					item.accept(this);
				
				bdistinctOn = false;
			}
			else
				projection.setType(ProjectionJSQL.SELECT_DISTINCT);	
		}
	
		for (SelectItem item : plainSelect.getSelectItems()) 
			item.accept(this);
	}

	/* visit also the Operation as UNION
	 * it is not supported now */
	@Override
	public void visit(SetOperationList setOpList) { 
		unsupported(setOpList);
		setOpList.getPlainSelects().get(0).accept(this);
	}

	/* 
	 * Search for select in WITH statement
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.WithItem)
	 */
	@Override
	public void visit(WithItem withItem) {
		withItem.getSelectBody().accept(this);
	}
	
	/*
	 * Add the projection in the case of SELECT *
	 * @see net.sf.jsqlparser.statement.select.SelectItemVisitor#visit(net.sf.jsqlparser.statement.select.AllColumns)
	 */
	@Override
	public void visit(AllColumns allColumns) {
		projection.add(allColumns);
	}

	/*
	 * Add the projection in the case of SELECT table.*
	 * @see net.sf.jsqlparser.statement.select.SelectItemVisitor#visit(net.sf.jsqlparser.statement.select.AllTableColumns)
	 */
	@Override
	public void visit(AllTableColumns allTableColumns) {	
		projection.add(allTableColumns);
	}

	/*
	 * Add the projection for the selectExpressionItem, distinguishing between select all and select distinct
	 * @see net.sf.jsqlparser.statement.select.SelectItemVisitor#visit(net.sf.jsqlparser.statement.select.SelectExpressionItem)
	 */
	@Override
	public void visit(SelectExpressionItem selectExpr) {
		projection.add(selectExpr, bdistinctOn);
		selectExpr.getExpression().accept(this);
		// all complex expressions in SELECT must be named (by aliases)
		if (!(selectExpr.getExpression() instanceof Column) && selectExpr.getAlias() == null)
			unsupported(selectExpr);
	}

	@Override
	public void visit(NullValue nullValue) {
		// TODO Auto-generated method stub
	}

	/*
	 * The system cannot support function currently (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.Function)
	 * @link ConferenceConcatMySQLTest
	 */
	@Override
	public void visit(Function function) {
		switch (function.getName().toLowerCase()) {
			case "regexp_like" :
			case "regexp_replace" :
			case "replace" :
			case "concat" :
			//case "substr" : 
				for (Expression ex :function.getParameters().getExpressions()) 
					ex.accept(this);
				break;
			default:
				unsupported(function);
		}
		
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);		
	}

	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition);
	}

	@Override
	public void visit(Division division) {
		visitBinaryExpression(division);
	}

	@Override
	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication);
	}

	@Override
	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction);
	}

	@Override
	public void visit(SignedExpression arg0) {
		arg0.getExpression().accept(this);
	}
	
	@Override
	public void visit(AndExpression andExpression) {
		unsupported(andExpression);
	}

	@Override
	public void visit(OrExpression orExpression) {
		unsupported(orExpression);
	}

	@Override
	public void visit(Between between) {
		between.getLeftExpression().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		unsupported(equalsTo);
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		unsupported(greaterThan);
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		unsupported(greaterThanEquals);
	}

	@Override
	public void visit(InExpression inExpression) {
		//Expression e = inExpression.getLeftExpression();
		ItemsList e1 = inExpression.getLeftItemsList();
		if (e1 instanceof SubSelect){
			((SubSelect)e1).accept(this);
		}
		else if (e1 instanceof ExpressionList) {
			for (Expression expr : ((ExpressionList)e1).getExpressions()) {
				expr.accept(this);
			}
		}
		else if (e1 instanceof MultiExpressionList) {
			for (ExpressionList exp : ((MultiExpressionList)e1).getExprList()){
				for (Expression expr : ((ExpressionList)exp).getExpressions()) {
					expr.accept(this);
				}
			}
		}
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		unsupported(isNullExpression);
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		unsupported(likeExpression);
	}

	@Override
	public void visit(MinorThan minorThan) {
		unsupported(minorThan);
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		unsupported(minorThanEquals);
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		unsupported(notEqualsTo);
	}

	/*
	 * Visit the column and remove the quotes if they are present(non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
	 */
	@Override
	public void visit(Column tableColumn) {
		// CHANGES TABLE AND COLUMN NAMES
		ParsedSQLQuery.normalizeColumnName(idfac, tableColumn);
	}

	@Override
	public void visit(SubSelect subSelect) {
		if (subSelect.getSelectBody() instanceof PlainSelect) {

			PlainSelect subSelBody = (PlainSelect) (subSelect.getSelectBody());

			if (subSelBody.getJoins() != null || subSelBody.getWhere() != null) {
				unsupported(subSelect);
			} 
			else {
				subSelBody.accept(this);
			}
		} 
		else
			unsupported(subSelect);
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		unsupported(caseExpression);
	}

	@Override
	public void visit(WhenClause whenClause) {
		unsupported(whenClause);
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		unsupported(existsExpression);
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		unsupported(allComparisonExpression);
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		unsupported(anyComparisonExpression);
	}

	@Override
	public void visit(Concat concat) {
		visitBinaryExpression(concat);
	}

	@Override
	public void visit(Matches matches) {
		unsupported(matches);
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		unsupported(bitwiseAnd);
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		unsupported(bitwiseOr);
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		unsupported(bitwiseXor);
	}

	@Override
	public void visit(CastExpression cast) {
		
		
	}

	@Override
	public void visit(Modulo modulo) {
		unsupported(modulo);
	}

	@Override
	public void visit(AnalyticExpression aexpr) {
		unsupported(aexpr);
	}

	@Override
	public void visit(ExtractExpression eexpr) {
		unsupported(eexpr);
	}

	@Override
	public void visit(IntervalExpression iexpr) {
		unsupported(iexpr);
	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
		unsupported(oexpr);
	}

	@Override
	public void visit(RegExpMatchOperator arg0) {
		unsupported(arg0);
	}


	@Override
	public void visit(JsonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RegExpMySQLOperator arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		unsupported(jdbcParameter);
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		unsupported(jdbcNamedParameter);
	}

	
	private void visitBinaryExpression(BinaryExpression binaryExpression) {
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);
	}
	
	/*
	 * scalar values: all supported 
	 */
	
	@Override
	public void visit(DoubleValue doubleValue) {
		// NO-OP
	}

	@Override
	public void visit(LongValue longValue) {
		// NO-OP
	}

	@Override
	public void visit(DateValue dateValue) {
		// NO-OP
	}

	@Override
	public void visit(TimeValue timeValue) {
		// NO-OP
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		// NO-OP
	}

	@Override
	public void visit(StringValue stringValue) {
		// NO-OP
	}
	
}
