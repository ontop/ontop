package inf.unibz.it.obda.owlapi.sparql;

import java.util.Set;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.util.SimpleURIShortFormProvider;

public class SPARQLPrefixBuilder {

	public String getDefaultPrefix(Set<OWLOntology> activeOntologies, OWLOntology activeOntology) {
		SimpleURIShortFormProvider shortFormProvider = new SimpleURIShortFormProvider();
		String sparqlPrefix = "";
		String defaultNamespace = activeOntology.getURI().toString();
		if (defaultNamespace.endsWith("#")) {
			sparqlPrefix += "BASE <" + defaultNamespace.substring(0, defaultNamespace.length() - 1) + ">\n";
		} else {
			sparqlPrefix += "BASE <" + defaultNamespace + ">\n";
		}
		sparqlPrefix += "PREFIX :   <" + defaultNamespace + "#>\n";

		for (OWLOntology ontology : activeOntologies) {
			if (ontology == activeOntology)
				continue;
			sparqlPrefix += "PREFIX " + shortFormProvider.getShortForm(ontology.getURI()) + ": <" + ontology.getURI() + ">\n";
		}
		// BASE <http://www.owl-ontologies.com/Ontology1222766179.owl>
		// PREFIX : <http://www.owl-ontologies.com/Ontology1222766179.owl#>
		// PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		// PREFIX owl: <http://www.w3.org/2002/07/owl#>
		// PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
		// PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

		sparqlPrefix += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
		sparqlPrefix += "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
		sparqlPrefix += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		sparqlPrefix += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
		sparqlPrefix += "PREFIX obdap: <http://obda.org/mapping/predicates/>\n";
		// queryString += "PREFIX xs: <http://www.w3.org/2001/XMLSchema#>\n";

		return sparqlPrefix;
	}

}
