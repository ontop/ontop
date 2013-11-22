/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

import it.unibz.krdb.obda.parser.AliasMapVisitor;
import it.unibz.krdb.obda.parser.JoinConditionVisitor;
import it.unibz.krdb.obda.parser.TablesNameParser;
import it.unibz.krdb.sql.util.BinaryTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;



/**
 * A tree structure to represent SQL query string.
 */
public class ParsedQueryTree extends BinaryTree<RelationalAlgebra> {

	private static final long serialVersionUID = -4590590361733833782L;

	
	private String query;
	private Select select;
	private TablesNameParser tnp;
	/**
	 * Constructs an empty tree.
	 */
	public ParsedQueryTree() {
		super();
	}

	/**
	 * Constructs a node tree with no children.
	 * 
	 * @param value
	 *            the node value.
	 */
	public ParsedQueryTree(RelationalAlgebra value) {
		super(value);
	}
	
	public ParsedQueryTree(String queryString) {
		query = queryString;
		Statement stm;
		try {
			stm = CCJSqlParserUtil.parse(query);
			if (stm instanceof Select) {
				select = (Select)stm;
				tnp = new TablesNameParser();
			}
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Constructs a node tree with two children.
	 * 
	 * @param value
	 *            the node value.
	 * @param left
	 *            the left subtree.
	 * @param right
	 *            the right subtree.
	 */
	public ParsedQueryTree(RelationalAlgebra value, ParsedQueryTree left, ParsedQueryTree right) {
		super(value, left, right);
	}

	@Override
	public ParsedQueryTree root() {
		return (ParsedQueryTree)super.root();
	}

	@Override
	public ParsedQueryTree left() {
		return (ParsedQueryTree)super.left();
	}

	@Override
	public ParsedQueryTree right() {
		return (ParsedQueryTree)super.right();
	}
	
	@Override
	public RelationalAlgebra value() {
		return (RelationalAlgebra)super.value();
	}

	@Override
	public String toString() {
		return print(this); 
	}
	
	/**
	 * Returns all the tables in this query tree.
	 */
	public ArrayList<RelationJSQL> getTableSet() {
		return tnp.getTableList(select);
	}
	
	/**
	 * Get the string construction of alias name. 
	 */
	public HashMap<String, String> getAliasMap() {
		AliasMapVisitor aliasV = new AliasMapVisitor();
		return aliasV.getAliasMap(select);
	}

	/**
	 * Get the string construction of the join condition. The string has the
	 * format of "VAR1=VAR2".
	 */
	public List<String> getJoinCondition() {
		JoinConditionVisitor joinCV = new JoinConditionVisitor();
		return joinCV.getJoinConditions(select);
	}

	/**
	 * Get the object construction for the WHERE clause.
	 */
	public Selection getSelection() {
		return this.value().getSelection();
	}
	
	public Projection getProjection() {
		return this.value().getProjection();
	}
	
	/**
	 * Algorithm for browsing the tree in pre-order traversal.
	 */
	private String print(ParsedQueryTree tree) {
		String statement = "";
		
		RelationalAlgebra node = tree.value();
		String selectClause = getSelectClause(node.getProjection());
		String whereClause = getWhereClause(node.getSelection());
		String groupByClause = getGroupByClause(node.getAggregation());

		statement += String.format("%s %s %s %s", selectClause, node.toString(), whereClause, groupByClause);
		
		ParsedQueryTree left = tree.left();
		String lNode = "";
		if (left != null) {
			lNode = print(left);
		}
		ParsedQueryTree right = tree.right();
		String rNode = "";
		if (right != null) {
			rNode = print(right);
		}
		return String.format(statement, lNode.trim(), rNode.trim());
	}
	
	/**
	 * Constructs the SELECT statement based on the Projection object.
	 */
	private String getSelectClause(Projection prj) {
		String selectClause = "";
		if (prj != null) {
			selectClause = prj.toString();
		}
		return selectClause;
	}

	/**
	 * Constructs the WHERE statement based on the Selection object.
	 */
	private String getWhereClause(Selection slc) {
		String whereClause = "";
		if (slc != null) {
			whereClause = slc.toString();
		}
		return whereClause;
	}
	
	/**
	 * Constructs the GROUP BY statement based on the Aggregation
	 * object.
	 */
	private String getGroupByClause(Aggregation agg) {
		String groupByClause = "";
		if (agg != null) {
			groupByClause = agg.toString();
		}
		return groupByClause;
	}
	
	@Override
	public ParsedQueryTree clone() {
		RelationalAlgebra value = this.value.clone();
		ParsedQueryTree left = null;
		ParsedQueryTree right = null;
		if (!isLeaf()) {
			if (this.left != null) {
				left = this.left().clone();
			}
			if (this.right != null) {
				right = this.right().clone();
			}
		}
		return new ParsedQueryTree(value, left, right);
	}
}
