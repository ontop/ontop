package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.io.IOException;

public class RelationIDImpl implements RelationID {
    private final QuotedID schema, table;

    /**
     * (used only in QuotedIDFactory implementations)
     *
     * @param schema
     * @param table
     */

    public RelationIDImpl(QuotedID schema, QuotedID table) {
        this.schema = schema;
        this.table = table;
    }

    /**
     * creates relation id from the database record (as though it is quoted)
     *
     * @param schema as is in DB (possibly null)
     * @param table as is in DB
     * @return
     */

    public static RelationID createRelationIdFromDatabaseRecord(QuotedIDFactory idfac, String schema, String table) {
        // both IDs are as though they are quoted -- DB stores names as is
        return new RelationIDImpl(QuotedIDImpl.createIdFromDatabaseRecord(idfac, schema),
                QuotedIDImpl.createIdFromDatabaseRecord(idfac, table));
    }

    /**
     *
     * @return the relation ID that has the same name but no schema name
     */
    @JsonIgnore
    @Override
    public RelationID getSchemalessID() {
        return new RelationIDImpl(QuotedIDImpl.EMPTY_ID, table);
    }

    /**
     *
     * @return true if the relation ID contains schema
     */
    @Override
    public boolean hasSchema() {
        return schema.getName() != null;
    }

    /**
     *
     * @return null if the schema name is empty or SQL rendering of the schema name (possibly in quotation marks)
     */
    @JsonIgnore
    @Override
    public String getSchemaSQLRendering() {
        return schema.getSQLRendering();
    }

    /**
     *
     * @return SQL rendering of the table name (possibly in quotation marks)
     */
    @JsonIgnore
    @Override
    public String getTableNameSQLRendering() {
        return table.getSQLRendering();
    }

    /**
     *
     * @return null if the schema name is empty or the schema name (as is, without quotation marks)
     */
    @JsonProperty("schema")
    @Override
    public String getSchemaName() {
        return schema.getName();
    }

    /**
     *
     * @return table name (as is, without quotation marks)
     */

    @JsonProperty("name")
    @Override
    public String getTableName() {
        return table.getName();
    }

    /**
     *
     * @return SQL rendering of the name (possibly with quotation marks)
     */
    @Override
    public String getSQLRendering() {
        String s = schema.getSQLRendering();
        if (s == null)
            return table.getSQLRendering();

        return s + "." + table.getSQLRendering();
    }

    @Override
    public String toString() {
        return getSQLRendering();
    }

    @Override
    public int hashCode() {
        return table.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof RelationIDImpl) {
            RelationIDImpl other = (RelationIDImpl)obj;
            return (this.schema.equals(other.schema) && this.table.equals(other.table));
        }

        return false;
    }

    public static class RelationIDSerializer extends JsonSerializer<RelationID> {

        @Override
        public void serialize(RelationID value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getSQLRendering());
        }
    }

}
