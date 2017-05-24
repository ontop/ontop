package it.unibz.inf.ontop.ontology.utils;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;

import java.util.List;

public class MappingVocabularyExtractor {

    private static OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();

    public static ImmutableOntologyVocabulary extractVocabulary(ImmutableList<OBDAMappingAxiom> mappingAxioms) {
        OntologyVocabulary ontologyVocabulary = ONTOLOGY_FACTORY.createVocabulary();

        for (OBDAMappingAxiom mappingAxiom : mappingAxioms) {
            List<Function> rule = mappingAxiom.getTargetQuery();
            for (Function f : rule) {
                if (f.getArity() == 1)
                    ontologyVocabulary.createClass(f.getFunctionSymbol().getName());
                else {
                    Predicate.COL_TYPE secondArgType = f.getFunctionSymbol().getType(1);
//                        if (secondArgType == null) {
//                            // TODO: this should only happen for data properties (no ambiguity for object property).
//                            throw new IllegalArgumentException("In the target of a mapping assertion, the atom "
//                                    + f + " does not have a type for its second argument. This type is required.");
//                        }
                        if ((secondArgType != null) && secondArgType.equals(Predicate.COL_TYPE.OBJECT))
                            ontologyVocabulary.createObjectProperty(f.getFunctionSymbol().getName());
                        else
                            ontologyVocabulary.createDataProperty(f.getFunctionSymbol().getName());
                }
            }
        }
        return ontologyVocabulary;
    }

    public static Ontology extractOntology(ImmutableList<OBDAMappingAxiom> mappingAxioms) {
        return ONTOLOGY_FACTORY.createOntology(extractVocabulary(mappingAxioms));
    }
}
