package it.unibz.inf.ontop.injection;

public interface OntopKGQuerySettings extends OntopOBDASettings, OntopOptimizationSettings {

    /**
     * Returns true if the pattern "?s ?p <describedIRI>" should also be
     * considered while answering a DESCRIBE query.
     *
     */
    boolean isFixedObjectIncludedInDescribe();

    boolean isCustomSPARQLFunctionRegistrationEnabled();

    String INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE = "ontop.includeFixedObjectPositionInDescribe";
    String REGISTER_CUSTON_SPARQL_AGGREGATE_FUNCTIONS = "ontop.registerCustomSPARQLAggregateFunctions";
}
