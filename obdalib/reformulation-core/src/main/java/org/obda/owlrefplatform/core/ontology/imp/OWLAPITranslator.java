package org.obda.owlrefplatform.core.ontology.imp;

import inf.unibz.it.obda.api.controller.OBDADataFactory;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.obda.owlrefplatform.core.ontology.ConceptDescription;
import org.obda.owlrefplatform.core.ontology.DLLiterOntology;
import org.obda.owlrefplatform.core.ontology.DescriptionFactory;
import org.obda.owlrefplatform.core.ontology.RoleDescription;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAxiom;
import org.semanticweb.owl.model.OWLDataPropertyCharacteristicAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLNaryClassAxiom;
import org.semanticweb.owl.model.OWLNaryPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLUnaryPropertyAxiom;

public class OWLAPITranslator {


	private OBDADataFactory predicateFactory = null;
	private DescriptionFactory descFactory = null;
	private HashSet<String> objectproperties = null;
	private HashSet<String> dataproperties = null;

	public OWLAPITranslator (){
		predicateFactory = OBDADataFactoryImpl.getInstance();
		descFactory = new BasicDescriptionFactory();
		objectproperties = new HashSet<String>();
		dataproperties = new HashSet<String>();
	}

	public DLLiterOntology translate(OWLOntology owl) throws Exception{

		DLLiterOntology dl_onto = new DLLiterOntologyImpl(owl.getURI());

		Set<OWLEntity> entities = owl.getSignature();
		Iterator<OWLEntity> eit = entities.iterator();
		while(eit.hasNext()){
			OWLEntity entity = eit.next();
			if(entity instanceof OWLClass){

				URI uri = entity.getURI();
				Predicate p = predicateFactory.createPredicate(uri, 1);
				ConceptDescription cd =descFactory.getConceptDescription(p);
				dl_onto.addConcept(cd);

			}else if(entity instanceof OWLObjectProperty){

				URI uri = entity.getURI();
				objectproperties.add(uri.toString());
				Predicate p = predicateFactory.createPredicate(uri, 2);
				RoleDescription rd = descFactory.getRoleDescription(p);
				if(dataproperties.contains(uri.toString())){
					throw new Exception("Please avoid using the same name for object and data properties.");
				}else{
					dl_onto.addRole(rd);
				}

			}else if(entity instanceof OWLDataProperty){
				URI uri = entity.getURI();
				dataproperties.add(uri.toString());
				Predicate p = predicateFactory.createPredicate(uri, 2);
				RoleDescription rd = descFactory.getRoleDescription(p);
				if(objectproperties.contains(uri.toString())){
					throw new Exception("Please avoid using the same name for object and data properties.");
				}else{
					dl_onto.addRole(rd);
				}
			}
		}

		Set<OWLAxiom> axioms = owl.getAxioms();
		Iterator<OWLAxiom> it = axioms.iterator();
		while(it.hasNext()){
			OWLAxiom axiom = it.next();
			if(axiom instanceof OWLClassAxiom){

				if(axiom instanceof OWLNaryClassAxiom){

					if(axiom instanceof OWLDisjointClassesAxiom){

						OWLDisjointClassesAxiom aux= (OWLDisjointClassesAxiom) axiom;
						Set<OWLDescription> desc = aux.getDescriptions();
						if(checkValidityofOWLDescritpion(desc)){
							Iterator<OWLDescription> dit = desc.iterator();
							Vector<ConceptDescription> vec = new Vector<ConceptDescription>();
							while(dit.hasNext()){
								OWLDescription d = dit.next();
								vec.add(translateIntoConceptDescritpion(d));
							}
//							for(int i=0;i<vec.size();i++){
//								ConceptDescription d1 = vec.get(i);
//								for(int j=1+i;j<vec.size();j++){
//									ConceptDescription d2 = vec.get(j);
//
//								}
//
//							}
							DLLiterDisjointConceptAssertion dis = new DLLiterDisjointConceptAssertion(vec);
							dl_onto.addAssertion(dis);
						}
					}else if(axiom instanceof OWLEquivalentClassesAxiom){

						OWLEquivalentClassesAxiom aux = (OWLEquivalentClassesAxiom) axiom;
						Set<OWLDescription> desc = aux.getDescriptions();
						if(checkValidityofOWLDescritpion(desc)){
							Iterator<OWLDescription> dit = desc.iterator();
							Vector<ConceptDescription> vec = new Vector<ConceptDescription>();
							while(dit.hasNext()){
								OWLDescription d = dit.next();
								vec.add(translateIntoConceptDescritpion(d));
							}
							for(int i=0;i<vec.size();i++){
								ConceptDescription c1 = vec.get(i);
								for(int j=0;j<vec.size();j++){
									if(i!=j){
										ConceptDescription c2 = vec.get(j);
										DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(c1, c2);
										dl_onto.addAssertion(inc);
									}
								}
							}
						}
					}else if(axiom instanceof OWLSubClassAxiom){

						OWLSubClassAxiom aux = (OWLSubClassAxiom) axiom;
						OWLDescription subclass = aux.getSubClass();
						OWLDescription superclass = aux.getSuperClass();


						if(subclass instanceof OWLObjectIntersectionOf){
//							System.out.println(axiom.getClass() + "is ignored during translation.");
						}else if(subclass instanceof OWLObjectUnionOf){
							OWLObjectUnionOf union = (OWLObjectUnionOf) axiom;
							Set<OWLDescription> operants = union.getOperands();
							Iterator<OWLDescription> op_it = operants.iterator();
							while(op_it.hasNext()){
								OWLDescription desc = op_it.next();
								ConceptDescription cd_subclass = translateIntoConceptDescritpion(subclass);
								ConceptDescription cd_superclass = translateIntoConceptDescritpion(desc);

								if(cd_subclass != null && cd_superclass != null){
									DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(cd_subclass, cd_superclass);
									dl_onto.addAssertion(inc);
								}
							}
						}else if(subclass instanceof OWLObjectComplementOf){
							System.out.println(axiom.getClass() + "is ignored during translation.");
						}else if(superclass instanceof OWLObjectIntersectionOf){
							OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) superclass;
							Set<OWLDescription> operants = inter.getOperands();
							Iterator<OWLDescription> op_it = operants.iterator();
							while(op_it.hasNext()){
								OWLDescription desc = op_it.next();
								ConceptDescription cd_subclass = translateIntoConceptDescritpion(subclass);
								ConceptDescription cd_superclass = translateIntoConceptDescritpion(desc);

								if(cd_subclass != null && cd_superclass != null){
									DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(cd_subclass, cd_superclass);
									dl_onto.addAssertion(inc);
								}
							}
						}else if(superclass instanceof OWLObjectUnionOf){
//							System.out.println(axiom.getClass() + "is ignored during translation.");
						}else if(superclass instanceof OWLObjectComplementOf){

							OWLObjectComplementOf comp = (OWLObjectComplementOf)superclass;
							OWLDescription operant = comp.getOperand();
							ConceptDescription class1 = translateIntoConceptDescritpion(subclass);
							ConceptDescription class2 = translateIntoConceptDescritpion(operant);

							if(class1 != null && class2 != null){
								Vector<ConceptDescription> vec = new Vector<ConceptDescription>();
								vec.add(class1);
								vec.add(class2);
								DLLiterDisjointConceptAssertion dis = new DLLiterDisjointConceptAssertion(vec);
								dl_onto.addAssertion(dis);
							}

						}else{

							ConceptDescription cd_subclass = translateIntoConceptDescritpion(subclass);
							ConceptDescription cd_superclass = translateIntoConceptDescritpion(superclass);

							if(cd_subclass != null && cd_superclass != null){
								DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(cd_subclass, cd_superclass);
								dl_onto.addAssertion(inc);
							}
						}

					}else {
//						System.out.println(axiom.getClass() + "is ignored during translation.");
						//TODO use logger instead of println
					}

				}else {
//					System.out.println(axiom.getClass() + "is ignored during translation.");
					//TODO use logger instead of println
				}

			}else if(axiom instanceof OWLDataPropertyAxiom){

				if(axiom instanceof OWLFunctionalDataPropertyAxiom){

					OWLFunctionalDataPropertyAxiom aux = (OWLFunctionalDataPropertyAxiom) axiom;
					URI uri = aux.getProperty().asOWLDataProperty().getURI();
					Predicate p = predicateFactory.createPredicate(uri, 2);
					RoleDescription role = descFactory.getRoleDescription(p);
					DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
					dl_onto.addAssertion(func);

				}else if(axiom instanceof OWLDataPropertyDomainAxiom){

					OWLDataPropertyDomainAxiom aux = (OWLDataPropertyDomainAxiom) axiom;
					URI dom = aux.getDomain().asOWLClass().getURI();
					URI prop = aux.getProperty().asOWLDataProperty().getURI();
					Predicate p = predicateFactory.createPredicate(prop, 2);
					Predicate d = predicateFactory.createPredicate(dom,1);
					ConceptDescription exist = descFactory.getConceptDescription(p, false, false);
					ConceptDescription concept = descFactory.getConceptDescription(d,false,false);
					DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(exist, concept);
					dl_onto.addAssertion(inc);

				}else if(axiom instanceof OWLDataSubPropertyAxiom){

					OWLDataSubPropertyAxiom aux = (OWLDataSubPropertyAxiom) axiom;
					URI sub_uri = aux.getSubProperty().asOWLDataProperty().getURI();
					URI super_uri = aux.getSuperProperty().asOWLDataProperty().getURI();
					Predicate sub = predicateFactory.createPredicate(sub_uri, 2);
					Predicate superR = predicateFactory.createPredicate(super_uri, 2);
					RoleDescription subrole = descFactory.getRoleDescription(sub);
					RoleDescription superrole = descFactory.getRoleDescription(superR);
					DLLiterRoleInclusionImpl roleinc = new DLLiterRoleInclusionImpl(subrole, superrole);
					dl_onto.addAssertion(roleinc);

				}else if(axiom instanceof OWLDisjointDataPropertiesAxiom){

					OWLDisjointDataPropertiesAxiom aux = (OWLDisjointDataPropertiesAxiom) axiom;
					Set<OWLDataPropertyExpression> properties = aux.getProperties();
					Iterator<OWLDataPropertyExpression> pit = properties.iterator();
					Vector<RoleDescription> vec = new Vector<RoleDescription>();
					while(pit.hasNext()){
						URI uri = pit.next().asOWLDataProperty().getURI();
						Predicate prop = predicateFactory.createPredicate(uri, 2);
						RoleDescription role = descFactory.getRoleDescription(prop);
						vec.add(role);
					}
					DLLiterRoleDisjointAssertion dis = new DLLiterRoleDisjointAssertion(vec);
					dl_onto.addAssertion(dis);
				}else if(axiom instanceof OWLEquivalentDataPropertiesAxiom){

					OWLEquivalentDataPropertiesAxiom aux = (OWLEquivalentDataPropertiesAxiom) axiom;
					Set<OWLDataPropertyExpression> properties = aux.getProperties();
					Iterator<OWLDataPropertyExpression> pit = properties.iterator();
					Vector<RoleDescription> vec = new Vector<RoleDescription>();
					while(pit.hasNext()){
						URI uri = pit.next().asOWLDataProperty().getURI();
						Predicate prop = predicateFactory.createPredicate(uri, 2);
						RoleDescription role = descFactory.getRoleDescription(prop);
						vec.add(role);
					}
					DLLiterRoleEquivalenceAssertion dis = new DLLiterRoleEquivalenceAssertion(vec);
					dl_onto.addAssertion(dis);

				}else {
//					System.out.println(axiom.getClass() + "is ignored during translation.");
					//TODO use logger instead of println
				}

			}else if(axiom instanceof OWLPropertyAxiom){

				if(axiom instanceof OWLNaryPropertyAxiom){

					if(axiom instanceof OWLDisjointDataPropertiesAxiom){

						OWLDisjointDataPropertiesAxiom aux = (OWLDisjointDataPropertiesAxiom) axiom;
						Set<OWLDataPropertyExpression> properties = aux.getProperties();
						Iterator<OWLDataPropertyExpression> pit = properties.iterator();
						Vector<RoleDescription> vec = new Vector<RoleDescription>();
						while(pit.hasNext()){
							URI uri = pit.next().asOWLDataProperty().getURI();
							Predicate prop = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(prop);
							vec.add(role);
						}
						DLLiterRoleDisjointAssertion dis = new DLLiterRoleDisjointAssertion(vec);
						dl_onto.addAssertion(dis);

					}else if(axiom instanceof OWLDisjointObjectPropertiesAxiom){

						OWLDisjointObjectPropertiesAxiom aux = (OWLDisjointObjectPropertiesAxiom) axiom;
						Set<OWLObjectPropertyExpression> properties = aux.getProperties();
						Iterator<OWLObjectPropertyExpression> pit = properties.iterator();
						Vector<RoleDescription> vec = new Vector<RoleDescription>();
						while(pit.hasNext()){
							OWLObjectPropertyExpression exp = pit.next();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri = inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = pit.next().asOWLObjectProperty().getURI();
							}
							Predicate prop = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(prop, isInverse, false);
							vec.add(role);
						}
						DLLiterRoleDisjointAssertion dis = new DLLiterRoleDisjointAssertion(vec);
						dl_onto.addAssertion(dis);

					}else if(axiom instanceof OWLEquivalentDataPropertiesAxiom){

						OWLEquivalentDataPropertiesAxiom aux = (OWLEquivalentDataPropertiesAxiom) axiom;
						Set<OWLDataPropertyExpression> properties = aux.getProperties();
						Iterator<OWLDataPropertyExpression> pit = properties.iterator();
						Vector<RoleDescription> vec = new Vector<RoleDescription>();
						while(pit.hasNext()){
							URI uri = pit.next().asOWLDataProperty().getURI();
							Predicate prop = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(prop);
							vec.add(role);
						}
						DLLiterRoleEquivalenceAssertion dis = new DLLiterRoleEquivalenceAssertion(vec);
						dl_onto.addAssertion(dis);

					}else if(axiom instanceof OWLEquivalentObjectPropertiesAxiom){

						OWLEquivalentObjectPropertiesAxiom aux = (OWLEquivalentObjectPropertiesAxiom) axiom;
						Set<OWLObjectPropertyExpression> properties = aux.getProperties();
						Iterator<OWLObjectPropertyExpression> pit = properties.iterator();
						Vector<RoleDescription> vec = new Vector<RoleDescription>();
						while(pit.hasNext()){
							OWLObjectPropertyExpression exp = pit.next();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri = inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = pit.next().asOWLObjectProperty().getURI();
							}
							Predicate prop = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(prop, isInverse, false);
							vec.add(role);
						}
						DLLiterRoleEquivalenceAssertion dis = new DLLiterRoleEquivalenceAssertion(vec);
						dl_onto.addAssertion(dis);

					}else if(axiom instanceof OWLInverseObjectPropertiesAxiom){

						OWLInverseObjectPropertiesAxiom aux = (OWLInverseObjectPropertiesAxiom) axiom;
						OWLObjectPropertyExpression exp1 = aux.getFirstProperty();
						URI uri1 = null;
						boolean isInverse1 = false;
						if(exp1.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
							uri1 = inv.asOWLObjectProperty().getURI();
							isInverse1 = true;
						}else{
							uri1 = exp1.asOWLObjectProperty().getURI();
						}
						OWLObjectPropertyExpression exp2 = aux.getSecondProperty();
						URI uri2 = null;
						boolean isInverse2 = false;
						if(exp2.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp2).getInverse();
							uri2 = inv.asOWLObjectProperty().getURI();
							isInverse2 = true;
						}else{
							uri2 = exp2.asOWLObjectProperty().getURI();
						}
						Predicate p1 = predicateFactory.createPredicate(uri1, 2);
						Predicate p2 = predicateFactory.createPredicate(uri2, 2);
						RoleDescription role1 = descFactory.getRoleDescription(p1, isInverse1, false);
						RoleDescription role2 = descFactory.getRoleDescription(p2,!isInverse2,false);
						RoleDescription role3 = descFactory.getRoleDescription(p2,isInverse2, false);
						RoleDescription role4 = descFactory.getRoleDescription(p1,!isInverse1,false);
						DLLiterRoleInclusionImpl inc1 = new DLLiterRoleInclusionImpl(role1, role2);
						DLLiterRoleInclusionImpl inc2 = new DLLiterRoleInclusionImpl(role3, role4);
						dl_onto.addAssertion(inc1);
						dl_onto.addAssertion(inc2);
					}
				}else if(axiom instanceof OWLObjectPropertyAxiom){

					if(axiom instanceof OWLDisjointObjectPropertiesAxiom){

						OWLDisjointObjectPropertiesAxiom aux = (OWLDisjointObjectPropertiesAxiom) axiom;
						Set<OWLObjectPropertyExpression> properties = aux.getProperties();
						Iterator<OWLObjectPropertyExpression> pit = properties.iterator();
						Vector<RoleDescription> vec = new Vector<RoleDescription>();
						while(pit.hasNext()){
							OWLObjectPropertyExpression exp = pit.next();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri = inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = pit.next().asOWLObjectProperty().getURI();
							}
							Predicate prop = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(prop, isInverse, false);
							vec.add(role);
						}
						DLLiterRoleDisjointAssertion dis = new DLLiterRoleDisjointAssertion(vec);
						dl_onto.addAssertion(dis);

					}else if(axiom instanceof OWLEquivalentObjectPropertiesAxiom){

						OWLEquivalentObjectPropertiesAxiom aux = (OWLEquivalentObjectPropertiesAxiom) axiom;
						Set<OWLObjectPropertyExpression> properties = aux.getProperties();
						Iterator<OWLObjectPropertyExpression> pit = properties.iterator();
						Vector<RoleDescription> vec = new Vector<RoleDescription>();
						while(pit.hasNext()){
							OWLObjectPropertyExpression exp = pit.next();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri = inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = pit.next().asOWLObjectProperty().getURI();
							}
							Predicate prop = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(prop, isInverse, false);
							vec.add(role);
						}
						DLLiterRoleEquivalenceAssertion dis = new DLLiterRoleEquivalenceAssertion(vec);
						dl_onto.addAssertion(dis);

					}else if(axiom instanceof OWLInverseObjectPropertiesAxiom){

						OWLInverseObjectPropertiesAxiom aux = (OWLInverseObjectPropertiesAxiom) axiom;
						OWLObjectPropertyExpression exp1 = aux.getFirstProperty();
						URI uri1 = null;
						boolean isInverse1 = false;
						if(exp1.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
							uri1 = inv.asOWLObjectProperty().getURI();
							isInverse1 = true;
						}else{
							uri1 = exp1.asOWLObjectProperty().getURI();
						}
						OWLObjectPropertyExpression exp2 = aux.getSecondProperty();
						URI uri2 = null;
						boolean isInverse2 = false;
						if(exp2.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp2).getInverse();
							uri2 = inv.asOWLObjectProperty().getURI();
							isInverse2 = true;
						}else{
							uri2 = exp2.asOWLObjectProperty().getURI();
						}
						Predicate p1 = predicateFactory.createPredicate(uri1, 2);
						Predicate p2 = predicateFactory.createPredicate(uri2, 2);
						RoleDescription role1 = descFactory.getRoleDescription(p1, isInverse1, false);
						RoleDescription role2 = descFactory.getRoleDescription(p2,isInverse2,true);
						RoleDescription role3 = descFactory.getRoleDescription(p2,isInverse2,false);
						RoleDescription role4 = descFactory.getRoleDescription(p1,isInverse1,true);
						DLLiterRoleInclusionImpl inc1 = new DLLiterRoleInclusionImpl(role1, role2);
						DLLiterRoleInclusionImpl inc2 = new DLLiterRoleInclusionImpl(role3, role4);
						dl_onto.addAssertion(inc1);
						dl_onto.addAssertion(inc2);

					}else if(axiom instanceof OWLObjectPropertyCharacteristicAxiom){

						if(axiom instanceof OWLFunctionalDataPropertyAxiom){

							OWLFunctionalDataPropertyAxiom aux = (OWLFunctionalDataPropertyAxiom) axiom;
							URI uri = aux.getProperty().asOWLDataProperty().getURI();
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p);
							DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
							dl_onto.addAssertion(func);

						}else if(axiom instanceof OWLFunctionalObjectPropertyAxiom){

							OWLFunctionalObjectPropertyAxiom aux = (OWLFunctionalObjectPropertyAxiom) axiom;
							OWLObjectPropertyExpression exp1 = aux.getProperty();
							URI uri = null;
							boolean isInverse = false;
							if(exp1.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
								uri= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = exp1.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p, isInverse, false);
							DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
							dl_onto.addAssertion(func);

						}else if(axiom instanceof OWLInverseFunctionalObjectPropertyAxiom){

							OWLInverseFunctionalObjectPropertyAxiom aux = (OWLInverseFunctionalObjectPropertyAxiom) axiom;
							OWLObjectPropertyExpression exp1 = aux.getProperty();
							URI uri = null;
							boolean isInverse = false;
							if(exp1.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
								uri= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = exp1.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p,isInverse,true);
							DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
							dl_onto.addAssertion(func);

						}else if(axiom instanceof OWLSymmetricObjectPropertyAxiom){

							OWLSymmetricObjectPropertyAxiom aux = (OWLSymmetricObjectPropertyAxiom) axiom;
							OWLObjectPropertyExpression exp1 = aux.getProperty();
							URI uri = null;
							boolean isInverse = false;
							if(exp1.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
								uri= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = exp1.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p, isInverse, false);
							DLLiterSymmetricRoleAssertion symm = new DLLiterSymmetricRoleAssertion(role);
							dl_onto.addAssertion(symm);
						}else {
//							System.out.println(axiom.getClass() + "is ignored during translation.");
							//TODO use logger instead of println
						}

					}else if(axiom instanceof OWLObjectPropertyDomainAxiom){

						OWLObjectPropertyDomainAxiom aux = (OWLObjectPropertyDomainAxiom) axiom;
						URI dom = null;
						boolean domIsInverse = false;
						int arity = 1;
						OWLDescription domain = aux.getDomain();
						if(domain.isAnonymous()){
							if(domain instanceof OWLRestriction){
								OWLRestriction rest = (OWLRestriction) domain;
								OWLObjectPropertyExpression exp1 = (OWLObjectPropertyExpression) rest.getProperty();
								if(exp1.isAnonymous()){
									OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
									dom= inv.asOWLObjectProperty().getURI();
									domIsInverse = true;
								}else{
									dom = exp1.asOWLObjectProperty().getURI();
								}
								arity = 2;
							}else{
								throw new Exception("Unexpected type of OWLDescription");
							}
						}else{
							dom = domain.asOWLClass().getURI();
						}
						OWLObjectPropertyExpression exp1 = aux.getProperty();
						URI prop = null;
						boolean isInverse = false;
						if(exp1.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
							prop= inv.asOWLObjectProperty().getURI();
							isInverse = true;
						}else{
							prop = exp1.asOWLObjectProperty().getURI();
						}
						Predicate p = predicateFactory.createPredicate(prop, 2);
						Predicate d = predicateFactory.createPredicate(dom,arity);
						ConceptDescription exist = descFactory.getConceptDescription(p, false, isInverse);
						ConceptDescription concept = descFactory.getConceptDescription(d,false,domIsInverse);
						DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(exist, concept);
						dl_onto.addAssertion(inc);

					}else if(axiom instanceof OWLObjectPropertyRangeAxiom){

						OWLObjectPropertyRangeAxiom aux = (OWLObjectPropertyRangeAxiom) axiom;
						URI rangeuri = null;
						boolean rangeIsInverse = false;
						int arity = 1;
						OWLDescription range = aux.getRange();
						if(range.isAnonymous()){
							if(range instanceof OWLRestriction){
								OWLRestriction rest = (OWLRestriction) range;
								OWLObjectPropertyExpression exp1 = (OWLObjectPropertyExpression) rest.getProperty();
								URI uri = null;
								if(exp1.isAnonymous()){
									OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
									rangeuri= inv.asOWLObjectProperty().getURI();
									rangeIsInverse = true;
								}else{
									rangeuri = exp1.asOWLObjectProperty().getURI();
								}
								arity = 2;
							}else{
								throw new Exception("Unexpected type of OWLDescription");
							}
						}else{
							rangeuri = range.asOWLClass().getURI();
						}
						OWLObjectPropertyExpression exp1 = aux.getProperty();
						URI u = null;
						boolean isInverse = false;
						if(exp1.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
							u= inv.asOWLObjectProperty().getURI();
							isInverse = true;
						}else{
							u = exp1.asOWLObjectProperty().getURI();
						}
						Predicate p = predicateFactory.createPredicate(u, 2);
						Predicate d = predicateFactory.createPredicate(rangeuri,arity);
						ConceptDescription exist = descFactory.getConceptDescription(p, false, !isInverse);
						ConceptDescription concept = descFactory.getConceptDescription(d,false,rangeIsInverse);
						DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(exist, concept);
						dl_onto.addAssertion(inc);

					}else if(axiom instanceof OWLObjectSubPropertyAxiom){

						OWLObjectSubPropertyAxiom aux = (OWLObjectSubPropertyAxiom) axiom;
						OWLObjectPropertyExpression subexp = aux.getSubProperty();
						boolean isSubInverse = false;
						URI sub_uri = null;
						if(subexp.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) subexp).getInverse();
							sub_uri= inv.asOWLObjectProperty().getURI();
							isSubInverse = true;
						}else{
							sub_uri = subexp.asOWLObjectProperty().getURI();
						}
						OWLObjectPropertyExpression superexp = aux.getSuperProperty();
						URI super_uri = null;
						boolean isSuperInverse = false;
						if(superexp.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) superexp).getInverse();
							super_uri= inv.asOWLObjectProperty().getURI();
							isSuperInverse = true;
						}else{
							super_uri = superexp.asOWLObjectProperty().getURI();
						}
						Predicate sub = predicateFactory.createPredicate(sub_uri, 2);
						Predicate superR = predicateFactory.createPredicate(super_uri, 2);
						RoleDescription subrole = descFactory.getRoleDescription(sub, isSubInverse, false);
						RoleDescription superrole = descFactory.getRoleDescription(superR, isSuperInverse, false);
						DLLiterRoleInclusionImpl roleinc = new DLLiterRoleInclusionImpl(subrole, superrole);
						dl_onto.addAssertion(roleinc);

					}else {
//						System.out.println(axiom.getClass() + "is ignored during translation.");
						//TODO use logger instead of println
					}
				}else if(axiom instanceof OWLSubPropertyAxiom){

					if(axiom instanceof OWLDataSubPropertyAxiom){

						OWLDataSubPropertyAxiom aux = (OWLDataSubPropertyAxiom) axiom;
						URI sub_uri = aux.getSubProperty().asOWLDataProperty().getURI();
						URI super_uri = aux.getSuperProperty().asOWLDataProperty().getURI();
						Predicate sub = predicateFactory.createPredicate(sub_uri, 2);
						Predicate superR = predicateFactory.createPredicate(super_uri, 2);
						RoleDescription subrole = descFactory.getRoleDescription(sub);
						RoleDescription superrole = descFactory.getRoleDescription(superR);
						DLLiterRoleInclusionImpl roleinc = new DLLiterRoleInclusionImpl(subrole, superrole);
						dl_onto.addAssertion(roleinc);

					}else if(axiom instanceof OWLObjectSubPropertyAxiom){

						OWLObjectSubPropertyAxiom aux = (OWLObjectSubPropertyAxiom) axiom;
						OWLObjectPropertyExpression subexp = aux.getSubProperty();
						boolean isSubInverse = false;
						URI sub_uri = null;
						if(subexp.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) subexp).getInverse();
							sub_uri= inv.asOWLObjectProperty().getURI();
							isSubInverse = true;
						}else{
							sub_uri = subexp.asOWLObjectProperty().getURI();
						}
						OWLObjectPropertyExpression superexp = aux.getSuperProperty();
						URI super_uri = null;
						boolean isSuperInverse = false;
						if(superexp.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) superexp).getInverse();
							super_uri= inv.asOWLObjectProperty().getURI();
							isSuperInverse = true;
						}else{
							super_uri = superexp.asOWLObjectProperty().getURI();
						}
						Predicate sub = predicateFactory.createPredicate(sub_uri, 2);
						Predicate superR = predicateFactory.createPredicate(super_uri, 2);
						RoleDescription subrole = descFactory.getRoleDescription(sub, isSubInverse, false);
						RoleDescription superrole = descFactory.getRoleDescription(superR, isSuperInverse, false);
						DLLiterRoleInclusionImpl roleinc = new DLLiterRoleInclusionImpl(subrole, superrole);
						dl_onto.addAssertion(roleinc);

					}
				}else if(axiom instanceof OWLUnaryPropertyAxiom){

					if(axiom instanceof OWLDataPropertyCharacteristicAxiom){

						if(axiom instanceof OWLFunctionalDataPropertyAxiom){

							OWLFunctionalDataPropertyAxiom aux = (OWLFunctionalDataPropertyAxiom) axiom;
							URI uri = aux.getProperty().asOWLDataProperty().getURI();
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p);
							DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
							dl_onto.addAssertion(func);

						}else if(axiom instanceof OWLFunctionalObjectPropertyAxiom){

							OWLFunctionalObjectPropertyAxiom aux = (OWLFunctionalObjectPropertyAxiom) axiom;
							OWLObjectPropertyExpression exp = aux.getProperty();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = exp.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p, isInverse, false);
							DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
							dl_onto.addAssertion(func);

						}else if(axiom instanceof OWLInverseFunctionalObjectPropertyAxiom){

							OWLInverseFunctionalObjectPropertyAxiom aux = (OWLInverseFunctionalObjectPropertyAxiom) axiom;
							OWLObjectPropertyExpression exp = aux.getProperty();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = exp.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p,isInverse,true);
							DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
							dl_onto.addAssertion(func);

						}else if(axiom instanceof OWLSymmetricObjectPropertyAxiom){

							OWLSymmetricObjectPropertyAxiom aux = (OWLSymmetricObjectPropertyAxiom) axiom;
							OWLObjectPropertyExpression exp = aux.getProperty();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = exp.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p, isInverse, false);
							DLLiterSymmetricRoleAssertion symm = new DLLiterSymmetricRoleAssertion(role);
							dl_onto.addAssertion(symm);
						}else {
//							System.out.println(axiom.getClass() + "is ignored during translation.");
							//TODO use logger instead of println
						}

					}else if(axiom instanceof OWLObjectPropertyCharacteristicAxiom){

						if(axiom instanceof OWLFunctionalDataPropertyAxiom){

							OWLFunctionalDataPropertyAxiom aux = (OWLFunctionalDataPropertyAxiom) axiom;
							URI uri = aux.getProperty().asOWLDataProperty().getURI();
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p);
							DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
							dl_onto.addAssertion(func);

						}else if(axiom instanceof OWLFunctionalObjectPropertyAxiom){

							OWLFunctionalObjectPropertyAxiom aux = (OWLFunctionalObjectPropertyAxiom) axiom;
							OWLObjectPropertyExpression exp = aux.getProperty();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = exp.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p, isInverse,false);
							DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
							dl_onto.addAssertion(func);

						}else if(axiom instanceof OWLInverseFunctionalObjectPropertyAxiom){

							OWLInverseFunctionalObjectPropertyAxiom aux = (OWLInverseFunctionalObjectPropertyAxiom) axiom;
							OWLObjectPropertyExpression exp = aux.getProperty();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = exp.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p,isInverse,true);
							DLLiterFunctionalAssertion func = new DLLiterFunctionalAssertion(role);
							dl_onto.addAssertion(func);

						}else if(axiom instanceof OWLSymmetricObjectPropertyAxiom){

							OWLSymmetricObjectPropertyAxiom aux = (OWLSymmetricObjectPropertyAxiom) axiom;
							OWLObjectPropertyExpression exp = aux.getProperty();
							URI uri = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								uri= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								uri = exp.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(uri, 2);
							RoleDescription role = descFactory.getRoleDescription(p,isInverse, false);
							DLLiterSymmetricRoleAssertion symm = new DLLiterSymmetricRoleAssertion(role);
							dl_onto.addAssertion(symm);

						}else {
//							System.out.println(axiom.getClass() + "is ignored during translation.");
							//TODO use logger instead of println
						}

					}else if(axiom instanceof OWLPropertyDomainAxiom){

						if(axiom instanceof OWLObjectPropertyDomainAxiom ){

							OWLObjectPropertyDomainAxiom aux = (OWLObjectPropertyDomainAxiom) axiom;
							URI dom = null;
							boolean isDomInverse = false;
							int arity = 1;
							OWLDescription domain = aux.getDomain();
							if(domain.isAnonymous()){
								if(domain instanceof OWLRestriction){
									OWLRestriction rest = (OWLRestriction) domain;
									OWLObjectPropertyExpression exp1 = (OWLObjectPropertyExpression) rest.getProperty();
									if(exp1.isAnonymous()){
										OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
										dom= inv.asOWLObjectProperty().getURI();
										isDomInverse = true;
									}else{
										dom = exp1.asOWLObjectProperty().getURI();
									}
									arity = 2;
								}else{
									throw new Exception("Unexpected type of OWLDescription");
								}
							}else{
								dom = domain.asOWLClass().getURI();
							}
							OWLObjectPropertyExpression exp = aux.getProperty();
							URI prop = null;
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								prop= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								prop = exp.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(prop, 2);
							Predicate d = predicateFactory.createPredicate(dom,arity);
							ConceptDescription exist = descFactory.getConceptDescription(p, false, isInverse);
							ConceptDescription concept = descFactory.getConceptDescription(d,false,isDomInverse);
							DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(exist, concept);
							dl_onto.addAssertion(inc);

						}else if(axiom instanceof OWLDataPropertyDomainAxiom ){

							OWLDataPropertyDomainAxiom aux = (OWLDataPropertyDomainAxiom) axiom;
							URI dom = null;
							int arity = 1;
							OWLDescription domain = aux.getDomain();
							if(domain.isAnonymous()){
								if(domain instanceof OWLRestriction){
									OWLRestriction rest = (OWLRestriction) domain;
									OWLObjectPropertyExpression exp1 = (OWLObjectPropertyExpression) rest.getProperty();
									if(exp1.isAnonymous()){
										OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
										dom= inv.asOWLObjectProperty().getURI();
									}else{
										dom = exp1.asOWLObjectProperty().getURI();
									}
									arity = 2;
								}else{
									throw new Exception("Unexpected type of OWLDescription");
								}
							}else{
								dom = domain.asOWLClass().getURI();
							}
							URI prop = aux.getProperty().asOWLDataProperty().getURI();
							Predicate p = predicateFactory.createPredicate(prop, 2);
							Predicate d = predicateFactory.createPredicate(dom,arity);
							ConceptDescription exist = descFactory.getConceptDescription(p, false, false);
							ConceptDescription concept = descFactory.getConceptDescription(d,false,false);
							DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(exist, concept);
							dl_onto.addAssertion(inc);

						}else {
//							System.out.println(axiom.getClass() + "is ignored during translation.");
							//TODO use logger instead of printl
						}

					}else if(axiom instanceof OWLPropertyRangeAxiom){

						if(axiom instanceof OWLObjectPropertyRangeAxiom ){

							OWLObjectPropertyRangeAxiom aux = (OWLObjectPropertyRangeAxiom) axiom;
							URI rangeuri = null;
							int arity = 1;
							OWLDescription range = aux.getRange();
							boolean rangeIsInverse = false;
							if(range.isAnonymous()){
								if(range instanceof OWLRestriction){
									OWLRestriction rest = (OWLRestriction) range;
									OWLObjectPropertyExpression exp1 = (OWLObjectPropertyExpression) rest.getProperty();
									if(exp1.isAnonymous()){
										OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp1).getInverse();
										rangeuri= inv.asOWLObjectProperty().getURI();
										rangeIsInverse = true;
									}else{
										rangeuri = exp1.asOWLObjectProperty().getURI();
									}
									arity = 2;
								}else{
									throw new Exception("Unexpected type of OWLDescription");
								}
							}else{
								rangeuri = range.asOWLClass().getURI();
							}
							URI prop = null;
							OWLObjectPropertyExpression exp = aux.getProperty();
							boolean isInverse = false;
							if(exp.isAnonymous()){
								OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
								prop= inv.asOWLObjectProperty().getURI();
								isInverse = true;
							}else{
								prop = exp.asOWLObjectProperty().getURI();
							}
							Predicate p = predicateFactory.createPredicate(prop, 2);
							Predicate d = predicateFactory.createPredicate(rangeuri,arity);
							ConceptDescription exist = descFactory.getConceptDescription(p, false, !isInverse);
							ConceptDescription concept = descFactory.getConceptDescription(d,false,rangeIsInverse);
							DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(exist, concept);
							dl_onto.addAssertion(inc);

						}else {
//							System.out.println(axiom.getClass() + "is ignored during translation.");
							//TODO use logger instead of printl
						}
					}else {
//						System.out.println(axiom.getClass() + "is ignored during translation.");
						//TODO use logger instead of println
					}
				}else {
//					System.out.println(axiom.getClass() + "is ignored during translation.");
					//TODO use logger instead of println
				}

			}else {
//				System.out.println(axiom.getClass() + "is ignored during translation.");
				//TODO use logger instead of println
			}
		}
		return dl_onto;
	}


	private boolean checkValidityofOWLDescritpion(Set<OWLDescription> set){

		boolean valid = true;
		Iterator<OWLDescription> it = set.iterator();
		while(it.hasNext() && valid){
			valid = checkValidityofOWLDescritpion(it.next());
		}
		return valid;
	}

	private boolean checkValidityofOWLDescritpion(OWLDescription desc){
		ConceptDescription d = translateIntoConceptDescritpion(desc);
		if(d == null){
			return false;
		}else{
			return true;
		}
	}

	private ConceptDescription translateIntoConceptDescritpion(OWLDescription d){

		if(d instanceof  OWLClass){
			URI uri = ((OWLClass)d).getURI();
			Predicate p = predicateFactory.createPredicate(uri, 1);
			ConceptDescription cd =descFactory.getConceptDescription(p);
			return cd;
		}else if(d instanceof OWLDataMinCardinalityRestriction){

			OWLDataMinCardinalityRestriction rest = (OWLDataMinCardinalityRestriction)d;
			int cardinatlity = rest.getCardinality();
			OWLDataRange range = rest.getFiller();
			if(cardinatlity != 1 || range != null){
				return null;
			}else{
				URI uri = rest.getProperty().asOWLDataProperty().getURI();
				Predicate p = predicateFactory.createPredicate(uri, 2);
				ConceptDescription cd =descFactory.getConceptDescription(p);
				return cd;
			}
		}else if(d instanceof OWLObjectMinCardinalityRestriction){
			OWLObjectMinCardinalityRestriction rest = (OWLObjectMinCardinalityRestriction)d;
			int cardinatlity = rest.getCardinality();
			OWLDescription filler =rest.getFiller();
			if(cardinatlity != 1){
				return null;
			}else{
				if(filler.isOWLThing()){
					OWLObjectPropertyExpression propExp = rest.getProperty();
					URI uri = propExp.getNamedProperty().getURI();
					Predicate p = predicateFactory.createPredicate(uri, 2);
					ConceptDescription cd;
					if(propExp instanceof OWLObjectPropertyInverse){
						cd = descFactory.getConceptDescription(p,false,true);
					}else{
						cd =descFactory.getConceptDescription(p);
					}
					return cd;
				}else{
					if(!(filler instanceof OWLObjectComplementOf)){
						URI prop = null;
						OWLObjectPropertyExpression exp = rest.getProperty();
						if(exp.isAnonymous()){
							OWLObjectPropertyExpression inv = ((OWLObjectPropertyInverse) exp).getInverse();
							prop= inv.asOWLObjectProperty().getURI();
						}else{
							prop = exp.asOWLObjectProperty().getURI();
						}
						Predicate p = predicateFactory.createPredicate(prop, 2);
						Vector<Predicate> vec = new Vector<Predicate>();
						vec.add(p);
						ConceptDescription cd =descFactory.getConceptDescription(vec);
						return cd;
					}else{
						return null;
					}
				}

			}
		}else if(d instanceof OWLObjectSomeRestriction){
			OWLObjectSomeRestriction rest = (OWLObjectSomeRestriction) d;
			OWLDescription filler =rest.getFiller();
			OWLObjectPropertyExpression pro = rest.getProperty();
			OWLObjectProperty owlpro = null;
			boolean isInverse = false;
			if(pro instanceof OWLObjectPropertyInverse){
				OWLObjectPropertyInverse inv = (OWLObjectPropertyInverse)pro;
				owlpro = inv.getNamedProperty();
				isInverse = true;
			}else{
				owlpro = pro.asOWLObjectProperty();
			}
			URI uri = owlpro.getURI();
			if(filler.isOWLThing()){
				Predicate p = predicateFactory.createPredicate(uri, 2);
				ConceptDescription cd =descFactory.getConceptDescription(p,false, isInverse);
				return cd;
			}else{
				if(!(filler instanceof OWLObjectComplementOf)){
					Predicate p = predicateFactory.createPredicate(uri, 2);
					Vector<Predicate> vec = new Vector<Predicate>();
					vec.add(p);
					ConceptDescription cd =descFactory.getConceptDescription(vec);
					return cd;
				}else{
					return null;
				}
			}
		}else{
			return null;
			//TODO throw exception?
		}
	}
}
