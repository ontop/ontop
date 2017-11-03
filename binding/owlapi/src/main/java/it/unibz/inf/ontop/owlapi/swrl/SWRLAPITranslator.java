package it.unibz.inf.ontop.owlapi.swrl;

import it.unibz.inf.ontop.datalog.DatalogProgram;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLRule;


/**
 * Class that allows to create Datalog 
 * from SWRL rules in an ontology 
 *  
 */
public class SWRLAPITranslator {
	
	DatalogProgram program;
			
	public SWRLAPITranslator(OWLOntology ontology, AtomFactory atomFactory) {
		
		SWRLVisitor visitor = new SWRLVisitor(atomFactory);
		
		//get the axioms from the ontology and consider only the rules	
		program =visitor.createDatalog(ontology);
					
		
	}
	
	public SWRLAPITranslator(SWRLRule rule, AtomFactory atomFactory) {
		
		SWRLVisitor visitor = new SWRLVisitor(atomFactory);
		
		//transform the rule
		program =visitor.createDatalog(rule);
					
		
	}
	
	public DatalogProgram getDatalog(){
		return program;
		
	}
}
