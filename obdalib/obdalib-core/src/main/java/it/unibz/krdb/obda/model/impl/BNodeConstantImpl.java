package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.BNode;

/**
 * Implementation for BNodes.
 */
public class BNodeConstantImpl implements BNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 214867118996974157L;

	/**
	 * 
	 */
	private final String name;

	private final int identifier;

	/**
	 * The default constructor.
	 * 
	 * @param uri
	 *            URI from a term.
	 */
	protected BNodeConstantImpl(String name) {
		this.name = name;
		this.identifier = name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || !(obj instanceof BNodeConstantImpl))
			return false;

		BNodeConstantImpl uri2 = (BNodeConstantImpl) obj;
		return this.identifier == uri2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BNode clone() {
		return this;
	}

	@Override
	public String toString() {
		return name;
	}
}
