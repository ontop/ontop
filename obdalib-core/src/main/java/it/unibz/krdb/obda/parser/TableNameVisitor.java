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

import it.unibz.krdb.sql.api.TableJSQL;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Find all used tables within an select statement.
 */
public class TableNameVisitor {

	// Store the table selected by the SQL query in RelationJSQL
	private final List<TableJSQL> tables = new ArrayList<>();

	// There are special names that are not table names but are parsed as tables. 
	// These names are collected here and are not included in the table names
	private final List<String> otherItemNames = new ArrayList<>();
	
	private boolean unsupported = false;


	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * NOTE: CHANGES THE QUERY (BRINGS ALIAS NAMES TO THE CANONICAL FORM)
	 *
	 * @param select
	 * @param deepParsing
	 * @return
	 */
	public TableNameVisitor(Select select, boolean deepParsing) throws JSQLParserException {
		
 		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(selectVisitor);
			}
		}
		select.getSelectBody().accept(selectVisitor);
		
		if (unsupported && deepParsing) // used to throw exception for the currently unsupported methods
			throw new JSQLParserException(SQLQueryParser.QUERY_NOT_SUPPORTED);
	}
		
	public List<TableJSQL> getTables() {	
		return tables;
	}
	

	private final SelectVisitor selectVisitor = new SelectVisitor() {

		/*Visit the FROM clause to find tables
		 * Visit the JOIN and WHERE clauses to check if nested queries are present
		 * (non-Javadoc)
		 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
		 */
		@Override
		public void visit(PlainSelect plainSelect) {
			plainSelect.getFromItem().accept(fromItemVisitor);

			// When the mapping contains a DISTINCT we interpret it as a HINT to create a subview.
			// Thus we presume that the unusual use of DISTINCT here on done ON PURPOSE 
			// for obtaining this behavior.
			if (plainSelect.getDistinct() != null) {
	            unsupported = true;
	        }

			if (plainSelect.getJoins() != null) {
				for (Join join : plainSelect.getJoins()) 
					join.getRightItem().accept(fromItemVisitor);
			}
			if (plainSelect.getWhere() != null) {
				plainSelect.getWhere().accept(expressionVisitor);
			}
			
			for (SelectItem expr : plainSelect.getSelectItems()) 
				expr.accept(selectItemVisitor);
		}


		/*
		 * Visit UNION, INTERSECT, MINUM and EXCEPT to search for table names
		 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.SetOperationList)
		 */
		@Override
		public void visit(SetOperationList list) {
			unsupported = true;
			for (PlainSelect plainSelect : list.getPlainSelects()) {
				visit(plainSelect);
			}
		}

		@Override
		public void visit(WithItem withItem) {
			otherItemNames.add(withItem.getName().toLowerCase());
			withItem.getSelectBody().accept(this);
		}
	};

	private final FromItemVisitor fromItemVisitor = new FromItemVisitor() {

		/*
		 * Visit Table and store its value in the list of RelationJSQL(non-Javadoc)
		 * We want to maintain duplicate tables to retrieve the different aliases assigned
		 * we use the class TableJSQL to handle quotes and user case choice if present
		 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.schema.Table)
		 */
		
		@Override
		public void visit(Table tableName) {
			if (!otherItemNames.contains(tableName.getFullyQualifiedName().toLowerCase())) {
				// CHNAGES THE ALIAS NAME 
				TableJSQL relation = new TableJSQL(tableName.getSchemaName(), tableName.getName(), tableName.getAlias());
				tables.add(relation);
			}
		}

		@Override
		public void visit(SubSelect subSelect) {
			visitSubSelect(subSelect);
		}

		@Override
		public void visit(SubJoin subjoin) {
			unsupported = true;
			subjoin.getLeft().accept(this);
			subjoin.getJoin().getRightItem().accept(this);
		}

		@Override
		public void visit(LateralSubSelect lateralSubSelect) {
			unsupported = true;
			lateralSubSelect.getSubSelect().getSelectBody().accept(selectVisitor);
		}

		@Override
		public void visit(ValuesList valuesList) {
			unsupported = true;
		}
	};
	
	
	private void visitSubSelect(SubSelect subSelect) {
		if (subSelect.getSelectBody() instanceof PlainSelect) {
			PlainSelect subSelBody = (PlainSelect) (subSelect.getSelectBody());	
			if (subSelBody.getJoins() != null || subSelBody.getWhere() != null) 
				unsupported = true;	
		} 
		else
			unsupported = true;
		
		subSelect.getSelectBody().accept(selectVisitor);
	}
	
	private final SelectItemVisitor selectItemVisitor = new SelectItemVisitor() {
		@Override
		public void visit(AllColumns expr) {
			//Do nothing!
		}

		@Override
		public void visit(AllTableColumns arg0) {
			//Do nothing!
		}

		@Override
		public void visit(SelectExpressionItem expr) {
			expr.getExpression().accept(expressionVisitor);
		}
	};

	private final ExpressionVisitor expressionVisitor = new ExpressionVisitor() {
	
	/*
	 * We do the same procedure for all Binary Expressions
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Addition)
	 */
	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition);
	}

	@Override
	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression);
	}

	@Override
	public void visit(Between between) {
		between.getLeftExpression().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);
	}

	@Override
	public void visit(Column tableColumn) {
		//it does nothing here, everything is good
	}

	@Override
	public void visit(Division division) {
		visitBinaryExpression(division);
	}

	@Override
	public void visit(DoubleValue doubleValue) {
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo);
	}

	@Override
	public void visit(Function function) {
        switch (function.getName().toLowerCase()) {
            case "regexp_like" :
            case "regexp_replace" :
            case "replace" :
            case "concat" :
                for(Expression ex :function.getParameters().getExpressions()) {
                    ex.accept(this);
                }
                break;

            default:
                unsupported = true;
                break;
        }
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
		inExpression.getLeftExpression().accept(this);
		inExpression.getRightItemsList().accept(itemsListVisitor);
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		unsupported = true;
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression);
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		existsExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(LongValue longValue) {
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
	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication);
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo);
	}

	@Override
	public void visit(NullValue nullValue) {
	}

	@Override
	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression);
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
	}

	@Override
	public void visit(StringValue stringValue) {
	}

	@Override
	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction);
	}

	private void visitBinaryExpression(BinaryExpression binaryExpression) {
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(DateValue dateValue) {
	}

	@Override
	public void visit(TimestampValue timestampValue) {
	}

	@Override
	public void visit(TimeValue timeValue) {
	}


	@Override
	public void visit(CaseExpression caseExpression) {
		unsupported = true;
	}


	@Override
	public void visit(WhenClause whenClause) {
		unsupported = true;
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		unsupported = true;
		allComparisonExpression.getSubSelect().getSelectBody().accept(selectVisitor);
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		unsupported = true;
		anyComparisonExpression.getSubSelect().getSelectBody().accept(selectVisitor);
	}

	@Override
	public void visit(Concat concat) {
		visitBinaryExpression(concat);
	}

	@Override
	public void visit(Matches matches) {
		unsupported = true;
		visitBinaryExpression(matches);
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		unsupported = true;
		visitBinaryExpression(bitwiseAnd);
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		unsupported = true;
		visitBinaryExpression(bitwiseOr);
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		unsupported = true;
		visitBinaryExpression(bitwiseXor);
	}

	@Override
	public void visit(CastExpression cast) {
		cast.getLeftExpression().accept(this);
	}

	@Override
	public void visit(Modulo modulo) {
		unsupported = true;
		visitBinaryExpression(modulo);
	}

	@Override
	public void visit(AnalyticExpression analytic) {
		unsupported = true;
	}

	@Override
	public void visit(ExtractExpression eexpr) {
		unsupported = true;
	}

	@Override
	public void visit(IntervalExpression iexpr) {
		unsupported = true;
	}

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
		unsupported = true;
    }

	@Override
	public void visit(OracleHierarchicalExpression arg0) {
		unsupported = true;
		
		
	}

	@Override
	public void visit(RegExpMatchOperator rexpr) {
//		unsupported = true;
//		visitBinaryExpression(rexpr);
	}



	@Override
	public void visit(SignedExpression arg0) {
		System.out.println("WARNING: SignedExpression   not implemented ");
		unsupported = true;
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
	public void visit(SubSelect subSelect) {
		visitSubSelect(subSelect);
	}
	
	};

	
	private final ItemsListVisitor itemsListVisitor = new ItemsListVisitor() {
		@Override
		public void visit(ExpressionList expressionList) {
			for (Expression expression : expressionList.getExpressions()) {
				expression.accept(expressionVisitor);
			}

		}

		@Override
		public void visit(MultiExpressionList multiExprList) {
			unsupported = true;
			for (ExpressionList exprList : multiExprList.getExprList()) {
				exprList.accept(this);
			}
		}

		@Override
		public void visit(SubSelect subSelect) {
			visitSubSelect(subSelect);
		}
	};
	
}
