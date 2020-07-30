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


public abstract class AbstractTurtleOBDAParser implements TargetQueryParser {

	private final ImmutableMap<String, String> prefixes;
	private final Supplier<TurtleOBDAVisitor> visitorSupplier;

	/**
	 * Constructs the parser object with prefixes. These prefixes will
	 * help to generate the query header that contains the prefix definitions
	 * (i.e., the directives @BASE and @PREFIX).
	 *
	 */
	public AbstractTurtleOBDAParser(ImmutableMap<String, String> prefixes,
									Supplier<TurtleOBDAVisitor> visitorSupplier) {
		this.prefixes = prefixes;
		this.visitorSupplier = visitorSupplier;
	}

	/**
	 * Returns the CQIE object from the input string. If the input prefix
	 * manager is null then no directive header will be appended.
	 * 
	 * @param input
	 *            A target query string written in Turtle syntax.
	 * @return A CQIE object.
	 */
	@Override
	public ImmutableList<TargetAtom> parse(String input) throws TargetQueryParserException {
		StringBuffer bf = new StringBuffer(input.trim());
		if (!bf.substring(bf.length() - 2, bf.length()).equals(" .")) {
			bf.insert(bf.length() - 1, ' ');
		}
		if (!prefixes.isEmpty()) {
			// Update the input by appending the directives
			appendDirectives(bf);
		}		
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
		} catch (RuntimeException e) {
			throw new TargetQueryParserException(e.getMessage(), e);
		}
	}

	/**
	 * The turtle syntax predefines the quest, rdf, rdfs and owl prefixes.
	 * 
	 * Adds directives to the query header from the PrefixManager.
	 */
	private void appendDirectives(StringBuffer query) {
		StringBuffer sb = new StringBuffer();
		for (String prefix : prefixes.keySet()) {
			sb.append("@PREFIX");
			sb.append(" ");
			sb.append(prefix);
			sb.append(" ");
			sb.append("<");
			sb.append(prefixes.get(prefix));
			sb.append(">");
			sb.append(" .\n");
		}
		sb.append("@PREFIX " + OntopInternal.PREFIX_XSD + " <" + XSD.PREFIX + "> .\n");
		sb.append("@PREFIX " + OntopInternal.PREFIX_OBDA + " <" + Ontop.PREFIX + "> .\n");
		sb.append("@PREFIX " + OntopInternal.PREFIX_RDF + " <" + RDF.PREFIX + "> .\n");
		sb.append("@PREFIX " + OntopInternal.PREFIX_RDFS + " <" + RDFS.PREFIX + "> .\n");
		sb.append("@PREFIX " + OntopInternal.PREFIX_OWL + " <" + OWL.PREFIX + "> .\n");
		query.insert(0, sb);
	}
}
