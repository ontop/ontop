SELECT DISTINCT ?wellbore ?wc ?well 
		   WHERE { 
		      	   ?wellbore npdv:wellboreForDiscovery ?discovery;
		      	             npdv:belongsToWell ?well.  
		      	   ?wc npdv:coreForWellbore ?wellbore.
		   }