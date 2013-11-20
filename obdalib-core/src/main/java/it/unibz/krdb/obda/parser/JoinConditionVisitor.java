/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;

public class JoinConditionVisitor implements SelectVisitor {
	
	List<String> joinConditions;
	
	public List<String> getJoinConditions(Select select) {
		joinConditions = new ArrayList<String>();
		select.getSelectBody().accept(this);
		return joinConditions;
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		List<Join> joins = plainSelect.getJoins();
		if (joins != null)
		for (Join join : joins)
			joinConditions.add(join.getOnExpression().toString());
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
