package it.unibz.krdb.sql.api;

import it.unibz.krdb.sql.util.BinaryTree;

/**
 * A tree structure to represent SQL query string.
 */
public class QueryTree extends BinaryTree<RelationalAlgebra> {
	
	/**
	 * Constructs an empty tree.
	 */
	public QueryTree() {
		super();
	}

	/**
	 * Constructs a node tree with no children.
	 * 
	 * @param value
	 *            the node value.
	 */
	public QueryTree(RelationalAlgebra value) {
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
	public QueryTree(RelationalAlgebra value, QueryTree left, QueryTree right) {
		super(value, left, right);
	}

	@Override
	public QueryTree root() {
		return (QueryTree)super.root();
	}

	@Override
	public QueryTree left() {
		return (QueryTree)super.left();
	}

	@Override
	public QueryTree right() {
		return (QueryTree)super.right();
	}

	@Override
	public String toString() {
		return preOrder(this); 
	}
	
	/**
	 * Algorithm for browsing the tree in pre-order traversal.
	 */
	public String preOrder(QueryTree tree) {		
		String statement = "";
		
		RelationalAlgebra node = tree.value();
		String selectClause = getSelectClause(node.getProjection());
		String whereClause = getWhereClause(node.getSelection());
		String groupByClause = getGroupByClause(node.getAggregation());

		statement += String.format("%s %s %s %s", selectClause, node.toString(), whereClause, groupByClause);
		
		QueryTree left = tree.left();
		String lNode = "";
		if (left != null) {
			lNode = preOrder(left);
		}
		QueryTree right = tree.right();
		String rNode = "";
		if (right != null) {
			rNode = right.preOrder(right);
		}
		return String.format(statement, lNode, rNode);
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
	public QueryTree clone() {
		RelationalAlgebra value = this.value.clone();
		QueryTree left = null;
		QueryTree right = null;

		if (!isLeaf()) {
			if (this.left != null) {
				left = this.left().clone();
			}
			if (this.right != null) {
				right = this.right().clone();
			}
		}
		return new QueryTree(value, left, right);
	}
}
