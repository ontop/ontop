package it.unibz.inf.ontop.owlapi.swrl;

import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.DatalogProgram;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.RDF;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLRule;


/**
 * Class that allows to create Datalog 
 * from SWRL rules in an ontology 
 *  
 */
public class SWRLAPITranslator {

	DatalogProgram program;
			
	public SWRLAPITranslator(OWLOntology ontology, AtomFactory atomFactory, TermFactory termFactory,
							 TypeFactory typeFactory, DatalogFactory datalogFactory, RDF rdfFactory) {

		SWRLVisitor visitor = new SWRLVisitor(atomFactory, termFactory, typeFactory, datalogFactory, rdfFactory);
		
		//get the axioms from the ontology and consider only the rules	
		program =visitor.createDatalog(ontology);
					
		
	}
	
	public SWRLAPITranslator(SWRLRule rule, AtomFactory atomFactory, TermFactory termFactory, TypeFactory typeFactory,
							 DatalogFactory datalogFactory, RDF rdfFactory) {
		
		SWRLVisitor visitor = new SWRLVisitor(atomFactory, termFactory, typeFactory, datalogFactory, rdfFactory);
		
		//transform the rule
		program =visitor.createDatalog(rule);
					
		
	}
	
	public DatalogProgram getDatalog(){
		return program;
		
	}
}
