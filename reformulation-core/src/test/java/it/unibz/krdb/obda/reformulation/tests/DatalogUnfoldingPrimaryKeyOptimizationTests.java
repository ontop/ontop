package it.unibz.krdb.obda.reformulation.tests;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.QuotedIDFactory;
import it.unibz.krdb.sql.QuotedIDFactoryStandardSQL;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.UniqueConstraint;

import java.sql.Types;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class DatalogUnfoldingPrimaryKeyOptimizationTests extends TestCase {

	DBMetadata metadata;

	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	DatalogProgram unfoldingProgram;

	QuotedIDFactory idfac = new QuotedIDFactoryStandardSQL();
	
	@Override
	public void setUp() {
		metadata = new DBMetadata("dummy class", null, null, idfac);
		TableDefinition table = new TableDefinition(idfac.createRelationFromString(null, "TABLE"));
		table.addAttribute(idfac.createFromString("col1"), Types.INTEGER, null, false);
		table.addAttribute(idfac.createFromString("col2"), Types.INTEGER, null, false);
		table.addAttribute(idfac.createFromString("col3"), Types.INTEGER, null, false);
		table.addAttribute(idfac.createFromString("col4"), Types.INTEGER, null, false);
		table.setPrimaryKey(UniqueConstraint.of(table.getAttribute(idfac.createFromString("col1"))));
		metadata.add(table);
		
		
		table = new TableDefinition(idfac.createRelationFromString(null, "TABLE2"));
		table.addAttribute(idfac.createFromString("col1"), Types.INTEGER, null, false);
		table.addAttribute(idfac.createFromString("col2"), Types.INTEGER, null, false);
		table.addAttribute(idfac.createFromString("col3"), Types.INTEGER, null, false);
		table.addAttribute(idfac.createFromString("col4"), Types.INTEGER, null, false);
		table.setPrimaryKey(UniqueConstraint.of(table.getAttribute(idfac.createFromString("col1"))));
		metadata.add(table);

		unfoldingProgram = fac.getDatalogProgram();

		Function head = fac.getFunction(fac.getDataPropertyPredicate("name"), fac.getVariable("x"), fac.getVariable("y"));
		List<Term> bodyTerms = new LinkedList<Term>();
		bodyTerms.add(fac.getVariable("x"));
		bodyTerms.add(fac.getVariable("y"));
		bodyTerms.add(fac.getVariable("z"));
		bodyTerms.add(fac.getVariable("m"));
		Function body = fac.getFunction(fac.getPredicate("TABLE", 4), bodyTerms);
		CQIE rule = fac.getCQIE(head, body);
		unfoldingProgram.appendRule(rule);

		head = fac.getFunction(fac.getDataPropertyPredicate("lastname"), fac.getVariable("x"), fac.getVariable("z"));
		bodyTerms = new LinkedList<Term>();
		bodyTerms.add(fac.getVariable("x"));
		bodyTerms.add(fac.getVariable("y"));
		bodyTerms.add(fac.getVariable("z"));
		bodyTerms.add(fac.getVariable("m"));
		body = fac.getFunction(fac.getPredicate("TABLE", 4), bodyTerms);
		rule = fac.getCQIE(head, body);
		unfoldingProgram.appendRule(rule);

		head = fac.getFunction(fac.getDataPropertyPredicate("id"), fac.getVariable("x"), fac.getVariable("m"));
		bodyTerms = new LinkedList<Term>();
		bodyTerms.add(fac.getVariable("x"));
		bodyTerms.add(fac.getVariable("y"));
		bodyTerms.add(fac.getVariable("z"));
		bodyTerms.add(fac.getVariable("m"));
		body = fac.getFunction(fac.getPredicate("TABLE", 4), bodyTerms);
		rule = fac.getCQIE(head, body);
		unfoldingProgram.appendRule(rule);
		
		head = fac.getFunction(fac.getDataPropertyPredicate("name"), fac.getVariable("x"), fac.getVariable("y"));
		bodyTerms = new LinkedList<Term>();
		bodyTerms.add(fac.getVariable("x"));
		bodyTerms.add(fac.getVariable("y"));
		bodyTerms.add(fac.getVariable("z"));
		bodyTerms.add(fac.getVariable("m"));
		body = fac.getFunction(fac.getPredicate("TABLE2", 4), bodyTerms);
		rule = fac.getCQIE(head, body);
		unfoldingProgram.appendRule(rule);

		head = fac.getFunction(fac.getDataPropertyPredicate("lastname"), fac.getVariable("x"), fac.getVariable("z"));
		bodyTerms = new LinkedList<Term>();
		bodyTerms.add(fac.getVariable("x"));
		bodyTerms.add(fac.getVariable("y"));
		bodyTerms.add(fac.getVariable("z"));
		bodyTerms.add(fac.getVariable("m"));
		body = fac.getFunction(fac.getPredicate("TABLE2", 4), bodyTerms);
		rule = fac.getCQIE(head, body);
		unfoldingProgram.appendRule(rule);

		head = fac.getFunction(fac.getDataPropertyPredicate("id"), fac.getVariable("x"), fac.getVariable("m"));
		bodyTerms = new LinkedList<Term>();
		bodyTerms.add(fac.getVariable("x"));
		bodyTerms.add(fac.getVariable("y"));
		bodyTerms.add(fac.getVariable("z"));
		bodyTerms.add(fac.getVariable("m"));
		body = fac.getFunction(fac.getPredicate("TABLE2", 4), bodyTerms);
		rule = fac.getCQIE(head, body);
		unfoldingProgram.appendRule(rule);
	}

	public void testRedundancyElimination() throws Exception {
		DatalogUnfolder unfolder = QuestUnfolder.createDatalogUnfolder(unfoldingProgram.getRules(), metadata);

		LinkedList<Term> headterms = new LinkedList<Term>();
		headterms.add(fac.getVariable("m"));
		headterms.add(fac.getVariable("n"));
		headterms.add(fac.getVariable("o"));
		headterms.add(fac.getVariable("p"));

		Function head = fac.getFunction(fac.getPredicate("q", 4), headterms);
		List<Function> body = new LinkedList<Function>();
		body.add(fac.getFunction(fac.getDataPropertyPredicate("name"), fac.getVariable("m"), fac.getVariable("n")));
		body.add(fac.getFunction(fac.getDataPropertyPredicate("lastname"), fac.getVariable("m"), fac.getVariable("o")));
		body.add(fac.getFunction(fac.getDataPropertyPredicate("id"), fac.getVariable("m"), fac.getVariable("p")));
		CQIE query = fac.getCQIE(head, body);

		DatalogProgram input = fac.getDatalogProgram(Collections.singletonList(query));
		DatalogProgram output = unfolder.unfold(input, "q");
		System.out.println("input " + input);

		int atomcount = 0;
		for (CQIE result: output.getRules()) {
			for (Function atom: result.getBody()) {
				atomcount +=1;
			}
		}
		
		System.out.println("output " + output);
		assertTrue(output.toString(), atomcount == 14);
		
		
		headterms = new LinkedList<Term>();
		headterms.add(fac.getVariable("x"));
		headterms.add(fac.getVariable("y"));
		headterms.add(fac.getVariable("m"));
		headterms.add(fac.getVariable("o"));

		head = fac.getFunction(fac.getPredicate("q", 4), headterms);
		body = new LinkedList<Function>();
		body.add(fac.getFunction(fac.getDataPropertyPredicate("name"), fac.getVariable("s1"), fac.getVariable("x")));
		body.add(fac.getFunction(fac.getDataPropertyPredicate("name"), fac.getVariable("s2"), fac.getVariable("m")));
		body.add(fac.getFunction(fac.getDataPropertyPredicate("lastname"), fac.getVariable("s1"), fac.getVariable("y")));
		body.add(fac.getFunction(fac.getDataPropertyPredicate("lastname"), fac.getVariable("s2"), fac.getVariable("o")));
		query = fac.getCQIE(head, body);

		input = fac.getDatalogProgram(Collections.singletonList(query));
		output = unfolder.unfold(input, "q");
		System.out.println("input " + input);

		atomcount = 0;
		for (CQIE result: output.getRules()) {
			for (Function atom: result.getBody()) {
				atomcount +=1;
			}
		}
		
		System.out.println("output " + output);
		assertTrue(output.toString(), atomcount == 48);

	}
}
