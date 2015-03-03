package org.semanticweb.ontop.owlrefplatform.core.reformulation;

/*
 * #%L
 * ontop-reformulation-core
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

import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

/***
 * A query reformulator that does nothing on the given query. 
 * 
 * @author mariano
 *
 */
public class DummyReformulator implements QueryRewriter {

	
	@Override
	public DatalogProgram rewrite(DatalogProgram input) throws OBDAException {
		return input;
	}

	@Override
	public void setTBox(TBoxReasoner ontology, LinearInclusionDependencies sigma) {
		// NO-OP		
	}
}
