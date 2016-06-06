package it.unibz.inf.ontop.planning.utils.combinators_test;

import static org.junit.Assert.*;
import it.unibz.inf.ontop.planning.utils.combinations.CombinationVisitor;
import it.unibz.inf.ontop.planning.utils.combinations.Combinator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class CombinatorTest {
    
    List<String> f1 = new ArrayList<>();
    List<String> f2 = new ArrayList<>();
    List<String> f3 = new ArrayList<>();
    
    List<List<String>> fragments = new ArrayList<>();
    
    String expected = "[a, d, f][a, d, g][a, d, h][a, e, f][a, e, g][a, e, h][b, d, f][b, d, g]" +
	    "[b, d, h][b, e, f][b, e, g][b, e, h][c, d, f][c, d, g][c, d, h][c, e, f][c, e, g][c, e, h]";
    
    @Before
    public void setUp() throws Exception {
	f1.add("a");
	f1.add("b");
	f1.add("c");
	f2.add("d");
	f2.add("e");
	f3.add("f");
	f3.add("g");
	f3.add("h");
	
	fragments.add(f1); fragments.add(f2); fragments.add(f3);
    }

    @Test
    public void test() {
	
	class SysoutVisitor implements CombinationVisitor<String>{
	    StringBuilder result;
	    public SysoutVisitor(StringBuilder stringBuilder) {
		this.result = stringBuilder;
	    }
	    @Override
	    public void visit(List<String> combination) {
		System.out.println(combination);
		
		result.append(combination);
		
		
	    }
	    
	}
	
	StringBuilder statesTracker = new StringBuilder();
	SysoutVisitor visitor = new SysoutVisitor(statesTracker);
	
	Combinator<String> comb = new Combinator<>(fragments, visitor);
	
	comb.combine();
	
	assertEquals(expected, statesTracker.toString());
    }
}
