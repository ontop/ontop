/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.parser;

import java.util.ArrayList;

import it.unibz.krdb.sql.api.AllComparison;
import it.unibz.krdb.sql.api.AnyComparison;
import it.unibz.krdb.sql.api.SelectionJSQL;
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
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * Visitor class to retrieve the selection of the statement (WHERE expressions)
 * 
 */
public class SelectionVisitor implements SelectVisitor, ExpressionVisitor {
	
	ArrayList<SelectionJSQL> selections;
	SelectionJSQL selection;
	boolean binaryExp =false; // true when the binary expression contains all other expressions
	
	/**
	 * Give the WHERE clause of the select statement
	 * @param select the parsed select statement
	 * @return a SelectionJSQL
	 */
	public ArrayList<SelectionJSQL> getSelection(Select select)
	{
		
		selections= new ArrayList<SelectionJSQL>();
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
		return selections;
		
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		
		selection= new SelectionJSQL();
		
         if (plainSelect.getWhere() != null) {
                 Expression where=plainSelect.getWhere();
                 if(where instanceof BinaryExpression){
                	 selection.addCondition((BinaryExpression) where);
                	 binaryExp=true;
                 }
                 where.accept(this);
                 
                 
                 selections.add(selection); 
         }
         
        
         
		
	}

	@Override
	public void visit(SetOperationList setOpList) {
		
	}

	@Override
	public void visit(WithItem withItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NullValue nullValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Function function) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InverseExpression inverseExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue doubleValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LongValue longValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DateValue dateValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimeValue timeValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StringValue stringValue) {
		// TODO Auto-generated method stub
		
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
	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression);
		
		
	}

	@Override
	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression);
		
		
	}

	@Override
	public void visit(Between between) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo);
		
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan);
		
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals);
		
	}

	@Override
	public void visit(InExpression inExpression) {
		if(binaryExp==false)
		selection.addCondition(inExpression);
		
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		if(binaryExp==false)
		selection.addCondition(isNullExpression);
		
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression);
		
	}

	@Override
	public void visit(MinorThan minorThan) {
		visitBinaryExpression(minorThan);
		
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals);
		
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo);
		
	}

	@Override
	public void visit(Column tableColumn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SubSelect subSelect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause whenClause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		if(binaryExp==false)
		selection.addCondition(allComparisonExpression);
		
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		if(binaryExp==false)
		selection.addCondition(anyComparisonExpression);
		
	}

	@Override
	public void visit(Concat concat) {
		visitBinaryExpression(concat);
		
	}

	@Override
	public void visit(Matches matches) {
		visitBinaryExpression(matches);
		
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		visitBinaryExpression(bitwiseAnd);
		
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		visitBinaryExpression(bitwiseOr);
		
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		visitBinaryExpression(bitwiseXor);
		
	}

	@Override
	public void visit(CastExpression cast) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Modulo modulo) {
		visitBinaryExpression(modulo);
		
	}

	@Override
	public void visit(AnalyticExpression aexpr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExtractExpression eexpr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IntervalExpression iexpr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
		
		
	}
	
	/*
	 * We handle differently AnyComparisonExpression and AllComparisonExpression
	 *  since they do not have a toString method, we substitute them with ad hoc classes
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
//			selection.addCondition(binaryExpression);
		}
		else
		{
			left.accept(this);
			right.accept(this);
		}
		
	}

}
