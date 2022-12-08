package it.unibz.inf.ontop.spec.mapping.bootstrap.util;

import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.clusters.Cluster;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.dictionary.Dictionary;

import java.util.*;

/**
 * @author  Davide Lanti
 */
public class BootConf {

  private final it.unibz.inf.ontop.spec.mapping.bootstrap.util.dictionary.Dictionary dictionary;
  private final JoinPairs joinPairs;
  private final boolean enableSH;
  private final NullValue nullValue;
  private final List<GenerateOnlyEntry> generateOnlyEntries;
  private final List<Cluster> clusters;
  private final String schema; // limit generation to a schema only

  public BootConf(Builder builder){ // Empty conf
    this.dictionary = builder.dictionary; // Emtpy dictionary
    this.joinPairs = builder.joinPairs;   // Empty joinPairs
    this.enableSH = builder.enableSH;
    this.nullValue = builder.nullValue;
    this.generateOnlyEntries = builder.generateOnlyEntries;
    this.clusters = builder.clusters;
    this.schema = builder.schema;
  }

  public boolean isEnableSH(){
    return this.enableSH;
  }

  public NullValue getNullValue() {
    return this.nullValue;
  }

  public Dictionary getDictionary() {
    return dictionary;
  }

  public String getSchema(){
    return schema;
  }

  public List<Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>>> getJoinPairs(MetadataProvider metadataProvider){
    return this.joinPairs.getQualifiedAttributePairs(metadataProvider.getQuotedIDFactory());
  }

  public Optional<Cluster> getClusterForTable(String schema, String tableName){
    for( Cluster c : getClusters() ){
      if( (schema == null || c.getTableSchema().equals(schema)) && c.getTableName().equals(tableName))
        return Optional.of(c);
    }
    return Optional.empty();
  }

  public List<Cluster> getClusters(){
    return this.clusters;
  }

  public static class NullValue {

    private final String stringNull;
    private final int intNull;
    private String dateNull;

    public NullValue(String stringNull, int intNull, String dateNull){
      this.stringNull = stringNull;
      this.intNull = intNull;
      this.dateNull = dateNull;
    }

    public String getStringNull() {
      return stringNull;
    }
    public int getIntNull() {
      return intNull;
    }

    @Override
    public String toString() {
      
      return "NullValue{" +
              "stringNull='" + stringNull + '\'' +
              ", intNull=" + intNull +
              ", dateNull=" + dateNull +
              '}';
    }

      public String getDateNull() {
        return this.dateNull;
      }
  }

  public static class Builder{

    // All optional parameters
    Dictionary dictionary = new Dictionary();
    JoinPairs joinPairs = new JoinPairs();
    boolean enableSH = false;
    NullValue nullValue;
    List<GenerateOnlyEntry> generateOnlyEntries = new LinkedList<>();
    List<Cluster> clusters = new LinkedList<>();
    String schema = "";

    public Builder dictionary(Dictionary dictionary){
      this.dictionary = dictionary;
      return this;
    }

    public Builder joinPairs(JoinPairs joinPairs) {
      this.joinPairs = joinPairs;
      return this;
    }

    public Builder nullValue(NullValue nullValue) {
      this.nullValue = nullValue;
      return this;
    }

    public Builder generateOnlyEntries(List<GenerateOnlyEntry> generateOnlyEntries) {
      // TODO
      this.generateOnlyEntries = generateOnlyEntries;
      return this;
    }

    public Builder clusters(List<Cluster> clusters) {
      this.clusters = clusters;
      return this;
    }

    public Builder enableSH(boolean enableSH) {
      this.enableSH = enableSH;
      return this;
    }

    public Builder schema(String schema) {
      this.schema = schema;
      return this;
    }

    public BootConf build(){
      return new BootConf(this);
    }
  }

  public static class GenerateOnlyEntry {
    private final String tableName;
    private final String schemaName;
    private final List<String> attributes;

    public GenerateOnlyEntry(String tableName, String schemaName, List<String> attributes) {
      this.tableName = tableName;
      this.schemaName = schemaName;
      this.attributes = attributes;
    }

    public String getTableName() {
      return tableName;
    }

    public String getSchemaName() {
      return schemaName;
    }

    public List<String> getAttributes() {
      return attributes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GenerateOnlyEntry that = (GenerateOnlyEntry) o;
      return tableName.equals(that.tableName) &&
              schemaName.equals(that.schemaName) &&
              attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
      return Objects.hash(tableName, schemaName, attributes);
    }

    @Override
    public String toString() {
      return "GenerateOnlyEntry{" +
              "tableName='" + tableName + '\'' +
              ", schemaName='" + schemaName + '\'' +
              ", attributes=" + attributes +
              '}';
    }
  }

  class NullValueException extends Exception {
    public NullValueException(String message) {
      super(message);
    }
  }
}