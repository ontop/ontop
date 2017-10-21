package it.unibz.inf.ontop.spec.ontology.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyFactory;
import it.unibz.inf.ontop.spec.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.MappingVocabularyExtractor;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;


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
                        TermType secondArgType = f.getFunctionSymbol().getExpectedBaseType(1);
                        if (secondArgType.isA(TYPE_FACTORY.getAbstractObjectRDFType()))
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
