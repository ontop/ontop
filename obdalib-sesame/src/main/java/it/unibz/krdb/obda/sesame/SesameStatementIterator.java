package it.unibz.krdb.obda.sesame;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openrdf.model.Statement;

/***
 * An iterator that will dynamically construct ABox assertions for the given
 * predicate based on the results of executing the mappings for the predicate in
 * each data source.
 * 
 */
public class SesameStatementIterator implements Iterator<Statement> {
	private Iterator<Assertion> iterator;	
	

	public SesameStatementIterator(Iterator<Assertion> it) {
		this.iterator = it;
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public Statement next() {
		Assertion assertion = iterator.next();
		Statement individual = new SesameStatement(assertion);
		return individual;
	}

	public void remove() {
		throw new UnsupportedOperationException("This iterator is read-only");
	}
	
	
}
