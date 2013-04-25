[QueryItem="Q1"]
PREFIX : <http://ksg.meraka.co.za/adolena.owl#>

SELECT ?x WHERE {
?x a :Device.
?x :assistsWith ?y.
}

[QueryItem="Q2"]
PREFIX : <http://ksg.meraka.co.za/adolena.owl#>

SELECT ?x WHERE {
?x a :Device.
?x :assistsWith ?y.
?y a :UpperLimbMobility.
}

[QueryItem="Q3"]
PREFIX : <http://ksg.meraka.co.za/adolena.owl#>
SELECT ?x WHERE {
?x a :Device.
?x :assistsWith ?y.
?y a :Hear.
?z :affects ?y.
?z a :Autism.
}

[QueryItem="Q4"]
PREFIX : <http://ksg.meraka.co.za/adolena.owl#>
SELECT ?x WHERE {
?x a :Device.
?x :assistsWith ?y.
?y a :PhysicalAbility. 
}

[QueryItem="Q5"]
PREFIX : <http://ksg.meraka.co.za/adolena.owl#>
SELECT ?x WHERE {
?x a :Device.
?x :assistsWith ?y.
?y a :PhysicalAbility. 
?z :affects ?y. 
?z a :Quadriplegia.
}