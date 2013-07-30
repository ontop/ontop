/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CQIE extends OBDAQuery {

	public Function getHead();

	public List<Function> getBody();

	public void updateHead(Function head);

	public void updateBody(List<Function> body);

	public CQIE clone();
	
	public Set<Variable> getReferencedVariables();
	
	public Map<Variable,Integer> getVariableCount();
}
