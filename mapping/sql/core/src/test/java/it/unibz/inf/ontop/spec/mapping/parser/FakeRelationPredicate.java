package it.unibz.inf.ontop.spec.mapping.parser;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationPredicateImpl;
import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

/**
 * For low-level tests only!
 */
public class FakeRelationPredicate extends RelationPredicateImpl {

    public FakeRelationPredicate(RelationDefinition relation) {
        super(relation);
    }

}
