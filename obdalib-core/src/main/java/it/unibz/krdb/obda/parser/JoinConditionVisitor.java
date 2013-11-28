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
import java.util.HashMap;
import java.util.List;

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
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * 
 * Visitor class allows to retrieve the JoinConditions in a select statement.
 *
 */

public class JoinConditionVisitor implements SelectVisitor, ExpressionVisitor {
	
	ArrayList<String> joinConditions;
	
	/**
	 * Obtain the join conditions in a format "expression condition expression"
	 * for example "table1.home = table2.house"
	 * 
	 * @param select statement with the parsed query
	 * @return a list of string containing the join conditions
	 */
	public ArrayList<String> getJoinConditions(Select select) {
		joinConditions = new ArrayList<String>();
		select.getSelectBody().accept(this);
		return joinConditions;
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		List<Join> joins = plainSelect.getJoins();
		if (joins != null)
		for (Join join : joins){
			Expression expr = join.getOnExpression();
			
			
			if (join.getUsingColumns()!=null)
				for (Column column : join.getUsingColumns()){
					joinConditions.add(plainSelect.getFromItem()+"."+column.getColumnName()+" = "+join.getRightItem()+"."+column.getColumnName());
				}
					
			else{
				if(expr!=null)
//					joinConditions.add(expr.toString());
					expr.accept(this);
//				else
//					if(join.isSimple())
//						joinConditions.add(plainSelect.getWhere().toString());
					
			}
				
		}
	}

	@Override
	public void visit(SetOperationList arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WithItem arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Function arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcNamedParameter arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimeValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimestampValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parenthesis arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition);
		
	}

	@Override
	public void visit(Division arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(Multiplication arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(Subtraction arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(AndExpression arg0) {
		visitBinaryExpression(arg0);
		
	}
	@Override
	public void visit(OrExpression arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(Between arg0) {
		
		
	}

	public void visitBinaryExpression(BinaryExpression binaryExpression) {
		Expression left =binaryExpression.getLeftExpression();
		Expression right =binaryExpression.getRightExpression();
		
		if (!(left instanceof BinaryExpression) && 
				!(right instanceof BinaryExpression)) {
			joinConditions.add(binaryExpression.toString());
		}
		else
		{
			left.accept(this);
			right.accept(this);
		}
		
	}
	@Override
	public void visit(EqualsTo arg0) {
		visitBinaryExpression(arg0);
//		Expression left = arg0.getLeftExpression();
//		Expression right = arg0.getRightExpression();
//		if ((left instanceof Column || left instanceof AnalyticExpression) && 
//				(right instanceof Column || right  instanceof AnalyticExpression)) {
//			joinConditions.add(left + "=" + right);
//
//		} else {
//			left.accept(this);
//			right.accept(this);
//		}
	}

	@Override
	public void visit(GreaterThan arg0) {
		visitBinaryExpression(arg0);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		visitBinaryExpression(arg0);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InExpression arg0) {
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LikeExpression arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(MinorThan arg0) {
		visitBinaryExpression(arg0);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		visitBinaryExpression(arg0);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		visitBinaryExpression(arg0);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Column arg0) {
		joinConditions.add(arg0.getWholeColumnName());
//		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(Matches arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(BitwiseOr arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(BitwiseXor arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(CastExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Modulo arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(AnalyticExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExtractExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IntervalExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OracleHierarchicalExpression arg0) {
		// TODO Auto-generated method stub
		
	}

}
