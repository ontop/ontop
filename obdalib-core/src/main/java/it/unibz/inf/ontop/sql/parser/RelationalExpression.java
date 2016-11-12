package it.unibz.inf.ontop.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.Attribute;
import it.unibz.inf.ontop.sql.QualifiedAttributeID;
import it.unibz.inf.ontop.sql.QuotedID;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQuery;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQuery;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * @param atoms
     * @param attributes
     * @param attributeOccurrences
     */

    public RelationalExpression(ImmutableList<Function> atoms,
                                ImmutableMap<QualifiedAttributeID, Variable> attributes,
                                ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences) {
        this.atoms = atoms;
        this.attributes = attributes;
        this.attributeOccurrences = attributeOccurrences;
    }

    /**
     *
     * @param attribute
     * @return
     */

    public boolean isAbsent(QuotedID attribute) {
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
     * @param e1
     * @param e2
     * @return
     */

    public static RelationalExpression crossJoin(RelationalExpression e1, RelationalExpression e2) {
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
     * NATURAL JOIN of two relations (also denoted by , in SQL)
     *
     * @param e1
     * @param e2
     * @return
     */
    public static RelationalExpression naturalJoin(RelationalExpression e1, RelationalExpression e2) {

        // All the common columns
        ImmutableMap<QualifiedAttributeID, Variable> commonAttributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(e1.attributes.keySet().stream()
                        .filter(id -> (id.getRelation() != null) && ! e2.isAbsent(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e1.attributes.get(id))))
                .putAll(e2.attributes.keySet().stream()
                    .filter(id -> (id.getRelation() != null) && ! e1.isAbsent(id.getAttribute()))
                    .collect(Collectors.toMap(id -> id, id -> e2.attributes.get(id)))).build();

        // Every column in the first (left) table that is not a common column
        // Every column in the second (right) table that is not a common column
        ImmutableMap<QualifiedAttributeID, Variable> leftAndRightAttributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
                .putAll(e1.attributes.keySet().stream()
                        .filter(id -> !commonAttributes.containsKey(id) && (id.getRelation() != null) || e2.isAbsent(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e1.attributes.get(id))))
                .putAll(e2.attributes.keySet().stream()
                        .filter(id -> !commonAttributes.containsKey(id) && (id.getRelation() != null) || e1.isAbsent(id.getAttribute()))
                        .collect(Collectors.toMap(id -> id, id -> e2.attributes.get(id))))
                .build();

        ImmutableMap<QualifiedAttributeID, Variable> attributes = ImmutableMap.<QualifiedAttributeID, Variable>builder()
            .putAll(commonAttributes).putAll( leftAndRightAttributes ).build();

        final ImmutableList<Term> eqTerms = ImmutableList.copyOf( commonAttributes.values());
        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms).addAll(e2.atoms)
                .add(FACTORY.getFunction(ExpressionOperation.EQ, eqTerms))
                .build();

        // TODO: add attributeOccurrences   { C  → F1.attr-in(C) | C ∈ S }
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
     * Add an {@link ImmutableList} of atoms {@link Function} to {@link RelationalExpression}
     *
     * @param e1 ia a {@link RelationalExpression)
     * @param atomsToAdd {@link ImmutableList} of {@link Function}
     * @return a {@link RelationalExpression}
     */
    // TODO: i'm not sure this method is helpful (to be removed)
    public static RelationalExpression addAtoms(RelationalExpression e1,  ImmutableList<Function> atomsToAdd) {

        // and add an atom for the expression
        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(e1.atoms).addAll(atomsToAdd).build();

        return new RelationalExpression(atoms, e1.attributes, e1.attributeOccurrences);
    }

    /**
     *
     * @param e1 is a {@link RelationalExpression)
     * @param e2 is a {@link RelationalExpression)
     * @param getAtomFromOnExpression is a function that given a {@link RelationalExpression} returns an atom form an ON {@link Expression}
     * @return a {@link RelationalExpression}
     */
    public static RelationalExpression joinOn(RelationalExpression e1, RelationalExpression e2, java.util.function.Function<RelationalExpression, Function> getAtomOnExpression) {

        RelationalExpression current = RelationalExpression.crossJoin(e1,e2);
        final Function atomOn = getAtomOnExpression.apply(current);
        // and add an atom for the ON expression
        ImmutableList<Function> atoms = ImmutableList.<Function>builder()
                .addAll(current.getAtoms()).add(atomOn).build();

        return new RelationalExpression(atoms, e1.attributes, e1.attributeOccurrences);
    }


    /**
     * JOIN USING of two relations (also denoted by , in SQL)
     *
     * @param e1
     * @param e2
     * @param usingColumns
     * @return
     */
    public static RelationalExpression joinUsing(RelationalExpression e1, RelationalExpression e2, List<Column> usingColumns) {
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

    @Override
    public String toString() {
        return "RelationalExpression : " + atoms + "\n" + attributes + "\n" + attributeOccurrences;
    }



}
