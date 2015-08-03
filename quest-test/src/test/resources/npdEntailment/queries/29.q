SELECT DISTINCT ?wellbore ?wc ?well ?length
		   WHERE { 
		      	   ?wellbore npdv:wellboreForDiscovery ?discovery;
		      	    		 npdv:belongsToWell ?well.  
		      	    ?wc npdv:coreForWellbore ?wellbore;
		      	        npdv:coresTotalLength ?length;
		      	        npdv:coreIntervalUOM "000001"^^xsd:string . # feets
		      	        	      	        
	FILTER (?length < 56796)		   
}