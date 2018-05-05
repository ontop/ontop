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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class DatalogMTLSyntaxParserImpl implements DatalogMTLSyntaxParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatalogMTLSyntaxParserImpl.class);
    private final TermFactory termFactory;
    private final AtomFactory atomFactory;

    @Inject
    public DatalogMTLSyntaxParserImpl(AtomFactory atomFactory, TermFactory termFactory) {
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
    }

    @Override
    public DatalogMTLProgram parse(String input) {
        CharStream inputStream = CharStreams.fromString(input.trim());
        DatalogMTLLexer lexer = new DatalogMTLLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        DatalogMTLParser parser = new DatalogMTLParser(tokenStream);
        return new DatalogMTLVisitorImpl(termFactory, atomFactory).visitParse(parser.parse());
    }

    @Override
    public DatalogMTLProgram parse(File datalogFile){
        try {
            return parse(new String(Files.readAllBytes(datalogFile.toPath()), StandardCharsets.UTF_8));
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void save(DatalogMTLProgram datalogMTLProgram, File ruleFile) {
        try (final BufferedWriter writer = Files.newBufferedWriter(ruleFile.toPath())) {
            writer.write(datalogMTLProgram.toString());
            writer.flush();
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
