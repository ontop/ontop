package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DelegatingMetadataProvider;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.List;
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

    private final ImmutableMultimap<RelationID, QuotedID> notNullConstraints;
    private final ImmutableMultimap<RelationID, DatabaseRelationDescriptor> uniqueConstraints;
    private final ImmutableMultimap<RelationID, DatabaseRelationDescriptor> primaryKeyConstraints;
    private final ImmutableMultimap<RelationID, Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor>> foreignKeys;

    ImplicitDBConstraintsProvider(MetadataProvider provider,
                                  ImmutableList<DatabaseRelationDescriptor> notNullConstraints,
                                  ImmutableList<DatabaseRelationDescriptor> uniqueConstraints,
                                  ImmutableList<DatabaseRelationDescriptor> primaryKeyConstraints,
                                  ImmutableList<Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor>> foreignKeys) {
        super(provider);

        this.notNullConstraints = Stream
                .concat(notNullConstraints.stream(), primaryKeyConstraints.stream())
                .flatMap(c -> c.attributeIds.stream().map(id -> new AbstractMap.SimpleEntry<>(c.tableId, id)))
                .collect(ImmutableCollectors.toMultimap(Map.Entry::getKey, Map.Entry::getValue));

        this.uniqueConstraints = uniqueConstraints.stream()
                .collect(ImmutableCollectors.toMultimap(c -> c.tableId, Function.identity()));

        this.primaryKeyConstraints = primaryKeyConstraints.stream()
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

            List<QuotedID> nns = getConstraints(relation, notNullConstraints);
            List<DatabaseRelationDescriptor> pks = getConstraints(relation, primaryKeyConstraints);
            List<DatabaseRelationDescriptor> ucs = getConstraints(relation, uniqueConstraints);
            List<Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor>> fks = getConstraints(relation, foreignKeys);

            if (!nns.isEmpty()) {
                Attribute[] notNullAttributes = new Attribute[nns.size()];
                for (int i = 0; i < nns.size(); ++ i) {
                    notNullAttributes[i] = relation.getAttribute(nns.get(i));
                }
                relation.addNotNullConstraint(notNullAttributes);
            }

            if (!pks.isEmpty() && relation.getPrimaryKey().isEmpty()) {
                DatabaseRelationDescriptor pk = pks.remove(pks.size() - 1); // choose last PK, others will end up as UCs
                String name = getTableName(relation) + "_USER_PK_" + counter++;
                UniqueConstraint.Builder builder = UniqueConstraint.primaryKeyBuilder(relation, name);
                for (QuotedID a : pk.attributeIds)
                    builder.addDeterminant(a);
                builder.build();
            }

            for (DatabaseRelationDescriptor uc : Iterables.concat(pks, ucs)) {
                String name = getTableName(relation) + "_USER_UC_" + counter++;
                UniqueConstraint.Builder builder = UniqueConstraint.builder(relation, name);
                for (QuotedID a : uc.attributeIds)
                    builder.addDeterminant(a);
                builder.build();
            }

            for (Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor> fkc : fks) {
                NamedRelationDefinition referencedRelation;
                try {
                    referencedRelation = metadataLookup.getRelation(fkc.getValue().tableId);
                }
                catch (MetadataExtractionException e) {
                    LOGGER.warn("Cannot find table {} for user-supplied FK {} -> {}", fkc.getValue().tableId, fkc.getKey(), fkc.getKey());
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

    private static <T> List<T> getConstraints(NamedRelationDefinition relation, ImmutableMultimap<RelationID, T> constraints) {
        return relation.getAllIDs().stream()
                .flatMap(id -> constraints.get(id).stream())
                .collect(Collectors.toList());
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
