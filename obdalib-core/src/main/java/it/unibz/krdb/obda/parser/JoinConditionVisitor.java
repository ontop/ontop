
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

import java.util.LinkedList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

/**
 * 
 * Visitor class allows to retrieve the JoinConditions in a select statement.
 * 
 * BRINGS TABLE NAME / SCHEMA / ALIAS AND COLUMN NAMES in the JOIN / JOIN ON clauses into NORMAL FORM
 *
 */

public class JoinConditionVisitor {
	
	private final List<Expression> joinConditions = new LinkedList<Expression>();
	private boolean notSupported = false;

	private final QuotedIDFactory idfac;
	
	/**
	 * Obtain the join conditions in a format "expression condition expression"
	 * for example "table1.home = table2.house"<br>
	 *
	 * BRINGS TABLE NAME / SCHEMA / ALIAS AND COLUMN NAMES in the JOIN / JOIN ON clauses into NORMAL FORM
	 * 
	 * @param select statement with the parsed query
	 * @return a list of string containing the join conditions
	 */
	public JoinConditionVisitor(Select select, boolean deepParsing, QuotedIDFactory idfac)  throws JSQLParserException {
		this.idfac = idfac;
		
		select.getSelectBody().accept(selectVisitor);
	
		if(notSupported && deepParsing) // used to throw exception for the currently unsupported methods
			throw new JSQLParserException(SQLQueryParser.QUERY_NOT_SUPPORTED);
	}
		
		
	public List<Expression> getJoinConditions() {	
		return joinConditions;
	}

	
	private final SelectVisitor selectVisitor = new SelectVisitor() {
		/*
		 * visit PlainSelect, search for the join conditions, we do not consider the simple join that are considered in whereClause
		 * 
		 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
		 */
		@Override
		public void visit(PlainSelect plainSelect) {
			FromItem fromItem = plainSelect.getFromItem();
			fromItem.accept(fromItemVisitor);
			
			List<Join> joins = plainSelect.getJoins();
			
			if (joins != null)
			for (Join join : joins) {
				Expression expr = join.getOnExpression();
				
				if (join.getUsingColumns() != null) // JOIN USING column
					for (Column column : join.getUsingColumns()) {
						
						if (fromItem instanceof Table && join.getRightItem() instanceof Table) {
							Table table1 = (Table)fromItem;
							BinaryExpression bexpr = new EqualsTo();
							Column column1 = new Column(table1, column.getColumnName());
							ParsedSQLQuery.normalizeColumnName(idfac, column1);
							bexpr.setLeftExpression(column1);
							
							Column column2 = new Column((Table)join.getRightItem(), column.getColumnName());
							ParsedSQLQuery.normalizeColumnName(idfac, column2);
							bexpr.setRightExpression(column2);
							joinConditions.add(bexpr);
							//plainSelect.getFromItem()+"."+columnName+ bexpr.getStringExpression() +join.getRightItem()+"."+columnName);
						} 
						else {
							//more complex structure in FROM or JOIN e.g. subselects
							//	plainSelect.getFromItem().accept(this);
							//	join.getRightItem().accept(this);
							notSupported = true;				
						}
					}
						
				else{ //JOIN ON cond
					if (expr != null) {
						join.getRightItem().accept(fromItemVisitor);
						// ROMAN (25 Sep 2015): this transforms (A.id = B.id) OR (A.id2 = B.id2) into the list 
						// { (A.id = B.id), (A.id2 = B.id2) }, which is equivalent to AND!
						// similarly, it will transform NOT (A <> B) into { (A <> B) }, which gets rid of NOT
						expr.accept(expressionVisitor);
					} 
					//we do not consider simple joins
//					else
//						if(join.isSimple())
//							joinConditions.add(plainSelect.getWhere().toString());
						
				}
					
			}
		}

		@Override
		public void visit(SetOperationList operations) { //UNION
			 notSupported = true;
			 operations.getPlainSelects().get(0).accept(this);
			// we do not consider the case of union
			/*for (PlainSelect plainSelect: operations.getPlainSelects()){
				plainSelect.getFromItem().accept(this);
				
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
//							joinConditions.add(expr.toString());
							expr.accept(this);
//						
							
					}
				}
			}*/
			
		}

		@Override
		public void visit(WithItem arg0) {
			// we do not consider the case of with
			
		}
	};
	
	
	private final FromItemVisitor fromItemVisitor = new FromItemVisitor() {
		@Override
		public void visit(Table tableName) {
			// we do not execute anything
		}
		
		@Override
		public void visit(LateralSubSelect lateralSubSelect) {
			notSupported = true;
		}

		@Override
		public void visit(ValuesList valuesList) {
			// we do not execute anything
		}

		@Override
		public void visit(SubSelect subSelect) {
			visitSubSelect(subSelect);		
		}

		/*
		 * search for the subjoin conditions, we do not consider the simple join that are considered in whereClause
		 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.SubJoin)
		 */
		@Override
		public void visit(SubJoin subjoin) {
			notSupported = true;
//			Join join =subjoin.getJoin();
//			Expression expr = join.getOnExpression();
//			
//			
//			if (join.getUsingColumns()!=null)
//				for (Column column : join.getUsingColumns())
//				{
//					String columnName= column.getColumnName();
//					
//					if(VisitedQuery.pQuotes.matcher(columnName).matches())
//					{
//						columnName=columnName.substring(1, columnName.length()-1);
//						column.setColumnName(columnName);
//					}
//					
//					joinConditions.add(subjoin.getLeft()+"."+column.getColumnName()+" = "+join.getRightItem()+"."+column.getColumnName());
//				}
//					
//			else{
//				if(expr!=null)
//					expr.accept(this);
//			}
//			
		}

	};


	private void visitSubSelect(SubSelect subSelect) {
		if (subSelect.getSelectBody() instanceof PlainSelect) {

			PlainSelect subSelBody = (PlainSelect) (subSelect.getSelectBody());

			if (subSelBody.getJoins() != null || subSelBody.getWhere() != null) {
				notSupported = true;
			} 
			else {
				subSelBody.accept(selectVisitor);
			}
		} 
		else
			notSupported = true;
	}

	
	
	/**
	 * Visitor for expression in the JOIN ON conditions
	 * 
	 */
	
	private final ExpressionVisitor expressionVisitor = new ExpressionVisitor() {
		@Override
		public void visit(NullValue arg0) {
			//we do not execute anything 
			
		}

		@Override
		public void visit(Function arg0) {
			//we do not execute anything 
			
		}

		@Override
		public void visit(JdbcParameter arg0) {
			//we do not execute anything 
			
		}

		@Override
		public void visit(JdbcNamedParameter arg0) {
			//we do not execute anything 
			
		}

		@Override
		public void visit(DoubleValue arg0) {
			//we do not execute anything 
			
		}

		@Override
		public void visit(LongValue arg0) {
			//we do not execute anything 
			
		}

		@Override
		public void visit(DateValue arg0) {
			//we do not execute anything 
			
		}

		@Override
		public void visit(TimeValue arg0) {
			//we do not execute anything 
			
		}

		@Override
		public void visit(TimestampValue arg0) {
			//we do not execute anything 
			
		}

		@Override
		public void visit(Parenthesis parenthesis) {
			parenthesis.getExpression().accept(this);
			
		}

		@Override
		public void visit(StringValue arg0) {
			//we do not execute anything 
			
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Addition)
		 */
		@Override
		public void visit(Addition addition) {
			visitBinaryExpression(addition);
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Division)
		 */
		@Override
		public void visit(Division arg0) {
			visitBinaryExpression(arg0);
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Multiplication)
		 */
		@Override
		public void visit(Multiplication arg0) {
			visitBinaryExpression(arg0);
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Subtraction)
		 */
		@Override
		public void visit(Subtraction arg0) {
			visitBinaryExpression(arg0);
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.AndExpression)
		 */
		@Override
		public void visit(AndExpression arg0) {
			visitBinaryExpression(arg0);
		}
		
		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.OrExpression)
		 */
		@Override
		public void visit(OrExpression arg0) {
			visitBinaryExpression(arg0);
		}

		@Override
		public void visit(Between arg0) {
		//we do not consider the case of BETWEEN	
			
		}

		/*
		 * We store in join conditions the binary expression that are not nested, 
		 * for the others we continue to visit the subexpression
		 * Example: AndExpression and OrExpression always have subexpression.
		 */
		
		public void visitBinaryExpression(BinaryExpression binaryExpression) {
			Expression left = binaryExpression.getLeftExpression();
			Expression right = binaryExpression.getRightExpression();
			
			if (!(left instanceof BinaryExpression) && 
					!(right instanceof BinaryExpression)) {
				
				left.accept(this);
				right.accept(this);
				// ROMAN (25 Sep 2015): this transforms OR into AND
				joinConditions.add(binaryExpression);
			}
			else {
				left.accept(this);
				right.accept(this);
			}
		}
		
		/*
		 *  We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.EqualsTo)
		 */
		@Override
		public void visit(EqualsTo arg0) {
			visitBinaryExpression(arg0);
		}

		/*
		 *  We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThan)
		 */
		@Override
		public void visit(GreaterThan arg0) {
			visitBinaryExpression(arg0);	
		}

		/*
		 *  We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals)
		 */
		@Override
		public void visit(GreaterThanEquals arg0) {
			visitBinaryExpression(arg0);
		}


		@Override
		public void visit(InExpression arg0) {
			//we do not support the case for IN condition
		
		}

		@Override
		public void visit(IsNullExpression arg0) {
			//we do not execute anything
			
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.LikeExpression)
		 */
		@Override
		public void visit(LikeExpression arg0) {
			visitBinaryExpression(arg0);	
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThan)
		 */
		@Override
		public void visit(MinorThan arg0) {
			visitBinaryExpression(arg0);
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
		 */
		@Override
		public void visit(MinorThanEquals arg0) {
			visitBinaryExpression(arg0);
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.NotEqualsTo)
		 */
		@Override
		public void visit(NotEqualsTo arg0) {
			visitBinaryExpression(arg0);
		}

		/*
		 * Remove quotes from columns if they are present (non-Javadoc)
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
		 */
		@Override
		public void visit(Column tableColumn) {
			// CHANGE TABLE / COLUMN NAME IN THE JOIN CONDITION
			// TableJSQL.unquoteColumnAndTableName(tableColumn);
			ParsedSQLQuery.normalizeColumnName(idfac, tableColumn);
		}
		
		/*
		 * We visit also the subselect to find nested joins
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.statement.select.SubSelect)
		 */
		@Override
		public void visit(SubSelect subSelect) {
			visitSubSelect(subSelect);
		}

		@Override
		public void visit(CaseExpression arg0) {
			// we do not support case expression
			notSupported = true;
			
		}

		@Override
		public void visit(WhenClause arg0) {
			// we do not support when expression
			notSupported = true;
		}

		@Override
		public void visit(ExistsExpression exists) {
			// we do not support exists
			notSupported = true;
		}

		/*
		 * We visit the subselect in ALL(...)
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AllComparisonExpression)
		 */
		@Override
		public void visit(AllComparisonExpression all) {
			notSupported = true;
		}

		/*
		 * We visit the subselect in ANY(...)
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AnyComparisonExpression)
		 */
		@Override
		public void visit(AnyComparisonExpression any) {
			notSupported = true;
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
		 */
		@Override
		public void visit(Concat arg0) {
			notSupported = true;
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
		 */
		@Override
		public void visit(Matches arg0) {
			notSupported = true;
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
		 */
		@Override
		public void visit(BitwiseAnd arg0) {
			notSupported = true;
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
		 */
		@Override
		public void visit(BitwiseOr arg0) {
			notSupported = true;
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
		 */
		@Override
		public void visit(BitwiseXor arg0) {
			notSupported = true;
		}

		@Override
		public void visit(CastExpression arg0) {
			
		}

		/*
		 * We handle in the same way all BinaryExpression
		 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
		 */
		@Override
		public void visit(Modulo arg0) {
			notSupported = true;
		}

		@Override
		public void visit(AnalyticExpression arg0) {
			// we do not consider AnalyticExpression
			notSupported = true;
		}

		@Override
		public void visit(ExtractExpression arg0) {
			// we do not consider ExtractExpression
			notSupported = true;
		}

		@Override
		public void visit(IntervalExpression arg0) {
			// we do not consider IntervalExpression
			notSupported = true;
		}

		@Override
		public void visit(OracleHierarchicalExpression arg0) {
			// we do not consider OracleHierarchicalExpression
			notSupported = true;
		}


		@Override
		public void visit(RegExpMatchOperator arg0) {
			notSupported = true;
		}

		@Override
		public void visit(SignedExpression arg0) {
			notSupported = true;
			
		}

		@Override
		public void visit(JsonExpression arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(RegExpMySQLOperator arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	

}
