package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides a storage to put the URI constant.
 */
public class URIConstantImpl extends AbstractLiteral implements URIConstant {

	private static final long serialVersionUID = -1263974895010238519L;


	private final int identifier;

	private final String iristr;

	protected URIConstantImpl(String iri) {
		this.iristr = iri;
		this.identifier = iri.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof URIConstantImpl)) {
			return false;
		}
		URIConstantImpl uri2 = (URIConstantImpl) obj;
		return this.identifier == uri2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public String getURI() {
		return this.iristr;
	}

	@Override
	public URIConstant clone() {
		return this;
	}

	@Override
	public String toString() {
		return TermUtil.toString(this);
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
		throw new RuntimeException("Impossible to cast as atom: " + this.getClass());
	}

	@Override
	public COL_TYPE getType() {
		return COL_TYPE.OBJECT;
	}

	@Override
	public String getValue() {
		return iristr;
	}

	@Override
	public String getLanguage() {
		return null;
	}
}
