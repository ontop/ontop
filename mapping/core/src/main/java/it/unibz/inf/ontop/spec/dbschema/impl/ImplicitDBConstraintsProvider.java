package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.impl.DelegatingMetadataProvider;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    // List of two-element arrays: table id and a comma-separated list of columns
    private final ImmutableMultimap<RelationID, DatabaseRelationDescriptor> uniqueConstraints;

    // List of four-element arrays: foreign key table id, comma-separated foreign key columns,
    //                              primary key (referred) table id, comma-separated primary key columns
    private final ImmutableMultimap<RelationID, Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor>> foreignKeys;

    ImplicitDBConstraintsProvider(MetadataProvider provider,
                                  ImmutableList<DatabaseRelationDescriptor> uniqueConstraints,
                                  ImmutableList<Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor>> foreignKeys) throws MetadataExtractionException {
        super(provider);
        this.uniqueConstraints = uniqueConstraints.stream()
                .collect(ImmutableCollectors.toMultimap(c -> c.tableId, Function.identity()));

        this.foreignKeys = foreignKeys.stream()
                .collect(ImmutableCollectors.toMultimap(c -> c.getKey().tableId, Function.identity()));
    }

    /**
     *
     * Inserts the user-supplied primary keys / unique constraints columns into the metadata object
     */
    @Override
    public void insertIntegrityConstraints(DatabaseRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {

        provider.insertIntegrityConstraints(relation, metadataLookup);

        int counter = 0; // id of the generated constraint

        for (DatabaseRelationDescriptor uc : relation.getAllIDs().stream()
                .flatMap(id -> uniqueConstraints.get(id).stream())
                .collect(ImmutableCollectors.toList())) {
            try {
                UniqueConstraint.Builder builder = UniqueConstraint.builder(relation, getTableName(relation) + "_USER_UC_" + counter);
                for (QuotedID a : uc.attributeIds)
                    builder.addDeterminant(a);
                builder.build();
                counter++;
            }
            catch (AttributeNotFoundException e) {
                throw new MetadataExtractionException("Error in user-supplied unique constraints: " + e.getAttributeID() + " not found in " + e.getRelation());
            }
        }

        for (Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor> fkc : relation.getAllIDs().stream()
                .flatMap(id -> foreignKeys.get(id).stream())
                .collect(ImmutableCollectors.toList())) {
            DatabaseRelationDefinition referencedRelation;
            try {
                referencedRelation = metadataLookup.getRelation(fkc.getValue().tableId);
            }
            catch (MetadataExtractionException e) {
                System.out.println("User-supplied foreign key constraint is ignored because the referenced relation is not found: " +
                        fkc.getKey() + " -> " + fkc.getValue());
                continue;
            }
            try {
                String name = getTableName(relation) + "_USER_FK_" + getTableName(referencedRelation) + "_" + counter;
                ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(name, relation, referencedRelation);
                for (int i = 0; i < fkc.getKey().attributeIds.size(); i++)
                    builder.add(fkc.getKey().attributeIds.get(i), fkc.getValue().attributeIds.get(i));
                builder.build();
                counter++;
            }
            catch (AttributeNotFoundException e) {
                throw new MetadataExtractionException("Error in user-supplied foreign key constraints: " + e.getAttributeID() + " not found in " + e.getRelation());
            }
        }
    }

    private String getTableName(DatabaseRelationDefinition relation) {
        return relation.getID().getTableID().getName();
    }

    static final class DatabaseRelationDescriptor {
        final RelationID tableId;
        final ImmutableList<QuotedID> attributeIds;

        DatabaseRelationDescriptor(QuotedIDFactory idFactory, String tableName, String[] attributeNames) {
            this.tableId = getRelationIDFromString(idFactory, tableName);
            this.attributeIds = Stream.of(attributeNames)
                    .map(idFactory::createAttributeID)
                    .collect(ImmutableCollectors.toList());
        }

        private RelationID getRelationIDFromString(QuotedIDFactory idFactory, String tableName) {
            String[] names = tableName.split("\\.");
            return (names.length == 1)
                    ? idFactory.createRelationID(null, tableName)
                    : idFactory.createRelationID(names[0], names[1]);
        }

        @Override
        public String toString() { return tableId + "" + attributeIds; }
    }

}
