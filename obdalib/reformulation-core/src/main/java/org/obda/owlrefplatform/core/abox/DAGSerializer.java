package org.obda.owlrefplatform.core.abox;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obda.owlrefplatform.core.abox.SemanticIndexRange.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store DAG in DB
 *
 * @author Sergejs Pugacs
 */
public final class DAGSerializer {

    private final static Logger log = LoggerFactory
            .getLogger(DAGSerializer.class);

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
    public final static int OBJECTPROPERTY_TYPE = 2;
    public final static int DATAPROPERTY_TYPE = 3;

    /**
     * Save dag in the database
     *
     * @param dag  SemanticIndex for the TBox of the ontology
     * @param conn connection to a datastore where to store the DAG
     * @throws SQLException
     */
    public static void DAG2DB(DAG dag, Connection conn) throws SQLException {
        Map<String, DAGNode> index = dag.getClassIndex();
        PreparedStatement stm = conn.prepareStatement(insert_query);
        for (DAGNode node : index.values()) {
            String uri = node.getUri();

            for (Interval it : node.getRange().getIntervals()) {
                stm.setString(1, uri);
                stm.setInt(2, node.getIndex());
                stm.setInt(2, it.getStart());
                stm.setInt(3, it.getEnd());
                stm.setInt(4, CLASS_TYPE);
                stm.addBatch();
            }
        }
        stm.executeBatch();

        index = dag.getObjectPropertyIndex();
        for (DAGNode node : index.values()) {
            String uri = node.getUri();

            for (Interval it : node.getRange().getIntervals()) {
                stm.setString(1, uri);
                stm.setInt(2, node.getIndex());
                stm.setInt(2, it.getStart());
                stm.setInt(3, it.getEnd());
                stm.setInt(4, OBJECTPROPERTY_TYPE);
                stm.addBatch();
            }
        }
        stm.executeBatch();

        index = dag.getDataPropertyIndex();
        for (DAGNode node : index.values()) {
            String uri = node.getUri();

            for (Interval it : node.getRange().getIntervals()) {
                stm.setString(1, uri);
                stm.setInt(2, node.getIndex());
                stm.setInt(2, it.getStart());
                stm.setInt(3, it.getEnd());
                stm.setInt(4, DATAPROPERTY_TYPE);
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

        Map<String, DAGNode> res_classes = new HashMap<String, DAGNode>();
        Map<String, DAGNode> res_obj_props = new HashMap<String, DAGNode>();
        Map<String, DAGNode> res_data_props = new HashMap<String, DAGNode>();
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

                    if (type == CLASS_TYPE) {
                        tmp = res_classes;
                    } else if (type == OBJECTPROPERTY_TYPE) {
                        tmp = res_obj_props;
                    } else if (type == DATAPROPERTY_TYPE) {
                        tmp = res_data_props;
                    } else {
                        log.error("Unsupported DAG element type: " + type);
                        tmp = null;
                    }

                    if (tmp.containsKey(uri)) {
                        tmp.get(uri).getRange().addInterval(start_idx, end_idx);
                    } else {
                        DAGNode node = new DAGNode(uri);
                        node.setIndex(idx);
                        node.setRange(new SemanticIndexRange(start_idx, end_idx));
                        tmp.put(uri, node);
                    }
                }
            } else {
                log.error("SemanticIndex does not exist");
            }
        } catch (SQLException e) {
            // FIXME: add proper error handling
            e.printStackTrace();
        }
        return new DAG((List) res_classes.values(), (List) res_obj_props.values(), (List) res_data_props.values());
    }
}

