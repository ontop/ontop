package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.ontology.Ontology;

/***
 * A query reformulator that does nothing on the given query. 
 * 
 * @author mariano
 *
 */
public class DummyReformulator implements QueryRewriter {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8989177354924893482L;

	@Override
	public OBDAQuery rewrite(OBDAQuery input) throws OBDAException {
		return input;
	}

	@Override
	public void setTBox(Ontology ontology) {
		// NO-OP
		
	}

	@Override
	public void setCBox(Ontology sigma) {
		// NO-OP
		
	}

	@Override
	public void initialize() {
		// NO-OP
		
	}

}
