package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.impl.DelegatingMetadataProvider;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Stream;

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

public class ImplicitDBConstraintsProvider extends DelegatingMetadataProvider {

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
    public void insertIntegrityConstraints(DatabaseRelationDefinition relation, MetadataLookup metadataLookup) {

        int counter = 0; // id of the generated constraint

        for (String[] constraint : uniqueConstraints) {
            try {
                ConstraintDescriptor uc = getConstraintDescriptor(metadataLookup, constraint[0], constraint[1].split(","));
                if (!uc.table.getID().equals(relation.getID()))
                    continue;
                UniqueConstraint.Builder builder = UniqueConstraint.builder(uc.table, getTableName(uc.table) + "_USER_UC_" + counter);
                for (QuotedID a : uc.attributeIds)
                    builder.addDeterminant(a);
                builder.build();
                counter++;
            }
            catch (AttributeNotFoundException e) {
                LOGGER.warn("Error in user-supplied foreign key constraints: {} not found in {}.", e.getAttributeID(), e.getRelation());
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
                String name = getTableName(fk.table) + "_USER_FK_" + getTableName(pk.table) + "_" + counter;
                ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(name, fk.table, pk.table);
                for (int i = 0; i < pkAttrs.length; i++)
                    builder.add(fk.attributeIds.get(i), pk.attributeIds.get(i));
                builder.build();
                counter++;
            }
            catch (AttributeNotFoundException e) {
                LOGGER.warn("Error in user-supplied foreign key constraints: {} not found in {}.", e.getAttributeID(), e.getRelation());
            }
            catch (MetadataExtractionException e) {
                LOGGER.warn("Error in user-supplied foreign key constraints: {}.", e.getMessage());
            }
        }
    }

    private String getTableName(DatabaseRelationDefinition relation) {
        return relation.getID().getTableID().getName();
    }

    private static final class ConstraintDescriptor {
        final DatabaseRelationDefinition table;
        final ImmutableList<QuotedID> attributeIds;

        ConstraintDescriptor(DatabaseRelationDefinition table, ImmutableList<QuotedID> attributeIds) {
            this.table = table;
            this.attributeIds = attributeIds;
        }
    }

    private ConstraintDescriptor getConstraintDescriptor(MetadataLookup metadataLookup, String tableName, String[] attributeNames) throws MetadataExtractionException {

        DatabaseRelationDefinition relation = metadataLookup.getRelation(getRelationIDFromString(tableName));
        return new ConstraintDescriptor(
               relation,
                Stream.of(attributeNames)
                    .map(idFactory::createAttributeID)
                    .collect(ImmutableCollectors.toList()));
    }

    private RelationID getRelationIDFromString(String tableName) {
        String[] names = tableName.split("\\.");
        return (names.length == 1)
                ? idFactory.createRelationID(null, tableName)
                : idFactory.createRelationID(names[0], names[1]);
    }
}
