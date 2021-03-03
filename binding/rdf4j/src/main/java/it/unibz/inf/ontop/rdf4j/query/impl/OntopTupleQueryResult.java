package it.unibz.inf.ontop.rdf4j.query.impl;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibz.inf.ontop.exception.OntopResultConversionException;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;

public class OntopTupleQueryResult implements TupleQueryResult {

	private final byte[] salt;
	TupleResultSet res;
	List<String> signature;
	Set<String> bindingNames;
	
	public OntopTupleQueryResult(TupleResultSet res, List<String> signature, byte[] salt){
		this.salt = salt;
		if(res == null)
			throw new NullPointerException();
		this.res = res;
		this.signature = signature;
		this.bindingNames = new HashSet<>(signature);
	}
	
	@Override
	public void close() throws QueryEvaluationException {
		try {
			res.close();
		} catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public boolean hasNext() throws QueryEvaluationException {
		try {
			return res.hasNext();
		} catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public BindingSet next() throws QueryEvaluationException {
        try {
            return new OntopRDF4JBindingSet(res.next(), salt);
        } catch (OntopConnectionException | OntopResultConversionException e) {
            throw new QueryEvaluationException(e);
		}
//
//        MapBindingSet set = new MapBindingSet(this.signature.size() * 2);
//		for (String name : this.signature) {
//			Binding binding = createBinding(name, ontopBindingSet, this.bindingNames);
//			if (binding != null) {
//				set.addBinding(binding);
//			}
//		}

	}

	@Override
	public void remove() throws QueryEvaluationException {
		throw new QueryEvaluationException("The query result is read-only. Elements cannot be removed");
	}
	

	private Binding createBinding(String bindingName, OntopBindingSet set) {
		OntopRDF4JBindingSet bset = new OntopRDF4JBindingSet(set, salt);
		return bset.getBinding(bindingName);
	}


	@Override
	public List<String> getBindingNames() throws QueryEvaluationException {
		return this.signature;
	}

}
