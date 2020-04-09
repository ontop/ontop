package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.impl.DelegatingRDBMetadataProvider;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 *
 * Used for reading user-supplied information about unique constraints and foreign keys
 * Needed for better performance in cases where views and materialized views are present.
 *
 * Associated JUnit Tests @TestImplicitDBConstraints, @TestQuestImplicitDBConstraints
 *
 *  @author Dag Hovland (first version)
 *
 */

public class ImplicitDBConstraintsProvider extends DelegatingRDBMetadataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImplicitDBConstraintsProvider.class);

    private final QuotedIDFactory idFactory;

    // List of two-element arrays: table id and a comma-separated list of columns
    private final ImmutableList<String[]> uniqueConstraints;

    // List of four-element arrays: foreign key table id, comma-separated foreign key columns,
    //                              primary key (referred) table id, comma-separated primary key columns
    private final ImmutableList<String[]> foreignKeys;

    ImplicitDBConstraintsProvider(MetadataProvider provider,
                                  ImmutableList<String[]> uniqueConstraints,
                                  ImmutableList<String[]> foreignKeys) {
        super(provider);
        this.idFactory = provider.getDBParameters().getQuotedIDFactory();
        this.uniqueConstraints = uniqueConstraints;
        this.foreignKeys = foreignKeys;
    }

    /**
     *
     * Inserts the user-supplied primary keys / unique constraints columns into the metadata object
     */
    @Override
    public void insertIntegrityConstraints(RelationDefinition relation, MetadataLookup metadataLookup) {

        int counter = 0; // id of the generated constraint

        for (String[] constraint : uniqueConstraints) {
            try {
                ConstraintDescriptor uc = getConstraintDescriptor(metadataLookup, constraint[0], constraint[1].split(","));
                if (!uc.table.getID().equals(relation.getID()))
                    continue;
                UniqueConstraint.BuilderImpl builder = UniqueConstraint.builder(uc.table, uc.table.getID().getTableName() + "_USER_UC_" + counter);
                for (Attribute a : uc.attributes)
                    builder.addDeterminant(a);
                uc.table.addUniqueConstraint(builder.build());
                counter++;
            }
            catch (MetadataExtractionException e) {
                LOGGER.warn("Error in user-supplied unique constraints: {}.", e.getMessage());
            }
        }

        for (String[] constraint : foreignKeys) {
            try {
                String[] fkAttrs = constraint[1].split(",");
                String[] pkAttrs = constraint[3].split(",");
                if (fkAttrs.length != pkAttrs.length)
                    throw new MetadataExtractionException("Different number of columns in " + Arrays.toString(constraint));

                ConstraintDescriptor fk = getConstraintDescriptor(metadataLookup, constraint[0], fkAttrs);
                if (!fk.table.getID().equals(relation.getID()))
                    continue;
                ConstraintDescriptor pk = getConstraintDescriptor(metadataLookup, constraint[2], pkAttrs);
                ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(fk.table, pk.table);
                for (int i = 0; i < pkAttrs.length; i++)
                    builder.add(fk.attributes[i], pk.attributes[i]);

                fk.table.addForeignKeyConstraint(
                        builder.build(fk.table.getID().getTableName() + "_USER_FK_" + pk.table.getID().getTableID().getName() + "_" + counter));
                counter++;
            }
            catch (MetadataExtractionException e) {
                LOGGER.warn("Error in user-supplied foreign key constraints: {}.", e.getMessage());
            }
        }
    }


    private static final class ConstraintDescriptor {
        DatabaseRelationDefinition table;
        Attribute[] attributes;
    }

    private ConstraintDescriptor getConstraintDescriptor(MetadataLookup metadataLookup, String tableName, String[] attributeNames) throws MetadataExtractionException {
        ConstraintDescriptor result = new ConstraintDescriptor();

        RelationID relationId = getRelationIDFromString(tableName);
        RelationDefinition relation = metadataLookup.getRelation(relationId);

        if (!(relation instanceof DatabaseRelationDefinition))
            throw new MetadataExtractionException("Relation " + relation + " is not a " + DatabaseRelationDefinition.class.getName());

        result.table = (DatabaseRelationDefinition)relation;
        result.attributes = new Attribute[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++) {
            QuotedID attributeId = idFactory.createAttributeID(attributeNames[i]);
            result.attributes[i] = result.table.getAttribute(attributeId);
            if (result.attributes[i] == null)
                throw new MetadataExtractionException("Attribute " + attributeId + " not found in " + relationId);
        }
        return result;
    }

    private RelationID getRelationIDFromString(String tableName) {
        String[] names = tableName.split("\\.");
        return (names.length == 1)
                ? idFactory.createRelationID(null, tableName)
                : idFactory.createRelationID(names[0], names[1]);
    }
}
