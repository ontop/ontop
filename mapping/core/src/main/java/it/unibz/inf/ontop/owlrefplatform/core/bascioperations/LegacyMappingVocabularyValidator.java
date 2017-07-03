package it.unibz.inf.ontop.owlrefplatform.core.bascioperations;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;

public class LegacyMappingVocabularyValidator extends VocabularyValidator {

    public LegacyMappingVocabularyValidator(TBoxReasoner reasoner, ImmutableOntologyVocabulary voc) {
        super(reasoner, voc);
    }

    /***
     * Given a collection of mappings and an equivalence map for classes and
     * properties, it returns a new collection in which all references to
     * class/properties with equivalents has been removed and replaced by the
     * equivalents.
     *
     * For example, given the map hasFather -> inverse(hasChild)
     *
     * If there is a mapping:
     *
     * q(x,y):- hasFather(x,y) <- t(x,y)
     *
     * This will be replaced by the mapping
     *
     * q(x,y):- hasChild(y,x) <- t(x,y)
     *
     * The same is done for classes.
     *
     */
    public ImmutableList<CQIE> replaceEquivalences(Stream<CQIE> mappingAssertions) {
        return mappingAssertions
                .map(this::transformMappingAssertion)
                .collect(ImmutableCollectors.toList());
    }

    public ImmutableList<CQIE> replaceEquivalences(Collection<CQIE> mappingAssertions) {
        return replaceEquivalences(mappingAssertions.stream());
    }

    private CQIE transformMappingAssertion(CQIE mappingAssertion) {
        List<Function> newHeads = replaceEquivalences(ImmutableList.of(mappingAssertion.getHead()));

        switch (newHeads.size()) {
            case 1:
                return DATALOG_FACTORY.getCQIE(newHeads.get(0), mappingAssertion.getBody());
            default:
                throw new EquivalenceReplacingException("Only one head must be returned after replacing equivalences");
        }
    }

    private class EquivalenceReplacingException extends OntopInternalBugException {
        private EquivalenceReplacingException(String message) {
            super(message);
        }
    }
}
