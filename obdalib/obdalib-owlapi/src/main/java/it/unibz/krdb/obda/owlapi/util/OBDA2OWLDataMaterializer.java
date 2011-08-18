package it.unibz.krdb.obda.owlapi.util;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.CQIEImpl;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.semanticweb.owl.model.OWLOntologyManager;

/***
 * This utility allows you to materialize the data comming from the mappings and
 * datasources specified in an OBDA model into owl individuals that are inserted
 * into an OWLOntology.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OBDA2OWLDataMaterializer {

	private boolean isCanceled = false;
	
	public void materializeAbox(OBDAModel controller, OWLOntologyManager manager, OWLOntology currentOntology) throws Exception {
		List<DataSource> sources = controller.getAllSources();
		Iterator<DataSource> sit = sources.iterator();
		while (sit.hasNext()) {
			materializeAbox(controller, manager, currentOntology, sit.next());
		}
	}

	public void materializeAbox(OBDAModel controller, OWLOntologyManager manager, OWLOntology currentOntology, DataSource targetSource)
			throws Exception {

		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLIndividualAxiom> individuals = new HashSet<OWLIndividualAxiom>();
		JDBCConnectionManager jdbcMan = JDBCConnectionManager.getJDBCConnectionManager();
		List<OBDAMappingAxiom> maps = controller.getMappingController().getMappings(targetSource.getSourceID());
		// DataSource ds =
		// controller.getCurrentDataSource();
		Iterator<OBDAMappingAxiom> it = maps.iterator();
		HashSet<String> classesURIs = new HashSet<String>();
		HashSet<String> dataProperties = new HashSet<String>();
		HashSet<String> objectProperties = new HashSet<String>();

		Set<OWLClass> set = currentOntology.getClassesInSignature();
		// String ontoUri = currentOntology.getURI().toString();
		// NamespaceManager nm = new DefaultNamespaceManager(ontoUri);
		Iterator<OWLClass> sit = set.iterator();
		while (sit.hasNext()) {
			OWLClass c = sit.next();
			classesURIs.add(c.getURI().toString());
			// System.out.println(c.getURI().toString());
		}
		for (OWLDataProperty c : currentOntology.getDataPropertiesInSignature()) {
			dataProperties.add(c.getURI().toString());
		}
		for (OWLObjectProperty c : currentOntology.getObjectPropertiesInSignature()) {
			objectProperties.add(c.getURI().toString());
		}
		while (it.hasNext()) {
			OBDAMappingAxiom ax = it.next();
			String sql = ax.getSourceQuery().toString();
			CQIEImpl cq = (CQIEImpl) ax.getTargetQuery();
			List<Atom> atoms = cq.getBody();
			Iterator<Atom> a_it = atoms.iterator();
			while (a_it.hasNext()) {
				ResultSet res = jdbcMan.executeQuery(targetSource, sql);
				PredicateAtom atom = (PredicateAtom) a_it.next();
				// String name = atom.getPredicate().getName().getFragment();
				// String uri = ontoUri+"#"+name;
				String uri = atom.getPredicate().getName().toString();
				int arity = atom.getArity();
				if (arity == 1) { // Concept query atom
					if (classesURIs.contains(uri)) {
						List<Term> terms = atom.getTerms();
						while (res.next()) {
							Iterator<Term> teit = terms.iterator();
							while (teit.hasNext()) {
								FunctionalTermImpl ft = (FunctionalTermImpl) teit.next();
								String sb = getObjectURI(res, ft);
								// System.out.println(sb.toString());
								OWLIndividual ind = factory.getOWLIndividual(URI.create(sb.toString()));
								OWLClass clazz = factory.getOWLClass(new URI(uri));

								OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(ind, clazz);
								// manager.addAxiom(currentOntology,
								// classAssertion);
								individuals.add(classAssertion);
							}
						}
					} else {
						throw new RuntimeException("Unknow concept: " + uri);
					}
				} else {
					if (dataProperties.contains(uri)) {
						while (res.next()) {
							String valueVar = ((Variable)atom.getTerms().get(1)).getName();
							FunctionalTermImpl ft = (FunctionalTermImpl) atom.getTerms().get(0);
							String sb = getObjectURI(res, ft);

							
							OWLIndividual ind = factory.getOWLIndividual(URI.create(sb.toString()));
							OWLDataProperty prop = factory.getOWLDataProperty(new URI(uri));
							String value = res.getString(valueVar).trim();
							OWLDataPropertyAssertionAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(ind, prop,
									factory.getOWLUntypedConstant(value));
							// manager.addAxiom(currentOntology, axiom);
							individuals.add(axiom);
						}
					} else if (objectProperties.contains(uri)) {
						while (res.next()) {
							FunctionalTermImpl ft1 = (FunctionalTermImpl) atom.getTerms().get(0);
							String sb1 = getObjectURI(res, ft1);
							
							FunctionalTermImpl ft2 = (FunctionalTermImpl) atom.getTerms().get(1);
							String sb2 = getObjectURI(res, ft2);
							
							OWLIndividual ind1 = factory.getOWLIndividual(URI.create(sb1.toString()));
							OWLIndividual ind2 = factory.getOWLIndividual(URI.create(sb2.toString()));
							OWLObjectProperty prop = factory.getOWLObjectProperty(new URI(uri));
							OWLObjectPropertyAssertionAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(ind1, prop, ind2);
							// manager.addAxiom(currentOntology, axiom);
							individuals.add(axiom);
						}
					} else {
						throw new RuntimeException("Unknow concept");
					}
				}
				res.close();
			}
		}
		if(!isCanceled){
			manager.addAxioms(currentOntology, individuals);
		}

	}

	public void cancelAction(){
		isCanceled = true;
	}
	
	public boolean isCanceled(){
		return isCanceled;
	}
	
	/**
	 * Transforms a functional term into an Object URI by grounding any variable
	 * that appears in the term with the value of the column in the current row
	 * of the result set.
	 * 
	 * @param res
	 * @param functionalTerm
	 * @return
	 * @throws SQLException
	 */
	private String getObjectURI(ResultSet res, FunctionalTermImpl functionalTerm) throws SQLException {
		StringBuffer objectURIString = new StringBuffer();
		objectURIString.append(functionalTerm.getFunctionSymbol().toString());
		objectURIString.append("-");

		StringBuffer aux = new StringBuffer();
		for (Term qt : functionalTerm.getTerms()) {
			if (aux.length() > 0) {
				aux.append("-");
			}

			if (qt instanceof Variable) {
				String s = res.getString(((Variable) qt).getName()).trim();
				aux.append(s);
			} else if (qt instanceof ValueConstant) {

				aux.append(((ValueConstant) qt).getValue().trim());
			} else {
				throw new RuntimeException("Invalid term type in function symbol");
			}

		}
		objectURIString.append(aux.toString());
		return objectURIString.toString();
	}

}
