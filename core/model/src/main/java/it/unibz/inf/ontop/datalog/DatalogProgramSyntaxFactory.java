package it.unibz.inf.ontop.datalog;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import it.unibz.inf.ontop.datalog.impl.DatalogAlgebraOperatorPredicates;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;


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
		DatalogProgram datalogProgram = DATALOG_FACTORY.getDatalogProgram();
		datalogProgram.appendRule(rules);
		return datalogProgram;
	}
	
	public static CQIE rule(Function head, Function... body){
		return DATALOG_FACTORY.getCQIE(head, body);
	}
	
	public static CQIE rule(Function head, List<Function> body){
		return DATALOG_FACTORY.getCQIE(head, body);
	}
	
	public static Predicate predicate(String uri, int arity){
		return TERM_FACTORY.getPredicate(uri, arity);
	}
	
	public static Predicate objectProperty(String name){
		return TERM_FACTORY.getObjectPropertyPredicate(name);
	}

	public static Predicate dataProperty(String name){
		return TERM_FACTORY.getDataPropertyPredicate(name);
	}
	
	public static Predicate cls(String name){
		return TERM_FACTORY.getClassPredicate(name);
	}
	
	public static Function func(Predicate functor, Term... terms){
		return TERM_FACTORY.getFunction(functor, terms);
	}
	
	public static Function func(Predicate functor, List<Term> terms){
		return TERM_FACTORY.getFunction(functor, terms);
	}

	public static URIConstant constantURI(String uri){
		return TERM_FACTORY.getConstantURI(uri);
	}

	public static Variable var(String name){
		return TERM_FACTORY.getVariable(name);
	}
	
	public static ValueConstant constant(String value){
		return TERM_FACTORY.getConstantLiteral(value);
	}
	
	public static Function uri(Term... terms){
		return TERM_FACTORY.getUriTemplate(terms);
	}
	
	public static Function rdfsLiteral(Term term){
		return TERM_FACTORY.getTypedTerm(term, Predicate.COL_TYPE.LITERAL);
	}
	
	public static Function and(Term... terms){
		return TERM_FACTORY.getFunction(ExpressionOperation.AND, terms);
	}
	
	public static Function or(Term... terms){
		return TERM_FACTORY.getFunction(ExpressionOperation.OR, terms);
	}
	
	public static Function eq(Term... terms){
		return TERM_FACTORY.getFunction(ExpressionOperation.EQ, terms);
	}
	
	public static Function gt(Term... terms){
		return TERM_FACTORY.getFunction(ExpressionOperation.GT, terms);
	}

	public static Function isNotNull(Term term){
		return TERM_FACTORY.getFunction(ExpressionOperation.IS_NOT_NULL, term);
	}
	
	public static Function isNull(Term term){
		return TERM_FACTORY.getFunction(ExpressionOperation.IS_NULL, term);
	}
	
	public static Function lt(Term... terms){
		return TERM_FACTORY.getFunction(ExpressionOperation.LT, terms);
	}
	
	public static Function lte(Term... terms){
		return TERM_FACTORY.getFunction(ExpressionOperation.LTE, terms);
	}
	
	public static Function neq(Term... terms){
		return TERM_FACTORY.getFunction(ExpressionOperation.NEQ, terms);
	}
	
	public static Function not(Term... terms){
		return TERM_FACTORY.getFunction(ExpressionOperation.NOT, terms);
	}
	
	public static Function leftJoin(Term... terms){
		return TERM_FACTORY.getFunction(DatalogAlgebraOperatorPredicates.SPARQL_LEFTJOIN, terms);
	}
	
}
