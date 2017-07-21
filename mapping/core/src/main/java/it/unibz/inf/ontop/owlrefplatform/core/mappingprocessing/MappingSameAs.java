package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;

public class MappingSameAs {

    /**
     * add the inverse of the same as present in the mapping
     */
    public static ImmutableList<CQIE> addSameAsInverse(ImmutableList<CQIE> mappingRules) {
        Stream<CQIE> newRuleStream = mappingRules.stream()
                // the targets are already split. We have only one target atom
                .filter(r -> r.getHead().getFunctionSymbol().getName().equals(OBDAVocabulary.SAME_AS))
                .map(r -> {
                    Function head = r.getHead();
                    Function inversedHead = DATA_FACTORY.getFunction(head.getFunctionSymbol(),
                            head.getTerm(1),
                            head.getTerm(0));
                    return DATALOG_FACTORY.getCQIE(inversedHead, new ArrayList<>(r.getBody()));
                });

        return Stream.concat(mappingRules.stream(), newRuleStream)
                .collect(ImmutableCollectors.toList());
    }
}
