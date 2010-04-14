package inf.unibz.it.obda.constraints.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertionController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.domain.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class RDBMSUniquenessConstraintController extends
				AbstractConstraintAssertionController<RDBMSUniquenessConstraint> {

	private DataSource currentDataSource = null;
	private HashMap<String, HashSet<RDBMSUniquenessConstraint>> uniquenessconstraints= null;
	
	public RDBMSUniquenessConstraintController (){
		uniquenessconstraints = new HashMap<String, HashSet<RDBMSUniquenessConstraint>>();
		
	}
	
	@Override
	public AssertionController<RDBMSUniquenessConstraint> getInstance() {
		return new RDBMSUniquenessConstraintController();
	}

	@Override
	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("datasource_uri");
		return attributes;
	}

	@Override
	public String getElementTag() {
		return "RDBMSUniquenessConstraint";
	}
	
	/**
	 * Adds the given assertion and fires an assertion added event to all
	 * listeners.
	 */
	@Override
	public void addAssertion(RDBMSUniquenessConstraint u) {
		if(u == null){
			return;
		}
		HashSet<RDBMSUniquenessConstraint> aux = uniquenessconstraints.get(currentDataSource.getName());
		if(aux != null && aux.add(u)){
			uniquenessconstraints.put(currentDataSource.getName(), aux);
			fireAssertionAdded(u);
		}
	}
	
	/**
	 * Removes the given assertion and fires an assertion removed event to all
	 * listeners.
	 */
	@Override
	public void removeAssertion(RDBMSUniquenessConstraint u) {
		HashSet<RDBMSUniquenessConstraint> aux = uniquenessconstraints.get(currentDataSource.getName());
		if(aux != null&&aux.remove(u)){
			uniquenessconstraints.put(currentDataSource.getName(), aux);
			fireAssertionRemoved(u);
		}
	}

	/**
	 * Is executed when the data source listener gets an all data source
	 * deleted event. All assertion are removed. Fires a assertion removed
	 * event to remove also the currently shown assertion from the UI.
	 */
	public void alldatasourcesDeleted() {
		HashSet<RDBMSUniquenessConstraint> list = uniquenessconstraints.get(currentDataSource.getName());
		Iterator<RDBMSUniquenessConstraint> it = list.iterator();
		while(it.hasNext()){
			fireAssertionRemoved(it.next());
		}
		uniquenessconstraints = new HashMap<String, HashSet<RDBMSUniquenessConstraint>>();
		
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
				HashSet<RDBMSUniquenessConstraint> list = uniquenessconstraints.get(previousdatasource.getName());
				Iterator<RDBMSUniquenessConstraint> it = list.iterator();
				while(it.hasNext()){
					fireAssertionRemoved(it.next());
				}
			}
			if(currentsource != null){
				HashSet<RDBMSUniquenessConstraint> list1 = uniquenessconstraints.get(currentsource.getName());
				Iterator<RDBMSUniquenessConstraint> it1 = list1.iterator();
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
		
		uniquenessconstraints.put(source.getName(), new HashSet<RDBMSUniquenessConstraint>());
	}

	/**
	 * Removes all assertions from the controller associated to the
	 * given data source. Is executed when the data source listener gets
	 * a data source delete event.
	 */
	public void datasourceDeleted(DataSource source) {
		
		if(currentDataSource == source){
			HashSet<RDBMSUniquenessConstraint> list = uniquenessconstraints.get(currentDataSource.getName());
			Iterator<RDBMSUniquenessConstraint> it = list.iterator();
			while(it.hasNext()){
				fireAssertionRemoved(it.next());
			}
		}
		uniquenessconstraints.remove(source.getName());
		
	}

	/**
	 * Updates the name of the data source to the given new one.
	 * The method is executed when the data source listener gets a 
	 * data source updated event.
	 */
	public void datasourceUpdated(String oldname, DataSource currendata) {
		HashSet<RDBMSUniquenessConstraint> aux = uniquenessconstraints.get(oldname);
		uniquenessconstraints.remove(oldname);
		uniquenessconstraints.put(currendata.getName(), aux);
		currentDataSource = currendata;
		
	}

	/**
	 * Returns all inclusion dependency assertions associated to the
	 * currently selected data source.
	 */
	@Override
	public HashSet<RDBMSUniquenessConstraint> getDependenciesForCurrentDataSource() {
		// TODO Auto-generated method stub
		if(currentDataSource !=null){
			return uniquenessconstraints.get(currentDataSource.getName());
		}else{
			return new HashSet<RDBMSUniquenessConstraint>();
		}
	}
	
	/**
	 * Returns all inclusion dependency assertion in this controller.
	 */
	@Override
	public Collection<RDBMSUniquenessConstraint> getAssertions() {
		
		Vector<RDBMSUniquenessConstraint> assertions = new Vector<RDBMSUniquenessConstraint>();
		Set<String>keys = uniquenessconstraints.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			HashSet<RDBMSUniquenessConstraint> aux = uniquenessconstraints.get(it.next());
			assertions.addAll(aux);
		}
		
		return assertions;
	}

	/**
	 * Adds a inclusion dependency assertion to the controller without updating 
	 * the UI.
	 * @param a the RDBMSInclusionDependency to add
	 */
	public boolean insertAssertion(RDBMSUniquenessConstraint a) {
		HashSet<RDBMSUniquenessConstraint> aux = uniquenessconstraints.get(currentDataSource.getName());
		if(aux != null){
			if(aux.add(a)){
				uniquenessconstraints.put(currentDataSource.getName(), aux);
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
	public HashSet<RDBMSUniquenessConstraint> getAssertionsForDataSource(String uri) {
	
		 return uniquenessconstraints.get(uri); 
	}

}
