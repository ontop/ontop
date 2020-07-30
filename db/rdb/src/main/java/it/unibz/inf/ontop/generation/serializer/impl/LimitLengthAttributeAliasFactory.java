package it.unibz.inf.ontop.generation.serializer.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;

import java.util.HashSet;
import java.util.Set;

public class LimitLengthAttributeAliasFactory implements AttributeAliasFactory {

    private final QuotedIDFactory rawIdFactory;
    private final Set<String> used = new HashSet<>();

    private final int maxLength;
    private final int maxIndexLength;
    private final int maxIndexValue;

    LimitLengthAttributeAliasFactory(QuotedIDFactory idFactory, int maxLength, int maxIndexLength) {
        this.rawIdFactory = new RawQuotedIDFactory(idFactory);
        this.maxLength = maxLength;
        this.maxIndexLength = maxIndexLength;
        this.maxIndexValue = (int)Math.pow(10, maxIndexLength);
    }

    @Override
    public QuotedID createAttributeAlias(String variable) {

        int length = variable.length();
        if (length <= maxLength) {
            used.add(variable);
            return rawIdFactory.createAttributeID(variable);
        }

        String shortened = variable.substring(0, maxLength - maxIndexLength);

        // Naive implementation
        for (int i = 0; i < maxIndexValue; i++) {
            String replacement = shortened + i;
            if (!used.contains(replacement)) {
                used.add(replacement);
                return rawIdFactory.createAttributeID(replacement);
            }
            used.add(replacement);
        }

        // TODO: find a better exception
        throw new RuntimeException("Impossible to create a new variable/view " + shortened
                + "???" + " : already " + maxIndexValue + " of them.");
    }
}
