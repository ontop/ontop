package it.unibz.inf.ontop.reformulation;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.model.atom.AtomPredicate;

public interface RuleUnfolder {

    IntermediateQuery unfold(IntermediateQuery query, ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap)
            throws EmptyQueryException;
}
