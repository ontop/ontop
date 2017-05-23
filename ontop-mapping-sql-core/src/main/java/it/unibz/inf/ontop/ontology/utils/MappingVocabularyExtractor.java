package it.unibz.inf.ontop.ontology.utils;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;

import java.util.List;

public class MappingVocabularyExtractor {

    private static OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();

    public static ImmutableOntologyVocabulary extractVocabulary(OBDAModel mapping) {
        OntologyVocabulary ontologyVocabulary = ONTOLOGY_FACTORY.createVocabulary();

        for (OBDAMappingAxiom mappingAssertion : mapping.getMappings()) {
            List<Function> rule = mappingAssertion.getTargetQuery();
            for (Function f : rule) {
                if (f.getArity() == 1)
                    ontologyVocabulary.createClass(f.getFunctionSymbol().getName());
                else {
                    Predicate.COL_TYPE secondArgType = f.getFunctionSymbol().getType(1);
                        if (secondArgType == null) {
                            // TODO: this should only happen for data properties (no ambiguity for object property).
                            throw new IllegalArgumentException("In the target of a mapping assertion, the atom "
                                    + f + " does not have a type for its second argument. This type is required.");
                        }
                        else if (secondArgType.equals(Predicate.COL_TYPE.OBJECT))
                            ontologyVocabulary.createObjectProperty(f.getFunctionSymbol().getName());
                        else
                            ontologyVocabulary.createDataProperty(f.getFunctionSymbol().getName());
                }
            }
        }
        return ontologyVocabulary;
    }

    public static Ontology extractOntology(OBDAModel mapping) {
        return ONTOLOGY_FACTORY.createOntology(extractVocabulary(mapping));
    }
}
