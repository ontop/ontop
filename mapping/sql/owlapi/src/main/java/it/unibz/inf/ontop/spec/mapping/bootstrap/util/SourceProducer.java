package it.unibz.inf.ontop.spec.mapping.bootstrap.util;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.spec.mapping.bootstrap.util.DirectMappingAxiomProducer.*;

public class SourceProducer {

    public static String getSQL(NamedRelationDefinition table, BootConf.NullValue nullValue){
        if( nullValue != null){
            if( !table.getPrimaryKey().isPresent() ){
                // Table without a pkey
                // We rewrite null-values
                // Explictly select all columns
                String columns = table.getAttributes().stream()
                        .map(a -> getCoalesceString(a, nullValue, a.getID().getName())
                        ).collect(Collectors.joining(", "));

                return String.format("SELECT %s FROM %s", columns, table.getID().getSQLRendering());
            }
        }
        return DirectMappingAxiomProducer.getSQL(table);
    }

    public String getClusteringSQL(String clusteringMapEntryKey, String clusteringAttribute, NamedRelationDefinition table, BootConf.NullValue nullValue) {
        if( nullValue != null){
            if( !table.getPrimaryKey().isPresent() ){
                // Table without a pkey
                // We rewrite null-values
                // Explictly select all columns
                String columns = table.getAttributes().stream()
                        .map(a -> getCoalesceString(a, nullValue, a.getID().getName())
                        ).collect(Collectors.joining(", "));
                return String.format("SELECT %s FROM %s WHERE %s = '%s'", columns, table.getID().getSQLRendering(), clusteringAttribute, clusteringMapEntryKey);
            }
        }
        return String.format("SELECT * FROM %s WHERE %s = '%s'", table.getID().getSQLRendering(), clusteringAttribute, clusteringMapEntryKey);
    }

    private static String getCoalesceString(Attribute a, BootConf.NullValue nullValue, String attributeString){
        String result;
        if( a.isNullable() ){
            if( isNumericAttribute(a) ){
                result = "COALESCE(" + getQualifiedColumnName(a) + ", " + nullValue.getIntNull() + ") AS " + attributeString + "_coal" + ", " + "(" + getQualifiedColumnName(a) + ") AS " + attributeString;
            }
            else if ( isDateAttribute(a) ){
                result = "COALESCE(" + getQualifiedColumnName(a) + ", '" + nullValue.getDateNull() + "') AS " + attributeString + "_coal" + ", " + "(" + getQualifiedColumnName(a) + ") AS " + attributeString;
            }
            else{ // String null value
                result = "COALESCE(" + getQualifiedColumnName(a) + ", '" + nullValue.getStringNull() + "') AS " + attributeString + "_coal" + ", " + "(" + getQualifiedColumnName(a) + ") AS " + attributeString;
            }
        }
        else {
            result = "(" + getQualifiedColumnName(a) + ") AS " + attributeString;
        }
        return result;
    }

    private static boolean isDateAttribute(Attribute a) {
        return a.getTermType().getCategory().equals(DBTermType.Category.DATETIME);
    }

    private static boolean isNumericAttribute(Attribute a){
        return a.getTermType().getCategory().equals(DBTermType.Category.INTEGER)
                || a.getTermType().getCategory().equals(DBTermType.Category.DECIMAL)
                || a.getTermType().getCategory().equals(DBTermType.Category.FLOAT_DOUBLE);
    }

    /***
     * Definition reference triple: an RDF triple with:
     * <p/>
     * subject: the row node for the row.
     * predicate: the reference property IRI for the columns.
     * object: the row node for the referenced row.
     *
     * @param fk
     * @return
     */
    public String getRefSQL(ForeignKeyConstraint fk, BootConf conf) {

        String columns = Stream.concat(
                        getIdentifyingAttributes(fk.getRelation()).stream(),
                        getIdentifyingAttributes(fk.getReferencedRelation()).stream())
                .map(a -> a.getRelation().getUniqueConstraints().isEmpty() && conf.getNullValue()!= null
                        ? getCoalesceString(a, conf.getNullValue(), getColumnAlias(a))
                        : getQualifiedColumnName(a) + " AS " + getColumnAlias(a))
                .collect(Collectors.joining(", "));

        String tables = fk.getRelation().getID().getSQLRendering() +
                ", " + fk.getReferencedRelation().getID().getSQLRendering();

        String conditions = fk.getComponents().stream()
                .map(c -> getQualifiedColumnName(c.getAttribute()) + " = " + getQualifiedColumnName(c.getReferencedAttribute()))
                .collect(Collectors.joining(" AND "));

        return String.format("SELECT %s FROM %s WHERE %s", columns, tables, conditions);
    }

    public String getRefSQL(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> pair, ImmutableList<NamedRelationDefinition> tableDefs, BootConf conf) {

        NamedRelationDefinition table1 = retrieveDatabaseTableDefinition(tableDefs, pair.first().get(0).getRelation());
        NamedRelationDefinition table2 = retrieveDatabaseTableDefinition(tableDefs, pair.second().get(0).getRelation());

        String columns = Stream.concat(
                        getIdentifyingAttributes(table1).stream(),
                        getIdentifyingAttributes(table2).stream())
                .map(a -> a.getRelation().getUniqueConstraints().isEmpty() && conf.getNullValue() != null
                        ? getCoalesceString(a, conf.getNullValue(), getColumnAlias(a))
                        : getQualifiedColumnName(a) + " AS " + getColumnAlias(a))
                .collect(Collectors.joining(", "));

        String tables = table1.getID().getSQLRendering() +
                ", " + table2.getID().getSQLRendering();

        // Multi-attribute case
        StringBuilder conditionsBuilder = new StringBuilder();
        for( int i = 0; i < pair.first().size() ; ++i ){
            if(conditionsBuilder.length() > 0){
                conditionsBuilder.append(" AND ");
            }
            conditionsBuilder.append(pair.first().get(i).getSQLRendering() + " = " + pair.second().get(i).getSQLRendering());
        }
        String conditions = conditionsBuilder.toString();

        return String.format("SELECT %s FROM %s WHERE %s", columns, tables, conditions);

    }

    public static NamedRelationDefinition retrieveDatabaseTableDefinition(ImmutableList<NamedRelationDefinition> tables, RelationID relationID) {
        return tables.stream().filter(t -> t.getID().equals(relationID)).findFirst().get();
    }

    // A \subclassOf B
    public static List<NamedRelationDefinition> retrieveParentTables(NamedRelationDefinition table) {
        List<NamedRelationDefinition> parentTables = new LinkedList<>();
        for( ForeignKeyConstraint fkey : table.getForeignKeys() ){
            if( checkWhetherSubclassFkey(fkey) ){
                parentTables.add(fkey.getReferencedRelation());
            }
        }
        return parentTables;
    }

    // Davide>
    // Retrieve the ancestor of the subclassOf hierarchy. If there is a cycle, and no definite ancestor, then
    // the ancestor is to be decided upon lexicographic order.
    // In case of multiple-inheritance, I assign the same template to all connected tables.
    // The algorithm follows a bfs strategy
    public static NamedRelationDefinition retrieveAncestorTable(NamedRelationDefinition source) {

        List<List<NamedRelationDefinition>> frontier = new LinkedList<>();
        List<List<NamedRelationDefinition>> foundCycles = new LinkedList<>(); // TODO> Remove
        List<NamedRelationDefinition> possibleAncestors = new LinkedList<>();

        List<NamedRelationDefinition> initialPath = new ArrayList<>();
        initialPath.add(source);
        frontier.add(initialPath);
        while( !frontier.isEmpty() ){
            List<NamedRelationDefinition> path = frontier.remove(0);
            NamedRelationDefinition last = path.get(path.size()-1);
            List<NamedRelationDefinition> neighbors =
                    last.getForeignKeys().stream()
                            .filter(fkey -> checkWhetherSubclassFkey(fkey))
                            .map(fkey -> fkey.getReferencedRelation())
                            .collect(Collectors.toList());
            if(neighbors.size() == 0){
                // No parents for the current node, so the current node is a possible ancestor
                possibleAncestors.add(last);
            }
            for( NamedRelationDefinition neighbor : neighbors ){
                if( !path.contains(neighbor) ) {
                    List<NamedRelationDefinition> newPath = new LinkedList<>();
                    newPath.addAll(path);
                    newPath.add(neighbor);
                    frontier.add(newPath);
                }
                else {
                    path.add(neighbor);
                    foundCycles.add(path); // Things in the cycle will require the same template.
                }
            }
        }
        // Frontier is empty, determining the ancestor according to a lexicographic order
        possibleAncestors.sort(new Comparator<NamedRelationDefinition>() {
            @Override
            public int compare(NamedRelationDefinition NamedRelationDefinition, NamedRelationDefinition t1) {
                return NamedRelationDefinition.getID().getComponents().get(RelationID.TABLE_INDEX).getSQLRendering().toLowerCase()
                        .compareTo(t1.getID().getComponents().get(RelationID.TABLE_INDEX).getSQLRendering().toLowerCase());
            }
        });

        assert( possibleAncestors.size() != 0 );

        return possibleAncestors.get(0);
    }

    // check whether the fkey is of the kind (pkey -> pkey)
    public static boolean checkWhetherSubclassFkey(ForeignKeyConstraint fkey) {
        Optional<UniqueConstraint> referredTablePkey = fkey.getReferencedRelation().getPrimaryKey();
        Optional<UniqueConstraint> referringTablePkey = fkey.getRelation().getPrimaryKey();

        if( !(referredTablePkey.isPresent() && referringTablePkey.isPresent()) ) return false;

        ImmutableList<Attribute> referringTablePkeyAtts = referringTablePkey.get().getAttributes();
        ImmutableList<Attribute> referredTablePkeyAtts = referredTablePkey.get().getAttributes();

        List<Attribute> fkeyReferringAtts = fkey.getComponents().stream().map(component -> component.getAttribute()).collect(Collectors.toList());
        List<Attribute> fkeyReferredAtts = fkey.getComponents().stream().map(component -> component.getReferencedAttribute()).collect(Collectors.toList());

        boolean containmentCheck = referringTablePkeyAtts.containsAll(fkeyReferringAtts)
                && fkeyReferringAtts.containsAll(referringTablePkeyAtts);

        return containmentCheck && referredTablePkeyAtts.equals(fkeyReferredAtts);
    }
}
