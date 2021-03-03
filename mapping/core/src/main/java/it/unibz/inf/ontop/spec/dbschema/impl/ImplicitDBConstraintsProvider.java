package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.impl.DelegatingMetadataProvider;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
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

    private final ImmutableMultimap<RelationID, DatabaseRelationDescriptor> uniqueConstraints;
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
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {

        provider.insertIntegrityConstraints(relation, metadataLookup);

        try {
            int counter = 0; // id of the generated constraint

            for (DatabaseRelationDescriptor uc : relation.getAllIDs().stream()
                    .flatMap(id -> uniqueConstraints.get(id).stream())
                    .collect(ImmutableCollectors.toList())) {
                String name = getTableName(relation) + "_USER_UC_" + counter++;
                UniqueConstraint.Builder builder = UniqueConstraint.builder(relation, name);
                for (QuotedID a : uc.attributeIds)
                    builder.addDeterminant(a);
                builder.build();
            }

            for (Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor> fkc : relation.getAllIDs().stream()
                    .flatMap(id -> foreignKeys.get(id).stream())
                    .collect(ImmutableCollectors.toList())) {
                NamedRelationDefinition referencedRelation;
                try {
                    referencedRelation = metadataLookup.getRelation(fkc.getValue().tableId);
                }
                catch (MetadataExtractionException e) {
                    LOGGER.warn("Cannot find table {} for user-supplied FK {} -> {}", fkc.getValue().tableId, fkc.getKey().toString(), fkc.getKey().toString());
                    continue;
                }
                String name = getTableName(relation) + "_USER_FK_" + getTableName(referencedRelation) + "_" + counter++;
                ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(name, relation, referencedRelation);
                for (int i = 0; i < fkc.getKey().attributeIds.size(); i++)
                    builder.add(fkc.getKey().attributeIds.get(i), fkc.getValue().attributeIds.get(i));
                builder.build();
            }
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException("Error in user-supplied unique constraints: " + e.getAttributeID() + " not found in " + e.getRelation());
        }
    }

    private static String getTableName(NamedRelationDefinition relation) {
        return relation.getID().getComponents().get(RelationID.TABLE_INDEX).getName();
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
            return idFactory.createRelationID(names);
        }

        @Override
        public String toString() { return tableId + "" + attributeIds; }
    }

}
