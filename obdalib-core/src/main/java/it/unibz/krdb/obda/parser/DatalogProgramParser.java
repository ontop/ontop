/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.parser;

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
