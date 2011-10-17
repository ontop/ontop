package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;

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

	private void test_function(String ontoname, ReformulationPlatformPreferences pref) throws Exception {
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

	public void test_600DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_600";
		test_function(ontoname, pref);
	}

	public void test_1DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_1";
		test_function(ontoname, pref);
	}

	public void test_2DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_2";
		test_function(ontoname, pref);
	}

	public void test_3DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_3";
		test_function(ontoname, pref);
	}

	public void test_4DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_4";
		test_function(ontoname, pref);
	}

	public void test_5DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_5";
		test_function(ontoname, pref);
	}

	public void test_6DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_6";
		test_function(ontoname, pref);
	}

	public void test_7DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_7";
		test_function(ontoname, pref);
	}

	public void test_8DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_8";
		test_function(ontoname, pref);
	}

	public void test_9DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_9";
		test_function(ontoname, pref);
	}

	public void test_10DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_10";
		test_function(ontoname, pref);
	}

	public void test_11DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_11";
		test_function(ontoname, pref);
	}

	public void test_12DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_12";
		test_function(ontoname, pref);
	}

	public void test_13DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_13";
		test_function(ontoname, pref);
	}

	public void test_14DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_14";
		test_function(ontoname, pref);
	}

	public void test_15DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_15";
		test_function(ontoname, pref);
	}

	public void test_16DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_16";
		test_function(ontoname, pref);
	}

	public void test_17DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_17";
		test_function(ontoname, pref);
	}

	public void test_18DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_18";
		test_function(ontoname, pref);
	}

	public void test_19DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_19";
		test_function(ontoname, pref);
	}

	public void test_20DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_20";
		test_function(ontoname, pref);
	}

	public void test_21DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_21";
		test_function(ontoname, pref);
	}

	public void test_22DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_22";
		test_function(ontoname, pref);
	}

	public void test_23DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_23";
		test_function(ontoname, pref);
	}

	public void test_24DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_24";
		test_function(ontoname, pref);
	}

	public void test_25DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_25";
		test_function(ontoname, pref);
	}

	public void test_26DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_26";
		test_function(ontoname, pref);
	}

	public void test_27DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_27";
		test_function(ontoname, pref);
	}

	public void test_28DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_28";
		test_function(ontoname, pref);
	}

	public void test_29DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_29";
		test_function(ontoname, pref);
	}

	public void test_30DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_30";
		test_function(ontoname, pref);
	}

	public void test_31DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_31";
		test_function(ontoname, pref);
	}

	public void test_32DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_32";
		test_function(ontoname, pref);
	}

	public void test_33DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_33";
		test_function(ontoname, pref);
	}

	public void test_34DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_34";
		test_function(ontoname, pref);
	}

	public void test_35DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_35";
		test_function(ontoname, pref);
	}

	public void test_36DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_36";
		test_function(ontoname, pref);
	}

	public void test_37DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_37";
		test_function(ontoname, pref);
	}

	public void test_38DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_38";
		test_function(ontoname, pref);
	}

	public void test_39DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_39";
		test_function(ontoname, pref);
	}

	public void test_40DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_40";
		test_function(ontoname, pref);
	}

	public void test_41DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_41";
		test_function(ontoname, pref);
	}

	public void test_42DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_42";
		test_function(ontoname, pref);
	}

	public void test_43DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_43";
		test_function(ontoname, pref);
	}

	public void test_44DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_44";
		test_function(ontoname, pref);
	}

	public void test_45DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_45";
		test_function(ontoname, pref);
	}

	public void test_46DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_46";
		test_function(ontoname, pref);
	}

	public void test_47DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_47";
		test_function(ontoname, pref);
	}

	public void test_48DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_48";
		test_function(ontoname, pref);
	}

	public void test_49DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_49";
		test_function(ontoname, pref);
	}

	public void test_50DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_50";
		test_function(ontoname, pref);
	}

	public void test_51DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_51";
		test_function(ontoname, pref);
	}

	public void test_52DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_52";
		test_function(ontoname, pref);
	}

	public void test_53DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_53";
		test_function(ontoname, pref);
	}

	public void test_54DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_54";
		test_function(ontoname, pref);
	}

	public void test_55DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_55";
		test_function(ontoname, pref);
	}

	public void test_56DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_56";
		test_function(ontoname, pref);
	}

	public void test_57DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_57";
		test_function(ontoname, pref);
	}

	public void test_58DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_58";
		test_function(ontoname, pref);
	}

	public void test_59DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_59";
		test_function(ontoname, pref);
	}

	public void test_60DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_60";
		test_function(ontoname, pref);
	}

	public void test_61DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_61";
		test_function(ontoname, pref);
	}

	public void test_62DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_62";
		test_function(ontoname, pref);
	}

	public void test_63DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_63";
		test_function(ontoname, pref);
	}

	public void test_64DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_64";
		test_function(ontoname, pref);
	}

	public void test_65DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_65";
		test_function(ontoname, pref);
	}

	public void test_66DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_66";
		test_function(ontoname, pref);
	}

	public void test_67DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_67";
		test_function(ontoname, pref);
	}

	public void test_68DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_68";
		test_function(ontoname, pref);
	}

	public void test_69DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_69";
		test_function(ontoname, pref);
	}

	public void test_70DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_70";
		test_function(ontoname, pref);
	}

	public void test_71DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_71";
		test_function(ontoname, pref);
	}

	public void test_72DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_72";
		test_function(ontoname, pref);
	}

	public void test_73DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_73";
		test_function(ontoname, pref);
	}

	public void test_74DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_74";
		test_function(ontoname, pref);
	}

	public void test_75DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_75";
		test_function(ontoname, pref);
	}

	public void test_76DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_76";
		test_function(ontoname, pref);
	}

	public void test_77DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_77";
		test_function(ontoname, pref);
	}

	public void test_78DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_78";
		test_function(ontoname, pref);
	}

	public void test_79DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_79";
		test_function(ontoname, pref);
	}

	public void test_80DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_80";
		test_function(ontoname, pref);
	}

	public void test_81DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_81";
		test_function(ontoname, pref);
	}

	public void test_82DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_82";
		test_function(ontoname, pref);
	}

	public void test_83DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_83";
		test_function(ontoname, pref);
	}

	public void test_84DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_84";
		test_function(ontoname, pref);
	}

	public void test_85DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_85";
		test_function(ontoname, pref);
	}

	public void test_86DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_86";
		test_function(ontoname, pref);
	}

	public void test_87DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_87";
		test_function(ontoname, pref);
	}

	public void test_88DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_88";
		test_function(ontoname, pref);
	}

	public void test_89DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_89";
		test_function(ontoname, pref);
	}

	public void test_90DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_90";
		test_function(ontoname, pref);
	}

	public void test_200DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_200";
		test_function(ontoname, pref);
	}

	public void test_201DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_201";
		test_function(ontoname, pref);
	}

	public void test_210DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_210";
		test_function(ontoname, pref);
	}

	public void test_401DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_401";
		test_function(ontoname, pref);
	}

	public void test_402DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_402";
		test_function(ontoname, pref);
	}

	public void test_403DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_403";
		test_function(ontoname, pref);
	}

	public void test_404DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_404";
		test_function(ontoname, pref);
	}

	public void test_405DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_405";
		test_function(ontoname, pref);
	}

	public void test_190DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_190";
		test_function(ontoname, pref);
	}

	public void test_191DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_191";
		test_function(ontoname, pref);
	}

	public void test_192DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_192";
		test_function(ontoname, pref);
	}

	public void test_193DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_193";
		test_function(ontoname, pref);
	}

	public void test_194DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_194";
		test_function(ontoname, pref);
	}

	public void test_195DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_195";
		test_function(ontoname, pref);
	}

	public void test_196DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_196";
		test_function(ontoname, pref);
	}

	public void test_197DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_197";
		test_function(ontoname, pref);
	}

	public void test_198DirectNoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_198";
		test_function(ontoname, pref);
	}

	public void test_600DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_600";
		test_function(ontoname, pref);
	}

	public void test_1DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_1";
		test_function(ontoname, pref);
	}

	public void test_2DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_2";
		test_function(ontoname, pref);
	}

	public void test_3DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_3";
		test_function(ontoname, pref);
	}

	public void test_4DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_4";
		test_function(ontoname, pref);
	}

	public void test_5DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_5";
		test_function(ontoname, pref);
	}

	public void test_6DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_6";
		test_function(ontoname, pref);
	}

	public void test_7DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_7";
		test_function(ontoname, pref);
	}

	public void test_8DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_8";
		test_function(ontoname, pref);
	}

	public void test_9DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_9";
		test_function(ontoname, pref);
	}

	public void test_10DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_10";
		test_function(ontoname, pref);
	}

	public void test_11DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_11";
		test_function(ontoname, pref);
	}

	public void test_12DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_12";
		test_function(ontoname, pref);
	}

	public void test_13DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_13";
		test_function(ontoname, pref);
	}

	public void test_14DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_14";
		test_function(ontoname, pref);
	}

	public void test_15DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_15";
		test_function(ontoname, pref);
	}

	public void test_16DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_16";
		test_function(ontoname, pref);
	}

	public void test_17DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_17";
		test_function(ontoname, pref);
	}

	public void test_18DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_18";
		test_function(ontoname, pref);
	}

	public void test_19DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_19";
		test_function(ontoname, pref);
	}

	public void test_20DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_20";
		test_function(ontoname, pref);
	}

	public void test_21DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_21";
		test_function(ontoname, pref);
	}

	public void test_22DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_22";
		test_function(ontoname, pref);
	}

	public void test_23DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_23";
		test_function(ontoname, pref);
	}

	public void test_24DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_24";
		test_function(ontoname, pref);
	}

	public void test_25DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_25";
		test_function(ontoname, pref);
	}

	public void test_26DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_26";
		test_function(ontoname, pref);
	}

	public void test_27DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_27";
		test_function(ontoname, pref);
	}

	public void test_28DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_28";
		test_function(ontoname, pref);
	}

	public void test_29DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_29";
		test_function(ontoname, pref);
	}

	public void test_30DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_30";
		test_function(ontoname, pref);
	}

	public void test_31DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_31";
		test_function(ontoname, pref);
	}

	public void test_32DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_32";
		test_function(ontoname, pref);
	}

	public void test_33DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_33";
		test_function(ontoname, pref);
	}

	public void test_34DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_34";
		test_function(ontoname, pref);
	}

	public void test_35DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_35";
		test_function(ontoname, pref);
	}

	public void test_36DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_36";
		test_function(ontoname, pref);
	}

	public void test_37DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_37";
		test_function(ontoname, pref);
	}

	public void test_38DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_38";
		test_function(ontoname, pref);
	}

	public void test_39DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_39";
		test_function(ontoname, pref);
	}

	public void test_40DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_40";
		test_function(ontoname, pref);
	}

	public void test_41DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_41";
		test_function(ontoname, pref);
	}

	public void test_42DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_42";
		test_function(ontoname, pref);
	}

	public void test_43DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_43";
		test_function(ontoname, pref);
	}

	public void test_44DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_44";
		test_function(ontoname, pref);
	}

	public void test_45DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_45";
		test_function(ontoname, pref);
	}

	public void test_46DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_46";
		test_function(ontoname, pref);
	}

	public void test_47DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_47";
		test_function(ontoname, pref);
	}

	public void test_48DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_48";
		test_function(ontoname, pref);
	}

	public void test_49DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_49";
		test_function(ontoname, pref);
	}

	public void test_50DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_50";
		test_function(ontoname, pref);
	}

	public void test_51DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_51";
		test_function(ontoname, pref);
	}

	public void test_52DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_52";
		test_function(ontoname, pref);
	}

	public void test_53DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_53";
		test_function(ontoname, pref);
	}

	public void test_54DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_54";
		test_function(ontoname, pref);
	}

	public void test_55DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_55";
		test_function(ontoname, pref);
	}

	public void test_56DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_56";
		test_function(ontoname, pref);
	}

	public void test_57DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_57";
		test_function(ontoname, pref);
	}

	public void test_58DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_58";
		test_function(ontoname, pref);
	}

	public void test_59DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_59";
		test_function(ontoname, pref);
	}

	public void test_60DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_60";
		test_function(ontoname, pref);
	}

	public void test_61DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_61";
		test_function(ontoname, pref);
	}

	public void test_62DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_62";
		test_function(ontoname, pref);
	}

	public void test_63DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_63";
		test_function(ontoname, pref);
	}

	public void test_64DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_64";
		test_function(ontoname, pref);
	}

	public void test_65DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_65";
		test_function(ontoname, pref);
	}

	public void test_66DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_66";
		test_function(ontoname, pref);
	}

	public void test_67DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_67";
		test_function(ontoname, pref);
	}

	public void test_68DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_68";
		test_function(ontoname, pref);
	}

	public void test_69DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_69";
		test_function(ontoname, pref);
	}

	public void test_70DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_70";
		test_function(ontoname, pref);
	}

	public void test_71DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_71";
		test_function(ontoname, pref);
	}

	public void test_72DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_72";
		test_function(ontoname, pref);
	}

	public void test_73DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_73";
		test_function(ontoname, pref);
	}

	public void test_74DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_74";
		test_function(ontoname, pref);
	}

	public void test_75DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_75";
		test_function(ontoname, pref);
	}

	public void test_76DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_76";
		test_function(ontoname, pref);
	}

	public void test_77DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_77";
		test_function(ontoname, pref);
	}

	public void test_78DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_78";
		test_function(ontoname, pref);
	}

	public void test_79DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_79";
		test_function(ontoname, pref);
	}

	public void test_80DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_80";
		test_function(ontoname, pref);
	}

	public void test_81DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_81";
		test_function(ontoname, pref);
	}

	public void test_82DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_82";
		test_function(ontoname, pref);
	}

	public void test_83DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_83";
		test_function(ontoname, pref);
	}

	public void test_84DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_84";
		test_function(ontoname, pref);
	}

	public void test_85DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_85";
		test_function(ontoname, pref);
	}

	public void test_86DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_86";
		test_function(ontoname, pref);
	}

	public void test_87DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_87";
		test_function(ontoname, pref);
	}

	public void test_88DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_88";
		test_function(ontoname, pref);
	}

	public void test_89DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_89";
		test_function(ontoname, pref);
	}

	public void test_90DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_90";
		test_function(ontoname, pref);
	}

	public void test_200DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_200";
		test_function(ontoname, pref);
	}

	public void test_201DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_201";
		test_function(ontoname, pref);
	}

	public void test_210DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_210";
		test_function(ontoname, pref);
	}

	public void test_401DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_401";
		test_function(ontoname, pref);
	}

	public void test_402DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_402";
		test_function(ontoname, pref);
	}

	public void test_403DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_403";
		test_function(ontoname, pref);
	}

	public void test_404DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_404";
		test_function(ontoname, pref);
	}

	public void test_405DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_405";
		test_function(ontoname, pref);
	}

	public void test_190DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_190";
		test_function(ontoname, pref);
	}

	public void test_191DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_191";
		test_function(ontoname, pref);
	}

	public void test_192DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_192";
		test_function(ontoname, pref);
	}

	public void test_193DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_193";
		test_function(ontoname, pref);
	}

	public void test_194DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_194";
		test_function(ontoname, pref);
	}

	public void test_195DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_195";
		test_function(ontoname, pref);
	}

	public void test_196DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_196";
		test_function(ontoname, pref);
	}

	public void test_197DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_197";
		test_function(ontoname, pref);
	}

	public void test_198DirectEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_198";
		test_function(ontoname, pref);
	}

	public void test_600DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_600";
		test_function(ontoname, pref);
	}

	public void test_1DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_1";
		test_function(ontoname, pref);
	}

	public void test_2DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_2";
		test_function(ontoname, pref);
	}

	public void test_3DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_3";
		test_function(ontoname, pref);
	}

	public void test_4DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_4";
		test_function(ontoname, pref);
	}

	public void test_5DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_5";
		test_function(ontoname, pref);
	}

	public void test_6DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_6";
		test_function(ontoname, pref);
	}

	public void test_7DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_7";
		test_function(ontoname, pref);
	}

	public void test_8DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_8";
		test_function(ontoname, pref);
	}

	public void test_9DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_9";
		test_function(ontoname, pref);
	}

	public void test_10DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_10";
		test_function(ontoname, pref);
	}

	public void test_11DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_11";
		test_function(ontoname, pref);
	}

	public void test_12DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_12";
		test_function(ontoname, pref);
	}

	public void test_13DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_13";
		test_function(ontoname, pref);
	}

	public void test_14DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_14";
		test_function(ontoname, pref);
	}

	public void test_15DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_15";
		test_function(ontoname, pref);
	}

	public void test_16DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_16";
		test_function(ontoname, pref);
	}

	public void test_17DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_17";
		test_function(ontoname, pref);
	}

	public void test_18DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_18";
		test_function(ontoname, pref);
	}

	public void test_19DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_19";
		test_function(ontoname, pref);
	}

	public void test_20DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_20";
		test_function(ontoname, pref);
	}

	public void test_21DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_21";
		test_function(ontoname, pref);
	}

	public void test_22DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_22";
		test_function(ontoname, pref);
	}

	public void test_23DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_23";
		test_function(ontoname, pref);
	}

	public void test_24DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_24";
		test_function(ontoname, pref);
	}

	public void test_25DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_25";
		test_function(ontoname, pref);
	}

	public void test_26DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_26";
		test_function(ontoname, pref);
	}

	public void test_27DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_27";
		test_function(ontoname, pref);
	}

	public void test_28DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_28";
		test_function(ontoname, pref);
	}

	public void test_29DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_29";
		test_function(ontoname, pref);
	}

	public void test_30DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_30";
		test_function(ontoname, pref);
	}

	public void test_31DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_31";
		test_function(ontoname, pref);
	}

	public void test_32DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_32";
		test_function(ontoname, pref);
	}

	public void test_33DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_33";
		test_function(ontoname, pref);
	}

	public void test_34DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_34";
		test_function(ontoname, pref);
	}

	public void test_35DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_35";
		test_function(ontoname, pref);
	}

	public void test_36DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_36";
		test_function(ontoname, pref);
	}

	public void test_37DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_37";
		test_function(ontoname, pref);
	}

	public void test_38DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_38";
		test_function(ontoname, pref);
	}

	public void test_39DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_39";
		test_function(ontoname, pref);
	}

	public void test_40DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_40";
		test_function(ontoname, pref);
	}

	public void test_41DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_41";
		test_function(ontoname, pref);
	}

	public void test_42DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_42";
		test_function(ontoname, pref);
	}

	public void test_43DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_43";
		test_function(ontoname, pref);
	}

	public void test_44DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_44";
		test_function(ontoname, pref);
	}

	public void test_45DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_45";
		test_function(ontoname, pref);
	}

	public void test_46DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_46";
		test_function(ontoname, pref);
	}

	public void test_47DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_47";
		test_function(ontoname, pref);
	}

	public void test_48DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_48";
		test_function(ontoname, pref);
	}

	public void test_49DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_49";
		test_function(ontoname, pref);
	}

	public void test_50DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_50";
		test_function(ontoname, pref);
	}

	public void test_51DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_51";
		test_function(ontoname, pref);
	}

	public void test_52DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_52";
		test_function(ontoname, pref);
	}

	public void test_53DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_53";
		test_function(ontoname, pref);
	}

	public void test_54DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_54";
		test_function(ontoname, pref);
	}

	public void test_55DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_55";
		test_function(ontoname, pref);
	}

	public void test_56DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_56";
		test_function(ontoname, pref);
	}

	public void test_57DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_57";
		test_function(ontoname, pref);
	}

	public void test_58DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_58";
		test_function(ontoname, pref);
	}

	public void test_59DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_59";
		test_function(ontoname, pref);
	}

	public void test_60DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_60";
		test_function(ontoname, pref);
	}

	public void test_61DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_61";
		test_function(ontoname, pref);
	}

	public void test_62DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_62";
		test_function(ontoname, pref);
	}

	public void test_63DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_63";
		test_function(ontoname, pref);
	}

	public void test_64DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_64";
		test_function(ontoname, pref);
	}

	public void test_65DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_65";
		test_function(ontoname, pref);
	}

	public void test_66DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_66";
		test_function(ontoname, pref);
	}

	public void test_67DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_67";
		test_function(ontoname, pref);
	}

	public void test_68DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_68";
		test_function(ontoname, pref);
	}

	public void test_69DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_69";
		test_function(ontoname, pref);
	}

	public void test_70DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_70";
		test_function(ontoname, pref);
	}

	public void test_71DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_71";
		test_function(ontoname, pref);
	}

	public void test_72DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_72";
		test_function(ontoname, pref);
	}

	public void test_73DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_73";
		test_function(ontoname, pref);
	}

	public void test_74DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_74";
		test_function(ontoname, pref);
	}

	public void test_75DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_75";
		test_function(ontoname, pref);
	}

	public void test_76DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_76";
		test_function(ontoname, pref);
	}

	public void test_77DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_77";
		test_function(ontoname, pref);
	}

	public void test_78DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_78";
		test_function(ontoname, pref);
	}

	public void test_79DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_79";
		test_function(ontoname, pref);
	}

	public void test_80DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_80";
		test_function(ontoname, pref);
	}

	public void test_81DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_81";
		test_function(ontoname, pref);
	}

	public void test_82DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_82";
		test_function(ontoname, pref);
	}

	public void test_83DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_83";
		test_function(ontoname, pref);
	}

	public void test_84DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_84";
		test_function(ontoname, pref);
	}

	public void test_85DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_85";
		test_function(ontoname, pref);
	}

	public void test_86DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_86";
		test_function(ontoname, pref);
	}

	public void test_87DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_87";
		test_function(ontoname, pref);
	}

	public void test_88DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_88";
		test_function(ontoname, pref);
	}

	public void test_89DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_89";
		test_function(ontoname, pref);
	}

	public void test_90DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_90";
		test_function(ontoname, pref);
	}

	public void test_200DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_200";
		test_function(ontoname, pref);
	}

	public void test_201DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_201";
		test_function(ontoname, pref);
	}

	public void test_210DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_210";
		test_function(ontoname, pref);
	}

	public void test_401DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_401";
		test_function(ontoname, pref);
	}

	public void test_402DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_402";
		test_function(ontoname, pref);
	}

	public void test_403DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_403";
		test_function(ontoname, pref);
	}

	public void test_404DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_404";
		test_function(ontoname, pref);
	}

	public void test_405DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_405";
		test_function(ontoname, pref);
	}

	public void test_190DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_190";
		test_function(ontoname, pref);
	}

	public void test_191DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_191";
		test_function(ontoname, pref);
	}

	public void test_192DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_192";
		test_function(ontoname, pref);
	}

	public void test_193DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_193";
		test_function(ontoname, pref);
	}

	public void test_194DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_194";
		test_function(ontoname, pref);
	}

	public void test_195DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_195";
		test_function(ontoname, pref);
	}

	public void test_196DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_196";
		test_function(ontoname, pref);
	}

	public void test_197DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_197";
		test_function(ontoname, pref);
	}

	public void test_198DirectNoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_198";
		test_function(ontoname, pref);
	}

	public void test_600DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_600";
		test_function(ontoname, pref);
	}

	public void test_1DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_1";
		test_function(ontoname, pref);
	}

	public void test_2DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_2";
		test_function(ontoname, pref);
	}

	public void test_3DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_3";
		test_function(ontoname, pref);
	}

	public void test_4DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_4";
		test_function(ontoname, pref);
	}

	public void test_5DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_5";
		test_function(ontoname, pref);
	}

	public void test_6DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_6";
		test_function(ontoname, pref);
	}

	public void test_7DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_7";
		test_function(ontoname, pref);
	}

	public void test_8DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_8";
		test_function(ontoname, pref);
	}

	public void test_9DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_9";
		test_function(ontoname, pref);
	}

	public void test_10DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_10";
		test_function(ontoname, pref);
	}

	public void test_11DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_11";
		test_function(ontoname, pref);
	}

	public void test_12DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_12";
		test_function(ontoname, pref);
	}

	public void test_13DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_13";
		test_function(ontoname, pref);
	}

	public void test_14DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_14";
		test_function(ontoname, pref);
	}

	public void test_15DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_15";
		test_function(ontoname, pref);
	}

	public void test_16DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_16";
		test_function(ontoname, pref);
	}

	public void test_17DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_17";
		test_function(ontoname, pref);
	}

	public void test_18DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_18";
		test_function(ontoname, pref);
	}

	public void test_19DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_19";
		test_function(ontoname, pref);
	}

	public void test_20DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_20";
		test_function(ontoname, pref);
	}

	public void test_21DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_21";
		test_function(ontoname, pref);
	}

	public void test_22DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_22";
		test_function(ontoname, pref);
	}

	public void test_23DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_23";
		test_function(ontoname, pref);
	}

	public void test_24DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_24";
		test_function(ontoname, pref);
	}

	public void test_25DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_25";
		test_function(ontoname, pref);
	}

	public void test_26DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_26";
		test_function(ontoname, pref);
	}

	public void test_27DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_27";
		test_function(ontoname, pref);
	}

	public void test_28DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_28";
		test_function(ontoname, pref);
	}

	public void test_29DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_29";
		test_function(ontoname, pref);
	}

	public void test_30DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_30";
		test_function(ontoname, pref);
	}

	public void test_31DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_31";
		test_function(ontoname, pref);
	}

	public void test_32DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_32";
		test_function(ontoname, pref);
	}

	public void test_33DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_33";
		test_function(ontoname, pref);
	}

	public void test_34DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_34";
		test_function(ontoname, pref);
	}

	public void test_35DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_35";
		test_function(ontoname, pref);
	}

	public void test_36DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_36";
		test_function(ontoname, pref);
	}

	public void test_37DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_37";
		test_function(ontoname, pref);
	}

	public void test_38DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_38";
		test_function(ontoname, pref);
	}

	public void test_39DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_39";
		test_function(ontoname, pref);
	}

	public void test_40DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_40";
		test_function(ontoname, pref);
	}

	public void test_41DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_41";
		test_function(ontoname, pref);
	}

	public void test_42DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_42";
		test_function(ontoname, pref);
	}

	public void test_43DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_43";
		test_function(ontoname, pref);
	}

	public void test_44DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_44";
		test_function(ontoname, pref);
	}

	public void test_45DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_45";
		test_function(ontoname, pref);
	}

	public void test_46DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_46";
		test_function(ontoname, pref);
	}

	public void test_47DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_47";
		test_function(ontoname, pref);
	}

	public void test_48DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_48";
		test_function(ontoname, pref);
	}

	public void test_49DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_49";
		test_function(ontoname, pref);
	}

	public void test_50DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_50";
		test_function(ontoname, pref);
	}

	public void test_51DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_51";
		test_function(ontoname, pref);
	}

	public void test_52DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_52";
		test_function(ontoname, pref);
	}

	public void test_53DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_53";
		test_function(ontoname, pref);
	}

	public void test_54DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_54";
		test_function(ontoname, pref);
	}

	public void test_55DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_55";
		test_function(ontoname, pref);
	}

	public void test_56DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_56";
		test_function(ontoname, pref);
	}

	public void test_57DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_57";
		test_function(ontoname, pref);
	}

	public void test_58DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_58";
		test_function(ontoname, pref);
	}

	public void test_59DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_59";
		test_function(ontoname, pref);
	}

	public void test_60DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_60";
		test_function(ontoname, pref);
	}

	public void test_61DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_61";
		test_function(ontoname, pref);
	}

	public void test_62DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_62";
		test_function(ontoname, pref);
	}

	public void test_63DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_63";
		test_function(ontoname, pref);
	}

	public void test_64DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_64";
		test_function(ontoname, pref);
	}

	public void test_65DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_65";
		test_function(ontoname, pref);
	}

	public void test_66DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_66";
		test_function(ontoname, pref);
	}

	public void test_67DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_67";
		test_function(ontoname, pref);
	}

	public void test_68DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_68";
		test_function(ontoname, pref);
	}

	public void test_69DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_69";
		test_function(ontoname, pref);
	}

	public void test_70DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_70";
		test_function(ontoname, pref);
	}

	public void test_71DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_71";
		test_function(ontoname, pref);
	}

	public void test_72DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_72";
		test_function(ontoname, pref);
	}

	public void test_73DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_73";
		test_function(ontoname, pref);
	}

	public void test_74DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_74";
		test_function(ontoname, pref);
	}

	public void test_75DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_75";
		test_function(ontoname, pref);
	}

	public void test_76DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_76";
		test_function(ontoname, pref);
	}

	public void test_77DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_77";
		test_function(ontoname, pref);
	}

	public void test_78DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_78";
		test_function(ontoname, pref);
	}

	public void test_79DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_79";
		test_function(ontoname, pref);
	}

	public void test_80DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_80";
		test_function(ontoname, pref);
	}

	public void test_81DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_81";
		test_function(ontoname, pref);
	}

	public void test_82DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_82";
		test_function(ontoname, pref);
	}

	public void test_83DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_83";
		test_function(ontoname, pref);
	}

	public void test_84DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_84";
		test_function(ontoname, pref);
	}

	public void test_85DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_85";
		test_function(ontoname, pref);
	}

	public void test_86DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_86";
		test_function(ontoname, pref);
	}

	public void test_87DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_87";
		test_function(ontoname, pref);
	}

	public void test_88DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_88";
		test_function(ontoname, pref);
	}

	public void test_89DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_89";
		test_function(ontoname, pref);
	}

	public void test_90DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_90";
		test_function(ontoname, pref);
	}

	public void test_200DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_200";
		test_function(ontoname, pref);
	}

	public void test_201DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_201";
		test_function(ontoname, pref);
	}

	public void test_210DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_210";
		test_function(ontoname, pref);
	}

	public void test_401DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_401";
		test_function(ontoname, pref);
	}

	public void test_402DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_402";
		test_function(ontoname, pref);
	}

	public void test_403DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_403";
		test_function(ontoname, pref);
	}

	public void test_404DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_404";
		test_function(ontoname, pref);
	}

	public void test_405DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_405";
		test_function(ontoname, pref);
	}

	public void test_190DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_190";
		test_function(ontoname, pref);
	}

	public void test_191DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_191";
		test_function(ontoname, pref);
	}

	public void test_192DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_192";
		test_function(ontoname, pref);
	}

	public void test_193DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_193";
		test_function(ontoname, pref);
	}

	public void test_194DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_194";
		test_function(ontoname, pref);
	}

	public void test_195DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_195";
		test_function(ontoname, pref);
	}

	public void test_196DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_196";
		test_function(ontoname, pref);
	}

	public void test_197DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_197";
		test_function(ontoname, pref);
	}

	public void test_198DirectEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_198";
		test_function(ontoname, pref);
	}

	public void test_600SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_600";
		test_function(ontoname, pref);
	}

	public void test_1SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_1";
		test_function(ontoname, pref);
	}

	public void test_2SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_2";
		test_function(ontoname, pref);
	}

	public void test_3SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_3";
		test_function(ontoname, pref);
	}

	public void test_4SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_4";
		test_function(ontoname, pref);
	}

	public void test_5SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_5";
		test_function(ontoname, pref);
	}

	public void test_6SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_6";
		test_function(ontoname, pref);
	}

	public void test_7SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_7";
		test_function(ontoname, pref);
	}

	public void test_8SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_8";
		test_function(ontoname, pref);
	}

	public void test_9SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_9";
		test_function(ontoname, pref);
	}

	public void test_10SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_10";
		test_function(ontoname, pref);
	}

	public void test_11SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_11";
		test_function(ontoname, pref);
	}

	public void test_12SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_12";
		test_function(ontoname, pref);
	}

	public void test_13SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_13";
		test_function(ontoname, pref);
	}

	public void test_14SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_14";
		test_function(ontoname, pref);
	}

	public void test_15SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_15";
		test_function(ontoname, pref);
	}

	public void test_16SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_16";
		test_function(ontoname, pref);
	}

	public void test_17SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_17";
		test_function(ontoname, pref);
	}

	public void test_18SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_18";
		test_function(ontoname, pref);
	}

	public void test_19SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_19";
		test_function(ontoname, pref);
	}

	public void test_20SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_20";
		test_function(ontoname, pref);
	}

	public void test_21SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_21";
		test_function(ontoname, pref);
	}

	public void test_22SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_22";
		test_function(ontoname, pref);
	}

	public void test_23SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_23";
		test_function(ontoname, pref);
	}

	public void test_24SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_24";
		test_function(ontoname, pref);
	}

	public void test_25SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_25";
		test_function(ontoname, pref);
	}

	public void test_26SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_26";
		test_function(ontoname, pref);
	}

	public void test_27SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_27";
		test_function(ontoname, pref);
	}

	public void test_28SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_28";
		test_function(ontoname, pref);
	}

	public void test_29SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_29";
		test_function(ontoname, pref);
	}

	public void test_30SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_30";
		test_function(ontoname, pref);
	}

	public void test_31SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_31";
		test_function(ontoname, pref);
	}

	public void test_32SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_32";
		test_function(ontoname, pref);
	}

	public void test_33SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_33";
		test_function(ontoname, pref);
	}

	public void test_34SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_34";
		test_function(ontoname, pref);
	}

	public void test_35SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_35";
		test_function(ontoname, pref);
	}

	public void test_36SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_36";
		test_function(ontoname, pref);
	}

	public void test_37SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_37";
		test_function(ontoname, pref);
	}

	public void test_38SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_38";
		test_function(ontoname, pref);
	}

	public void test_39SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_39";
		test_function(ontoname, pref);
	}

	public void test_40SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_40";
		test_function(ontoname, pref);
	}

	public void test_41SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_41";
		test_function(ontoname, pref);
	}

	public void test_42SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_42";
		test_function(ontoname, pref);
	}

	public void test_43SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_43";
		test_function(ontoname, pref);
	}

	public void test_44SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_44";
		test_function(ontoname, pref);
	}

	public void test_45SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_45";
		test_function(ontoname, pref);
	}

	public void test_46SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_46";
		test_function(ontoname, pref);
	}

	public void test_47SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_47";
		test_function(ontoname, pref);
	}

	public void test_48SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_48";
		test_function(ontoname, pref);
	}

	public void test_49SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_49";
		test_function(ontoname, pref);
	}

	public void test_50SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_50";
		test_function(ontoname, pref);
	}

	public void test_51SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_51";
		test_function(ontoname, pref);
	}

	public void test_52SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_52";
		test_function(ontoname, pref);
	}

	public void test_53SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_53";
		test_function(ontoname, pref);
	}

	public void test_54SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_54";
		test_function(ontoname, pref);
	}

	public void test_55SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_55";
		test_function(ontoname, pref);
	}

	public void test_56SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_56";
		test_function(ontoname, pref);
	}

	public void test_57SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_57";
		test_function(ontoname, pref);
	}

	public void test_58SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_58";
		test_function(ontoname, pref);
	}

	public void test_59SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_59";
		test_function(ontoname, pref);
	}

	public void test_60SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_60";
		test_function(ontoname, pref);
	}

	public void test_61SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_61";
		test_function(ontoname, pref);
	}

	public void test_62SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_62";
		test_function(ontoname, pref);
	}

	public void test_63SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_63";
		test_function(ontoname, pref);
	}

	public void test_64SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_64";
		test_function(ontoname, pref);
	}

	public void test_65SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_65";
		test_function(ontoname, pref);
	}

	public void test_66SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_66";
		test_function(ontoname, pref);
	}

	public void test_67SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_67";
		test_function(ontoname, pref);
	}

	public void test_68SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_68";
		test_function(ontoname, pref);
	}

	public void test_69SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_69";
		test_function(ontoname, pref);
	}

	public void test_70SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_70";
		test_function(ontoname, pref);
	}

	public void test_71SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_71";
		test_function(ontoname, pref);
	}

	public void test_72SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_72";
		test_function(ontoname, pref);
	}

	public void test_73SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_73";
		test_function(ontoname, pref);
	}

	public void test_74SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_74";
		test_function(ontoname, pref);
	}

	public void test_75SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_75";
		test_function(ontoname, pref);
	}

	public void test_76SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_76";
		test_function(ontoname, pref);
	}

	public void test_77SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_77";
		test_function(ontoname, pref);
	}

	public void test_78SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_78";
		test_function(ontoname, pref);
	}

	public void test_79SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_79";
		test_function(ontoname, pref);
	}

	public void test_80SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_80";
		test_function(ontoname, pref);
	}

	public void test_81SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_81";
		test_function(ontoname, pref);
	}

	public void test_82SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_82";
		test_function(ontoname, pref);
	}

	public void test_83SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_83";
		test_function(ontoname, pref);
	}

	public void test_84SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_84";
		test_function(ontoname, pref);
	}

	public void test_85SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_85";
		test_function(ontoname, pref);
	}

	public void test_86SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_86";
		test_function(ontoname, pref);
	}

	public void test_87SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_87";
		test_function(ontoname, pref);
	}

	public void test_88SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_88";
		test_function(ontoname, pref);
	}

	public void test_89SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_89";
		test_function(ontoname, pref);
	}

	public void test_90SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_90";
		test_function(ontoname, pref);
	}

	public void test_200SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_200";
		test_function(ontoname, pref);
	}

	public void test_201SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_201";
		test_function(ontoname, pref);
	}

	public void test_210SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_210";
		test_function(ontoname, pref);
	}

	public void test_401SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_401";
		test_function(ontoname, pref);
	}

	public void test_402SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_402";
		test_function(ontoname, pref);
	}

	public void test_403SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_403";
		test_function(ontoname, pref);
	}

	public void test_404SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_404";
		test_function(ontoname, pref);
	}

	public void test_405SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_405";
		test_function(ontoname, pref);
	}

	public void test_190SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_190";
		test_function(ontoname, pref);
	}

	public void test_191SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_191";
		test_function(ontoname, pref);
	}

	public void test_192SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_192";
		test_function(ontoname, pref);
	}

	public void test_193SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_193";
		test_function(ontoname, pref);
	}

	public void test_194SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_194";
		test_function(ontoname, pref);
	}

	public void test_195SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_195";
		test_function(ontoname, pref);
	}

	public void test_196SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_196";
		test_function(ontoname, pref);
	}

	public void test_197SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_197";
		test_function(ontoname, pref);
	}

	public void test_198SINoEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_198";
		test_function(ontoname, pref);
	}

	public void test_600SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_600";
		test_function(ontoname, pref);
	}

	public void test_1SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_1";
		test_function(ontoname, pref);
	}

	public void test_2SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_2";
		test_function(ontoname, pref);
	}

	public void test_3SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_3";
		test_function(ontoname, pref);
	}

	public void test_4SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_4";
		test_function(ontoname, pref);
	}

	public void test_5SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_5";
		test_function(ontoname, pref);
	}

	public void test_6SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_6";
		test_function(ontoname, pref);
	}

	public void test_7SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_7";
		test_function(ontoname, pref);
	}

	public void test_8SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_8";
		test_function(ontoname, pref);
	}

	public void test_9SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_9";
		test_function(ontoname, pref);
	}

	public void test_10SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_10";
		test_function(ontoname, pref);
	}

	public void test_11SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_11";
		test_function(ontoname, pref);
	}

	public void test_12SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_12";
		test_function(ontoname, pref);
	}

	public void test_13SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_13";
		test_function(ontoname, pref);
	}

	public void test_14SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_14";
		test_function(ontoname, pref);
	}

	public void test_15SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_15";
		test_function(ontoname, pref);
	}

	public void test_16SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_16";
		test_function(ontoname, pref);
	}

	public void test_17SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_17";
		test_function(ontoname, pref);
	}

	public void test_18SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_18";
		test_function(ontoname, pref);
	}

	public void test_19SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_19";
		test_function(ontoname, pref);
	}

	public void test_20SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_20";
		test_function(ontoname, pref);
	}

	public void test_21SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_21";
		test_function(ontoname, pref);
	}

	public void test_22SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_22";
		test_function(ontoname, pref);
	}

	public void test_23SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_23";
		test_function(ontoname, pref);
	}

	public void test_24SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_24";
		test_function(ontoname, pref);
	}

	public void test_25SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_25";
		test_function(ontoname, pref);
	}

	public void test_26SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_26";
		test_function(ontoname, pref);
	}

	public void test_27SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_27";
		test_function(ontoname, pref);
	}

	public void test_28SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_28";
		test_function(ontoname, pref);
	}

	public void test_29SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_29";
		test_function(ontoname, pref);
	}

	public void test_30SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_30";
		test_function(ontoname, pref);
	}

	public void test_31SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_31";
		test_function(ontoname, pref);
	}

	public void test_32SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_32";
		test_function(ontoname, pref);
	}

	public void test_33SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_33";
		test_function(ontoname, pref);
	}

	public void test_34SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_34";
		test_function(ontoname, pref);
	}

	public void test_35SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_35";
		test_function(ontoname, pref);
	}

	public void test_36SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_36";
		test_function(ontoname, pref);
	}

	public void test_37SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_37";
		test_function(ontoname, pref);
	}

	public void test_38SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_38";
		test_function(ontoname, pref);
	}

	public void test_39SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_39";
		test_function(ontoname, pref);
	}

	public void test_40SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_40";
		test_function(ontoname, pref);
	}

	public void test_41SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_41";
		test_function(ontoname, pref);
	}

	public void test_42SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_42";
		test_function(ontoname, pref);
	}

	public void test_43SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_43";
		test_function(ontoname, pref);
	}

	public void test_44SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_44";
		test_function(ontoname, pref);
	}

	public void test_45SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_45";
		test_function(ontoname, pref);
	}

	public void test_46SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_46";
		test_function(ontoname, pref);
	}

	public void test_47SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_47";
		test_function(ontoname, pref);
	}

	public void test_48SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_48";
		test_function(ontoname, pref);
	}

	public void test_49SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_49";
		test_function(ontoname, pref);
	}

	public void test_50SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_50";
		test_function(ontoname, pref);
	}

	public void test_51SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_51";
		test_function(ontoname, pref);
	}

	public void test_52SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_52";
		test_function(ontoname, pref);
	}

	public void test_53SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_53";
		test_function(ontoname, pref);
	}

	public void test_54SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_54";
		test_function(ontoname, pref);
	}

	public void test_55SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_55";
		test_function(ontoname, pref);
	}

	public void test_56SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_56";
		test_function(ontoname, pref);
	}

	public void test_57SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_57";
		test_function(ontoname, pref);
	}

	public void test_58SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_58";
		test_function(ontoname, pref);
	}

	public void test_59SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_59";
		test_function(ontoname, pref);
	}

	public void test_60SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_60";
		test_function(ontoname, pref);
	}

	public void test_61SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_61";
		test_function(ontoname, pref);
	}

	public void test_62SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_62";
		test_function(ontoname, pref);
	}

	public void test_63SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_63";
		test_function(ontoname, pref);
	}

	public void test_64SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_64";
		test_function(ontoname, pref);
	}

	public void test_65SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_65";
		test_function(ontoname, pref);
	}

	public void test_66SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_66";
		test_function(ontoname, pref);
	}

	public void test_67SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_67";
		test_function(ontoname, pref);
	}

	public void test_68SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_68";
		test_function(ontoname, pref);
	}

	public void test_69SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_69";
		test_function(ontoname, pref);
	}

	public void test_70SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_70";
		test_function(ontoname, pref);
	}

	public void test_71SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_71";
		test_function(ontoname, pref);
	}

	public void test_72SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_72";
		test_function(ontoname, pref);
	}

	public void test_73SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_73";
		test_function(ontoname, pref);
	}

	public void test_74SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_74";
		test_function(ontoname, pref);
	}

	public void test_75SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_75";
		test_function(ontoname, pref);
	}

	public void test_76SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_76";
		test_function(ontoname, pref);
	}

	public void test_77SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_77";
		test_function(ontoname, pref);
	}

	public void test_78SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_78";
		test_function(ontoname, pref);
	}

	public void test_79SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_79";
		test_function(ontoname, pref);
	}

	public void test_80SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_80";
		test_function(ontoname, pref);
	}

	public void test_81SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_81";
		test_function(ontoname, pref);
	}

	public void test_82SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_82";
		test_function(ontoname, pref);
	}

	public void test_83SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_83";
		test_function(ontoname, pref);
	}

	public void test_84SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_84";
		test_function(ontoname, pref);
	}

	public void test_85SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_85";
		test_function(ontoname, pref);
	}

	public void test_86SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_86";
		test_function(ontoname, pref);
	}

	public void test_87SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_87";
		test_function(ontoname, pref);
	}

	public void test_88SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_88";
		test_function(ontoname, pref);
	}

	public void test_89SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_89";
		test_function(ontoname, pref);
	}

	public void test_90SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_90";
		test_function(ontoname, pref);
	}

	public void test_200SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_200";
		test_function(ontoname, pref);
	}

	public void test_201SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_201";
		test_function(ontoname, pref);
	}

	public void test_210SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_210";
		test_function(ontoname, pref);
	}

	public void test_401SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_401";
		test_function(ontoname, pref);
	}

	public void test_402SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_402";
		test_function(ontoname, pref);
	}

	public void test_403SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_403";
		test_function(ontoname, pref);
	}

	public void test_404SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_404";
		test_function(ontoname, pref);
	}

	public void test_405SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_405";
		test_function(ontoname, pref);
	}

	public void test_190SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_190";
		test_function(ontoname, pref);
	}

	public void test_191SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_191";
		test_function(ontoname, pref);
	}

	public void test_192SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_192";
		test_function(ontoname, pref);
	}

	public void test_193SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_193";
		test_function(ontoname, pref);
	}

	public void test_194SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_194";
		test_function(ontoname, pref);
	}

	public void test_195SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_195";
		test_function(ontoname, pref);
	}

	public void test_196SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_196";
		test_function(ontoname, pref);
	}

	public void test_197SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_197";
		test_function(ontoname, pref);
	}

	public void test_198SIEqNoSig() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		String ontoname = "test_198";
		test_function(ontoname, pref);
	}

	public void test_600SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_600";
		test_function(ontoname, pref);
	}

	public void test_1SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_1";
		test_function(ontoname, pref);
	}

	public void test_2SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_2";
		test_function(ontoname, pref);
	}

	public void test_3SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_3";
		test_function(ontoname, pref);
	}

	public void test_4SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_4";
		test_function(ontoname, pref);
	}

	public void test_5SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_5";
		test_function(ontoname, pref);
	}

	public void test_6SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_6";
		test_function(ontoname, pref);
	}

	public void test_7SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_7";
		test_function(ontoname, pref);
	}

	public void test_8SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_8";
		test_function(ontoname, pref);
	}

	public void test_9SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_9";
		test_function(ontoname, pref);
	}

	public void test_10SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_10";
		test_function(ontoname, pref);
	}

	public void test_11SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_11";
		test_function(ontoname, pref);
	}

	public void test_12SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_12";
		test_function(ontoname, pref);
	}

	public void test_13SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_13";
		test_function(ontoname, pref);
	}

	public void test_14SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_14";
		test_function(ontoname, pref);
	}

	public void test_15SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_15";
		test_function(ontoname, pref);
	}

	public void test_16SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_16";
		test_function(ontoname, pref);
	}

	public void test_17SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_17";
		test_function(ontoname, pref);
	}

	public void test_18SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_18";
		test_function(ontoname, pref);
	}

	public void test_19SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_19";
		test_function(ontoname, pref);
	}

	public void test_20SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_20";
		test_function(ontoname, pref);
	}

	public void test_21SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_21";
		test_function(ontoname, pref);
	}

	public void test_22SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_22";
		test_function(ontoname, pref);
	}

	public void test_23SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_23";
		test_function(ontoname, pref);
	}

	public void test_24SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_24";
		test_function(ontoname, pref);
	}

	public void test_25SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_25";
		test_function(ontoname, pref);
	}

	public void test_26SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_26";
		test_function(ontoname, pref);
	}

	public void test_27SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_27";
		test_function(ontoname, pref);
	}

	public void test_28SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_28";
		test_function(ontoname, pref);
	}

	public void test_29SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_29";
		test_function(ontoname, pref);
	}

	public void test_30SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_30";
		test_function(ontoname, pref);
	}

	public void test_31SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_31";
		test_function(ontoname, pref);
	}

	public void test_32SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_32";
		test_function(ontoname, pref);
	}

	public void test_33SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_33";
		test_function(ontoname, pref);
	}

	public void test_34SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_34";
		test_function(ontoname, pref);
	}

	public void test_35SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_35";
		test_function(ontoname, pref);
	}

	public void test_36SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_36";
		test_function(ontoname, pref);
	}

	public void test_37SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_37";
		test_function(ontoname, pref);
	}

	public void test_38SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_38";
		test_function(ontoname, pref);
	}

	public void test_39SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_39";
		test_function(ontoname, pref);
	}

	public void test_40SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_40";
		test_function(ontoname, pref);
	}

	public void test_41SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_41";
		test_function(ontoname, pref);
	}

	public void test_42SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_42";
		test_function(ontoname, pref);
	}

	public void test_43SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_43";
		test_function(ontoname, pref);
	}

	public void test_44SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_44";
		test_function(ontoname, pref);
	}

	public void test_45SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_45";
		test_function(ontoname, pref);
	}

	public void test_46SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_46";
		test_function(ontoname, pref);
	}

	public void test_47SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_47";
		test_function(ontoname, pref);
	}

	public void test_48SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_48";
		test_function(ontoname, pref);
	}

	public void test_49SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_49";
		test_function(ontoname, pref);
	}

	public void test_50SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_50";
		test_function(ontoname, pref);
	}

	public void test_51SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_51";
		test_function(ontoname, pref);
	}

	public void test_52SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_52";
		test_function(ontoname, pref);
	}

	public void test_53SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_53";
		test_function(ontoname, pref);
	}

	public void test_54SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_54";
		test_function(ontoname, pref);
	}

	public void test_55SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_55";
		test_function(ontoname, pref);
	}

	public void test_56SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_56";
		test_function(ontoname, pref);
	}

	public void test_57SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_57";
		test_function(ontoname, pref);
	}

	public void test_58SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_58";
		test_function(ontoname, pref);
	}

	public void test_59SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_59";
		test_function(ontoname, pref);
	}

	public void test_60SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_60";
		test_function(ontoname, pref);
	}

	public void test_61SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_61";
		test_function(ontoname, pref);
	}

	public void test_62SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_62";
		test_function(ontoname, pref);
	}

	public void test_63SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_63";
		test_function(ontoname, pref);
	}

	public void test_64SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_64";
		test_function(ontoname, pref);
	}

	public void test_65SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_65";
		test_function(ontoname, pref);
	}

	public void test_66SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_66";
		test_function(ontoname, pref);
	}

	public void test_67SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_67";
		test_function(ontoname, pref);
	}

	public void test_68SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_68";
		test_function(ontoname, pref);
	}

	public void test_69SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_69";
		test_function(ontoname, pref);
	}

	public void test_70SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_70";
		test_function(ontoname, pref);
	}

	public void test_71SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_71";
		test_function(ontoname, pref);
	}

	public void test_72SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_72";
		test_function(ontoname, pref);
	}

	public void test_73SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_73";
		test_function(ontoname, pref);
	}

	public void test_74SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_74";
		test_function(ontoname, pref);
	}

	public void test_75SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_75";
		test_function(ontoname, pref);
	}

	public void test_76SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_76";
		test_function(ontoname, pref);
	}

	public void test_77SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_77";
		test_function(ontoname, pref);
	}

	public void test_78SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_78";
		test_function(ontoname, pref);
	}

	public void test_79SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_79";
		test_function(ontoname, pref);
	}

	public void test_80SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_80";
		test_function(ontoname, pref);
	}

	public void test_81SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_81";
		test_function(ontoname, pref);
	}

	public void test_82SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_82";
		test_function(ontoname, pref);
	}

	public void test_83SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_83";
		test_function(ontoname, pref);
	}

	public void test_84SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_84";
		test_function(ontoname, pref);
	}

	public void test_85SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_85";
		test_function(ontoname, pref);
	}

	public void test_86SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_86";
		test_function(ontoname, pref);
	}

	public void test_87SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_87";
		test_function(ontoname, pref);
	}

	public void test_88SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_88";
		test_function(ontoname, pref);
	}

	public void test_89SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_89";
		test_function(ontoname, pref);
	}

	public void test_90SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_90";
		test_function(ontoname, pref);
	}

	public void test_200SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_200";
		test_function(ontoname, pref);
	}

	public void test_201SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_201";
		test_function(ontoname, pref);
	}

	public void test_210SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_210";
		test_function(ontoname, pref);
	}

	public void test_401SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_401";
		test_function(ontoname, pref);
	}

	public void test_402SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_402";
		test_function(ontoname, pref);
	}

	public void test_403SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_403";
		test_function(ontoname, pref);
	}

	public void test_404SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_404";
		test_function(ontoname, pref);
	}

	public void test_405SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_405";
		test_function(ontoname, pref);
	}

	public void test_190SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_190";
		test_function(ontoname, pref);
	}

	public void test_191SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_191";
		test_function(ontoname, pref);
	}

	public void test_192SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_192";
		test_function(ontoname, pref);
	}

	public void test_193SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_193";
		test_function(ontoname, pref);
	}

	public void test_194SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_194";
		test_function(ontoname, pref);
	}

	public void test_195SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_195";
		test_function(ontoname, pref);
	}

	public void test_196SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_196";
		test_function(ontoname, pref);
	}

	public void test_197SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_197";
		test_function(ontoname, pref);
	}

	public void test_198SINoEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "false");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_198";
		test_function(ontoname, pref);
	}

	public void test_600SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_600";
		test_function(ontoname, pref);
	}

	public void test_1SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_1";
		test_function(ontoname, pref);
	}

	public void test_2SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_2";
		test_function(ontoname, pref);
	}

	public void test_3SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_3";
		test_function(ontoname, pref);
	}

	public void test_4SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_4";
		test_function(ontoname, pref);
	}

	public void test_5SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_5";
		test_function(ontoname, pref);
	}

	public void test_6SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_6";
		test_function(ontoname, pref);
	}

	public void test_7SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_7";
		test_function(ontoname, pref);
	}

	public void test_8SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_8";
		test_function(ontoname, pref);
	}

	public void test_9SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_9";
		test_function(ontoname, pref);
	}

	public void test_10SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_10";
		test_function(ontoname, pref);
	}

	public void test_11SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_11";
		test_function(ontoname, pref);
	}

	public void test_12SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_12";
		test_function(ontoname, pref);
	}

	public void test_13SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_13";
		test_function(ontoname, pref);
	}

	public void test_14SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_14";
		test_function(ontoname, pref);
	}

	public void test_15SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_15";
		test_function(ontoname, pref);
	}

	public void test_16SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_16";
		test_function(ontoname, pref);
	}

	public void test_17SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_17";
		test_function(ontoname, pref);
	}

	public void test_18SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_18";
		test_function(ontoname, pref);
	}

	public void test_19SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_19";
		test_function(ontoname, pref);
	}

	public void test_20SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_20";
		test_function(ontoname, pref);
	}

	public void test_21SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_21";
		test_function(ontoname, pref);
	}

	public void test_22SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_22";
		test_function(ontoname, pref);
	}

	public void test_23SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_23";
		test_function(ontoname, pref);
	}

	public void test_24SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_24";
		test_function(ontoname, pref);
	}

	public void test_25SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_25";
		test_function(ontoname, pref);
	}

	public void test_26SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_26";
		test_function(ontoname, pref);
	}

	public void test_27SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_27";
		test_function(ontoname, pref);
	}

	public void test_28SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_28";
		test_function(ontoname, pref);
	}

	public void test_29SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_29";
		test_function(ontoname, pref);
	}

	public void test_30SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_30";
		test_function(ontoname, pref);
	}

	public void test_31SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_31";
		test_function(ontoname, pref);
	}

	public void test_32SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_32";
		test_function(ontoname, pref);
	}

	public void test_33SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_33";
		test_function(ontoname, pref);
	}

	public void test_34SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_34";
		test_function(ontoname, pref);
	}

	public void test_35SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_35";
		test_function(ontoname, pref);
	}

	public void test_36SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_36";
		test_function(ontoname, pref);
	}

	public void test_37SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_37";
		test_function(ontoname, pref);
	}

	public void test_38SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_38";
		test_function(ontoname, pref);
	}

	public void test_39SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_39";
		test_function(ontoname, pref);
	}

	public void test_40SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_40";
		test_function(ontoname, pref);
	}

	public void test_41SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_41";
		test_function(ontoname, pref);
	}

	public void test_42SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_42";
		test_function(ontoname, pref);
	}

	public void test_43SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_43";
		test_function(ontoname, pref);
	}

	public void test_44SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_44";
		test_function(ontoname, pref);
	}

	public void test_45SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_45";
		test_function(ontoname, pref);
	}

	public void test_46SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_46";
		test_function(ontoname, pref);
	}

	public void test_47SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_47";
		test_function(ontoname, pref);
	}

	public void test_48SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_48";
		test_function(ontoname, pref);
	}

	public void test_49SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_49";
		test_function(ontoname, pref);
	}

	public void test_50SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_50";
		test_function(ontoname, pref);
	}

	public void test_51SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_51";
		test_function(ontoname, pref);
	}

	public void test_52SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_52";
		test_function(ontoname, pref);
	}

	public void test_53SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_53";
		test_function(ontoname, pref);
	}

	public void test_54SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_54";
		test_function(ontoname, pref);
	}

	public void test_55SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_55";
		test_function(ontoname, pref);
	}

	public void test_56SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_56";
		test_function(ontoname, pref);
	}

	public void test_57SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_57";
		test_function(ontoname, pref);
	}

	public void test_58SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_58";
		test_function(ontoname, pref);
	}

	public void test_59SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_59";
		test_function(ontoname, pref);
	}

	public void test_60SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_60";
		test_function(ontoname, pref);
	}

	public void test_61SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_61";
		test_function(ontoname, pref);
	}

	public void test_62SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_62";
		test_function(ontoname, pref);
	}

	public void test_63SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_63";
		test_function(ontoname, pref);
	}

	public void test_64SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_64";
		test_function(ontoname, pref);
	}

	public void test_65SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_65";
		test_function(ontoname, pref);
	}

	public void test_66SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_66";
		test_function(ontoname, pref);
	}

	public void test_67SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_67";
		test_function(ontoname, pref);
	}

	public void test_68SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_68";
		test_function(ontoname, pref);
	}

	public void test_69SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_69";
		test_function(ontoname, pref);
	}

	public void test_70SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_70";
		test_function(ontoname, pref);
	}

	public void test_71SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_71";
		test_function(ontoname, pref);
	}

	public void test_72SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_72";
		test_function(ontoname, pref);
	}

	public void test_73SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_73";
		test_function(ontoname, pref);
	}

	public void test_74SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_74";
		test_function(ontoname, pref);
	}

	public void test_75SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_75";
		test_function(ontoname, pref);
	}

	public void test_76SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_76";
		test_function(ontoname, pref);
	}

	public void test_77SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_77";
		test_function(ontoname, pref);
	}

	public void test_78SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_78";
		test_function(ontoname, pref);
	}

	public void test_79SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_79";
		test_function(ontoname, pref);
	}

	public void test_80SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_80";
		test_function(ontoname, pref);
	}

	public void test_81SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_81";
		test_function(ontoname, pref);
	}

	public void test_82SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_82";
		test_function(ontoname, pref);
	}

	public void test_83SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_83";
		test_function(ontoname, pref);
	}

	public void test_84SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_84";
		test_function(ontoname, pref);
	}

	public void test_85SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_85";
		test_function(ontoname, pref);
	}

	public void test_86SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_86";
		test_function(ontoname, pref);
	}

	public void test_87SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_87";
		test_function(ontoname, pref);
	}

	public void test_88SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_88";
		test_function(ontoname, pref);
	}

	public void test_89SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_89";
		test_function(ontoname, pref);
	}

	public void test_90SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_90";
		test_function(ontoname, pref);
	}

	public void test_200SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_200";
		test_function(ontoname, pref);
	}

	public void test_201SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_201";
		test_function(ontoname, pref);
	}

	public void test_210SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_210";
		test_function(ontoname, pref);
	}

	public void test_401SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_401";
		test_function(ontoname, pref);
	}

	public void test_402SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_402";
		test_function(ontoname, pref);
	}

	public void test_403SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_403";
		test_function(ontoname, pref);
	}

	public void test_404SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_404";
		test_function(ontoname, pref);
	}

	public void test_405SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_405";
		test_function(ontoname, pref);
	}

	public void test_190SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_190";
		test_function(ontoname, pref);
	}

	public void test_191SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_191";
		test_function(ontoname, pref);
	}

	public void test_192SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_192";
		test_function(ontoname, pref);
	}

	public void test_193SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_193";
		test_function(ontoname, pref);
	}

	public void test_194SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_194";
		test_function(ontoname, pref);
	}

	public void test_195SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_195";
		test_function(ontoname, pref);
	}

	public void test_196SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_196";
		test_function(ontoname, pref);
	}

	public void test_197SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_197";
		test_function(ontoname, pref);
	}

	public void test_198SIEqSigma() throws Exception {
		ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
		pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String ontoname = "test_198";
		test_function(ontoname, pref);
	}
}
