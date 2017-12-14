package it.unibz.inf.ontop.spec.ontology.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyFactory;
import it.unibz.inf.ontop.spec.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.MappingVocabularyExtractor;

import java.util.stream.Stream;


public class MappingVocabularyExtractorImpl implements MappingVocabularyExtractor {

    private static OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();
    private final Mapping2DatalogConverter mapping2DatalogConverter;

    @Inject
    private MappingVocabularyExtractorImpl(Mapping2DatalogConverter mapping2DatalogConverter){
        this.mapping2DatalogConverter = mapping2DatalogConverter;
    }


    @Override
    public Ontology extractVocabulary(Stream<? extends Function> targetAtoms) {
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
        return ONTOLOGY_FACTORY.createOntology(ontologyVocabulary);
    }

    @Override
    public Ontology extractVocabulary(Mapping mapping) {
        return extractVocabulary(mapping2DatalogConverter.convert(mapping)
                .map(CQIE::getHead)
        );
    }

}
