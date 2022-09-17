package it.unibz.inf.ontop.iq.node;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;

import java.util.stream.Stream;

/**
 * This node is analogous to the VALUES statement supported by many relational databases.
 * See for example the documentation for PostgreSQL: https://www.postgresql.org/docs/current/queries-values.html
 *
 * @author Lukas Sundqvist
 */
public interface ValuesNode extends LeafIQTree {

    ImmutableList<Variable> getOrderedVariables();

    ImmutableList<ImmutableList<Constant>> getValues();

    Stream<Constant> getValueStream(Variable variable);

    @Override
    ValuesNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    @Override
    ValuesNode applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution);

}
