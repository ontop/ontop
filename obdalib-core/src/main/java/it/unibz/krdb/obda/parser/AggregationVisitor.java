/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.api.Aggregation;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;


/**
 * Find all the table used for GROUP BY in the statement.
 */

public class AggregationVisitor implements SelectVisitor {

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
	
	@Override
	public void visit(PlainSelect plainSelect) {
		
		if(plainSelect.getGroupByColumnReferences()!=null)
			
			aggregation.addAll(plainSelect.getGroupByColumnReferences());
		
	}

	@Override
	public void visit(SetOperationList setOpList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WithItem withItem) {
		// TODO Auto-generated method stub
		
	}

	

	
}
