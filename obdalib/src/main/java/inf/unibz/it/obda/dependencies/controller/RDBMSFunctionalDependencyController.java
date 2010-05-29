package inf.unibz.it.obda.dependencies.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertionController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.domain.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


/**
 * The functional dependency controller manages the insertion, 
 * deletion and update of functional dependency assertion. 
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 *
 */
public class RDBMSFunctionalDependencyController extends
	AbstractDependencyAssertionController<RDBMSFunctionalDependency> {
	
	private DataSource currentDataSource = null;
	private HashMap<String, HashSet<RDBMSFunctionalDependency>> functionalDependencies= null;
	
	/**
	 * Creates a new instance of the RDBMSFunctionalDependencyController.
	 */
	public RDBMSFunctionalDependencyController (){
		functionalDependencies = new HashMap<String, HashSet<RDBMSFunctionalDependency>>();

	}
	
	/**
	 * Returns a new instance of the RDBMSFunctionalDependencyController.
	 */
	@Override
	public AssertionController<RDBMSFunctionalDependency> getInstance() {
		// TODO Auto-generated method stub
		return new RDBMSFunctionalDependencyController();
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
		return "RDBMSFunctionalDependencies";
	}

	/**
	 * Adds the given assertion and fires an assertion added event to all
	 * listeners.
	 */
	@Override
	public void addAssertion(RDBMSFunctionalDependency a) {
		if(a == null){
			return;
		}
		HashSet<RDBMSFunctionalDependency> aux = functionalDependencies.get(currentDataSource.getName());
		if(aux != null){
			aux.add(a);
			functionalDependencies.put(currentDataSource.getName(), aux);
			fireAssertionAdded(a);
		}
	}
	
	/**
	 * Removes the given assertion and fires an assertion removed event to all
	 * listeners.
	 */
	@Override
	public void removeAssertion(RDBMSFunctionalDependency a) {
		HashSet<RDBMSFunctionalDependency> aux = functionalDependencies.get(currentDataSource.getName());
		if(aux != null){
			aux.remove(a);
			functionalDependencies.put(currentDataSource.getName(), aux);
			fireAssertionRemoved(a);
		}
	}

	/**
	 * Is executed when the data source listener gets an all data source
	 * deleted event. All assertion are removed. Fires a assertion removed
	 * event to remove also the currently shown assertion from the UI.
	 */
	public void alldatasourcesDeleted() {
		HashSet<RDBMSFunctionalDependency> list = functionalDependencies.get(currentDataSource.getName());
		if(list != null){
			Iterator<RDBMSFunctionalDependency> it = list.iterator();
			while(it.hasNext()){
				fireAssertionRemoved(it.next());
			}
		}
		functionalDependencies = new HashMap<String, HashSet<RDBMSFunctionalDependency>>();
		
	}

	/**
	 * Is executed when the listener gets a datasource removed event.
	 * The method removes the assertion of the old data source from the
	 * UI and shows the assertions associated to the new data soruce
	 */
	public void currentDatasourceChange(DataSource previousdatasource,
			DataSource currentsource) {
		currentDataSource = currentsource;
		if(previousdatasource != currentsource){
			if(previousdatasource != null){
				HashSet<RDBMSFunctionalDependency> list = functionalDependencies.get(previousdatasource.getName());
				Iterator<RDBMSFunctionalDependency> it = list.iterator();
				while(it.hasNext()){
					fireAssertionRemoved(it.next());
				}
			}
			if(currentsource != null){
				HashSet<RDBMSFunctionalDependency> list1 = functionalDependencies.get(currentsource.getName());
				Iterator<RDBMSFunctionalDependency> it1 = list1.iterator();
				while(it1.hasNext()){
					fireAssertionAdded(it1.next());
				}
			}
		}
	}

	/**
	 * Add a new data source the Map. Is executed when the listener
	 * geta a data source added event.
	 */
	public void datasourceAdded(DataSource source) {
		
		functionalDependencies.put(source.getName(), new HashSet<RDBMSFunctionalDependency>());
	}

	/**
	 * Removes all assertions from the controller associated to the
	 * given data source. Is executed when the data source listener gets
	 * a data source delete event.
	 */
	public void datasourceDeleted(DataSource source) {
		
		if(currentDataSource == source){
			HashSet<RDBMSFunctionalDependency> list = functionalDependencies.get(currentDataSource.getName());
			Iterator<RDBMSFunctionalDependency> it = list.iterator();
			while(it.hasNext()){
				fireAssertionRemoved(it.next());
			}
		}
		functionalDependencies.remove(source.getName());
	}

	/**
	 * Updates the name of the data source to the given new one.
	 * The method is executed when the data source listener gets a 
	 * data source updated event.
	 */
	public void datasourceUpdated(String oldname, DataSource currendata) {
		HashSet<RDBMSFunctionalDependency> aux = functionalDependencies.get(oldname);
		functionalDependencies.remove(oldname);
		functionalDependencies.put(currendata.getName(), aux);
		currentDataSource = currendata;
		
	}

	/**
	 * Returns all functional dependency assertions associated to the
	 * currently selected data source.
	 */
	@Override
	public HashSet<RDBMSFunctionalDependency> getDependenciesForCurrentDataSource() {
		if(currentDataSource !=null){
			return functionalDependencies.get(currentDataSource.getName());
		}else{
			return  new HashSet<RDBMSFunctionalDependency>();
		}
	}
	
	/**
	 * Returns all functional dependency assertion in this controller.
	 */
	@Override
	public Collection<RDBMSFunctionalDependency> getAssertions() {
		
		Vector<RDBMSFunctionalDependency> assertions = new Vector<RDBMSFunctionalDependency>();
		Set<String>keys = functionalDependencies.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			HashSet<RDBMSFunctionalDependency> aux = functionalDependencies.get(it.next());
			assertions.addAll(aux);
		}
		
		return assertions;
	}
	
	/**
	 * Adds a functional dependency assertion to the controller without updating 
	 * the UI.
	 * @param a the RDBMSFunctionalDependency to add
	 */
	public boolean insertAssertion(RDBMSFunctionalDependency a) {
		HashSet<RDBMSFunctionalDependency> aux = functionalDependencies.get(currentDataSource.getName());
		if(aux != null){
			if(aux.add(a)){
				functionalDependencies.put(currentDataSource.getName(), aux);
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
	public HashSet<RDBMSFunctionalDependency> getAssertionsForDataSource(String uri) {
		
		return functionalDependencies.get(uri);
	}
	
	@Override
	public void datasourcParametersUpdated() {}

}
