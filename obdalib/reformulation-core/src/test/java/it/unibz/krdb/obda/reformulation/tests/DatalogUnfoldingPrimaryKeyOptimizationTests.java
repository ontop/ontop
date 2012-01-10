package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class DatalogUnfoldingPrimaryKeyOptimizationTests extends TestCase {

	DBMetadata metadata;

	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	DatalogProgram unfoldingProgram;

	@Override
	public void setUp() {
		metadata = new DBMetadata();
		TableDefinition table = new TableDefinition();
		table.setName("datatable");
		table.setAttribute(1, new Attribute("col1", "INT", true));
		table.setAttribute(2, new Attribute("col2", "INT", false));
		table.setAttribute(3, new Attribute("col3", "INT", false));
		table.setAttribute(4, new Attribute("col4", "INT", false));
		metadata.add(table);

		unfoldingProgram = fac.getDatalogProgram();

		Atom head = fac.getAtom(fac.getDataPropertyPredicate("name"), fac.getVariable("x"), fac.getVariable("y"));
		List<Term> bodyTerms = new LinkedList<Term>();
		bodyTerms.add(fac.getVariable("x"));
		bodyTerms.add(fac.getVariable("y"));
		bodyTerms.add(fac.getVariable("z"));
		bodyTerms.add(fac.getVariable("m"));
		Atom body = fac.getAtom(fac.getPredicate("datatable", 4), bodyTerms);
		CQIE rule = fac.getCQIE(head, body);
		unfoldingProgram.appendRule(rule);

		head = fac.getAtom(fac.getDataPropertyPredicate("lastname"), fac.getVariable("x"), fac.getVariable("z"));
		bodyTerms = new LinkedList<Term>();
		bodyTerms.add(fac.getVariable("x"));
		bodyTerms.add(fac.getVariable("y"));
		bodyTerms.add(fac.getVariable("z"));
		bodyTerms.add(fac.getVariable("m"));
		body = fac.getAtom(fac.getPredicate("datatable", 4), bodyTerms);
		rule = fac.getCQIE(head, body);
		unfoldingProgram.appendRule(rule);

		head = fac.getAtom(fac.getDataPropertyPredicate("id"), fac.getVariable("x"), fac.getVariable("m"));
		bodyTerms = new LinkedList<Term>();
		bodyTerms.add(fac.getVariable("x"));
		bodyTerms.add(fac.getVariable("y"));
		bodyTerms.add(fac.getVariable("z"));
		bodyTerms.add(fac.getVariable("m"));
		body = fac.getAtom(fac.getPredicate("datatable", 4), bodyTerms);
		rule = fac.getCQIE(head, body);
		unfoldingProgram.appendRule(rule);
	}

	public void testRedundancyElimination() throws Exception {
		DatalogUnfolder unfolder = new DatalogUnfolder(unfoldingProgram, metadata);

		LinkedList<Term> headterms = new LinkedList<Term>();
		headterms.add(fac.getVariable("m"));
		headterms.add(fac.getVariable("n"));
		headterms.add(fac.getVariable("o"));
		headterms.add(fac.getVariable("p"));

		Atom head = fac.getAtom(fac.getPredicate("q", 4), headterms);
		List<Atom> body = new LinkedList<Atom>();
		body.add(fac.getAtom(fac.getDataPropertyPredicate("name"), fac.getVariable("m"), fac.getVariable("n")));
		body.add(fac.getAtom(fac.getDataPropertyPredicate("lastname"), fac.getVariable("m"), fac.getVariable("o")));
		body.add(fac.getAtom(fac.getDataPropertyPredicate("id"), fac.getVariable("m"), fac.getVariable("p")));
		CQIE query = fac.getCQIE(head, body);

		DatalogProgram input = fac.getDatalogProgram(query);
		DatalogProgram output = unfolder.unfold(input);
		System.out.println("input " + input);

		System.out.println("output " + output);
		assertTrue(output.toString(), output.getRules().get(0).getBody().size() == 1);

	}
}
