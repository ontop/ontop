package it.unibz.inf.ontop.spec.ontology.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.spec.ontology.*;

import java.util.stream.Stream;

public class MappingVocabularyExtractorImpl implements MappingVocabularyExtractor {

    @Override
    public OntologyVocabulary extractVocabulary(Stream<? extends Function> targetAtoms) {
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
        return ontologyBuilder.buildVocabulary();
    }
}
