package org.obda.SemanticIndex;

import junit.framework.TestCase;
import org.obda.owlrefplatform.core.abox.DAG;
import org.obda.owlrefplatform.core.abox.DAGNode;
import org.obda.owlrefplatform.core.abox.DAGSerializer;
import org.obda.owlrefplatform.core.abox.SemanticIndexRange;
import org.semanticweb.owl.model.OWLOntologyCreationException;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DAGSerializerTest extends TestCase {

    SemanticIndexHelper helper = new SemanticIndexHelper();

    public void test_create_schema() throws SQLException {
        DAGSerializer.recreate_tables(helper.conn);
        DatabaseMetaData md = helper.conn.getMetaData();
        ResultSet rs = md.getTables(null, null, DAGSerializer.index_table, null);
        assertEquals(true, rs.next());
    }

    private void load_and_test_dag(String ontoname)
            throws OWLOntologyCreationException, SQLException {

        DAG dag = helper.load_dag(ontoname);

        DAGSerializer.recreate_tables(helper.conn);
        DAGSerializer.DAG2DB(dag, helper.conn);

        ResultSet res_rows = helper.conn.createStatement().executeQuery(
                "SELECT * FROM " + DAGSerializer.index_table);
        Map<String, DAGNode> res_ranges = new HashMap<String, DAGNode>();
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
        DAG res_dag = new DAG(new ArrayList<DAGNode>(res_ranges.values()));
        DAG expected_dag = new DAG(helper.get_results(ontoname));
        assertEquals(expected_dag, res_dag);

    }

    public void test_1_0_0() throws OWLOntologyCreationException, SQLException {
        String onto_name = "test_1_0_0";
        load_and_test_dag(onto_name);
    }

    public void test_1_0_1() throws OWLOntologyCreationException, SQLException {
        String onto_name = "test_1_0_1";
        load_and_test_dag(onto_name);
    }

    public void test_1_1_0() throws OWLOntologyCreationException, SQLException {
        String onto_name = "test_1_1_0";
        load_and_test_dag(onto_name);
    }

    public void test_1_2_0() throws OWLOntologyCreationException, SQLException {
        String onto_name = "test_1_2_0";
        load_and_test_dag(onto_name);
    }

    public void test_1_3_0() throws OWLOntologyCreationException, SQLException {
        String onto_name = "test_1_3_0";
        load_and_test_dag(onto_name);
    }

    public void test_1_4_0() throws OWLOntologyCreationException, SQLException {
        String onto_name = "test_1_4_0";
        load_and_test_dag(onto_name);
    }

}
