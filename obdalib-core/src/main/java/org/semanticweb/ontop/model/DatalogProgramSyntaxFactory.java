package org.semanticweb.ontop.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;


/**
 * 
 * This class is a utility class for creating Datalog Program.
 * 
 * It is intended only for test case creators.
 * 
 * @author xiao
 *
 */

public class DatalogProgramSyntaxFactory {

	private static final OBDADataFactory termFactory = OBDADataFactoryImpl.getInstance();
	
	public static DatalogProgram program(CQIE rule){
		return termFactory.getDatalogProgram(rule);
	}
	
	public static DatalogProgram program(CQIE... rules){
		return termFactory.getDatalogProgram(Arrays.asList(rules));
	}
	
	public static DatalogProgram program(Collection<CQIE> rules){
		return termFactory.getDatalogProgram(rules);
	}
	
	public static CQIE rule(Function head, Function... body){
		return termFactory.getCQIE(head, body);
	}
	
	public static CQIE rule(Function head, List<Function> body){
		return termFactory.getCQIE(head, body);
	}
	
	public static Predicate predicate(String uri, int arity){
		return termFactory.getPredicate(uri, arity);
	}
	
	public static Predicate objectProperty(String name){
		return termFactory.getObjectPropertyPredicate(name);
	}

	public static Predicate dataProperty(String name){
		return termFactory.getDataPropertyPredicate(name);
	}
	
	public static Predicate cls(String name){
		return termFactory.getClassPredicate(name);
	}
	
	public static Function func(Predicate functor, Term... terms){
		return termFactory.getFunction(functor, terms);
	}
	
	public static Function func(Predicate functor, List<Term> terms){
		return termFactory.getFunction(functor, terms);
	}

	public static URIConstant constantURI(String uri){
		return termFactory.getConstantURI(uri);
	}

	public static Variable var(String name){
		return termFactory.getVariable(name);
	}
	
	public static ValueConstant constant(String value){
		return termFactory.getConstantLiteral(value);
	}
	
	public static Function uri(Term... terms){
		return termFactory.getUriTemplate(terms);
	}
	
	public static Function rdfsLiteral(Term term){
		Predicate rdfsLiteralPredicate = termFactory.getDataTypePredicateLiteral();
		return termFactory.getFunction(rdfsLiteralPredicate, term);
	}
	
	public static Function and(Term... terms){
		return termFactory.getFunction(OBDAVocabulary.AND, terms);
	}
	
	public static Function or(Term... terms){
		return termFactory.getFunction(OBDAVocabulary.OR, terms);
	}
	
	public static Function eq(Term... terms){
		return termFactory.getFunction(OBDAVocabulary.EQ, terms);
	}
	
	public static Function gt(Term... terms){
		return termFactory.getFunction(OBDAVocabulary.GT, terms);
	}

	public static Function isNotNull(Term term){
		return termFactory.getFunction(OBDAVocabulary.IS_NOT_NULL, term);
	}
	
	public static Function isNull(Term term){
		return termFactory.getFunction(OBDAVocabulary.IS_NULL, term);
	}
	
	public static Function lt(Term... terms){
		return termFactory.getFunction(OBDAVocabulary.LT, terms);
	}
	
	public static Function lte(Term... terms){
		return termFactory.getFunction(OBDAVocabulary.LTE, terms);
	}
	
	public static Function neq(Term... terms){
		return termFactory.getFunction(OBDAVocabulary.NEQ, terms);
	}
	
	public static Function not(Term... terms){
		return termFactory.getFunction(OBDAVocabulary.NOT, terms);
	}
	
	public static Function leftJoin(Term... terms){
		return termFactory.getFunction(OBDAVocabulary.SPARQL_LEFTJOIN, terms);
	}
	
}
