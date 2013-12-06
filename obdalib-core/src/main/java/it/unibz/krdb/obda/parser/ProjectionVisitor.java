package it.unibz.krdb.obda.parser;

import java.util.ArrayList;
import java.util.List;

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
 */

public class ProjectionVisitor implements SelectVisitor, SelectItemVisitor{
	
//	ArrayList<ProjectionJSQL> projections; //create a list of projections if we want to consider union 
	ProjectionJSQL projection;
	boolean bdistinctOn = false;
	boolean setProj = false;
	
	
	/**
	 * Return the list of Projection with the expressions between SELECT and FROM
	 * @param select parsed statement
	 * @return
	 */
	
	public ProjectionJSQL getProjection(Select select) {
		
//		projections = new ArrayList<ProjectionJSQL>(); //used if we want to consider UNION
		
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
		
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
		if(setProj==true){
			
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
		
		else{ //working with getProjection
		
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
//		projections.add(projection);
		
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
	 
		
	}

	

	

}
