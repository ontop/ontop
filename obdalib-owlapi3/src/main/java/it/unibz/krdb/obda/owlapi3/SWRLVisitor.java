package it.unibz.krdb.obda.owlapi3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import it.unibz.krdb.obda.model.CQIE;
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
	
	public SWRLVisitor(){
		fac=  OBDADataFactoryImpl.getInstance();
		
	}
	
	public CQIE createDatalog(SWRLRule rule) throws Exception{
		head=null;
		body= new LinkedList<Function>();
//		rule.accept(this);
		//transform SWRL head in Function
		getHead(rule.getHead());
		
		//transform SWRL body in list Function
		getBody(rule.getBody());
		
		return fac.getCQIE(head, body);
		
	}
	
	private void getHead(Set<SWRLAtom> atoms) throws Exception{
		
		if(atoms.size()>1){
			throw new Exception("SWRL function with more than one atom");
		}
		atoms.iterator().next().accept(this);
		head=function;
		
		
	}
	
	private void getBody(Set<SWRLAtom> atoms){
		for(SWRLAtom a : atoms){
			a.accept(this);
			body.add(function);
		}
		
	}
	
	@Override
	public void visit(SWRLRule node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SWRLClassAtom node) {
		//get predicate for datalog
		Predicate predicate=fac.getClassPredicate(node.getPredicate().toString());
		
		terms = new ArrayList<Term>();
		//get terms for datalog
		for(SWRLArgument argument: node.getAllArguments()){
			argument.accept(this);
			
		}
		function = fac.getFunction(predicate, terms);
		
	}

	@Override
	public void visit(SWRLDataRangeAtom node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SWRLObjectPropertyAtom node) {
		Predicate predicate=fac.getObjectPropertyPredicate(node.getPredicate().toString());
		
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
		Predicate predicate=fac.getDataPropertyPredicate(node.getPredicate().toString());
				
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
		OWLIndividual individual= node.getIndividual();
		
//		fac.getConstantLiteral(value, type)
		terms.add(fac.getConstantLiteral(individual.toStringID()));
		
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
