package it.unibz.inf.ontop.dbschema.impl.json;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

public class ConflictingVariableInJoinViewException extends MetadataExtractionException {
    private final ImmutableSet<QuotedID> conflictingAttributeIds;

    protected ConflictingVariableInJoinViewException(ImmutableSet<QuotedID> conflictingAttributeIds) {
        super("Conflict(s) detected: the following attribute(s) correspond(s) to multiple" +
                " columns in the parent relations: " + conflictingAttributeIds);
        this.conflictingAttributeIds = conflictingAttributeIds;
    }

    public ImmutableSet<QuotedID> getConflictingAttributeIds() {
        return conflictingAttributeIds;
    }
}
