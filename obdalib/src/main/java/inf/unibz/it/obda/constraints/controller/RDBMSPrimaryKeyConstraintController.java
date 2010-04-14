package inf.unibz.it.obda.constraints.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertionController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSPrimaryKeyConstraint;
import inf.unibz.it.obda.domain.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class RDBMSPrimaryKeyConstraintController extends
AbstractConstraintAssertionController<RDBMSPrimaryKeyConstraint> {

	private DataSource currentDataSource = null;
	private HashMap<String, HashSet<RDBMSPrimaryKeyConstraint>> pkconstraints= null;
	
	public RDBMSPrimaryKeyConstraintController (){
		pkconstraints = new HashMap<String, HashSet<RDBMSPrimaryKeyConstraint>>();
		
	}
	
	@Override
	public AssertionController<RDBMSPrimaryKeyConstraint> getInstance() {
		return new RDBMSPrimaryKeyConstraintController();
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
	public void addAssertion(RDBMSPrimaryKeyConstraint u) {
		if(u == null){
			return;
		}
		HashSet<RDBMSPrimaryKeyConstraint> aux = pkconstraints.get(currentDataSource.getName());
		if(aux != null && aux.add(u)){
			pkconstraints.put(currentDataSource.getName(), aux);
			fireAssertionAdded(u);
		}
	}
	
	/**
	 * Removes the given assertion and fires an assertion removed event to all
	 * listeners.
	 */
	@Override
	public void removeAssertion(RDBMSPrimaryKeyConstraint u) {
		HashSet<RDBMSPrimaryKeyConstraint> aux = pkconstraints.get(currentDataSource.getName());
		if(aux != null&&aux.remove(u)){
			pkconstraints.put(currentDataSource.getName(), aux);
			fireAssertionRemoved(u);
		}
	}

	/**
	 * Is executed when the data source listener gets an all data source
	 * deleted event. All assertion are removed. Fires a assertion removed
	 * event to remove also the currently shown assertion from the UI.
	 */
	public void alldatasourcesDeleted() {
		HashSet<RDBMSPrimaryKeyConstraint> list = pkconstraints.get(currentDataSource.getName());
		Iterator<RDBMSPrimaryKeyConstraint> it = list.iterator();
		while(it.hasNext()){
			fireAssertionRemoved(it.next());
		}
		pkconstraints = new HashMap<String, HashSet<RDBMSPrimaryKeyConstraint>>();
		
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
				HashSet<RDBMSPrimaryKeyConstraint> list = pkconstraints.get(previousdatasource.getName());
				Iterator<RDBMSPrimaryKeyConstraint> it = list.iterator();
				while(it.hasNext()){
					fireAssertionRemoved(it.next());
				}
			}
			if(currentsource != null){
				HashSet<RDBMSPrimaryKeyConstraint> list1 = pkconstraints.get(currentsource.getName());
				Iterator<RDBMSPrimaryKeyConstraint> it1 = list1.iterator();
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
		
		pkconstraints.put(source.getName(), new HashSet<RDBMSPrimaryKeyConstraint>());
	}

	/**
	 * Removes all assertions from the controller associated to the
	 * given data source. Is executed when the data source listener gets
	 * a data source delete event.
	 */
	public void datasourceDeleted(DataSource source) {
		
		if(currentDataSource == source){
			HashSet<RDBMSPrimaryKeyConstraint> list = pkconstraints.get(currentDataSource.getName());
			Iterator<RDBMSPrimaryKeyConstraint> it = list.iterator();
			while(it.hasNext()){
				fireAssertionRemoved(it.next());
			}
		}
		pkconstraints.remove(source.getName());
		
	}

	/**
	 * Updates the name of the data source to the given new one.
	 * The method is executed when the data source listener gets a 
	 * data source updated event.
	 */
	public void datasourceUpdated(String oldname, DataSource currendata) {
		HashSet<RDBMSPrimaryKeyConstraint> aux = pkconstraints.get(oldname);
		pkconstraints.remove(oldname);
		pkconstraints.put(currendata.getName(), aux);
		currentDataSource = currendata;
		
	}

	/**
	 * Returns all inclusion dependency assertions associated to the
	 * currently selected data source.
	 */
	@Override
	public HashSet<RDBMSPrimaryKeyConstraint> getDependenciesForCurrentDataSource() {
		// TODO Auto-generated method stub
		if(currentDataSource !=null){
			return pkconstraints.get(currentDataSource.getName());
		}else{
			return new HashSet<RDBMSPrimaryKeyConstraint>();
		}
	}
	
	/**
	 * Returns all inclusion dependency assertion in this controller.
	 */
	@Override
	public Collection<RDBMSPrimaryKeyConstraint> getAssertions() {
		
		Vector<RDBMSPrimaryKeyConstraint> assertions = new Vector<RDBMSPrimaryKeyConstraint>();
		Set<String>keys = pkconstraints.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			HashSet<RDBMSPrimaryKeyConstraint> aux = pkconstraints.get(it.next());
			assertions.addAll(aux);
		}
		
		return assertions;
	}

	/**
	 * Adds a inclusion dependency assertion to the controller without updating 
	 * the UI.
	 * @param a the RDBMSInclusionDependency to add
	 */
	public boolean insertAssertion(RDBMSPrimaryKeyConstraint a) {
		HashSet<RDBMSPrimaryKeyConstraint> aux = pkconstraints.get(currentDataSource.getName());
		if(aux != null){
			if(aux.add(a)){
				pkconstraints.put(currentDataSource.getName(), aux);
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
	public HashSet<RDBMSPrimaryKeyConstraint> getAssertionsForDataSource(String uri) {
	
		 return pkconstraints.get(uri); 
	}

}
