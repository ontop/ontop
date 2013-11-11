/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Variable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AnonymousVariable extends AbstractLiteral implements Variable {

	private static final long serialVersionUID = 6099056787768897902L;

	private static final String DEFAULT_NAME = "_";
	
	private static final int identifier = -4000;

	protected AnonymousVariable() {
		// NO-OP
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof AnonymousVariable)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public String getName() {
		return DEFAULT_NAME;
	}

	@Override
	public Variable clone() {
		return this;
	}

	@Override
	public String toString() {
		return DEFAULT_NAME;
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		return new LinkedHashSet<Variable>();
	}
	
	@Override
	public Map<Variable, Integer> getVariableCount() {
		// TODO This is wrong but it shouldn't affect query containment
		Map<Variable,Integer> count =  new HashMap<Variable,Integer>();
		count.put(this, 1);
		return count;
	}

}
