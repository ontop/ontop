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
import it.unibz.inf.ontop.sql.parser.exceptions.IllegalJoinException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Set;

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
     * @param atoms                an {@link ImmutableList}<{@link Function}>
     * @param attributes           an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>
     * @param attributeOccurrences an {@link ImmutableMap}<{@link QuotedID}, {@link ImmutableSet}<{@link RelationID}>>
     */
    public RelationalExpression(ImmutableList<Function> atoms,
                                ImmutableMap<QualifiedAttributeID, Variable> attributes,
                                ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences) {
        this.atoms = atoms;
        this.attributes = attributes;
        this.attributeOccurrences = attributeOccurrences;
    }

    /**
     * checks if the attributeOccurrences contains the attribute
     *
     * @param attribute a  {@link QuotedID}
     * @return true if attributeOccurrences contains the attribute otherwise false
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
     * CROSS JOIN (also denoted by , in SQL)
     *
     * @param re1 a {@link RelationalExpression}
     * @param re2 a {@link RelationalExpression}
     * @return a {@link RelationalExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */
    public static RelationalExpression crossJoin(RelationalExpression re1, RelationalExpression re2) throws IllegalJoinException {
        return joinOn(re1, re2, BooleanExpressionParser.empty());
    }


    /**
     * JOIN ON
     *
     * @param re1 a {@link RelationalExpression}
     * @param re2 a {@link RelationalExpression}
     * @param getAtomOnExpression a {@link BooleanExpressionParser}
     * @return a {@link RelationalExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */
    public static RelationalExpression joinOn(RelationalExpression re1, RelationalExpression re2,
                                       BooleanExpressionParser getAtomOnExpression) throws IllegalJoinException {

        checkRelationAliasesConsistency(re1, re2);

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(re1.filterAttributes(id ->
                        (id.getRelation() != null) || re2.isAbsent(id.getAttribute())))

                .putAll(re2.filterAttributes(id ->
                        (id.getRelation() != null) || re1.isAbsent(id.getAttribute())))

                .build();

        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(re1.atoms)
                .addAll(re2.atoms)
                .addAll(getAtomOnExpression.apply(attributes))
                .build();

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                getAttributeOccurrences(re1, re2, id -> attributeOccurrencesUnion(id, re1, re2));

        return new RelationalExpression(atoms, attributes, attributeOccurrences);
    }

    /**
     * NATURAL JOIN
     *
     * @param re1 a {@link RelationalExpression}
     * @param re2 a {@link RelationalExpression}
     * @return a {@link RelationalExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     *          or one of the shared attributes is ambiguous
     */

    public static RelationalExpression naturalJoin(RelationalExpression re1, RelationalExpression re2) throws IllegalJoinException {

        checkRelationAliasesConsistency(re1, re2);

        ImmutableSet<QuotedID> shared = re1.attributeOccurrences.keySet().stream()
                .filter(id -> !re1.isAbsent(id) && !re2.isAbsent(id))
                .collect(ImmutableCollectors.toSet());

        if (shared.stream().anyMatch(id -> re1.isAmbiguous(id) || re2.isAmbiguous(id)))
            throw new IllegalJoinException(re1, re2,
                    "Ambiguous common attribute " +
                            shared.stream()
                                    .filter(id -> re1.isAmbiguous(id) || re2.isAmbiguous(id))
                                    .collect(ImmutableCollectors.toList()) +
                            " in NATURAL JOIN");

        return internalJoinUsing(re1, re2, shared);
    }

    /**
     * JOIN USING
     *
     * @param re1 a {@link RelationalExpression}
     * @param re2 a {@link RelationalExpression}
     * @param using a {@link ImmutableSet}<{@link QuotedID}>
     * @return a {@link RelationalExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     *          or one of the `using' attributes is ambiguous or absent
     */

    public static RelationalExpression joinUsing(RelationalExpression re1, RelationalExpression re2,
                                          ImmutableSet<QuotedID> using) throws IllegalJoinException {

        checkRelationAliasesConsistency(re1, re2);

        if (using.stream().anyMatch(id -> !re1.isUnique(id) || !re2.isUnique(id))) {

            ImmutableList<QuotedID> notFound = using.stream()
                    .filter(id -> re1.isAbsent(id) || re2.isAbsent(id))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<QuotedID> ambiguous = using.stream()
                    .filter(id -> re1.isAmbiguous(id) || re2.isAmbiguous(id))
                    .collect(ImmutableCollectors.toList());

            throw new IllegalJoinException(re1, re2,
                    (!notFound.isEmpty() ? "Attribute " + notFound + " in USING cannot be found" : "") +
                    (!notFound.isEmpty() && !ambiguous.isEmpty() ? ", " : "") +
                    (!ambiguous.isEmpty() ? "Attribute " + ambiguous + " in USING are ambiguous" : ""));
        }

        return RelationalExpression.internalJoinUsing(re1, re2, using);
    }

    /**
     * internal implementation of JOIN USING and NATURAL JOIN
     *
     * @param re1 a {@link RelationalExpression}
     * @param re2 a {@link RelationalExpression}
     * @param using a {@link Set}<{@link QuotedID}>
     * @return a {@link RelationalExpression}
     */
    private static RelationalExpression internalJoinUsing(RelationalExpression re1, RelationalExpression re2,
                                                          ImmutableSet<QuotedID> using) {

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(re1.filterAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && re2.isAbsent(id.getAttribute()))
                                || (id.getRelation() == null && using.contains(id.getAttribute()))))

                .putAll(re2.filterAttributes(id ->
                        (id.getRelation() != null && !using.contains(id.getAttribute()))
                                || (id.getRelation() == null && re1.isAbsent(id.getAttribute()))))

                .build();

        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(re1.atoms)
                .addAll(re2.atoms)
                .addAll(using.stream()
                        .map(id -> new QualifiedAttributeID(null, id))
                        .map(id -> {
                            // TODO: this will be removed later, when OBDA factory will start checking non-nulls
                            Variable v1 = re1.attributes.get(id);
                            if (v1 == null)
                                throw new IllegalArgumentException("Variable " + id + " not found in " + re1);
                            Variable v2 = re2.attributes.get(id);
                            if (v2 == null)
                                throw new IllegalArgumentException("Variable " + id + " not found in " + re2);
                            return FACTORY.getFunctionEQ(v1, v2);
                        })
                        .iterator())
                .build();

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                getAttributeOccurrences(re1, re2,
                        id -> using.contains(id)
                                ? re1.attributeOccurrences.get(id)
                                : attributeOccurrencesUnion(id, re1, re2));

        return new RelationalExpression(atoms, attributes, attributeOccurrences);
    }

    /**
     *
     * @param atoms a {@link ImmutableList}<{@link Function}>
     * @param unqualifiedAttributes a {@link ImmutableMap}<{@link QuotedID}, {@link Variable}>
     * @param alias a {@link QuotedID}
     * @return a {@link RelationalExpression}
     */

    public static RelationalExpression create(ImmutableList<Function> atoms, ImmutableMap<QuotedID, Variable> unqualifiedAttributes, RelationID alias) {

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(unqualifiedAttributes.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> new QualifiedAttributeID(alias, e.getKey()),
                                Map.Entry::getValue)))
                .putAll(unqualifiedAttributes.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> new QualifiedAttributeID(null, e.getKey()),
                                Map.Entry::getValue)))
                .build();

        ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                unqualifiedAttributes.keySet().stream()
                        .collect(ImmutableCollectors.toMap(identity(), id -> ImmutableSet.of(alias)));

        return new RelationalExpression(atoms, attributes, attributeOccurrences);
    }

    /**
     * (relational expression) AS A
     *
     * @param re a {@link RelationalExpression}
     * @param alias a {@link QuotedID}
     * @return a {@link RelationalExpression}
     */

    public static RelationalExpression alias(RelationalExpression re, RelationID alias) {

        ImmutableMap<QuotedID, Variable> unqualifiedAttributes =
                re.attributes.entrySet().stream()
                        .filter(e -> e.getKey().getRelation() == null)
                        .collect(ImmutableCollectors.toMap(
                                e -> e.getKey().getAttribute(), Map.Entry::getValue));

        return create(re.atoms, unqualifiedAttributes, alias);
    }


    /**
     * treats null values as empty sets
     *
     * @param id a {@link QuotedID}
     * @param re1 a {@link RelationalExpression}
     * @param re2 a {@link RelationalExpression}
     * @return the union of occurrences of id in e1 and e2
     */

    private static ImmutableSet<RelationID> attributeOccurrencesUnion(QuotedID id,
                                                                      RelationalExpression re1, RelationalExpression re2) {

        ImmutableSet<RelationID> s1 = re1.attributeOccurrences.get(id);
        ImmutableSet<RelationID> s2 = re2.attributeOccurrences.get(id);

        if (s1 == null)
            return s2;

        if (s2 == null)
            return s1;

        return ImmutableSet.<RelationID>builder().addAll(s1).addAll(s2).build();
    }

    private ImmutableMap<QualifiedAttributeID, Variable> filterAttributes(java.util.function.Predicate<QualifiedAttributeID> condition) {

        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()))
                .collect(ImmutableCollectors.toMap());
    }


    private static ImmutableMap<QuotedID, ImmutableSet<RelationID>> getAttributeOccurrences(RelationalExpression re1,
                                                                                            RelationalExpression re2,
                                                                                            java.util.function.Function<QuotedID, ImmutableSet<RelationID>>
                                                                                                    collector) {

        ImmutableSet<QuotedID> keys = ImmutableSet.<QuotedID>builder()
                .addAll(re1.attributeOccurrences.keySet())
                .addAll(re2.attributeOccurrences.keySet())
                .build();

        return keys.stream()
                .collect(ImmutableCollectors.toMap(identity(), collector));
    }


    /**
     * throw IllegalJoinException if a relation alias occurs in both arguments of the join
     *
     * @param re1 a {@link RelationalExpression}
     * @param re2 a {@link RelationalExpression}
     * @throws IllegalJoinException if the same alias occurs in both arguments
     */

    private static void checkRelationAliasesConsistency(RelationalExpression re1, RelationalExpression re2) throws IllegalJoinException {

        ImmutableSet<RelationID> alias1 = re1.attributes.keySet().stream()
                .filter(id -> id.getRelation() != null)
                .map(QualifiedAttributeID::getRelation).collect(ImmutableCollectors.toSet());

        ImmutableSet<RelationID> alias2 = re2.attributes.keySet().stream()
                .filter(id -> id.getRelation() != null)
                .map(QualifiedAttributeID::getRelation).collect(ImmutableCollectors.toSet());

        if (alias1.stream().anyMatch(alias2::contains))
            throw new IllegalJoinException(re1, re2 ,"Relation alias " +
                    alias1.stream().filter(alias2::contains).collect(ImmutableCollectors.toList()) +
                    " occurs in both arguments of the JOIN");

    }


    @Override
    public String toString() {
        return "RelationalExpression : " + atoms + " with " + attributes + " and " + attributeOccurrences;
    }


}
