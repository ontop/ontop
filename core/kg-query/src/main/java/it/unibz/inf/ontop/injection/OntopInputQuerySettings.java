package it.unibz.inf.ontop.injection;

public interface OntopInputQuerySettings extends OntopOBDASettings, OntopOptimizationSettings {

    /**
     * Returns true if the pattern "?s ?p <describedIRI>" should also be
     * considered while answering a DESCRIBE query.
     *
     */
    boolean isFixedObjectIncludedInDescribe();

    String INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE = "ontop.includeFixedObjectPositionInDescribe";
}
