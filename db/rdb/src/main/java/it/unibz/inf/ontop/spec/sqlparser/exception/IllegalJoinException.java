package it.unibz.inf.ontop.spec.sqlparser.exception;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.spec.sqlparser.RAExpressionAttributes;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class IllegalJoinException extends Exception {

    public IllegalJoinException(RAExpressionAttributes re1, RAExpressionAttributes re2, ImmutableList<QuotedID> absent, ImmutableList<QuotedID> ambiguous) {
        super(Stream.of(
                        renderErrorMessage(absent, "cannot be found", "cannot be found"),
                        renderErrorMessage(ambiguous, "is ambiguous", "are ambiguous"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", ")) + " with " + re1 + " and " + re2);
    }

    public IllegalJoinException(RAExpressionAttributes re1, RAExpressionAttributes re2, ImmutableSet<RelationID> intersection) {
        super(intersection.stream()
                .map(RelationID::getSQLRendering)
                .collect(Collectors.joining(", ", "Relation alias ", " occurs in both arguments of the JOIN " + re1 + " and " + re2)));
    }

    private static String renderErrorMessage(ImmutableList<QuotedID> list, String suffix1, String suffixN) {
        switch (list.size()) {
            case 0:
                return "";
            case 1:
                return "Attribute " + list.get(0).getSQLRendering() + " " + suffix1;
            default:
                return list.stream().map(QuotedID::getSQLRendering).collect(Collectors.joining(", ", "Attributes ", " " + suffixN));
        }
    }
}
