package org.obda.reformulation.tests;

import java.util.Set;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReformulationTest extends TestCase {

    private Tester tester = null;
    Logger log = LoggerFactory.getLogger(this.getClass());
    private String propfile = "src/test/resources/test.properties";

    public ReformulationTest() {
        tester = new Tester(propfile);
    }

    private void test_function(String ontoname) throws Exception {
        log.debug("Test case: {}", ontoname);
        log.debug("Testing in-memory db/material abox");
        tester.load(ontoname, "material");
        for (String id : tester.getQueryIds()) {
            log.debug("Testing query: {}", id);
            Set<String> exp = tester.getExpectedResult(id);
            Set<String> res = tester.executeQuery(id);
            assertTrue(exp.size() == res.size());
            for (String realResult : res) {
                assertTrue(exp.contains(realResult));
            }
        }

        log.debug("Testing in-memory db/vitual abox");
        tester.load(ontoname, "virtual");
        for (String id : tester.getQueryIds()) {
            log.debug("Testing query: {}", id);
            Set<String> exp = tester.getExpectedResult(id);
            Set<String> res = tester.executeQuery(id);
            assertTrue(exp.size() == res.size());
            for (String realResult : res) {
                assertTrue(exp.contains(realResult));
            }
        }
        // Uncomment for testing SemanticIndex
//		log.debug("Testing in-memory db/SemanticIndex");
//		tester.load(ontoname, "semantic");
//		for(String id : tester.getQueryIds()) {
//			log.debug("Testing query: {}", id);
//			Set<String> exp = tester.getExpectedResult(id);
//			Set<String> res = tester.executeQuery(id);
//			assertTrue(exp.size() == res.size());
//			for (String realResult : res) {
//				assertTrue(exp.contains(realResult));
//			}
//		}

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

    public void test_190_1() throws Exception {
        String ontoname = "test_190_1";
        test_function(ontoname);
    }

    public void test_190_2() throws Exception {
        String ontoname = "test_190_2";
        test_function(ontoname);
    }

    public void test_190_3() throws Exception {
        String ontoname = "test_190_3";
        test_function(ontoname);
    }

    public void test_190_4() throws Exception {
        String ontoname = "test_190_4";
        test_function(ontoname);
    }

    public void test_190_5() throws Exception {
        String ontoname = "test_190_5";
        test_function(ontoname);
    }

    public void test_190_6() throws Exception {
        String ontoname = "test_190_6";
        test_function(ontoname);
    }

    public void test_190_7() throws Exception {
        String ontoname = "test_190_7";
        test_function(ontoname);
    }

    public void test_190_8() throws Exception {
        String ontoname = "test_190_8";
        test_function(ontoname);
    }

    public void test_190_9() throws Exception {
        String ontoname = "test_190_9";
        test_function(ontoname);
    }

    public void test_190_10() throws Exception {
        String ontoname = "test_190_10";
        test_function(ontoname);
    }

    public void test_190_11() throws Exception {
        String ontoname = "test_190_11";
        test_function(ontoname);
    }

    public void test_191_0() throws Exception {
        String ontoname = "test_191_0";
        test_function(ontoname);
    }

    public void test_191_1() throws Exception {
        String ontoname = "test_191_1";
        test_function(ontoname);
    }

    public void test_191_2() throws Exception {
        String ontoname = "test_191_2";
        test_function(ontoname);
    }

    public void test_191_3() throws Exception {
        String ontoname = "test_191_3";
        test_function(ontoname);
    }

    public void test_191_4() throws Exception {
        String ontoname = "test_191_4";
        test_function(ontoname);
    }

    public void test_191_5() throws Exception {
        String ontoname = "test_191_5";
        test_function(ontoname);
    }

    public void test_191_6() throws Exception {
        String ontoname = "test_191_6";
        test_function(ontoname);
    }

    public void test_191_7() throws Exception {
        String ontoname = "test_191_7";
        test_function(ontoname);
    }

    public void test_191_8() throws Exception {
        String ontoname = "test_191_8";
        test_function(ontoname);
    }

    public void test_191_9() throws Exception {
        String ontoname = "test_191_9";
        test_function(ontoname);
    }

    public void test_191_10() throws Exception {
        String ontoname = "test_191_10";
        test_function(ontoname);
    }

    public void test_191_11() throws Exception {
        String ontoname = "test_191_11";
        test_function(ontoname);
    }

    public void test_192_0() throws Exception {
        String ontoname = "test_192_0";
        test_function(ontoname);
    }

    public void test_192_1() throws Exception {
        String ontoname = "test_192_1";
        test_function(ontoname);
    }

    public void test_192_2() throws Exception {
        String ontoname = "test_192_2";
        test_function(ontoname);
    }

    public void test_192_3() throws Exception {
        String ontoname = "test_192_3";
        test_function(ontoname);
    }

    public void test_192_4() throws Exception {
        String ontoname = "test_192_4";
        test_function(ontoname);
    }

    public void test_192_5() throws Exception {
        String ontoname = "test_192_5";
        test_function(ontoname);
    }

    public void test_192_6() throws Exception {
        String ontoname = "test_192_6";
        test_function(ontoname);
    }

    public void test_192_7() throws Exception {
        String ontoname = "test_192_7";
        test_function(ontoname);
    }

    public void test_192_8() throws Exception {
        String ontoname = "test_192_8";
        test_function(ontoname);
    }

    public void test_192_9() throws Exception {
        String ontoname = "test_192_9";
        test_function(ontoname);
    }

    public void test_192_10() throws Exception {
        String ontoname = "test_192_10";
        test_function(ontoname);
    }

    public void test_192_11() throws Exception {
        String ontoname = "test_192_11";
        test_function(ontoname);
    }

    public void test_192_12() throws Exception {
        String ontoname = "test_192_12";
        test_function(ontoname);
    }

    public void test_192_13() throws Exception {
        String ontoname = "test_192_13";
        test_function(ontoname);
    }

    public void test_192_14() throws Exception {
        String ontoname = "test_192_14";
        test_function(ontoname);
    }

    public void test_192_15() throws Exception {
        String ontoname = "test_192_15";
        test_function(ontoname);
    }

    public void test_192_16() throws Exception {
        String ontoname = "test_192_16";
        test_function(ontoname);
    }

    public void test_192_17() throws Exception {
        String ontoname = "test_192_17";
        test_function(ontoname);
    }

    public void test_192_18() throws Exception {
        String ontoname = "test_192_18";
        test_function(ontoname);
    }

    public void test_192_19() throws Exception {
        String ontoname = "test_192_19";
        test_function(ontoname);
    }

    public void test_192_20() throws Exception {
        String ontoname = "test_192_20";
        test_function(ontoname);
    }

    public void test_192_21() throws Exception {
        String ontoname = "test_192_21";
        test_function(ontoname);
    }

    public void test_192_22() throws Exception {
        String ontoname = "test_192_22";
        test_function(ontoname);
    }

    public void test_192_23() throws Exception {
        String ontoname = "test_192_23";
        test_function(ontoname);
    }

    public void test_192_24() throws Exception {
        String ontoname = "test_192_24";
        test_function(ontoname);
    }

    public void test_192_25() throws Exception {
        String ontoname = "test_192_25";
        test_function(ontoname);
    }

    public void test_192_26() throws Exception {
        String ontoname = "test_192_26";
        test_function(ontoname);
    }

    public void test_192_27() throws Exception {
        String ontoname = "test_192_27";
        test_function(ontoname);
    }

    public void test_192_28() throws Exception {
        String ontoname = "test_192_28";
        test_function(ontoname);
    }

    public void test_192_29() throws Exception {
        String ontoname = "test_192_29";
        test_function(ontoname);
    }

    public void test_192_30() throws Exception {
        String ontoname = "test_192_30";
        test_function(ontoname);
    }

    public void test_192_31() throws Exception {
        String ontoname = "test_192_31";
        test_function(ontoname);
    }

    public void test_192_32() throws Exception {
        String ontoname = "test_192_32";
        test_function(ontoname);
    }

    public void test_192_33() throws Exception {
        String ontoname = "test_192_33";
        test_function(ontoname);
    }

    public void test_192_34() throws Exception {
        String ontoname = "test_192_34";
        test_function(ontoname);
    }

    public void test_192_35() throws Exception {
        String ontoname = "test_192_35";
        test_function(ontoname);
    }

    public void test_193_0() throws Exception {
        String ontoname = "test_193_0";
        test_function(ontoname);
    }

    public void test_193_1() throws Exception {
        String ontoname = "test_193_1";
        test_function(ontoname);
    }

    public void test_193_2() throws Exception {
        String ontoname = "test_193_2";
        test_function(ontoname);
    }

    public void test_193_3() throws Exception {
        String ontoname = "test_193_3";
        test_function(ontoname);
    }

    public void test_193_4() throws Exception {
        String ontoname = "test_193_4";
        test_function(ontoname);
    }

    public void test_193_5() throws Exception {
        String ontoname = "test_193_5";
        test_function(ontoname);
    }

    public void test_193_6() throws Exception {
        String ontoname = "test_193_6";
        test_function(ontoname);
    }

    public void test_193_7() throws Exception {
        String ontoname = "test_193_7";
        test_function(ontoname);
    }

    public void test_193_8() throws Exception {
        String ontoname = "test_193_8";
        test_function(ontoname);
    }

    public void test_193_9() throws Exception {
        String ontoname = "test_193_9";
        test_function(ontoname);
    }

    public void test_193_10() throws Exception {
        String ontoname = "test_193_10";
        test_function(ontoname);
    }

    public void test_193_11() throws Exception {
        String ontoname = "test_193_11";
        test_function(ontoname);
    }

    public void test_193_12() throws Exception {
        String ontoname = "test_193_12";
        test_function(ontoname);
    }

    public void test_193_13() throws Exception {
        String ontoname = "test_193_13";
        test_function(ontoname);
    }

    public void test_193_14() throws Exception {
        String ontoname = "test_193_14";
        test_function(ontoname);
    }

    public void test_193_15() throws Exception {
        String ontoname = "test_193_15";
        test_function(ontoname);
    }

    public void test_193_16() throws Exception {
        String ontoname = "test_193_16";
        test_function(ontoname);
    }

    public void test_193_17() throws Exception {
        String ontoname = "test_193_17";
        test_function(ontoname);
    }

    public void test_193_18() throws Exception {
        String ontoname = "test_193_18";
        test_function(ontoname);
    }

    public void test_193_19() throws Exception {
        String ontoname = "test_193_19";
        test_function(ontoname);
    }

    public void test_193_20() throws Exception {
        String ontoname = "test_193_20";
        test_function(ontoname);
    }

    public void test_193_21() throws Exception {
        String ontoname = "test_193_21";
        test_function(ontoname);
    }

    public void test_193_22() throws Exception {
        String ontoname = "test_193_22";
        test_function(ontoname);
    }

    public void test_193_23() throws Exception {
        String ontoname = "test_193_23";
        test_function(ontoname);
    }

    public void test_193_24() throws Exception {
        String ontoname = "test_193_24";
        test_function(ontoname);
    }

    public void test_193_25() throws Exception {
        String ontoname = "test_193_25";
        test_function(ontoname);
    }

    public void test_193_26() throws Exception {
        String ontoname = "test_193_26";
        test_function(ontoname);
    }

    public void test_193_27() throws Exception {
        String ontoname = "test_193_27";
        test_function(ontoname);
    }

    public void test_193_28() throws Exception {
        String ontoname = "test_193_28";
        test_function(ontoname);
    }

    public void test_193_29() throws Exception {
        String ontoname = "test_193_29";
        test_function(ontoname);
    }

    public void test_193_30() throws Exception {
        String ontoname = "test_193_30";
        test_function(ontoname);
    }

    public void test_193_31() throws Exception {
        String ontoname = "test_193_31";
        test_function(ontoname);
    }

    public void test_193_32() throws Exception {
        String ontoname = "test_193_32";
        test_function(ontoname);
    }

    public void test_193_33() throws Exception {
        String ontoname = "test_193_33";
        test_function(ontoname);
    }

    public void test_193_34() throws Exception {
        String ontoname = "test_193_34";
        test_function(ontoname);
    }

    public void test_193_35() throws Exception {
        String ontoname = "test_193_35";
        test_function(ontoname);
    }

    public void test_194_0() throws Exception {
        String ontoname = "test_194_0";
        test_function(ontoname);
    }

    public void test_194_1() throws Exception {
        String ontoname = "test_194_1";
        test_function(ontoname);
    }

    public void test_194_2() throws Exception {
        String ontoname = "test_194_2";
        test_function(ontoname);
    }

    public void test_194_3() throws Exception {
        String ontoname = "test_194_3";
        test_function(ontoname);
    }

    public void test_194_4() throws Exception {
        String ontoname = "test_194_4";
        test_function(ontoname);
    }

    public void test_194_5() throws Exception {
        String ontoname = "test_194_5";
        test_function(ontoname);
    }

    public void test_194_6() throws Exception {
        String ontoname = "test_194_6";
        test_function(ontoname);
    }

    public void test_194_7() throws Exception {
        String ontoname = "test_194_7";
        test_function(ontoname);
    }

    public void test_194_8() throws Exception {
        String ontoname = "test_194_8";
        test_function(ontoname);
    }

    public void test_194_9() throws Exception {
        String ontoname = "test_194_9";
        test_function(ontoname);
    }

    public void test_194_10() throws Exception {
        String ontoname = "test_194_10";
        test_function(ontoname);
    }

    public void test_194_11() throws Exception {
        String ontoname = "test_194_11";
        test_function(ontoname);
    }

    public void test_194_12() throws Exception {
        String ontoname = "test_194_12";
        test_function(ontoname);
    }

    public void test_194_13() throws Exception {
        String ontoname = "test_194_13";
        test_function(ontoname);
    }

    public void test_194_14() throws Exception {
        String ontoname = "test_194_14";
        test_function(ontoname);
    }

    public void test_194_15() throws Exception {
        String ontoname = "test_194_15";
        test_function(ontoname);
    }

    public void test_194_16() throws Exception {
        String ontoname = "test_194_16";
        test_function(ontoname);
    }

    public void test_194_17() throws Exception {
        String ontoname = "test_194_17";
        test_function(ontoname);
    }

    public void test_194_18() throws Exception {
        String ontoname = "test_194_18";
        test_function(ontoname);
    }

    public void test_194_19() throws Exception {
        String ontoname = "test_194_19";
        test_function(ontoname);
    }

    public void test_194_20() throws Exception {
        String ontoname = "test_194_20";
        test_function(ontoname);
    }

    public void test_194_21() throws Exception {
        String ontoname = "test_194_21";
        test_function(ontoname);
    }

    public void test_194_22() throws Exception {
        String ontoname = "test_194_22";
        test_function(ontoname);
    }

    public void test_194_23() throws Exception {
        String ontoname = "test_194_23";
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
}
