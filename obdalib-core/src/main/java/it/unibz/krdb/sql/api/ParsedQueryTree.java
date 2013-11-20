/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

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
		ArrayList<RelationJSQL> tableList = new ArrayList<RelationJSQL>();
		Stack<ParsedQueryTree> nodes = new Stack<ParsedQueryTree>();
		
		Statement statement=null;
		try {
			statement = CCJSqlParserUtil.parse("SELECT * FROM MY_TABLE1");
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Select selectStatement = (Select) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<Table> tables =null;
//		tables= tablesNamesFinder.getTableList(selectStatement);
		for( Table table: tables)
		{
			tableList.add(new RelationJSQL(table));
		}

//		nodes.push(this);
//		QueryTree currentNode = null;
//		while (!nodes.isEmpty()) {
//			currentNode = nodes.pop();
//			QueryTree right = currentNode.right();
//			if (right != null) {
//				nodes.push(right);
//			}
//			QueryTree left = currentNode.left();
//			if (left != null) {
//				nodes.push(left);
//			}
//			if (currentNode.isLeaf()) {
//				tableList.add((Relation) currentNode.value());
//			}
//		}
		return tableList;
	}
	
	/**
	 * Get the string construction of alias name. The string has the format of
	 * "ALIAS_NAME=COLUMN_NAME"
	 */
	public HashMap<String, String> getAliasMap() {
		HashMap<String, String> aliasMap = new HashMap<String, String>();
		Stack<ParsedQueryTree> nodes = new Stack<ParsedQueryTree>();

		nodes.push(this);
		ParsedQueryTree currentNode = null;
		while (!nodes.isEmpty()) {
			currentNode = nodes.pop();
			ParsedQueryTree right = currentNode.right();
			if (right != null) {
				nodes.push(right);
			}
			ParsedQueryTree left = currentNode.left();
			if (left != null) {
				nodes.push(left);
			}

			Projection prj = currentNode.value().getProjection();
			if (prj != null) {
				ArrayList<DerivedColumn> selectList = prj.getColumnList();
				for (DerivedColumn selection : selectList) {
					if (selection == null) { // an asterisk was found
						break;
					}
					if (selection.hasAlias()) { // check if the column has an alias name
						AbstractValueExpression exp = selection.getValueExpression();
						if (exp instanceof ReferenceValueExpression) {
							ColumnReference reference = exp.get(0);
							aliasMap.put(reference.toString(), selection.getAlias());
						}
					}
				}
			}
		}
		return aliasMap;
	}

	/**
	 * Get the string construction of the join condition. The string has the
	 * format of "VAR1=VAR2".
	 */
	public ArrayList<String> getJoinCondition() {
		ArrayList<String> equalConditions = new ArrayList<String>();
		Stack<ParsedQueryTree> nodes = new Stack<ParsedQueryTree>();

		nodes.push(this);
		ParsedQueryTree currentNode = null;
		while (!nodes.isEmpty()) {
			currentNode = nodes.pop();
			ParsedQueryTree right = currentNode.right();
			if (right != null) {
				if (!right.isLeaf()) {
					nodes.push(right);
				}
			}
			ParsedQueryTree left = currentNode.left();
			if (left != null) {
				if (!left.isLeaf()) {
					nodes.push(left);
				}
			}

			RelationalAlgebra operator = currentNode.value();
			if (operator instanceof JoinOperator) {
				JoinOperator joinOp = (JoinOperator) operator;

				// Cross join has a different approach to define conditions in
				// which they are defined in the selection "where" clause.
				if (joinOp.getType() != JoinOperator.CROSS_JOIN) {
					for (int index = 0; index < joinOp.conditionSize(); index++) {
						ComparisonPredicate predicate = joinOp.getCondition(index);
						String leftReference = predicate.getValueExpressions()[0].toString();
						String rightReference = predicate.getValueExpressions()[1].toString();
						equalConditions.add(String.format("%s=%s", leftReference, rightReference));
					}
				}
			}
		}
		return equalConditions;
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
