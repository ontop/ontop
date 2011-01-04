package inf.unibz.it.obda.owlapi.abox.materialization;

import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.datasource.JDBCConnectionManager;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.owlapi.OWLAPIController;

import java.io.File;
import java.net.URI;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.obda.query.domain.Atom;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.FunctionalTermImpl;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.NamespaceManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.DefaultNamespaceManager;

public class AboxMaterializer {

	private OWLAPIController controller = null;
	private String						owlFile			= null;
	private OWLOntologyManager	manager			= null;
	private OWLOntology currentOntology = null;
	private OWLDataFactory factory = null;


	private AboxMaterializer(String owlfile){

		this.owlFile= owlfile;
		try {
			this.manager = OWLManager.createOWLOntologyManager();
			this.factory = manager.getOWLDataFactory();
			currentOntology = manager.loadOntologyFromPhysicalURI((new File(owlFile)).toURI());
			controller = new OWLAPIController(this.manager, currentOntology);
			controller.loadData(new File(owlFile).toURI());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	public AboxMaterializer(){

	}

	public Set<OWLIndividualAxiom> materializeAbox(String file,OWLOntology new_onto, OWLOntologyManager man) throws Exception{

		if(manager == null){
			currentOntology = new_onto;
			factory = man.getOWLDataFactory();
			manager = man;
			controller = new OWLAPIController(this.manager, currentOntology);
			controller.loadData(new File(file).toURI());
		}
		Set<OWLIndividualAxiom> individuals = new HashSet<OWLIndividualAxiom>();
		JDBCConnectionManager jdbcMan = JDBCConnectionManager.getJDBCConnectionManager();
		List<OBDAMappingAxiom> maps = getMappings();
		DataSource ds = controller.getDatasourcesController().getCurrentDataSource();
		Iterator<OBDAMappingAxiom> it = maps.iterator();
		HashSet<String> classesURIs = new HashSet<String>();
		HashSet<String> dataProperties = new HashSet<String>();
		HashSet<String> objectProperties = new HashSet<String>();
		Set<OWLClass> set = currentOntology.getClassesInSignature();
		String ontoUri = currentOntology.getURI().toString();
		NamespaceManager nm = new DefaultNamespaceManager(ontoUri);
		Iterator<OWLClass> sit = set.iterator();
		while(sit.hasNext()){
			OWLClass c = sit.next();
			classesURIs.add(c.getURI().toString());
//			System.out.println(c.getURI().toString());
		}
		for (OWLDataProperty c: currentOntology.getDataPropertiesInSignature()) {
			dataProperties.add(c.getURI().toString());
		}
		for (OWLObjectProperty c: currentOntology.getObjectPropertiesInSignature()) {
			objectProperties.add(c.getURI().toString());
		}
		while (it.hasNext()) {
			OBDAMappingAxiom ax = it.next();
			String sql = ax.getSourceQuery().toString();
			CQIEImpl cq = (CQIEImpl) ax.getTargetQuery();
			List<Atom> atoms = cq.getBody();
			Iterator<Atom> a_it = atoms.iterator();
			while (a_it.hasNext()) {
				ResultSet res = jdbcMan.executeQuery(ds, sql);
				Atom atom = a_it.next();
				String name = atom.getPredicate().getName().getFragment();
				String uri = ontoUri+"#"+name;
				int arity = atom.getArity();
				if (arity == 1) {  // Concept query atom
					if (classesURIs.contains(uri)) {
						List<Term> terms = atom.getTerms();
						while (res.next()) {
							Iterator<Term> teit = terms.iterator();
							while (teit.hasNext()) {
								FunctionalTermImpl ft = (FunctionalTermImpl) teit.next();
								StringBuffer sb = new StringBuffer();
								sb.append(ft.getName());
								sb.append("-");
								List<Term> parameters = ft.getTerms();
								Iterator<Term> pit = parameters.iterator();
								StringBuffer aux = new StringBuffer();
								while (pit.hasNext()) {
									if (aux.length() > 0) {
										aux.append("-");
									}
									Term qt = pit.next();
									String s = res.getString(qt.getName());
									aux.append(s);
								}
								sb.append(aux.toString());
//								System.out.println(sb.toString());
								OWLIndividual ind = factory.getOWLIndividual(sb.toString(), nm);
								OWLClass clazz = factory.getOWLClass(new URI(uri));
								OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(ind, clazz);
//								manager.addAxiom(currentOntology, classAssertion);
								individuals.add(classAssertion);
							}
						}
					}
					else {
						throw new RuntimeException("Unknow concept: " + uri);
					}
				}
				else {
					if (dataProperties.contains(uri)) {
						while (res.next()) {
							String valueVar = atom.getTerms().get(1).getName();
							FunctionalTermImpl ft = (FunctionalTermImpl) atom.getTerms().get(0);
							StringBuffer sb = new StringBuffer();
							sb.append(ft.getName());
							sb.append("-");
							List<Term> parameters = ft.getTerms();
							Iterator<Term> pit = parameters.iterator();
							StringBuffer aux = new StringBuffer();
							while (pit.hasNext()) {
								if (aux.length() > 0) {
									aux.append("-");
								}
								Term qt = pit.next();
								String s = res.getString(qt.getName());
								aux.append(s);
							}
							sb.append(aux.toString());
//							System.out.println(sb.toString());
							OWLIndividual ind = factory.getOWLIndividual(sb.toString(),nm);
							OWLDataProperty prop = factory.getOWLDataProperty(new URI(uri));
							String value = res.getString(valueVar);
							OWLDataPropertyAssertionAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(ind, prop, value);
//							manager.addAxiom(currentOntology, axiom);
							individuals.add(axiom);
						}
					}
					else if (objectProperties.contains(uri)) {
						while (res.next()) {
							FunctionalTermImpl ft1 = (FunctionalTermImpl) atom.getTerms().get(0);
							StringBuffer sb1 = new StringBuffer();
							sb1.append(ft1.getName());
							sb1.append("-");
							List<Term> parameters = ft1.getTerms();
							Iterator<Term> pit = parameters.iterator();
							StringBuffer aux = new StringBuffer();
							while (pit.hasNext()) {
								if (aux.length() > 0) {
									aux.append("-");
								}
								Term qt = pit.next();
								String s = res.getString(qt.getName());
								aux.append(s);
							}
							sb1.append(aux.toString());
//							System.out.println(sb1.toString());
							FunctionalTermImpl ft2 = (FunctionalTermImpl) atom.getTerms().get(1);
							StringBuffer sb2 = new StringBuffer();
							sb2.append(ft2.getName());
							sb2.append("-");
							List<Term> parameters2 = ft2.getTerms();
							Iterator<Term> pit2 = parameters2.iterator();
							StringBuffer aux2 = new StringBuffer();
							while (pit2.hasNext()) {
								if (aux2.length() > 0) {
									aux2.append("-");
								}
								Term qt = pit2.next();
								String s = res.getString(qt.getName());
								aux2.append(s);
							}
							sb2.append(aux2.toString());
//							System.out.println(sb2.toString());
							OWLIndividual ind1 = factory.getOWLIndividual(sb1.toString(),nm);
							OWLIndividual ind2 = factory.getOWLIndividual(sb2.toString(),nm);
							OWLObjectProperty prop = factory.getOWLObjectProperty(new URI(uri));
							OWLObjectPropertyAssertionAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(ind1, prop, ind2);
//							manager.addAxiom(currentOntology, axiom);
							individuals.add(axiom);
						}
					}else{
						throw new RuntimeException("Unknow concept");
					}
				}
				res.close();
			}
		}
//		manager.addAxioms(new_onto, individuals);
		return individuals;
	}

	private ArrayList<OBDAMappingAxiom> getMappings(){

		ArrayList<OBDAMappingAxiom> mappings = null;
		MappingController mapcon = controller.getMappingController();
		DatasourcesController dscon = controller.getDatasourcesController();
		DataSource ds = dscon.getCurrentDataSource();
		if(ds == null){
			HashMap<URI, DataSource> map = dscon.getAllSources();
			Set<URI> keys = map.keySet();
			if(keys.iterator().hasNext()){
				URI s = keys.iterator().next();
				ds = map.get(s);
				dscon.setCurrentDataSource(ds.getSourceID());
			}
		}
		URI currentDS_uri = ds.getSourceID();
		mappings = mapcon.getMappings(currentDS_uri);
		return mappings;
	}

	public void saveOntology(String path) throws UnknownOWLOntologyException, OWLOntologyStorageException{
		File f = new File(path);
		manager.saveOntology(currentOntology, f.toURI());
	}

	public OWLOntologyManager getManager(){
		return manager;
	}

	public OWLOntology getCurrentOntology(){
		return currentOntology;
	}

	public static void main(String[] args) throws Exception{

		if(args.length == 2){
			String owlfile = args[0];
			String path = args[1];
			AboxMaterializer t = new AboxMaterializer(owlfile);
			Set<OWLIndividualAxiom> axioms = t.materializeAbox(owlfile,t.getCurrentOntology(), t.getManager());
			t.getManager().addAxioms(t.getCurrentOntology(), axioms);
			t.saveOntology(path);
		}else{
			throw new Exception("The progamm requires an owlfile and the path to the file where the new ontology should be saved, as Arguments");
		}
	}
}
