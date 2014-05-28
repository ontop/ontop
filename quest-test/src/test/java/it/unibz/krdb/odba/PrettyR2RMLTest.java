package it.unibz.krdb.odba;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Iterator;

import org.callimachusproject.io.TurtleStreamWriter;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleWriter;

import eu.optique.api.mapping.R2RMLMappingManager;
import eu.optique.api.mapping.R2RMLMappingManagerFactory;

public class PrettyR2RMLTest {
	@Test
	public void test1() throws Exception {
		FileInputStream fis = new FileInputStream(new File("src/test/resources/r2rml/npd.ttl"));
		
		R2RMLMappingManager mm = R2RMLMappingManagerFactory.getSesameMappingManager();
		
		// Read the file into a model.
		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
		Model m = new LinkedHashModel();
		rdfParser.setRDFHandler(new StatementCollector(m));
		rdfParser.parse(fis, "http://example.org");
		
		FileWriter fw = new FileWriter(new File("src/test/resources/r2rml/npd.pretty.ttl"));
		
		TurtleWriter writer = new TurtleStreamWriter(fw, null);
		
		writer.startRDF();
		
		writer.handleNamespace("rr", "http://www.w3.org/ns/r2rml#");
		writer.handleNamespace("npd", "http://sws.ifi.uio.no/data/npd-v2");
		writer.handleNamespace("void", "http://rdfs.org/ns/void#");
		writer.handleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		writer.handleNamespace("owl", "http://www.w3.org/2002/07/owl#");
		writer.handleNamespace("ex", "http://example.org/ex#");
		writer.handleNamespace("mastro", "http://www.dis.uniroma1.it/mastro#");
		
		
		
		Iterator<Statement> it = m.iterator();
		while(it.hasNext()){
			writer.handleStatement(it.next());
		}
		
		writer.endRDF();
		
			
	}
}
