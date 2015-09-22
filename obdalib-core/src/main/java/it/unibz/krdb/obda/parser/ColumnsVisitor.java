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

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

/**
 * Visitor to retrieve the list of columns of the given select statement. (SELECT... FROM).
 *
 */

public class ColumnsVisitor implements SelectVisitor, SelectItemVisitor, ExpressionVisitor {
	
	private final List<String> columns = new ArrayList<>(); //create a list of projections if we want to consider union 

	/**
	 * Return the list of columns with the expressions between SELECT and FROM, 
	 * considering also the case of UNION
	 * 
	 * @param select parsed statement
	 * @return
	 */
	
	public ColumnsVisitor(Select select) {	
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
	}
		
	public List<String> getColumns() {
		return columns;	
	}
	

	/*
	 * visit PlainSelect, search for the SelectExpressionItems
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
	 */
	
	@Override
	public void visit(PlainSelect plainSelect) {
	    /*
		working with getProjection we visit the SelectItems and distinguish between select distinct,
		select distinct on, select all 
		*/
		
		Distinct distinct = plainSelect.getDistinct();
		
		if (distinct != null) { // for SELECT DISTINCT [ON (...)]
			if (distinct.getOnSelectItems() != null) {
				for(SelectItem item : distinct.getOnSelectItems())
					item.accept(this);
			}
		}
		
		for (SelectItem item : plainSelect.getSelectItems())
			item.accept(this);
	}

	/* visit also the Operation as UNION*/
	@Override
	public void visit(SetOperationList setOpList) {
		// NOTE: don't use for loop, as loop will result duplicated columns
		//for (PlainSelect ps: setOpList.getPlainSelects())
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
	 * We do not add columns in the case of SELECT *
	 * @see net.sf.jsqlparser.statement.select.SelectItemVisitor#visit(net.sf.jsqlparser.statement.select.AllColumns)
	 */
	@Override
	public void visit(AllColumns allColumns) {
		//do not store anything
		
	}

	/*
	 * We do not add columns in the case of SELECT table.*
	 * @see net.sf.jsqlparser.statement.select.SelectItemVisitor#visit(net.sf.jsqlparser.statement.select.AllTableColumns)
	 */
	@Override
	public void visit(AllTableColumns allTableColumns) {	
		//do not store anything
		
		
	}


	@Override
	public void visit(SelectExpressionItem selectExpr) {

		/*
		 * Here we found a column
		 */
		if (selectExpr.getAlias() != null) {
			columns.add(selectExpr.getAlias().getName());
		}
		else {
			selectExpr.getExpression().accept(this);
		}
	}
	
	/*
	 * Visit the column 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
	 */
	@Override
	public void visit(Column tableColumn) {
		columns.add(tableColumn.getColumnName());
	}
	

	
	
	@Override
	public void visit(NullValue nullValue) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void visit(Function function) {
		
		
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
		parenthesis.getExpression().accept(this);
		
	}

	@Override
	public void visit(StringValue stringValue) {
		
		
	}

	@Override
	public void visit(Addition addition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Division division) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Multiplication multiplication) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Subtraction subtraction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AndExpression andExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OrExpression orExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Between between) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InExpression inExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinorThan minorThan) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat concat) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Matches matches) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CastExpression cast) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Modulo modulo) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(RegExpMatchOperator arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(SignedExpression arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(JsonExpression arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(RegExpMySQLOperator arg0) {
		// TODO Auto-generated method stub
		
	}

	

	

}
