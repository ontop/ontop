package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * Used for reading user-provided information about unique constraints and foreign keys
 * Needed for better performance in cases where views and materialized views are present.
 *
 * Associated JUnit Tests @TestImplicitDBConstraints, @TestQuestImplicitDBConstraints
 *
 *  @author Dag Hovland (first version)
 *
 *  Moved from ImplicitDBContraintsReader
 *
 */

public class ImplicitDBConstraintsProvider implements MetadataProvider {

    private static final Logger log = LoggerFactory.getLogger(ImplicitDBConstraintsProvider.class);

    private final QuotedIDFactory idFactory;

    // List of two-element arrays: table id and a comma-separated list of columns
    private final ImmutableList<String[]> uniqueConstraints;

    // List of four-element arrays: foreign key table id, comma-separated foreign key columns,
    //                              primary key (referred) table id, comma-separated primary key columns
    private final ImmutableList<String[]> foreignKeys;

    ImplicitDBConstraintsProvider(QuotedIDFactory idFactory,
                                  ImmutableList<String[]> uniqueConstraints,
                                  ImmutableList<String[]> foreignKeys) {
        this.idFactory = idFactory;
        this.uniqueConstraints = uniqueConstraints;
        this.foreignKeys = foreignKeys;
    }

    /**
     * Extracts relation IDs for all relations referred to by the user supplied foreign keys
     * (but not the relations of the foreign keys)
     *
     * @return relation ids that are referred to by foreign keys
     */
    @Override
    public ImmutableList<RelationID> getRelationIDs() {
        Set<RelationID> referredTables = new HashSet<>();

        for (String[] fk : foreignKeys) {
            RelationID pkTableId = getRelationIDFromString(fk[2], idFactory);
            referredTables.add(pkTableId);
        }

        return ImmutableList.copyOf(referredTables);
    }

    @Override
    public ImmutableList<RelationDefinition.AttributeListBuilder> getRelationAttributes(RelationID relationID) {
        return ImmutableList.of();
    }

    /**
     *
     * Inserts the user-supplied primary keys / unique constraints columns into the metadata object
     */
    @Override
    public void insertIntegrityConstraints(MetadataLookup md) {
        int counter = 0; // id of the generated constraint

        for (String[] constraint : uniqueConstraints) {
            ConstraintDescriptor uc = getConstraintDescriptor(md, constraint[0],  constraint[1].split(","), idFactory);
            if (uc != null) { // if all attributes have been identified
                UniqueConstraint.BuilderImpl builder = UniqueConstraint.builder(uc.table, uc.table.getID().getTableName() + "_USER_UC_" + counter);
                for (Attribute a : uc.attributes)
                    builder.addDeterminant(a);
                uc.table.addUniqueConstraint(builder.build());
            }
            else {
                System.out.println("NOT FOUND: " + Arrays.toString(constraint)  + " in " + md);
            }
            counter++;
        }
        for (String[] constraint : foreignKeys) {
            String[] pkAttrs = constraint[3].split(",");
            String[] fkAttrs = constraint[1].split(",");
            if (fkAttrs.length != pkAttrs.length) {
                log.warn("Error in user-supplied foreign key: foreign key refers to different number of columns " + constraint + ".");
                continue;
            }

            ConstraintDescriptor pk = getConstraintDescriptor(md, constraint[2], pkAttrs, idFactory);
            ConstraintDescriptor fk = getConstraintDescriptor(md, constraint[0], fkAttrs, idFactory);
            if (pk != null && fk != null) { // if all attributes have been identified
                ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(fk.table, pk.table);
                for (int i = 0; i < pkAttrs.length; i++) {
                    builder.add(fk.attributes[i], pk.attributes[i]);
                }
                fk.table.addForeignKeyConstraint(
                        builder.build(fk.table.getID().getTableName() + "_USER_FK_" + pk.table.getID().getTableID().getName() + "_" + counter));
                counter++;
            }
        }
    }

    private static final class ConstraintDescriptor {
        DatabaseRelationDefinition table;
        Attribute[] attributes;
    }

    private static ConstraintDescriptor getConstraintDescriptor(MetadataLookup md, String tableName, String[] attributeNames, QuotedIDFactory idFactory) {
        ConstraintDescriptor result = new ConstraintDescriptor();

        RelationID tableId = getRelationIDFromString(tableName, idFactory);
        try {
            result.table = (DatabaseRelationDefinition)md.get(tableId);
        }
        catch (RelationNotFoundException e) {
            log.warn("Error in user-supplied constraint: table " + tableId + " not found.");
            return null;
        }

        result.attributes = new Attribute[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++) {
            QuotedID attrId = idFactory.createAttributeID(attributeNames[i]);
            result.attributes[i] = result.table.getAttribute(attrId);
            if (result.attributes[i] == null) {
                log.warn("Error in user-supplied constraint: column " + attrId + " not found in table " + result.table.getID() + ".");
                return null;
            }
        }
        return result;
    }

    private static RelationID getRelationIDFromString(String tableName, QuotedIDFactory idFactory) {
        String[] names = tableName.split("\\.");
        return (names.length == 1)
                ? idFactory.createRelationID(null, tableName)
                : idFactory.createRelationID(names[0], names[1]);
    }
}
