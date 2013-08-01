/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package inf.unibz.ontp.sesame.repository.test;

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.impl.PunningException;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.OWLAPI3Materializer;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLIndividualIterator;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import sesameWrapper.SesameMaterializer;
import sesameWrapper.SesameStatementIterator;

public class SesameMaterializerCmdTest extends TestCase {
	
	private OBDAModel model;
	private Ontology onto;
	private OWLOntology ontology = null;
	
	@Override
	public void setUp() throws IOException, InvalidMappingException {
		// obda file
		File f = new File("src/test/resources/materializer/MaterializeTest.obda");
		//create model
		model = OBDADataFactoryImpl.getInstance().getOBDAModel();
		ModelIOManager modelIO = new ModelIOManager(model);
		modelIO.load(f);
	}
	
	public void setUpOnto() throws OWLOntologyCreationException, PunningException {
		//create onto
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f = new File("src/test/resources/materializer/MaterializeTest.owl");
		// Loading the OWL ontology from the file as with normal OWLReasoners
		ontology = manager.loadOntologyFromOntologyDocument(f);
		onto =  new OWLAPI3Translator().translate(ontology);
	}
	
	public void testModelN3() throws Exception {
		// output
		File out = new File("src/test/resources/materializer/materializeN3.N3");
		String outfile = out.getAbsolutePath();
		System.out.println(outfile);
		SesameMaterializer materializer = new SesameMaterializer(model);
		SesameStatementIterator iterator = materializer.getIterator();
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8")); 
		RDFHandler handler = new N3Writer(writer);
		handler.startRDF();
		while(iterator.hasNext())
			handler.handleStatement(iterator.next());
		handler.endRDF();
		
		assertEquals(27, materializer.getTriplesCount());
		assertEquals(3, materializer.getVocabularySize());
		
		materializer.disconnect();
		if (out!=null)
			writer.close();
	}
	
	public void testModelTurtle() throws Exception {
		// output
		File out = new File("src/test/resources/materializer/materializeTurtle.ttl");
		String outfile = out.getAbsolutePath();
		System.out.println(outfile);

		// output
		SesameMaterializer materializer = new SesameMaterializer(model);
		SesameStatementIterator iterator = materializer.getIterator();
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(out), "UTF-8"));
		RDFHandler handler = new TurtleWriter(writer);
		handler.startRDF();
		while (iterator.hasNext())
			handler.handleStatement(iterator.next());
		handler.endRDF();

		assertEquals(27, materializer.getTriplesCount());
		assertEquals(3, materializer.getVocabularySize());

		materializer.disconnect();
		if (out != null)
			writer.close();

	}

	public void testModelRdfXml() throws Exception {
		// output
		File out = new File("src/test/resources/materializer/materializeRdf.owl");
		String outfile = out.getAbsolutePath();
		System.out.println(outfile);

		// output
		SesameMaterializer materializer = new SesameMaterializer(model);
		SesameStatementIterator iterator = materializer.getIterator();
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(out), "UTF-8"));
		RDFHandler handler = new RDFXMLWriter(writer);
		handler.startRDF();
		while (iterator.hasNext())
			handler.handleStatement(iterator.next());
		handler.endRDF();

		assertEquals(27, materializer.getTriplesCount());
		assertEquals(3, materializer.getVocabularySize());

		materializer.disconnect();
		if (out != null)
			writer.close();

	}
	
	public void testModelOntoN3() throws Exception {
		// output
		File out = new File("src/test/resources/materializer/materializeN3.N3");
		String outfile = out.getAbsolutePath();
		System.out.println(outfile);
		
		setUpOnto();
		SesameMaterializer materializer = new SesameMaterializer(model, onto);
		SesameStatementIterator iterator = materializer.getIterator();
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8")); 
		RDFHandler handler = new N3Writer(writer);
		handler.startRDF();
		while(iterator.hasNext())
			handler.handleStatement(iterator.next());
		handler.endRDF();
		
		assertEquals(51, materializer.getTriplesCount());
		assertEquals(5, materializer.getVocabularySize());
		
		materializer.disconnect();
		if (out!=null)
			writer.close();
	}
	
	public void testModelOntoTurtle() throws Exception {
		// output
		File out = new File("src/test/resources/materializer/materializeTurtle.ttl");
		String outfile = out.getAbsolutePath();
		System.out.println(outfile);

		setUpOnto();
		// output
		SesameMaterializer materializer = new SesameMaterializer(model, onto);
		SesameStatementIterator iterator = materializer.getIterator();
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(out), "UTF-8"));
		RDFHandler handler = new TurtleWriter(writer);
		handler.startRDF();
		while (iterator.hasNext())
			handler.handleStatement(iterator.next());
		handler.endRDF();

		assertEquals(51, materializer.getTriplesCount());
		assertEquals(5, materializer.getVocabularySize());

		materializer.disconnect();
		if (out != null)
			writer.close();

	}

	public void testModelOntoRdfXml() throws Exception {
		// output
		File out = new File("src/test/resources/materializer/materializeRdf.owl");
		String outfile = out.getAbsolutePath();
		System.out.println(outfile);

		setUpOnto();
		// output
		SesameMaterializer materializer = new SesameMaterializer(model, onto);
		SesameStatementIterator iterator = materializer.getIterator();
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(out), "UTF-8"));
		RDFHandler handler = new RDFXMLWriter(writer);
		handler.startRDF();
		while (iterator.hasNext())
			handler.handleStatement(iterator.next());
		handler.endRDF();

		assertEquals(51, materializer.getTriplesCount());
		assertEquals(5, materializer.getVocabularySize());

		materializer.disconnect();
		if (out != null)
			writer.close();

	}
	
	public void testOWLApiModel() throws Exception {
		File out = new File("src/test/resources/materializer/materializeOWL.owl");
		String outfile = out.getAbsolutePath();
		System.out.println(outfile);
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(out)); 
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
	
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.createOntology(IRI.create(out));
		manager = ontology.getOWLOntologyManager();
		OWLAPI3Materializer materializer = new OWLAPI3Materializer(model);

		
		QuestOWLIndividualIterator iterator = materializer.getIterator();
		
		while(iterator.hasNext()) 
			manager.addAxiom(ontology, iterator.next());
		manager.saveOntology(ontology, new OWLXMLOntologyFormat(), new WriterDocumentTarget(writer));	
		
		assertEquals(27, materializer.getTriplesCount());
		assertEquals(3, materializer.getVocabularySize());
		
		materializer.disconnect();
		if (out!=null)
			output.close();
	}
	
	public void testOWLApiModeOnto() throws Exception {
		File out = new File("src/test/resources/materializer/materializeOWL2.owl");
		String outfile = out.getAbsolutePath();
		System.out.println(outfile);
		
		setUpOnto();
		
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLAPI3Materializer	materializer = new OWLAPI3Materializer(model, onto);
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(out)); 
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
		QuestOWLIndividualIterator iterator = materializer.getIterator();

		while(iterator.hasNext()) 
			manager.addAxiom(ontology, iterator.next());
		manager.saveOntology(ontology, new OWLXMLOntologyFormat(), new WriterDocumentTarget(writer));	
		
		assertEquals(51, materializer.getTriplesCount());
		assertEquals(5, materializer.getVocabularySize());
		
		materializer.disconnect();
		if (out!=null)
			output.close();
	}
}
