package it.unibz.krdb.obda.parser;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

/**
 * Executes the parsing process with a reference to a Parser object.
 */
public class DatalogProgramParser {

	private DatalogParser parser;

	private DatalogProgram datalog;

	/**
	 * Default constructor;
	 */
	public DatalogProgramParser() { }

    /**
     * Returns the datalog object from the parsing process.
	 *
	 * @param query a string of Datalog query.
	 * @return the datalog object.
     * @throws RecognitionException the syntax is not supported yet.
	 */
	public DatalogProgram parse(String query) throws RecognitionException {

		ANTLRStringStream inputStream = new ANTLRStringStream(query);
		DatalogLexer lexer = new DatalogLexer(inputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		parser = new DatalogParser(tokenStream);

		datalog = parser.parse();
		if (parser.getNumberOfSyntaxErrors() != 0) {
			throw new RecognitionException();
		}
		return datalog;
	}

	/**
	 * Returns the rule from the datalog object based on the index number.
	 *
	 * @param index the rule index.
	 * @return a conjunctive query object.
	 */
	public CQIE getRule(int index) {
		return datalog.getRules().get(index);
	}
}
