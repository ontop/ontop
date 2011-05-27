package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import java.net.URI;
import java.util.List;
import java.util.Set;

public interface DLLiterOntology extends Ontology {

	public void addAssertion(Assertion assertion);
	public void addAssertions(List<Assertion> assertion);
	public void addConcept(ConceptDescription cd);
	public void addRole(RoleDescription rd);
	public void addConcepts(List<ConceptDescription> cd);
	public void addRoles(List<RoleDescription> rd);
	public Set<ConceptDescription> getConcepts();
	public Set<RoleDescription> getRoles();
	public URI getUri();
}
