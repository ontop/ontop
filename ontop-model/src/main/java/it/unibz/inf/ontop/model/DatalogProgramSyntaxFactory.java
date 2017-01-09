package it.unibz.inf.ontop.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import it.unibz.inf.ontop.model.impl.OBDAVocabulary;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;


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
	
	public static DatalogProgram program(CQIE... rules){
		return program(Arrays.asList(rules));
	}
	
	public static DatalogProgram program(Collection<CQIE> rules){
		DatalogProgram datalogProgram = DATA_FACTORY.getDatalogProgram();
		datalogProgram.appendRule(rules);
		return datalogProgram;
	}
	
	public static CQIE rule(Function head, Function... body){
		return DATA_FACTORY.getCQIE(head, body);
	}
	
	public static CQIE rule(Function head, List<Function> body){
		return DATA_FACTORY.getCQIE(head, body);
	}
	
	public static Predicate predicate(String uri, int arity){
		return DATA_FACTORY.getPredicate(uri, arity);
	}
	
	public static Predicate objectProperty(String name){
		return DATA_FACTORY.getObjectPropertyPredicate(name);
	}

	public static Predicate dataProperty(String name){
		return DATA_FACTORY.getDataPropertyPredicate(name);
	}
	
	public static Predicate cls(String name){
		return DATA_FACTORY.getClassPredicate(name);
	}
	
	public static Function func(Predicate functor, Term... terms){
		return DATA_FACTORY.getFunction(functor, terms);
	}
	
	public static Function func(Predicate functor, List<Term> terms){
		return DATA_FACTORY.getFunction(functor, terms);
	}

	public static URIConstant constantURI(String uri){
		return DATA_FACTORY.getConstantURI(uri);
	}

	public static Variable var(String name){
		return DATA_FACTORY.getVariable(name);
	}
	
	public static ValueConstant constant(String value){
		return DATA_FACTORY.getConstantLiteral(value);
	}
	
	public static Function uri(Term... terms){
		return DATA_FACTORY.getUriTemplate(terms);
	}
	
	public static Function rdfsLiteral(Term term){
		return DATA_FACTORY.getTypedTerm(term, Predicate.COL_TYPE.LITERAL);
	}
	
	public static Function and(Term... terms){
		return DATA_FACTORY.getFunction(ExpressionOperation.AND, terms);
	}
	
	public static Function or(Term... terms){
		return DATA_FACTORY.getFunction(ExpressionOperation.OR, terms);
	}
	
	public static Function eq(Term... terms){
		return DATA_FACTORY.getFunction(ExpressionOperation.EQ, terms);
	}
	
	public static Function gt(Term... terms){
		return DATA_FACTORY.getFunction(ExpressionOperation.GT, terms);
	}

	public static Function isNotNull(Term term){
		return DATA_FACTORY.getFunction(ExpressionOperation.IS_NOT_NULL, term);
	}
	
	public static Function isNull(Term term){
		return DATA_FACTORY.getFunction(ExpressionOperation.IS_NULL, term);
	}
	
	public static Function lt(Term... terms){
		return DATA_FACTORY.getFunction(ExpressionOperation.LT, terms);
	}
	
	public static Function lte(Term... terms){
		return DATA_FACTORY.getFunction(ExpressionOperation.LTE, terms);
	}
	
	public static Function neq(Term... terms){
		return DATA_FACTORY.getFunction(ExpressionOperation.NEQ, terms);
	}
	
	public static Function not(Term... terms){
		return DATA_FACTORY.getFunction(ExpressionOperation.NOT, terms);
	}
	
	public static Function leftJoin(Term... terms){
		return DATA_FACTORY.getFunction(OBDAVocabulary.SPARQL_LEFTJOIN, terms);
	}
	
}
