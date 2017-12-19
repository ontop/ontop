package it.unibz.inf.ontop.spec.ontology.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyBuilder;
import it.unibz.inf.ontop.spec.ontology.MappingVocabularyExtractor;
import it.unibz.inf.ontop.spec.ontology.OntologyVocabulary;

import java.util.stream.Stream;


public class MappingVocabularyExtractorImpl implements MappingVocabularyExtractor {

    private final Mapping2DatalogConverter mapping2DatalogConverter;

    @Inject
    private MappingVocabularyExtractorImpl(Mapping2DatalogConverter mapping2DatalogConverter){
        this.mapping2DatalogConverter = mapping2DatalogConverter;
    }


    @Override
    public OntologyVocabulary extractVocabulary(Stream<? extends Function> targetAtoms) {
        return extractVocabularyInternal(targetAtoms).buildVocabulary();
    }

    private OntologyBuilder extractVocabularyInternal(Stream<? extends Function> targetAtoms) {
        OntologyBuilder ontologyBuilder = OntologyBuilderImpl.builder();
        targetAtoms
                .forEach(f -> {
                    String name = f.getFunctionSymbol().getName();
                    if (f.getArity() == 1)
                        ontologyBuilder.declareClass(name);
                    else {
                        Predicate.COL_TYPE secondArgType = f.getFunctionSymbol().getType(1);
                        if ((secondArgType != null) && secondArgType.equals(Predicate.COL_TYPE.OBJECT))
                            ontologyBuilder.declareObjectProperty(name);
                        else
                            ontologyBuilder.declareDataProperty(name);
                    }
                });
        return ontologyBuilder;
    }

    @Override
    public Ontology extractOntology(Mapping mapping) {
        return (extractVocabularyInternal(mapping2DatalogConverter.convert(mapping)
                .map(CQIE::getHead)))
                .build();
    }

}
