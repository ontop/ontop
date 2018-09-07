package it.unibz.inf.ontop.answering.reformulation.rewriting;

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

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.LinearInclusionDependency;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

import java.util.List;

public interface QueryRewriter {

	List<CQIE> rewrite(List<CQIE> input) throws OntopReformulationException;

	/***
	 * Sets the ontology and the ABox dependencies that this rewriter should 
	 * use to compute any reformulation.
	 * 
	 * @param ontology
	 */
	void setTBox(ClassifiedTBox ontology);

	ImmutableMultimap<Predicate, LinearInclusionDependency> getSigma();
}
