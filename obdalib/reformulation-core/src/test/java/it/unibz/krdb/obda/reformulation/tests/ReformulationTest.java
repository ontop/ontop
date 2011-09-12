package it.unibz.krdb.obda.reformulation.tests;
import junit.framework.TestCase;
import java.util.Set;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;


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

	public void test_1() throws Exception {
		String ontoname = "test_1";
		test_function(ontoname);
	}
	public void test_2() throws Exception {
		String ontoname = "test_2";
		test_function(ontoname);
	}
	public void test_3() throws Exception {
		String ontoname = "test_3";
		test_function(ontoname);
	}
	public void test_4() throws Exception {
		String ontoname = "test_4";
		test_function(ontoname);
	}
	public void test_5() throws Exception {
		String ontoname = "test_5";
		test_function(ontoname);
	}
	public void test_6() throws Exception {
		String ontoname = "test_6";
		test_function(ontoname);
	}
	public void test_7() throws Exception {
		String ontoname = "test_7";
		test_function(ontoname);
	}
	public void test_8() throws Exception {
		String ontoname = "test_8";
		test_function(ontoname);
	}
	public void test_9() throws Exception {
		String ontoname = "test_9";
		test_function(ontoname);
	}
	public void test_10() throws Exception {
		String ontoname = "test_10";
		test_function(ontoname);
	}
	public void test_11() throws Exception {
		String ontoname = "test_11";
		test_function(ontoname);
	}
	public void test_12() throws Exception {
		String ontoname = "test_12";
		test_function(ontoname);
	}
	public void test_13() throws Exception {
		String ontoname = "test_13";
		test_function(ontoname);
	}
	public void test_14() throws Exception {
		String ontoname = "test_14";
		test_function(ontoname);
	}
	public void test_15() throws Exception {
		String ontoname = "test_15";
		test_function(ontoname);
	}
	public void test_16() throws Exception {
		String ontoname = "test_16";
		test_function(ontoname);
	}
	public void test_17() throws Exception {
		String ontoname = "test_17";
		test_function(ontoname);
	}
	public void test_18() throws Exception {
		String ontoname = "test_18";
		test_function(ontoname);
	}
	public void test_19() throws Exception {
		String ontoname = "test_19";
		test_function(ontoname);
	}
	public void test_20() throws Exception {
		String ontoname = "test_20";
		test_function(ontoname);
	}
	public void test_21() throws Exception {
		String ontoname = "test_21";
		test_function(ontoname);
	}
	public void test_22() throws Exception {
		String ontoname = "test_22";
		test_function(ontoname);
	}
	public void test_23() throws Exception {
		String ontoname = "test_23";
		test_function(ontoname);
	}
	public void test_24() throws Exception {
		String ontoname = "test_24";
		test_function(ontoname);
	}
	public void test_25() throws Exception {
		String ontoname = "test_25";
		test_function(ontoname);
	}
	public void test_26() throws Exception {
		String ontoname = "test_26";
		test_function(ontoname);
	}
	public void test_27() throws Exception {
		String ontoname = "test_27";
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
	public void test_30() throws Exception {
		String ontoname = "test_30";
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
	public void test_43() throws Exception {
		String ontoname = "test_43";
		test_function(ontoname);
	}
	public void test_44() throws Exception {
		String ontoname = "test_44";
		test_function(ontoname);
	}
	public void test_45() throws Exception {
		String ontoname = "test_45";
		test_function(ontoname);
	}
	public void test_46() throws Exception {
		String ontoname = "test_46";
		test_function(ontoname);
	}
	public void test_47() throws Exception {
		String ontoname = "test_47";
		test_function(ontoname);
	}
	public void test_48() throws Exception {
		String ontoname = "test_48";
		test_function(ontoname);
	}
	public void test_49() throws Exception {
		String ontoname = "test_49";
		test_function(ontoname);
	}
	public void test_50() throws Exception {
		String ontoname = "test_50";
		test_function(ontoname);
	}
	public void test_51() throws Exception {
		String ontoname = "test_51";
		test_function(ontoname);
	}
	public void test_52() throws Exception {
		String ontoname = "test_52";
		test_function(ontoname);
	}
	public void test_53() throws Exception {
		String ontoname = "test_53";
		test_function(ontoname);
	}
	public void test_54() throws Exception {
		String ontoname = "test_54";
		test_function(ontoname);
	}
	public void test_55() throws Exception {
		String ontoname = "test_55";
		test_function(ontoname);
	}
	public void test_56() throws Exception {
		String ontoname = "test_56";
		test_function(ontoname);
	}
	public void test_57() throws Exception {
		String ontoname = "test_57";
		test_function(ontoname);
	}
	public void test_58() throws Exception {
		String ontoname = "test_58";
		test_function(ontoname);
	}
	public void test_59() throws Exception {
		String ontoname = "test_59";
		test_function(ontoname);
	}
	public void test_60() throws Exception {
		String ontoname = "test_60";
		test_function(ontoname);
	}
	public void test_61() throws Exception {
		String ontoname = "test_61";
		test_function(ontoname);
	}
	public void test_62() throws Exception {
		String ontoname = "test_62";
		test_function(ontoname);
	}
	public void test_63() throws Exception {
		String ontoname = "test_63";
		test_function(ontoname);
	}
	public void test_64() throws Exception {
		String ontoname = "test_64";
		test_function(ontoname);
	}
	public void test_65() throws Exception {
		String ontoname = "test_65";
		test_function(ontoname);
	}
	public void test_66() throws Exception {
		String ontoname = "test_66";
		test_function(ontoname);
	}
	public void test_67() throws Exception {
		String ontoname = "test_67";
		test_function(ontoname);
	}
	public void test_68() throws Exception {
		String ontoname = "test_68";
		test_function(ontoname);
	}
	public void test_69() throws Exception {
		String ontoname = "test_69";
		test_function(ontoname);
	}
	public void test_70() throws Exception {
		String ontoname = "test_70";
		test_function(ontoname);
	}
	public void test_71() throws Exception {
		String ontoname = "test_71";
		test_function(ontoname);
	}
	public void test_72() throws Exception {
		String ontoname = "test_72";
		test_function(ontoname);
	}
	public void test_73() throws Exception {
		String ontoname = "test_73";
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
	public void test_87() throws Exception {
		String ontoname = "test_87";
		test_function(ontoname);
	}
	public void test_88() throws Exception {
		String ontoname = "test_88";
		test_function(ontoname);
	}
	public void test_89() throws Exception {
		String ontoname = "test_89";
		test_function(ontoname);
	}
	public void test_90() throws Exception {
		String ontoname = "test_90";
		test_function(ontoname);
	}
	public void test_200() throws Exception {
		String ontoname = "test_200";
		test_function(ontoname);
	}
	public void test_201() throws Exception {
		String ontoname = "test_201";
		test_function(ontoname);
	}
	public void test_210() throws Exception {
		String ontoname = "test_210";
		test_function(ontoname);
	}
	public void test_401() throws Exception {
		String ontoname = "test_401";
		test_function(ontoname);
	}
	public void test_402() throws Exception {
		String ontoname = "test_402";
		test_function(ontoname);
	}
	public void test_403() throws Exception {
		String ontoname = "test_403";
		test_function(ontoname);
	}
	public void test_404() throws Exception {
		String ontoname = "test_404";
		test_function(ontoname);
	}
	public void test_405() throws Exception {
		String ontoname = "test_405";
		test_function(ontoname);
	}
	public void test_190() throws Exception {
		String ontoname = "test_190";
		test_function(ontoname);
	}
	public void test_191() throws Exception {
		String ontoname = "test_191";
		test_function(ontoname);
	}
	public void test_192() throws Exception {
		String ontoname = "test_192";
		test_function(ontoname);
	}
	public void test_193() throws Exception {
		String ontoname = "test_193";
		test_function(ontoname);
	}
	public void test_194() throws Exception {
		String ontoname = "test_194";
		test_function(ontoname);
	}
	public void test_195() throws Exception {
		String ontoname = "test_195";
		test_function(ontoname);
	}
	public void test_196() throws Exception {
		String ontoname = "test_196";
		test_function(ontoname);
	}
	public void test_197() throws Exception {
		String ontoname = "test_197";
		test_function(ontoname);
	}
	public void test_198() throws Exception {
		String ontoname = "test_198";
		test_function(ontoname);
	}
}
