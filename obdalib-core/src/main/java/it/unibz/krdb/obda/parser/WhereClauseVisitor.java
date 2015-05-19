
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

import it.unibz.krdb.sql.api.AllComparison;
import it.unibz.krdb.sql.api.AnyComparison;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import it.unibz.krdb.sql.api.SelectionJSQL;
import it.unibz.krdb.sql.api.TableJSQL;
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
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.SignedExpression;
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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * Visitor class to retrieve the whereClause of the statement (WHERE expressions)
 * 
 * 
 */
public class WhereClauseVisitor implements SelectVisitor, ExpressionVisitor, FromItemVisitor {
	
//	ArrayList<SelectionJSQL> selections; // add if we want to consider the UNION case
	SelectionJSQL whereClause;
	boolean unsupported =false;
	boolean isSetting =false;
	
	/**
	 * Give the WHERE clause of the select statement
	 * @param select the parsed select statement
	 * @return a SelectionJSQL
	 * @throws JSQLParserException 
	 */
	public SelectionJSQL getWhereClause(Select select, boolean deepParsing) throws JSQLParserException
	{
		
//		selections= new ArrayList<SelectionJSQL>(); // use when we want to consider the UNION
		
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
		if(unsupported && deepParsing)
				throw new JSQLParserException("Query not yet supported");
		return whereClause;
		
	}

    public void setWhereClause(Select selectQuery, SelectionJSQL whereClause) {

        isSetting = true;

        this.whereClause = whereClause;

        selectQuery.getSelectBody().accept(this);
    }

	/*
	 * visit Plainselect, search for the where structure that returns an Expression
	 * Stored in SelectionJSQL. 
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
	 */
	
	
	@Override
	public void visit(PlainSelect plainSelect) {
		
		/*
		 * First check if we are setting a new whereClause. Add the expression contained in the SelectionJSQL
		 * in the WHERE clause.
		 */
		//FROM (subselect) -> process
		//plainSelect.getFromItem().accept(this);

        if (isSetting) {
            plainSelect.setWhere(whereClause.getRawConditions());

        } else {

            if (plainSelect.getWhere() != null) {

                Expression where = plainSelect.getWhere();

                whereClause = new SelectionJSQL();
                whereClause.addCondition(where);

                //we visit the where clause to remove quotes and fix any and all comparison
                where.accept(this);

            }
        }
        
         
		
	}

	@Override
	public void visit(SetOperationList setOpList) {
//		we do not consider the case of UNION
		setOpList.getPlainSelects().get(0).accept(this);
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
		if(function.getName().toLowerCase().equals("regexp_like") ) {
			
			for(Expression ex :function.getParameters().getExpressions()){
				ex.accept(this);
			}
			
		}
		else{
            unsupported = true;
		}
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
		between.getLeftExpression().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);
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

		//Expression e = inExpression.getLeftExpression();
		ItemsList e1 = inExpression.getLeftItemsList();
		if (e1 instanceof SubSelect){
			((SubSelect)e1).accept((ExpressionVisitor)this);
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
		if(ParsedSQLQuery.pQuotes.matcher(columnName).matches())
			tableColumn.setColumnName(columnName.substring(1, columnName.length()-1));
		
	}

	/*
	 * we search for nested where in SubSelect
	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.SubSelect)
	 */
	@Override
	public void visit(SubSelect subSelect) {
		if (subSelect.getSelectBody() instanceof PlainSelect) {
			
			PlainSelect subSelBody = (PlainSelect) (subSelect.getSelectBody());
			
			if (subSelBody.getJoins() != null || subSelBody.getWhere() != null) {
				unsupported = true;
			} else {
				subSelBody.accept(this);
			}
		} else
			unsupported = true;
		
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

	@Override
	public void visit(Table tableName) {
        // do nothing
	}

	@Override
	public void visit(SubJoin subjoin) {
		unsupported = true;
	}

	@Override
	public void visit(LateralSubSelect lateralSubSelect) {
		unsupported = true;
	}

	@Override
	public void visit(ValuesList valuesList) {
        // do nothing
    }

	@Override
	public void visit(RegExpMatchOperator arg0) {
        // do nothing
    }

	@Override
	public void visit(SignedExpression arg0) {
        // do nothing
	}

	@Override
	public void visit(JsonExpression arg0) {
        unsupported = true;		
	}

	@Override
	public void visit(RegExpMySQLOperator arg0) {
        // do nothing
	}

}
