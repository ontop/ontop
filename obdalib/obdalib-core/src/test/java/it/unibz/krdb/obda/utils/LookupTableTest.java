package it.unibz.krdb.obda.utils;

import junit.framework.TestCase;

public class LookupTableTest extends TestCase {
	
	private LookupTable lookupTable = new LookupTable();
	
	public void setUp() {
		lookupTable.add("Employee.id");
		lookupTable.add("Employee.name");
		lookupTable.add(new String[] {"public.Salary.id", "Salary.id", "id"});
		lookupTable.add(new String[] {"public.Salary.amount", "Salary.amount", "amount"});
		lookupTable.add(new String[] {"public.Salary.pid", "Salary.pid", "pid"});
	}
	
	public void testPrintTable() {
		System.out.println(lookupTable.toString());
	}
	
	public void testAlternativeName() {
		String altName = lookupTable.lookup("Employee.name");
		assertEquals(altName, "t2");
		
		altName = lookupTable.lookup("public.Salary.amount");
		assertEquals(altName, "t4");
		
		altName = lookupTable.lookup("Salary.amount");
		assertEquals(altName, "t4");
		
		altName = lookupTable.lookup("amount");
		assertEquals(altName, "t4");
		
		altName = lookupTable.lookup("Salary.pid");
		assertEquals(altName, "t5");
	}
	
	public void testEntryOverride() {
		lookupTable.add(new String[] {"public.Position.id", "Position.id", "id"});
		
		String altName = lookupTable.lookup("Salary.id");
		assertEquals(altName, "t3");
		
		altName = lookupTable.lookup("id");  // i.e., Salary.id
		assertEquals(altName, "t6");
	}
	
	public void testRenaming() {
		boolean flag = lookupTable.asEqualTo("public.Salary.id", "public.Position.id");
		assertFalse(flag);
		
		flag = lookupTable.asEqualTo("Salary.pid", "Employee.id");
		assertTrue(flag);
		
		String altName = lookupTable.lookup("Salary.pid");
		String altName2 = lookupTable.lookup("Employee.id");
		assertEquals(altName, altName2);
	}
	
	public void testEntryDeleting() {
		System.out.println("Before deletions:");
		System.out.println(lookupTable.toString());
		
		lookupTable.remove("Employee.id");
		String name = lookupTable.lookup("Employee.id");
		assertEquals(name, "");
		
		lookupTable.remove("public.Salary.id");
		name = lookupTable.lookup("Salary.id");
		assertEquals(name, "t3");
		
		lookupTable.remove(new String[] {"public.Salary.pid", "Salary.pid", "pid"});
		System.out.println("After deletions:");
		System.out.println(lookupTable.toString());
	}
}
