package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */
public class RAExpression implements RAEntity<RAExpression> {

    private final ImmutableList<ExtensionalDataNode> atoms;
    private final ImmutableList<ImmutableExpression> filters;
    private final RAExpressionAttributes attributes;
    private final TermFactory termFactory;

    /**
     * constructs a relation expression
     * @param atoms           an {@link ImmutableList}<{@link ExtensionalDataNode}>
     * @param filters         an {@link ImmutableList}<{@link ImmutableExpression}>
     * @param attributes      an {@link RAExpressionAttributes}
     */
    public RAExpression(ImmutableList<ExtensionalDataNode> atoms,
                        ImmutableList<ImmutableExpression> filters,
                        RAExpressionAttributes attributes,
                        TermFactory termFactory) {
        this.atoms = atoms;
        this.filters = filters;
        this.attributes = attributes;
        this.termFactory = termFactory;
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

        /**
         * CROSS JOIN (also denoted by , in SQL)
         *
         * @param re2 a {@link RAExpression}
         * @return a {@link RAExpression}
         * @throws IllegalJoinException if the same alias occurs in both arguments
         */
    @Override
    public RAExpression crossJoin(RAExpression re2) throws IllegalJoinException {
        return new RAExpression(union(this.atoms, re2.atoms),
                union(this.filters, re2.filters),
                this.attributes.crossJoin(re2.attributes), termFactory);
    }


    /**
     * JOIN ON
     *
     * @param re2 a {@link RAExpression}
     * @param getAtomOnExpression
     * @return a {@link RAExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */
    @Override
    public RAExpression joinOn(RAExpression re2,
                               Function<RAExpressionAttributes, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException {

        RAExpressionAttributes attributes =
                this.attributes.crossJoin(re2.attributes);

        return new RAExpression(union(this.atoms, re2.atoms),
                union(this.filters, re2.filters,
                        getAtomOnExpression.apply(attributes)), attributes, termFactory);
    }

    @Override
    public RAExpression naturalJoin(RAExpression re2) throws IllegalJoinException {
        return joinUsing(re2, attributes.getSharedAttributeNames(re2.attributes));
    }

    /**
     * JOIN USING
     *
     * @param re2 a {@link RAExpression}
     * @param using a {@link ImmutableSet}<{@link QuotedID}>
     * @return a {@link RAExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     *          or one of the `using' attributes is ambiguous or absent
     */

    @Override
    public RAExpression joinUsing(RAExpression re2, ImmutableSet<QuotedID> using) throws IllegalJoinException {

        RAExpressionAttributes attributes =
                this.attributes.joinUsing(re2.attributes, using);

        return new RAExpression(union(this.atoms, re2.atoms),
                union(this.filters, re2.filters,
                        getJoinOnFilter(this.attributes, re2.attributes, using, termFactory)),
                attributes, termFactory);
    }

    /**
     * internal implementation of JOIN USING and NATURAL JOIN
     *
     * @param re1 a {@link RAExpressionAttributes}
     * @param re2 a {@link RAExpressionAttributes}
     * @param using a {@link ImmutableSet}<{@link QuotedID}>
     * @return a {@Link ImmutableList}<{@link ImmutableExpression}>
     */
    private static ImmutableList<ImmutableExpression> getJoinOnFilter(RAExpressionAttributes re1,
                                                                      RAExpressionAttributes re2,
                                                                      ImmutableSet<QuotedID> using,
                                                                      TermFactory termFactory) {

        return using.stream()
                .map(id -> termFactory.getNotYetTypedEquality(re1.get(id), re2.get(id)))
                .collect(ImmutableCollectors.toList());
    }


    /**
     * (relational expression) AS A
     *
     * @param aliasId a {@link RelationID}
     * @return a {@link RAExpression}
     */

    @Override
    public RAExpression withAlias(RelationID aliasId) {
        return new RAExpression(atoms, filters, attributes.withAlias(aliasId), termFactory);
    }



    private static <T> ImmutableList<T> union(ImmutableList<T> atoms1, ImmutableList<T> atoms2) {
        return Stream.concat(atoms1.stream(), atoms2.stream()).collect(ImmutableCollectors.toList());
    }

    private static <T> ImmutableList<T> union(ImmutableList<T> atoms1, ImmutableList<T> atoms2, ImmutableList<T> atoms3) {
        return Stream.concat(Stream.concat(atoms1.stream(), atoms2.stream()), atoms3.stream()).collect(ImmutableCollectors.toList());
    }


    @Override
    public String toString() {
        return "RAExpression : " + atoms + " FILTER " + filters + " with " + attributes;
    }

}
