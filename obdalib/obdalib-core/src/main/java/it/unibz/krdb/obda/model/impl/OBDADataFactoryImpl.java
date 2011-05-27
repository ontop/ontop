package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Query;
import it.unibz.krdb.obda.model.RDBMSMappingAxiom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.sun.msv.datatype.xsd.XSDatatype;


public class OBDADataFactoryImpl  implements OBDADataFactory {

	private static OBDADataFactoryImpl instance = null;

	protected OBDADataFactoryImpl(){
		// protected constructor prevents instantiation from other classes.
	}

	public static OBDADataFactory getInstance(){
		if(instance == null){
			instance = new OBDADataFactoryImpl();
		}
		return instance;
	}
	

	
	public OBDAModel getOBDAModel() {
		return new OBDAModelImpl();
	}

	public PredicateImpl getPredicate(URI name, int arity) {
		return new PredicateImpl(name, arity);
	}
	
	@Override
	public URIConstant getURIConstant(URI uri) {
		return new URIConstantImpl(uri);
	}

	@Override
	public ValueConstant getValueConstant(String value) {
		return new ValueConstantImpl(value, null);
	}

	@Override
	public ValueConstant getValueConstant(String value, XSDatatype type) {
		return new ValueConstantImpl(value, type);
	}

	@Override
	public Variable getVariable(String name) {
		return new VariableImpl(name, null);
	}

	@Override
	public Variable getVariable(String name, XSDatatype type) {
		return new VariableImpl(name, type);
	}

	@Override
	public Variable getNondistinguishedVariable() {
		return new UndistinguishedVariable();
	}

	@Override
	public Function getFunctionalTerm(Predicate functor, List<Term> arguments){
		return new FunctionalTermImpl(functor, arguments);
	}
	
	@Override
	public Function getFunctionalTerm(Predicate functor, Term term1){
		return new FunctionalTermImpl(functor, Collections.singletonList(term1));
	}
	
	@Override
	public Function getFunctionalTerm(Predicate functor, Term term1, Term term2){
		LinkedList<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		return new FunctionalTermImpl(functor, terms);
	}

	@Override
	public DataSource getDataSource(URI id) {
		return new DataSourceImpl(id);
	}

	@Override
	public Atom getAtom(Predicate predicate, List<Term> terms) {
		return new AtomImpl(predicate, terms);
	}
	
	@Override
	public Atom getAtom(Predicate predicate, Term term1) {
		return new AtomImpl(predicate, Collections.singletonList(term1));
	}
	
	@Override
	public Atom getAtom(Predicate predicate, Term term1, Term term2) {
		LinkedList<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		return new AtomImpl(predicate, terms);
	}

	@Override
	public CQIE getCQIE(Atom head, List<Atom> body) {
		return new CQIEImpl( head,  body);
	}
	
	@Override
	public CQIE getCQIE(Atom head, Atom body) {
		return new CQIEImpl( head,  Collections.singletonList(body));
	}

	@Override
	public DatalogProgram getDatalogProgram() {
		return new DatalogProgramImpl();
	}
	
	@Override
	public DatalogProgram getDatalogProgram(CQIE rule) {
		DatalogProgram p = new DatalogProgramImpl();
		p.appendRule(rule);
		return p;
	}
	
	@Override
	public DatalogProgram getDatalogProgram(List<CQIE> rules) {
		DatalogProgram p = new DatalogProgramImpl();
		p.appendRule(rules);
		return p;
	}

	@Override
	public RDBMSMappingAxiomImpl getRDBMSMappingAxiom(String id, Query sourceQuery, Query targetQuery) {
		return new RDBMSMappingAxiomImpl(id, sourceQuery, targetQuery);
	}

	@Override
	public SQLQueryImpl getSQLQuery(String query) {
		return new SQLQueryImpl(query);
	}

	@Override
	public RDBMSMappingAxiom getRDBMSMappingAxiom(String id, String sql, Query targetQuery) {
		return new RDBMSMappingAxiomImpl(id, new SQLQueryImpl(sql), targetQuery);
	}

	@Override
	public RDBMSMappingAxiom getRDBMSMappingAxiom(String sql, Query targetQuery) {
		String id = new String("MAP" + System.currentTimeMillis());
		return getRDBMSMappingAxiom(id, sql, targetQuery);
	}

}
