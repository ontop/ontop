package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.api.AggregationJSQL;
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
 * Find all the table used for GROUP BY in the statement.
 */

public class AggregationVisitor implements SelectVisitor, FromItemVisitor {

	AggregationJSQL aggregation= new AggregationJSQL();
	
	/**
	 * Return a {@link AggregationSQL} containing GROUP BY statement
	 * @param select 
	 * @return
	 */
	public AggregationJSQL getAggregation(Select select){
		
		
		
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);

		return aggregation;
		
	}
	
	/*
	 * visit Plainselect, search for the group by structure that returns a list of Expression
	 * Stored in AggregationJSQL. 
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
	 */
	
	@Override
	public void visit(PlainSelect plainSelect) {
		plainSelect.getFromItem().accept(this);
		
		if(plainSelect.getGroupByColumnReferences()!=null)
			
			aggregation.addAll(plainSelect.getGroupByColumnReferences());
		
	}

	@Override
	public void visit(SetOperationList setOpList) {
		// until now we are not considering the case of UNION statement 
		
	}

	@Override
	public void visit(WithItem withItem) {
		//we are not considering the subquery with WITH
		
	}

	@Override
	public void visit(Table tableName) {
		// we do not execute anything
		
	}
	
	/*
	 * we search for nested group by in SubSelect
	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.SubSelect)
	 */

	@Override
	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(this);
		
	}

	@Override
	public void visit(SubJoin subjoin) {
		//we do not execute anything
		
	}

	/*
	 * we search for nested group by in SubSelect
	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.LateralSubSelect)
	 */
	@Override
	public void visit(LateralSubSelect lateralSubSelect) {
		lateralSubSelect.getSubSelect().getSelectBody().accept(this);
		
	}

	@Override
	public void visit(ValuesList valuesList) {
		// we do not execute anything
		
	}

	

	
}
