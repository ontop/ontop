package it.unibz.inf.ontop.datalog;

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
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.LinkedList;
import java.util.List;

public class DatalogNormalizer {

	private final TermFactory termFactory;
	private final TypeFactory typeFactory;
	private final DatalogFactory datalogFactory;

	@Inject
	private DatalogNormalizer(TermFactory termFactory, TypeFactory typeFactory, DatalogFactory datalogFactory) {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.datalogFactory = datalogFactory;
	}

	public void foldJoinTrees(CQIE query) {
	    foldJoinTrees(query.getBody(), false);
	}

	private void foldJoinTrees(List atoms, boolean isJoin) {
		List<Function> dataAtoms = new LinkedList<>();
		List<Function> booleanAtoms = new LinkedList<>();

		/*
		 * Collecting all data and boolean atoms for later processing. Calling
		 * recursively fold Join trees on any algebra function.
		 */
		for (Object o : atoms) {
			Function atom = (Function) o;
			if (atom.isOperation()) {
				booleanAtoms.add(atom);
			} else {
				dataAtoms.add(atom);
				if (atom.getFunctionSymbol().equals(datalogFactory.getSparqlLeftJoinPredicate()))
					foldJoinTrees(atom.getTerms(), false);
				if (atom.getFunctionSymbol().equals(datalogFactory.getSparqlJoinPredicate()))
					foldJoinTrees(atom.getTerms(), true);
			}

		}

		if (!isJoin || dataAtoms.size() <= 2)
			return;

		/*
		 * We process all atoms in dataAtoms to make only BINARY joins. Taking
		 * two at a time and replacing them for JOINs, until only two are left.
		 * All boolean conditions of the original join go into the first join
		 * generated. It always merges from the left to the right.
		 */
		while (dataAtoms.size() > 2) {
			Function joinAtom = datalogFactory.getSPARQLJoin(dataAtoms.remove(0), dataAtoms.remove(0));
			joinAtom.getTerms().addAll(booleanAtoms);
			booleanAtoms.clear();

			dataAtoms.add(0, joinAtom);
		}
		atoms.clear();
		atoms.addAll(dataAtoms);

	}




	/***
	 * Adds a trivial equality to a LeftJoin in case the left join doesn't have
	 * at least one boolean condition. This is necessary to have syntactically
	 * correct LeftJoins in SQL.
	 */

    public void addMinimalEqualityToLeftJoin(CQIE query) {
        for (Function f : query.getBody()) {
            if (f.isAlgebraFunction()) {
                addMinimalEqualityToLeftJoin(f);
            }
        }
    }

    private void addMinimalEqualityToLeftJoin(Function leftJoin) {
		int booleanAtoms = 0;
		for (Term term : leftJoin.getTerms()) {
			Function f = (Function) term;
			if (f.isAlgebraFunction()) {
				addMinimalEqualityToLeftJoin(f);
			}
			if (f.isOperation())
				booleanAtoms++;
		}
		if (leftJoin.isAlgebraFunction() && booleanAtoms == 0) {
			Function trivialEquality = termFactory.getFunctionEQ(
			        termFactory.getConstantLiteral("1", typeFactory.getXsdIntegerDatatype()),
					termFactory.getConstantLiteral("1", typeFactory.getXsdIntegerDatatype()));
			leftJoin.getTerms().add(trivialEquality);
		}
	}

}
