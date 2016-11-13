package it.unibz.inf.ontop.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ExpressionOperation;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.QualifiedAttributeID;
import it.unibz.inf.ontop.sql.QuotedID;
import it.unibz.inf.ontop.sql.RelationID;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 */
public class RelationalExpression {
    private ImmutableList<Function> atoms;
    private ImmutableMap<QualifiedAttributeID, Variable> attributes;
    private ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences;
    private static final OBDADataFactory FACTORY = OBDADataFactoryImpl.getInstance();

    /**
     * constructs a relation expression
     *
     * @param atoms                is an {@link ImmutableList}<{@link Function}>
     * @param attributes           is an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>
     * @param attributeOccurrences is an {@link ImmutableMap}<{@link QuotedID}, {@link ImmutableSet}<{@link RelationID}>>
     */

    public RelationalExpression(ImmutableList<Function> atoms,
                                ImmutableMap<QualifiedAttributeID, Variable> attributes,
                                ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences) {
        this.atoms = atoms;
        this.attributes = attributes;
        this.attributeOccurrences = attributeOccurrences;
    }

    /**
     * Checks if the attributeOccurrences contains the {@link QuotedID} attribute
     *
     * @param attribute is a  {@link QuotedID}
     * @return true if attributeOccurrences contains the {@link QuotedID} attribute otherwise false
     */

    private boolean isAbsent(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = attributeOccurrences.get(attribute);
        return (occurrences == null) || occurrences.isEmpty();
    }

    public ImmutableList<Function> getAtoms() {
        return atoms;
    }

    public ImmutableMap<QualifiedAttributeID, Variable> getAttributes() {
        return attributes;
    }

    /**
     * CROSS JOIN of two relations (also denoted by , in SQL)
     *
     * @param e1 is a {@link RelationalExpression)
     * @param e2 is a {@link RelationalExpression)
     * @return a {@link RelationalExpression}
     */

    public static RelationalExpression crossJoin(RelationalExpression e1, RelationalExpression e2) {

        checkTableAliasConsistency(e1.attributes, e2.attributes);

        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms).addAll(e2.atoms).build();

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(e1.attributes.keySet().stream()
                        .filter(id -> (id.getRelation() != null) || e2.isAbsent(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e1.attributes.get(id))))
                .putAll(e2.attributes.keySet().stream()
                        .filter(id -> (id.getRelation() != null) || e1.isAbsent(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e2.attributes.get(id))))
                .build();

        ImmutableSet<QuotedID> keys = ImmutableSet.<QuotedID>builder()
                .addAll(e1.attributeOccurrences.keySet())
                .addAll(e2.attributeOccurrences.keySet())
                .build();

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences = ImmutableMap.copyOf(
                keys.stream().collect(Collectors.toMap(id -> id,
                        id -> relationSetUnion(
                                e1.attributeOccurrences.get(id),
                                e2.attributeOccurrences.get(id)))));

        return new RelationalExpression(atoms, attributes, attributeOccurrences);
    }


    /**
     * NATURAL JOIN of two relations
     *
     * @param e1 is a {@link RelationalExpression)
     * @param e2 is a {@link RelationalExpression)
     * @return a {@link RelationalExpression}
     */
    static RelationalExpression naturalJoin(RelationalExpression e1, RelationalExpression e2) {

        checkTableAliasConsistency(e1.attributes, e2.attributes);

        ImmutableSet<QualifiedAttributeID> sharedAttributes = ImmutableSet.<QualifiedAttributeID>builder()
                .addAll(e1.attributes.keySet().stream()
                        .filter(id -> (id.getRelation() == null) && !(e2.isAbsent(id.getAttribute())))
                        .collect(Collectors.toSet()))
                .build();
        ImmutableSet<QuotedID> sharedAttributesValue = ImmutableSet.<QuotedID>builder().addAll(
                sharedAttributes.stream().map(QualifiedAttributeID::getAttribute).collect(Collectors.toSet())).build();

        for (QuotedID quotedID : sharedAttributesValue) {
            final ImmutableSet<RelationID> relationIDs1 = e1.attributeOccurrences.get(quotedID);
            final ImmutableSet<RelationID> relationIDs2 = e2.attributeOccurrences.get(quotedID);
            if ((relationIDs1 != null && relationIDs1.size() > 1) || (relationIDs2 != null && relationIDs1.size() > 1))
                throw new UnsupportedOperationException("common ambiguous attribute in select");
        }

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(e1.attributes.keySet().stream()
                        .filter(id -> !sharedAttributesValue.contains(id.getAttribute()) || e2.isAbsent(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e1.attributes.get(id))))
                .putAll(e2.attributes.keySet().stream()
                        .filter(id -> !sharedAttributesValue.contains(id.getAttribute()) || e1.isAbsent(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e2.attributes.get(id))))
                .putAll(e1.attributes.keySet().stream()
                        .filter(sharedAttributes::contains)
                        .collect(Collectors.toMap(id -> id, id -> e1.attributes.get(id))))
                .build();

        ImmutableList.Builder<Function> atomsBuilder = new ImmutableList.Builder<>();
        atomsBuilder.addAll(e1.atoms).addAll(e2.atoms);

        sharedAttributes.forEach(id ->
                atomsBuilder.add(FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(e1.attributes.get(id), e2.attributes.get(id)))));

        ImmutableSet<QuotedID> keys = ImmutableSet.<QuotedID>builder()
                .addAll(e1.attributeOccurrences.keySet())
                .addAll(e2.attributeOccurrences.keySet())
                .build();

        ImmutableMap.Builder<QuotedID, ImmutableSet<RelationID>> attributeOccurrencesBuilder = new ImmutableMap.Builder<>();
        for (QuotedID id : keys) {
            if (!sharedAttributesValue.contains(id)) {
                attributeOccurrencesBuilder.put(id, relationSetUnion(
                        e1.attributeOccurrences.get(id),
                        e2.attributeOccurrences.get(id)));
            } else {
                final ImmutableSet<RelationID> relationIDs = e1.attributeOccurrences.get(id);
                if (relationIDs != null)
                    attributeOccurrencesBuilder.put(id, e1.attributeOccurrences.get(id));
            }
        }

        return new RelationalExpression(atomsBuilder.build(), attributes, attributeOccurrencesBuilder.build());

    }


    /**
     * Add an {@link ImmutableList} of atoms {@link Function} to {@link RelationalExpression}
     *
     * @param e1         ia a {@link RelationalExpression)
     * @param atomsToAdd {@link ImmutableList} of {@link Function}
     * @return a {@link RelationalExpression}
     */
    // TODO: i'm not sure this method is helpful (to be removed)
    static RelationalExpression addAtoms(RelationalExpression e1, ImmutableList<Function> atomsToAdd) {

        // and add an atom for the expression
        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms).addAll(atomsToAdd).build();

        return new RelationalExpression(atoms, e1.attributes, e1.attributeOccurrences);
    }

    /**
     * @param e1                      is a {@link RelationalExpression)
     * @param e2                      is a {@link RelationalExpression)
     * @param getAtomFromOnExpression is a function that given a {@link RelationalExpression} returns an atom form an ON {@link Expression}
     * @return a {@link RelationalExpression}
     */
    static RelationalExpression joinOn(RelationalExpression e1, RelationalExpression e2, java.util.function.Function<RelationalExpression, Function> getAtomOnExpression) {

        RelationalExpression current = RelationalExpression.crossJoin(e1, e2);
        final Function atomOn = getAtomOnExpression.apply(current);
        // and add an atom for the ON expression
        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(current.getAtoms()).add(atomOn).build();

        return new RelationalExpression(atoms, e1.attributes, e1.attributeOccurrences);
    }


    /**
     * JOIN USING of two relations
     *
     * @param e1           is a {@link RelationalExpression)
     * @param e2           is a {@link RelationalExpression)
     * @param usingColumns is a {@link List}<{@link Column}>
     * @return a {@link RelationalExpression)
     */
    static RelationalExpression joinUsing(RelationalExpression e1, RelationalExpression e2, List<Column> usingColumns) {
        // TODO: Implement the case
        return e1;
    }

    /**
     * treats null values as empty sets
     *
     * @param s1 a set of relations
     * @param s2 a set of relations
     * @return the union of s1 and s2
     */
    private static ImmutableSet<RelationID> relationSetUnion(ImmutableSet<RelationID> s1,
                                                             ImmutableSet<RelationID> s2) {
        if (s1 == null)
            return s2;

        if (s2 == null)
            return s1;

        return ImmutableSet.<RelationID>builder().addAll(s1).addAll(s2).build();
    }

    /**
     * An exception is thrown if a table alias occurs in both arguments of the join.
     *
     * @param attributes1 is an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>
     * @param attributes2 is an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>
     */
    private static void checkTableAliasConsistency(ImmutableMap<QualifiedAttributeID, Variable> attributes1, ImmutableMap<QualifiedAttributeID, Variable> attributes2) {
        final Set<RelationID> alias1 = attributes1.keySet().stream().filter(key -> key.getRelation() != null).map(QualifiedAttributeID::getRelation).collect(Collectors.toSet());
        final Set<RelationID> alias2 = attributes2.keySet().stream().filter(key -> key.getRelation() != null).map(QualifiedAttributeID::getRelation).collect(Collectors.toSet());
        if (alias1.isEmpty() || alias2.isEmpty())
            return;
        alias1.retainAll(alias2);
        if (alias1.size() > 0)
            throw new UnsupportedOperationException("Table alias occurs in both arguments of the join");
    }


    @Override
    public String toString() {
        return "RelationalExpression : " + atoms + "\n" + attributes + "\n" + attributeOccurrences;
    }


}
