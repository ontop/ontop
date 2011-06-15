package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexRange.Interval;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store DAG in DB
 *
 * @author Sergejs Pugacs
 */
public final class DAGSerializer {

    private final static Logger log = LoggerFactory.getLogger(DAGSerializer.class);

    public final static String index_table = "IDX";

    private final static String create_ddl =
            "CREATE TABLE " + index_table + " ( "
                    + "URI VARCHAR(100), "
                    + "IDX INTEGER, "
                    + "IDX_FROM INTEGER, "
                    + "IDX_TO INTEGER, "
                    + "ENTITY_TYPE INTEGER" + ");";

    private final static String drop_dll = "DROP TABLE " + index_table
            + " IF EXISTS;";

    private final static String insert_query = "INSERT INTO " + index_table
            + "(URI, IDX, IDX_FROM, IDX_TO, ENTITY_TYPE) VALUES(?, ?, ?, ?, ?)";

    private final static String select_query = "SELECT * FROM " + index_table;

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final DescriptionFactory descFactory = new BasicDescriptionFactory();

    public static void recreate_tables(Connection conn) {
        try {
            log.debug("Recreating DAG tables");
            conn.createStatement().execute(drop_dll);
            conn.createStatement().execute(create_ddl);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public final static int CLASS_TYPE = 1;
    public final static int ROLE_TYPE = 2;

    /**
     * Save dag in the database
     *
     * @param dag  SemanticIndex for the TBox of the ontology
     * @param conn connection to a datastore where to store the DAG
     * @throws SQLException
     */
    public static void DAG2DB(DAG dag, Connection conn) throws SQLException {
        PreparedStatement stm = conn.prepareStatement(insert_query);
        for (DAGNode node : dag.getClasses()) {

            ConceptDescription description = (ConceptDescription) node.getDescription();

            String uri = description.toString();


            for (Interval it : node.getRange().getIntervals()) {
                stm.setString(1, uri);
                stm.setInt(2, node.getIndex());
                stm.setInt(3, it.getStart());
                stm.setInt(4, it.getEnd());
                stm.setInt(5, CLASS_TYPE);
                stm.addBatch();
            }
        }
        stm.executeBatch();

        for (DAGNode node : dag.getRoles()) {
            RoleDescription description = (RoleDescription) node.getDescription();
            String uri = description.toString();

            for (Interval it : node.getRange().getIntervals()) {
                stm.setString(1, uri);
                stm.setInt(2, node.getIndex());
                stm.setInt(3, it.getStart());
                stm.setInt(4, it.getEnd());
                stm.setInt(5, ROLE_TYPE);
                stm.addBatch();
            }
        }
        stm.executeBatch();
    }

    /**
     * Build the DAG from the DB data
     *
     * @param conn Connection to the DB where a DAG was serialized
     */
    public static DAG DB2DAG(Connection conn) {

        log.debug("Checking if SemanticIndex exists in DB");

        Map<Description, DAGNode> res_classes = new HashMap<Description, DAGNode>();
        Map<Description, DAGNode> res_roles = new HashMap<Description, DAGNode>();
        Map<String, DAGNode> tmp;
        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, index_table, null);
            if (rs.next()) {
                log.debug("SemanticIndex exists in the DB, trying to load it");
                ResultSet res_rows = conn.createStatement().executeQuery(select_query);
                while (res_rows.next()) {
                    String uri = res_rows.getString(1);
                    int idx = res_rows.getInt(2);
                    int start_idx = res_rows.getInt(3);
                    int end_idx = res_rows.getInt(4);
                    int type = res_rows.getInt(5);

                    Predicate p;
                    if (type == CLASS_TYPE) {

                        boolean exists = false;
                        boolean inverse = false;

                        // ExistentialNode
                        if (uri.startsWith("E")) {
                            exists = true;
                            uri = uri.substring(1);
                        }
                        // Inverse
                        if (uri.endsWith("-")) {
                            uri = uri.substring(0, uri.length() - 2);
                            inverse = true;
                        }
                        ConceptDescription description;

                        if (exists) {
                            p = predicateFactory.getPredicate(URI.create(uri), 2);
                            description = descFactory.getExistentialConceptDescription(p, inverse);
                        } else {
                            p = predicateFactory.getPredicate(URI.create(uri), 1);
                            description = descFactory.getAtomicConceptDescription(p);
                        }

                        if (res_classes.containsKey(description)) {
                            res_classes.get(description).getRange().addInterval(start_idx, end_idx);
                        } else {
                            DAGNode node = new DAGNode(description);
                            node.setIndex(idx);
                            node.setRange(new SemanticIndexRange(start_idx, end_idx));
                            res_classes.put(description, node);
                        }

                    } else if (type == ROLE_TYPE) {

                        RoleDescription description;
                        boolean inverse = false;

                        // Inverse
                        if (uri.endsWith("-")) {
                            uri = uri.substring(0, uri.length() - 2);
                            inverse = true;
                        }
                        p = predicateFactory.getPredicate(URI.create(uri), 2);
                        description = descFactory.getRoleDescription(p, inverse);

                        if (res_roles.containsKey(description)) {
                            res_roles.get(description).getRange().addInterval(start_idx, end_idx);
                        } else {
                            DAGNode node = new DAGNode(description);
                            node.setIndex(idx);
                            node.setRange(new SemanticIndexRange(start_idx, end_idx));
                            res_roles.put(description, node);
                        }
                    }
                }
            } else {
                log.error("SemanticIndex does not exist");
            }
        } catch (SQLException e) {
            // FIXME: add proper error handling
            e.printStackTrace();
        }
        return new DAG(res_classes, res_roles);
    }
}

