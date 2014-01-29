/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.api.AllComparison;
import it.unibz.krdb.sql.api.AnyComparison;
import it.unibz.krdb.sql.api.SelectionJSQL;
import it.unibz.krdb.sql.api.TableJSQL;
import it.unibz.krdb.sql.api.VisitedQuery;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * Visitor class to retrieve the selection of the statement (WHERE expressions)
 * 
 * 
 */
public class SelectionVisitor implements SelectVisitor, ExpressionVisitor {
	
//	ArrayList<SelectionJSQL> selections; // add if we want to consider the UNION case
	SelectionJSQL selection;
	boolean notSupported=false;
	boolean setSel=false;
	
	/**
	 * Give the WHERE clause of the select statement
	 * @param select the parsed select statement
	 * @return a SelectionJSQL
	 * @throws JSQLParserException 
	 */
	public SelectionJSQL getSelection(Select select) throws JSQLParserException
	{
		
//		selections= new ArrayList<SelectionJSQL>(); // use when we want to consider the UNION
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
		if(notSupported)
				throw new JSQLParserException("Query not yet supported");
		return selection;
		
	}
	
	public void setSelection(Select select, SelectionJSQL selection){
		
		setSel= true;
		this.selection=selection;
		
		select.getSelectBody().accept(this);
	}

	/*
	 * visit Plainselect, search for the where structure that returns an Expression
	 * Stored in SelectionJSQL. 
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
	 */
	
	
	@Override
	public void visit(PlainSelect plainSelect) {
		
		/*
		 * First check if we are setting a new selection. Add the expression contained in the SelectionJSQL
		 * in the WHERE clause.
		 */
		
		if(setSel){
			plainSelect.setWhere(selection.getRawConditions());
			
		}
		
		else{
			
         if (plainSelect.getWhere() != null) {
        	 
             Expression where=plainSelect.getWhere();
                 
        	 selection= new SelectionJSQL();
			 selection.addCondition(where);
                	 
             //we visit the where clause to remove quotes and fix any and all comparison
             where.accept(this);

         }
		}
        
         
		
	}

	@Override
	public void visit(SetOperationList setOpList) {
//		we do not consider the case of UNION
	}

	@Override
	public void visit(WithItem withItem) {
		// we do not consider the case for WITH
		
	}

	@Override
	public void visit(NullValue nullValue) {
		// we do not execute anything
		
	}

	@Override
	public void visit(Function function) {
		notSupported=true;
		
	}

	@Override
	public void visit(InverseExpression inverseExpression) {
		// we do not execute anything
		
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		//we do not execute anything
		
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		// we do not execute anything
		
	}

	@Override
	public void visit(DoubleValue doubleValue) {
		// we do not execute anything
		
	}

	@Override
	public void visit(LongValue longValue) {
		// we do not execute anything
		
	}

	@Override
	public void visit(DateValue dateValue) {
		// we do not execute anything
		
	}

	@Override
	public void visit(TimeValue timeValue) {
		// we do not execute anything
		
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		// we do not execute anything
		
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
		
	}

	@Override
	public void visit(StringValue stringValue) {
		// we do not execute anything
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Addition)
	 */
	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Division)
	 */
	@Override
	public void visit(Division division) {
		visitBinaryExpression(division);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Multiplication)
	 */
	@Override
	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Subtraction)
	 */
	@Override
	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction);
		
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.AndExpression)
	 */
	@Override
	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression);
		
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.OrExpression)
	 */
	@Override
	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression);
		
		
	}

	@Override
	public void visit(Between between) {
		//we do not support BETWEEN
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.EqualsTo)
	 */
	@Override
	public void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThan)
	 */
	@Override
	public void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals)
	 */
	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals);
		
	}

	/*
	 * We add the content of the inExpression in SelectionJSQL
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.InExpression)
	 */
	@Override
	public void visit(InExpression inExpression) {
		
	}

	/*
	 * We add the content of isNullExpression in SelectionJSQL
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.InExpression)
	 */
	@Override
	public void visit(IsNullExpression isNullExpression) {
		
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.InExpression)
	 */
	@Override
	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThan)
	 */
	@Override
	public void visit(MinorThan minorThan) {
		visitBinaryExpression(minorThan);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.NotEqualsTo)
	 */
	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo);
		
	}

	/*
	 * Visit the column and remove the quotes if they are present(non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
	 */
	
	@Override
	public void visit(Column tableColumn) {
		Table table= tableColumn.getTable();
		if(table.getName()!=null){
			
			TableJSQL fixTable = new TableJSQL(table);
			table.setAlias(fixTable.getAlias());
			table.setName(fixTable.getTableName());
			table.setSchemaName(fixTable.getSchema());
		
		}
		String columnName= tableColumn.getColumnName();
		if(VisitedQuery.pQuotes.matcher(columnName).matches())
			tableColumn.setColumnName(columnName.substring(1, columnName.length()-1));
		
	}

	/*
	 * we search for nested where in SubSelect
	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.SubSelect)
	 */
	@Override
	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(this);
		
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		// it is not supported
		
	}

	@Override
	public void visit(WhenClause whenClause) {
		// it is not supported
		
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		// it is not supported
		
	}

	/*
	 * We add the content of AllComparisonExpression in SelectionJSQL
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AllComparisonExpression)
	 */
	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		
	}

	/*
	 * We add the content of AnyComparisonExpression in SelectionJSQL
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AnyComparisonExpression)
	 */
	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Concat)
	 */
	@Override
	public void visit(Concat concat) {
		visitBinaryExpression(concat);
		
	}

	/*
	 *We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.Matches)
	 */
	@Override
	public void visit(Matches matches) {
		visitBinaryExpression(matches);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd)
	 */
	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		visitBinaryExpression(bitwiseAnd);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr)
	 */
	@Override
	public void visit(BitwiseOr bitwiseOr) {
		visitBinaryExpression(bitwiseOr);
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor)
	 */
	@Override
	public void visit(BitwiseXor bitwiseXor) {
		visitBinaryExpression(bitwiseXor);
		
	}

	@Override
	public void visit(CastExpression cast) {
		// not supported
		
	}

	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Modulo)
	 */
	@Override
	public void visit(Modulo modulo) {
		visitBinaryExpression(modulo);
		
	}

	
	@Override
	public void visit(AnalyticExpression aexpr) {
		// not supported
		
	}

	@Override
	public void visit(ExtractExpression eexpr) {
		// not supported
		
	}

	@Override
	public void visit(IntervalExpression iexpr) {
		// not supported
		
	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
		//not supported 
		
	}
	
	/*
	 * We handle differently AnyComparisonExpression and AllComparisonExpression
	 *  since they do not have a toString method, we substitute them with ad hoc classes.
	 *  we continue to visit the subexpression.
	 * 
	 */
	
	public void visitBinaryExpression(BinaryExpression binaryExpression) {
		
		Expression left =binaryExpression.getLeftExpression();
		Expression right =binaryExpression.getRightExpression();
		
		if (right instanceof AnyComparisonExpression){
			right = new AnyComparison(((AnyComparisonExpression) right).getSubSelect());
			binaryExpression.setRightExpression(right);
		}
		
		if (right instanceof AllComparisonExpression){
			right = new AllComparison(((AllComparisonExpression) right).getSubSelect());
			binaryExpression.setRightExpression(right);
		}
		
		if (!(left instanceof BinaryExpression) && 
				!(right instanceof BinaryExpression)) {

			left.accept(this);
			right.accept(this);
		}
		else
		{
			left.accept(this);
			right.accept(this);
		}
		
	}

}
