package org.obda.owlrefplatform.core.ontology.imp;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.ConceptDescription;
import org.obda.owlrefplatform.core.ontology.DLLiterOntology;
import org.obda.owlrefplatform.core.ontology.RoleDescription;

public class DLLiterOntologyImpl implements DLLiterOntology {

	private List<Assertion> assertions = null;
	private Set<ConceptDescription> concepts = null;
	private Set<RoleDescription> roles = null;
	private URI ontouri = null;
	
	public DLLiterOntologyImpl (URI uri){
		ontouri = uri;
		assertions = new Vector<Assertion>();
		concepts = new HashSet<ConceptDescription>();
		roles = new HashSet<RoleDescription>();
	}
	
	public void addAssertion(Assertion assertion) {
		
		assertions.add(assertion);
	}

	public List<Assertion> getAssertions() {
		return assertions;
	}

	public void addAssertions(List<Assertion> ass) {
		assertions.addAll(ass);	
	}

	public void addConcept(ConceptDescription cd) {
		concepts.add(cd);
	}

	public void addConcepts(List<ConceptDescription> cd) {
		concepts.addAll(cd);
	}

	public void addRole(RoleDescription rd) {
		roles.add(rd);
	}

	public void addRoles(List<RoleDescription> rd) {
		roles.addAll(rd);
	}

	public Set<ConceptDescription> getConcepts() {
		return concepts;
	}

	public Set<RoleDescription> getRoles() {
		return roles;
	}

	@Override
	public URI getUri() {
		return ontouri;
	}

}
