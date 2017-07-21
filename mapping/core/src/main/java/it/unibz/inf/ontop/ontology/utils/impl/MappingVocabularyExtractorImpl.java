package it.unibz.inf.ontop.ontology.utils.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.ontology.utils.MappingVocabularyExtractor;

import java.util.stream.Stream;


public class MappingVocabularyExtractorImpl implements MappingVocabularyExtractor {

    private static OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();
    private final Mapping2DatalogConverter mapping2DatalogConverter;

    @Inject
    private MappingVocabularyExtractorImpl(Mapping2DatalogConverter mapping2DatalogConverter){
        this.mapping2DatalogConverter = mapping2DatalogConverter;
    }


    @Override
    public ImmutableOntologyVocabulary extractVocabulary(Stream<? extends Function> targetAtoms) {
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

    @Override
    public Ontology extractOntology(Mapping mapping) {

        return extractOntology(mapping2DatalogConverter.convert(mapping)
                .map(CQIE::getHead)
        );
    }

    private Ontology extractOntology(Stream<? extends Function> mappingAxioms) {
        return ONTOLOGY_FACTORY.createOntology(extractVocabulary(mappingAxioms));
    }
}
