package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGEdgeIterator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.Edge;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAG;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.AbstractGraphIterator;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 *  Retrieve all the connection built in our DAG 
 * 
 *
 */

public class TBoxReasonerImpl implements TBoxReasoner{

	DAGImpl dag;
	GraphImpl graph;
	AbstractGraphIterator<Description, DefaultEdge> iterator;
	
	private Set<OClass> namedClasses;
	private Set<Property> property;
	

	public TBoxReasonerImpl(Ontology ontology, boolean named){
		
		//generate Graph
		GraphBuilderImpl change= new GraphBuilderImpl(ontology);
		
		GraphImpl graph = (GraphImpl) change.getGraph();
		
		//generate DAG
		DAGBuilderImpl change2 = new DAGBuilderImpl (graph);
		
		dag=(DAGImpl) change2.getDAG();
		
		if(named) //generate namedDAG
		{
			NamedDAGBuilderImpl transform = new NamedDAGBuilderImpl(dag);
			dag= transform.getDAG();	
		}
		
		namedClasses= dag.getClasses();
		property = dag.getRoles();
	}

	public TBoxReasonerImpl(DAG dag){
		this.dag=(DAGImpl) dag;
		namedClasses= dag.getClasses();
		property = dag.getRoles();


	}
	
	//reasoner on the graph (cycles admitted)
	public TBoxReasonerImpl(Graph graph){
		this.graph=(GraphImpl) graph;
		namedClasses= graph.getClasses();
		property = graph.getRoles();
		


	}

	/**return the direct children starting from the given node of the dag
	 *  @param named when it's true only the children that correspond to named classes and property
	 *   are returned
	 *  @result we return a set of set of description to distinguish between different nodes and equivalent nodes. 
	 *  equivalent nodes will be in the same set of description
	 */
	@Override
	public Set<Set<Description>> getDirectChildren(Description desc, boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		if(dag!=null){ //direct children over a dag
		
			//take the representative node
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;

		Set<DefaultEdge> edges = dag.incomingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description source = dag.getEdgeSource(edge);
			
	
			
			//get the child node and its equivalent nodes
			Set<Description> equivalences =getEquivalences(source,false);
			
			if(named){ //if true I search only for the named nodes
				
				Set<Description> namedEquivalences= getEquivalences(source, true);
				
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else{
					
					result.addAll( getNamedChildren(source));
//				for (Description equivalent: equivalences){
//					//I search for the first named description
//					if(!namedEquivalences.contains(equivalent) ){
//						
//						result.addAll( getNamedChildren(equivalent));
//					}	
//				}
				}
			}
			
			else{
			
			if (!equivalences.isEmpty())
			result.add(equivalences);
			}
		}
		
		}
		else //direct children over a graph
		{
			


			//get equivalences of the current node
			Set<Description> equivalenceSet= getEquivalences(desc, false);
			//I want to consider also the children of the equivalent nodes
			for (Description n: equivalenceSet){
			Set<DefaultEdge> edges = graph.incomingEdgesOf(n);
			for (DefaultEdge edge : edges) {
				Description source = graph.getEdgeSource(edge);
				
				
				//I don't want to consider as children the equivalent node of the current node desc
				if(equivalenceSet.contains(source)){
					continue;
				}
				Set<Description> equivalences =getEquivalences(source,false);
				
				if(named){ //if true I search only for the named nodes
					
					Set<Description> namedEquivalences= getEquivalences(source, true);
					
					if (!namedEquivalences.isEmpty())
						result.add(namedEquivalences);
					else{
					for (Description node: equivalences){
						//I search for the first named description
						if(!namedEquivalences.contains(node) ){
							
							result.addAll( getNamedChildren(node));
						}	
					}
					}
				}

				else{
				
				if (!equivalences.isEmpty())
				result.add(equivalences);
				}
			}
			}
		}
			
			

		return Collections.unmodifiableSet(result);
	}
	
	//private method that searches for the first named children

	private Set<Set<Description>> getNamedChildren(Description desc) {
		if(dag!=null){
			LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
			
			//get equivalences of the current node
			Set<Description> equivalenceSet= getEquivalences(desc, false);
			//I want to consider also the children of the equivalent nodes
			if(!dag.containsVertex(desc)){
				System.out.println(desc);
				System.out.println(equivalenceSet);
			}
			Set<DefaultEdge> edges = dag.incomingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description source = dag.getEdgeSource(edge);
				
				
				//I don't want to consider as children the equivalent node of the current node desc
				if(equivalenceSet.contains(source)){
					continue;
				}
				Set<Description> equivalences =getEquivalences(source,false);
				
					
					Set<Description> namedEquivalences= getEquivalences(source, true);
					
					if (!namedEquivalences.isEmpty())
						result.add(namedEquivalences);
					else{
						result.addAll( getNamedChildren(source));
//					for (Description node: equivalences){
//						//I search for the first named description
//						if(!namedEquivalences.contains(node) ){
//							
//							result.addAll( getNamedChildren(node));
//						}	
//					}
					}
			}
			return result;
		}
		else{
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		
		//get equivalences of the current node
		Set<Description> equivalenceSet= getEquivalences(desc, false);
		//I want to consider also the children of the equivalent nodes
		
		Set<DefaultEdge> edges = graph.incomingEdgesOf(desc);
		for (DefaultEdge edge : edges) {
			Description source = graph.getEdgeSource(edge);
			
			
			//I don't want to consider as children the equivalent node of the current node desc
			if(equivalenceSet.contains(source)){
				continue;
			}
			Set<Description> equivalences =getEquivalences(source,false);
			
				
				Set<Description> namedEquivalences= getEquivalences(source, true);
				
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else{
				for (Description node: equivalences){
					//I search for the first named description
					if(!namedEquivalences.contains(node) ){
						
						result.addAll( getNamedChildren(node));
					}	
				}
				}
		}
		return result;
		}
		
			
		
	}
	/**return the direct parents starting from the given node of the dag
	 *  @param named when it's true only the parents that correspond to named classes or property
	 *  are returned 
	 * */

	@Override
	public Set<Set<Description>> getDirectParents(Description desc, boolean named) {
		
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		if(dag!=null){ //direct parents over a dag
		
			//take the representative node
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;

		Set<DefaultEdge> edges = dag.outgoingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description target = dag.getEdgeTarget(edge);
			
			//get the child node and its equivalent nodes
			Set<Description> equivalences =getEquivalences(target,false);
			
			if(named){ //if true I search only for the named nodes
				
				Set<Description> namedEquivalences= getEquivalences(target, true);
				if (!namedEquivalences.isEmpty())
					result.add(namedEquivalences);
				else{
					result.addAll(getNamedParents(target));
//				for (Description equivalent: equivalences){
//					//I search for the first named description
//					if(!namedEquivalences.contains(equivalent) ){
//						
//						result.addAll(getNamedParents(equivalent));
//					}	
//				}
				}
				
			}
			else{
			
			if (!equivalences.isEmpty())
			result.add(equivalences);
			}
		}
			
		
		
		}
		else //direct parents over a graph
		{

			//get equivalences of the current node
			Set<Description> equivalenceSet= getEquivalences(desc, false);
			
				//I want to consider also the parents of the equivalent nodes
				for (Description n: equivalenceSet){
					Set<DefaultEdge> edges = graph.outgoingEdgesOf(n);
					for (DefaultEdge edge : edges) {
						Description target = graph.getEdgeTarget(edge);
					
					
					//I don't want to consider as parents the equivalent node of the current node desc
					if(equivalenceSet.contains(target)){
						continue;
					}
					Set<Description> equivalences =getEquivalences(target,false);
					
					if(named){ //if true I search only for the named nodes
						
						Set<Description> namedEquivalences= getEquivalences(target, true);
						if (!namedEquivalences.isEmpty())
							result.add(namedEquivalences);
						else{
						for (Description node: equivalences){
							//I search for the first named description
							if(!namedEquivalences.contains(node) ){
								
								result.addAll(getNamedParents(node));
							}	
						}
						}
						
					}
					else{
					
					if (!equivalences.isEmpty())
					result.add(equivalences);
					}
				}
				}
			}
				
				

			return Collections.unmodifiableSet(result);
		}

	//private method that search for the first named parents
		private Set<Set<Description>> getNamedParents(Description desc) {
			if(dag!=null){
				LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
				
				//get equivalences of the current node
				Set<Description> equivalenceSet= getEquivalences(desc, false);
				//I want to consider also the parents of the equivalent nodes
				
				Set<DefaultEdge> edges = dag.outgoingEdgesOf(desc);
				for (DefaultEdge edge : edges) {
					Description target = dag.getEdgeTarget(edge);
					
					
					//I don't want to consider as parents the equivalent node of the current node desc
					if(equivalenceSet.contains(target)){
						continue;
					}
					Set<Description> equivalences =getEquivalences(target,false);
					
						
						Set<Description> namedEquivalences= getEquivalences(target, true);
						
						if (!namedEquivalences.isEmpty())
							result.add(namedEquivalences);
						else{
							result.addAll(getNamedParents(target));
//						for (Description node: equivalences){
//							//I search for the first named description
//							if(!namedEquivalences.contains(node) ){
//								
//								result.addAll(getNamedParents(node));
//							}	
//						}
						}
					}
				return result;
			}
			else{
			LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
			
			//get equivalences of the current node
			Set<Description> equivalenceSet= getEquivalences(desc, false);
			//I want to consider also the parents of the equivalent nodes
			
			Set<DefaultEdge> edges = graph.outgoingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description target = graph.getEdgeTarget(edge);
				
				
				//I don't want to consider as parents the equivalent node of the current node desc
				if(equivalenceSet.contains(target)){
					continue;
				}
				Set<Description> equivalences =getEquivalences(target,false);
				
					
					Set<Description> namedEquivalences= getEquivalences(target, true);
					
					if (!namedEquivalences.isEmpty())
						result.add(namedEquivalences);
					else{
					for (Description node: equivalences){
						//I search for the first named description
						if(!namedEquivalences.contains(node) ){
							
							result.addAll(getNamedParents(node));
						}	
					}
					}
				}
			return result;
				
			}
		}
	/**traverse the graph 
	return the descendants starting from the given node of the dag
	 @param named when it's true only the descendants that are named classes or property 
	 are returned
	 */
	@Override
	public Set<Set<Description>> getDescendants(Description desc, boolean named){
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		if(dag!=null){

			Description node = dag.getReplacements().get(desc);
			if (node == null)
				node = desc;
			//reverse the dag
			 DirectedGraph<Description, DefaultEdge> reversed =
			            new EdgeReversedGraph<Description, DefaultEdge>(dag);
			 if(!reversed.containsVertex(node))
				 System.out.println(node+ " "+desc);
			 if(!reversed.containsVertex(desc))
				 System.out.println(node+ " "+desc);
			 iterator= new BreadthFirstIterator<Description, DefaultEdge>(reversed, node);
			
//			I don't want to consider the current node
			Description start=iterator.next();
			 
			 Description startNode=desc;
			Set<Description> sourcesStart =getEquivalences(startNode, named);
			Set<Description> sourcesStartnoNode= new HashSet<Description>();
			for(Description equivalent: sourcesStart){
				if(equivalent.equals(startNode))
					continue;
				sourcesStartnoNode.add(equivalent);
				
			}
			
			
			if(!sourcesStartnoNode.isEmpty())
			result.add(sourcesStartnoNode);
 
			//iterate over the subsequent nodes, they are all descendant of desc
			while(iterator.hasNext()){
				Description child=iterator.next();
				
					
				//add the node and its equivalent nodes		
				
				Set<Description> sources =getEquivalences(child, named);
			
				
				if(!sources.isEmpty())
				result.add(sources);
				
				
			}
		}
		else{
		//reverse the graph
		 DirectedGraph<Description, DefaultEdge> reversed =
		            new EdgeReversedGraph<Description, DefaultEdge>(graph);
		 
		iterator= new BreadthFirstIterator<Description, DefaultEdge>(reversed, desc);
		
		//I don't want to consider the current node
		Description current=iterator.next();
		
		//get equivalences of the current node
		Set<Description> equivalenceSet= getEquivalences(current, named);
		//iterate over the subsequent nodes, they are all descendant of desc
		while(iterator.hasNext()){
			Description node=iterator.next();

			//I don't want to add between the descendants a node equivalent to the starting node
					if(node.equals(current))
						continue;
				
					
			if(named){ //add only the named classes and property
				if(namedClasses.contains(node) | property.contains(node)){
				Set<Description> sources = new HashSet<Description>();
				sources.add(node);
				
				result.add(sources);
				}
			}
			else{
			Set<Description> sources = new HashSet<Description>();
			sources.add(node);
			
			result.add(sources);
			}
			
		}
		
		}
		
		
		//add each of them to the result
		return Collections.unmodifiableSet(result);
		
	}


	/** traverse the graph
	return the ancestors starting from the given node of the dag
	 @param named when it's true only the ancestors that are named classes or property 
	 are returned
	 */
	@Override
	public Set<Set<Description>> getAncestors(Description desc, boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		
		if(dag!=null){
			Description node = dag.getReplacements().get(desc);
			if (node == null)
				node = desc;
			 
			iterator= new BreadthFirstIterator<Description, DefaultEdge>(dag, node);
			
			//I don't want to consider the current node
			Description start=iterator.next();
		
			 Description startNode=desc;
			Set<Description> sourcesStart =getEquivalences(startNode, named);
			Set<Description> sourcesStartnoNode= new HashSet<Description>();
			for(Description equivalent: sourcesStart){
				if(equivalent.equals(startNode))
					continue;
				sourcesStartnoNode.add(equivalent);
				
			}
			
			
			if(!sourcesStartnoNode.isEmpty())
			result.add(sourcesStartnoNode);
			
 
			//iterate over the subsequent nodes, they are all ancestor of desc
			while(iterator.hasNext()){
				Description parent=iterator.next();
					
				//add the node and its equivalent nodes		
				
				Set<Description> sources =getEquivalences(parent, named);
				
				
				if(!sources.isEmpty())
				result.add(sources);
				
				
			}
		}
		else{

		 
		iterator= new BreadthFirstIterator<Description, DefaultEdge>(graph, desc);
		
		//I don't want to consider the current node
		Description current=iterator.next();
		
		//get equivalences of the current node
		Set<Description> equivalenceSet= getEquivalences(current, named);
		//iterate over the subsequent nodes, they are all ancestor of desc
		while(iterator.hasNext()){
			Description node=iterator.next();

			//I don't want to add between the ancestors a node equivalent to the starting node
					if(current.equals(node))
						continue;
				
					
			if(named){ //add only the named classes and property
				if(namedClasses.contains(node) | property.contains(node)){
				Set<Description> sources = new HashSet<Description>();
				sources.add(node);
				
				result.add(sources);
				}
			}
			else{
			Set<Description> sources = new HashSet<Description>();
			sources.add(node);
			
			result.add(sources);
			}
			
		}
		
		}
		
		
		//add each of them to the result
		return Collections.unmodifiableSet(result);
		

	}

	/**return the equivalences starting from the given node of the dag
	 *  @param named when it's true only the equivalences that are named classes or property 
	 are returned
	 */
	@Override
	public  Set<Description> getEquivalences(Description desc, boolean named) {
		//equivalences over a dag
		if(dag!= null){
		Set<Description> equivalents = dag.getMapEquivalences().get(desc);
		
		//if there are no equivalent nodes return the node or nothing
		if (equivalents == null ){
			
			if (named){
			if(namedClasses.contains(desc) | property.contains(desc)){
			return Collections.unmodifiableSet(Collections.singleton(desc));
			}
			else{ //return empty set if the node we are considering (desc) is not a named class or propertu
				Set<Description> equivalences = Collections.emptySet();
				return equivalences;
			}
			}
			return Collections.unmodifiableSet(Collections.singleton(desc));
		}
		Set<Description> equivalences = new LinkedHashSet<Description> ();
		if (named){
			for(Description vertex: equivalents){
				if(namedClasses.contains(vertex) | property.contains(vertex)){
					equivalences.add(vertex);
				}
			}
		}
		else{
			equivalences = equivalents;
		}
		return Collections.unmodifiableSet(equivalences);
		}
		//if equivalences over a graph
		else{

		//search for cycles 
		StrongConnectivityInspector<Description, DefaultEdge> inspector = new StrongConnectivityInspector<Description, DefaultEdge>(graph);
		
		//each set contains vertices which together form a strongly connected component within the given graph
		List<Set<Description>> equivalenceSets = inspector.stronglyConnectedSets();

		Set<Description> equivalences = new LinkedHashSet<Description> ();
			//I want to find the equivalent node of desc
			for (Set<Description> equivalenceSet : equivalenceSets) {
				if (equivalenceSet.size() >= 2){
					if(equivalenceSet.contains(desc)){
						if (named){
							for(Description vertex: equivalenceSet){
								if(namedClasses.contains(vertex) | property.contains(vertex)){
									equivalences.add(vertex);
								}
							}
							return Collections.unmodifiableSet(equivalences);
						}
					
						return Collections.unmodifiableSet(equivalenceSet);
					}
						
				}
					
			}
			
			//if there are not equivalent node return the node or nothing
			if (named){
				if(namedClasses.contains(desc) | property.contains(desc)){
				return Collections.unmodifiableSet(Collections.singleton(desc));
				}
				else{ //return empty set if the node we are considering (desc) is not a named class or propertu
					equivalences = Collections.emptySet();
					return equivalences;
				}
			}
			return Collections.unmodifiableSet(Collections.singleton(desc));
		
		
			
		}
	}

	public Set<Set <Description>> getNodes(boolean named){
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		if(dag!=null){
		for (Description vertex: dag.vertexSet()){
			result.add(getEquivalences(vertex,named));
		}
		}
		else{
			for (Description vertex: graph.vertexSet()){
				result.add(getEquivalences(vertex,named));
			}
			}
		
		return result;
		
	}

	@Override
	public DAGImpl getDAG() {

		return dag;
	}
	
//	public Ontology getSigmaOntology() {
//
//		OntologyFactory descFactory = new OntologyFactoryImpl();
//		Ontology sigma = descFactory.createOntology(OBDADataFactoryImpl.getIRI("sigma"));
//
//		AbstractGraphIterator<Description, DefaultEdge> iterator= new BreadthFirstIterator<Description, DefaultEdge>((DAGImpl)dag);
//		OntologyFactory fac = OntologyFactoryImpl.getInstance();
//
//		while (iterator.hasNext()) {
//			Description node = iterator.next();
//			//edges with parents
//			for(DefaultEdge edge: ((DAGImpl) dag).outgoingEdgesOf(node))
//			if (((DAGImpl) dag).getEdgeSource(edge) instanceof ClassDescription) {
//				ClassDescription sub = (ClassDescription)((DAGImpl) dag).getEdgeSource(edge);
//				ClassDescription superp = (ClassDescription) ((DAGImpl) dag).getEdgeTarget(edge);
//				if (superp instanceof PropertySomeRestriction)
//					continue;
//
//				Axiom ax = fac.createSubClassAxiom(sub, superp);
//				sigma.addEntities(ax.getReferencedEntities());
//				sigma.addAssertion(ax);
//			} else {
//				Property sub = (Property) ((DAGImpl) dag).getEdgeSource(edge);
//				Property superp = (Property) ((DAGImpl) dag).getEdgeTarget(edge);
//
//				Axiom ax = fac.createSubPropertyAxiom(sub, superp);
//				sigma.addEntities(ax.getReferencedEntities());
//
//				sigma.addAssertion(ax);
//			}
//			Set<Description> equivalents = dag.getMapEquivalences().get(node);
//			for(Description equivalent:equivalents){
//				if(equivalent!=node){
//					if (node instanceof ClassDescription) {
//						ClassDescription sub = (ClassDescription) node;
//						ClassDescription superp = (ClassDescription) equivalent;
//						if (superp instanceof PropertySomeRestriction)
//							continue;
//
//						Axiom ax = fac.createSubClassAxiom(sub, superp);
//						sigma.addEntities(ax.getReferencedEntities());
//						sigma.addAssertion(ax);
//					} else {
//						Property sub = (Property) node;
//						Property superp = (Property) equivalent;
//
//						Axiom ax = fac.createSubPropertyAxiom(sub, superp);
//						sigma.addEntities(ax.getReferencedEntities());
//
//						sigma.addAssertion(ax);
//					}
//					
//					if (equivalent instanceof ClassDescription) {
//						ClassDescription sub = (ClassDescription) equivalent;
//						ClassDescription superp = (ClassDescription) node;
//						if (superp instanceof PropertySomeRestriction)
//							continue;
//
//						Axiom ax = fac.createSubClassAxiom(sub, superp);
//						sigma.addEntities(ax.getReferencedEntities());
//						sigma.addAssertion(ax);
//					} else {
//						Property sub = (Property) equivalent;
//						Property superp = (Property) node;
//
//						Axiom ax = fac.createSubPropertyAxiom(sub, superp);
//						sigma.addEntities(ax.getReferencedEntities());
//
//						sigma.addAssertion(ax);
//					}
//				}
//			}
//		}
//
//		
//		return sigma;
//	}

	public  void getChainDAG() {
		if(dag!= null){
			//move everything to a graph that admits cycles
			GraphImpl modifiedGraph= new GraphImpl( DefaultEdge.class);
			
			
			//clone all the vertex and edges from dag
			
			for (Description v: dag.vertexSet()){
				modifiedGraph.addVertex(v);
				
			}
			 for (DefaultEdge e : dag.edgeSet()) {
		            Description s = dag.getEdgeSource(e);
		            Description t = dag.getEdgeTarget(e);
		            
		           
		            modifiedGraph.addEdge(s, t, e);
		        }
			 
		Collection<Description> nodes = new HashSet<Description>(dag.vertexSet());
		OntologyFactory fac = OntologyFactoryImpl.getInstance();
		HashSet<Description> processedNodes = new HashSet<Description>();
		for (Description node : nodes) {
			if (!(node instanceof PropertySomeRestriction) || processedNodes.contains(node)) {
				continue;
			}

			/*
			 * Adding a cycle between exists R and exists R- for each R.
			 */

			PropertySomeRestriction existsR = (PropertySomeRestriction) node;
			PropertySomeRestriction existsRin = fac.createPropertySomeRestriction(existsR.getPredicate(), !existsR.isInverse());
			Description existsNode = node;
			Description existsInvNode = dag.getNode(existsRin);
			Set<Set<Description>> childrenExist = new HashSet<Set<Description>>(getDirectChildren(existsNode, false));
			Set<Set<Description>> childrenExistInv = new HashSet<Set<Description>>(getDirectChildren(existsInvNode, false));

			for (Set<Description> children : childrenExist) {
//				for(Description child:children){
//				DAGOperations.addParentEdge(child, existsInvNode);
				Description firstChild=children.iterator().next();
				Description child=dag.getReplacements().get(firstChild);
				if(child==null)
					child=firstChild;
				if(!child.equals(existsInvNode))
				modifiedGraph.addEdge(child, existsInvNode);
				
//				}
			}
			for (Set<Description> children : childrenExistInv) {
//				for(Description child:children){
//				DAGOperations.addParentEdge(child, existsNode);
				Description firstChild=children.iterator().next();
				Description child=dag.getReplacements().get(firstChild);
				if(child==null)
					child=firstChild;
				if(!child.equals(existsNode))
				modifiedGraph.addEdge(child, existsNode);
				
//				}
			}
			
			Set<Set<Description>> parentExist = new HashSet<Set<Description>>(getDirectParents(existsNode,false));
			Set<Set<Description>> parentsExistInv = new HashSet<Set<Description>>(getDirectParents(existsInvNode,false));

			for (Set<Description> parents : parentExist) {
				Description firstParent=parents.iterator().next();
				Description parent=dag.getReplacements().get(firstParent);
				if(parent==null)
					parent=firstParent;
				if(!parent.equals(existsInvNode))
				modifiedGraph.addEdge( existsInvNode, parent);
				
//				}
			}
			
			for (Set<Description> parents : parentsExistInv) {
				Description firstParent=parents.iterator().next();
				Description parent=dag.getReplacements().get(firstParent);
				if(parent==null)
					parent=firstParent;
				if(!parent.equals(existsInvNode))
				modifiedGraph.addEdge( existsNode, parent);
				
//				}
			}

			processedNodes.add(existsInvNode);
			processedNodes.add(existsNode);
		}

		/* Collapsing the cycles */
		DAGBuilderImpl change2 = new DAGBuilderImpl (modifiedGraph, dag.getMapEquivalences(), dag.getReplacements());
		
		dag=(DAGImpl) change2.getDAG();
		
		
		}
		else //if graph
		{
			Collection<Description> nodes = new HashSet<Description>(graph.vertexSet());
			OntologyFactory fac = OntologyFactoryImpl.getInstance();
			HashSet<Description> processedNodes = new HashSet<Description>();
			for (Description node : nodes) {
				if (!(node instanceof PropertySomeRestriction) || processedNodes.contains(node)) {
					continue;
				}

				/*
				 * Adding a cycle between exists R and exists R- for each R.
				 */

				PropertySomeRestriction existsR = (PropertySomeRestriction) node;
				PropertySomeRestriction existsRin = fac.createPropertySomeRestriction(existsR.getPredicate(), !existsR.isInverse());
				Description existsNode = node;
				Description existsInvNode = existsRin;
				Set<Set<Description>> childrenExist = new HashSet<Set<Description>>(getDirectChildren(existsNode, false));
				Set<Set<Description>> childrenExistInv = new HashSet<Set<Description>>(getDirectChildren(existsInvNode, false));

				for (Set<Description> children : childrenExist) {
					for(Description child:children){
//					DAGOperations.addParentEdge(child, existsInvNode);
					graph.addEdge(child, existsInvNode);
					
					}
				}
				for (Set<Description> children : childrenExistInv) {
					for(Description child:children){
//					DAGOperations.addParentEdge(child, existsNode);
					graph.addEdge(child, existsNode);
					
					}
				}
				
				Set<Set<Description>> parentExist = new HashSet<Set<Description>>(getDirectParents(existsNode,false));
				Set<Set<Description>> parentsExistInv = new HashSet<Set<Description>>(getDirectParents(existsInvNode,false));

				for (Set<Description> parents : parentExist) {
					for(Description parent:parents){
//					DAGOperations.addParentEdge(existsInvNode, parent);
					graph.addEdge( existsInvNode, parent);
					
					}
				}
				
				for (Set<Description> parents : parentsExistInv) {
					for(Description parent:parents){
//					DAGOperations.addParentEdge(existsNode,parent);
					graph.addEdge( existsNode, parent);
					
					}
				}

				processedNodes.add(existsInvNode);
				processedNodes.add(existsNode);
			}

			
			/* Collapsing the cycles */
			DAGBuilderImpl change2 = new DAGBuilderImpl (graph);
			
			dag=(DAGImpl) change2.getDAG();
			
		}
		
	}
	

	public Ontology getSigmaOntology() {
		OntologyFactory descFactory = new OntologyFactoryImpl();

		Ontology sigma = descFactory.createOntology("sigma");

//		DAGEdgeIterator edgeiterator = new DAGEdgeIterator(dag);
		OntologyFactory fac = OntologyFactoryImpl.getInstance();
//		for(DefaultEdge edge: dag.edgeSet()){
//		while (edgeiterator.hasNext()) {
//			Edge edge = edgeiterator.next();
			for(Description node:dag.vertexSet()){
				for (Set<Description> descendants: getDescendants(node, false)){
						Description firstDescendant=descendants.iterator().next();
						Description descendant= dag.getReplacements().get(firstDescendant);
						if(descendant==null)
							descendant=firstDescendant;
						Axiom axiom = null;
						/*
						 * Creating subClassOf or subPropertyOf axioms
						 */
						if(!descendant.equals(node)){
						if (descendant instanceof ClassDescription) {
							ClassDescription sub = (ClassDescription)descendant;
							ClassDescription superp = (ClassDescription) node;
							if (superp instanceof PropertySomeRestriction)
								continue;

							Axiom ax = fac.createSubClassAxiom(sub, superp);
							sigma.addEntities(ax.getReferencedEntities());
							sigma.addAssertion(ax);
						} else {
							Property sub = (Property) descendant;
							Property superp = (Property) node;

							Axiom ax = fac.createSubPropertyAxiom(sub, superp);
							sigma.addEntities(ax.getReferencedEntities());

							sigma.addAssertion(ax);
						}
					
						}
				}
				for(Description equivalent: getEquivalences(node, false)){
					if(!equivalent.equals(node)){
						Axiom ax = null;
						if (node instanceof ClassDescription) {
							ClassDescription sub = (ClassDescription)node;
							ClassDescription superp = (ClassDescription) equivalent;
							if (!(superp instanceof PropertySomeRestriction)){
								ax = fac.createSubClassAxiom(sub, superp);
								sigma.addEntities(ax.getReferencedEntities());
								sigma.addAssertion(ax);
							}
							
						} else {
							Property sub = (Property)node;
							Property superp = (Property) equivalent;

							ax = fac.createSubPropertyAxiom(sub, superp);
							sigma.addEntities(ax.getReferencedEntities());
							sigma.addAssertion(ax);
							
						}
						
						if (equivalent instanceof ClassDescription) {
							ClassDescription sub = (ClassDescription)equivalent;
							ClassDescription superp = (ClassDescription) node;
							if (!(superp instanceof PropertySomeRestriction)){
							 ax = fac.createSubClassAxiom(sub, superp);
							 sigma.addEntities(ax.getReferencedEntities());
								sigma.addAssertion(ax);
							}
							
						} else {
							Property sub = (Property) equivalent;
							Property superp = (Property) node;

							ax = fac.createSubPropertyAxiom(sub, superp);
							sigma.addEntities(ax.getReferencedEntities());
							sigma.addAssertion(ax);
							
						}
						
						
					}
				
			}
			}
//		}
			

		return sigma;
	}
			
	
	
	public static Ontology getSigma(Ontology ontology) {
		OntologyFactory descFactory = new OntologyFactoryImpl();
		Ontology sigma = descFactory.createOntology("sigma");
		sigma.addConcepts(ontology.getConcepts());
		sigma.addRoles(ontology.getRoles());
		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl inclusion = (SubClassAxiomImpl) assertion;
				Description parent = inclusion.getSuper();
				Description child = inclusion.getSub();
				if (parent instanceof PropertySomeRestriction) {
					continue;
				}
			}

			sigma.addAssertion(assertion);
		}

		sigma.saturate();
		return sigma;
	}

	

}
