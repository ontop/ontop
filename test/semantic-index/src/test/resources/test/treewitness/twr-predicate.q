[QueryItem="Q1"]

SELECT DISTINCT ?prop WHERE 
{ 
	?s a <http://www.my_vrdf_data.com/vocab/class/SampleClass>. 
	?s ?prop ?x. 
	FILTER (?prop != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)
}
