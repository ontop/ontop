package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.dbschema.QuotedID;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */
public class RAExpression {

    private final ImmutableList<ExtensionalDataNode> atoms;
    private final ImmutableList<ImmutableExpression> filters;
    private final RAExpressionAttributes attributes;

    /**
     * constructs a relation expression
     * @param atoms           an {@link ImmutableList}<{@link ExtensionalDataNode}>
     * @param filters         an {@link ImmutableList}<{@link ImmutableExpression}>
     * @param attributes      an {@link RAExpressionAttributes}
     */
    public RAExpression(ImmutableList<ExtensionalDataNode> atoms,
                        ImmutableList<ImmutableExpression> filters,
                        RAExpressionAttributes attributes) {
        this.atoms = atoms;
        this.filters = filters;
        this.attributes = attributes;
    }


    public ImmutableList<ExtensionalDataNode> getDataAtoms() {
        return atoms;
    }

    public ImmutableList<ImmutableExpression> getFilterAtoms() {
        return filters;
    }

    public RAExpressionAttributes getAttributes() { return attributes; }

    public ImmutableMap<QuotedID, ImmutableTerm> getUnqualifiedAttributes() {
        return attributes.getUnqualifiedAttributes();
    }


    @Override
    public String toString() {
        return "RAExpression : " + atoms + " FILTER " + filters + " with " + attributes;
    }
}
