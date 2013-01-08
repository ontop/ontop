package it.unibz.krdb.obda.model.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Variable;

/**
 * Implementation for BNodes.
 */
public class BNodeConstantImpl extends AbstractLiteral implements BNode {

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

	@Override
	public Set<Variable> getReferencedVariables() {
		return new LinkedHashSet<Variable>();
	}

	@Override
	public Map<Variable, Integer> getVariableCount() {
		return new HashMap<Variable, Integer>();
	}

	@Override
	public Atom asAtom() {
		throw new RuntimeException("Impossible to cast as atom: "
				+ this.getClass());
	}

	@Override
	public COL_TYPE getType() {
		return COL_TYPE.BNODE;
	}

	@Override
	public String getValue() {
		return name;
	}

	@Override
	public String getLanguage() {
		return null;
	}
}
