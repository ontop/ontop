package org.semanticweb.ontop.parser;

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

import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;

public class TurtleOBDASyntaxParser implements TargetQueryParser {

	private PrefixManager prefMan;

	/**
	 * Default constructor;
	 */
	public TurtleOBDASyntaxParser() {
		this(null);
	}

	/**
	 * Constructs the parser object with a prefix manager. This manager will
	 * help to generate the query header that contains the prefix definitions
	 * (i.e., the directives @BASE and @PREFIX).
	 * 
	 * @param manager
	 *            The prefix manager.
	 */
	public TurtleOBDASyntaxParser(PrefixManager manager) {
		setPrefixManager(manager);
	}

	/**
	 * Sets the prefix manager to this parser object. This prefix manager is
	 * used to construct the directive header. Set <i>null</i> to avoid such
	 * construction.
	 * 
	 * @param manager
	 *            The prefix manager.
	 */
	@Override
	public void setPrefixManager(PrefixManager manager) {
		prefMan = manager;
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
	public CQIE parse(String input) throws TargetQueryParserException {
		StringBuffer bf = new StringBuffer(input.trim());
		if (!bf.substring(bf.length() - 2, bf.length()).equals(" .")) {
			bf.insert(bf.length() - 1, ' ');
		}
		if (prefMan != null) {
			// Update the input by appending the directives
			appendDirectives(bf);
		}		
		try {
			ANTLRStringStream inputStream = new ANTLRStringStream(bf.toString());
			TurtleOBDALexer lexer = new TurtleOBDALexer(inputStream);
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			TurtleOBDAParser parser = new TurtleOBDAParser(tokenStream);
			return parser.parse();
		} catch (RecognitionException e) {
			throw new TargetQueryParserException(e);
		} catch (RuntimeException e) {
			throw new TargetQueryParserException(e);
		}
	}

	/**
	 * The turtle syntax predefines the quest, rdf, rdfs and owl prefixes.
	 * 
	 * Adds directives to the query header from the PrefixManager.
	 */
	private void appendDirectives(StringBuffer query) {
		Map<String, String> prefixMap = prefMan.getPrefixMap();
		StringBuffer sb = new StringBuffer();
		for (String prefix : prefixMap.keySet()) {
			sb.append("@PREFIX");
			sb.append(" ");
			sb.append(prefix);
			sb.append(" ");
			sb.append("<");
			sb.append(prefixMap.get(prefix));
			sb.append(">");
			sb.append(" .\n");
		}
		sb.append("@PREFIX " + OBDAVocabulary.PREFIX_XSD + " <" + OBDAVocabulary.NS_XSD + "> .\n");
		sb.append("@PREFIX " + OBDAVocabulary.PREFIX_QUEST + " <" + OBDAVocabulary.NS_QUEST + "> .\n");
		sb.append("@PREFIX " + OBDAVocabulary.PREFIX_RDF + " <" + OBDAVocabulary.NS_RDF + "> .\n");
		sb.append("@PREFIX " + OBDAVocabulary.PREFIX_RDFS + " <" + OBDAVocabulary.NS_RDFS + "> .\n");
		sb.append("@PREFIX " + OBDAVocabulary.PREFIX_OWL + " <" + OBDAVocabulary.NS_OWL + "> .\n");
		query.insert(0, sb);
	}
}
