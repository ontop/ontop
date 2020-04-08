package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.*;

public class DynamicMetadataLookup implements MetadataLookup {

    private final RDBMetadataProvider provider;
    private final Map<RelationID, RelationDefinition> map = new HashMap<>();
    private final List<RelationDefinition> list = new ArrayList<>();

    public DynamicMetadataLookup(RDBMetadataProvider provider) {
        this.provider = provider;
    }

    @Override
    public Optional<RelationDefinition> getRelation(RelationID id) {
        //System.out.println("GET: " + id);
        RelationID canonicalId = provider.getRelationCanonicalID(id);
        RelationDefinition def = map.get(canonicalId);
        boolean quiet = id.toString().endsWith("_ASSERTION");

        if (def == null) {
            try {
                if (!quiet)
                    System.out.println("RETRIEVE: " + canonicalId);
                ImmutableList<RelationDefinition.AttributeListBuilder> builders = provider.getRelationAttributes(canonicalId);
                for (RelationDefinition.AttributeListBuilder builder : builders) {
                    RelationDefinition table = new DatabaseRelationDefinition(builder);
                    if (map.containsKey(table.getID())) {
                        if (!quiet)
                            System.out.println("OVERWRITTEN " + table.getID());
                        table = map.get(table.getID());
                    }
                    else
                        list.add(table);
                    if (!quiet)
                        System.out.println("ADD " + table.getID());
                    map.put(table.getID(), table);
                    if (def == null) // CATCH THE FIRST
                        def = table;
                    if (!id.hasSchema() && table.getID().hasSchema()) {
                        RelationID noSchemaID = table.getID().getSchemalessID();
                        if (!map.containsKey(noSchemaID)) {
                            map.put(noSchemaID, table);
                            if (!quiet)
                                System.out.println("ADD " + table.getID() + " AT " + noSchemaID);
                        }
                    }
                }
            }
            catch (MetadataExtractionException e) {
                System.out.println("METADATAEXTRACTION: " + e + " ON " + id);
                return Optional.empty();
            }
        }

        return Optional.ofNullable(def);
    }

    public List<RelationDefinition> getAllRelations() { return list; }
}
