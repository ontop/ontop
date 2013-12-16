package it.unibz.krdb.obda.parser;

import java.util.ArrayList;
import java.util.List;

import it.unibz.krdb.sql.api.ProjectionJSQL;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
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
	
	public ProjectionJSQL getProjection(Select select) throws JSQLParserException {
		
//		projections = new ArrayList<ProjectionJSQL>(); //used if we want to consider UNION
		
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
		
		if(notSupported) // used to throw exception for the currently unsupported methods
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
		parenthesis.getExpression().accept(this);
		
	}

	@Override
	public void visit(StringValue stringValue) {
		// TODO Auto-generated method stub
		
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

	/*
	 * Visit the column and remove the quotes if they are present(non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
	 */
	@Override
	public void visit(Column tableColumn) {
		String tableName= tableColumn.getColumnName();
		if(tableName.contains("\""))
			tableColumn.setColumnName(tableName.substring(1, tableName.length()-1));
				
		
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

	

	

}
