package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.querymanager.QueryController;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.DefaultOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFileGenerator {

	private static final String															ONTOURI								= "http://obda.inf.unibz.it/ontologies/tests/dllitef/";

	private static final String															SPARQL_MODE							= "sparql";
	private static final String															DATALOG_MODE						= "datalog";

	private String																		obdalocation						= null;
	private String																		xmllocation							= null;
	private String																		testlocation						= null;
	private String																		mode								= null;

	// private OWLOntologyManager manager = null;
//	private OWLDataFactory																dataFactory							= null;
	private OBDAModel																	apic								= null;
	private it.unibz.krdb.obda.reformulation.tests.XMLResultWriter						writer								= null;
	private Vector<String>																tests								= null;

	private Vector<it.unibz.krdb.obda.reformulation.tests.OntologyGeneratorExpression>	expressions							= null;

	private int																			linenr								= 1;

//	private boolean																		GENERATE_ALTERNATIVES_FOR_EXISTS	= false;

	Logger																				log									= LoggerFactory
																																	.getLogger(this
																																			.getClass());

	public TestFileGenerator(String basefolder, String mode) {
		this.obdalocation = "file://" + basefolder + "src/test/resources/test/ontologies/";
		this.xmllocation =  basefolder + "src/test/resources/test/ontologies/";
		this.testlocation =  basefolder + "src/test/java/it/unibz/krdb/obda/reformulation/tests/";
		this.mode = mode;
		tests = new Vector<String>();
		expressions = new Vector<it.unibz.krdb.obda.reformulation.tests.OntologyGeneratorExpression>();
		// manager = OWLManager.createOWLOntologyManager();
		log.info("Main Location: {}", basefolder);
		log.info("Location for OBDA files: {}", obdalocation);
		log.info("Location for XML files: {}", xmllocation);
		log.info("Mode: {}", mode);

	}

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			throw new Exception("The program requires 3 parameters.\n" + "1. root folder for the tests"
					+  "2. the query mode. Can either be 'sparql' or 'datalog'" 
					+ "3. path to the input file\n");
		}

		String tl = args[0];
		String mode = args[1];
		String inputfile = args[2];
		

		TestFileGenerator gen = new TestFileGenerator(tl, mode);
		gen.parseInputFile(inputfile);
	}

	public void parseInputFile(String f) throws Exception {

		File file = new File(f);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;

		try {
			fis = new FileInputStream(file);

			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			it.unibz.krdb.obda.reformulation.tests.OntologyGeneratorExpression expression = null;

			while (dis.available() != 0) {

				String line = dis.readLine().trim();
				log.debug("Input: {}", line);
				if (line.contains("#END#")) {
					log.debug("Found end. Ignoring the rest of the file");
					break;
				}

				if (line.startsWith("||")) {
					line = line.substring(2, line.length() - 2);
					line.trim();
					line = line.replace("||", "#");
					String[] array = line.split("#");
					if (array.length != 7) {
						throw new Exception(" Input at " + linenr + " is not in proper format!");
					} else {
						if (lineContainsScenario(array[0].trim())) {
							if (array[0].trim().length() > 0) {
								if (expression != null) {
									expressions.add(expression);
								}
								String id = array[0].trim();
								String tbox = array[2].trim();
								String abox = array[3].trim();
								expression = new OntologyGeneratorExpression(id, tbox, abox);
							}
							String queryid = array[1].trim();
							String query = array[4].trim();
							String result = array[5].trim();
							expression.addQuery(query, result);
							expression.addQueryID(query, queryid);
						}
					}
				}
				linenr++;
			}
			if (expression != null) {
				expressions.add(expression);
			}
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Iterator<OntologyGeneratorExpression> it = expressions.iterator();
		int t = 1;
		while (it.hasNext()) {
			OntologyGeneratorExpression ex = it.next();
			String abox = ex.getAbox();
			String tbox = ex.getTbox();
			String id = ex.getId();
			Vector<OWLOntology> o = null;

			// Iterator<OWLOntology> oit = o.iterator();

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

			// OWLOntology onto = oit.next();

			OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
			apic = obdafac.getOBDAModel();
			QueryController controller = new QueryController();

			OWLOntology onto = null;
			try {
				onto = createOntology(id, abox, tbox, manager, manager.getOWLDataFactory());
			} catch (Exception e) {
				log.error(e.getMessage());
				log.error("Processing ontology: {} {}", id, abox + " " + tbox);
				throw e;
			}
			HashMap<String, String> queriesMap = ex.getQueries();
			HashMap<String, String> ids = ex.getQueriesIds();
			Set<String> queries = queriesMap.keySet();

			String ontoname = onto.getOntologyID().getOntologyIRI().toString();
			int z = ontoname.lastIndexOf("/");
			ontoname = ontoname.substring(z + 1, ontoname.length() - 4);
			writer = new XMLResultWriter(ontoname);

			Iterator<String> qit = queries.iterator();
			while (qit.hasNext()) {
				String query = qit.next();

				String genQuery = "";
				try {
					if (mode.equals(SPARQL_MODE))
						genQuery = getSPARQLQuery(query);
					else if (mode.equals(DATALOG_MODE))
						genQuery = getDatalogQuery(query);
					else
						throw new Exception("Cannot generate the proper query");
				} catch (Exception e) {
					log.error("Processing query: {}", query);
					log.error(e.getMessage());
					throw e;
				}

				controller.addQuery(genQuery, ids.get(query));
				String[] queryhead = getQueryHead(query);
				String[] results = getResults(queriesMap.get(query));
				writer.addResult(ids.get(query), queryhead, results);
			}
			String loc = obdalocation + ontoname + ".obda";
			DataManager ioManager = new DataManager(apic, controller);
			ioManager.saveOBDAData(URI.create(loc), apic.getPrefixManager());

			/* we start from 7 because the location must start with file:// */
			String owluri = obdalocation.substring(7) + ontoname + ".owl";
			manager.saveOntology(onto, new DefaultOntologyFormat(), IRI.create(new File(owluri).toURI()));
			String xmluri = xmllocation + ontoname + ".xml";
			writer.saveXMLResults(xmluri);
			tests.add(ontoname);
			t++;

		}

		generateJUnitTestClass();
	}

	private String[] getQueryHead(String query) throws Exception {

		String[] aux = query.split(":-");
		if (aux.length != 2) {
			throw new Exception("Wrong query format at " + linenr);
		}
		String head = aux[0].trim();
		int i1 = head.indexOf("(");
		int i2 = head.indexOf(")");
		String headvars = head.substring(i1 + 1, i2);
		if (headvars.trim().length() > 0) {
			String vars[] = headvars.split(",");
			return vars;
		} else {
			return new String[0];
		}
	}

	private OWLClassExpression getOWLEntity(URI ontouri, String e, OWLDataFactory dataFactory) {
		e = e.trim();
		
		if (e.length() == 0)
			return null;
		if (e.startsWith("E")) {
			e = e.substring(1);
			if (e.contains("-")) {
				e = e.substring(0, e.length() - 5);// remove ^^-^^ from end
				OWLObjectProperty op = dataFactory.getOWLObjectProperty(IRI.create(URI.create(ontouri.toString() + "#" + e)));
				OWLObjectInverseOf inv = dataFactory.getOWLObjectInverseOf(op);
				return dataFactory.getOWLObjectSomeValuesFrom(inv, dataFactory.getOWLThing());
			} else {
				OWLObjectProperty op = dataFactory.getOWLObjectProperty(IRI.create(URI.create(ontouri.toString() + "#" + e)));
				return dataFactory.getOWLObjectSomeValuesFrom(op, dataFactory.getOWLThing());
			}
		}
		return dataFactory.getOWLClass(IRI.create(URI.create(ontouri.toString() + "#" + e)));

	}

	private OWLObjectPropertyExpression getOWLObjectProperty(URI ontouri, String name, OWLDataFactory dataFactory) {
		name = name.trim();
//		System.out.println(name);
		if (name.contains("-")) {
			name = name.substring(0, name.length() - 5);// remove ^^-^^ from end
			OWLObjectProperty op = dataFactory.getOWLObjectProperty(IRI.create(URI.create(ontouri.toString() + "#" + name)));
			return dataFactory.getOWLObjectInverseOf(op);
		} else {
			return dataFactory.getOWLObjectProperty(IRI.create(URI.create(ontouri.toString() + "#" + name)));
		}
	}

	private OWLOntology createOntology(String testid, String abox, String tbox, OWLOntologyManager manager, OWLDataFactory dataFactory) throws Exception {

		
//		URI uri = URI.create(ONTOURI + "test.owl");
		URI uri = URI.create("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl");

		/*
		 * Adding the TBox
		 */
		String tboxcontent[] = tbox.split(",");
		List<OWLAxiom> axioms = getAxiomsFromTBox(tboxcontent, uri, manager, dataFactory);
		OWLOntology onto = getOntolgoies(axioms, testid);

		/*
		 * Adding the individuals
		 */
		String[] individuals = abox.trim().split(" ");
		OWLAxiom axiom = null;
		for (int i = 0; i < individuals.length; i++) {
			String aux = individuals[i].trim();
			if (aux.length() > 0) {
				int i1 = aux.indexOf("(");
				int i2 = aux.indexOf(")");
				String entity = aux.substring(0, i1);
				String parameters = aux.substring(i1 + 1, i2);
				String[] names = parameters.split(",");
				axiom = null;
				if (names.length == 2) {
					String objprop = uri.toString() + "#" + entity;
					OWLObjectProperty op = dataFactory.getOWLObjectProperty(IRI.create(URI.create(objprop)));
					String ind1uri = uri + "#" + names[0];
					String ind2uri = uri + "#" + names[1];
					OWLIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(URI.create(ind1uri)));
					OWLIndividual ind2 = dataFactory.getOWLNamedIndividual(IRI.create(URI.create(ind2uri)));
					
					axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(op, ind1, ind2);

					// declaration
					manager.addAxiom(onto, dataFactory.getOWLDeclarationAxiom(op));

				} else {
					String clazz = uri.toString() + "#" + entity;
					OWLClass cl = dataFactory.getOWLClass(IRI.create(URI.create(clazz)));
					String induri = uri + "#" + parameters;
					OWLIndividual ind = dataFactory.getOWLNamedIndividual(IRI.create(URI.create(induri)));
					axiom = dataFactory.getOWLClassAssertionAxiom( cl,ind);

					
				}
			}
			if (axiom != null) {
				manager.addAxiom(onto, axiom);
			}
		}

		return onto;
	}

	private OWLOntology getOntolgoies(List<OWLAxiom> axioms, String testid) throws Exception {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.createOntology(IRI.create(URI.create(ONTOURI + "test_" + testid.trim() + ".owl")));
//		OWLOntology onto = manager.createOntology(IRI.create("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl"));

		for (int i = 0; i < axioms.size(); i++) {
			OWLAxiom vex = axioms.get(i);
			manager.addAxiom(onto, vex);
		}
		return onto;
	}

	private List<OWLAxiom> getAxiomsFromTBox(String[] tbox, URI uri, OWLOntologyManager manager, OWLDataFactory dataFactory) throws Exception {

		List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
		for (int i = 0; i < tbox.length; i++) {
			// Vector<OWLAxiom> axioms = new Vector<OWLAxiom>();
			String exp = tbox[i].trim();
			String[] entities = null;
			boolean isRoleInclusion = false;

			if (exp.contains(" RISA ")) {
				entities = exp.split("RISA");
				isRoleInclusion = true;
			} else {
				entities = exp.split("ISA");
			}

			if (entities.length != 2)
				throw new Exception(" TBox at " + linenr + " is not in proper format!");

			String s1 = entities[0].trim();
			String s2 = entities[1].trim();
			if (isRoleInclusion) {
				OWLObjectPropertyExpression role1 = getOWLObjectProperty(uri, s1, dataFactory);
				OWLObjectPropertyExpression role2 = getOWLObjectProperty(uri, s2, dataFactory);
				OWLAxiom ax = dataFactory.getOWLSubObjectPropertyOfAxiom(role1, role2);

				for (OWLEntity ent : role1.getSignature()) {
					axioms.add(dataFactory.getOWLDeclarationAxiom(ent));
				}
				for (OWLEntity ent : role2.getSignature()) {
					axioms.add(dataFactory.getOWLDeclarationAxiom(ent));
				}

				axioms.add(ax);
			} else {
				OWLClassExpression left = getOWLEntity(uri, s1, dataFactory);
				OWLClassExpression right = getOWLEntity(uri, s2, dataFactory);
				int c = 1;

				OWLAxiom ax = dataFactory.getOWLSubClassOfAxiom(left, right);

				for (OWLEntity ent : ax.getSignature()) {
					axioms.add(dataFactory.getOWLDeclarationAxiom(ent));
				}

				axioms.add(ax);

			}
		}
		return axioms;
	}

	private String getSPARQLQuery(String query) throws Exception {

		String[] aux = query.split(":-");
		if (aux.length != 2) {
			throw new Exception("Wrong query format at " + linenr);
		}
		String head = aux[0].trim();
		String body = aux[1].trim();
		int i1 = head.indexOf("(");
		int i2 = head.indexOf(")");
		String headvars = head.substring(i1 + 1, i2);
		String vars[] = headvars.split(",");
		StringBuffer sb = new StringBuffer();
		if (headvars.trim().length() > 0) {
			StringBuffer hv = new StringBuffer();
			for (int i = 0; i < vars.length; i++) {
				if (hv.length() > 0) {
					hv.append(" ");
				}
				hv.append("$");
				hv.append(vars[i]);
			}
			sb.append("SELECT ");
			sb.append(hv.toString());
		} else {
			sb.append("ASK");
		}

		String atoms[] = body.split(" ");
		StringBuffer sb1 = new StringBuffer();
		for (int i = 0; i < atoms.length; i++) {
			String atom = atoms[i];

			if (sb1.length() > 0) {
				sb1.append(" . ");
			}

			i1 = atom.indexOf("(");
			i2 = atom.indexOf(")");
			String name = atom.substring(0, i1);
			String parameters = atom.substring(i1 + 1, i2);
			if (parameters.contains(",")) {
				String[] p = parameters.split(",");
				if (p.length != 2) {
					throw new Exception("Wrong query format at line nr " + linenr);
				}
				sb1.append(getSPARQLTerm(p[0]));
				// if (!(p[0].startsWith("'") && p[0].endsWith("'"))) {
				// sb1.append("$");
				// }
				// sb1.append(p[0]);
				sb1.append(" dllite:");
				sb1.append(name);

				sb1.append(getSPARQLTerm(p[1]));
				// if (!(p[1].startsWith("") && p[1].endsWith("'"))) {
				// sb1.append("$");
				// }
				// sb1.append(p[1]);
			} else {
				sb1.append(getSPARQLTerm(parameters));
				sb1.append(" rdf:type dllite:");
				sb1.append(name);
			}
		}
		sb.append(" WHERE {");
		sb.append(sb1);
		sb.append("}");

		return sb.toString();
	}

	private String getSPARQLTerm(String parameters) {
		StringBuffer bf = new StringBuffer();
		if ((parameters.startsWith("<")) && parameters.endsWith(">")) {
			bf.append(":" + parameters.substring(1, parameters.length() - 1));
		} else {
			if (!(parameters.startsWith("'") && parameters.endsWith("'"))) {
				bf.append("$");
			}
			bf.append(parameters);
		}
		return bf.toString();
	}

	private String getDatalogQuery(String query) throws Exception {

		StringBuffer ds = new StringBuffer(); // datalog string

		String[] aux = query.split(":-");
		if (aux.length != 2)
			throw new Exception("Wrong query format at " + linenr);

		// The head
		String head = aux[0].trim();
		int i1 = head.indexOf("(");
		int i2 = head.indexOf(")");

		String headName = head.substring(0, i1);
		String headVars = head.substring(i1 + 1, i2);
		String[] vars = headVars.split(",");

		StringBuffer as = new StringBuffer(); // atom string
		as.append("ucq:").append(headName);
		as.append("(");
		if (headVars.trim().length() > 0) {
			StringBuffer av = new StringBuffer(); // atom variables
			for (String var : vars) {
				if (av.length() > 0) {
					av.append(", ");
				}
				av.append("$").append(var); // $x, $y
			}
			as.append(av.toString());
		}
		as.append(")");

		ds.append(as).append(" :- ");

		// The body
		String body = aux[1].trim();
		String atoms[] = body.split(" ");

		as = new StringBuffer(); // atom string
		for (String atom : atoms) {
			if (as.length() > 0) {
				as.append(", ");
			}
			i1 = atom.indexOf("(");
			i2 = atom.indexOf(")");

			String atomName = atom.substring(0, i1);
			String atomVars = atom.substring(i1 + 1, i2);
			vars = atomVars.split(",");

			as.append("dllite:").append(atomName);
			as.append("(");
			if (atomVars.trim().length() > 0) {
				StringBuffer av = new StringBuffer(); // atom variables
				for (String var : vars) {
					if (av.length() > 0) {
						av.append(", ");
					}
					av.append("$").append(var);
				}
				as.append(av);
			}
			as.append(")");
		}
		ds.append(as);

		return ds.toString();
	}

	private String[] getResults(String res) {
		res = res.trim();
		if (!res.equals("{}") && res.length() > 2) {
			res = res.substring(1, res.length() - 1);
			res = res.replace("(", "");
			res = res.replace(")", "");
			String[] results = res.split(" ");
			return results;
		} else {
			return new String[0];
		}
	}

	private void generateJUnitTestClass() {
		String outfile = testlocation + "ReformulationTest.java";
		log.info("Generating JUnit test file: {}", outfile);
		try {
			FileWriter fstream = new FileWriter(outfile);

			BufferedWriter out = new BufferedWriter(fstream);

			out.append("package it.unibz.krdb.obda.reformulation.tests;\n");
			out.append("import junit.framework.TestCase;\n");
			out.append("import java.util.Set;\n");
			out.append("import java.util.Iterator;\n");
			out.append("import java.util.LinkedList;\n");
			out.append("import java.util.Collections;\n");
			out.append("import java.util.List;\n");
			out.append("import org.slf4j.Logger;\n");
			out.append("import org.slf4j.LoggerFactory;\n");
			out.append("import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;\n");
			out.append("import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;\n");

			out.newLine();
			out.newLine();
			out.append("public class ReformulationTest extends TestCase {\n\n");
			out.append("\tprivate Tester tester = null;\n");
			out.append("\tLogger	log	= LoggerFactory.getLogger(this.getClass());\n");
			out.append("\tprivate String propfile = \"src/test/resources/test.properties\";\n");
			out.append("\tpublic ReformulationTest(){\n");
			out.append("\t\ttester = new Tester(propfile);\n");
			out.append("\t}\n");

			out.append("        private void test_function(String ontoname, QuestPreferences pref) throws Exception {\n");
			out.append("    \tlog.debug(\"Test case: {}\", ontoname);\n");
			out.append("    \tlog.debug(\"Quest configuration: {}\", pref.toString());\n");
			out.append("    \ttester.load(ontoname, pref);\n");
			out.append("    \tfor (String id : tester.getQueryIds()) {\n");
			out.append("    \t\tlog.debug(\"Testing query: {}\", id);\n");
			out.append("    \t\tSet<String> exp = tester.getExpectedResult(id);\n");
			out.append("    \t\tSet<String> res = tester.executeQuery(id);\n");
			out.append("    \t\tassertTrue(\"Expected \" + exp + \" Result \" + res, exp.size() == res.size());\n");
			out.append("    \t\tfor (String realResult : res) {\n");
			out.append("    \t\t\tassertTrue(\"expeted: \" + exp.toString() + \" obtained: \" + res.toString(), exp.contains(realResult));\n");
			out.append("    \t\t}\n");
			out.append("    \t}\n");
			out.append("    }\n");


//			for (int i = 0; i < tests.size(); i++) {
//				out.append("\tpublic void " + tests.get(i) + "DirectNoEqNoSig() throws Exception {");
//				out.newLine();
//				out.append("QuestPreferences pref = new QuestPreferences();\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.DIRECT);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, \"false\");\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, \"true\");\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, \"false\");\n");
//				out.append("\t\tString ontoname = \"" + tests.get(i) + "\";\n");
//				out.append("\t\ttest_function(ontoname,pref);\n");
//				out.append("\t}");
//				out.newLine();
//			}
//			for (int i = 0; i < tests.size(); i++) {
//
//				out.append("\tpublic void " + tests.get(i) + "DirectEqNoSig() throws Exception {");
//				out.newLine();
//				out.append("QuestPreferences pref = new QuestPreferences();\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.DIRECT);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, \"true\");\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, \"true\");\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, \"false\");\n");
//				out.append("\t\tString ontoname = \"" + tests.get(i) + "\";\n");
//				out.append("\t\ttest_function(ontoname,pref);\n");
//				out.append("\t}");
//				out.newLine();
//			}
//			
//			for (int i = 0; i < tests.size(); i++) {
//				
//				out.append("\tpublic void " + tests.get(i) + "DirectNoEqSigma() throws Exception {");
//				out.newLine();
//				out.append("QuestPreferences pref = new QuestPreferences();\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.DIRECT);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, \"false\");\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, \"true\");\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, \"true\");\n");
//				out.append("\t\tString ontoname = \"" + tests.get(i) + "\";\n");
//				out.append("\t\ttest_function(ontoname,pref);\n");
//				out.append("\t}");
//				out.newLine();
//			}
//			for (int i = 0; i < tests.size(); i++) {
//
//				out.append("\tpublic void " + tests.get(i) + "DirectEqSigma() throws Exception {");
//				out.newLine();
//				out.append("QuestPreferences pref = new QuestPreferences();\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.DIRECT);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, \"true\");\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, \"true\");\n");
//				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, \"true\");\n");
//				out.append("\t\tString ontoname = \"" + tests.get(i) + "\";\n");
//				out.append("\t\ttest_function(ontoname,pref);\n");
//				out.append("\t}");
//				out.newLine();
//			}
			
			for (int i = 0; i < tests.size(); i++) {
		
				out.append("\tpublic void " + tests.get(i) + "SINoEqNoSig() throws Exception {");
				out.newLine();
				out.append("QuestPreferences pref = new QuestPreferences();\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, \"false\");\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, \"true\");\n"); 
				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, \"false\");\n");
				out.append("\t\tString ontoname = \"" + tests.get(i) + "\";\n");
				out.append("\t\ttest_function(ontoname,pref);\n");
				out.append("\t}");
				out.newLine();
			}
			for (int i = 0; i < tests.size(); i++) {
	
				out.append("\tpublic void " + tests.get(i) + "SIEqNoSig() throws Exception {");
				out.newLine();
				out.append("QuestPreferences pref = new QuestPreferences();\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, \"true\");\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, \"true\");\n"); 
				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, \"false\");\n");
				out.append("\t\tString ontoname = \"" + tests.get(i) + "\";\n");
				out.append("\t\ttest_function(ontoname,pref);\n");
				out.append("\t}");
				out.newLine();
			
			}

			for (int i = 0; i < tests.size(); i++) {
	
				out.append("\tpublic void " + tests.get(i) + "SINoEqSigma() throws Exception {");
				out.newLine();
				out.append("QuestPreferences pref = new QuestPreferences();\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, \"false\");\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, \"true\");\n"); 
				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, \"true\");\n");
				out.append("\t\tString ontoname = \"" + tests.get(i) + "\";\n");
				out.append("\t\ttest_function(ontoname,pref);\n");
				out.append("\t}");
				out.newLine();
			}
			for (int i = 0; i < tests.size(); i++) {
	
				out.append("\tpublic void " + tests.get(i) + "SIEqSigma() throws Exception {");
				out.newLine();
				out.append("QuestPreferences pref = new QuestPreferences();\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, \"true\");\n");
				out.append("pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, \"true\");\n"); 
				out.append("pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, \"true\");\n");
				out.append("\t\tString ontoname = \"" + tests.get(i) + "\";\n");
				out.append("\t\ttest_function(ontoname,pref);\n");
				out.append("\t}");
				out.newLine();
			}
			out.append("}");
			out.newLine();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean lineContainsScenario(String s) {
		if (s.length() > 0) {
			try {
				Integer.parseInt(s);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		} else {
			return true;
		}
	}
}
