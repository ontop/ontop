package it.unibz.krdb.obda.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class LookupTable {
	
	private static final String DEFAULT_NAME_FORMAT = "t%s";  // e.g., t1, t2, ...
	
	/**
	 * Map of entries that have an alternative name.
	 */
	private HashMap<String, Integer> log = new HashMap<String, Integer>();
	
	/**
	 * Map of alternative names.
	 */
	private HashMap<Integer, String> master = new HashMap<Integer, String>();
	
	/**
	 * A special number that connects one or more entries to its alternative name.
	 */
	private int index = 1;
	
	public LookupTable() {
		// Does nothing!
	}
	
	/**
	 * Adds a string entry to this lookup table. A special index will be assigned
	 * to this entry which points to a alternative name defined by the system.
	 * 
	 * @param entry
	 * 			Any string.
	 */
	public void add(String entry) {
		if (entry == null) {
			return;
		}		
		log.put(entry, index);
		register();
		increaseIndex();
	}
	
	/**
	 * Adds a collection of strings to this lookup table. All those strings will
	 * point to a same alternative name defined by the system.
	 * 
	 * @param entries
	 * 			An array of strings.
	 */
	public void add(String[] entries) {
		for (int i = 0; i < entries.length; i++) {
			if (entries[i] != null) {
				log.put(entries[i], index);
			}
		}
		register();
		increaseIndex();
	}
	
	/**
	 * Adds a string entry to this lookup table with a reference of an existing
	 * entry. This method is used when users want to insert a new entry that 
	 * has a similarity to the existing one, so both have a same alternative
	 * name. For example, consider the initial lookup table has:
	 * <pre>
	 * "Employee.id" --> "t1"
	 * "Employee.name" --> "t2"</pre>
	 * Calling the method add("id_number", "Employee.id") will give you the 
	 * result:
	 * <pre>
	 * "Employee.id" --> "t1"
	 * "Employee.name" --> "t2"
	 * "id_number" --> "t1" </pre>
	 * 
	 * @param entry
	 * 			A new entry.
	 * @param reference
	 * 			An entry that exists already in the lookup table. The method
	 * 			will get its index and assign it to the new entry. If the 
	 * 			reference is not existed yet, then the new entry will get
	 * 			a new index.
	 */
	public void add(String entry, String reference) {
		if (entry == null) {
			return;
		}
		if (!log.containsKey(reference)) {
			add(entry);
		}
		else {
			Integer index = log.get(reference);
			log.put(entry, index);
		}
	}
	
	/**
	 * Returns the alternative name for the given entry.
	 */
	public String lookup(String entry) {
		if (log.containsKey(entry)) {
			Integer index = log.get(entry);
			return retrieve(index);
		}
		return "";
	}
	
	/**
	 * Removes the given entry from the lookup table. This action follows the
	 * removal of the alternative name, if necessary.
	 */
	public void remove(String entry) {
		log.remove(entry);
		unregister();
	}
	
	/**
	 * Removes more than one entry from the lookup table. This action follows
	 * the removal of the alternative name, if necessary.
	 */
	public void remove(String[] entries) {
		for (int i = 0; i < entries.length; i++) {
			log.remove(entries[i]);
		}
		unregister();
	}
	
	/**
	 * Updates the alternative name for the given entry to be the same as the
	 * reference entry. The method returns false if either one or both entry
	 * values are not in the lookup table. For example, consider the initial 
	 * lookup table has:
	 * <pre>
	 * "Employee.id" --> "t1"
	 * "Employee.name" --> "t2"
	 * "Salary.pid" --> "t3" </pre>
	 * Calling the method asEqualTo("Salary.pid", "Employee.id") will give you the 
	 * result:
	 * <pre>
	 * "Employee.id" --> "t1"
	 * "Employee.name" --> "t2"
	 * "Salary.pid" --> "t1" </pre>
	 */
	public boolean asEqualTo(String entry, String reference) {
		if (!log.containsKey(entry) || !log.containsKey(reference)) {
			return false;
		}
		String name = lookup(reference);
		Integer index = log.get(entry);
		update(index, name);
		
		return true;
	}
	
	@Override
	public String toString() {
		final String printFormat = "%s --> %s";
		
		String str = "";		
		for (String entry : log.keySet()) {
			String name = lookup(entry);
			str += String.format(printFormat, entry, name);
			str += "\n";
		}
		return str;
	}
	
	/* Retrieves the alternative name given the index number */
	private String retrieve(int index) {
		return master.get(index);
	}
	
	/* Changes the alternative in the given index number */
	private void update(int index, String value) {
		master.put(index, value);
	}
	
	/* Assigns the newly added entry to an alternative name. */
	private void register() {
		if (!master.containsKey(index)) {
			String name = String.format(DEFAULT_NAME_FORMAT, index);
			master.put(index, name);
		}
	}
	
	/* Removes the alternative name if the index is no longer available
	 * in the lookup table.
	 */
	private void unregister() {
		Set<Integer> set = new HashSet<Integer>();
		Collections.addAll(set, log.values().toArray(new Integer[0]));
		Integer[] logIndex = set.toArray(new Integer[0]);		
		Integer[] masterIndex = master.keySet().toArray(new Integer[0]);
		
		for (int i = 0; i < masterIndex.length; i++) {
			boolean bExist = false;
			for (int j = 0; j < logIndex.length; j++) {
				if (masterIndex[i] == logIndex[j]) {
					bExist = true;
					break;
				}
			}
			if (!bExist) {
				master.remove(masterIndex[i]);
			}
		}
	}
	
	/* Advances by one the index number. */
	private void increaseIndex() {
		index++;
	}
}
