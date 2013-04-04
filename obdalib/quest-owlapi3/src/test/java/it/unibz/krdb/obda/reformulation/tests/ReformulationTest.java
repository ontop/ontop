package it.unibz.krdb.obda.reformulation.tests;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

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
        private void test_function(String ontoname, QuestPreferences pref) throws Exception {
        	
            pref.setProperty("rewrite", "true");

    	log.debug("Test case: {}", ontoname);
    	log.debug("Quest configuration: {}", pref.toString());
    	tester.load(ontoname, pref);
    	for (String id : tester.getQueryIds()) {
    		log.debug("Testing query: {}", id);
    		Set<String> exp = tester.getExpectedResult(id);
    		Set<String> res = tester.executeQuery(id);
    		assertTrue("Expected " + exp + " Result " + res, exp.size() == res.size());
    		for (String realResult : res) {
    			assertTrue("expeted: " + exp.toString() + " obtained: " + res.toString(), exp.contains(realResult));
    		}
    	}
    }
	public void test_600SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_600";
		test_function(ontoname,pref);
	}
	public void test_1SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_1";
		test_function(ontoname,pref);
	}
	public void test_2SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_2";
		test_function(ontoname,pref);
	}
	public void test_3SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_3";
		test_function(ontoname,pref);
	}
	public void test_4SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_4";
		test_function(ontoname,pref);
	}
	public void test_5SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_5";
		test_function(ontoname,pref);
	}
	public void test_6SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_6";
		test_function(ontoname,pref);
	}
	public void test_7SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_7";
		test_function(ontoname,pref);
	}
	public void test_8SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_8";
		test_function(ontoname,pref);
	}
	public void test_9SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_9";
		test_function(ontoname,pref);
	}
	public void test_10SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_10";
		test_function(ontoname,pref);
	}
	public void test_11SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_11";
		test_function(ontoname,pref);
	}
	public void test_12SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_12";
		test_function(ontoname,pref);
	}
	public void test_13SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_13";
		test_function(ontoname,pref);
	}
	public void test_14SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_14";
		test_function(ontoname,pref);
	}
	public void test_15SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_15";
		test_function(ontoname,pref);
	}
	public void test_16SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_16";
		test_function(ontoname,pref);
	}
	public void test_17SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_17";
		test_function(ontoname,pref);
	}
	public void test_18SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_18";
		test_function(ontoname,pref);
	}
	public void test_19SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_19";
		test_function(ontoname,pref);
	}
	public void test_20SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_20";
		test_function(ontoname,pref);
	}
	public void test_21SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_21";
		test_function(ontoname,pref);
	}
	public void test_22SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_22";
		test_function(ontoname,pref);
	}
	public void test_23SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_23";
		test_function(ontoname,pref);
	}
	public void test_24SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_24";
		test_function(ontoname,pref);
	}
	public void test_25SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_25";
		test_function(ontoname,pref);
	}
	public void test_26SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_26";
		test_function(ontoname,pref);
	}
	public void test_27SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_27";
		test_function(ontoname,pref);
	}
	public void test_28SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_28";
		test_function(ontoname,pref);
	}
	public void test_29SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_29";
		test_function(ontoname,pref);
	}
	public void test_30SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_30";
		test_function(ontoname,pref);
	}
	public void test_31SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_31";
		test_function(ontoname,pref);
	}
	public void test_32SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_32";
		test_function(ontoname,pref);
	}
	public void test_33SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_33";
		test_function(ontoname,pref);
	}
	public void test_34SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_34";
		test_function(ontoname,pref);
	}
	public void test_35SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_35";
		test_function(ontoname,pref);
	}
	public void test_36SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_36";
		test_function(ontoname,pref);
	}
	public void test_37SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_37";
		test_function(ontoname,pref);
	}
	public void test_38SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_38";
		test_function(ontoname,pref);
	}
	public void test_39SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_39";
		test_function(ontoname,pref);
	}
	public void test_40SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_40";
		test_function(ontoname,pref);
	}
	public void test_41SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_41";
		test_function(ontoname,pref);
	}
	public void test_42SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_42";
		test_function(ontoname,pref);
	}
	public void test_43SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_43";
		test_function(ontoname,pref);
	}
	public void test_44SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_44";
		test_function(ontoname,pref);
	}
	public void test_45SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_45";
		test_function(ontoname,pref);
	}
	public void test_46SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_46";
		test_function(ontoname,pref);
	}
	public void test_47SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_47";
		test_function(ontoname,pref);
	}
	public void test_48SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_48";
		test_function(ontoname,pref);
	}
	public void test_49SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_49";
		test_function(ontoname,pref);
	}
	public void test_50SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_50";
		test_function(ontoname,pref);
	}
	public void test_51SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_51";
		test_function(ontoname,pref);
	}
	public void test_52SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_52";
		test_function(ontoname,pref);
	}
	public void test_53SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_53";
		test_function(ontoname,pref);
	}
	public void test_54SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_54";
		test_function(ontoname,pref);
	}
	public void test_55SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_55";
		test_function(ontoname,pref);
	}
	public void test_56SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_56";
		test_function(ontoname,pref);
	}
	public void test_57SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_57";
		test_function(ontoname,pref);
	}
	public void test_58SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_58";
		test_function(ontoname,pref);
	}
	public void test_59SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_59";
		test_function(ontoname,pref);
	}
	public void test_60SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_60";
		test_function(ontoname,pref);
	}
	public void test_61SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_61";
		test_function(ontoname,pref);
	}
	public void test_62SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_62";
		test_function(ontoname,pref);
	}
	public void test_63SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_63";
		test_function(ontoname,pref);
	}
	public void test_64SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_64";
		test_function(ontoname,pref);
	}
	public void test_65SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_65";
		test_function(ontoname,pref);
	}
	public void test_66SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_66";
		test_function(ontoname,pref);
	}
	public void test_67SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_67";
		test_function(ontoname,pref);
	}
	public void test_68SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_68";
		test_function(ontoname,pref);
	}
	public void test_69SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_69";
		test_function(ontoname,pref);
	}
	public void test_70SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_70";
		test_function(ontoname,pref);
	}
	public void test_71SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_71";
		test_function(ontoname,pref);
	}
	public void test_72SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_72";
		test_function(ontoname,pref);
	}
	public void test_73SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_73";
		test_function(ontoname,pref);
	}
	public void test_74SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_74";
		test_function(ontoname,pref);
	}
	public void test_75SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_75";
		test_function(ontoname,pref);
	}
	public void test_76SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_76";
		test_function(ontoname,pref);
	}
	public void test_77SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_77";
		test_function(ontoname,pref);
	}
	public void test_78SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_78";
		test_function(ontoname,pref);
	}
	public void test_79SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_79";
		test_function(ontoname,pref);
	}
	public void test_80SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_80";
		test_function(ontoname,pref);
	}
	public void test_81SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_81";
		test_function(ontoname,pref);
	}
	public void test_82SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_82";
		test_function(ontoname,pref);
	}
	public void test_83SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_83";
		test_function(ontoname,pref);
	}
	public void test_84SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_84";
		test_function(ontoname,pref);
	}
	public void test_85SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_85";
		test_function(ontoname,pref);
	}
	public void test_86SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_86";
		test_function(ontoname,pref);
	}
	public void test_87SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_87";
		test_function(ontoname,pref);
	}
	public void test_88SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_88";
		test_function(ontoname,pref);
	}
	public void test_89SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_89";
		test_function(ontoname,pref);
	}
	public void test_90SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_90";
		test_function(ontoname,pref);
	}
	public void test_200SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_200";
		test_function(ontoname,pref);
	}
	public void test_201SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_201";
		test_function(ontoname,pref);
	}
	public void test_210SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_210";
		test_function(ontoname,pref);
	}
	public void test_401SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_401";
		test_function(ontoname,pref);
	}
	public void test_402SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_402";
		test_function(ontoname,pref);
	}
	public void test_403SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_403";
		test_function(ontoname,pref);
	}
	public void test_404SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_404";
		test_function(ontoname,pref);
	}
	public void test_405SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_405";
		test_function(ontoname,pref);
	}
	public void test_190SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_190";
		test_function(ontoname,pref);
	}
	public void test_191SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_191";
		test_function(ontoname,pref);
	}
	public void test_192SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_192";
		test_function(ontoname,pref);
	}
	public void test_193SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_193";
		test_function(ontoname,pref);
	}
	public void test_194SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_194";
		test_function(ontoname,pref);
	}
	public void test_195SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_195";
		test_function(ontoname,pref);
	}
	public void test_196SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_196";
		test_function(ontoname,pref);
	}
	public void test_197SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_197";
		test_function(ontoname,pref);
	}
	public void test_198SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_198";
		test_function(ontoname,pref);
	}
	public void test_500SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_500";
		test_function(ontoname,pref);
	}
	public void test_501SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_501";
		test_function(ontoname,pref);
	}
	public void test_502SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_502";
		test_function(ontoname,pref);
	}
	public void test_503SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_503";
		test_function(ontoname,pref);
	}
	public void test_504SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_504";
		test_function(ontoname,pref);
	}
	public void test_505SINoEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_505";
		test_function(ontoname,pref);
	}
	public void test_600SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_600";
		test_function(ontoname,pref);
	}
	public void test_1SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_1";
		test_function(ontoname,pref);
	}
	public void test_2SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_2";
		test_function(ontoname,pref);
	}
	public void test_3SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_3";
		test_function(ontoname,pref);
	}
	public void test_4SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_4";
		test_function(ontoname,pref);
	}
	public void test_5SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_5";
		test_function(ontoname,pref);
	}
	public void test_6SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_6";
		test_function(ontoname,pref);
	}
	public void test_7SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_7";
		test_function(ontoname,pref);
	}
	public void test_8SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_8";
		test_function(ontoname,pref);
	}
	public void test_9SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_9";
		test_function(ontoname,pref);
	}
	public void test_10SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_10";
		test_function(ontoname,pref);
	}
	public void test_11SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_11";
		test_function(ontoname,pref);
	}
	public void test_12SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_12";
		test_function(ontoname,pref);
	}
	public void test_13SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_13";
		test_function(ontoname,pref);
	}
	public void test_14SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_14";
		test_function(ontoname,pref);
	}
	public void test_15SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_15";
		test_function(ontoname,pref);
	}
	public void test_16SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_16";
		test_function(ontoname,pref);
	}
	public void test_17SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_17";
		test_function(ontoname,pref);
	}
	public void test_18SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_18";
		test_function(ontoname,pref);
	}
	public void test_19SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_19";
		test_function(ontoname,pref);
	}
	public void test_20SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_20";
		test_function(ontoname,pref);
	}
	public void test_21SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_21";
		test_function(ontoname,pref);
	}
	public void test_22SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_22";
		test_function(ontoname,pref);
	}
	public void test_23SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_23";
		test_function(ontoname,pref);
	}
	public void test_24SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_24";
		test_function(ontoname,pref);
	}
	public void test_25SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_25";
		test_function(ontoname,pref);
	}
	public void test_26SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_26";
		test_function(ontoname,pref);
	}
	public void test_27SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_27";
		test_function(ontoname,pref);
	}
	public void test_28SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_28";
		test_function(ontoname,pref);
	}
	public void test_29SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_29";
		test_function(ontoname,pref);
	}
	public void test_30SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_30";
		test_function(ontoname,pref);
	}
	public void test_31SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_31";
		test_function(ontoname,pref);
	}
	public void test_32SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_32";
		test_function(ontoname,pref);
	}
	public void test_33SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_33";
		test_function(ontoname,pref);
	}
	public void test_34SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_34";
		test_function(ontoname,pref);
	}
	public void test_35SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_35";
		test_function(ontoname,pref);
	}
	public void test_36SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_36";
		test_function(ontoname,pref);
	}
	public void test_37SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_37";
		test_function(ontoname,pref);
	}
	public void test_38SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_38";
		test_function(ontoname,pref);
	}
	public void test_39SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_39";
		test_function(ontoname,pref);
	}
	public void test_40SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_40";
		test_function(ontoname,pref);
	}
	public void test_41SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_41";
		test_function(ontoname,pref);
	}
	public void test_42SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_42";
		test_function(ontoname,pref);
	}
	public void test_43SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_43";
		test_function(ontoname,pref);
	}
	public void test_44SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_44";
		test_function(ontoname,pref);
	}
	public void test_45SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_45";
		test_function(ontoname,pref);
	}
	public void test_46SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_46";
		test_function(ontoname,pref);
	}
	public void test_47SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_47";
		test_function(ontoname,pref);
	}
	public void test_48SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_48";
		test_function(ontoname,pref);
	}
	public void test_49SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_49";
		test_function(ontoname,pref);
	}
	public void test_50SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_50";
		test_function(ontoname,pref);
	}
	public void test_51SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_51";
		test_function(ontoname,pref);
	}
	public void test_52SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_52";
		test_function(ontoname,pref);
	}
	public void test_53SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_53";
		test_function(ontoname,pref);
	}
	public void test_54SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_54";
		test_function(ontoname,pref);
	}
	public void test_55SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_55";
		test_function(ontoname,pref);
	}
	public void test_56SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_56";
		test_function(ontoname,pref);
	}
	public void test_57SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_57";
		test_function(ontoname,pref);
	}
	public void test_58SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_58";
		test_function(ontoname,pref);
	}
	public void test_59SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_59";
		test_function(ontoname,pref);
	}
	public void test_60SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_60";
		test_function(ontoname,pref);
	}
	public void test_61SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_61";
		test_function(ontoname,pref);
	}
	public void test_62SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_62";
		test_function(ontoname,pref);
	}
	public void test_63SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_63";
		test_function(ontoname,pref);
	}
	public void test_64SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_64";
		test_function(ontoname,pref);
	}
	public void test_65SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_65";
		test_function(ontoname,pref);
	}
	public void test_66SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_66";
		test_function(ontoname,pref);
	}
	public void test_67SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_67";
		test_function(ontoname,pref);
	}
	public void test_68SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_68";
		test_function(ontoname,pref);
	}
	public void test_69SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_69";
		test_function(ontoname,pref);
	}
	public void test_70SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_70";
		test_function(ontoname,pref);
	}
	public void test_71SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_71";
		test_function(ontoname,pref);
	}
	public void test_72SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_72";
		test_function(ontoname,pref);
	}
	public void test_73SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_73";
		test_function(ontoname,pref);
	}
	public void test_74SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_74";
		test_function(ontoname,pref);
	}
	public void test_75SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_75";
		test_function(ontoname,pref);
	}
	public void test_76SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_76";
		test_function(ontoname,pref);
	}
	public void test_77SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_77";
		test_function(ontoname,pref);
	}
	public void test_78SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_78";
		test_function(ontoname,pref);
	}
	public void test_79SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_79";
		test_function(ontoname,pref);
	}
	public void test_80SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_80";
		test_function(ontoname,pref);
	}
	public void test_81SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_81";
		test_function(ontoname,pref);
	}
	public void test_82SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_82";
		test_function(ontoname,pref);
	}
	public void test_83SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_83";
		test_function(ontoname,pref);
	}
	public void test_84SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_84";
		test_function(ontoname,pref);
	}
	public void test_85SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_85";
		test_function(ontoname,pref);
	}
	public void test_86SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_86";
		test_function(ontoname,pref);
	}
	public void test_87SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_87";
		test_function(ontoname,pref);
	}
	public void test_88SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_88";
		test_function(ontoname,pref);
	}
	public void test_89SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_89";
		test_function(ontoname,pref);
	}
	public void test_90SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_90";
		test_function(ontoname,pref);
	}
	public void test_200SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_200";
		test_function(ontoname,pref);
	}
	public void test_201SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_201";
		test_function(ontoname,pref);
	}
	public void test_210SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_210";
		test_function(ontoname,pref);
	}
	public void test_401SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_401";
		test_function(ontoname,pref);
	}
	public void test_402SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_402";
		test_function(ontoname,pref);
	}
	public void test_403SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_403";
		test_function(ontoname,pref);
	}
	public void test_404SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_404";
		test_function(ontoname,pref);
	}
	public void test_405SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_405";
		test_function(ontoname,pref);
	}
	public void test_190SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_190";
		test_function(ontoname,pref);
	}
	public void test_191SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_191";
		test_function(ontoname,pref);
	}
	public void test_192SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_192";
		test_function(ontoname,pref);
	}
	public void test_193SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_193";
		test_function(ontoname,pref);
	}
	public void test_194SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_194";
		test_function(ontoname,pref);
	}
	public void test_195SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_195";
		test_function(ontoname,pref);
	}
	public void test_196SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_196";
		test_function(ontoname,pref);
	}
	public void test_197SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_197";
		test_function(ontoname,pref);
	}
	public void test_198SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_198";
		test_function(ontoname,pref);
	}
	public void test_500SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_500";
		test_function(ontoname,pref);
	}
	public void test_501SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_501";
		test_function(ontoname,pref);
	}
	public void test_502SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_502";
		test_function(ontoname,pref);
	}
	public void test_503SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_503";
		test_function(ontoname,pref);
	}
	public void test_504SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_504";
		test_function(ontoname,pref);
	}
	public void test_505SIEqNoSig() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_505";
		test_function(ontoname,pref);
	}
	public void test_600SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_600";
		test_function(ontoname,pref);
	}
	public void test_1SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_1";
		test_function(ontoname,pref);
	}
	public void test_2SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_2";
		test_function(ontoname,pref);
	}
	public void test_3SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_3";
		test_function(ontoname,pref);
	}
	public void test_4SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_4";
		test_function(ontoname,pref);
	}
	public void test_5SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_5";
		test_function(ontoname,pref);
	}
	public void test_6SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_6";
		test_function(ontoname,pref);
	}
	public void test_7SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_7";
		test_function(ontoname,pref);
	}
	public void test_8SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_8";
		test_function(ontoname,pref);
	}
	public void test_9SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_9";
		test_function(ontoname,pref);
	}
	public void test_10SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_10";
		test_function(ontoname,pref);
	}
	public void test_11SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_11";
		test_function(ontoname,pref);
	}
	public void test_12SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_12";
		test_function(ontoname,pref);
	}
	public void test_13SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_13";
		test_function(ontoname,pref);
	}
	public void test_14SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_14";
		test_function(ontoname,pref);
	}
	public void test_15SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_15";
		test_function(ontoname,pref);
	}
	public void test_16SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_16";
		test_function(ontoname,pref);
	}
	public void test_17SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_17";
		test_function(ontoname,pref);
	}
	public void test_18SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_18";
		test_function(ontoname,pref);
	}
	public void test_19SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_19";
		test_function(ontoname,pref);
	}
	public void test_20SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_20";
		test_function(ontoname,pref);
	}
	public void test_21SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_21";
		test_function(ontoname,pref);
	}
	public void test_22SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_22";
		test_function(ontoname,pref);
	}
	public void test_23SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_23";
		test_function(ontoname,pref);
	}
	public void test_24SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_24";
		test_function(ontoname,pref);
	}
	public void test_25SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_25";
		test_function(ontoname,pref);
	}
	public void test_26SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_26";
		test_function(ontoname,pref);
	}
	public void test_27SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_27";
		test_function(ontoname,pref);
	}
	public void test_28SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_28";
		test_function(ontoname,pref);
	}
	public void test_29SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_29";
		test_function(ontoname,pref);
	}
	public void test_30SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_30";
		test_function(ontoname,pref);
	}
	public void test_31SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_31";
		test_function(ontoname,pref);
	}
	public void test_32SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_32";
		test_function(ontoname,pref);
	}
	public void test_33SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_33";
		test_function(ontoname,pref);
	}
	public void test_34SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_34";
		test_function(ontoname,pref);
	}
	public void test_35SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_35";
		test_function(ontoname,pref);
	}
	public void test_36SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_36";
		test_function(ontoname,pref);
	}
	public void test_37SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_37";
		test_function(ontoname,pref);
	}
	public void test_38SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_38";
		test_function(ontoname,pref);
	}
	public void test_39SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_39";
		test_function(ontoname,pref);
	}
	public void test_40SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_40";
		test_function(ontoname,pref);
	}
	public void test_41SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_41";
		test_function(ontoname,pref);
	}
	public void test_42SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_42";
		test_function(ontoname,pref);
	}
	public void test_43SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_43";
		test_function(ontoname,pref);
	}
	public void test_44SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_44";
		test_function(ontoname,pref);
	}
	public void test_45SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_45";
		test_function(ontoname,pref);
	}
	public void test_46SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_46";
		test_function(ontoname,pref);
	}
	public void test_47SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_47";
		test_function(ontoname,pref);
	}
	public void test_48SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_48";
		test_function(ontoname,pref);
	}
	public void test_49SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_49";
		test_function(ontoname,pref);
	}
	public void test_50SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_50";
		test_function(ontoname,pref);
	}
	public void test_51SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_51";
		test_function(ontoname,pref);
	}
	public void test_52SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_52";
		test_function(ontoname,pref);
	}
	public void test_53SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_53";
		test_function(ontoname,pref);
	}
	public void test_54SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_54";
		test_function(ontoname,pref);
	}
	public void test_55SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_55";
		test_function(ontoname,pref);
	}
	public void test_56SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_56";
		test_function(ontoname,pref);
	}
	public void test_57SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_57";
		test_function(ontoname,pref);
	}
	public void test_58SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_58";
		test_function(ontoname,pref);
	}
	public void test_59SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_59";
		test_function(ontoname,pref);
	}
	public void test_60SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_60";
		test_function(ontoname,pref);
	}
	public void test_61SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_61";
		test_function(ontoname,pref);
	}
	public void test_62SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_62";
		test_function(ontoname,pref);
	}
	public void test_63SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_63";
		test_function(ontoname,pref);
	}
	public void test_64SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_64";
		test_function(ontoname,pref);
	}
	public void test_65SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_65";
		test_function(ontoname,pref);
	}
	public void test_66SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_66";
		test_function(ontoname,pref);
	}
	public void test_67SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_67";
		test_function(ontoname,pref);
	}
	public void test_68SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_68";
		test_function(ontoname,pref);
	}
	public void test_69SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_69";
		test_function(ontoname,pref);
	}
	public void test_70SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_70";
		test_function(ontoname,pref);
	}
	public void test_71SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_71";
		test_function(ontoname,pref);
	}
	public void test_72SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_72";
		test_function(ontoname,pref);
	}
	public void test_73SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_73";
		test_function(ontoname,pref);
	}
	public void test_74SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_74";
		test_function(ontoname,pref);
	}
	public void test_75SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_75";
		test_function(ontoname,pref);
	}
	public void test_76SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_76";
		test_function(ontoname,pref);
	}
	public void test_77SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_77";
		test_function(ontoname,pref);
	}
	public void test_78SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_78";
		test_function(ontoname,pref);
	}
	public void test_79SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_79";
		test_function(ontoname,pref);
	}
	public void test_80SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_80";
		test_function(ontoname,pref);
	}
	public void test_81SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_81";
		test_function(ontoname,pref);
	}
	public void test_82SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_82";
		test_function(ontoname,pref);
	}
	public void test_83SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_83";
		test_function(ontoname,pref);
	}
	public void test_84SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_84";
		test_function(ontoname,pref);
	}
	public void test_85SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_85";
		test_function(ontoname,pref);
	}
	public void test_86SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_86";
		test_function(ontoname,pref);
	}
	public void test_87SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_87";
		test_function(ontoname,pref);
	}
	public void test_88SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_88";
		test_function(ontoname,pref);
	}
	public void test_89SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_89";
		test_function(ontoname,pref);
	}
	public void test_90SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_90";
		test_function(ontoname,pref);
	}
	public void test_200SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_200";
		test_function(ontoname,pref);
	}
	public void test_201SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_201";
		test_function(ontoname,pref);
	}
	public void test_210SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_210";
		test_function(ontoname,pref);
	}
	public void test_401SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_401";
		test_function(ontoname,pref);
	}
	public void test_402SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_402";
		test_function(ontoname,pref);
	}
	public void test_403SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_403";
		test_function(ontoname,pref);
	}
	public void test_404SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_404";
		test_function(ontoname,pref);
	}
	public void test_405SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_405";
		test_function(ontoname,pref);
	}
	public void test_190SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_190";
		test_function(ontoname,pref);
	}
	public void test_191SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_191";
		test_function(ontoname,pref);
	}
	public void test_192SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_192";
		test_function(ontoname,pref);
	}
	public void test_193SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_193";
		test_function(ontoname,pref);
	}
	public void test_194SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_194";
		test_function(ontoname,pref);
	}
	public void test_195SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_195";
		test_function(ontoname,pref);
	}
	public void test_196SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_196";
		test_function(ontoname,pref);
	}
	public void test_197SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_197";
		test_function(ontoname,pref);
	}
	public void test_198SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_198";
		test_function(ontoname,pref);
	}
	public void test_500SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_500";
		test_function(ontoname,pref);
	}
	public void test_501SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_501";
		test_function(ontoname,pref);
	}
	public void test_502SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_502";
		test_function(ontoname,pref);
	}
	public void test_503SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_503";
		test_function(ontoname,pref);
	}
	public void test_504SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_504";
		test_function(ontoname,pref);
	}
	public void test_505SINoEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_505";
		test_function(ontoname,pref);
	}
	public void test_600SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_600";
		test_function(ontoname,pref);
	}
	public void test_1SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_1";
		test_function(ontoname,pref);
	}
	public void test_2SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_2";
		test_function(ontoname,pref);
	}
	public void test_3SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_3";
		test_function(ontoname,pref);
	}
	public void test_4SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_4";
		test_function(ontoname,pref);
	}
	public void test_5SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_5";
		test_function(ontoname,pref);
	}
	public void test_6SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_6";
		test_function(ontoname,pref);
	}
	public void test_7SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_7";
		test_function(ontoname,pref);
	}
	public void test_8SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_8";
		test_function(ontoname,pref);
	}
	public void test_9SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_9";
		test_function(ontoname,pref);
	}
	public void test_10SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_10";
		test_function(ontoname,pref);
	}
	public void test_11SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_11";
		test_function(ontoname,pref);
	}
	public void test_12SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_12";
		test_function(ontoname,pref);
	}
	public void test_13SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_13";
		test_function(ontoname,pref);
	}
	public void test_14SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_14";
		test_function(ontoname,pref);
	}
	public void test_15SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_15";
		test_function(ontoname,pref);
	}
	public void test_16SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_16";
		test_function(ontoname,pref);
	}
	public void test_17SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_17";
		test_function(ontoname,pref);
	}
	public void test_18SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_18";
		test_function(ontoname,pref);
	}
	public void test_19SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_19";
		test_function(ontoname,pref);
	}
	public void test_20SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_20";
		test_function(ontoname,pref);
	}
	public void test_21SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_21";
		test_function(ontoname,pref);
	}
	public void test_22SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_22";
		test_function(ontoname,pref);
	}
	public void test_23SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_23";
		test_function(ontoname,pref);
	}
	public void test_24SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_24";
		test_function(ontoname,pref);
	}
	public void test_25SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_25";
		test_function(ontoname,pref);
	}
	public void test_26SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_26";
		test_function(ontoname,pref);
	}
	public void test_27SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_27";
		test_function(ontoname,pref);
	}
	public void test_28SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_28";
		test_function(ontoname,pref);
	}
	public void test_29SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_29";
		test_function(ontoname,pref);
	}
	public void test_30SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_30";
		test_function(ontoname,pref);
	}
	public void test_31SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_31";
		test_function(ontoname,pref);
	}
	public void test_32SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_32";
		test_function(ontoname,pref);
	}
	public void test_33SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_33";
		test_function(ontoname,pref);
	}
	public void test_34SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_34";
		test_function(ontoname,pref);
	}
	public void test_35SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_35";
		test_function(ontoname,pref);
	}
	public void test_36SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_36";
		test_function(ontoname,pref);
	}
	public void test_37SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_37";
		test_function(ontoname,pref);
	}
	public void test_38SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_38";
		test_function(ontoname,pref);
	}
	public void test_39SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_39";
		test_function(ontoname,pref);
	}
	public void test_40SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_40";
		test_function(ontoname,pref);
	}
	public void test_41SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_41";
		test_function(ontoname,pref);
	}
	public void test_42SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_42";
		test_function(ontoname,pref);
	}
	public void test_43SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_43";
		test_function(ontoname,pref);
	}
	public void test_44SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_44";
		test_function(ontoname,pref);
	}
	public void test_45SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_45";
		test_function(ontoname,pref);
	}
	public void test_46SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_46";
		test_function(ontoname,pref);
	}
	public void test_47SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_47";
		test_function(ontoname,pref);
	}
	public void test_48SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_48";
		test_function(ontoname,pref);
	}
	public void test_49SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_49";
		test_function(ontoname,pref);
	}
	public void test_50SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_50";
		test_function(ontoname,pref);
	}
	public void test_51SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_51";
		test_function(ontoname,pref);
	}
	public void test_52SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_52";
		test_function(ontoname,pref);
	}
	public void test_53SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_53";
		test_function(ontoname,pref);
	}
	public void test_54SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_54";
		test_function(ontoname,pref);
	}
	public void test_55SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_55";
		test_function(ontoname,pref);
	}
	public void test_56SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_56";
		test_function(ontoname,pref);
	}
	public void test_57SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_57";
		test_function(ontoname,pref);
	}
	public void test_58SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_58";
		test_function(ontoname,pref);
	}
	public void test_59SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_59";
		test_function(ontoname,pref);
	}
	public void test_60SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_60";
		test_function(ontoname,pref);
	}
	public void test_61SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_61";
		test_function(ontoname,pref);
	}
	public void test_62SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_62";
		test_function(ontoname,pref);
	}
	public void test_63SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_63";
		test_function(ontoname,pref);
	}
	public void test_64SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_64";
		test_function(ontoname,pref);
	}
	public void test_65SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_65";
		test_function(ontoname,pref);
	}
	public void test_66SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_66";
		test_function(ontoname,pref);
	}
	public void test_67SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_67";
		test_function(ontoname,pref);
	}
	public void test_68SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_68";
		test_function(ontoname,pref);
	}
	public void test_69SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_69";
		test_function(ontoname,pref);
	}
	public void test_70SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_70";
		test_function(ontoname,pref);
	}
	public void test_71SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_71";
		test_function(ontoname,pref);
	}
	public void test_72SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_72";
		test_function(ontoname,pref);
	}
	public void test_73SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_73";
		test_function(ontoname,pref);
	}
	public void test_74SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_74";
		test_function(ontoname,pref);
	}
	public void test_75SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_75";
		test_function(ontoname,pref);
	}
	public void test_76SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_76";
		test_function(ontoname,pref);
	}
	public void test_77SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_77";
		test_function(ontoname,pref);
	}
	public void test_78SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_78";
		test_function(ontoname,pref);
	}
	public void test_79SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_79";
		test_function(ontoname,pref);
	}
	public void test_80SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_80";
		test_function(ontoname,pref);
	}
	public void test_81SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_81";
		test_function(ontoname,pref);
	}
	public void test_82SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_82";
		test_function(ontoname,pref);
	}
	public void test_83SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_83";
		test_function(ontoname,pref);
	}
	public void test_84SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_84";
		test_function(ontoname,pref);
	}
	public void test_85SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_85";
		test_function(ontoname,pref);
	}
	public void test_86SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_86";
		test_function(ontoname,pref);
	}
	public void test_87SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_87";
		test_function(ontoname,pref);
	}
	public void test_88SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_88";
		test_function(ontoname,pref);
	}
	public void test_89SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_89";
		test_function(ontoname,pref);
	}
	public void test_90SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_90";
		test_function(ontoname,pref);
	}
	public void test_200SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_200";
		test_function(ontoname,pref);
	}
	public void test_201SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_201";
		test_function(ontoname,pref);
	}
	public void test_210SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_210";
		test_function(ontoname,pref);
	}
	public void test_401SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_401";
		test_function(ontoname,pref);
	}
	public void test_402SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_402";
		test_function(ontoname,pref);
	}
	public void test_403SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_403";
		test_function(ontoname,pref);
	}
	public void test_404SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_404";
		test_function(ontoname,pref);
	}
	public void test_405SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_405";
		test_function(ontoname,pref);
	}
	public void test_190SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_190";
		test_function(ontoname,pref);
	}
	public void test_191SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_191";
		test_function(ontoname,pref);
	}
	public void test_192SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_192";
		test_function(ontoname,pref);
	}
	public void test_193SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_193";
		test_function(ontoname,pref);
	}
	public void test_194SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_194";
		test_function(ontoname,pref);
	}
	public void test_195SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_195";
		test_function(ontoname,pref);
	}
	public void test_196SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_196";
		test_function(ontoname,pref);
	}
	public void test_197SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_197";
		test_function(ontoname,pref);
	}
	public void test_198SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_198";
		test_function(ontoname,pref);
	}
	public void test_500SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_500";
		test_function(ontoname,pref);
	}
	public void test_501SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_501";
		test_function(ontoname,pref);
	}
	public void test_502SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_502";
		test_function(ontoname,pref);
	}
	public void test_503SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_503";
		test_function(ontoname,pref);
	}
	public void test_504SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_504";
		test_function(ontoname,pref);
	}
	public void test_505SIEqSigma() throws Exception {
QuestPreferences pref = new QuestPreferences();
pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_505";
		test_function(ontoname,pref);
	}
}
