package it.unibz.inf.ontop.answering.reformulation.generation.impl;

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

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;

public class DatalogNormalizer {

	private final TermFactory termFactory;
	private final TypeFactory typeFactory;

	@Inject
	private DatalogNormalizer(TermFactory termFactory, TypeFactory typeFactory) {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
	}


	/***
	 * Adds a trivial equality to a LeftJoin in case the left join doesn't have
	 * at least one boolean condition. This is necessary to have syntactically
	 * correct LeftJoins in SQL.
	 */

    public void addMinimalEqualityToLeftOrNestedInnerJoin(CQIE query) {
        for (Function f : query.getBody()) {
            if (f.isAlgebraFunction()) {
                addMinimalEqualityToLeftOrNestedInnerJoin(f);
            }
        }
    }

    // recursive method: algebraFunctionalTerm is checked to be isAlgebraFunction() before every call

    private void addMinimalEqualityToLeftOrNestedInnerJoin(Function algebraFunctionalTerm) {
		int booleanAtoms = 0;
		for (Term term : algebraFunctionalTerm.getTerms()) {
			Function f = (Function) term;
			if (f.isAlgebraFunction()) {
				addMinimalEqualityToLeftOrNestedInnerJoin(f);
			}
			if (f.isOperation())
				booleanAtoms++;
		}
		if (booleanAtoms == 0) {
			Function trivialEquality = termFactory.getFunctionEQ(
			        termFactory.getConstantLiteral("1", typeFactory.getXsdIntegerDatatype()),
					termFactory.getConstantLiteral("1", typeFactory.getXsdIntegerDatatype()));
			algebraFunctionalTerm.getTerms().add(trivialEquality);
		}
	}

}
