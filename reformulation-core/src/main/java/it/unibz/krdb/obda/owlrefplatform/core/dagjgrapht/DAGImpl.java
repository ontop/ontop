package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/** Use to build a DAG and a named DAG.
 * Extend SimpleDirectedGraph from the JGrapht library
 * 
 *  
 * @author Sarah
 *
 */

public class DAGImpl extends SimpleDirectedGraph <Description,DefaultEdge> implements DAG {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4466539952784531284L;
	
	boolean dag = false;
	boolean namedDAG = false;
	
	private Set<OClass> classes = new LinkedHashSet<OClass> ();
	private Set<Property> roles = new LinkedHashSet<Property> ();
	
	//map between an element  and the representative between the equivalent elements
	private Map<Description, Description> replacements = new HashMap<Description, Description>();
	
	//map of the equivalent elements of an element
	private Map<Description, Set<Description>> equivalencesMap = new HashMap<Description, Set<Description>>();

	public DAGImpl(Class<? extends DefaultEdge> arg0) {
		super(arg0);
		dag=true;
	}

	public DAGImpl(EdgeFactory<Description,DefaultEdge> ef) {
		super(ef);
		dag=true;
	}
	
	
	//set the graph is a dag
	public void setIsaDAG(boolean d){
		
		dag=d;
		namedDAG=!d;

	}

	//set the graph is a named description dag
	public void setIsaNamedDAG(boolean nd){
		
		namedDAG=nd;
		dag=!nd;

	}
	//check if the graph is a dag
	public boolean isaDAG(){
		return dag;

	}

	//check if the graph is a named description dag
	public boolean isaNamedDAG(){
		return namedDAG;


	}
	
	//return all property (not inverse) in the DAG
	public Set<Property> getRoles(){
		for (Description r: this.vertexSet()){
			
			//check in the equivalent nodes if there are properties
			if(replacements.containsValue(r)){
			if(equivalencesMap.get(r)!=null){
				for (Description e: equivalencesMap.get(r))	{
					if (e instanceof Property){
//						System.out.println("roles: "+ e +" "+ e.getClass());
						if(!((Property) e).isInverse())
						roles.add((Property)e);
						
				}
				}
			}
			}
			if (r instanceof Property){
//				System.out.println("roles: "+ r +" "+ r.getClass());
				if(!((Property) r).isInverse())
				roles.add((Property)r);
			}

		}
		return roles;

	}

	
	//return all named classes in the dag
	public Set<OClass> getClasses(){
		for (Description c: this.vertexSet()){
			
			//check in the equivalent nodes if there are named classes
			if(replacements.containsValue(c)){
			if(equivalencesMap.get(c)!=null){
				
				for (Description e: equivalencesMap.get(c))	{
					if (e instanceof OClass){
//						System.out.println("classes: "+ e +" "+ e.getClass());
						classes.add((OClass)e);
				}
				}
			}
			}
			
			if (c instanceof OClass){
//				System.out.println("classes: "+ c+ " "+ c.getClass());
				classes.add((OClass)c);
			}

		}
		return classes;

	}


	@Override
	public Map<Description, Set<Description>> getMapEquivalences() {
		
		return equivalencesMap;
	}

	@Override
	public Map<Description, Description> getReplacements() {
		return replacements;
	}

	@Override
	public void setMapEquivalences(Map<Description, Set<Description>> equivalences) {
		this.equivalencesMap= equivalences;
		
	}

	@Override
	public void setReplacements(Map<Description, Description> replacements) {
		this.replacements=replacements;
		
	}
	
	@Override
	//return the node considering also the equivalent nodes
	public Description getNode(Description node){
		if(replacements.containsKey(node))
			node= replacements.get(node);
		else
		if(!this.vertexSet().contains(node))
			node=null;
		return node;
		
	}

	






}
