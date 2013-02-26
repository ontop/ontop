package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.CQIEImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryBase;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;


public class R2RMLManager {

	
	private GraphUtil util = new GraphUtil();
	
	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private int i=0;
	
	private org.openrdf.model.Graph myGraph;
	
	private R2RMLParser r2rmlParser;
	
	public R2RMLManager(String file)
	{
		this(new File(file));
	}
	
	public R2RMLManager(File file)
	{
		try{
			r2rmlParser = new R2RMLParser();
		RDFParser parser = new org.openrdf.rio.turtle.TurtleParser();
		InputStream in = new FileInputStream(file);
		URL documentUrl = new URL("file://"+file);
		myGraph = new org.openrdf.model.impl.GraphImpl();
		StatementCollector collector = new StatementCollector(myGraph);
		parser.setRDFHandler(collector);
		parser.parse(in, documentUrl.toString());
		
//		
//		Iterator<Statement> stit = myGraph.iterator();
//		while(stit.hasNext())
//			System.out.println(stit.next().toString());

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Graph getGraph()
	{
		return myGraph;
	}
	
	/*
	 * this method return the list of mappings from the Graph
	 * main method to be called, asssembles everything
	 */
	public ArrayList<OBDAMappingAxiom> getMappings(Graph myGraph) {

		ArrayList<OBDAMappingAxiom> mappings = new ArrayList<OBDAMappingAxiom>();
		
		//retrieve the TriplesMap nodes
		Set<Resource> tripleMaps = r2rmlParser.getMappingNodes(myGraph);

		for (Resource tripleMap : tripleMaps) {

			//for each node get a mapping
			OBDAMappingAxiom mapping;
			
			try {
				mapping = getMapping(myGraph, tripleMap);
				
				//add it to the list of mappings
				mappings.add(mapping);
				
				System.out.println(mapping.toString());
				
				//pass 2 - check for join conditions, add to list
				List<OBDAMappingAxiom> joinMappings = getJoinMappings(myGraph, tripleMap);
				if (joinMappings !=null)
					mappings.addAll(joinMappings);
				
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return mappings;

	}
	

	/*
	 * method to get an OBDAMappingAxiom from a Resource node in the given Graph
	 */
	private OBDAMappingAxiom getMapping(Graph myGraph, Resource subj) throws Exception
	{
		
		String sourceQuery = r2rmlParser.getSQLQuery(myGraph, subj);
		List<Function> body = getMappingTripleAtoms(myGraph, subj);
		Function head = getHeadAtom(body);
		CQIE targetQuery = fac.getCQIE(head, body);
		i++;
		OBDAMappingAxiom mapping = fac.getRDBMSMappingAxiom("mapping"+i, sourceQuery, targetQuery);
		return mapping;
			
	}
	
	

	/*
	 * method to get the join OBDAMappingAxioms from a Resource node in the given Graph
	 */
	private List<OBDAMappingAxiom> getJoinMappings(Graph myGraph, Resource tripleMap) throws Exception
	{
		String sourceQuery = "";
		
		//get all predicateobject nodes that contain joins for the subj triplemap given
		List<Resource> joinNodes = r2rmlParser.getJoinNodes(myGraph, tripleMap);
		if (!joinNodes.isEmpty())
		{
		List<OBDAMappingAxiom> joinMappings = new ArrayList<OBDAMappingAxiom>(joinNodes.size());
		
		//get subject sql string and newliteral of given node
		String sourceQuery1 = r2rmlParser.getSQLQuery(myGraph, tripleMap);
		NewLiteral joinSubject1 = r2rmlParser.getSubjectAtom(myGraph, tripleMap);
		NewLiteral joinSubject1Child = r2rmlParser.getSubjectAtom(myGraph, tripleMap, "CHILD_");
		
		
		//for each predicateobject map that contains a join
		for (Resource joinPredObjNode : joinNodes)
		{
			//get the predicates
			List<Predicate> joinPredicates = r2rmlParser.getBodyPredicates(myGraph, joinPredObjNode);
			
			//get the referenced triple map node
			Resource referencedTripleMap = r2rmlParser.getReferencedTripleMap(myGraph, joinPredObjNode);
			
			//get the referenced triple map sql query and subject atom
			String sourceQuery2 = r2rmlParser.getSQLQuery(myGraph, referencedTripleMap);
			NewLiteral joinSubject2 = r2rmlParser.getSubjectAtom(myGraph, referencedTripleMap);
			NewLiteral joinSubject2Parent = r2rmlParser.getSubjectAtom(myGraph, referencedTripleMap, "PARENT_");
			
			//get join condition
			String childCol = r2rmlParser.getChildColumn(myGraph, joinPredObjNode);
			String parentCol = r2rmlParser.getParentColumn(myGraph, joinPredObjNode);
			
			
			List<Function> body = new ArrayList<Function>();
			// construct the atom from subject 1 and 2
			List<NewLiteral> terms = new ArrayList<NewLiteral>();
			
			
			//if join condition is empty, the two sql queries are the same
			if (childCol == null || parentCol == null)
			{
				sourceQuery = sourceQuery1;
				terms.add(joinSubject1);
				terms.add(joinSubject2);
			}
			else
			{
				sourceQuery = "SELECT * FROM ("+sourceQuery1 + ") as CHILD, ("+ sourceQuery2 +") as PARENT " +
						"WHERE CHILD."+childCol+" = PARENT."+parentCol;
				terms.add(joinSubject1Child);
				terms.add(joinSubject2Parent);
			}
			
			
			//for each predicate construct an atom and add to body
			for (Predicate pred : joinPredicates)
			{
				Function bodyAtom = fac.getAtom(pred, terms);
				body.add(bodyAtom);
			}
			//get head and construct cqie
			Function head = getHeadAtom(body);
			CQIE targetQuery = fac.getCQIE(head, body);
			
			if (sourceQuery.isEmpty())
				throw new Exception("Could not create source query for join in "+tripleMap.stringValue());
			
			i++;
			//finally, create mapping and add it to the list
			OBDAMappingAxiom mapping = fac.getRDBMSMappingAxiom("mapping"+i, sourceQuery, targetQuery);
			
			System.out.println("joinMapping: "+mapping.toString());
			
			joinMappings.add(mapping);
		}
		
		return joinMappings;
		}
		return null;
	}
	
	
	
	
	/*
	 * construct head of mapping q(variables) from the body
	 */
	private Function getHeadAtom(List<Function> body)
	{
		Set<Variable> vars = new HashSet<Variable>();
		for (Function bodyAtom : body)
		{
			 vars.addAll(bodyAtom.getReferencedVariables());
		}
		//System.out.println(vars.toString());
		int arity = vars.size();
		List<NewLiteral> dvars = new ArrayList<NewLiteral>(vars);
		Function head = fac.getAtom(fac.getPredicate(OBDALibConstants.QUERY_HEAD_URI, arity, null), dvars);
     //   System.out.println("headAtom = "+head.toString());
		return head;
	}
	
	
	/*
	 * method to get the body atoms of the mapping from a given Resource node in the Graph
	 */
	private List<Function> getMappingTripleAtoms(Graph myGraph, Resource subj) throws Exception
	{
		//the body to return
		List<Function> body = new ArrayList<Function>();
		
		
		//get subject
		NewLiteral subjectAtom = r2rmlParser.getSubjectAtom(myGraph, subj);		
		
		//get any class predicates, construct atom Class(subject), add to body
		List<Predicate> classPredicates = r2rmlParser.getClassPredicates();
		for (Predicate classPred : classPredicates)
			body.add(fac.getFunctionalTerm(classPred, subjectAtom.asAtom()));
		
		
		//get predicate-object nodes
		Set<Resource> predicateObjectNodes = r2rmlParser.getPredicateObjects(myGraph, subj);	
		
		for (Resource predobj : predicateObjectNodes) {
			//for each predicate object map
			
			//get body predicate
			List<Predicate> bodyPredicates = r2rmlParser.getBodyPredicates(myGraph, predobj);
			
			//get object atom
			NewLiteral objectAtom = r2rmlParser.getObjectAtom(myGraph, predobj);
			if (objectAtom==null)
			{
				// skip, object is a join
				continue;
			}
			
			// construct the atom, add it to the body
			List<NewLiteral> terms = new ArrayList<NewLiteral>();
			terms.add(subjectAtom);
			terms.add(objectAtom);
			
			for (Predicate bodyPred : bodyPredicates) {
				//for each predicate if there are more in the same node
				
				//check if predicate = rdf:type
				if (bodyPred.toString().equals(OBDAVocabulary.RDF_TYPE))
				{
					if(objectAtom.getReferencedVariables().size()<1)
					{
						Predicate newpred = fac.getClassPredicate(objectAtom.toString());
						body.add(fac.getFunctionalTerm(newpred, subjectAtom.asAtom()));
					}
				}
				else 
				{
					// create predicate(subject, object) and add it to the body
					Function bodyAtom = fac.getAtom(bodyPred, terms);
					body.add(bodyAtom);
				}
			}
			
		}
		//	System.out.println("bodyAtom = "+bodyAtom.toString());

		
		return body;
	}
	
	

}
