package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.impl.AbstractDBMetadata;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class BasicDBMetadata extends AbstractDBMetadata implements DBMetadata {

    private final Map<RelationID, DatabaseRelationDefinition> tables;

    // relations include tables and views (views are only created for complex queries in mappings)
    protected final Map<RelationID, RelationDefinition> relations;
    private final List<DatabaseRelationDefinition> listOfTables;

    private final String driverName;
    private final String driverVersion;
    private final String databaseProductName;
    private final String databaseVersion;
    private final QuotedIDFactory idfac;
    private boolean isStillMutable;
    @Nullable
    private ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> uniqueConstraints;

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicDBMetadata.class);
    private final AtomFactory atomFactory;

    protected BasicDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
                              QuotedIDFactory idfac, AtomFactory atomFactory, Relation2Predicate relation2Predicate,
                              TermFactory termFactory, DatalogFactory datalogFactory) {
        this(driverName, driverVersion, databaseProductName, databaseVersion, idfac, new HashMap<>(), new HashMap<>(),
                new LinkedList<>(), relation2Predicate, atomFactory, termFactory, datalogFactory);
    }

    protected BasicDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
                              QuotedIDFactory idfac, Map<RelationID, DatabaseRelationDefinition> tables,
                              Map<RelationID, RelationDefinition> relations, List<DatabaseRelationDefinition> listOfTables,
                              Relation2Predicate relation2Predicate, AtomFactory atomFactory,
                              TermFactory termFactory, DatalogFactory datalogFactory) {
        super(relation2Predicate, termFactory, datalogFactory);
        this.driverName = driverName;
        this.driverVersion = driverVersion;
        this.databaseProductName = databaseProductName;
        this.databaseVersion = databaseVersion;
        this.idfac = idfac;
        this.tables = tables;
        this.relations = relations;
        this.listOfTables = listOfTables;
        this.atomFactory = atomFactory;
        this.isStillMutable = true;
        this.uniqueConstraints = null;
    }

    /**
     * creates a database table (which can also be a database view)
     * if the <name>id</name> contains schema than the relation is added
     * to the lookup table (see getDatabaseRelation and getRelation) with
     * both the fully qualified id and the table name only id
     *
     * @param id
     * @return
     */

    public DatabaseRelationDefinition createDatabaseRelation(RelationID id) {
        if (!isStillMutable) {
            throw new IllegalStateException("Too late, cannot create a DB relation");
        }
        DatabaseRelationDefinition table = new DatabaseRelationDefinition(id);
        add(table, tables);
        add(table, relations);
        listOfTables.add(table);
        return table;
    }

    /**
     * Inserts a new data definition to this metadata object.
     *
     * @param td
     *            The data definition. It can be a {@link DatabaseRelationDefinition} or a
     *            {@link ParserViewDefinition} object.
     */
    protected <T extends RelationDefinition> void add(T td, Map<RelationID, T> schema) {
        if (!isStillMutable) {
            throw new IllegalStateException("Too late, cannot add a schema");
        }
        schema.put(td.getID(), td);
        if (td.getID().hasSchema()) {
            RelationID noSchemaID = td.getID().getSchemalessID();
            if (!schema.containsKey(noSchemaID)) {
                schema.put(noSchemaID, td);
            }
            else {
                LOGGER.warn("DUPLICATE TABLE NAMES, USE QUALIFIED NAMES:\n" + td + "\nAND\n" + schema.get(noSchemaID));
                //schema.remove(noSchemaID);
                // TODO (ROMAN 8 Oct 2015): think of a better way of resolving ambiguities
            }
        }
    }

    @Override
    public DatabaseRelationDefinition getDatabaseRelation(RelationID id) {
        DatabaseRelationDefinition def = tables.get(id);
        if (def == null && id.hasSchema()) {
            def = tables.get(id.getSchemalessID());
        }
        return def;
    }

    @Override
    public RelationDefinition getRelation(RelationID name) {
        RelationDefinition def = relations.get(name);
        if (def == null && name.hasSchema()) {
            def = relations.get(name.getSchemalessID());
        }
        return def;
    }

    @Override
    public Collection<DatabaseRelationDefinition> getDatabaseRelations() {
        return Collections.unmodifiableCollection(listOfTables);
    }

    @Override
    public Optional<TermType> getTermType(Attribute attribute) {
        throw new RuntimeException("This method should not be called");
    }

    @Override
    public void freeze() {
        isStillMutable = false;
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    public String getDriverVersion() {
        return driverVersion;
    }

    @Override
    public String printKeys() {
        StringBuilder builder = new StringBuilder();
        Collection<DatabaseRelationDefinition> table_list = getDatabaseRelations();
        // Prints all primary keys
        builder.append("\n====== Unique constraints ==========\n");
        for (DatabaseRelationDefinition dd : table_list) {
            builder.append(dd + ";\n");
            for (UniqueConstraint uc : dd.getUniqueConstraints())
                builder.append(uc + ";\n");
            builder.append("\n");
        }
        // Prints all foreign keys
        builder.append("====== Foreign key constraints ==========\n");
        for(DatabaseRelationDefinition dd : table_list) {
            for (ForeignKeyConstraint fk : dd.getForeignKeys())
                builder.append(fk + ";\n");
        }
        return builder.toString();
    }

    @Override
    public ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getUniqueConstraints() {
        if (uniqueConstraints == null) {
            ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> constraints = extractUniqueConstraints();
            if (!isStillMutable)
                uniqueConstraints = constraints;
            return constraints;
        }
        else
            return uniqueConstraints;

    }

    private ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> extractUniqueConstraints() {
        Map<Predicate, AtomPredicate> predicateCache = new HashMap<>();

        return getDatabaseRelations().stream()
                .flatMap(relation -> extractUniqueConstraintsFromRelation(relation, predicateCache))
                .collect(ImmutableCollectors.toMultimap());
    }

    private Stream<Map.Entry<AtomPredicate, ImmutableList<Integer>>> extractUniqueConstraintsFromRelation(
            DatabaseRelationDefinition relation, Map<Predicate, AtomPredicate> predicateCache) {

        Predicate originalPredicate = getRelation2Predicate().createPredicateFromRelation(relation);
        AtomPredicate atomPredicate = convertToAtomPredicate(originalPredicate, predicateCache);

        return relation.getUniqueConstraints().stream()
                .map(uc -> uc.getAttributes().stream()
                        .map(Attribute::getIndex)
                        .collect(ImmutableCollectors.toList()))
                .map(positions -> new AbstractMap.SimpleEntry<>(atomPredicate, positions));
    }


    @Override
    protected AtomPredicate convertToAtomPredicate(Predicate originalPredicate,
                                                   Map<Predicate, AtomPredicate> predicateCache) {
        if (originalPredicate instanceof AtomPredicate) {
            return (AtomPredicate) originalPredicate;
        }
        else if (predicateCache.containsKey(originalPredicate)) {
            return predicateCache.get(originalPredicate);
        }
        else {
            AtomPredicate atomPredicate = atomFactory.getAtomPredicate(originalPredicate);
            // Cache it
            predicateCache.put(originalPredicate, atomPredicate);
            return atomPredicate;
        }
    }

    @Override
    public String getDbmsProductName() {
        return databaseProductName;
    }

    public String getDbmsVersion() {
        return databaseVersion;
    }


    public QuotedIDFactory getQuotedIDFactory() {
        return idfac;
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        for (RelationID key : relations.keySet()) {
            bf.append(key);
            bf.append("=");
            bf.append(relations.get(key).toString());
            bf.append("\n");
        }
        return bf.toString();
    }

    protected Map<RelationID, DatabaseRelationDefinition> getTables() {
        return tables;
    }

    @Deprecated
    @Override
    public BasicDBMetadata clone() {
        return new BasicDBMetadata(driverName, driverVersion, databaseProductName, databaseVersion, idfac,
                new HashMap<>(tables), new HashMap<>(relations), new LinkedList<>(listOfTables), getRelation2Predicate(),
                atomFactory, getTermFactory(), getDatalogFactory());
    }

    protected boolean isStillMutable() {
        return isStillMutable;
    }

    protected AtomFactory getAtomFactory() {
        return atomFactory;
    }

}
