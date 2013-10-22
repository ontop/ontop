[QueryItem="commonname"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX : <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?type ?species ?genus ?country
WHERE {
   ?nameID :comnames_ComName "Aal"^^xsd:string .
   ?nameID :comnames_NameType ?type .
   ?nameID :comnames_SpecCode ?code .
   ?nameID :comnames_C_Code ?ccode .
   ?code :species_Species ?species .
   ?code :species_Genus ?genus .
   ?ccode :countref_PAESE ?country .
}

[QueryItem="speciespage"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX : <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?common ?code ?refno ?author ?demerspelag ?anacat
               ?family ?order ?class ?entered ?pic ?picid ?description 
               ?refauthor ?refyear ?collaborator  ?comments
WHERE {
   ?x :species_Genus "Aboma"^^xsd:string .
   ?x :species_SpecCode ?code.
   ?x :species_Species "etheostoma"^^xsd:string .
   ?x :species_Comments ?comments .
   OPTIONAL {?x :species_Author ?author}.
   OPTIONAL {?x :species_FBname ?common}.
   OPTIONAL {?x :species_SpeciesRefNo ?refno}.
   OPTIONAL {?ref :refrens_RefNo ?refno}.
   OPTIONAL {?ref :refrens_Author ?refauthor}.
   OPTIONAL {?ref :refrens_Year ?refyear}.
   OPTIONAL {?x :species_Comments ?biology.}
   OPTIONAL {
      ?x :species_FamCode ?famcode.
      ?famcode :families_Family ?family.
      ?famcode :families_Order ?order.
      ?famcode :families_Class ?class.
   }
   OPTIONAL {
      ?morph :morphdat_Speccode ?x.
      ?morph :morphdat_AddChars ?description.
   }
   OPTIONAL {?x :species_DemersPelag ?demerspelag.}
   OPTIONAL {?x :species_AnaCat ?anacat.}
   OPTIONAL {
      ?x :species_PicPreferredName ?pic.
      ?pic_node :picturesmain_SpecCode ?x.
      ?pic_node :picturesmain_PicName ?pic.
      ?pic_node :picturesmain_autoctr ?picid.
      ?pic_node :picturesmain_Entered ?entered.
      ?pic_node :picturesmain_AuthName ?collaborator.
   }
}

[QueryItem="genus"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?species ?author ?family 
WHERE {
   ?code fd:species_Species ?species .
   ?code fd:species_Genus "Acanthicus"^^xsd:string .
   ?code fd:species_Author ?author .
   ?code fd:species_FamCode ?fcode .
   ?fcode fd:families_Family ?family .
}

[QueryItem="species"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/> 

SELECT ?genus ?author ?family
WHERE {  
   ?code fd:species_Species "adonis"^^xsd:string .
   ?code fd:species_Genus ?genus .
   ?code fd:species_Author ?author .
   ?code fd:species_FamCode ?fcode .
   ?fcode fd:families_Family ?family .
}

[QueryItem="familyinformation"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?noOfGenera ?noOfSpecies ?marine ?brackish ?freshwater 
               ?aquarium ?remark  ?division ?activityLevel ?author ?year  ?repguild  ?SpeciesCount
WHERE {
   ?familiesID fd:families_Family "Bothidae"^^xsd:string .
   ?familiesID fd:families_SpeciesCount ?SpeciesCount .
   ?familiesID fd:families_Genera ?noOfGenera .
   ?familiesID fd:families_Species ?noOfSpecies .
   ?familiesID fd:families_Marine ?marine .
   ?familiesID fd:families_Brackish ?brackish .
   ?familiesID fd:families_Freshwater ?freshwater .
   ?familiesID fd:families_Aquarium ?aquarium .
   ?familiesID fd:families_Remark ?remark .
   ?familiesID fd:families_Division ?division .
   ?familiesID fd:families_Activity ?activityLevel .
   ?familiesID fd:families_ReprGuild ?repguild .
   ?familiesID fd:families_FamiliesRefNo ?code .
   ?x fd:refrens_RefNo ?code .
   ?x fd:refrens_Author  ?author .
   ?x fd:refrens_Year ?year .
}

[QueryItem="familyallfish"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?species ?genus ?author 
WHERE {
   ?SpeciesID fd:species_Author ?author ;
   fd:species_Species ?species;
   fd:species_Genus ?genus ;
   fd:species_FamCode ?code .
   ?code  fd:families_Family "Bothidae"^^xsd:string .
}

[QueryItem="familynominalspecies"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?species ?author ?genus 
WHERE { 
   ?SpeciesID fd:species_Author ?author ;
   fd:species_Species ?species;
   fd:species_Genus ?genus ;
   fd:species_FamCode ?code .
   ?code  fd:families_Family "Bothidae"^^xsd:string . 
}

[QueryItem="familylistofpictures"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?genus ?species ?picture ?photographer
WHERE {
   ?picID fd:picturesmain_SpecCode ?code ;
   fd:picturesmain_PicName ?picture ; 
   fd:picturesmain_AuthName ?photographer .
   ?code fd:species_Species ?species;
   fd:species_Genus ?genus ;
   fd:species_FamCode ?fcode .
   ?fcode fd:families_Family "Bothidae"^^xsd:string .
}

[QueryItem="collaboratorspage"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?prename ?surname  ?email ?webpage ?fax ?institute ?street ?city ?country ?comments ?keywords ?year
WHERE {
   ?x fd:collaborators_Personnel "333"^^xsd:int .
   OPTIONAL {?x fd:collaborators_Prename ?prename }.
   OPTIONAL {?x fd:collaborators_Surname ?surname }.
   OPTIONAL {?x fd:collaborators_E-mail ?email }.
   OPTIONAL {?x fd:collaborators_StaffPhoto ?photo }.
   OPTIONAL {?x fd:collaborators_WebPage ?webpage }.
   OPTIONAL {?x fd:collaborators_FAX ?fax }.
   OPTIONAL {?x fd:collaborators_Institute ?institute }.
   OPTIONAL {?x fd:collaborators_Street ?street }.
   OPTIONAL {?x fd:collaborators_City ?city }.
   OPTIONAL {?x fd:collaborators_Country ?country }.
   OPTIONAL {?x fd:collaborators_Comments ?comments }.
   OPTIONAL {?x fd:collaborators_Keywords ?keywords }.
   OPTIONAL {?x fd:collaborators_Year ?year }.
}

[QueryItem="picturepage"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?genus ?species ?photographer ?stage  
WHERE {
   ?pcode fd:picturesmain_PicName "Abriv_u4.jpg"^^xsd:string .
   ?pcode fd:picturesmain_AuthName ?photographer .
   ?pcode fd:picturesmain_LifeStage ?stage .
   ?pcode fd:picturesmain_SpecCode ?scode .
   ?scode fd:species_Genus ?genus .
   ?scode fd:species_Species ?species .
}

[QueryItem="countryallfish"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?order ?family ?genus ?species ?occurrence 
WHERE { 
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status ?occurrence .
   ?c fd:country_C_Code ?cf .
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_FamCode ?f .
   ?f fd:families_Family  ?family .
   ?f fd:families_Order ?order .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .  
}

[QueryItem="countryspeciesinformation"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT  ?freshwater ?marine ?brackish ?occurrence ?factbook
WHERE {
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Freshwater ?freshwater .
   ?c fd:country_Brackish ?brackish .
   ?c fd:country_Saltwater ?marine .
   ?c fd:country_Status ?occurrence .
   ?c fd:country_C_Code ?cf .
   ?x fd:species_Genus "Oryzias"^^xsd:string .
   ?x fd:species_Species "javanicus"^^xsd:string .
   ?x fd:species_FamCode ?f .
   ?f fd:families_Family  ?family .
   ?f fd:families_Order ?order .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .
   ?cf fd:countref_Factbook ?factbook .
}

[QueryItem="countryfreshwater"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?order ?family ?genus ?species ?occurrence ?name 
WHERE {
   ?nameID fd:comnames_ComName ?name .
   ?nameID fd:comnames_C_Code ?ccode . 
   ?nameID fd:comnames_SpecCode ?x.
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_FamCode ?f .
   ?f fd:families_Family  ?family .
   ?f fd:families_Order ?order .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status ?occurrence .
   ?c fd:country_Freshwater 1 .
   ?c fd:country_C_Code ?cf .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .     
}

[QueryItem="countryintroduced"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?order ?family ?genus ?species ?name 
WHERE {
   ?nameID fd:comnames_ComName ?name .
   ?nameID fd:comnames_C_Code ?ccode . 
   ?nameID fd:comnames_SpecCode ?x.
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_FamCode ?f .
   ?f fd:families_Family  ?family .
   ?f fd:families_Order ?order .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status "introduced"^^xsd:string .
   ?c fd:country_C_Code ?cf .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .   
}

[QueryItem="countryendemic"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?order ?family ?genus ?species ?name 
WHERE {
   ?nameID fd:comnames_ComName ?name .
   ?nameID fd:comnames_C_Code ?ccode . 
   ?nameID fd:comnames_SpecCode ?x.
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_FamCode ?f .
   ?f fd:families_Family  ?family .
   ?f fd:families_Order ?order .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status "endemic"^^xsd:string .
   ?c fd:country_C_Code ?cf .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .     
}

[QueryItem="countryreefassociated"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?order ?family ?genus ?species ?occurrence ?name ?dangerous 
WHERE {
   ?nameID fd:comnames_ComName ?name .
   ?nameID fd:comnames_C_Code ?ccode . 
   ?nameID fd:comnames_SpecCode ?x.
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_Dangerous ?dangerous .
   ?x fd:species_DemersPelag "reef-associated"^^xsd:string .
   ?x fd:species_FamCode ?f .
   ?f fd:families_Family  ?family .
   ?f  fd:families_Order ?order .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status ?occurrence .
   ?c fd:country_C_Code ?cf .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .   
}

[QueryItem="countrypelagic"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?order ?family ?genus ?species ?occurrence ?name ?dangerous 
WHERE {
   ?nameID fd:comnames_ComName ?name .
   ?nameID fd:comnames_C_Code ?ccode . 
   ?nameID fd:comnames_SpecCode ?x.
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_Dangerous ?dangerous .
   ?x fd:species_DemersPelag "pelagic"^^xsd:string .
   ?x fd:species_FamCode ?f .
   ?f fd:families_Family  ?family .
   ?f  fd:families_Order ?order .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status ?occurrence .
   ?c fd:country_C_Code ?cf .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .  
}

[QueryItem="countrygamefish"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?order ?family ?genus ?species ?occurrence ?name ?gameref ?game 
WHERE {
   ?nameID fd:comnames_ComName ?name .
   ?nameID fd:comnames_C_Code ?ccode . 
   ?nameID fd:comnames_SpecCode ?x.
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_GameFish ?game .
   ?x fd:species_GameRef ?gameref .
   ?x fd:species_FamCode ?f .
   ?f fd:families_Family  ?family .
   ?f  fd:families_Order ?order .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status ?occurrence .
   ?c fd:country_C_Code ?cf .
   ?c fd:country_Game 1 .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .  
}

[QueryItem="countrycommercial"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?order ?family ?genus ?species ?occurrence ?name 
WHERE {
   ?nameID fd:comnames_ComName ?name .
   ?nameID fd:comnames_C_Code ?ccode . 
   ?nameID fd:comnames_SpecCode ?x.
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_FamCode ?f .
   ?f fd:families_Family  ?family .
   ?f  fd:families_Order ?order .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status ?occurrence .
   ?c fd:country_C_Code ?cf .
   ?c fd:country_Importance "minor commercial"^^xsd:string .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .  
}

[QueryItem="countryusedforaquaculture"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?genus ?species ?occurrence ?speciesAquaculture 
WHERE {
   ?nameID fd:comnames_ComName ?name .
   ?nameID fd:comnames_C_Code ?ccode . 
   ?nameID fd:comnames_SpecCode ?x.
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_UsedforAquaculture ?speciesAquaculture .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status ?occurrence .
   ?c fd:country_C_Code ?cf .
   ?c fd:country_Aquaculture "commercial"^^xsd:string .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .  
}

[QueryItem="countrypotentialuseforaquaculture"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?genus ?species ?occurrence ?countryAquaculture  
WHERE {
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_UsedforAquaculture "commercial"^^xsd:string .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status ?occurrence .
   ?c fd:country_C_Code ?cf .
   ?c fd:country_Aquaculture ?countryAquaculture .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .  
}

[QueryItem="countryuseforaquariumtrade"]
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fd: <http://fishdelish.cs.man.ac.uk/rdf/vocab/resource/>

SELECT ?genus ?species ?occurrence ?aquarium 
WHERE {
   ?x fd:species_Genus ?genus .
   ?x fd:species_Species ?species .
   ?x fd:species_Aquarium ?aquarium .
   ?c fd:country_SpecCode ?x.
   ?c fd:country_Status ?occurrence .
   ?c fd:country_LiveExport "ornamental"^^xsd:string .
   ?c fd:country_C_Code ?cf .
   ?cf fd:countref_PAESE "Indonesia"^^xsd:string .  
}
