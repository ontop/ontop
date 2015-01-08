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

import it.unibz.krdb.sql.api.ParsedSQLQuery;
import it.unibz.krdb.sql.api.ProjectionJSQL;
import it.unibz.krdb.sql.api.TableJSQL;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
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
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * Visitor to retrieve the projection of the given select statement. (SELECT... FROM).
 * We remove the quotes for each column it they are present.
 * Since the current release does not support Function, we throw a ParserException, when a function is present
 *
 */

public class ProjectionVisitor implements SelectVisitor, SelectItemVisitor, ExpressionVisitor{
	
//	ArrayList<ProjectionJSQL> projections; //create a list of projections if we want to consider union 
	ProjectionJSQL projection;
	boolean bdistinctOn = false; // true when a SELECT distinct is present
	boolean setProj = false; // true when we are using the method setProjection
	boolean notSupported = false; 

	
	
	/**
	 * Return the list of Projection with the expressions between SELECT and FROM
	 * @param select parsed statement
	 * @return
	 * @throws JSQLParserException 
	 */
	
	public ProjectionJSQL getProjection(Select select, boolean deepParsing) throws JSQLParserException {
		
//		projections = new ArrayList<ProjectionJSQL>(); //used if we want to consider UNION

		
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
		
		if(notSupported && deepParsing) // used to throw exception for the currently unsupported methods
				throw new JSQLParserException("Query not yet supported");
		
		return projection;	
		
	}
	
	/**
	 *  Modify the Select clause using the values of the Projection
	 * @param select parsed statement
	 * @param proj anew projection expression between SELECT and FROM
	 */
	public void setProjection(Select select, ProjectionJSQL proj) {
		
		
		setProj= true;
		projection=proj;
		
		select.getSelectBody().accept(this);
		
	}

	/*
	 * visit Plainselect, search for the SelectExpressionItems
	 * Stored in ProjectionSQL 
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
	 */
	
	@Override
	public void visit(PlainSelect plainSelect) {
		/*
		 * We first check if we are setting a new projection for SELECT clause, 
		 * we distinguish between select, select distinct and select distinct on 
		 */
		if(setProj){
			
			if(projection.getType().equals("select distinct on")){
				Distinct distinct = new Distinct();
				List<SelectItem> distinctList= new ArrayList<SelectItem>();
				
				for(SelectExpressionItem seItem :projection.getColumnList()){
					distinctList.add(seItem);
				}
				distinct.setOnSelectItems(distinctList);
				plainSelect.setDistinct(distinct);
			}
			
			else if(projection.getType().equals("select distinct")){
				Distinct distinct = new Distinct();
				plainSelect.setDistinct(distinct);
				plainSelect.getSelectItems().clear();
				plainSelect.getSelectItems().addAll(projection.getColumnList());
			}
			
			else{
			plainSelect.getSelectItems().clear();
			plainSelect.getSelectItems().addAll(projection.getColumnList());
			}
			
			}
		
		else{ /*
		working with getProjection we visit the SelectItems and distinguish between select distinct,
		select distinct on, select all 
		*/
		projection= new ProjectionJSQL();
		Distinct distinct= plainSelect.getDistinct();
		
		if(distinct!=null) // for SELECT DISTINCT [ON (...)]
			{
			
			if(distinct.getOnSelectItems()!=null){
				
				bdistinctOn=true;
				
				
			for(SelectItem item : distinct.getOnSelectItems())
			{
				item.accept(this);
			}
				bdistinctOn=false;
			}
			else
			projection.setType(ProjectionJSQL.SELECT_DISTINCT);
				
		}
		
		
		for (SelectItem item : plainSelect.getSelectItems())
		{
			item.accept(this);
		}
		
		}
		
	}

	/* visit also the Operation as UNION
	 * it is not supported now */
	@Override
	public void visit(SetOperationList setOpList) { 
		notSupported = true;
		setOpList.getPlainSelects().get(0).accept(this);
//		for (PlainSelect ps: setOpList.getPlainSelects())
//		{
//			ps.accept(this);
//		}
		
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
	 * Add the projection for the selectExpressionItem, distinguing between select all and select distinct
	 * @see net.sf.jsqlparser.statement.select.SelectItemVisitor#visit(net.sf.jsqlparser.statement.select.SelectExpressionItem)
	 */
	@Override
	public void visit(SelectExpressionItem selectExpr) {
	 projection.add(selectExpr, bdistinctOn);
	 selectExpr.getExpression().accept(this);
	
	 
		
	}

	@Override
	public void visit(NullValue nullValue) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * The system cannot support function currently (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.Function)
	 */
	@Override
	public void visit(Function function) {
		notSupported=true;
		
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		notSupported = true;
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
		notSupported=true;
		
	}

	@Override
	public void visit(Addition addition) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Division division) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Multiplication multiplication) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Subtraction subtraction) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AndExpression andExpression) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OrExpression orExpression) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Between between) {
		between.getLeftExpression().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);
		
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InExpression inExpression) {
		Expression e = inExpression.getLeftExpression();
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
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinorThan minorThan) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	/*
	 * Visit the column and remove the quotes if they are present(non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
	 */
	@Override
	public void visit(Column tableColumn) {
		String columnName= tableColumn.getColumnName();
		if(ParsedSQLQuery.pQuotes.matcher(columnName).matches())
			tableColumn.setColumnName(columnName.substring(1, columnName.length()-1));
				
		Table table= tableColumn.getTable();
		if(table.getName()!=null){
			
			TableJSQL fixTable = new TableJSQL(table); //create a tablejsql that recognized between quoted and unquoted tables
			table.setAlias(fixTable.getAlias());
			table.setName(fixTable.getTableName());
			table.setSchemaName(fixTable.getSchema());
		
		}
		
		
	}

	@Override
	public void visit(SubSelect subSelect) {
		if (subSelect.getSelectBody() instanceof PlainSelect) {

			PlainSelect subSelBody = (PlainSelect) (subSelect.getSelectBody());

			if (subSelBody.getJoins() != null || subSelBody.getWhere() != null) {
				notSupported = true;
			} else {
				subSelBody.accept(this);
			}
		} else
			notSupported = true;

	}

	@Override
	public void visit(CaseExpression caseExpression) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause whenClause) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat concat) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Matches matches) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		// TODO Auto-generated method stub
		notSupported = true;
		
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CastExpression cast) {
		
		
	}

	@Override
	public void visit(Modulo modulo) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnalyticExpression aexpr) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExtractExpression eexpr) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IntervalExpression iexpr) {
		notSupported = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
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

	

	

}
