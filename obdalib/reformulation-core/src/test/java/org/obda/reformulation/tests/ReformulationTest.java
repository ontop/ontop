package org.obda.reformulation.tests;
import junit.framework.TestCase;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReformulationTest extends TestCase {

    private Tester tester = null;
    Logger	log	= LoggerFactory.getLogger(this.getClass());
    private String propfile = "src/test/resources/test.properties";
    public ReformulationTest(){
        tester = new Tester(propfile);
    }
    private void test_function(String ontoname) throws Exception {
        log.debug("Test case: {}", ontoname);
        log.debug("Testing in-memory db/direct-mappings");
        tester.load(ontoname, "direct");
        for(String id : tester.getQueryIds()) {
            log.debug("Testing query: {}", id);
            List<String> exp = set2List(tester.getExpectedResult(id));
            List<String> res = set2List(tester.executeQuery(id));
            assertEquals(exp, res);
        }

        log.debug("Testing in-memory db/complex-mappings");
        tester.load(ontoname, "complex");
        for(String id : tester.getQueryIds()) {
            log.debug("Testing query: {}", id);
            List<String> exp = set2List(tester.getExpectedResult(id));
            List<String> res = set2List(tester.executeQuery(id));
            assertEquals(exp, res);
        }

        log.debug("Testing in-memory db/SemanticIndex");
        tester.load(ontoname, "semantic");
        for(String id : tester.getQueryIds()) {
            log.debug("Testing query: {}", id);
            List<String> exp = set2List(tester.getExpectedResult(id));
            List<String> res = set2List(tester.executeQuery(id));
            assertEquals(exp, res);
        }
    }
    private List<String> set2List(Set<String> s) {
        List<String> rv = new ArrayList<String>(s.size());
        for (String obj : s) {
            rv.add(obj);
        }
        Collections.sort(rv);
        return rv;
    }
    public void test_1_0_0() throws Exception {
        String ontoname = "test_1_0_0";
        test_function(ontoname);
    }

    public void test_2_0_0() throws Exception {
        String ontoname = "test_2_0_0";
        test_function(ontoname);
    }

    public void test_3_0_0() throws Exception {
        String ontoname = "test_3_0_0";
        test_function(ontoname);
    }

    public void test_4_1_1() throws Exception {
        String ontoname = "test_4_1_1";
        test_function(ontoname);
    }

    public void test_4_1_2() throws Exception {
        String ontoname = "test_4_1_2";
        test_function(ontoname);
    }

    public void test_5_1_1() throws Exception {
        String ontoname = "test_5_1_1";
        test_function(ontoname);
    }

    public void test_5_1_2() throws Exception {
        String ontoname = "test_5_1_2";
        test_function(ontoname);
    }

    public void test_6_1_1() throws Exception {
        String ontoname = "test_6_1_1";
        test_function(ontoname);
    }

    public void test_6_1_2() throws Exception {
        String ontoname = "test_6_1_2";
        test_function(ontoname);
    }

    public void test_7_1_1() throws Exception {
        String ontoname = "test_7_1_1";
        test_function(ontoname);
    }

    public void test_7_1_2() throws Exception {
        String ontoname = "test_7_1_2";
        test_function(ontoname);
    }

    public void test_8_1_1() throws Exception {
        String ontoname = "test_8_1_1";
        test_function(ontoname);
    }

    public void test_8_1_2() throws Exception {
        String ontoname = "test_8_1_2";
        test_function(ontoname);
    }

    public void test_9_1_1() throws Exception {
        String ontoname = "test_9_1_1";
        test_function(ontoname);
    }

    public void test_9_1_2() throws Exception {
        String ontoname = "test_9_1_2";
        test_function(ontoname);
    }

    public void test_10_0_0() throws Exception {
        String ontoname = "test_10_0_0";
        test_function(ontoname);
    }

    public void test_10_1_0() throws Exception {
        String ontoname = "test_10_1_0";
        test_function(ontoname);
    }

    public void test_10_0_3() throws Exception {
        String ontoname = "test_10_0_3";
        test_function(ontoname);
    }

    public void test_11_0_0() throws Exception {
        String ontoname = "test_11_0_0";
        test_function(ontoname);
    }

    public void test_11_1_0() throws Exception {
        String ontoname = "test_11_1_0";
        test_function(ontoname);
    }

    public void test_11_0_3() throws Exception {
        String ontoname = "test_11_0_3";
        test_function(ontoname);
    }

    public void test_12_0_0() throws Exception {
        String ontoname = "test_12_0_0";
        test_function(ontoname);
    }

    public void test_12_1_0() throws Exception {
        String ontoname = "test_12_1_0";
        test_function(ontoname);
    }

    public void test_12_0_3() throws Exception {
        String ontoname = "test_12_0_3";
        test_function(ontoname);
    }

    public void test_13_1_1() throws Exception {
        String ontoname = "test_13_1_1";
        test_function(ontoname);
    }

    public void test_13_1_2() throws Exception {
        String ontoname = "test_13_1_2";
        test_function(ontoname);
    }

    public void test_13_2_1() throws Exception {
        String ontoname = "test_13_2_1";
        test_function(ontoname);
    }

    public void test_13_2_2() throws Exception {
        String ontoname = "test_13_2_2";
        test_function(ontoname);
    }

    public void test_13_1_3() throws Exception {
        String ontoname = "test_13_1_3";
        test_function(ontoname);
    }

    public void test_13_2_3() throws Exception {
        String ontoname = "test_13_2_3";
        test_function(ontoname);
    }

    public void test_14_1_1() throws Exception {
        String ontoname = "test_14_1_1";
        test_function(ontoname);
    }

    public void test_14_1_2() throws Exception {
        String ontoname = "test_14_1_2";
        test_function(ontoname);
    }

    public void test_14_2_1() throws Exception {
        String ontoname = "test_14_2_1";
        test_function(ontoname);
    }

    public void test_14_2_2() throws Exception {
        String ontoname = "test_14_2_2";
        test_function(ontoname);
    }

    public void test_14_1_3() throws Exception {
        String ontoname = "test_14_1_3";
        test_function(ontoname);
    }

    public void test_14_2_3() throws Exception {
        String ontoname = "test_14_2_3";
        test_function(ontoname);
    }

    public void test_15_1_1() throws Exception {
        String ontoname = "test_15_1_1";
        test_function(ontoname);
    }

    public void test_15_1_2() throws Exception {
        String ontoname = "test_15_1_2";
        test_function(ontoname);
    }

    public void test_15_2_1() throws Exception {
        String ontoname = "test_15_2_1";
        test_function(ontoname);
    }

    public void test_15_2_2() throws Exception {
        String ontoname = "test_15_2_2";
        test_function(ontoname);
    }

    public void test_15_1_3() throws Exception {
        String ontoname = "test_15_1_3";
        test_function(ontoname);
    }

    public void test_15_2_3() throws Exception {
        String ontoname = "test_15_2_3";
        test_function(ontoname);
    }

    public void test_16_1_1() throws Exception {
        String ontoname = "test_16_1_1";
        test_function(ontoname);
    }

    public void test_16_1_2() throws Exception {
        String ontoname = "test_16_1_2";
        test_function(ontoname);
    }

    public void test_16_2_1() throws Exception {
        String ontoname = "test_16_2_1";
        test_function(ontoname);
    }

    public void test_16_2_2() throws Exception {
        String ontoname = "test_16_2_2";
        test_function(ontoname);
    }

    public void test_16_1_3() throws Exception {
        String ontoname = "test_16_1_3";
        test_function(ontoname);
    }

    public void test_16_2_3() throws Exception {
        String ontoname = "test_16_2_3";
        test_function(ontoname);
    }

    public void test_17_1_1() throws Exception {
        String ontoname = "test_17_1_1";
        test_function(ontoname);
    }

    public void test_17_1_2() throws Exception {
        String ontoname = "test_17_1_2";
        test_function(ontoname);
    }

    public void test_17_2_1() throws Exception {
        String ontoname = "test_17_2_1";
        test_function(ontoname);
    }

    public void test_17_2_2() throws Exception {
        String ontoname = "test_17_2_2";
        test_function(ontoname);
    }

    public void test_17_1_3() throws Exception {
        String ontoname = "test_17_1_3";
        test_function(ontoname);
    }

    public void test_17_2_3() throws Exception {
        String ontoname = "test_17_2_3";
        test_function(ontoname);
    }

    public void test_18_1_1() throws Exception {
        String ontoname = "test_18_1_1";
        test_function(ontoname);
    }

    public void test_18_1_2() throws Exception {
        String ontoname = "test_18_1_2";
        test_function(ontoname);
    }

    public void test_18_2_1() throws Exception {
        String ontoname = "test_18_2_1";
        test_function(ontoname);
    }

    public void test_18_2_2() throws Exception {
        String ontoname = "test_18_2_2";
        test_function(ontoname);
    }

    public void test_18_1_3() throws Exception {
        String ontoname = "test_18_1_3";
        test_function(ontoname);
    }

    public void test_18_2_3() throws Exception {
        String ontoname = "test_18_2_3";
        test_function(ontoname);
    }

    public void test_19_0_0() throws Exception {
        String ontoname = "test_19_0_0";
        test_function(ontoname);
    }

    public void test_19_1_0() throws Exception {
        String ontoname = "test_19_1_0";
        test_function(ontoname);
    }

    public void test_19_0_3() throws Exception {
        String ontoname = "test_19_0_3";
        test_function(ontoname);
    }

    public void test_20_0_0() throws Exception {
        String ontoname = "test_20_0_0";
        test_function(ontoname);
    }

    public void test_20_1_0() throws Exception {
        String ontoname = "test_20_1_0";
        test_function(ontoname);
    }

    public void test_20_0_3() throws Exception {
        String ontoname = "test_20_0_3";
        test_function(ontoname);
    }

    public void test_21_0_0() throws Exception {
        String ontoname = "test_21_0_0";
        test_function(ontoname);
    }

    public void test_21_1_0() throws Exception {
        String ontoname = "test_21_1_0";
        test_function(ontoname);
    }

    public void test_21_0_3() throws Exception {
        String ontoname = "test_21_0_3";
        test_function(ontoname);
    }

    public void test_22_1_1() throws Exception {
        String ontoname = "test_22_1_1";
        test_function(ontoname);
    }

    public void test_22_1_2() throws Exception {
        String ontoname = "test_22_1_2";
        test_function(ontoname);
    }

    public void test_22_2_1() throws Exception {
        String ontoname = "test_22_2_1";
        test_function(ontoname);
    }

    public void test_22_2_2() throws Exception {
        String ontoname = "test_22_2_2";
        test_function(ontoname);
    }

    public void test_22_1_3() throws Exception {
        String ontoname = "test_22_1_3";
        test_function(ontoname);
    }

    public void test_22_2_3() throws Exception {
        String ontoname = "test_22_2_3";
        test_function(ontoname);
    }

    public void test_23_1_1() throws Exception {
        String ontoname = "test_23_1_1";
        test_function(ontoname);
    }

    public void test_23_1_2() throws Exception {
        String ontoname = "test_23_1_2";
        test_function(ontoname);
    }

    public void test_23_2_1() throws Exception {
        String ontoname = "test_23_2_1";
        test_function(ontoname);
    }

    public void test_23_2_2() throws Exception {
        String ontoname = "test_23_2_2";
        test_function(ontoname);
    }

    public void test_23_1_3() throws Exception {
        String ontoname = "test_23_1_3";
        test_function(ontoname);
    }

    public void test_23_2_3() throws Exception {
        String ontoname = "test_23_2_3";
        test_function(ontoname);
    }

    public void test_24_1_1() throws Exception {
        String ontoname = "test_24_1_1";
        test_function(ontoname);
    }

    public void test_24_1_2() throws Exception {
        String ontoname = "test_24_1_2";
        test_function(ontoname);
    }

    public void test_24_2_1() throws Exception {
        String ontoname = "test_24_2_1";
        test_function(ontoname);
    }

    public void test_24_2_2() throws Exception {
        String ontoname = "test_24_2_2";
        test_function(ontoname);
    }

    public void test_24_1_3() throws Exception {
        String ontoname = "test_24_1_3";
        test_function(ontoname);
    }

    public void test_24_2_3() throws Exception {
        String ontoname = "test_24_2_3";
        test_function(ontoname);
    }

    public void test_25_1_1() throws Exception {
        String ontoname = "test_25_1_1";
        test_function(ontoname);
    }

    public void test_25_1_2() throws Exception {
        String ontoname = "test_25_1_2";
        test_function(ontoname);
    }

    public void test_25_2_1() throws Exception {
        String ontoname = "test_25_2_1";
        test_function(ontoname);
    }

    public void test_25_2_2() throws Exception {
        String ontoname = "test_25_2_2";
        test_function(ontoname);
    }

    public void test_25_1_3() throws Exception {
        String ontoname = "test_25_1_3";
        test_function(ontoname);
    }

    public void test_25_2_3() throws Exception {
        String ontoname = "test_25_2_3";
        test_function(ontoname);
    }

    public void test_26_1_1() throws Exception {
        String ontoname = "test_26_1_1";
        test_function(ontoname);
    }

    public void test_26_1_2() throws Exception {
        String ontoname = "test_26_1_2";
        test_function(ontoname);
    }

    public void test_26_2_1() throws Exception {
        String ontoname = "test_26_2_1";
        test_function(ontoname);
    }

    public void test_26_2_2() throws Exception {
        String ontoname = "test_26_2_2";
        test_function(ontoname);
    }

    public void test_26_1_3() throws Exception {
        String ontoname = "test_26_1_3";
        test_function(ontoname);
    }

    public void test_26_2_3() throws Exception {
        String ontoname = "test_26_2_3";
        test_function(ontoname);
    }

    public void test_27_1_1() throws Exception {
        String ontoname = "test_27_1_1";
        test_function(ontoname);
    }

    public void test_27_1_2() throws Exception {
        String ontoname = "test_27_1_2";
        test_function(ontoname);
    }

    public void test_27_2_1() throws Exception {
        String ontoname = "test_27_2_1";
        test_function(ontoname);
    }

    public void test_27_2_2() throws Exception {
        String ontoname = "test_27_2_2";
        test_function(ontoname);
    }

    public void test_27_1_3() throws Exception {
        String ontoname = "test_27_1_3";
        test_function(ontoname);
    }

    public void test_27_2_3() throws Exception {
        String ontoname = "test_27_2_3";
        test_function(ontoname);
    }

    public void test_28() throws Exception {
        String ontoname = "test_28";
        test_function(ontoname);
    }

    public void test_29() throws Exception {
        String ontoname = "test_29";
        test_function(ontoname);
    }

    public void test_30_1_1() throws Exception {
        String ontoname = "test_30_1_1";
        test_function(ontoname);
    }

    public void test_30_1_2() throws Exception {
        String ontoname = "test_30_1_2";
        test_function(ontoname);
    }

    public void test_30_2_1() throws Exception {
        String ontoname = "test_30_2_1";
        test_function(ontoname);
    }

    public void test_30_2_2() throws Exception {
        String ontoname = "test_30_2_2";
        test_function(ontoname);
    }

    public void test_30_1_3() throws Exception {
        String ontoname = "test_30_1_3";
        test_function(ontoname);
    }

    public void test_30_2_3() throws Exception {
        String ontoname = "test_30_2_3";
        test_function(ontoname);
    }

    public void test_31() throws Exception {
        String ontoname = "test_31";
        test_function(ontoname);
    }

    public void test_32() throws Exception {
        String ontoname = "test_32";
        test_function(ontoname);
    }

    public void test_33() throws Exception {
        String ontoname = "test_33";
        test_function(ontoname);
    }

    public void test_34() throws Exception {
        String ontoname = "test_34";
        test_function(ontoname);
    }

    public void test_35() throws Exception {
        String ontoname = "test_35";
        test_function(ontoname);
    }

    public void test_36() throws Exception {
        String ontoname = "test_36";
        test_function(ontoname);
    }

    public void test_37() throws Exception {
        String ontoname = "test_37";
        test_function(ontoname);
    }

    public void test_38() throws Exception {
        String ontoname = "test_38";
        test_function(ontoname);
    }

    public void test_39() throws Exception {
        String ontoname = "test_39";
        test_function(ontoname);
    }

    public void test_40() throws Exception {
        String ontoname = "test_40";
        test_function(ontoname);
    }

    public void test_41() throws Exception {
        String ontoname = "test_41";
        test_function(ontoname);
    }

    public void test_42() throws Exception {
        String ontoname = "test_42";
        test_function(ontoname);
    }

    public void test_43_0() throws Exception {
        String ontoname = "test_43_0";
        test_function(ontoname);
    }

    public void test_43_1() throws Exception {
        String ontoname = "test_43_1";
        test_function(ontoname);
    }

    public void test_44_0() throws Exception {
        String ontoname = "test_44_0";
        test_function(ontoname);
    }

    public void test_44_1() throws Exception {
        String ontoname = "test_44_1";
        test_function(ontoname);
    }

    public void test_45_0() throws Exception {
        String ontoname = "test_45_0";
        test_function(ontoname);
    }

    public void test_45_1() throws Exception {
        String ontoname = "test_45_1";
        test_function(ontoname);
    }

    public void test_46_0() throws Exception {
        String ontoname = "test_46_0";
        test_function(ontoname);
    }

    public void test_46_1() throws Exception {
        String ontoname = "test_46_1";
        test_function(ontoname);
    }

    public void test_47_0_0() throws Exception {
        String ontoname = "test_47_0_0";
        test_function(ontoname);
    }

    public void test_48_0_0() throws Exception {
        String ontoname = "test_48_0_0";
        test_function(ontoname);
    }

    public void test_49_0_0() throws Exception {
        String ontoname = "test_49_0_0";
        test_function(ontoname);
    }

    public void test_50_1_1() throws Exception {
        String ontoname = "test_50_1_1";
        test_function(ontoname);
    }

    public void test_50_1_2() throws Exception {
        String ontoname = "test_50_1_2";
        test_function(ontoname);
    }

    public void test_51_1_1() throws Exception {
        String ontoname = "test_51_1_1";
        test_function(ontoname);
    }

    public void test_51_1_2() throws Exception {
        String ontoname = "test_51_1_2";
        test_function(ontoname);
    }

    public void test_52_1_1() throws Exception {
        String ontoname = "test_52_1_1";
        test_function(ontoname);
    }

    public void test_52_1_2() throws Exception {
        String ontoname = "test_52_1_2";
        test_function(ontoname);
    }

    public void test_53_1_1() throws Exception {
        String ontoname = "test_53_1_1";
        test_function(ontoname);
    }

    public void test_53_1_2() throws Exception {
        String ontoname = "test_53_1_2";
        test_function(ontoname);
    }

    public void test_54_1_1() throws Exception {
        String ontoname = "test_54_1_1";
        test_function(ontoname);
    }

    public void test_54_1_2() throws Exception {
        String ontoname = "test_54_1_2";
        test_function(ontoname);
    }

    public void test_55_1_1() throws Exception {
        String ontoname = "test_55_1_1";
        test_function(ontoname);
    }

    public void test_55_1_2() throws Exception {
        String ontoname = "test_55_1_2";
        test_function(ontoname);
    }

    public void test_56_0_0() throws Exception {
        String ontoname = "test_56_0_0";
        test_function(ontoname);
    }

    public void test_56_1_0() throws Exception {
        String ontoname = "test_56_1_0";
        test_function(ontoname);
    }

    public void test_56_0_3() throws Exception {
        String ontoname = "test_56_0_3";
        test_function(ontoname);
    }

    public void test_57_0_0() throws Exception {
        String ontoname = "test_57_0_0";
        test_function(ontoname);
    }

    public void test_57_1_0() throws Exception {
        String ontoname = "test_57_1_0";
        test_function(ontoname);
    }

    public void test_57_0_3() throws Exception {
        String ontoname = "test_57_0_3";
        test_function(ontoname);
    }

    public void test_58_0_0() throws Exception {
        String ontoname = "test_58_0_0";
        test_function(ontoname);
    }

    public void test_58_1_0() throws Exception {
        String ontoname = "test_58_1_0";
        test_function(ontoname);
    }

    public void test_58_0_3() throws Exception {
        String ontoname = "test_58_0_3";
        test_function(ontoname);
    }

    public void test_59_1_1() throws Exception {
        String ontoname = "test_59_1_1";
        test_function(ontoname);
    }

    public void test_59_1_2() throws Exception {
        String ontoname = "test_59_1_2";
        test_function(ontoname);
    }

    public void test_59_2_1() throws Exception {
        String ontoname = "test_59_2_1";
        test_function(ontoname);
    }

    public void test_59_2_2() throws Exception {
        String ontoname = "test_59_2_2";
        test_function(ontoname);
    }

    public void test_59_1_3() throws Exception {
        String ontoname = "test_59_1_3";
        test_function(ontoname);
    }

    public void test_59_2_3() throws Exception {
        String ontoname = "test_59_2_3";
        test_function(ontoname);
    }

    public void test_60_1_1() throws Exception {
        String ontoname = "test_60_1_1";
        test_function(ontoname);
    }

    public void test_60_1_2() throws Exception {
        String ontoname = "test_60_1_2";
        test_function(ontoname);
    }

    public void test_60_2_1() throws Exception {
        String ontoname = "test_60_2_1";
        test_function(ontoname);
    }

    public void test_60_2_2() throws Exception {
        String ontoname = "test_60_2_2";
        test_function(ontoname);
    }

    public void test_60_1_3() throws Exception {
        String ontoname = "test_60_1_3";
        test_function(ontoname);
    }

    public void test_60_2_3() throws Exception {
        String ontoname = "test_60_2_3";
        test_function(ontoname);
    }

    public void test_61_1_1() throws Exception {
        String ontoname = "test_61_1_1";
        test_function(ontoname);
    }

    public void test_61_1_2() throws Exception {
        String ontoname = "test_61_1_2";
        test_function(ontoname);
    }

    public void test_61_2_1() throws Exception {
        String ontoname = "test_61_2_1";
        test_function(ontoname);
    }

    public void test_61_2_2() throws Exception {
        String ontoname = "test_61_2_2";
        test_function(ontoname);
    }

    public void test_61_1_3() throws Exception {
        String ontoname = "test_61_1_3";
        test_function(ontoname);
    }

    public void test_61_2_3() throws Exception {
        String ontoname = "test_61_2_3";
        test_function(ontoname);
    }

    public void test_62_1_1() throws Exception {
        String ontoname = "test_62_1_1";
        test_function(ontoname);
    }

    public void test_62_1_2() throws Exception {
        String ontoname = "test_62_1_2";
        test_function(ontoname);
    }

    public void test_62_2_1() throws Exception {
        String ontoname = "test_62_2_1";
        test_function(ontoname);
    }

    public void test_62_2_2() throws Exception {
        String ontoname = "test_62_2_2";
        test_function(ontoname);
    }

    public void test_62_1_3() throws Exception {
        String ontoname = "test_62_1_3";
        test_function(ontoname);
    }

    public void test_62_2_3() throws Exception {
        String ontoname = "test_62_2_3";
        test_function(ontoname);
    }

    public void test_63_1_1() throws Exception {
        String ontoname = "test_63_1_1";
        test_function(ontoname);
    }

    public void test_63_1_2() throws Exception {
        String ontoname = "test_63_1_2";
        test_function(ontoname);
    }

    public void test_63_2_1() throws Exception {
        String ontoname = "test_63_2_1";
        test_function(ontoname);
    }

    public void test_63_2_2() throws Exception {
        String ontoname = "test_63_2_2";
        test_function(ontoname);
    }

    public void test_63_1_3() throws Exception {
        String ontoname = "test_63_1_3";
        test_function(ontoname);
    }

    public void test_63_2_3() throws Exception {
        String ontoname = "test_63_2_3";
        test_function(ontoname);
    }

    public void test_64_1_1() throws Exception {
        String ontoname = "test_64_1_1";
        test_function(ontoname);
    }

    public void test_64_1_2() throws Exception {
        String ontoname = "test_64_1_2";
        test_function(ontoname);
    }

    public void test_64_2_1() throws Exception {
        String ontoname = "test_64_2_1";
        test_function(ontoname);
    }

    public void test_64_2_2() throws Exception {
        String ontoname = "test_64_2_2";
        test_function(ontoname);
    }

    public void test_64_1_3() throws Exception {
        String ontoname = "test_64_1_3";
        test_function(ontoname);
    }

    public void test_64_2_3() throws Exception {
        String ontoname = "test_64_2_3";
        test_function(ontoname);
    }

    public void test_65_0_0() throws Exception {
        String ontoname = "test_65_0_0";
        test_function(ontoname);
    }

    public void test_65_1_0() throws Exception {
        String ontoname = "test_65_1_0";
        test_function(ontoname);
    }

    public void test_65_0_3() throws Exception {
        String ontoname = "test_65_0_3";
        test_function(ontoname);
    }

    public void test_66_0_0() throws Exception {
        String ontoname = "test_66_0_0";
        test_function(ontoname);
    }

    public void test_66_1_0() throws Exception {
        String ontoname = "test_66_1_0";
        test_function(ontoname);
    }

    public void test_66_0_3() throws Exception {
        String ontoname = "test_66_0_3";
        test_function(ontoname);
    }

    public void test_67_0_0() throws Exception {
        String ontoname = "test_67_0_0";
        test_function(ontoname);
    }

    public void test_67_1_0() throws Exception {
        String ontoname = "test_67_1_0";
        test_function(ontoname);
    }

    public void test_67_0_3() throws Exception {
        String ontoname = "test_67_0_3";
        test_function(ontoname);
    }

    public void test_68_1_1() throws Exception {
        String ontoname = "test_68_1_1";
        test_function(ontoname);
    }

    public void test_68_1_2() throws Exception {
        String ontoname = "test_68_1_2";
        test_function(ontoname);
    }

    public void test_68_2_1() throws Exception {
        String ontoname = "test_68_2_1";
        test_function(ontoname);
    }

    public void test_68_2_2() throws Exception {
        String ontoname = "test_68_2_2";
        test_function(ontoname);
    }

    public void test_68_1_3() throws Exception {
        String ontoname = "test_68_1_3";
        test_function(ontoname);
    }

    public void test_68_2_3() throws Exception {
        String ontoname = "test_68_2_3";
        test_function(ontoname);
    }

    public void test_69_1_1() throws Exception {
        String ontoname = "test_69_1_1";
        test_function(ontoname);
    }

    public void test_69_1_2() throws Exception {
        String ontoname = "test_69_1_2";
        test_function(ontoname);
    }

    public void test_69_2_1() throws Exception {
        String ontoname = "test_69_2_1";
        test_function(ontoname);
    }

    public void test_69_2_2() throws Exception {
        String ontoname = "test_69_2_2";
        test_function(ontoname);
    }

    public void test_69_1_3() throws Exception {
        String ontoname = "test_69_1_3";
        test_function(ontoname);
    }

    public void test_69_2_3() throws Exception {
        String ontoname = "test_69_2_3";
        test_function(ontoname);
    }

    public void test_70_1_1() throws Exception {
        String ontoname = "test_70_1_1";
        test_function(ontoname);
    }

    public void test_70_1_2() throws Exception {
        String ontoname = "test_70_1_2";
        test_function(ontoname);
    }

    public void test_70_2_1() throws Exception {
        String ontoname = "test_70_2_1";
        test_function(ontoname);
    }

    public void test_70_2_2() throws Exception {
        String ontoname = "test_70_2_2";
        test_function(ontoname);
    }

    public void test_70_1_3() throws Exception {
        String ontoname = "test_70_1_3";
        test_function(ontoname);
    }

    public void test_70_2_3() throws Exception {
        String ontoname = "test_70_2_3";
        test_function(ontoname);
    }

    public void test_71_1_1() throws Exception {
        String ontoname = "test_71_1_1";
        test_function(ontoname);
    }

    public void test_71_1_2() throws Exception {
        String ontoname = "test_71_1_2";
        test_function(ontoname);
    }

    public void test_71_2_1() throws Exception {
        String ontoname = "test_71_2_1";
        test_function(ontoname);
    }

    public void test_71_2_2() throws Exception {
        String ontoname = "test_71_2_2";
        test_function(ontoname);
    }

    public void test_71_1_3() throws Exception {
        String ontoname = "test_71_1_3";
        test_function(ontoname);
    }

    public void test_71_2_3() throws Exception {
        String ontoname = "test_71_2_3";
        test_function(ontoname);
    }

    public void test_72_1_1() throws Exception {
        String ontoname = "test_72_1_1";
        test_function(ontoname);
    }

    public void test_72_1_2() throws Exception {
        String ontoname = "test_72_1_2";
        test_function(ontoname);
    }

    public void test_72_2_1() throws Exception {
        String ontoname = "test_72_2_1";
        test_function(ontoname);
    }

    public void test_72_2_2() throws Exception {
        String ontoname = "test_72_2_2";
        test_function(ontoname);
    }

    public void test_72_1_3() throws Exception {
        String ontoname = "test_72_1_3";
        test_function(ontoname);
    }

    public void test_72_2_3() throws Exception {
        String ontoname = "test_72_2_3";
        test_function(ontoname);
    }

    public void test_73_1_1() throws Exception {
        String ontoname = "test_73_1_1";
        test_function(ontoname);
    }

    public void test_73_1_2() throws Exception {
        String ontoname = "test_73_1_2";
        test_function(ontoname);
    }

    public void test_73_2_1() throws Exception {
        String ontoname = "test_73_2_1";
        test_function(ontoname);
    }

    public void test_73_2_2() throws Exception {
        String ontoname = "test_73_2_2";
        test_function(ontoname);
    }

    public void test_73_1_3() throws Exception {
        String ontoname = "test_73_1_3";
        test_function(ontoname);
    }

    public void test_73_2_3() throws Exception {
        String ontoname = "test_73_2_3";
        test_function(ontoname);
    }

    public void test_74() throws Exception {
        String ontoname = "test_74";
        test_function(ontoname);
    }

    public void test_75() throws Exception {
        String ontoname = "test_75";
        test_function(ontoname);
    }

    public void test_76() throws Exception {
        String ontoname = "test_76";
        test_function(ontoname);
    }

    public void test_77() throws Exception {
        String ontoname = "test_77";
        test_function(ontoname);
    }

    public void test_78() throws Exception {
        String ontoname = "test_78";
        test_function(ontoname);
    }

    public void test_79() throws Exception {
        String ontoname = "test_79";
        test_function(ontoname);
    }

    public void test_80() throws Exception {
        String ontoname = "test_80";
        test_function(ontoname);
    }

    public void test_81() throws Exception {
        String ontoname = "test_81";
        test_function(ontoname);
    }

    public void test_82() throws Exception {
        String ontoname = "test_82";
        test_function(ontoname);
    }

    public void test_83() throws Exception {
        String ontoname = "test_83";
        test_function(ontoname);
    }

    public void test_84() throws Exception {
        String ontoname = "test_84";
        test_function(ontoname);
    }

    public void test_85() throws Exception {
        String ontoname = "test_85";
        test_function(ontoname);
    }

    public void test_86() throws Exception {
        String ontoname = "test_86";
        test_function(ontoname);
    }

    public void test_87_1_1() throws Exception {
        String ontoname = "test_87_1_1";
        test_function(ontoname);
    }

    public void test_87_1_2() throws Exception {
        String ontoname = "test_87_1_2";
        test_function(ontoname);
    }

    public void test_87_2_1() throws Exception {
        String ontoname = "test_87_2_1";
        test_function(ontoname);
    }

    public void test_87_2_2() throws Exception {
        String ontoname = "test_87_2_2";
        test_function(ontoname);
    }

    public void test_87_1_3() throws Exception {
        String ontoname = "test_87_1_3";
        test_function(ontoname);
    }

    public void test_87_2_3() throws Exception {
        String ontoname = "test_87_2_3";
        test_function(ontoname);
    }

    public void test_88_0() throws Exception {
        String ontoname = "test_88_0";
        test_function(ontoname);
    }

    public void test_88_1() throws Exception {
        String ontoname = "test_88_1";
        test_function(ontoname);
    }

    public void test_89_0() throws Exception {
        String ontoname = "test_89_0";
        test_function(ontoname);
    }

    public void test_89_1() throws Exception {
        String ontoname = "test_89_1";
        test_function(ontoname);
    }

    public void test_90_0() throws Exception {
        String ontoname = "test_90_0";
        test_function(ontoname);
    }

    public void test_90_1() throws Exception {
        String ontoname = "test_90_1";
        test_function(ontoname);
    }

    public void test_90_2() throws Exception {
        String ontoname = "test_90_2";
        test_function(ontoname);
    }

    public void test_190_0() throws Exception {
        String ontoname = "test_190_0";
        test_function(ontoname);
    }

    public void test_191_0() throws Exception {
        String ontoname = "test_191_0";
        test_function(ontoname);
    }

    public void test_192_0() throws Exception {
        String ontoname = "test_192_0";
        test_function(ontoname);
    }

    public void test_193_0() throws Exception {
        String ontoname = "test_193_0";
        test_function(ontoname);
    }

    public void test_194_0() throws Exception {
        String ontoname = "test_194_0";
        test_function(ontoname);
    }

    public void test_195_0() throws Exception {
        String ontoname = "test_195_0";
        test_function(ontoname);
    }

    public void test_196_0() throws Exception {
        String ontoname = "test_196_0";
        test_function(ontoname);
    }

    public void test_197_0() throws Exception {
        String ontoname = "test_197_0";
        test_function(ontoname);
    }

    public void test_197_1() throws Exception {
        String ontoname = "test_197_1";
        test_function(ontoname);
    }

    public void test_197_2() throws Exception {
        String ontoname = "test_197_2";
        test_function(ontoname);
    }

    public void test_198_0() throws Exception {
        String ontoname = "test_198_0";
        test_function(ontoname);
    }

    public void test_200_0() throws Exception {
        String ontoname = "test_200_0";
        test_function(ontoname);
    }

    public void test_201_0() throws Exception {
        String ontoname = "test_201_0";
        test_function(ontoname);
    }

}
