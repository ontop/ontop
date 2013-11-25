package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.api.ProjectionJSQL;
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
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * Visitor to retrieve the projection of the given select statement. (SELECT... FROM).
 * 
 * @author Sarah
 *
 */

public class ProjectionVisitor implements SelectVisitor, SelectItemVisitor{
	
	ProjectionJSQL projections;
	boolean distinctOn = false;
	
	/**
	 * Return the Projection with the expressions between SELECT and FROM
	 * @param select parsed statement
	 * @return
	 */
	
	public ProjectionJSQL getProjection(Select select) {
		
		projections = new ProjectionJSQL();
		
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
		
		return projections;	
		
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		
		Distinct distinct= plainSelect.getDistinct();
		if(distinct!=null) // for SELECT DISTINCT [ON (...)]
			{
			
			if(distinct.getOnSelectItems()!=null){
				
				distinctOn=true;
				projections.setType(ProjectionJSQL.SELECT_DISTINCT_ON);
				
			for(SelectItem item : distinct.getOnSelectItems())
			{
				item.accept(this);
			}
				distinctOn=false;
			}
			else
				projections.setType(ProjectionJSQL.SELECT_DISTINCT);
		}
		
		
		for (SelectItem item : plainSelect.getSelectItems())
		{
			item.accept(this);
		}
		
	}

	@Override
	public void visit(SetOperationList setOpList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WithItem withItem) {
		withItem.getSelectBody().accept(this);
		
	}
	@Override
	public void visit(AllColumns allColumns) {
		projections.add(allColumns);
		
	}

	@Override
	public void visit(AllTableColumns allTableColumns) {
		projections.add(allTableColumns);
		
	}

	@Override
	public void visit(SelectExpressionItem selectExpr) {
		
	 projections.add(selectExpr, distinctOn);
		
	}

	

}
