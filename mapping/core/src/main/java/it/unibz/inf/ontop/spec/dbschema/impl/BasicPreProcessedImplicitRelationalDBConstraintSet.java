package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BasicPreProcessedImplicitRelationalDBConstraintSet implements PreProcessedImplicitRelationalDBConstraintSet {

    private static final Logger log = LoggerFactory.getLogger(BasicPreProcessedImplicitRelationalDBConstraintSet.class);

    // List of two-element arrays: table id and a comma-separated list of columns
    private final ImmutableList<String[]> ucs;

    // List of four-element arrays: foreign key table id, comma-separated foreign key columns,
    //                              primary key (referred) table id, comma-separated primary key columns
    private final ImmutableList<String[]> fks;


    BasicPreProcessedImplicitRelationalDBConstraintSet(ImmutableList<String[]> uniqueConstraints,
                                                       ImmutableList<String[]> foreignKeys) {
        this.ucs = uniqueConstraints;
        this.fks = foreignKeys;
    }

    /**
     * Extracts relation IDs for all relations referred to by the user supplied foreign keys
     * (but not the relations of the foreign keys)
     *
     * @param idfac QuotedIDFactory
     * @return relation ids that are referred to by foreign keys
     */
    @Override
    public Set<RelationID> getReferredTables(QuotedIDFactory idfac) {
        Set<RelationID> referredTables = new HashSet<>();

        for (String[] fk : fks) {
            RelationID pkTableId = getRelationIDFromString(fk[2], idfac);
            referredTables.add(pkTableId);
        }

        return referredTables;
    }

    /**
     *
     * Inserts the user-supplied primary keys / unique constraints columns into the metadata object
     *
     * TODO: refactor into an immutable style
     *
     */
    @Override
    public void insertUniqueConstraints(DBMetadata md) {
        QuotedIDFactory idfac = md.getDBParameters().getQuotedIDFactory();
        int counter = 0; // id of the generated constraint

        for (String[] uc : ucs) {
            RelationID tableId = getRelationIDFromString(uc[0], idfac);
            DatabaseRelationDefinition td = md.getDatabaseRelation(tableId);

            if (td == null) {
                log.warn("Error in user-supplied unique constraint: table " + tableId + " not found.");
                continue;
            }
            UniqueConstraint.BuilderImpl builder = UniqueConstraint.builder(td, td.getID().getTableName() + "_USER_UC_" + counter);
            String[] attrs = uc[1].split(",");
            for (String attr : attrs) {
                QuotedID attrId = idfac.createAttributeID(attr);
                Attribute attribute = td.getAttribute(attrId);
                if (attribute == null) {
                    log.warn("Error in user-supplied unique constraint: column " + attrId + " not found in table " + tableId + ".");
                    builder = null;
                    break;
                }
                //td.setAttribute(key_pos, new Attribute(td, attr.getName(), attr.getType(), false, attr.getSQLTypeName())); // true
                // ROMAN (17 Aug 2015): do we really change it into NON NULL?
                builder.addDeterminant(attribute);
            }
            if (builder != null) // if all attributes have been identified
                td.addUniqueConstraint(builder.build());
            counter++;
        }
    }



    /**
     *
     * Inserts the user-supplied foreign keys into the metadata object
     *
     * TODO: refactor into an immutable style
     */
    @Override
    public void insertForeignKeyConstraints(DBMetadata md) {
        QuotedIDFactory idfac = md.getDBParameters().getQuotedIDFactory();
        int counter = 0; // id of the generated constraint

        for (String[] fk : fks) {
            RelationID pkTableId = getRelationIDFromString(fk[2], idfac);
            DatabaseRelationDefinition pkTable = md.getDatabaseRelation(pkTableId);
            if (pkTable == null) {
                log.warn("Error in user-supplied foreign key: table " + pkTableId + " not found.");
                continue;
            }
            RelationID fkTableId = getRelationIDFromString(fk[0], idfac);
            DatabaseRelationDefinition fkTable = md.getDatabaseRelation(fkTableId);
            if (fkTable == null) {
                log.warn("Error in user-supplied foreign key: table " + fkTableId + " not found.");
                continue;
            }
            String[] pkAttrs = fk[3].split(",");
            String[] fkAttrs = fk[1].split(",");
            if (fkAttrs.length != pkAttrs.length) {
                log.warn("Error in user-supplied foreign key: foreign key refers to different number of columns " + fk + ".");
                continue;
            }

            ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(fkTable, pkTable);
            for (int i = 0; i < pkAttrs.length; i++) {
                QuotedID pkAttrId = idfac.createAttributeID(pkAttrs[i]);
                Attribute pkAttr = pkTable.getAttribute(pkAttrId);
                if (pkAttr == null) {
                    log.warn("Error in user-supplied foreign key: column " + pkAttrId + " not found in in table " + pkTable + ".");
                    builder = null;
                    break;
                }
                QuotedID fkAttrId = idfac.createAttributeID(fkAttrs[i]);
                Attribute fkAttr = fkTable.getAttribute(fkAttrId);
                if (fkAttr == null) {
                    log.warn("Error in user-supplied foreign key: column " + fkAttrId + " not found in table " + fkTable + ".");
                    builder = null;
                    break;
                }

                builder.add(fkAttr, pkAttr);
            }
            if (builder != null) // if all attributes have been identified
                fkTable.addForeignKeyConstraint(
                        builder.build(fkTable.getID().getTableName() + "_USER_FK_" + pkTable.getID().getTableName() + "_" + counter));
            counter++;
        }
    }

    private static RelationID getRelationIDFromString(String name, QuotedIDFactory idfac) {
        String[] names = name.split("\\.");
        if (names.length == 1)
            return idfac.createRelationID(null, name);
        else
            return idfac.createRelationID(names[0], names[1]);
    }
}
