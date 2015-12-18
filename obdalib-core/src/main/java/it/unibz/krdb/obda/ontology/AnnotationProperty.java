package it.unibz.krdb.obda.ontology;


import it.unibz.krdb.obda.model.Predicate;

/**
 * Represents AnnotationProperty from the OWL 2 Specification
 *
 * AnnotationProperty := IRI
 *
 * @author Sarah
 *
 */

public interface AnnotationProperty extends Description {

    /**
     * the name of the annotation property
     *
     * @return the predicate symbol that corresponds to the annotation property name
     */

    public Predicate getPredicate();


    public String getName();


    /**
     * the domain iri for the annotation property
     * <p>
     *
     *
     * @return iri  for the domain
     */

//    public AnnotationPropertyDomain getDomain();

    /**
     * the range iri for the annotation property
     * <p>
     * (
     * <p>
     *
     * @return iri for the range
     */

//    public AnnotationPropertyRange getRange();


}
