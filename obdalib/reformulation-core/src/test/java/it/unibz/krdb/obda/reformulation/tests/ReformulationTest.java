package it.unibz.krdb.obda.reformulation.tests;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

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
     log.debug("Testing in-memory db/material abox");
     tester.load(ontoname, QuestConstants.CLASSIC);
     for (String id : tester.getQueryIds()) {
         log.debug("Testing query: {}", id);
         Set<String> exp = tester.getExpectedResult(id);
         Set<String> res = tester.executeQuery(id);
assertTrue("Expected " + exp + " Result " + res, exp.size() == res.size());
         for (String realResult : res) {
             assertTrue("expeted: " +exp.toString() + " obtained: " + res.toString(), exp.contains(realResult));
         }
     }

     log.debug("Testing in-memory db/vitual abox");
     tester.load(ontoname, QuestConstants.VIRTUAL);
     for (String id : tester.getQueryIds()) {
         log.debug("Testing query: {}", id);
         Set<String> exp = tester.getExpectedResult(id);
         Set<String> res = tester.executeQuery(id);
assertTrue("Expected " + exp + " Result " + res, exp.size() == res.size());
         for (String realResult : res) {
             assertTrue("expeted: " +exp.toString() + " obtained: " + res.toString(),exp.contains(realResult));
         }
     }
     log.debug("Testing in-memory db/SemanticIndex");
     tester.load(ontoname, QuestConstants.CLASSIC, QuestConstants.SEMANTIC);
     for (String id : tester.getQueryIds()) {
         log.debug("Testing query: {}", id);
         Set<String> exp = tester.getExpectedResult(id);
         Set<String> res = tester.executeQuery(id);
         List<String> exp_list = new LinkedList<String>(exp);
         List<String> res_list = new LinkedList<String>(res);
         Collections.sort(exp_list);
         Collections.sort(res_list);
         assertEquals(exp_list, res_list);
     }
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
	public void test_4_0_0() throws Exception {
		String ontoname = "test_4_0_0";
		test_function(ontoname);
	}
	public void test_5_0_0() throws Exception {
		String ontoname = "test_5_0_0";
		test_function(ontoname);
	}
	public void test_6_0_0() throws Exception {
		String ontoname = "test_6_0_0";
		test_function(ontoname);
	}
	public void test_7_0_0() throws Exception {
		String ontoname = "test_7_0_0";
		test_function(ontoname);
	}
	public void test_8_0_0() throws Exception {
		String ontoname = "test_8_0_0";
		test_function(ontoname);
	}
	public void test_9_0_0() throws Exception {
		String ontoname = "test_9_0_0";
		test_function(ontoname);
	}
	public void test_10_0_0() throws Exception {
		String ontoname = "test_10_0_0";
		test_function(ontoname);
	}
	public void test_11_0_0() throws Exception {
		String ontoname = "test_11_0_0";
		test_function(ontoname);
	}
	public void test_12_0_0() throws Exception {
		String ontoname = "test_12_0_0";
		test_function(ontoname);
	}
	public void test_13_0_0() throws Exception {
		String ontoname = "test_13_0_0";
		test_function(ontoname);
	}
	public void test_14_0_0() throws Exception {
		String ontoname = "test_14_0_0";
		test_function(ontoname);
	}
	public void test_15_0_0() throws Exception {
		String ontoname = "test_15_0_0";
		test_function(ontoname);
	}
	public void test_16_0_0() throws Exception {
		String ontoname = "test_16_0_0";
		test_function(ontoname);
	}
	public void test_17_0_0() throws Exception {
		String ontoname = "test_17_0_0";
		test_function(ontoname);
	}
	public void test_18_0_0() throws Exception {
		String ontoname = "test_18_0_0";
		test_function(ontoname);
	}
	public void test_19_0_0() throws Exception {
		String ontoname = "test_19_0_0";
		test_function(ontoname);
	}
	public void test_20_0_0() throws Exception {
		String ontoname = "test_20_0_0";
		test_function(ontoname);
	}
	public void test_21_0_0() throws Exception {
		String ontoname = "test_21_0_0";
		test_function(ontoname);
	}
	public void test_22_0_0() throws Exception {
		String ontoname = "test_22_0_0";
		test_function(ontoname);
	}
	public void test_23_0_0() throws Exception {
		String ontoname = "test_23_0_0";
		test_function(ontoname);
	}
	public void test_24_0_0() throws Exception {
		String ontoname = "test_24_0_0";
		test_function(ontoname);
	}
	public void test_25_0_0() throws Exception {
		String ontoname = "test_25_0_0";
		test_function(ontoname);
	}
	public void test_26_0_0() throws Exception {
		String ontoname = "test_26_0_0";
		test_function(ontoname);
	}
	public void test_27_0_0() throws Exception {
		String ontoname = "test_27_0_0";
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
	public void test_30_0_0() throws Exception {
		String ontoname = "test_30_0_0";
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
	public void test_44_0() throws Exception {
		String ontoname = "test_44_0";
		test_function(ontoname);
	}
	public void test_45_0() throws Exception {
		String ontoname = "test_45_0";
		test_function(ontoname);
	}
	public void test_46_0() throws Exception {
		String ontoname = "test_46_0";
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
	public void test_50_0_0() throws Exception {
		String ontoname = "test_50_0_0";
		test_function(ontoname);
	}
	public void test_51_0_0() throws Exception {
		String ontoname = "test_51_0_0";
		test_function(ontoname);
	}
	public void test_52_0_0() throws Exception {
		String ontoname = "test_52_0_0";
		test_function(ontoname);
	}
	public void test_53_0_0() throws Exception {
		String ontoname = "test_53_0_0";
		test_function(ontoname);
	}
	public void test_54_0_0() throws Exception {
		String ontoname = "test_54_0_0";
		test_function(ontoname);
	}
	public void test_55_0_0() throws Exception {
		String ontoname = "test_55_0_0";
		test_function(ontoname);
	}
	public void test_56_0_0() throws Exception {
		String ontoname = "test_56_0_0";
		test_function(ontoname);
	}
	public void test_57_0_0() throws Exception {
		String ontoname = "test_57_0_0";
		test_function(ontoname);
	}
	public void test_58_0_0() throws Exception {
		String ontoname = "test_58_0_0";
		test_function(ontoname);
	}
	public void test_59_0_0() throws Exception {
		String ontoname = "test_59_0_0";
		test_function(ontoname);
	}
	public void test_60_0_0() throws Exception {
		String ontoname = "test_60_0_0";
		test_function(ontoname);
	}
	public void test_61_0_0() throws Exception {
		String ontoname = "test_61_0_0";
		test_function(ontoname);
	}
	public void test_62_0_0() throws Exception {
		String ontoname = "test_62_0_0";
		test_function(ontoname);
	}
	public void test_63_0_0() throws Exception {
		String ontoname = "test_63_0_0";
		test_function(ontoname);
	}
	public void test_64_0_0() throws Exception {
		String ontoname = "test_64_0_0";
		test_function(ontoname);
	}
	public void test_65_0_0() throws Exception {
		String ontoname = "test_65_0_0";
		test_function(ontoname);
	}
	public void test_66_0_0() throws Exception {
		String ontoname = "test_66_0_0";
		test_function(ontoname);
	}
	public void test_67_0_0() throws Exception {
		String ontoname = "test_67_0_0";
		test_function(ontoname);
	}
	public void test_68_0_0() throws Exception {
		String ontoname = "test_68_0_0";
		test_function(ontoname);
	}
	public void test_69_0_0() throws Exception {
		String ontoname = "test_69_0_0";
		test_function(ontoname);
	}
	public void test_70_0_0() throws Exception {
		String ontoname = "test_70_0_0";
		test_function(ontoname);
	}
	public void test_71_0_0() throws Exception {
		String ontoname = "test_71_0_0";
		test_function(ontoname);
	}
	public void test_72_0_0() throws Exception {
		String ontoname = "test_72_0_0";
		test_function(ontoname);
	}
	public void test_73_0_0() throws Exception {
		String ontoname = "test_73_0_0";
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
	public void test_87_0_0() throws Exception {
		String ontoname = "test_87_0_0";
		test_function(ontoname);
	}
	public void test_88_0() throws Exception {
		String ontoname = "test_88_0";
		test_function(ontoname);
	}
	public void test_89_0() throws Exception {
		String ontoname = "test_89_0";
		test_function(ontoname);
	}
	public void test_90_0() throws Exception {
		String ontoname = "test_90_0";
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
	public void test_210_0() throws Exception {
		String ontoname = "test_210_0";
		test_function(ontoname);
	}
	public void test_401_0() throws Exception {
		String ontoname = "test_401_0";
		test_function(ontoname);
	}
	public void test_402_0() throws Exception {
		String ontoname = "test_402_0";
		test_function(ontoname);
	}
	public void test_403_0() throws Exception {
		String ontoname = "test_403_0";
		test_function(ontoname);
	}
	public void test_404_0() throws Exception {
		String ontoname = "test_404_0";
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
	public void test_198_0() throws Exception {
		String ontoname = "test_198_0";
		test_function(ontoname);
	}
}
