package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.Set;

import it.unibz.krdb.obda.ontology.Description;

public class EquivalenceClass {
	private Set<Description> members;
	private Description representative;

	public EquivalenceClass(Set<Description> members) {
		this.members = members;
		this.representative = null;
	}
	
	public Set<Description> getMembers() {
		return members;
	}
	
	public Description getRepresentative() {
		return representative;
	}
	
	public void setRepresentative(Description representative) {
		assert(members.contains(representative));
		this.representative = representative;
	}
}
