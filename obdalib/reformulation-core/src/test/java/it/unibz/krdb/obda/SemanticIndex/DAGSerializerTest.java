package it.unibz.krdb.obda.SemanticIndex;

import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAGSerializer;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

public class DAGSerializerTest extends TestCase {

    SemanticIndexHelper helper = new SemanticIndexHelper();

    public void test_create_schema() throws SQLException {
        DAGSerializer.recreate_tables(helper.conn);
        DatabaseMetaData md = helper.conn.getMetaData();
        ResultSet rs = md.getTables(null, null, DAGSerializer.index_table, null);
        assertEquals(true, rs.next());
    }

    private void load_and_test_dag(String ontoname) throws Exception {

        DAG dag = helper.load_dag(ontoname);

        DAGSerializer.recreate_tables(helper.conn);
        DAGSerializer.DAG2DB(dag, helper.conn);
        DAG res = DAGSerializer.DB2DAG(helper.conn);

        assertEquals(dag, res);

    }

    public void test_1_0_0() throws Exception {
        String onto_name = "test_1_0_0";
        load_and_test_dag(onto_name);
    }

    public void test_1_0_1() throws Exception {
        String onto_name = "test_1_0_1";
        load_and_test_dag(onto_name);
    }

    public void test_1_1_0() throws Exception {
        String onto_name = "test_1_1_0";
        load_and_test_dag(onto_name);
    }

    public void test_1_2_0() throws Exception {
        String onto_name = "test_1_2_0";
        load_and_test_dag(onto_name);
    }

    public void test_1_3_0() throws Exception {
        String onto_name = "test_1_3_0";
        load_and_test_dag(onto_name);
    }

//    public void test_1_4_0() throws Exception {
//        String onto_name = "test_1_4_0";
//        load_and_test_dag(onto_name);
//    }

}
