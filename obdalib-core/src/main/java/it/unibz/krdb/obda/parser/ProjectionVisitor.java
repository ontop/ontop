package it.unibz.krdb.obda.parser;

import java.util.ArrayList;

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
 * Usually only one projection is returned, except when we have a union
 * 
 * @author Sarah
 *
 */

public class ProjectionVisitor implements SelectVisitor, SelectItemVisitor{
	
	ArrayList<ProjectionJSQL> projections; //create a list of projections 
	ProjectionJSQL projection;
	boolean bdistinctOn = false;
	
	
	/**
	 * Return the list of Projection with the expressions between SELECT and FROM
	 * @param select parsed statement
	 * @return
	 */
	
	public ArrayList<ProjectionJSQL> getProjection(Select select) {
		
		projections = new ArrayList<ProjectionJSQL>();
		
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
		
		projections.add(projection);
		
	}

	/* visit also the Operation as UNION */
	@Override
	public void visit(SetOperationList setOpList) { 
		for (PlainSelect ps: setOpList.getPlainSelects())
		{
			ps.accept(this);
		}
		
	}

	@Override
	public void visit(WithItem withItem) {
		withItem.getSelectBody().accept(this);
		
	}
	@Override
	public void visit(AllColumns allColumns) {
		projection.add(allColumns);
		
	}

	@Override
	public void visit(AllTableColumns allTableColumns) {	
		projection.add(allTableColumns);
		
		
	}

	@Override
	public void visit(SelectExpressionItem selectExpr) {
	 projection.add(selectExpr, bdistinctOn);
	 
		
	}

	

}
