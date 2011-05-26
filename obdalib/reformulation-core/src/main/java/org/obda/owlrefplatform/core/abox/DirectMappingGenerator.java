package org.obda.owlrefplatform.core.abox;

import inf.unibz.it.obda.api.controller.OBDADataFactory;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.domain.Query;
import inf.unibz.it.obda.model.impl.AtomImpl;
import inf.unibz.it.obda.model.impl.CQIEImpl;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DirectMappingGenerator {


	private final OBDADataFactory		predicateFactory		= OBDADataFactoryImpl.getInstance();
	private final OBDADataFactory				termFactory				= OBDADataFactoryImpl.getInstance();
	private int									mappingcounter			= 1;
	
	private final Logger								log						= LoggerFactory.getLogger(ABoxToDBDumper.class);
	
	public Set<OBDAMappingAxiom> getMappings(DataSource datasource) throws Exception{
		throw new Exception("Not yet implemented");
	}
	
	public Set<OBDAMappingAxiom> getMappings(Set<OWLOntology> ontologies, Map<URIIdentyfier,String> tableMap){
		
		Iterator<OWLOntology> ontologyIterator = ontologies.iterator();
		Set<OBDAMappingAxiom> mappings = new HashSet<OBDAMappingAxiom>();

		while (ontologyIterator.hasNext()) {

			/*
			 * For each ontology
			 */
			OWLOntology onto = ontologyIterator.next();
			log.debug("Materializing ABox for ontology: {}", onto.getURI().toString());

			Set<OWLEntity> entities = onto.getSignature();
			Iterator<OWLEntity> entityIterator = entities.iterator();
			
			while (entityIterator.hasNext()) {
				/* For each entity */
				OWLEntity entity = entityIterator.next();

				if (entity instanceof OWLClass) {
					OWLClass clazz = (OWLClass) entity;
					if (!clazz.isOWLThing()) {//if class equal to owl thing just skip it
						/* Creating the mapping */
						URI name = clazz.getURI();
						URIIdentyfier id = new URIIdentyfier(name, URIType.CONCEPT);
						String tablename = tableMap.get(id);
						Term qt = termFactory.createVariable("x");
						List<Term> terms = new Vector<Term>();
						terms.add(qt);
						Predicate predicate = predicateFactory.createPredicate(name, terms.size());
						Atom bodyAtom = new AtomImpl(predicate, terms);
						List<Atom> body = new Vector<Atom>();
						body.add(bodyAtom); // the body
						predicate = predicateFactory.createPredicate(URI.create("q"), terms.size());
						Atom head = new AtomImpl(predicate, terms); // the head
						Query cq = new CQIEImpl(head, body, false);
						String sql = "SELECT term0 as x FROM " + tablename;
						OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id" + mappingcounter++);
						ax.setTargetQuery(cq);
						ax.setSourceQuery(new RDBMSSQLQuery(sql));
						mappings.add(ax);

						log.debug("Mapping created: {}", ax.toString());
					}
				} else if (entity instanceof OWLObjectProperty) {
					OWLObjectProperty objprop = (OWLObjectProperty) entity;
					URIIdentyfier id = new URIIdentyfier(objprop.getURI(), URIType.OBJECTPROPERTY);
					String tablename = tableMap.get(id);
					Term qt1 = termFactory.createVariable("x");
					Term qt2 = termFactory.createVariable("y");
					List<Term> terms = new Vector<Term>();
					terms.add(qt1);
					terms.add(qt2);
					Predicate predicate = predicateFactory.createPredicate(objprop.getURI(), terms.size());
					Atom bodyAtom = new AtomImpl(predicate, terms);
					List<Atom> body = new Vector<Atom>();
					body.add(bodyAtom); // the body
					predicate = predicateFactory.createPredicate(URI.create("q"), terms.size());
					Atom head = new AtomImpl(predicate, terms); // the head
					Query cq = new CQIEImpl(head, body, false);
					String sql = "SELECT term0 as x, term1 as y FROM " + tablename;
					OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id" + mappingcounter++);
					ax.setTargetQuery(cq);
					ax.setSourceQuery(new RDBMSSQLQuery(sql));
					log.debug("Mapping created: {}", ax.toString());
					mappings.add(ax);

				} else if (entity instanceof OWLDataProperty) {

					OWLDataProperty dataProp = (OWLDataProperty) entity;
					URIIdentyfier id = new URIIdentyfier(dataProp.getURI(), URIType.DATAPROPERTY);					
					Term qt1 = termFactory.createVariable("x");
					String tablename = tableMap.get(id);
					Term qt2 = termFactory.createVariable("y");
					List<Term> terms = new Vector<Term>();
					terms.add(qt1);
					terms.add(qt2);
					Predicate predicate = predicateFactory.createPredicate(dataProp.getURI(), terms.size());
					Atom bodyAtom = new AtomImpl(predicate, terms);
					List<Atom> body = new Vector<Atom>();
					body.add(bodyAtom); // the body
					predicate = predicateFactory.createPredicate(URI.create("q"), terms.size());
					Atom head = new AtomImpl(predicate, terms); // the head
					Query cq = new CQIEImpl(head, body, false);
					String sql = "SELECT term0 as x, term1 as y FROM " + tablename;
					OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id" + mappingcounter++);
					ax.setTargetQuery(cq);
					ax.setSourceQuery(new RDBMSSQLQuery(sql));
					mappings.add(ax);
				}
			}
		}
		return mappings;
	}
}
