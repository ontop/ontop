package it.unibz.krdb.obda.sesame;

/*
 * #%L
 * ontop-obdalib-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.ontology.Assertion;

import java.util.Iterator;

import org.openrdf.model.Statement;

/***
 * An iterator that will dynamically construct ABox assertions for the given
 * predicate based on the results of executing the mappings for the predicate in
 * each data source.
 * 
 */
public class SesameStatementIterator implements Iterator<Statement> {
	private final Iterator<Assertion> iterator;	
	

	public SesameStatementIterator(Iterator<Assertion> it) {
		this.iterator = it;
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}


    /* //another version which skips errors
    public Statement next() {
        try {
            Assertion assertion = iterator.next();
            Statement statement = new SesameStatement(assertion);
            return statement;
        } catch (IllegalArgumentException ex){
            System.err.println("Skip the assertion cannot be converted into triple: ");
            ex.printStackTrace();
            return next();
        }

    }*/

	public Statement next() {
		Assertion assertion = iterator.next();
		Statement individual = new SesameStatement(assertion);
		return individual;
	}

	public void remove() {
		throw new UnsupportedOperationException("This iterator is read-only");
	}
	
	
}
