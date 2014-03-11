package it.unibz.krdb.obda.owlapi3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;



/**
 * SWRLVisitor to get the different parts of head and body
 * @author Sarah
 *
 */
public class SWRLVisitor implements SWRLObjectVisitor {
	
	Function head;
	List<Function> body;
	OBDADataFactory fac;
	Function function;
	Variable var;
	List<Term> terms;
	Predicate predicate;
	Set<CQIE> facts;
	
	public SWRLVisitor(){
		fac=  OBDADataFactoryImpl.getInstance();
		
	}
	
	
	public DatalogProgram createDatalog(OWLOntology onto) throws Exception{
		head=null;
		body= new LinkedList<Function>();

		
		//transform SWRL head in Function
//		getHead(rule.getHead());
//		
//		//transform SWRL body in list Function
//		getBody(rule.getBody());
		
		facts.add(fac.getCQIE(head, body));
		
		return fac.getDatalogProgram(facts);
		
	}
	
	public DatalogProgram createDatalog(SWRLRule rule) throws Exception{
		head=null;
		body= new LinkedList<Function>();
		facts = new HashSet<CQIE>();

		for(SWRLAtom a: rule.getHead()){
		//transform SWRL head in Function
		getHead(a);
		
		//transform SWRL body in list Function
		getBody(rule.getBody());
		}
		
		facts.add(fac.getCQIE(head, body));
		
		return fac.getDatalogProgram(facts);
		
	}
	
	private void getHead(SWRLAtom atoms) {
		
		atoms.accept(this);
		head=function;
		
		
	}
	
	private void getBody(Set<SWRLAtom> atoms){
		if(body.isEmpty()){ //do not execute again if the body has already been assigned
		for(SWRLAtom a : atoms){
			a.accept(this);
			body.add(function);
		}
		}
		
	}
	
	@Override
	public void visit(SWRLRule node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SWRLClassAtom node) {
		//get predicate for datalog
		Predicate predicate=fac.getClassPredicate(node.getPredicate().asOWLClass().toStringID());
		
		terms = new ArrayList<Term>();
		//get terms for datalog
		for(SWRLArgument argument: node.getAllArguments()){
			argument.accept(this);
			
		}
		function = fac.getFunction(predicate, terms);
		
	}

	@Override
	public void visit(SWRLDataRangeAtom node) {
//		throw new Exception("Data Range Not Supported");
	}

	@Override
	public void visit(SWRLObjectPropertyAtom node) {
		
		predicate=fac.getObjectPropertyPredicate(node.getPredicate().asOWLObjectProperty().toStringID());
		
		terms = new ArrayList<Term>();
		//get terms for datalog
		for(SWRLArgument argument: node.getAllArguments()){
			argument.accept(this);

		}
		function = fac.getFunction(predicate, terms);
		
	}

	@Override
	public void visit(SWRLDataPropertyAtom node) {
		//get predicate for datalog
		 predicate=fac.getDataPropertyPredicate(node.getPredicate().asOWLDataProperty().toStringID());
				
		terms = new ArrayList<Term>();
				//get terms for datalog
		for(SWRLArgument argument: node.getAllArguments()){
					argument.accept(this);
		
				}
		function = fac.getFunction(predicate, terms);
		
	}

	@Override
	public void visit(SWRLBuiltInAtom node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SWRLVariable node) {
		
		terms.add(fac.getVariable(node.getIRI().getFragment()));
		
	}

	@Override
	public void visit(SWRLIndividualArgument node) {
	
		
		
	}

	@Override
	public void visit(SWRLLiteralArgument node) {
//		node.accept(this);
		OWLLiteral literal=node.getLiteral();
		
	if (literal.isBoolean()) {
		if (literal.parseBoolean()){
			terms.add(fac.getConstantTrue());
		}
		else
			terms.add(fac.getConstantFalse());
	}
	else 
		if(literal.hasLang()){
			fac.getConstantLiteral(literal.getLiteral(), literal.getLang());
		}
		else
			
			fac.getConstantLiteral(literal.getLiteral());
	
	
	}

	@Override
	public void visit(SWRLSameIndividualAtom node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SWRLDifferentIndividualsAtom node) {
		// TODO Auto-generated method stub
		
	}


	

}
