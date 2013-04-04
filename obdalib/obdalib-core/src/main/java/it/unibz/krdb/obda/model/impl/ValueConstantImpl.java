package it.unibz.krdb.obda.model.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public class ValueConstantImpl extends AbstractLiteral implements ValueConstant {

	private static final long serialVersionUID = 8031338451909170400L;

	private final String value;

	private final String language;

	private final Predicate.COL_TYPE type;

	private final int identifier;

	private String string = null;

	/**
	 * The default constructor.
	 * 
	 * @param value
	 *            the constant value.
	 * @param type
	 *            the constant type.
	 */
	protected ValueConstantImpl(String value, Predicate.COL_TYPE type) {
		this(value, null, type);
	}

	protected ValueConstantImpl(String value, String language, Predicate.COL_TYPE type) {
		this.value = value;
		this.language = language;
		this.type = type;
		this.string = toString();
		this.identifier = string.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ValueConstantImpl)) {
			return false;
		}
		if (this == OBDAVocabulary.NULL) {
			return false;
		}
		ValueConstantImpl value2 = (ValueConstantImpl) obj;
		return this.identifier == value2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public ValueConstant clone() {
		return this;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public Predicate.COL_TYPE getType() {
		return type;
	}

	@Override
	public String toString() {
		if (string != null) {
			return string;
		}
		StringBuffer bf = new StringBuffer();
		bf.append("\"");
		bf.append(value);
		bf.append("\"");
		if (type == COL_TYPE.LITERAL_LANG) {
			bf.append("@");
			bf.append(language);
		} else if (type != COL_TYPE.LITERAL) {
			bf.append("^^");
			bf.append(type);
		}
		return bf.toString();
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
}
