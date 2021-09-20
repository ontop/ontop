package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.*;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by roman on 24/01/2017.
 */
public class RAExpressionAttributes  {

    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private final RAExpressionAttributeOccurrences occurrences;

    /**
     * constructs a relation expression
     *
     * @param attributes  an {@link ImmutableMap}<{@link QualifiedAttributeID}, {@link ImmutableTerm}>
     * @param occurrences an {@link RAExpressionAttributeOccurrences}>>
     */
    public RAExpressionAttributes(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                           RAExpressionAttributeOccurrences occurrences) {
        this.attributes = attributes;
        this.occurrences = occurrences;
    }

    public ImmutableMap<QualifiedAttributeID, ImmutableTerm> asMap() {
        return attributes;
    }

    public ImmutableTerm get(QuotedID attributeId) {
        return get(new QualifiedAttributeID(null, attributeId));
    }

    public ImmutableTerm get(QualifiedAttributeID id) {
        return attributes.get(id);
    }

    RAExpressionAttributeOccurrences getOccurrences() { return occurrences; }




    public ImmutableMap<QuotedID, ImmutableTerm> getUnqualifiedAttributes() {
        return selectAttributes(id -> id.getRelation() == null)
                .collect(ImmutableCollectors.toMap(e -> e.getKey().getAttribute(), Map.Entry::getValue));
    }


    Stream<Map.Entry<QualifiedAttributeID, ImmutableTerm>> selectAttributes(Predicate<QualifiedAttributeID> condition) {
        return attributes.entrySet().stream()
                .filter(e -> condition.test(e.getKey()));
    }


    @Override
    public String toString() {
        return "attributes: " + attributes + " with " + occurrences;
    }
}
