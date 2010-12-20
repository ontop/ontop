package org.obda.query.domain.imp;

import java.net.URI;
import java.util.HashSet;

import org.obda.query.domain.Predicate;
import org.obda.query.domain.PredicateFactory;

public class BasicPredicateFactoryImpl implements PredicateFactory {

	private HashSet<Predicate> existingPredicates = null;
	private static BasicPredicateFactoryImpl instance = null;
	int maxIdentifier = 0;
	int queryonlyidentifier= -59999;

	private BasicPredicateFactoryImpl(){
		existingPredicates = new HashSet<Predicate>();
	}

	public static BasicPredicateFactoryImpl getInstance(){
		if(instance == null){
			instance = new BasicPredicateFactoryImpl();
		}
		return instance;
	}

	public Predicate getPredicate(URI name, int arity){

		if(!existingPredicates.contains(name)){

			if(arity ==2){
				BinaryPredicateImp binary = new BinaryPredicateImp(name, maxIdentifier);
				maxIdentifier ++;
				String path = name.getPath();
				if(path.equals("http://www.obda.org/ucq/predicate/queryonly#")){
					existingPredicates.add(binary);
				}
				return binary;
			}else{
				PredicateImp predicate = new PredicateImp(name, maxIdentifier, arity);
				maxIdentifier++;
				String path = name.getPath();
				if(path.equals("http://www.obda.org/ucq/predicate/queryonly#")){
					existingPredicates.add(predicate);
				}
				return predicate;
			}
		}

//		checks if a predicate with the same exists already in its register. If it
//		 doesn't exists then create a entry in the register, asign a unique,
//		 incremental integer identifier to the newly created predicate. Note,
//		 we must permit predicates with the same name, and different arity. For example
//		 there can be a predicate Exam with arity 1, and another predicate Exam with
//		 arity 2.
//
//		 Also, if arity = 2, then this method returns a BinaryPredicateImpl object
//		 instead of a PredicateImpl. (using 2 as )
//
//		 Also, if the URI of the predicate has the root:
//		 http://www.obda.org/ucq/predicate/queryonly#
//		 Then this predicate is not registered in the factory's indexes
		return null;
		//TODO
	}

	public Predicate getQueryOnlyPredicate(String name, int arity) {
		URI uri = URI.create("http://www.obda.org/ucq/predicate/queryonly#"+name);
		return new QueryOnlyPredicateImpl(uri, queryonlyidentifier++, arity);
	}
}
