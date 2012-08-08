package it.unibz.krdb.obda.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CQIE extends OBDAQuery {

	public Atom getHead();

	public List<Atom> getBody();

	public void updateHead(Atom head);

	public void updateBody(List<Atom> body);

	public CQIE clone();
	
	public Set<Variable> getReferencedVariables();
	
	public Map<Variable,Integer> getVariableCount();
	
}
