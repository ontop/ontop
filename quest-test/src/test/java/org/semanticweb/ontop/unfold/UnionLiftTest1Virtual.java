package org.semanticweb.ontop.unfold;

/*
 * #%L
 * ontop-test
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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.AtomPredicateImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition.*;

/**
 * Class to check the translation of the combination of Optional/Union in SPARQL into Datalog, and finally 
 * SQL
 * @author Minda, Guohui, mrezk
 */
public class UnionLiftTest1Virtual {

	private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
	private static final Optional<ImmutableBooleanExpression> NO_EXPRESSION = Optional.absent();
	private static final Optional<ImmutableQueryModifiers> NO_MODIFIER = Optional.absent();

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/person.owl";
	final String obdafile = "src/test/resources/person4.obda";

	@Before
	public void setUp() throws Exception {

		fac = OBDADataFactoryImpl.getInstance();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		
	}

	private void runTests(Properties p, String query, int expectedvalue) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();


		try {

			executeQueryAssertResults(query, st, expectedvalue);

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
			conn.close();
			reasoner.dispose();
		}
	}
	
	public void executeQueryAssertResults(String query, QuestOWLStatement st, int expectedRows) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);
		int count = 0;
		while (rs.nextRow()) {
			count++;
			for (int i = 1; i <= rs.getColumnCount(); i++) {
				String varName = rs.getSignature().get(i-1);
				System.out.print(varName);
				//System.out.print("=" + rs.getOWLObject(i));
				System.out.print("=" + rs.getOWLObject(varName));
				System.out.print(" ");
			}
			System.out.println();
		}
		rs.close();
		assertEquals(expectedRows, count);
	}



    public IntermediateQuery buildQuery1() throws Exception {
		VariableImpl x = (VariableImpl) DATA_FACTORY.getVariable("x");
		VariableImpl y = (VariableImpl) DATA_FACTORY.getVariable("y");
		DataAtom rootDataAtom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans1", 2), x, y);
        ConstructionNode root = new ConstructionNodeImpl(rootDataAtom);

		IntermediateQueryBuilder queryBuilder = new JgraphtIntermediateQueryBuilder();
		queryBuilder.init(root);


		LeftJoinNode topLJ = new LeftJoinNodeImpl(NO_EXPRESSION);
		queryBuilder.addChild(root, topLJ);

		InnerJoinNode join1 = new InnerJoinNodeImpl(NO_EXPRESSION);
		queryBuilder.addChild(topLJ, join1, LEFT);

		/**
		 * Ans 2
		 */

		DataAtom ans2Atom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans2", 1), x);
		ConstructionNode topAns2Node = new ConstructionNodeImpl(ans2Atom);
		queryBuilder.addChild(join1, topAns2Node);

		UnionNode unionAns2 = new UnionNodeImpl();
		queryBuilder.addChild(topAns2Node, unionAns2);

		VariableImpl a = (VariableImpl) DATA_FACTORY.getVariable("a");
		ConstructionNode t1Ans2Node = new ConstructionNodeImpl(ans2Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, a)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns2, t1Ans2Node);

		TableNode t1 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t1", 1), a));
		queryBuilder.addChild(t1Ans2Node, t1);

		VariableImpl b = (VariableImpl) DATA_FACTORY.getVariable("b");
		ConstructionNode t2Ans2Node = new ConstructionNodeImpl(ans2Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, b)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns2, t2Ans2Node);

		TableNode t2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t2", 1), b));
		queryBuilder.addChild(t2Ans2Node, t2);

		/**
		 * Ans 3
		 */
		DataAtom ans3Atom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans3", 1), x);
		VariableImpl c = (VariableImpl) DATA_FACTORY.getVariable("c");
		ConstructionNode ans3Node = new ConstructionNodeImpl(ans3Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, c)), NO_MODIFIER);
		queryBuilder.addChild(join1, ans3Node);

		TableNode t3 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t3", 1), c));
		queryBuilder.addChild(ans3Node, t3);


		/**
		 * Ans 4
		 */
		DataAtom ans4Atom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans4", 2), x, y);
		ConstructionNode topAns4Node = new ConstructionNodeImpl(ans4Atom);
		queryBuilder.addChild(topLJ, topAns4Node, RIGHT);

		UnionNode unionAns4 = new UnionNodeImpl();
		queryBuilder.addChild(topAns4Node, unionAns4);

		VariableImpl d = (VariableImpl) DATA_FACTORY.getVariable("d");
		VariableImpl e = (VariableImpl) DATA_FACTORY.getVariable("e");
		ConstructionNode t4Ans4Node = new ConstructionNodeImpl(ans4Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, d, y, e)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns4, t4Ans4Node);

		TableNode t4 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t4", 2), d, e));
		queryBuilder.addChild(t4Ans4Node, t4);

		VariableImpl f = (VariableImpl) DATA_FACTORY.getVariable("f");
		VariableImpl g = (VariableImpl) DATA_FACTORY.getVariable("g");
		ConstructionNode t5Ans4Node = new ConstructionNodeImpl(ans4Atom,
				new ImmutableSubstitutionImpl<ImmutableTerm>(ImmutableMap.of(x, f, y, g)),
				NO_MODIFIER);
		queryBuilder.addChild(unionAns4, t5Ans4Node);

		TableNode t5 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("t5", 2), f, g));
		queryBuilder.addChild(t5Ans4Node, t5);


        return queryBuilder.build();
    }

	@Test
	public void printQuery1() throws Exception {
		System.out.println("Query 1: \n" + buildQuery1());
	}


        @Test
	public void testUnion() throws Exception {

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT * WHERE{ {?person a :Person } OPTIONAL {?person :hasFriend ?f} }";
		
		QuestPreferences p = new QuestPreferences();
		runTests(p,query_multi7,4);
	}

}
