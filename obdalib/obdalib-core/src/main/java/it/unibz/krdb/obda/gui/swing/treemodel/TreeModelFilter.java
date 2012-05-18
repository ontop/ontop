package it.unibz.krdb.obda.gui.swing.treemodel;

/**
 * An abstract filter that can be used by TreeModels to discriminate certain
 * elements stored in the controller associated to the tree model.
 * 
 * @param <T>
 *            T is the class name of the kind of object that is taken as input
 *            by an implementation of TreeModelFilter.
 */
public abstract class TreeModelFilter<T extends Object> {

	protected boolean bNegation;
	protected String[] vecKeyword;

	public void putNegation() {
		bNegation = true;
	}

	public void addStringFilter(String[] values) {
		vecKeyword = values;
	}

	/**
	 * A matching function that returns true if the object complies to the
	 * internal logic of the Filter. Instances of TreeModelFilter should define
	 * their own logic.
	 * 
	 * @param object
	 *            The object that needs to be matches
	 * @return true if the object matches the internal logic, false otherwise
	 */
	public abstract boolean match(T object);

}