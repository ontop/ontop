package it.unibz.inf.ontop.ontology.utils;

import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;

import java.util.stream.Stream;

public class MappingVocabularyExtractor {

    private static OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();

    public static ImmutableOntologyVocabulary extractVocabulary(Stream<? extends Function> targetAtoms) {
        OntologyVocabulary ontologyVocabulary = ONTOLOGY_FACTORY.createVocabulary();
        targetAtoms
                .forEach(f -> {
                    if (f.getArity() == 1)
                        ontologyVocabulary.createClass(f.getFunctionSymbol().getName());
                    else {
                        Predicate.COL_TYPE secondArgType = f.getFunctionSymbol().getType(1);
                        if ((secondArgType != null) && secondArgType.equals(Predicate.COL_TYPE.OBJECT))
                            ontologyVocabulary.createObjectProperty(f.getFunctionSymbol().getName());
                        else
                            ontologyVocabulary.createDataProperty(f.getFunctionSymbol().getName());
                    }
                });
        return ontologyVocabulary;
    }

    public static Ontology extractOntology(Stream<? extends Function> mappingAxioms) {
        return ONTOLOGY_FACTORY.createOntology(extractVocabulary(mappingAxioms));
    }
}
