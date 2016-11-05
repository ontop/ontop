package it.unibz.inf.ontop.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.sql.QualifiedAttributeID;
import it.unibz.inf.ontop.sql.QuotedID;
import it.unibz.inf.ontop.sql.RelationID;

import java.util.stream.Collectors;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 */
public class RelationalExpression {
    private ImmutableList<Function> atoms;
    private ImmutableMap<QualifiedAttributeID, Variable> attributes;
    private ImmutableMap<QuotedID, ImmutableSet<RelationID>> attributeOccurrences;

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
        return  e1;
    }


    /**
     * JOIN ON of two relations (also denoted by , in SQL)
     *
     * @param e1
     * @param e2
     * @return
     */
    public static RelationalExpression joinOn(RelationalExpression e1, RelationalExpression e2) {
        return e1;
    }

    /**
     * JOIN USING of two relations (also denoted by , in SQL)
     *
     * @param e1
     * @param e2
     * @return
     */
    public static RelationalExpression joinUsing(RelationalExpression e1, RelationalExpression e2) {
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
