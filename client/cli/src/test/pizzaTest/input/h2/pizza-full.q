[QueryItem="pizza-AllSensors"]
PREFIX : <http://www.co-ode.org/ontologies/pizza/pizza.owl#>

SELECT ?pizza ?sensor ?temp ?time
WHERE 	{ 
	    ?pizza :hasSensorMeasure [:value ?temp; :capturedAt ?time; :detectedBy ?sensor].
	}

[QueryItem="pizza-AllTemeperatures"]
PREFIX : <http://www.co-ode.org/ontologies/pizza/pizza.owl#>

SELECT ?pizza  ?sensor ?temp ?time
WHERE 	{ 
	    ?pizza :hasSensorMeasure [:value ?temp; :capturedAt ?time; :detectedBy ?sensor].
	    ?sensor a :TemperatureSensor.
	}

[QueryItem="pizza-AllHumidities"]
PREFIX : <http://www.co-ode.org/ontologies/pizza/pizza.owl#>

SELECT ?pizza  ?sensor ?temp ?time
WHERE 	{ 
	    ?pizza :hasSensorMeasure [:value ?temp; :capturedAt ?time; :detectedBy ?sensor].
	    ?sensor a :HumiditySensor.
	}

[QueryItem="S2-SensedBy"]
PREFIX : <http://www.co-ode.org/ontologies/pizza/pizza.owl#>

SELECT ?pizza ?humidity
WHERE 	{ 
	    :S2 a :Sensor; a :HumiditySensor.
	    :S2 :detects [:value ?humidity; :relativeTo ?pizza]. 
	}

[QueryItem="boolean-test"]
PREFIX : <http://www.co-ode.org/ontologies/pizza/pizza.owl#>

SELECT ?sensor ?active
WHERE 	{ 
	    ?sensor a :Sensor; :isEnabled ?active. 
	}

[QueryItem="integer-test"]
PREFIX : <http://www.co-ode.org/ontologies/pizza/pizza.owl#>

SELECT ?pizza ?number
WHERE 	{ 
	    ?pizza a :Pizza; :menuNumber ?number. 
	}
