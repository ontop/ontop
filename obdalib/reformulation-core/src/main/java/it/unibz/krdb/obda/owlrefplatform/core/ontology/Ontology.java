package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

public interface Ontology extends Cloneable {

	public void addAssertion(Axiom assertion);

	public void addAssertions(Collection<Axiom> assertion);

	public void addConcept(ClassDescription cd);

	public void addRole(Property rd);

	public void addConcepts(Collection<ClassDescription> cd);

	public void addRoles(Collection<Property> rd);

	public Set<Property> getRoles();

	public Set<ClassDescription> getConcepts();

	public Set<Axiom> getAssertions();

	/***
	 * This will retrun all the assertions whose right side concept description
	 * refers to the predicate 'pred'
	 * 
	 * @param pred
	 * @return
	 */
	public Set<SubDescriptionAxiom> getByIncluding(Predicate pred);

	/***
	 * As before but it will only return assetions where the right side is an
	 * existential role concept description
	 * 
	 * @param pred
	 * @param onlyAtomic
	 * @return
	 */
	public Set<SubDescriptionAxiom> getByIncludingExistOnly(Predicate pred);

	public Set<SubDescriptionAxiom> getByIncludingNoExist(Predicate pred);

	public Set<SubDescriptionAxiom> getByIncluded(Predicate pred);
//
//	public Set<PositiveInclusion> getByIncludedExistOnly(Predicate pred);
//	
//	public Set<PositiveInclusion> getByIncludedNoExist(Predicate pred);

	public URI getUri();

	/***
	 * This will saturate the ontology, i.e. it will make sure that all axioms
	 * implied by this ontology are asserted in the ontology and accessible
	 * through the methods of the ontology.
	 */
	public void saturate();
	
	public Ontology clone();
}
