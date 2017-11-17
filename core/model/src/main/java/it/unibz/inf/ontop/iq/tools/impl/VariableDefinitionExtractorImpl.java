package it.unibz.inf.ontop.iq.tools.impl;


import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.VariableDefinitionExtractor;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

@Singleton
public class VariableDefinitionExtractorImpl implements VariableDefinitionExtractor {

    private final SubstitutionFactory substitutionFactory;

    @Inject
    private VariableDefinitionExtractorImpl(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
    }

    /**
     * Given a query q, a variable x and a starting node n projecting x,
     * retrieves all substitutions (from ConstructionNodes) generating a value for x in the subtree of q
     * rooted in n.
     * Note that unions may cause multiple such substitutions to be returned.
     * <p>
     * The intended usage of this method it to determine the type of a variable from the returned substitution.
     * Therefore this method is robust to simple variable renaming,
     * but only until a more complex substitution is found.
     * E.g. if [x/x2], [x2/x3] and [x3/URI("http://myURI{}", x4] are three substitutions found in that order in a same
     * branch,
     * then x/URI("http://myURI{}", x4) will be the only output substitution for that branch.
     * <p>
     * This method does not support composition otherwise.
     * E.g. if [x/+(x1,x2)] and [x2/x3] are found in that order in a branch,
     * then [x/+(x1,x2)] is returned.
     */
    @Override
    public ImmutableSet<ImmutableTerm> extract(Variable variable, QueryNode node, IntermediateQuery query) {
        if (query.contains(node)) {
            if (query.getVariables(node).contains(variable)) {
                return getDefinitions(variable, variable, node, query)
                        .collect(ImmutableCollectors.toSet());
            }
            throw new IllegalArgumentException("The node should project the variable");
        }
        throw new IllegalArgumentException("The node should be in the query");
    }

    @Override
    public ImmutableSet<ImmutableTerm> extract(Variable variable, IntermediateQuery query) {
        return extract(variable, query.getRootNode(), query);
    }

    /**
     * Recursive
     */
    private Stream<ImmutableTerm> getDefinitions(Variable initialVariable, Variable currentVariable,
                                                        QueryNode currentNode, IntermediateQuery query) {
        if (currentNode instanceof ConstructionNode) {
            ImmutableTerm def = ((ConstructionNode) currentNode).getSubstitution().get(currentVariable);
            if (def == null) {
                def = currentVariable;
            }
            /* case of a complex substitution: substitute and return */
            if (!(def instanceof Variable)) {
                return Stream.of(
                        substitute(
                                initialVariable,
                                currentVariable,
                                def
                        ));
            }
            /* case of a simple variable renaming: update the substitution */
            currentVariable = (Variable) def;
        }
        /* Leaf node: return a simple variable substitution */
        if (!query.getFirstChild(currentNode).isPresent()) {
            return Stream.of(currentVariable);
        }
        /* recursive call */
        Variable fakeClone = (Variable) currentVariable.clone();
        return query.getChildren(currentNode).stream()
                .flatMap(n -> getDefinitions(
                        initialVariable,
                        fakeClone,
                        n,
                        query
                ));
    }

    private ImmutableTerm substitute(Variable initialVariable, Variable currentVariable, ImmutableTerm def) {
        return initialVariable.equals(currentVariable) ?
                def :
                substitutionFactory.getSubstitution(currentVariable, initialVariable).apply(def);
    }
}
