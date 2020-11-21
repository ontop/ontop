package it.unibz.inf.ontop.spec.mapping.parser.impl;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.vocabulary.*;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.parser.impl.listener.ThrowingErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class AbstractTurtleOBDAParser implements TargetQueryParser {

    private final String prefixesString;
	private final Supplier<TurtleOBDAVisitor> visitorSupplier;

	/**
	 * Constructs the parser object with prefixes. These prefixes will
	 * help to generate the query header that contains the prefix definitions
	 * (i.e., the directives @base and @prefix).
	 *
	 */
	public AbstractTurtleOBDAParser(ImmutableMap<String, String> prefixes,
									Supplier<TurtleOBDAVisitor> visitorSupplier) {
		this.prefixesString = !prefixes.isEmpty() ? getPrefixDirectives(prefixes) : "";
		this.visitorSupplier = visitorSupplier;
	}

	/**
	 * Returns the list of TargetAtom objects from the input string.
	 * If the input prefix manager is empty then no directive header will be appended.
	 * 
	 * @param input A target query string written in Turtle syntax.
	 * @return a list of TargetAtom objects.
	 */
	@Override
	public ImmutableList<TargetAtom> parse(String input) throws TargetQueryParserException {
		StringBuilder bf = new StringBuilder(input.trim());
		if (!bf.substring(bf.length() - 2, bf.length()).equals(" .")) {
			bf.insert(bf.length() - 1, ' ');
		}
		// Update the input by appending the directives
		bf.insert(0, prefixesString);
		try {
			CharStream inputStream = CharStreams.fromString(bf.toString());
			TurtleOBDALexer lexer = new TurtleOBDALexer(inputStream);

			//substitute the standard ConsoleErrorListener (simply print out the error) with ThrowingErrorListener
            lexer.removeErrorListeners();
			lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			TurtleOBDAParser parser = new TurtleOBDAParser(tokenStream);
            //substitute the standard ConsoleErrorListener (simply print out the error) with ThrowingErrorListener
			parser.removeErrorListeners();
			parser.addErrorListener(ThrowingErrorListener.INSTANCE);

			return (ImmutableList<TargetAtom>)visitorSupplier.get().visitParse(parser.parse());
		}
		catch (RuntimeException e) {
			throw new TargetQueryParserException(e.getMessage(), e);
		}
	}

	/**
	 * The turtle syntax predefines the quest, rdf, rdfs and owl prefixes.
	 */
	private static final ImmutableMap<String, String> standardPrefixes = ImmutableMap.of(
			OntopInternal.PREFIX_XSD, XSD.PREFIX,
			OntopInternal.PREFIX_OBDA, Ontop.PREFIX,
			OntopInternal.PREFIX_RDF, RDF.PREFIX,
			OntopInternal.PREFIX_RDFS, RDFS.PREFIX,
			OntopInternal.PREFIX_OWL, OWL.PREFIX);

	private static String getPrefixDirectives(ImmutableMap<String, String> prefixes) {
		return Stream.concat(prefixes.entrySet().stream(), standardPrefixes.entrySet().stream())
				.map(e -> "@prefix " + e.getKey() + " <" + e.getValue() + "> .\n")
				.collect(Collectors.joining());
	}
}
