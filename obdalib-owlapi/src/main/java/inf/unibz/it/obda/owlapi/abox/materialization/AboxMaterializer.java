package inf.unibz.it.obda.owlapi.abox.materialization;

import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.datasource.JDBCConnectionManager;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.owlapi.OWLAPIController;
import inf.unibz.it.ucq.domain.BinaryQueryAtom;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;

import java.io.File;
import java.net.URI;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
		while(it.hasNext()){
			OBDAMappingAxiom ax = it.next();
			String sql = ax.getSourceQuery().toString();
			ConjunctiveQuery cq = (ConjunctiveQuery) ax.getTargetQuery();
			List<QueryAtom> atoms = cq.getAtoms();
			Iterator<QueryAtom> a_it = atoms.iterator();
			while(a_it.hasNext()){
				ResultSet res = jdbcMan.executeQuery(ds, sql);
				QueryAtom atom = a_it.next();
				String name = atom.getName();			
				String uri = ontoUri+"#"+name;
				if(atom instanceof ConceptQueryAtom){
					if(classesURIs.contains(uri)){
						ConceptQueryAtom cqa = (ConceptQueryAtom) atom;
						List<QueryTerm> terms = cqa.getTerms();
						while(res.next()){
							Iterator<QueryTerm> teit = terms.iterator();
							while(teit.hasNext()){
								FunctionTerm ft = (FunctionTerm) teit.next();
								StringBuffer sb = new StringBuffer();
								sb.append(ft.getName());
								sb.append("-");
								List<QueryTerm> parameters = ft.getParameters();
								Iterator<QueryTerm> pit = parameters.iterator();
								StringBuffer aux = new StringBuffer();
								while(pit.hasNext()){
									if(aux.length()>0){
										aux.append("-");
									}
									QueryTerm qt = pit.next();
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
					}else{
						throw new RuntimeException("Unknow concept: " + uri);
					}
				}else{
					BinaryQueryAtom bqa = (BinaryQueryAtom) atom;
					if(dataProperties.contains(uri)){
						while(res.next()){
							String valueVar = bqa.getTerms().get(1).getName();
							FunctionTerm ft = (FunctionTerm) bqa.getTerms().get(0);
							StringBuffer sb = new StringBuffer();
							sb.append(ft.getName());
							sb.append("-");
							List<QueryTerm> parameters = ft.getParameters();
							Iterator<QueryTerm> pit = parameters.iterator();
							StringBuffer aux = new StringBuffer();
							while(pit.hasNext()){
								if(aux.length()>0){
									aux.append("-");
								}
								QueryTerm qt = pit.next();
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
					}else if(objectProperties.contains(uri)){
						while(res.next()){
							FunctionTerm ft1 = (FunctionTerm) bqa.getTerms().get(0);
							StringBuffer sb1 = new StringBuffer();
							sb1.append(ft1.getName());
							sb1.append("-");
							List<QueryTerm> parameters = ft1.getParameters();
							Iterator<QueryTerm> pit = parameters.iterator();
							StringBuffer aux = new StringBuffer();
							while(pit.hasNext()){
								if(aux.length()>0){
									aux.append("-");
								}
								QueryTerm qt = pit.next();
								String s = res.getString(qt.getName());
								aux.append(s);
							}
							sb1.append(aux.toString());
//							System.out.println(sb1.toString());
							FunctionTerm ft2 = (FunctionTerm) bqa.getTerms().get(1);
							StringBuffer sb2 = new StringBuffer();
							sb2.append(ft2.getName());
							sb2.append("-");
							List<QueryTerm> parameters2 = ft2.getParameters();
							Iterator<QueryTerm> pit2 = parameters2.iterator();
							StringBuffer aux2 = new StringBuffer();
							while(pit2.hasNext()){
								if(aux2.length()>0){
									aux2.append("-");
								}
								QueryTerm qt = pit2.next();
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
			HashMap<String, DataSource> map = dscon.getAllSources();
			Set<String> keys = map.keySet();
			if(keys.iterator().hasNext()){
				String s = keys.iterator().next();
				ds = map.get(s);
				dscon.setCurrentDataSource(s);
			}
		}
		String currentDS_uri = ds.getName();
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
