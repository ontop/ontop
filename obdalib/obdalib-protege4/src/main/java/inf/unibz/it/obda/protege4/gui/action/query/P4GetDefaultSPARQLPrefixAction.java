package inf.unibz.it.obda.protege4.gui.action.query;

import inf.unibz.it.obda.api.io.PrefixManager;
import inf.unibz.it.obda.gui.swing.action.GetDefaultSPARQLPrefixAction;

import java.util.Iterator;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.prefix.PrefixMapper;
import org.protege.editor.owl.ui.prefix.PrefixMapperManager;
import org.semanticweb.owl.model.OWLOntology;


public class P4GetDefaultSPARQLPrefixAction extends GetDefaultSPARQLPrefixAction {
	
	private OWLModelManager modelmanager = null;
	private String prefix = "";

	public Object getResult() {
		return prefix;
	}
	
	public P4GetDefaultSPARQLPrefixAction(OWLModelManager manager) {
		this.modelmanager = manager;
	}

	public void run() {
		Set<OWLOntology> activeOntologies = modelmanager.getActiveOntologies();
		OWLOntology activeOntology = modelmanager.getActiveOntology();
				
		String queryString = "";
		String defaultNamespace = activeOntology.getURI().toString();
		if (defaultNamespace.endsWith("#")) {
			queryString += "BASE <" + defaultNamespace.substring(0, defaultNamespace.length() - 1) + ">\n";
		} else {
			queryString += "BASE <" + defaultNamespace + ">\n";
		}
		queryString += "PREFIX :   <" + defaultNamespace + "#>\n";
		
		for (OWLOntology ontology: activeOntologies) {
			if (ontology == activeOntology)
				continue;
//			String prefix = (String) prefixes.next();
//			String namespace = owlModel.getNamespaceManager().getNamespaceForPrefix(prefix);
			queryString += "PREFIX " + modelmanager.getURIRendering(ontology.getURI()) + ": <" + ontology.getURI() + ">\n";
		}
//		BASE <http://www.owl-ontologies.com/Ontology1222766179.owl>
//		PREFIX :   <http://www.owl-ontologies.com/Ontology1222766179.owl#>
//		PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
//		PREFIX owl: <http://www.w3.org/2002/07/owl#>
//		PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
//		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

//		queryString += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
//		queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
//		queryString += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
//		queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
//		queryString += "PREFIX xs: <http://www.w3.org/2001/XMLSchema#>\n";
		
		PrefixMapper mapper = PrefixMapperManager.getInstance().getMapper();
		Set<String> prefixes = mapper.getPrefixes();
		Iterator<String> it = prefixes.iterator();
		while(it.hasNext()){
			String prefix = it.next();
			String uri = mapper.getValue(prefix);
			queryString += "PREFIX " + prefix + ": <" + uri + ">\n";
		}
				 
		prefix = queryString;
		System.out.println(prefix);
	}

}
