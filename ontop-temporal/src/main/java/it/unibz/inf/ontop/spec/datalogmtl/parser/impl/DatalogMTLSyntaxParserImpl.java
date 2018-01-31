package it.unibz.inf.ontop.spec.datalogmtl.parser.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLLexer;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLParser;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLSyntaxParser;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Map;

public class DatalogMTLSyntaxParserImpl implements DatalogMTLSyntaxParser {

    private final TermFactory termFactory;
    private final AtomFactory atomFactory;

    @Inject
    public DatalogMTLSyntaxParserImpl(AtomFactory atomFactory, TermFactory termFactory) {
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
    }

    @Override
    public DatalogMTLProgram parse(String input) {

        StringBuffer bf = new StringBuffer(input.trim());
        CharStream inputStream = CharStreams.fromString(bf.toString());
        DatalogMTLLexer lexer = new DatalogMTLLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        DatalogMTLParser parser = new DatalogMTLParser(tokenStream);
        return new DatalogMTLVisitorImpl(termFactory, atomFactory).visitParse(parser.parse());
    }
}
