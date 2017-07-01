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


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.rdf4j.RDF4JGraph;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class SQLPPMappingToR2RMLConverter {

	private List<SQLPPTriplesMap> ppMappingAxioms;
	private PrefixManager prefixmng;
	private OWLOntology ontology;

    public SQLPPMappingToR2RMLConverter(SQLPPMapping ppMapping, OWLOntology ontology)
	{
		this.ppMappingAxioms = ppMapping.getTripleMaps();
		this.prefixmng = ppMapping.getMetadata().getPrefixManager();
		this.ontology = ontology;
	}

	public Collection <TriplesMap> getTripleMaps() {
		OBDAMappingTransformer transformer = new OBDAMappingTransformer();
		transformer.setOntology(ontology);
		return  splitMappingAxioms(this.ppMappingAxioms).stream()
				.map(a -> transformer.getTriplesMap(a, prefixmng))
				.collect(Collectors.toList());
	}

	private ImmutableSet<SQLPPTriplesMap> splitMappingAxioms(List<SQLPPTriplesMap> mappingAxioms) {
		/*
		 * Delimiter string d used for assigning ids to split mapping axioms.
		 * If a mapping axiom with id j is split into multiple ones,
		 * each of then will have "j"+"d"+int as an identifier
		 */
		String delimiterSubstring = getSplitMappingAxiomIdDelimiterSubstring(mappingAxioms);
		return mappingAxioms.stream()
				.flatMap(m -> splitMappingAxiom(m, delimiterSubstring).stream())
				.collect(ImmutableCollectors.toSet());
	}

	private String getSplitMappingAxiomIdDelimiterSubstring(List<SQLPPTriplesMap> mappingAxioms) {
		String delimiterSubstring = "";
		boolean matched;
		do {
			delimiterSubstring += "_";
			Pattern pattern = Pattern.compile(delimiterSubstring + "(\\d)*$");
			matched = mappingAxioms.stream()
					.anyMatch(a -> pattern.matcher(a.getId()).matches());
		} while(matched);
		return delimiterSubstring;
	}

	private ImmutableList<SQLPPTriplesMap> splitMappingAxiom(SQLPPTriplesMap mappingAxiom, String delimiterSubstring) {
		Multimap<ImmutableFunctionalTerm, ImmutableFunctionalTerm> subjectTermToTargetTriples = ArrayListMultimap.create();
		for(ImmutableFunctionalTerm targetTriple : mappingAxiom.getTargetAtoms()){
			ImmutableFunctionalTerm subjectTerm = getFirstFunctionalTerm(targetTriple)
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
					.map(e -> mappingAxiom.extractPPMappingAssertions(
							subjectTermToMappingIndex.get(e.getKey()),
							ImmutableList.copyOf(e.getValue())))
					.collect(ImmutableCollectors.toList());
		}
		return ImmutableList.of(mappingAxiom);
	}

	private Optional<ImmutableFunctionalTerm> getFirstFunctionalTerm (ImmutableFunctionalTerm inputFunction) {
		return inputFunction.getArguments().stream()
				.findFirst()
				.filter(t -> t instanceof ImmutableFunctionalTerm)
				.map(t -> (ImmutableFunctionalTerm) t);
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
            RDF4JR2RMLMappingManager mm = RDF4JR2RMLMappingManager.getInstance();
            Collection<TriplesMap> coll = getTripleMaps();
            final RDF4JGraph rdf4JGraph = mm.exportMappings(coll);

            Rio.write(rdf4JGraph.asModel().get() , os, RDFFormat.TURTLE);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
			throw e;
		}
	}
}
