package inf.unibz.it.obda.dependencies.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertionController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.domain.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * The disjointness dependency controller manages the insertion, 
 * deletion and update of disjointness assertion. 
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 *
 */

public class RDBMSDisjointnessDependencyController extends
	AbstractDependencyAssertionController<RDBMSDisjointnessDependency> {

	private DataSource currentDataSource = null;
	private HashMap<String, HashSet<RDBMSDisjointnessDependency>> disjointnessDependencies= null;
//	
	/**
	 * Creates a new instance of the RDBMSDisjointnessDependencyController.
	 */
	public RDBMSDisjointnessDependencyController (){
		disjointnessDependencies = new HashMap<String, HashSet<RDBMSDisjointnessDependency>>();
	}
	
	/**
	 * Returns a new instance of the RDBMSDisjointnessDependencyController.
	 */
	@Override
	public AssertionController<RDBMSDisjointnessDependency> getInstance() {
		// TODO Auto-generated method stub
		return new RDBMSDisjointnessDependencyController();
	}

	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("datasource_uri");
		return attributes;
	}

	/**
	 * Returns the tag name, with which the assertions are stored in
	 * the obda file
	 */
	public String getElementTag() {
		// TODO Auto-generated method stub
		return "RDBMSDisjointnessDependencies";
	}

	/**
	 * Adds the given assertion and fires an assertion added event to all
	 * listeners.
	 */
	@Override
	public void addAssertion(RDBMSDisjointnessDependency a) {
		if(a == null){
			return;
		}
		HashSet<RDBMSDisjointnessDependency> aux = disjointnessDependencies.get(currentDataSource.getName());
		if(aux != null){
			aux.add(a);
			disjointnessDependencies.put(currentDataSource.getName(), aux);
			fireAssertionAdded(a);
		}
	}
	
	/**
	 * Removes the given assertion and fires an assertion removed event to all
	 * listeners.
	 */
	@Override
	public void removeAssertion(RDBMSDisjointnessDependency a) {
		HashSet<RDBMSDisjointnessDependency> aux = disjointnessDependencies.get(currentDataSource.getName());
		if(aux != null){
			aux.remove(a);
			disjointnessDependencies.put(currentDataSource.getName(), aux);
			fireAssertionRemoved(a);
		}
	}
	
	/**
	 * Is executed when the data source listener gets an all data source
	 * deleted event. All assertion are removed. Fires a assertion removed
	 * event to remove also the currently shown assertion from the UI.
	 */
	public void alldatasourcesDeleted() {
		
		HashSet<RDBMSDisjointnessDependency> list = disjointnessDependencies.get(currentDataSource.getName());
		if(list != null){
			Iterator<RDBMSDisjointnessDependency> it = list.iterator();
			while(it.hasNext()){
				fireAssertionRemoved(it.next());
			}
		}
		disjointnessDependencies = new HashMap<String, HashSet<RDBMSDisjointnessDependency>>();
		
	}

	/**
	 * Is executed when the listener gets a datasource removed event.
	 * The method removes the assertion of the old data source from the
	 * UI and shows the assertions associated to the new data soruce
	 */
	public void currentDatasourceChange(DataSource previousdatasource,
			DataSource currentsource) {
		currentDataSource=currentsource;
		if(previousdatasource != currentsource){
			if(previousdatasource != null){
				HashSet<RDBMSDisjointnessDependency> list = disjointnessDependencies.get(previousdatasource.getName());
				Iterator<RDBMSDisjointnessDependency> it = list.iterator();
				while(it.hasNext()){
					fireAssertionRemoved(it.next());
				}
			}
			if(currentsource != null){
				HashSet<RDBMSDisjointnessDependency> list1 = disjointnessDependencies.get(currentsource.getName());
				Iterator<RDBMSDisjointnessDependency> it1 = list1.iterator();
				while(it1.hasNext()){
					fireAssertionAdded(it1.next());
				}
			}
		}
	}
	
	/**
	 * Add a new data source the Map. Is executed when the listener
	 * gets a data source added event.
	 */
	public void datasourceAdded(DataSource source) {
		
		disjointnessDependencies.put(source.getName(), new HashSet<RDBMSDisjointnessDependency>());
	}

	/**
	 * Removes all assertions from the controller associated to the
	 * given data source. Is executed when the data source listener gets
	 * a data source delete event.
	 */
	public void datasourceDeleted(DataSource source) {
		
		if(currentDataSource == source){
			HashSet<RDBMSDisjointnessDependency> list = disjointnessDependencies.get(currentDataSource.getName());
			Iterator<RDBMSDisjointnessDependency> it = list.iterator();
			while(it.hasNext()){
				fireAssertionRemoved(it.next());
			}
		}
		disjointnessDependencies.remove(source.getName());
	}

	/**
	 * Updates the name of the data source to the given new one.
	 * The method is executed when the data source listener gets a 
	 * data source updated event.
	 */
	public void datasourceUpdated(String oldname, DataSource currendata) {
		HashSet<RDBMSDisjointnessDependency> aux = disjointnessDependencies.get(oldname);
		disjointnessDependencies.remove(oldname);
		disjointnessDependencies.put(currendata.getName(), aux);
		currentDataSource = currendata;
		
	}

	/**
	 * Returns all disjointness dependency assertions associated to the
	 * currently selected data source.
	 */
	@Override
	public HashSet<RDBMSDisjointnessDependency> getDependenciesForCurrentDataSource() {
		// TODO Auto-generated method stub
		if(currentDataSource !=null){
			return disjointnessDependencies.get(currentDataSource.getName());
		}else{
			return  new HashSet<RDBMSDisjointnessDependency>();
		}	
	}

	/**
	 * Returns all disjointness dependency assertion in this controller.
	 */
	@Override
	public Collection<RDBMSDisjointnessDependency> getAssertions() {
		
		Vector<RDBMSDisjointnessDependency> assertions = new Vector<RDBMSDisjointnessDependency>();
		Set<String>keys = disjointnessDependencies.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			HashSet<RDBMSDisjointnessDependency> aux = disjointnessDependencies.get(it.next());
			assertions.addAll(aux);
		}
		
		return assertions;
	}
	
	
	/**
	 * Adds a disjointness assertion to the controller without updating 
	 * the UI.
	 * @param a the RDBMSDisjointnessDependency to add
	 */
	public boolean insertAssertion(RDBMSDisjointnessDependency a) {
		HashSet<RDBMSDisjointnessDependency> aux = disjointnessDependencies.get(currentDataSource.getName());
		if(aux != null){
			if(aux.add(a)){
				disjointnessDependencies.put(currentDataSource.getName(), aux);
				return true;
			}else{
				return false;
			}
		}
		return false;
	}

	/**
	 * Returns all RDBMSDisjointnessDependency for the given data source uri
	 */
	@Override
	public HashSet<RDBMSDisjointnessDependency> getAssertionsForDataSource(String uri) {
		// TODO Auto-generated method stub
		return disjointnessDependencies.get(uri);
	}
}
