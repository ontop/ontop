package it.unibz.krdb.obda.SemanticIndex;

import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAGSerializer;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexRange;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.semanticweb.owl.model.OWLOntologyCreationException;

public class DAGSerializerTest extends TestCase {

    SemanticIndexHelper helper = new SemanticIndexHelper();

    public void test_create_schema() throws SQLException {
        DAGSerializer.recreate_tables(helper.conn);
        DatabaseMetaData md = helper.conn.getMetaData();
        ResultSet rs = md.getTables(null, null, DAGSerializer.index_table, null);
        assertEquals(true, rs.next());
    }

    private void load_and_test_dag(String ontoname) throws OWLOntologyCreationException, SQLException {

        DAG dag = helper.load_dag(ontoname);

        DAGSerializer.recreate_tables(helper.conn);
        DAGSerializer.DAG2DB(dag, helper.conn);

        ResultSet res_rows = helper.conn.createStatement().executeQuery(
                "SELECT * FROM " + DAGSerializer.index_table);

        Map<String, DAGNode> res_classes = new HashMap<String, DAGNode>();
        Map<String, DAGNode> res_obj_props = new HashMap<String, DAGNode>();
        Map<String, DAGNode> res_data_props = new HashMap<String, DAGNode>();
        Map<String, DAGNode> tmp;

        while (res_rows.next()) {
            String uri = res_rows.getString(1);
            int idx = res_rows.getInt(2);
            int start_idx = res_rows.getInt(3);
            int end_idx = res_rows.getInt(4);
            int type = res_rows.getInt(5);

            if (type == DAGSerializer.CLASS_TYPE) {
                tmp = res_classes;
            } else if (type == DAGSerializer.OBJECTPROPERTY_TYPE) {
                tmp = res_obj_props;
            } else if (type == DAGSerializer.DATAPROPERTY_TYPE) {
                tmp = res_data_props;
            } else {
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
        DAG res_dag = new DAG(new ArrayList<DAGNode>(res_classes.values()),
                new ArrayList<DAGNode>(res_obj_props.values()),
                new ArrayList<DAGNode>(res_data_props.values()));

        List<List<DAGNode>> exp_indexes = helper.get_results(ontoname);

        DAG expected_dag = new DAG(exp_indexes.get(0), exp_indexes.get(1), exp_indexes.get(2));
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
