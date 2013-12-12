package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.io.PrefixManager;

import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

public class TurtleSyntaxParser implements TargetQueryParser {

	private PrefixManager prefMan;

	/**
	 * Default constructor;
	 */
	public TurtleSyntaxParser() {
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
	public TurtleSyntaxParser(PrefixManager manager) {
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
			TurtleLexer lexer = new TurtleLexer(inputStream);
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			TurtleParser parser = new TurtleParser(tokenStream);
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
