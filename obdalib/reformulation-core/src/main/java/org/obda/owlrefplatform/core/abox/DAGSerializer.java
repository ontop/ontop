package org.obda.owlrefplatform.core.abox;

import org.obda.owlrefplatform.core.abox.SemanticIndexRange.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store DAG in DB
 *
 * @author Sergejs Pugacs
 */
public final class DAGSerializer {

    private final static Logger log = LoggerFactory
            .getLogger(DAGSerializer.class);

    public final static String index_table = "IDX";

    private final static String create_ddl = "CREATE TABLE " + index_table
            + " ( " + "URI VARCHAR(100), " + "IDX_FROM INTEGER, "
            + "IDX_TO INTEGER " + ");";

    private final static String drop_dll = "DROP TABLE " + index_table
            + " IF EXISTS;";

    private final static String insert_query = "INSERT INTO " + index_table
            + "(URI, IDX_FROM, IDX_TO) VALUES(?, ?, ?)";
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

    /**
     * Save dag in the database
     *
     * @param dag SemanticIndex for the TBox of the ontology
     * @param conn connection to a datastore where to store the DAG
     * @throws SQLException
     */
    public static void DAG2DB(DAG dag, Connection conn) throws SQLException {
        Map<String, DAGNode> index = dag.getIndex();
        PreparedStatement stm = conn.prepareStatement(insert_query);
        for (String uri : index.keySet()) {
            SemanticIndexRange range = index.get(uri).getRange();
            for (Interval it : range.getIntervals()) {
                stm.setString(1, uri);
                stm.setInt(2, it.getStart());
                stm.setInt(3, it.getEnd());
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

        Map<String, DAGNode> res_ranges = new HashMap<String, DAGNode>();
        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, index_table, null);
            if (rs.next()) {
                log.debug("SemanticIndex exists in the DB, trying to load it");
                ResultSet res_rows = conn.createStatement().executeQuery(select_query);
                while (res_rows.next()) {
                    String uri = res_rows.getString(1);
                    int start_idx = res_rows.getInt(2);
                    int end_idx = res_rows.getInt(3);
                    if (res_ranges.containsKey(uri)) {
                        res_ranges.get(uri).getRange().addInterval(start_idx, end_idx);
                    } else {
                        DAGNode node = new DAGNode(uri);
                        node.setRange(new SemanticIndexRange(start_idx, end_idx));
                        res_ranges.put(uri, node);
                    }
                }
            } else {
                log.error("SemanticIndex does not exist");
            }
        } catch (SQLException e) {
            // FIXME: add proper error handling
            e.printStackTrace();
        }
        return new DAG( (List) res_ranges.values());
    }
}

