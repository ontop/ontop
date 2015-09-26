
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
import it.unibz.krdb.sql.api.AllComparison;
import it.unibz.krdb.sql.api.AnyComparison;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

/**
 * Visitor class to retrieve the WHERE clause of the SELECT statement
 * 
 * BRINGS TABLE NAME / SCHEMA / ALIAS AND COLUMN NAMES in the WHERE clause into NORMAL FORM
 * 
 */
public class WhereClauseVisitor {
	
	private Expression whereClause;
	private boolean unsupported = false;

	private final QuotedIDFactory idfac;
	
	public WhereClauseVisitor(QuotedIDFactory idfac) {
		this.idfac = idfac;
	}
	
	/**
	 * Give the WHERE clause of the SELECT statement<br>
	 * 
	 * NOTE: is also BRINGS ALL SCHEMA / TABLE / ALIAS / COLUMN NAMES in the WHERE clause into NORMAL FORM
	 * 
	 * @param select the parsed select statement
	 * @return an Expression
	 * @throws JSQLParserException 
	 */
	public Expression getWhereClause(Select select, boolean deepParsing) throws JSQLParserException {
		
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) 
				withItem.accept(selectVisitor);
		}
		
		select.getSelectBody().accept(selectVisitor);
		
		if (unsupported && deepParsing)
				throw new JSQLParserException(SQLQueryParser.QUERY_NOT_SUPPORTED);
		
		return whereClause;
	}

    public void setWhereClause(Select selectQuery, final Expression whereClause) {

        selectQuery.getSelectBody().accept(new SelectVisitor() {
    		@Override
    		public void visit(PlainSelect plainSelect) {
                plainSelect.setWhere(whereClause);
    		}
    		@Override
    		public void visit(SetOperationList setOpList) {
    			// we do not consider the case of UNION
    			// ROMAN (22 Sep 2015): not sure why it is applied to the first one only 
        		setOpList.getPlainSelects().get(0).accept(this);
    		}
    		@Override
    		public void visit(WithItem withItem) {
    	  		// we do not consider the case for WITH
    		}    	
        });
    }

    
    private SelectVisitor selectVisitor = new SelectVisitor() {

    	/*
    	 * visit PlainSelect, search for the where structure that returns an Expression
    	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
    	 */
    	@Override
    	public void visit(PlainSelect plainSelect) {
            Expression where = plainSelect.getWhere();
            if (where != null) {
                whereClause = where;
                //we visit the where clause to fix any and all comparison
                where.accept(expressionVisitor);
            }
    	}
    	@Override
    	public void visit(SetOperationList setOpList) {
    		// we do not consider the case of UNION
			// ROMAN (22 Sep 2015): not sure why it is applied to the first one only 
    		setOpList.getPlainSelects().get(0).accept(this);
    	}

    	@Override
    	public void visit(WithItem withItem) {
    		// we do not consider the case for WITH
    	}
    };

    	
    private ExpressionVisitor expressionVisitor = new ExpressionVisitor() {

    	@Override
    	public void visit(NullValue nullValue) {
    		// we do not execute anything		
    	}

    	@Override
    	public void visit(Function function) {
    		// ROMAN (22 Sep 2015): longer list of supported functions?
    		if (function.getName().toLowerCase().equals("regexp_like")) {
    			for (Expression ex :function.getParameters().getExpressions()) 
    				ex.accept(this);
    		}
    		else
                unsupported = true;
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
    		between.getLeftExpression().accept(this);
    		between.getBetweenExpressionStart().accept(this);
    		between.getBetweenExpressionEnd().accept(this);
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

    		//Expression e = inExpression.getLeftExpression();
    		
    		// ROMAN (25 Sep 2015): why not getLeftExpression? getLeftItemList can be empty
    		// what about the right-hand side list?!
    		
    		ItemsList e1 = inExpression.getLeftItemsList();
    		if (e1 instanceof SubSelect) {
    			((SubSelect)e1).accept((ExpressionVisitor)this);
    		}
    		else if (e1 instanceof ExpressionList) {
    			for (Expression expr : ((ExpressionList)e1).getExpressions()) {
    				expr.accept(this);
    			}
    		}
    		else if (e1 instanceof MultiExpressionList) {
    			for (ExpressionList exp : ((MultiExpressionList)e1).getExprList()){
    				for (Expression expr : exp.getExpressions()) {
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

    	/*
    	 * Visit the column and remove the quotes if they are present(non-Javadoc)
    	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
    	 */
    	
    	@Override
    	public void visit(Column tableColumn) {
    		// CHANGES THE TABLE SCHEMA / NAME AND COLUMN NAME
    		ParsedSQLQuery.normalizeColumnName(idfac, tableColumn);
    	}

    	/*
    	 * we search for nested where in SubSelect
    	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.SubSelect)
    	 */
    	@Override
    	public void visit(SubSelect subSelect) {
    		if (subSelect.getSelectBody() instanceof PlainSelect) {
    			
    			PlainSelect subSelBody = (PlainSelect) (subSelect.getSelectBody());
    			if (subSelBody.getJoins() != null || subSelBody.getWhere() != null) 
    				unsupported = true;
    			else 
    				subSelBody.accept(selectVisitor);
    		} 
    		else
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
    	public void visit(Modulo modulo) {
    		visitBinaryExpression(modulo);
    	}


    	@Override
    	public void visit(CastExpression cast) {
    		// not supported    		
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
    	 */
    	
    	private void visitBinaryExpression(BinaryExpression binaryExpression) {
    		
    		Expression left = binaryExpression.getLeftExpression();
    		Expression right = binaryExpression.getRightExpression();
    		
    		if (right instanceof AnyComparisonExpression){
    			right = new AnyComparison(((AnyComparisonExpression) right).getSubSelect());
    			binaryExpression.setRightExpression(right);
    		}
    		
    		if (right instanceof AllComparisonExpression){
    			right = new AllComparison(((AllComparisonExpression) right).getSubSelect());
    			binaryExpression.setRightExpression(right);
    		}
    		
			left.accept(this);
			right.accept(this);
    	}
    	
    	@Override
    	public void visit(SignedExpression arg0) {
            // do nothing
    	}

    	@Override
    	public void visit(RegExpMatchOperator arg0) {
            // do nothing
        }

    	@Override
    	public void visit(RegExpMySQLOperator arg0) {
            // do nothing
    	}

    	@Override
    	public void visit(JsonExpression arg0) {
            unsupported = true;		
    	}
    };
}
