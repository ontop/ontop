package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ValueConstantImpl extends AbstractLiteral implements ValueConstant {

	private static final long serialVersionUID = 8031338451909170400L;

	private final String value;

	private final String language;

	private final Predicate.COL_TYPE type;
	
	private int hashcode = -1;

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
		this.hashcode = toString().hashCode();
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
		return this.hashCode() == value2.hashCode();
	}

	@Override
	public int hashCode() {		
		return hashcode;
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

}
