package it.unibz.inf.ontop.dbschema.impl.json;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

public class ConflictingVariableInJoinViewException extends MetadataExtractionException {
    private final ImmutableSet<QualifiedAttributeID> conflictingAttributeIds;

    protected ConflictingVariableInJoinViewException(ImmutableSet<QualifiedAttributeID> conflictingAttributeIds) {
        super("Conflict(s) detected: the following attribute(s) correspond(s) to multiple" +
                " columns in the parent relations: " + conflictingAttributeIds);
        this.conflictingAttributeIds = conflictingAttributeIds;
    }

    public ImmutableSet<QualifiedAttributeID> getConflictingAttributeIds() {
        return conflictingAttributeIds;
    }
}
