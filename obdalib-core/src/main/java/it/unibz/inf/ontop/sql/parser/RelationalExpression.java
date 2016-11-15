package it.unibz.inf.ontop.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.QualifiedAttributeID;
import it.unibz.inf.ontop.sql.QuotedID;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQuery;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

import java.util.List;
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

        // TODO: better exception?
        if (!relationAliasesConsistent(e1.attributes, e2.attributes))
            throw new InvalidSelectQuery("Relation alias occurs in both arguments of the join", null);

        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms).addAll(e2.atoms).build();

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(e1.filterAttributes(id ->
                        (id.getRelation() != null) || e2.isAbsent(id.getAttribute())))

                .putAll(e2.filterAttributes(id ->
                        (id.getRelation() != null) || e1.isAbsent(id.getAttribute())))

                .build();

        Map<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                attributeOccurrencesKeys(e1, e2).stream()
                        .collect(Collectors.toMap(identity(),
                            id -> attribiteOccuurencesUnion(id, e1, e2)));

        return new RelationalExpression(atoms, attributes, ImmutableMap.copyOf(attributeOccurrences));
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

        Set<QuotedID> sharedAttributes = e1.attributeOccurrences.keySet().stream()
                .filter(id -> !e1.isAbsent(id) && !e2.isAbsent(id))
                .collect(Collectors.toSet());

        // TODO: better exception? more informative error message?
        if (sharedAttributes.stream().anyMatch(id -> e1.isAmbiguous(id) || e2.isAmbiguous(id)))
            throw new UnsupportedOperationException("common ambiguous attribute in select");

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(e1.filterAttributes(id ->
                                (id.getRelation() != null && !sharedAttributes.contains(id.getAttribute()))
                                        || (id.getRelation() == null && e2.isAbsent(id.getAttribute()))))

                .putAll(e2.filterAttributes(id ->
                                (id.getRelation() !=null && !sharedAttributes.contains(id.getAttribute()))
                                    || (id.getRelation() == null && e1.isAbsent(id.getAttribute()))))
                // TODO: merge into the first group?
                .putAll(e1.filterAttributes(id ->
                                id.getRelation() == null && sharedAttributes.contains(id.getAttribute())))

                .build();

        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms)
                .addAll(e2.atoms)
                .addAll(sharedAttributes.stream()
                        .map(id -> FACTORY.getFunctionEQ(e1.attributes.get(id), e2.attributes.get(id)))
                        .iterator())
                .build();

        Map<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                attributeOccurrencesKeys(e1, e2).stream()
                        .collect(Collectors.toMap(identity(),
                            id -> sharedAttributes.contains(id)
                                    ? e1.attributeOccurrences.get(id)
                                    : attribiteOccuurencesUnion(id, e1, e2)));


        return new RelationalExpression(atoms, attributes, ImmutableMap.copyOf(attributeOccurrences));
    }


    /**
     * Add an {@link ImmutableList} of atoms {@link Function} to {@link RelationalExpression}
     *
     * @param e1         ia a {@link RelationalExpression)
     * @param atomsToAdd {@link ImmutableList} of {@link Function}
     * @return a {@link RelationalExpression}
     */
    // TODO: i'm not sure this method is helpful (to be removed)
    static RelationalExpression addAtoms(RelationalExpression e1, Function atom) {

        // and add an atom for the expression
        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms).add(atom).build();

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
        Function atomOn = getAtomOnExpression.apply(current);
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
     * @param usingColumns is a {@link ImmutableSet}<{@link QuotedID}>
     * @return a {@link RelationalExpression)
     */
    static RelationalExpression joinUsing(RelationalExpression e1, RelationalExpression e2, ImmutableSet<QuotedID> usingColumns) {

        for (QuotedID id : usingColumns)
            if (!e1.isUnique(id) || !e2.isUnique(id))
                throw new UnsupportedOperationException("ambiguous column attributes in using statement");

        // TODO: create a new method to avoid duplicating code (see naturalJoin)

        if (!relationAliasesConsistent(e1.attributes, e2.attributes))
            throw new UnsupportedOperationException("Relation alias occurs in both arguments of the join", null);

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(e1.attributes.keySet().stream()
                        .filter(id -> !usingColumns.contains(id.getAttribute()) || e2.isAbsent(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e1.attributes.get(id))))
                .putAll(e2.attributes.keySet().stream()
                        .filter(id -> !usingColumns.contains(id.getAttribute()) || e1.isAbsent(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e2.attributes.get(id))))
                .putAll(e1.attributes.keySet().stream()
                        .filter(id -> usingColumns.contains(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e1.attributes.get(id))))
                .build();

        final Map<QuotedID, Variable> e1QuotedID = e1.attributes.entrySet().stream()
                .filter( p->p.getKey().getRelation() != null )
                .collect(Collectors.toMap(e ->  e.getKey().getAttribute(), Map.Entry::getValue));
        final Map<QuotedID, Variable> e2QuotedID = e2.attributes.entrySet().stream()
                .filter( p->p.getKey().getRelation() != null )
                .collect(Collectors.toMap(e ->  e.getKey().getAttribute(), Map.Entry::getValue));

        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms)
                .addAll(e2.atoms)
                .addAll(usingColumns.stream()
                        // TODO: why can't you use e1.attributes and e2.attributes instead?
                        .map(id -> FACTORY.getFunction(ExpressionOperation.EQ,
                               e1QuotedID.get( id ), e2QuotedID.get(id))).collect(Collectors.toList()))
                .build();

        // TODO: this is not even needed
        ImmutableSet<QuotedID> keys = ImmutableSet.<QuotedID>builder()
                .addAll(e1.attributeOccurrences.keySet())
                .addAll(e2.attributeOccurrences.keySet())
                .build();

        Map<QuotedID, ImmutableSet<RelationID>> attributeOccurrences =
                attributeOccurrencesKeys(e1, e2).stream()
                        .collect(Collectors.toMap(identity(),
                                id -> usingColumns.contains(id)
                                        ? e1.attributeOccurrences.get(id)
                                        : attribiteOccuurencesUnion(id, e1, e2)));

        return new RelationalExpression(atoms, attributes, ImmutableMap.copyOf(attributeOccurrences));
    }

    /**
     * treats null values as empty sets
     *
     * @param e1 a relational expression
     * @param e2 a relational expression
     * @return the union of occurrences of id in e1 and e2
     */
    private static ImmutableSet<RelationID> attribiteOccuurencesUnion(QuotedID id,
                                                             RelationalExpression e1, RelationalExpression e2) {

        ImmutableSet<RelationID> s1 = e1.attributeOccurrences.get(id);
        ImmutableSet<RelationID> s2 = e2.attributeOccurrences.get(id);

        if (s1 == null)
            return s2;

        if (s2 == null)
            return s1;

        return ImmutableSet.<RelationID>builder().addAll(s1).addAll(s2).build();
    }

    private static ImmutableSet<QuotedID> attributeOccurrencesKeys(RelationalExpression e1,
                                                                   RelationalExpression e2) {
        return ImmutableSet.<QuotedID>builder()
                .addAll(e1.attributeOccurrences.keySet())
                .addAll(e2.attributeOccurrences.keySet())
                .build();
    }

    private Map<QualifiedAttributeID, Variable> filterAttributes(java.util.function.Predicate<QualifiedAttributeID> condition) {

            return attributes.entrySet().stream()
                    .filter(e -> condition.test(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * return false if a relation alias occurs in both arguments of the join.
     *
     * @param attributes1 is an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>
     * @param attributes2 is an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link Variable}>
     */
    private static boolean relationAliasesConsistent(ImmutableMap<QualifiedAttributeID, Variable> attributes1, ImmutableMap<QualifiedAttributeID, Variable> attributes2) {
        Set<RelationID> alias1 = attributes1.keySet().stream()
                .filter(key -> key.getRelation() != null)
                .map(QualifiedAttributeID::getRelation).collect(Collectors.toSet());
        Set<RelationID> alias2 = attributes2.keySet().stream()
                .filter(key -> key.getRelation() != null)
                .map(QualifiedAttributeID::getRelation).collect(Collectors.toSet());
        alias1.retainAll(alias2); // intersection
        return alias1.isEmpty();
    }


    @Override
    public String toString() {
        return "RelationalExpression : " + atoms + "\n" + attributes + "\n" + attributeOccurrences;
    }


}
