package inf.unibz.it.obda.constraints.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertionController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSCheckConstraint;
import inf.unibz.it.obda.domain.DataSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class RDBMSCheckConstraintController extends
AbstractConstraintAssertionController<RDBMSCheckConstraint> {

	private DataSource currentDataSource = null;
	private HashMap<URI, HashSet<RDBMSCheckConstraint>> checkconstraints= null;
	
	public RDBMSCheckConstraintController (){
		checkconstraints = new HashMap<URI, HashSet<RDBMSCheckConstraint>>();
	}
	
	@Override
	public AssertionController<RDBMSCheckConstraint> getInstance() {
		return new RDBMSCheckConstraintController();
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
	public void addAssertion(RDBMSCheckConstraint u) {
		if(u == null){
			return;
		}
		HashSet<RDBMSCheckConstraint> aux = checkconstraints.get(currentDataSource.getSourceID());
		if(aux != null && aux.add(u)){
			checkconstraints.put(currentDataSource.getSourceID(), aux);
			fireAssertionAdded(u);
		}
	}
	
	/**
	 * Removes the given assertion and fires an assertion removed event to all
	 * listeners.
	 */
	@Override
	public void removeAssertion(RDBMSCheckConstraint u) {
		HashSet<RDBMSCheckConstraint> aux = checkconstraints.get(currentDataSource.getSourceID());
		if(aux != null&&aux.remove(u)){
			checkconstraints.put(currentDataSource.getSourceID(), aux);
			fireAssertionRemoved(u);
		}
	}

	/**
	 * Is executed when the data source listener gets an all data source
	 * deleted event. All assertion are removed. Fires a assertion removed
	 * event to remove also the currently shown assertion from the UI.
	 */
	public void alldatasourcesDeleted() {
		HashSet<RDBMSCheckConstraint> list = checkconstraints.get(currentDataSource.getSourceID());
		Iterator<RDBMSCheckConstraint> it = list.iterator();
		while(it.hasNext()){
			fireAssertionRemoved(it.next());
		}
		checkconstraints = new HashMap<URI, HashSet<RDBMSCheckConstraint>>();
		
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
				HashSet<RDBMSCheckConstraint> list = checkconstraints.get(previousdatasource.getSourceID());
				Iterator<RDBMSCheckConstraint> it = list.iterator();
				while(it.hasNext()){
					fireAssertionRemoved(it.next());
				}
			}
			if(currentsource != null){
				HashSet<RDBMSCheckConstraint> list1 = checkconstraints.get(currentsource.getSourceID());
				Iterator<RDBMSCheckConstraint> it1 = list1.iterator();
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
		
		checkconstraints.put(source.getSourceID(), new HashSet<RDBMSCheckConstraint>());
	}

	/**
	 * Removes all assertions from the controller associated to the
	 * given data source. Is executed when the data source listener gets
	 * a data source delete event.
	 */
	public void datasourceDeleted(DataSource source) {
		
		if(currentDataSource == source){
			HashSet<RDBMSCheckConstraint> list = checkconstraints.get(currentDataSource.getSourceID());
			Iterator<RDBMSCheckConstraint> it = list.iterator();
			while(it.hasNext()){
				fireAssertionRemoved(it.next());
			}
		}
		checkconstraints.remove(source.getSourceID());
		
	}

	/**
	 * Updates the name of the data source to the given new one.
	 * The method is executed when the data source listener gets a 
	 * data source updated event.
	 */
	public void datasourceUpdated(String oldname, DataSource currendata) {
		HashSet<RDBMSCheckConstraint> aux = checkconstraints.get(oldname);
		checkconstraints.remove(oldname);
		checkconstraints.put(currendata.getSourceID(), aux);
		currentDataSource = currendata;
		
	}

	/**
	 * Returns all inclusion dependency assertions associated to the
	 * currently selected data source.
	 */
	@Override
	public HashSet<RDBMSCheckConstraint> getDependenciesForCurrentDataSource() {
		// TODO Auto-generated method stub
		if(currentDataSource !=null){
			return checkconstraints.get(currentDataSource.getSourceID());
		}else{
			return new HashSet<RDBMSCheckConstraint>();
		}
	}
	
	/**
	 * Returns all inclusion dependency assertion in this controller.
	 */
	@Override
	public Collection<RDBMSCheckConstraint> getAssertions() {
		
		Vector<RDBMSCheckConstraint> assertions = new Vector<RDBMSCheckConstraint>();
		Set<URI>keys = checkconstraints.keySet();
		Iterator<URI> it = keys.iterator();
		while(it.hasNext()){
			HashSet<RDBMSCheckConstraint> aux = checkconstraints.get(it.next());
			assertions.addAll(aux);
		}
		
		return assertions;
	}

	/**
	 * Adds a inclusion dependency assertion to the controller without updating 
	 * the UI.
	 * @param a the RDBMSInclusionDependency to add
	 */
	public boolean insertAssertion(RDBMSCheckConstraint a) {
		HashSet<RDBMSCheckConstraint> aux = checkconstraints.get(currentDataSource.getSourceID());
		if(aux != null){
			if(aux.add(a)){
				checkconstraints.put(currentDataSource.getSourceID(), aux);
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
	public HashSet<RDBMSCheckConstraint> getAssertionsForDataSource(URI uri) {
	
		 return checkconstraints.get(uri); 
	}
	
	@Override
	public void datasourcParametersUpdated() {}

}
