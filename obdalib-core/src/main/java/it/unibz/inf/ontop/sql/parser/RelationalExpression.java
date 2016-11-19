package it.unibz.inf.ontop.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.QualifiedAttributeID;
import it.unibz.inf.ontop.sql.QuotedID;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQuery;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
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

    private boolean isAmbiguous(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = attributeOccurrences.get(attribute);
        return (occurrences != null) && occurrences.size() > 1;
    }

    private boolean isUnique(QuotedID attribute) {
        ImmutableSet<RelationID> occurrences = attributeOccurrences.get(attribute);
        return (occurrences != null) && occurrences.size() == 1;
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
        return joinOn(e1, e2, att -> ImmutableList.of());
    }


    /**
     * @param e1                  is a {@link RelationalExpression)
     * @param e2                  is a {@link RelationalExpression)
     * @param getAtomOnExpression is a {@link java.util.function.Function} <{@link ImmutableMap}<{@link QualifiedAttributeID}, {@link ImmutableList}<{@link Function}>}>
     * @return a {@link RelationalExpression}
     */
    static RelationalExpression joinOn(RelationalExpression e1, RelationalExpression e2,
                                       java.util.function.Function<ImmutableMap<QualifiedAttributeID, Variable>, ImmutableList<Function>> getAtomOnExpression) {

        // TODO: better exception?
        if (!relationAliasesConsistent(e1.attributes, e2.attributes))
            throw new InvalidSelectQuery("Relation alias occurs in both arguments of the join", null);

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(e1.filterAttributes(id ->
                        (id.getRelation() != null) || e2.isAbsent(id.getAttribute())))

                .putAll(e2.filterAttributes(id ->
                        (id.getRelation() != null) || e1.isAbsent(id.getAttribute())))

                .build();

        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms)
                .addAll(e2.atoms)
                .addAll(getAtomOnExpression.apply(attributes))
                .build();

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                getAttributeOccurrences(e1, e2, id -> attributeOccurrencesUnion(id, e1, e2));

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

        // TODO: better exception?
        if (!relationAliasesConsistent(e1.attributes, e2.attributes))
            throw new InvalidSelectQuery("Relation alias occurs in both arguments of the join", null);

        ImmutableSet<QuotedID> sharedAttributes = e1.attributeOccurrences.keySet().stream()
                .filter(id -> !e1.isAbsent(id) && !e2.isAbsent(id))
                .collect(ImmutableCollectors.toSet());

        // TODO: better exception? more informative error message?
        if (sharedAttributes.stream().anyMatch(id -> e1.isAmbiguous(id) || e2.isAmbiguous(id)))
            throw new UnsupportedOperationException("common ambiguous attribute in select");

        return internalJoinUsing(e1, e2, sharedAttributes);
    }

    /**
     * JOIN USING of two relations
     *
     * @param e1           is a {@link RelationalExpression)
     * @param e2           is a {@link RelationalExpression)
     * @param usingColumns is a {@link ImmutableSet}<{@link QuotedID}>
     * @return a {@link RelationalExpression)
     */
    static RelationalExpression joinUsing(RelationalExpression e1, RelationalExpression e2,
                                          ImmutableSet<QuotedID> usingColumns) {

        // TODO: better exception?
        if (!relationAliasesConsistent(e1.attributes, e2.attributes))
            throw new InvalidSelectQuery("Relation alias occurs in both arguments of the join", null);

        if (usingColumns.stream().anyMatch(id -> !e1.isUnique(id) || !e2.isUnique(id)))
            throw new UnsupportedOperationException("ambiguous column attributes in using statement");

        return RelationalExpression.internalJoinUsing(e1, e2, usingColumns);
    }

    /**
     * JOIN USING of two relations
     *
     * @param e1              is a {@link RelationalExpression)
     * @param e2              is a {@link RelationalExpression)
     * @param usingAttributes is a {@link Set}<{@link QuotedID}>
     * @return a {@link RelationalExpression}
     */
    private static RelationalExpression internalJoinUsing(RelationalExpression e1, RelationalExpression e2,
                                                          ImmutableSet<QuotedID> usingAttributes) {

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(e1.filterAttributes(id ->
                        (id.getRelation() != null && !usingAttributes.contains(id.getAttribute()))
                                || (id.getRelation() == null && e2.isAbsent(id.getAttribute()))
                                || (id.getRelation() == null && usingAttributes.contains(id.getAttribute()))))

                .putAll(e2.filterAttributes(id ->
                        (id.getRelation() != null && !usingAttributes.contains(id.getAttribute()))
                                || (id.getRelation() == null && e1.isAbsent(id.getAttribute()))))

                .build();

        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms)
                .addAll(e2.atoms)
                .addAll(usingAttributes.stream()
                        .map(id -> new QualifiedAttributeID(null, id))
                        // TODO: throw an exception if id is not found in either of the ei
                        //.filter(id -> e1.attributes.get(id) != null && e2.attributes.get(id) != null)
                        .map(id -> FACTORY.getFunctionEQ(e1.attributes.get(id), e2.attributes.get(id)))
                        .iterator())
                .build();

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                getAttributeOccurrences(e1, e2,
                        id -> usingAttributes.contains(id)
                                ? e1.attributeOccurrences.get(id)
                                : attributeOccurrencesUnion(id, e1, e2));

        return new RelationalExpression(atoms, attributes, attributeOccurrences);
    }


    /**
     * Used for relation's where expression
     *
     * @param e1    ia a {@link RelationalExpression}
     * @param where is a {@link java.util.function.Function}<{@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>,
     *              {@link ImmutableList}<{@link Function}> }
     * @return a {@link RelationalExpression}
     */
    static RelationalExpression where(RelationalExpression e1,
                                      java.util.function.Function<ImmutableMap<QualifiedAttributeID, Variable>, ImmutableList<Function>> where) {

        return new RelationalExpression(ImmutableList.<Function>builder()
                .addAll(e1.atoms).addAll(where.apply(e1.attributes)).build(), e1.attributes, e1.attributeOccurrences);
    }

    /**
     * treats null values as empty sets
     *
     * @param id is a {@link QuotedID}
     * @param e1 a relational expression
     * @param e2 a relational expression
     * @return the union of occurrences of id in e1 and e2
     */
    private static ImmutableSet<RelationID> attributeOccurrencesUnion(QuotedID id,
                                                                      RelationalExpression e1, RelationalExpression e2) {

        ImmutableSet<RelationID> s1 = e1.attributeOccurrences.get(id);
        ImmutableSet<RelationID> s2 = e2.attributeOccurrences.get(id);

        if (s1 == null)
            return s2;

        if (s2 == null)
            return s1;

        return ImmutableSet.<RelationID>builder().addAll(s1).addAll(s2).build();
    }


    private ImmutableMap<QualifiedAttributeID, Variable> filterAttributes(java.util.function.Predicate<QualifiedAttributeID> condition) {

        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()))
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private static ImmutableMap<QuotedID, ImmutableSet<RelationID>> getAttributeOccurrences(RelationalExpression e1,
                                                                                            RelationalExpression e2,
                                                                                            java.util.function.Function<QuotedID, ImmutableSet<RelationID>>
                                                                                                    collector) {

        ImmutableSet<QuotedID> keys = ImmutableSet.<QuotedID>builder()
                .addAll(e1.attributeOccurrences.keySet())
                .addAll(e2.attributeOccurrences.keySet())
                .build();

        return keys.stream()
                .collect(ImmutableCollectors.toMap(identity(), collector));
    }


    /**
     * return false if a relation alias occurs in both arguments of the join.
     *
     * @param attributes1 is an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>
     * @param attributes2 is an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>
     */
    private static boolean relationAliasesConsistent(ImmutableMap<QualifiedAttributeID, Variable> attributes1,
                                                     ImmutableMap<QualifiedAttributeID, Variable> attributes2) {
        // the first one is mutable
        ImmutableSet<RelationID> alias1 = attributes1.keySet().stream()
                .filter(key -> key.getRelation() != null)
                .map(QualifiedAttributeID::getRelation).collect(ImmutableCollectors.toSet());

        ImmutableSet<RelationID> alias2 = attributes2.keySet().stream()
                .filter(key -> key.getRelation() != null)
                .map(QualifiedAttributeID::getRelation).collect(ImmutableCollectors.toSet());

        return !alias1.stream().anyMatch(alias2::contains);
    }


    @Override
    public String toString() {
        return "RelationalExpression : " + atoms + "\n" + attributes + "\n" + attributeOccurrences;
    }


}
