package it.unibz.krdb.obda.owlrefplatform.owlapi3;

/*
 * #%L
 * ontop-quest-owlapi3
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

import com.google.common.collect.ImmutableSet;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.QuestOWLIndividualAxiomIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;

import java.util.Collection;
import java.util.Iterator;

public class OWLAPI3Materializer implements AutoCloseable{

	private final Iterator<Assertion> assertions;
	private final QuestMaterializer materializer;
	
	public OWLAPI3Materializer(OBDAModel model, boolean doStreamResults) throws Exception {
		 this(model, null, doStreamResults);
	}

	
	public OWLAPI3Materializer(OBDAModel model, Ontology onto, boolean doStreamResults) throws Exception {
		 materializer = new QuestMaterializer(model, onto, doStreamResults);
		 assertions = materializer.getAssertionIterator();
	}

    /*
     * only materialize the predicates in  `predicates`
     */
    public OWLAPI3Materializer(OBDAModel model, Ontology onto, Collection<Predicate> predicates, boolean doStreamResults) throws Exception {
        materializer = new QuestMaterializer(model, onto, predicates, doStreamResults);
        assertions = materializer.getAssertionIterator();
    }

    public OWLAPI3Materializer(OBDAModel obdaModel, Ontology onto, Predicate predicate, boolean doStreamResults)  throws Exception{
        this(obdaModel, onto, ImmutableSet.of(predicate), doStreamResults);
    }

    public QuestOWLIndividualAxiomIterator getIterator() {
		return new QuestOWLIndividualAxiomIterator(assertions);
	}
	
	public void disconnect() {
		materializer.disconnect();
	}
	
	public long getTriplesCount() { 
		try {
			return materializer.getTriplesCount();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int getVocabularySize() {
		return materializer.getVocabSize();
	}

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
