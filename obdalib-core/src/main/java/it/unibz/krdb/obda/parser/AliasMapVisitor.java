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

public class AliasMapVisitor implements SelectVisitor, SelectItemVisitor{

	HashMap<String,String> aliasMap;
	
	public HashMap<String,String> getAliasMap(Select select) {
		aliasMap = new HashMap<String, String>();
		select.getSelectBody().accept(this);
		return aliasMap;
	}
	
	@Override
	public void visit(PlainSelect plainSelect) {
		for (SelectItem item : plainSelect.getSelectItems())
		{
			item.accept(this);
		}
	}

	@Override
	public void visit(AllColumns columns) {
		columns.accept(this);
		
	}

	@Override
	public void visit(AllTableColumns tableColumns) {
		tableColumns.accept(this);
	}

	@Override
	public void visit(SelectExpressionItem selectExpr) {
		if (selectExpr.getAlias() != null)
			aliasMap.put(selectExpr.toString(), selectExpr.getAlias());
	}

	@Override
	public void visit(SetOperationList arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WithItem arg0) {
		// TODO Auto-generated method stub
		
	}

}
