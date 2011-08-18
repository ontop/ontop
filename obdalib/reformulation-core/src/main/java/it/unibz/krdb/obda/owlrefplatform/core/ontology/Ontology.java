package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

public interface Ontology extends Cloneable {

	public void addAssertion(Assertion assertion);

	public void addAssertions(Collection<Assertion> assertion);

	public void addConcept(ConceptDescription cd);

	public void addRole(RoleDescription rd);

	public void addConcepts(Collection<ConceptDescription> cd);

	public void addRoles(Collection<RoleDescription> rd);

	public Set<RoleDescription> getRoles();

	public Set<ConceptDescription> getConcepts();

	public Set<Assertion> getAssertions();

	/***
	 * This will retrun all the assertions whose right side concept description
	 * refers to the predicate 'pred'
	 * 
	 * @param pred
	 * @return
	 */
	public Set<PositiveInclusion> getByIncluding(Predicate pred);

	/***
	 * As before but it will only return assetions where the right side is an
	 * existential role concept description
	 * 
	 * @param pred
	 * @param onlyAtomic
	 * @return
	 */
	public Set<PositiveInclusion> getByIncludingExistOnly(Predicate pred);

	public Set<PositiveInclusion> getByIncludingNoExist(Predicate pred);

	public Set<PositiveInclusion> getByIncluded(Predicate pred);
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
