/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.parser;

import java.util.HashMap;

/** 
 * Class to create an Alias Map for the select statement
 */

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * 
 * Find all the references between the column names and their aliases,
 * visiting the {@link SelectItem}.
 *
 */

public class AliasMapVisitor implements SelectVisitor, SelectItemVisitor{

	HashMap<String,String> aliasMap;
	
	/**
	 * Return a map between the column in the select statement and its alias.
	 * @param select parsed query 
	 * @return alias map
	 */
	
	public HashMap<String,String> getAliasMap(Select select) {
		
		aliasMap = new HashMap<String, String>();
		
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		
		select.getSelectBody().accept(this);
		return aliasMap;
	}
	
	/*
	 * visit Plainselect, search for the content in select
	 * Stored in AggregationJSQL. 
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
	 */
	
	@Override
	public void visit(PlainSelect plainSelect) {
		
		for (SelectItem item : plainSelect.getSelectItems())
		{
			item.accept(this);
		}
	}

	@Override
	public void visit(AllColumns columns) {
		//we are not interested in all columns (SELECT *) since it does not have aliases
		
	}

	@Override
	public void visit(AllTableColumns tableColumns) {
		//we are not interested in all table columns (SELECT table.*) since it does not have aliases
	}
	
	/*
	 *visit SelectExpressionItem that contains the expression and its alias as in SELECT expr1 AS EXPR
	 * @see net.sf.jsqlparser.statement.select.SelectItemVisitor#visit(net.sf.jsqlparser.statement.select.SelectExpressionItem)
	 */

	@Override
	public void visit(SelectExpressionItem selectExpr) {
		if (selectExpr.getAlias() != null) {
			Expression e = selectExpr.getExpression();
			
			aliasMap.put(e.toString(), selectExpr.getAlias());
	
		}
	}
	@Override
	public void visit(SetOperationList arg0) {
		// we are not considering UNION case
		
	}

	@Override
	public void visit(WithItem arg0) {
		// we are not considering withItem case
		
	}

}
