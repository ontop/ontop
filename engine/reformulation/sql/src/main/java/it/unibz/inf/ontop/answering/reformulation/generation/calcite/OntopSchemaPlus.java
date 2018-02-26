package it.unibz.inf.ontop.answering.reformulation.generation.calcite;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.materialize.Lattice;
import org.apache.calcite.schema.*;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class OntopSchemaPlus implements SchemaPlus {

    private final DBMetadata dbMetadata;

    public OntopSchemaPlus(DBMetadata dbMetadata) {
        this.dbMetadata = dbMetadata;
    }

    @Override
    public SchemaPlus getParentSchema() {
        return null;
    }

    @Override
    public String getName() {
        return "ontop";
    }

    @Override
    public Table getTable(String name) {
        return null;
    }

    @Override
    public Set<String> getTableNames() {
        return dbMetadata.getDatabaseRelations().stream()
                .map(r -> r.getID().getSchemalessID().toString())
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Function> getFunctions(String name) {
        return null;
    }

    @Override
    public Set<String> getFunctionNames() {
        return null;
    }

    @Override
    public SchemaPlus getSubSchema(String name) {
        return null;
    }

    @Override
    public Set<String> getSubSchemaNames() {
        return null;
    }

    @Override
    public Expression getExpression(SchemaPlus parentSchema, String name) {
        return null;
    }

    @Override
    public SchemaPlus add(String name, Schema schema) {
        return null;
    }

    @Override
    public void add(String name, Table table) {

    }

    @Override
    public void add(String name, Function function) {

    }

    @Override
    public void add(String name, Lattice lattice) {

    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Schema snapshot(SchemaVersion version) {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return null;
    }

    @Override
    public void setPath(ImmutableList<ImmutableList<String>> path) {

    }

    @Override
    public void setCacheEnabled(boolean cache) {

    }

    @Override
    public boolean isCacheEnabled() {
        return false;
    }
}
