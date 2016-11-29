package it.unibz.inf.ontop.r2rml;

/*
 * #%L
 * ontop-obdalib-sesame
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
/**
 * @author timea bagosi
 * Class responsible to write an r2rml turtle file given an obda model
 */

import com.google.common.collect.*;
import eu.optique.api.mapping.R2RMLMappingManager;
import eu.optique.api.mapping.TriplesMap;
import eu.optique.api.mapping.impl.sesame.SesameR2RMLMappingManagerFactory;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.openrdf.model.Graph;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class R2RMLWriter {

	private BufferedWriter out;
	private List<OBDAMappingAxiom> mappings;
	private URI sourceUri;
	private PrefixManager prefixmng;
	private OWLOntology ontology;
	private final OBDADataFactory OBDA_DATA_FACTORY = OBDADataFactoryImpl.getInstance();

	public R2RMLWriter(File file, OBDAModel obdamodel, URI sourceURI, OWLOntology ontology)
	{
		this(obdamodel, sourceURI, ontology);
		try {
			this.out = new BufferedWriter(new FileWriter(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public R2RMLWriter(OBDAModel obdamodel, URI sourceURI, OWLOntology ontology)
	{
		this.sourceUri = sourceURI;
		this.mappings = obdamodel.getMappings(sourceUri);
		this.prefixmng = obdamodel.getPrefixManager();
		this.ontology = ontology;
	}

	public R2RMLWriter(OBDAModel obdamodel, URI sourceURI){
		this(obdamodel, sourceURI, null);
	}

	public R2RMLWriter(File file, OBDAModel obdamodel, URI sourceURI){
		this(file, obdamodel, sourceURI, null);
	}


	/**
	 * call this method if you need the RDF Graph
	 * that represents the R2RML mappings
	 * @return an RDF Graph
	 */
	@Deprecated
	public Graph getGraph() {
		OBDAMappingTransformer transformer = new OBDAMappingTransformer();
		transformer.setOntology(ontology);
		List<Statement> statements = new ArrayList<Statement>();

		for (OBDAMappingAxiom axiom: this.mappings) {
			List<Statement> statements2 = transformer.getStatements(axiom,prefixmng);
			statements.addAll(statements2);
		}
		@SuppressWarnings("deprecation")
		Graph g = new GraphImpl();
		g.addAll(statements);
		return g;
	}

	public Collection <TriplesMap> getTripleMaps() {
		OBDAMappingTransformer transformer = new OBDAMappingTransformer();
		transformer.setOntology(ontology);
		return  splitMappingAxioms(this.mappings).stream()
				.map(a -> transformer.getTripleMap(a, prefixmng))
				.collect(Collectors.toList());
	}

	private ImmutableSet<OBDAMappingAxiom> splitMappingAxioms(List<OBDAMappingAxiom> mappingAxioms) {
		/**
		 * Delimiter string d used for assigning ids to split mapping axioms.
		 * If a mapping axiom with id j is split into multiple ones,
		 * each of then will have "j"+"d"+int as an identifier
		 */
		String delimiterSubtring = getSplitMappingAxiomIdDelimiterSubstring(mappingAxioms);
		return mappingAxioms.stream()
				.flatMap(m -> splitMappingAxiom(m, delimiterSubtring).stream())
				.collect(ImmutableCollectors.toSet());
	}

	private String getSplitMappingAxiomIdDelimiterSubstring(List<OBDAMappingAxiom> mappingAxioms) {
		String delimiterSubstring = "";
		boolean matched = false;
		do {
			delimiterSubstring += "s";
			Pattern pattern = Pattern.compile(delimiterSubstring + "(\\d)*$");
			matched = mappingAxioms.stream()
					.anyMatch(a -> pattern.matcher(a.getId()).matches());
		} while(matched);
		return delimiterSubstring;
	}

	private ImmutableList<OBDAMappingAxiom> splitMappingAxiom(OBDAMappingAxiom mappingAxiom, String delimiterSubstring) {
		Multimap<Function, Function> subjectTermToTargetTriples = ArrayListMultimap.create();
		for(Function targetTriple : mappingAxiom.getTargetQuery()){
			Function subjectTerm = getFirstFunctionalTerm(targetTriple)
						.orElseThrow( () -> new IllegalStateException("Invalid OBDA mapping"));
			subjectTermToTargetTriples.put(subjectTerm, targetTriple);
		}
		// If the partition per target triple subject is non trivial
		if(subjectTermToTargetTriples.size() > 1){
			// Create ids for the new mapping axioms
			Map<Function, String> subjectTermToMappingIndex = new HashMap<>();
			int i = 1;
			for (Function subjectTerm : subjectTermToTargetTriples.keySet()){
				subjectTermToMappingIndex.put(subjectTerm, mappingAxiom.getId()+delimiterSubstring+i);
				i++;
			}
			// Generate one mapping axiom per subject
			return subjectTermToTargetTriples.asMap().entrySet().stream()
					.map(e -> OBDA_DATA_FACTORY.getRDBMSMappingAxiom(
							subjectTermToMappingIndex.get(e.getKey()),
							mappingAxiom.getSourceQuery(),
							new ArrayList<Function>(e.getValue())))
					.collect(ImmutableCollectors.toList());
		}
		return ImmutableList.of(mappingAxiom);
	}

	private Optional<Function> getFirstFunctionalTerm (Function inputFunction) {
		return inputFunction.getTerms().stream()
				.findFirst()
				.filter(t -> t instanceof Function)
				.map(t -> (Function) t);
	}


	/**
	 * the method to write the R2RML mappings
	 * from an rdf Model to a file
	 * @param file the ttl file to write to
	 */
	public void write(File file) throws Exception {
		try {
            FileOutputStream fos = new FileOutputStream(file);
			write(fos);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

    /**
     * the method to write the R2RML mappings
     * from an rdf Model to a file
     * @param os the output target
     */
    public void write(OutputStream os) throws Exception {
        try {
            R2RMLMappingManager mm = new SesameR2RMLMappingManagerFactory().getR2RMLMappingManager();
            Collection<TriplesMap> coll = getTripleMaps();
            Model out = mm.exportMappings(coll, Model.class);
            Rio.write(out, os, RDFFormat.TURTLE);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
			throw e;
        }
    }

	
	public static void main(String args[])
	{
		String file = "/Users/mindaugas/r2rml/test2.ttl";
		try {
		R2RMLReader reader = new R2RMLReader(file);
		OWLOntology ontology = null;

		R2RMLWriter writer = new R2RMLWriter(reader.readModel(URI.create("test")),URI.create("test"), ontology);
		File out = new File("/Users/mindaugas/r2rml/out.ttl");
//		Graph g = writer.getGraph();
//		Iterator<Statement> st = g.iterator();
//		while (st.hasNext())
//			System.out.println(st.next());

			writer.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
