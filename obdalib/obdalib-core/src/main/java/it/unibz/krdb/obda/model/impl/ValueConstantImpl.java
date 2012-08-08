package it.unibz.krdb.obda.model.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public class ValueConstantImpl implements ValueConstant {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8031338451909170400L;

	private final String value;

	private final String language;
	
	private final Predicate.COL_TYPE type;

	private final int identifier;

	/**
	 * The default constructor.
	 * 
	 * @param value
	 *            the constant value.
	 * @param type
	 *            the constant type.
	 */
	protected ValueConstantImpl(String value, Predicate.COL_TYPE type) {
		this(value, "", type);
	}
	
	protected ValueConstantImpl(String value, String language, Predicate.COL_TYPE type) {
		if (language == null)
			language = "";
		
		this.value = value;
		this.language = language;
		this.type = type;
		StringBuffer bf =new StringBuffer();
		bf.append(value);
		bf.append(" ");
		bf.append(language);
		bf.append(" ");
		bf.append(type);
		this.identifier = bf.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || !(obj instanceof ValueConstantImpl))
			return false;

		ValueConstantImpl value2 = (ValueConstantImpl) obj;
		return this.identifier == value2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public ValueConstant clone() {
		// ValueConstantImpl clone = new ValueConstantImpl(value, this.type);
		// clone.identifier = identifier;
		// return clone;
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
		if (getType() == COL_TYPE.LITERAL) {
			if (language == null || language.equals(""))
				return value;
			else
				return value + "@" + language;
		} else {
			return value;
		}
		
	}
	
	@Override
	public Set<Variable> getReferencedVariables() {
		return new LinkedHashSet<Variable>();
	}
	
	@Override
	public Map<Variable, Integer> getVariableCount() {
		return new HashMap<Variable,Integer>();
	}

}
