/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.semindex.tests;



/**
 * @author Sergejs Pugac
 */
public class DAGOperationsTest  {

//    SemanticIndexHelper helper = new SemanticIndexHelper();
//
//    public void test_1_0_0() throws OWLOntologyCreationException {
//        String testname = "test_1_0_0";
//        DAG test_dag = helper.load_dag(testname);
//        Map<String, Set<DAGNode>> res = DAGOperations.buildAncestor(test_dag.getClassIndex());
//
//        Map<String, Set<String>> exp = new HashMap<String, Set<String>>();
//
//
//        exp.put("http://www.w3.org/2002/07/owl#Thing", new HashSet<String>());
//
//        Set<String> _1 = new HashSet<String>();
//        _1.add("http://www.w3.org/2002/07/owl#Thing");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#A", _1);
//
//        Set<String> _2 = new HashSet<String>();
//        _2.add("http://www.w3.org/2002/07/owl#Thing");
//        _2.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#A");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#B", _2);
//
//        Set<String> _3 = new HashSet<String>();
//        _3.add("http://www.w3.org/2002/07/owl#Thing");
//        _3.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#A");
//        _3.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#B");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#C", _3);
//
//        Set<String> _4 = new HashSet<String>();
//        _4.add("http://www.w3.org/2002/07/owl#Thing");
//        _4.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#A");
//        _4.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#B");
//        _4.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#C");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746417548.owl#D", _4);
//
//        assertEquals(res.size(), exp.size());
//        for (String uri : res.keySet()) {
//            Set<DAGNode> res_ancestors = res.get(uri);
//            Set<String> exp_ancestors = exp.get(uri);
//            assertEquals(res_ancestors.size(), exp_ancestors.size());
//            for (DAGNode node : res_ancestors) {
//                String res_uri = node.getUri();
//                assertTrue(exp_ancestors.contains(res_uri));
//            }
//        }
//    }
//
//    public void test_1_0_1() throws OWLOntologyCreationException {
//        String testname = "test_1_0_1";
//        DAG test_dag = helper.load_dag(testname);
//        Map<String, Set<DAGNode>> res = DAGOperations.buildAncestor(test_dag.getClassIndex());
//
//        Map<String, Set<String>> exp = new HashMap<String, Set<String>>();
//
//
//        exp.put("http://www.w3.org/2002/07/owl#Thing", new HashSet<String>());
//
//        Set<String> _1 = new HashSet<String>();
//        _1.add("http://www.w3.org/2002/07/owl#Thing");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#A", _1);
//
//        Set<String> _2 = new HashSet<String>();
//        _2.add("http://www.w3.org/2002/07/owl#Thing");
//        _2.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#A");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#B", _2);
//
//        Set<String> _3 = new HashSet<String>();
//        _3.add("http://www.w3.org/2002/07/owl#Thing");
//        _3.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#A");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#C", _3);
//
//        Set<String> _4 = new HashSet<String>();
//        _4.add("http://www.w3.org/2002/07/owl#Thing");
//        _4.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#A");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#D", _4);
//
//        Set<String> _5 = new HashSet<String>();
//        _5.add("http://www.w3.org/2002/07/owl#Thing");
//        _5.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#A");
//        _5.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#B");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#E", _5);
//
//        Set<String> _6 = new HashSet<String>();
//        _6.add("http://www.w3.org/2002/07/owl#Thing");
//        _6.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#A");
//        _6.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#C");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#F", _6);
//
//        Set<String> _7 = new HashSet<String>();
//        _7.add("http://www.w3.org/2002/07/owl#Thing");
//        _7.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#A");
//        _7.add("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#D");
//        exp.put("http://www.semanticweb.org/ontologies/2011/1/Ontology1298746790294.owl#G", _7);
//
//        assertEquals(res.size(), exp.size());
//        for (String uri : res.keySet()) {
//            Set<DAGNode> res_ancestors = res.get(uri);
//            Set<String> exp_ancestors = exp.get(uri);
//            assertEquals(res_ancestors.size(), exp_ancestors.size());
//            for (DAGNode node : res_ancestors) {
//                String res_uri = node.getUri();
//                assertTrue(exp_ancestors.contains(res_uri));
//            }
//        }
//    }
}
