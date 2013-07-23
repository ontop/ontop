package it.unibz.krdb.sql.util;

import java.io.Serializable;

/**
 * This class implements a single node of a binary tree. The construction
 * follows a recursive structure. The relationship consists of left, right and
 * parent references.
 */
public abstract class BinaryTree<T> implements Cloneable, Serializable {
	
	private static final long serialVersionUID = -1123469861254589828L;

	/**
	 * The value associated with this node.
	 */
	protected T value;

	/**
	 * The left child of this node.
	 */
	protected BinaryTree<T> left;

	/**
	 * The right child of this node.
	 */
	protected BinaryTree<T> right;

	/**
	 * The parent of this node.
	 */
	protected BinaryTree<T> parent;

	/**
	 * A default constructor, for constructing an empty tree.
	 */
	public BinaryTree() {
		this(null, null, null);
	}

	/**
	 * Constructs a node tree with no children.
	 * 
	 * @param value
	 *            the node value.
	 */
	public BinaryTree(T value) {
		this(value, null, null);
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
	public BinaryTree(T value, BinaryTree<T> left, BinaryTree<T> right) {
		setValue(value);
		attachLeft(left);
		attachRight(right);
	}

	/**
	 * Sets the value associated with this node.
	 * 
	 * @param node
	 *            the new value associated to this node, replacing the existing
	 *            value.
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * Attaches given tree (root node of tree) as the left child of this node.
	 * 
	 * @param tree
	 *            tree (root node) to be attached.
	 */
	public void attachLeft(BinaryTree<T> newLeft) {
		if (isEmpty()) {
			return;
		}
		
		if (left != null && left.parent() == this) {
			left.setParent(null);
		}
		left = newLeft;

		if (left != null) {
			left.setParent(this);
		}
	}

	/**
	 * Attaches given tree (root node of tree) as the right child of this node.
	 * 
	 * @param tree
	 *            tree (root node) to be attached.
	 */
	public void attachRight(BinaryTree<T> newRight) {
		if (isEmpty()) {
			return;
		}
		
		if (right != null && right.parent() == this) {
			right.setParent(null);
		}
		right = newRight;

		if (right != null) {
			right.setParent(this);
		}
	}

	/**
	 * Updates the parent of this node
	 * 
	 * @param newParent
	 *            a reference to the new parent of this node.
	 */
	protected void setParent(BinaryTree<T> newParent) {
		if (!isEmpty()) {
			parent = newParent;
		}
	}

	/**
	 * Gets the value at this node.
	 * 
	 * @return the value at this node.
	 */
	public T value() {
		return value;
	}

	/**
	 * Gets left subtree of current node.
	 * 
	 * @return the left subtree of this node.
	 */
	public BinaryTree<T> left() {
		return left;
	}

	/**
	 * Gets right subtree of current node.
	 * 
	 * @return the right subtree of this node.
	 */
	public BinaryTree<T> right() {
		return right;
	}

	/**
	 * Gets reference to parent of this node.
	 * 
	 * @return the reference to parent of this node.
	 */
	public BinaryTree<T> parent() {
		return parent;
	}

	/**
	 * Gets the leftmost subtree (i.e., the leaf) of current node.
	 * 
	 * @return the leftmost leaf of this node.
	 */
	public BinaryTree<T> leftmost() {
		if (isLeaf()) { // this itself is the leftmost leaf
			return this;
		}

		BinaryTree<T> node = this.left();
		while (!node.isLeaf()) {
			node = node.left();
		}
		return node;
	}

	/**
	 * Gets the rightmost subtree (i.e., the leaf) of current node.
	 * 
	 * @return the rightmost leaf of this node.
	 */
	public BinaryTree<T> rightmost() {
		if (isLeaf()) { // this itself is the rightmost leaf
			return this;
		}

		BinaryTree<T> node = this.right();
		while (!node.isLeaf()) {
			node = node.right();
		}
		return node;
	}

	/**
	 * Returns the number of descendants of node.
	 * 
	 * @return Size of the tree.
	 */
	public int size() {
		if (isEmpty()) {
			return 0;
		}
		return left().size() + right().size() + 1;
	}

	/**
	 * Compute the depth of a node. The depth is the path length from a node to
	 * the root
	 * 
	 * @return The path length to the root of a tree.
	 */
	public int depth() {
		if (parent() == null) {
			return 0;
		}
		return 1 + parent.depth();
	}

	/**
	 * Detaches and returns the left child of this node.
	 * 
	 * @return the left subtree of this node.
	 */
	public BinaryTree<T> detachLeft() {
		BinaryTree<T> detached = left();
		left = null;
		return detached;
	}

	/**
	 * Detaches and returns the right child of this node.
	 * 
	 * @return the right subtree of this node.
	 */
	public BinaryTree<T> detachRight() {
		BinaryTree<T> detached = right();
		right = null;
		return detached;
	}

	/**
	 * Performs a left rotation of tree where this node serves as the root and
	 * its right child serves as the pivot.
	 */
	public void rotateLeft() {
		BinaryTree<T> parent = parent();
		BinaryTree<T> pivot = right();
		boolean isChild = (parent != null);
		boolean isRightChild = isRightChild();

		attachRight(pivot.left());
		pivot.attachLeft(this);

		if (isChild) {
			if (isRightChild) {
				parent.attachRight(pivot);
			}
			else {
				parent.attachLeft(pivot);
			}
		}
	}

	/**
	 * Performs a left rotation of tree where this node serves as the root and
	 * its left child serves as the pivot.
	 */
	public void rotateRight() {
		BinaryTree<T> parent = parent();
		BinaryTree<T> pivot = left();
		boolean isChild = (parent != null);
		boolean isLeftChild = isLeftChild();

		attachLeft(pivot.right());
		pivot.attachRight(this);

		if (isChild) {
			if (isLeftChild) {
				parent.attachLeft(pivot);
			}
			else {
				parent.attachRight(pivot);
			}
		}
	}

	/**
	 * Tells whether this tree is empty or not.
	 * 
	 * @return true if there is no value in this tree, or false otherwise.
	 */
	public boolean isEmpty() {
		return value == null;
	}

	/**
	 * Tells whether this tree is the root or not.
	 * 
	 * @return true if the tree has no parent, or false otherwise.
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * Tells whether this tree is the leaf of not.
	 * 
	 * @return true if the tree has no child either in its left or right.
	 */
	public boolean isLeaf() {
		return (left == null && right == null);
	}

	/**
	 * Returns the root of the tree that contains this node.
	 * 
	 * @return the root of the tree.
	 */
	public BinaryTree<T> root() {
		if (isRoot()) { // this itself is the root
			return this;
		}

		BinaryTree<T> tree = parent();
		while (tree.parent() != null) {
			tree = tree.parent();
		}
		return tree;
	}

	/**
	 * Determines if this node is a left child.
	 * 
	 * @return true if this node is a left child of parent.
	 */
	public boolean isLeftChild() {
		if (parent == null) {
			return false;
		}
		return this.equals(parent.left());
	}

	/**
	 * Determines if this node is a right child.
	 * 
	 * @return true if this node is a right child of parent.
	 */
	public boolean isRightChild() {
		if (parent == null) {
			return false;
		}
		return equals(parent.right());
	}

	/**
	 * Prints the string representation of the tree.
	 */
	public String toString() {
		String str = "";
		if (left != null) {
			str += left.value().toString() + ", ";
		}		
		str += value().toString();
		if (right != null) {
			str += ", " + right.value().toString();
		}		
		return str;
	}

	/**
	 * Creates and returns a copy of this object.
	 */
	public abstract BinaryTree<T> clone();
}
