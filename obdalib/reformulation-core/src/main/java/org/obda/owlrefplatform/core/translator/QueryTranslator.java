package org.obda.owlrefplatform.core.translator;

import inf.unibz.it.dl.domain.DataProperty;
import inf.unibz.it.dl.domain.NamedConcept;
import inf.unibz.it.dl.domain.NamedProperty;
import inf.unibz.it.dl.domain.ObjectProperty;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.ucq.domain.BinaryQueryAtom;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConstantTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.UnionOfConjunctiveQueries;
import inf.unibz.it.ucq.domain.VariableTerm;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueBoolean;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

/**
 * Class is no longer used. Should probably be removed?
 * @author obda
 *
 */

public class QueryTranslator {
	

	public UnionOfConjunctiveQueries getUCQ(APIController apic, com.hp.hpl.jena.query.Query query) throws Exception {


		UnionOfConjunctiveQueries ucq = new UnionOfConjunctiveQueries();
		ArrayList<QueryTerm> headVariables = new ArrayList<QueryTerm>();
		
		
		List sel_vars = query.getResultVars();
		for (int i = 0; i < sel_vars.size(); i++) {
			Object var = sel_vars.get(i);
			headVariables.add(new VariableTerm(var.toString()));
		}

		com.hp.hpl.jena.sparql.syntax.Element pattern = query.getQueryPattern();
		ElementGroup group = (ElementGroup) pattern;

		List list = group.getElements();

		for (int k = 0; k < list.size(); k++) {
			inf.unibz.it.ucq.domain.ConjunctiveQuery newConjunct = new inf.unibz.it.ucq.domain.ConjunctiveQuery();
			newConjunct.addHeadTerms(headVariables);

			ElementGroup current_group = null;
			ElementTriplesBlock triplesBock = null;

			if (list.get(k) instanceof ElementGroup) {
				current_group = (ElementGroup) list.get(k);
				triplesBock = (ElementTriplesBlock) current_group.getElements().get(0);
			} else if (list.get(k) instanceof ElementTriplesBlock) {
				// current_group = (ElementGroup) list.get(k);
				triplesBock = (ElementTriplesBlock) list.get(0);
			} else if (list.get(k) instanceof ElementFilter) {
				ElementFilter qfilters = (ElementFilter) list.get(k);
				Expr expr = qfilters.getExpr();
				if (expr instanceof E_LogicalAnd) {
					E_LogicalAnd and = (E_LogicalAnd) expr;
					getAndExpressionAsAtoms(and);
					int i = 9;
				}
				continue;
			}

			BasicPattern triples = triplesBock.getTriples();

			for (int i = 0; i < triples.size(); i++) {

				QueryAtom newAtom = null;
				Triple triple = triples.get(i);

				Node o = triple.getObject();
				Node p = triple.getPredicate();
				Node s = triple.getSubject();

//				if (!((o instanceof Node_Literal) || (o instanceof Var))) {
//					throw new Exception("Unsupported query");
//				}
				if (!((p instanceof Node_URI))) { 
					throw new Exception("Unsupported query");
				}
				if (!((s instanceof Node_Literal) || (s instanceof Var))) {
					throw new Exception("Unsupported query");
				}

				// Node_Literal, Node_URI, Var
				Node_URI predicate = (Node_URI) p;

				if (predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					
					URI objURI = null;
					if(o instanceof Node_Literal){
						Node_Literal object = (Node_Literal) o;
						Element var_ind = null;
						
						String obj = object.getLiteralValue().toString();
						if(!obj.contains(":") || obj.startsWith(":")){
						
							obj = obj.replace(":","");
							String onotouri = apic.getCurrentOntologyURI().toString();
							obj = onotouri+"#"+obj;
							objURI = URI.create(obj);
							
						}else{
							String[] aux = obj.split(":");
							if(aux.length != 2){
								throw new Exception("Invalid Class name!");
							}else{
								String prefix = aux[0];
								String name = aux[1];
								String ontouri = apic.getCoupler().getUriForPrefix(prefix);
								if(ontouri == null){
									throw new Exception("Invalid prefix!");
								}
								objURI = URI.create(ontouri+"#"+name);
								
							}	
						}
					}else if(o instanceof Node_URI){
						objURI = URI.create(((Node_URI)o).getURI());
					}else{
						throw new Exception("Unsupported query syntax");
					}
					if (s instanceof Var) {
						Var subject = (Var) s;
						newAtom = new ConceptQueryAtom(new NamedConcept(objURI), new VariableTerm(
								subject.getName()));
					} else if (s instanceof Node_Literal) {
						Node_Literal subject = (Node_Literal) s;
						newAtom = new ConceptQueryAtom(new NamedConcept(objURI), new ConstantTerm(
								subject.getLiteralValue().toString()));
					}
				} else {

					NamedProperty role = null;
					QueryTerm term1 = null;
					QueryTerm term2 = null;

					if (s instanceof Var) {
						Var subject = (Var) s;
						term1 = new VariableTerm(subject.getName());

					}
					if (s instanceof Node_Literal) {
						Node_Literal subject = (Node_Literal) s;
						term1 = new ConstantTerm(subject.getLiteralValue().toString());
					}

					if (o instanceof Var) {
						Var object = (Var) o;
						term2 = new VariableTerm(object.getName());
					}
					if (o instanceof Node_Literal) {
						Node_Literal object = (Node_Literal) o;
						term2 = new ConstantTerm(object.getLiteralValue().toString());
					}

					URI roleURI = URI.create(predicate.getURI());
					boolean isDatatype = apic.getCoupler().isDatatypeProperty(apic.getCurrentOntologyURI(),roleURI);
					if (isDatatype){
						role = new DataProperty(roleURI);
					}else{
						role = new ObjectProperty(roleURI);
					}
					newAtom = new BinaryQueryAtom(role, term1, term2);
				}
				newConjunct.addQueryAtom(newAtom);
			}
			ucq.addQuery(newConjunct);
		}
		ucq.setDistinct(query.isDistinct());
		return ucq;
	}

	private List<BinaryQueryAtom> getAndExpressionAsAtoms(E_LogicalAnd and) {
		Expr arg1 = and.getArg1();
		Expr arg2 = and.getArg2();
		ArrayList<BinaryQueryAtom> atoms1 = new ArrayList<BinaryQueryAtom>();
		ArrayList<BinaryQueryAtom> atoms2 = new ArrayList<BinaryQueryAtom>();

		if (arg1 instanceof E_LogicalAnd) {
			atoms1.addAll(getAndExpressionAsAtoms((E_LogicalAnd) arg1));
		} else if (arg1 instanceof E_GreaterThan) {
			E_GreaterThan gt = (E_GreaterThan)arg1;
			Expr sub1 = gt.getArg1();
			Expr sub2 = gt.getArg2();
			getConstant((NodeValue)sub2);
			NodeValueInteger in = null;
			ExprVar v = null;
			int i = 0;
		} else if (arg1 instanceof E_LessThan) {

		} else if (arg1 instanceof E_GreaterThanOrEqual) {

		} else if (arg1 instanceof E_GreaterThanOrEqual) {

		} else if (arg1 instanceof E_Equals) {

		} else if (arg1 instanceof E_NotEquals) {

		}
		return null;
	}
	
	private ConstantTerm getConstant(NodeValue value) {
		StringBuilder constant = new StringBuilder();
		String str = value.asString();
		
		if (value instanceof NodeValueBoolean) {
			
		}
		return null;
	}

}
