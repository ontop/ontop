package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.*;

public class DynamicMetadataLookup implements MetadataLookup {

    private final MetadataProvider provider;
    private final Map<RelationID, RelationDefinition> map = new HashMap<>();
    private final List<RelationDefinition> list = new ArrayList<>();

    public DynamicMetadataLookup(MetadataProvider provider) {
        this.provider = provider;
    }

    @Override
    public Optional<RelationDefinition> getRelation(RelationID id) {
        //System.out.println("GET: " + id);
        RelationDefinition def = map.get(id);

        if (def == null) {
            try {
                System.out.println("RETRIEVE: " + id);
                ImmutableList<RelationDefinition.AttributeListBuilder> builders = provider.getRelationAttributes(id);
                for (RelationDefinition.AttributeListBuilder builder : builders) {
                    RelationDefinition table = new DatabaseRelationDefinition(builder);
                    if (map.containsKey(table.getID())) {
                        System.out.println("OVERWRITTEN " + table.getID());
                        table = map.get(table.getID());
                    }
                    else
                        list.add(table);
                    System.out.println("ADD " + table.getID());
                    map.put(table.getID(), table);
                    if (def == null) // CATCH THE FIRST
                        def = table;
                    if (!id.hasSchema() && table.getID().hasSchema()) {
                        RelationID noSchemaID = table.getID().getSchemalessID();
                        if (!map.containsKey(noSchemaID)) {
                            map.put(noSchemaID, table);
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
