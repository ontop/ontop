package it.unibz.krdb.obda.utils;

/*
 * #%L
 * ontop-test
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import junit.framework.TestCase;

public class LookupTableTest extends TestCase {
	
	private LookupTable lookupTable = new LookupTable();
	
	public void setUp() {
		lookupTable.add("Employee.id", 1);
		lookupTable.add("Employee.name", 2);
		lookupTable.add("public.Salary.id", 3);
		lookupTable.add("Salary.id", 3);
		lookupTable.add("id", 3);
		lookupTable.add("public.Salary.amount", 4);
		lookupTable.add("Salary.amount", 4);
		lookupTable.add("amount", 4);
		lookupTable.add("public.Salary.pid", 5);
		lookupTable.add("Salary.pid", 5);
		lookupTable.add("pid", 5);
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
		lookupTable.add("public.Position.tid", 6);
		lookupTable.add("Position.tid", 6);
		lookupTable.add("tid", 6);
		
		String altName = lookupTable.lookup("Salary.id");
		assertEquals(altName, "t3");
		
		altName = lookupTable.lookup("tid");  // i.e., Salary.id
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
		assertEquals(name, null);
		
		lookupTable.remove("public.Salary.id");
		name = lookupTable.lookup("Salary.id");
		assertEquals(name, "t3");
		
		lookupTable.remove(new String[] {"public.Salary.pid", "Salary.pid", "pid"});
		System.out.println("After deletions:");
		System.out.println(lookupTable.toString());
	}
	
	public void testAddingWithReference() {
		lookupTable.add("security_id", "Employee.id");
		String name = lookupTable.lookup("security_id");
		assertEquals(name, "t1");
		
		lookupTable.add("monthly_salary", "public.Salary.amount");
		name = lookupTable.lookup("monthly_salary");
		assertEquals(name, "t4");
	}
}
