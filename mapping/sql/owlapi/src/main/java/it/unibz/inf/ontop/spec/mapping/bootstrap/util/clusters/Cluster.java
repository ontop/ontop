package it.unibz.inf.ontop.spec.mapping.bootstrap.util.clusters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Davide Lanti
 */
public class Cluster {

    private final String type;
    private final String tableSchema;
    private final String tableName;
    private final String clusteringAttribute;
    private final List<ClusteringMapEntry> clusteringMapEntries;

    // Empty dictionary
    public Cluster(String type, String tableSchema, String tableName, String clusteringAttribute){
        this.type = type;
        this.tableSchema = tableSchema;
        this.tableName = tableName;
        this.clusteringAttribute = clusteringAttribute;
        this.clusteringMapEntries = new ArrayList<>();
    }

    // Empty dictionary
    public Cluster(String type, String tableSchema, String tableName, String clusteringAttribute, List<ClusteringMapEntry> clusteringMapEntries){
        this.type = type;
        this.tableSchema = tableSchema;
        this.tableName = tableName;
        this.clusteringAttribute = clusteringAttribute;
        this.clusteringMapEntries = clusteringMapEntries;
    }

    public String getType() {
        return type;
    }

    public String getClusteringAttribute() {
        return clusteringAttribute;
    }

    public List<ClusteringMapEntry> getClusteringMapEntries() {
        return clusteringMapEntries;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public static class ClusteringMapEntry {
        private final String key;
        private final String value;

        public ClusteringMapEntry(String key, String value){
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString(){
            return String.format("ClusteringMapEntry{key='%s', value='%s'}", key, value);
        }
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "type='" + type + '\'' +
                ", tableSchema='" + tableSchema + '\'' +
                ", tableName='" + tableName + '\'' +
                ", clusteringAttribute='" + clusteringAttribute + '\'' +
                ", clusteringMapEntries=" + clusteringMapEntries +
                '}';
    }
}
