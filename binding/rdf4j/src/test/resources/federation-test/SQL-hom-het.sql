------SPARQL Q1------------------------------------------------------------------------------------------------------------------
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT DISTINCT ?product ?label
WHERE {
 ?product rdfs:label ?label .
 ?product a bsbm:Product .
 ?product bsbm:productFeature bsbm-inst:ProductFeature89 .
 ?product bsbm:productFeature bsbm-inst:ProductFeature91 .
 ?product bsbm:productPropertyNumeric1 ?value1 .
 FILTER (?value1 < 1000)
}


generated IQ tree:
CONSTRUCT [product, label] [product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product0m2)),IRI), label/RDF(VARCHARToTEXT(label10m46),xsd:string)]
   DISTINCT
      CONSTRUCT [product0m2, label10m46] []
         JOIN NUM_LT(INTEGERToBIGINT(propertynum1m41),"1000"^^BIGINT)
            UNION [label10m46, product0m2]
               FILTER IS_NOT_NULL(label10m46)
                  EXTENSIONAL "product1"(0:product0m2,1:label10m46)
               FILTER IS_NOT_NULL(label10m46)
                  EXTENSIONAL "product2"(0:product0m2,1:label10m46)
            UNION [product0m2]
               EXTENSIONAL "productfeatureproduct1"(0:product0m2,1:"89"^^INTEGER)
               EXTENSIONAL "productfeatureproduct2"(0:product0m2,1:"89"^^INTEGER)
            UNION [product0m2]
               EXTENSIONAL "productfeatureproduct1"(0:product0m2,1:"91"^^INTEGER)
               EXTENSIONAL "productfeatureproduct2"(0:product0m2,1:"91"^^INTEGER)
            UNION [product0m2, propertynum1m41]
               FILTER IS_NOT_NULL(propertynum1m41)
                  EXTENSIONAL "product1"(0:product0m2,4:propertynum1m41)
               FILTER IS_NOT_NULL(propertynum1m41)
                  EXTENSIONAL "product2"(0:product0m2,4:propertynum1m41)


computed cost: [12, 0]
new generated tree
CONSTRUCT [product, label] [product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product0m2)),IRI), label/RDF(VARCHARToTEXT(label10m46),xsd:string)]
   DISTINCT
      CONSTRUCT [product0m2, label10m46] []
         UNION [product0m2, label10m46, propertynum1m41]
            JOIN NUM_LT(INTEGERToBIGINT(propertynum1m41),"1000"^^BIGINT)
               FILTER IS_NOT_NULL(label10m46)
                  EXTENSIONAL "product1"(0:product0m2,1:label10m46)
               EXTENSIONAL "productfeatureproduct1"(0:product0m2,1:"89"^^INTEGER)
               EXTENSIONAL "productfeatureproduct1"(0:product0m2,1:"91"^^INTEGER)
               FILTER IS_NOT_NULL(propertynum1m41)
                  EXTENSIONAL "product1"(0:product0m2,4:propertynum1m41)
            JOIN NUM_LT(INTEGERToBIGINT(propertynum1m41),"1000"^^BIGINT)
               FILTER IS_NOT_NULL(label10m46)
                  EXTENSIONAL "product2"(0:product0m2,1:label10m46)
               EXTENSIONAL "productfeatureproduct2"(0:product0m2,1:"89"^^INTEGER)
               EXTENSIONAL "productfeatureproduct2"(0:product0m2,1:"91"^^INTEGER)
               FILTER IS_NOT_NULL(propertynum1m41)
                  EXTENSIONAL "product2"(0:product0m2,4:propertynum1m41)

computed cost: 0




------SPARQL Q2------------------------------------------------------------------------------------------------------------------
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?label ?comment ?producer ?productFeature ?propertyTextual1 ?propertyTextual2 ?propertyTextual3
 ?propertyNumeric1 ?propertyNumeric2 ?propertyTextual4 ?propertyTextual5 ?propertyNumeric4
WHERE {
    ?product bsbm:productId ?id .
    FILTER (?id < 1000 )
    ?product rdfs:label ?label .
	?product rdfs:comment ?comment .
	?product bsbm:producer ?p .
	?p rdfs:label ?producer .
    ?product dc:publisher ?p .
	?product bsbm:productFeature ?f .
	?f rdfs:label ?productFeature .
	?product bsbm:productPropertyTextual1 ?propertyTextual1 .
	?product bsbm:productPropertyTextual2 ?propertyTextual2 .
    ?product bsbm:productPropertyTextual3 ?propertyTextual3 .
	?product bsbm:productPropertyNumeric1 ?propertyNumeric1 .
	?product bsbm:productPropertyNumeric2 ?propertyNumeric2 .
	OPTIONAL { ?product bsbm:productPropertyTextual4 ?propertyTextual4 }
    OPTIONAL { ?product bsbm:productPropertyTextual5 ?propertyTextual5 }
    OPTIONAL { ?product bsbm:productPropertyNumeric4 ?propertyNumeric4 }
}



generated IQ tree:
CONSTRUCT [label, comment, producer, productFeature, propertyTextual1, propertyTextual2, propertyTextual3, propertyNumeric1, propertyNumeric2, propertyTextual4, propertyTextual5, propertyNumeric4] [label/RDF(VARCHARToTEXT(label10m46),xsd:string), productFeature/RDF(VARCHARToTEXT(label10m16),xsd:string), propertyNumeric2/RDF(INTEGERToTEXT(propertynum1m40),xsd:integer), propertyNumeric1/RDF(INTEGERToTEXT(propertynum1m41),xsd:integer), propertyNumeric4/RDF(INTEGERToTEXT(propertynum1m25),IF_ELSE_NULL(IS_NOT_NULL(propertynum1m25),xsd:integer)), producer/RDF(VARCHARToTEXT(label10m5),xsd:string), comment/RDF(VARCHARToTEXT(comment10m20),xsd:string), propertyTextual1/RDF(VARCHARToTEXT(propertytex1m34),xsd:string), propertyTextual2/RDF(VARCHARToTEXT(propertytex1m33),xsd:string), propertyTextual3/RDF(VARCHARToTEXT(propertytex1m32),xsd:string), propertyTextual4/RDF(VARCHARToTEXT(propertytex1m31),IF_ELSE_NULL(IS_NOT_NULL(propertytex1m31),xsd:string)), propertyTextual5/RDF(VARCHARToTEXT(propertytex1m30),IF_ELSE_NULL(IS_NOT_NULL(propertytex1m30),xsd:string))]
   DISTINCT
      CONSTRUCT [nr2m23, label10m46, comment10m20, producer2m9, label10m5, productfeature2m2, label10m16, propertytex1m34, propertytex1m33, propertytex1m32, propertynum1m41, propertynum1m40, nr0m31, propertytex1m31, nr0m30, propertytex1m30, nr0m25, propertynum1m25] [nr0m25/IF_ELSE_NULL(IS_NOT_NULL(propertynum1m25),nr2m23), nr0m30/IF_ELSE_NULL(IS_NOT_NULL(propertytex1m30),nr2m23), nr0m31/IF_ELSE_NULL(IS_NOT_NULL(propertytex1m31),nr2m23)]
         FILTER AND3(NUM_LT(INTEGERToBIGINT(nr2m23),"1000"^^BIGINT),IS_NOT_NULL(label10m5),IS_NOT_NULL(label10m16))
            LJ
               LJ
                  LJ
                     JOIN
                        UNION [nr2m23]
                           EXTENSIONAL "product1"(0:nr2m23)
                           EXTENSIONAL "product2"(0:nr2m23)
                        UNION [label10m46, nr2m23]
                           FILTER IS_NOT_NULL(label10m46)
                              EXTENSIONAL "product1"(0:nr2m23,1:label10m46)
                           FILTER IS_NOT_NULL(label10m46)
                              EXTENSIONAL "product2"(0:nr2m23,1:label10m46)
                        UNION [comment10m20, nr2m23]
                           FILTER IS_NOT_NULL(comment10m20)
                              EXTENSIONAL "product1"(0:nr2m23,2:comment10m20)
                           FILTER IS_NOT_NULL(comment10m20)
                              EXTENSIONAL "product2"(0:nr2m23,2:comment10m20)
                        UNION [nr2m23, producer2m9, label10m5]
                           JOIN
                              EXTENSIONAL "product1"(0:nr2m23,3:producer2m9)
                              EXTENSIONAL "producer"(0:producer2m9,1:label10m5)
                           JOIN
                              EXTENSIONAL "product2"(0:nr2m23,3:producer2m9)
                              EXTENSIONAL "producer"(0:producer2m9,1:label10m5)
                        UNION [nr2m23, producer2m9]
                           FILTER IS_NOT_NULL(producer2m9)
                              EXTENSIONAL "product1"(0:nr2m23,3:producer2m9)
                           FILTER IS_NOT_NULL(producer2m9)
                              EXTENSIONAL "product2"(0:nr2m23,3:producer2m9)
                        UNION [nr2m23, productfeature2m2, label10m16]
                           JOIN
                              EXTENSIONAL "productfeatureproduct1"(0:nr2m23,1:productfeature2m2)
                              EXTENSIONAL "productfeature"(0:productfeature2m2,1:label10m16)
                           JOIN
                              EXTENSIONAL "productfeatureproduct2"(0:nr2m23,1:productfeature2m2)
                              EXTENSIONAL "productfeature"(0:productfeature2m2,1:label10m16)
                        UNION [nr2m23, propertytex1m34]
                           FILTER IS_NOT_NULL(propertytex1m34)
                              EXTENSIONAL "product1"(0:nr2m23,10:propertytex1m34)
                           FILTER IS_NOT_NULL(propertytex1m34)
                              EXTENSIONAL "product2"(0:nr2m23,10:propertytex1m34)
                        UNION [nr2m23, propertytex1m33]
                           FILTER IS_NOT_NULL(propertytex1m33)
                              EXTENSIONAL "product1"(0:nr2m23,11:propertytex1m33)
                           FILTER IS_NOT_NULL(propertytex1m33)
                              EXTENSIONAL "product2"(0:nr2m23,11:propertytex1m33)
                        UNION [nr2m23, propertytex1m32]
                           FILTER IS_NOT_NULL(propertytex1m32)
                              EXTENSIONAL "product1"(0:nr2m23,12:propertytex1m32)
                           FILTER IS_NOT_NULL(propertytex1m32)
                              EXTENSIONAL "product2"(0:nr2m23,12:propertytex1m32)
                        UNION [nr2m23, propertynum1m41]
                           FILTER IS_NOT_NULL(propertynum1m41)
                              EXTENSIONAL "product1"(0:nr2m23,4:propertynum1m41)
                           FILTER IS_NOT_NULL(propertynum1m41)
                              EXTENSIONAL "product2"(0:nr2m23,4:propertynum1m41)
                        UNION [nr2m23, propertynum1m40]
                           FILTER IS_NOT_NULL(propertynum1m40)
                              EXTENSIONAL "product1"(0:nr2m23,5:propertynum1m40)
                           FILTER IS_NOT_NULL(propertynum1m40)
                              EXTENSIONAL "product2"(0:nr2m23,5:propertynum1m40)
                     UNION [nr2m23, propertytex1m31]
                        FILTER IS_NOT_NULL(propertytex1m31)
                           EXTENSIONAL "product1"(0:nr2m23,13:propertytex1m31)
                        FILTER IS_NOT_NULL(propertytex1m31)
                           EXTENSIONAL "product2"(0:nr2m23,13:propertytex1m31)
                  UNION [nr2m23, propertytex1m30]
                     FILTER IS_NOT_NULL(propertytex1m30)
                        EXTENSIONAL "product1"(0:nr2m23,14:propertytex1m30)
                     FILTER IS_NOT_NULL(propertytex1m30)
                        EXTENSIONAL "product2"(0:nr2m23,14:propertytex1m30)
               UNION [nr2m23, propertynum1m25]
                  FILTER IS_NOT_NULL(propertynum1m25)
                     EXTENSIONAL "product1"(0:nr2m23,7:propertynum1m25)
                  FILTER IS_NOT_NULL(propertynum1m25)
                     EXTENSIONAL "product2"(0:nr2m23,7:propertynum1m25)

computed cost: [57, 0]
new generated tree
CONSTRUCT [label, comment, producer, productFeature, propertyTextual1, propertyTextual2, propertyTextual3, propertyNumeric1, propertyNumeric2, propertyTextual4, propertyTextual5, propertyNumeric4] [label/RDF(VARCHARToTEXT(label10m46),xsd:string), productFeature/RDF(VARCHARToTEXT(label10m16),xsd:string), propertyNumeric2/RDF(INTEGERToTEXT(propertynum1m40),xsd:integer), propertyNumeric1/RDF(INTEGERToTEXT(propertynum1m41),xsd:integer), propertyNumeric4/RDF(INTEGERToTEXT(propertynum1m25),IF_ELSE_NULL(IS_NOT_NULL(propertynum1m25),xsd:integer)), producer/RDF(VARCHARToTEXT(label10m5),xsd:string), comment/RDF(VARCHARToTEXT(comment10m20),xsd:string), propertyTextual1/RDF(VARCHARToTEXT(propertytex1m34),xsd:string), propertyTextual2/RDF(VARCHARToTEXT(propertytex1m33),xsd:string), propertyTextual3/RDF(VARCHARToTEXT(propertytex1m32),xsd:string), propertyTextual4/RDF(VARCHARToTEXT(propertytex1m31),IF_ELSE_NULL(IS_NOT_NULL(propertytex1m31),xsd:string)), propertyTextual5/RDF(VARCHARToTEXT(propertytex1m30),IF_ELSE_NULL(IS_NOT_NULL(propertytex1m30),xsd:string))]
   DISTINCT
      CONSTRUCT [nr2m23, label10m46, comment10m20, producer2m9, label10m5, productfeature2m2, label10m16, propertytex1m34, propertytex1m33, propertytex1m32, propertynum1m41, propertynum1m40, nr0m31, propertytex1m31, nr0m30, propertytex1m30, nr0m25, propertynum1m25] [nr0m25/IF_ELSE_NULL(IS_NOT_NULL(propertynum1m25),nr2m23), nr0m30/IF_ELSE_NULL(IS_NOT_NULL(propertytex1m30),nr2m23), nr0m31/IF_ELSE_NULL(IS_NOT_NULL(propertytex1m31),nr2m23)]
         FILTER AND3(NUM_LT(INTEGERToBIGINT(nr2m23),"1000"^^BIGINT),IS_NOT_NULL(label10m5),IS_NOT_NULL(label10m16))
            UNION [propertytex1m30, propertytex1m32, propertytex1m31, propertytex1m34, propertynum1m25, propertytex1m33, comment10m20, label10m5, nr2m23, productfeature2m2, label10m16, label10m46, producer2m9, propertynum1m41, propertynum1m40]
               JOIN
                  EXTENSIONAL "product1"(0:nr2m23)
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product1"(0:nr2m23,1:label10m46)
                  FILTER IS_NOT_NULL(comment10m20)
                     EXTENSIONAL "product1"(0:nr2m23,2:comment10m20)
                  EXTENSIONAL "product1"(0:nr2m23,3:producer2m9)
                  EXTENSIONAL "producer"(0:producer2m9,1:label10m5)
                  FILTER IS_NOT_NULL(producer2m9)
                     EXTENSIONAL "product1"(0:nr2m23,3:producer2m9)
                  EXTENSIONAL "productfeatureproduct1"(0:nr2m23,1:productfeature2m2)
                  EXTENSIONAL "productfeature"(0:productfeature2m2,1:label10m16)
                  FILTER IS_NOT_NULL(propertytex1m34)
                     EXTENSIONAL "product1"(0:nr2m23,10:propertytex1m34)
                  FILTER IS_NOT_NULL(propertytex1m33)
                     EXTENSIONAL "product1"(0:nr2m23,11:propertytex1m33)
                  FILTER IS_NOT_NULL(propertytex1m32)
                     EXTENSIONAL "product1"(0:nr2m23,12:propertytex1m32)
                  FILTER IS_NOT_NULL(propertynum1m41)
                     EXTENSIONAL "product1"(0:nr2m23,4:propertynum1m41)
                  FILTER IS_NOT_NULL(propertynum1m40)
                     EXTENSIONAL "product1"(0:nr2m23,5:propertynum1m40)
                  FILTER IS_NOT_NULL(propertytex1m31)
                     EXTENSIONAL "product1"(0:nr2m23,13:propertytex1m31)
                  FILTER IS_NOT_NULL(propertytex1m30)
                     EXTENSIONAL "product1"(0:nr2m23,14:propertytex1m30)
                  FILTER IS_NOT_NULL(propertynum1m25)
                     EXTENSIONAL "product1"(0:nr2m23,7:propertynum1m25)
               JOIN
                  EXTENSIONAL "product2"(0:nr2m23)
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product2"(0:nr2m23,1:label10m46)
                  FILTER IS_NOT_NULL(comment10m20)
                     EXTENSIONAL "product2"(0:nr2m23,2:comment10m20)
                  EXTENSIONAL "product2"(0:nr2m23,3:producer2m9)
                  EXTENSIONAL "producer"(0:producer2m9,1:label10m5)
                  FILTER IS_NOT_NULL(producer2m9)
                     EXTENSIONAL "product2"(0:nr2m23,3:producer2m9)
                  EXTENSIONAL "productfeatureproduct2"(0:nr2m23,1:productfeature2m2)
                  EXTENSIONAL "productfeature"(0:productfeature2m2,1:label10m16)
                  FILTER IS_NOT_NULL(propertytex1m34)
                     EXTENSIONAL "product2"(0:nr2m23,10:propertytex1m34)
                  FILTER IS_NOT_NULL(propertytex1m33)
                     EXTENSIONAL "product2"(0:nr2m23,11:propertytex1m33)
                  FILTER IS_NOT_NULL(propertytex1m32)
                     EXTENSIONAL "product2"(0:nr2m23,12:propertytex1m32)
                  FILTER IS_NOT_NULL(propertynum1m41)
                     EXTENSIONAL "product2"(0:nr2m23,4:propertynum1m41)
                  FILTER IS_NOT_NULL(propertynum1m40)
                     EXTENSIONAL "product2"(0:nr2m23,5:propertynum1m40)
                  FILTER IS_NOT_NULL(propertytex1m31)
                     EXTENSIONAL "product2"(0:nr2m23,13:propertytex1m31)
                  FILTER IS_NOT_NULL(propertytex1m30)
                     EXTENSIONAL "product2"(0:nr2m23,14:propertytex1m30)
                  FILTER IS_NOT_NULL(propertynum1m25)
                     EXTENSIONAL "product2"(0:nr2m23,7:propertynum1m25)

computed cost: 0




------SPARQL Q3------------------------------------------------------------------------------------------------------------------
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?product ?label
WHERE {
 ?product rdfs:label ?label .
 ?product a bsbm:Product .
 ?product bsbm:productFeature bsbm-inst:ProductFeature89 .
 ?product bsbm:productPropertyNumeric1 ?p1 .
 FILTER ( ?p1 > 10 )
 ?product bsbm:productPropertyNumeric3 ?p3 .
 FILTER ( ?p3 < 5000 )
 OPTIONAL {
 ?product bsbm:productFeature bsbm-inst:ProductFeature91 .
 ?product rdfs:label ?testVar }
 FILTER (!bound(?testVar))
}




CONSTRUCT [product, label] [product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product0m2)),IRI), label/RDF(VARCHARToTEXT(label10m46),xsd:string)]
   DISTINCT
      CONSTRUCT [label10m46, product0m2, propertynum1m41, propertynum1m26, product0m4, label10m10] [product0m4/IF_ELSE_NULL(IS_NOT_NULL(label10m10),product0m2)]
         FILTER AND3(IS_NULL(label10m10),NUM_LT(INTEGERToBIGINT(propertynum1m26),"5000"^^BIGINT),NUM_GT(INTEGERToBIGINT(propertynum1m41),"10"^^BIGINT))
            LJ
               JOIN
                  UNION [label10m46, product0m2]
                     FILTER IS_NOT_NULL(label10m46)
                        EXTENSIONAL "product1"(0:product0m2,1:label10m46)
                     FILTER IS_NOT_NULL(label10m46)
                        EXTENSIONAL "product2"(0:product0m2,1:label10m46)
                  UNION [product0m2]
                     EXTENSIONAL "productfeatureproduct1"(0:product0m2,1:"89"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m2,1:"89"^^INTEGER)
                  UNION [product0m2, propertynum1m41]
                     FILTER IS_NOT_NULL(propertynum1m41)
                        EXTENSIONAL "product1"(0:product0m2,4:propertynum1m41)
                     FILTER IS_NOT_NULL(propertynum1m41)
                        EXTENSIONAL "product2"(0:product0m2,4:propertynum1m41)
                  UNION [product0m2, propertynum1m26]
                     FILTER IS_NOT_NULL(propertynum1m26)
                        EXTENSIONAL "product1"(0:product0m2,6:propertynum1m26)
                     FILTER IS_NOT_NULL(propertynum1m26)
                        EXTENSIONAL "product2"(0:product0m2,6:propertynum1m26)
               JOIN
                  UNION [product0m2]
                     EXTENSIONAL "productfeatureproduct1"(0:product0m2,1:"91"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m2,1:"91"^^INTEGER)
                  UNION [label10m10, product0m2]
                     FILTER IS_NOT_NULL(label10m10)
                        EXTENSIONAL "product1"(0:product0m2,1:label10m10)
                     FILTER IS_NOT_NULL(label10m10)
                        EXTENSIONAL "product2"(0:product0m2,1:label10m10)

computed cost: [18, 0]
new generated tree
CONSTRUCT [product, label] [product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product0m2)),IRI), label/RDF(VARCHARToTEXT(label10m46),xsd:string)]
   DISTINCT
      CONSTRUCT [label10m46, product0m2, propertynum1m41, propertynum1m26, product0m4, label10m10] [product0m4/IF_ELSE_NULL(IS_NOT_NULL(label10m10),product0m2)]
         FILTER AND3(IS_NULL(label10m10),NUM_LT(INTEGERToBIGINT(propertynum1m26),"5000"^^BIGINT),NUM_GT(INTEGERToBIGINT(propertynum1m41),"10"^^BIGINT))
            UNION [propertynum1m26, product0m2, label10m46, propertynum1m41, label10m10]
               LJ
                  JOIN
                     FILTER IS_NOT_NULL(label10m46)
                        EXTENSIONAL "product1"(0:product0m2,1:label10m46)
                     EXTENSIONAL "productfeatureproduct1"(0:product0m2,1:"89"^^INTEGER)
                     FILTER IS_NOT_NULL(propertynum1m41)
                        EXTENSIONAL "product1"(0:product0m2,4:propertynum1m41)
                     FILTER IS_NOT_NULL(propertynum1m26)
                        EXTENSIONAL "product1"(0:product0m2,6:propertynum1m26)
                  JOIN
                     EXTENSIONAL "productfeatureproduct1"(0:product0m2,1:"91"^^INTEGER)
                     FILTER IS_NOT_NULL(label10m10)
                        EXTENSIONAL "product1"(0:product0m2,1:label10m10)
               LJ
                  JOIN
                     FILTER IS_NOT_NULL(label10m46)
                        EXTENSIONAL "product2"(0:product0m2,1:label10m46)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m2,1:"89"^^INTEGER)
                     FILTER IS_NOT_NULL(propertynum1m41)
                        EXTENSIONAL "product2"(0:product0m2,4:propertynum1m41)
                     FILTER IS_NOT_NULL(propertynum1m26)
                        EXTENSIONAL "product2"(0:product0m2,6:propertynum1m26)
                  JOIN
                     EXTENSIONAL "productfeatureproduct2"(0:product0m2,1:"91"^^INTEGER)
                     FILTER IS_NOT_NULL(label10m10)
                        EXTENSIONAL "product2"(0:product0m2,1:label10m10)

computed cost: 0




------SPARQL Q4------------------------------------------------------------------------------------------------------------------
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT DISTINCT ?product ?label ?propertyTextual
WHERE {
 {
 ?product rdfs:label ?label .
 ?product rdf:type bsbm:Product.
 ?product bsbm:productFeature bsbm-inst:ProductFeature89 .
 ?product bsbm:productFeature bsbm-inst:ProductFeature91 .
 ?product bsbm:productPropertyTextual1 ?propertyTextual .
 ?product bsbm:productPropertyNumeric1 ?p1 .
	FILTER ( ?p1 > 30 )
 }
 UNION
 {
 ?product rdfs:label ?label .
 ?product rdf:type bsbm:Product .
 ?product bsbm:productFeature bsbm-inst:ProductFeature89 .
 ?product bsbm:productFeature bsbm-inst:ProductFeature86 .
 ?product bsbm:productPropertyTextual1 ?propertyTextual .
 ?product bsbm:productPropertyNumeric2 ?p2 .
	FILTER ( ?p2> 50 )
 }
}



generated IQ tree:
CONSTRUCT [product, label, propertyTextual] [product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product0m8)),IRI), label/RDF(VARCHARToTEXT(label10m9),xsd:string), propertyTextual/RDF(VARCHARToTEXT(propertytex1m10),xsd:string)]
   DISTINCT
      UNION [product0m8, label10m9, propertytex1m10]
         CONSTRUCT [product0m8, label10m9, propertytex1m10] []
            DISTINCT
               JOIN NUM_GT(INTEGERToBIGINT(propertynum1m41),"30"^^BIGINT)
                  UNION [label10m9, product0m8]
                     FILTER IS_NOT_NULL(label10m9)
                        EXTENSIONAL "product1"(0:product0m8,1:label10m9)
                     FILTER IS_NOT_NULL(label10m9)
                        EXTENSIONAL "product2"(0:product0m8,1:label10m9)
                  UNION [product0m8]
                     EXTENSIONAL "productfeatureproduct1"(0:product0m8,1:"89"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m8,1:"89"^^INTEGER)
                  UNION [product0m8]
                     EXTENSIONAL "productfeatureproduct1"(0:product0m8,1:"91"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m8,1:"91"^^INTEGER)
                  UNION [product0m8, propertytex1m10]
                     FILTER IS_NOT_NULL(propertytex1m10)
                        EXTENSIONAL "product1"(0:product0m8,10:propertytex1m10)
                     FILTER IS_NOT_NULL(propertytex1m10)
                        EXTENSIONAL "product2"(0:product0m8,10:propertytex1m10)
                  UNION [product0m8, propertynum1m41]
                     FILTER IS_NOT_NULL(propertynum1m41)
                        EXTENSIONAL "product1"(0:product0m8,4:propertynum1m41)
                     FILTER IS_NOT_NULL(propertynum1m41)
                        EXTENSIONAL "product2"(0:product0m8,4:propertynum1m41)
         CONSTRUCT [product0m8, label10m9, propertytex1m10] []
            DISTINCT
               JOIN NUM_GT(INTEGERToBIGINT(propertynum1m40),"50"^^BIGINT)
                  UNION [label10m9, product0m8]
                     FILTER IS_NOT_NULL(label10m9)
                        EXTENSIONAL "product1"(0:product0m8,1:label10m9)
                     FILTER IS_NOT_NULL(label10m9)
                        EXTENSIONAL "product2"(0:product0m8,1:label10m9)
                  UNION [product0m8]
                     EXTENSIONAL "productfeatureproduct1"(0:product0m8,1:"89"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m8,1:"89"^^INTEGER)
                  UNION [product0m8]
                     EXTENSIONAL "productfeatureproduct1"(0:product0m8,1:"86"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m8,1:"86"^^INTEGER)
                  UNION [product0m8, propertytex1m10]
                     FILTER IS_NOT_NULL(propertytex1m10)
                        EXTENSIONAL "product1"(0:product0m8,10:propertytex1m10)
                     FILTER IS_NOT_NULL(propertytex1m10)
                        EXTENSIONAL "product2"(0:product0m8,10:propertytex1m10)
                  UNION [product0m8, propertynum1m40]
                     FILTER IS_NOT_NULL(propertynum1m40)
                        EXTENSIONAL "product1"(0:product0m8,5:propertynum1m40)
                     FILTER IS_NOT_NULL(propertynum1m40)
                        EXTENSIONAL "product2"(0:product0m8,5:propertynum1m40)

computed cost: [32, 0]


new generated tree
CONSTRUCT [product, label, propertyTextual] [product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product0m8)),IRI), label/RDF(VARCHARToTEXT(label10m9),xsd:string), propertyTextual/RDF(VARCHARToTEXT(propertytex1m10),xsd:string)]
   DISTINCT
      UNION [product0m8, label10m9, propertytex1m10]
         CONSTRUCT [product0m8, label10m9, propertytex1m10] []
            DISTINCT
               UNION [product0m8, propertytex1m10, label10m9, propertynum1m41]
                  JOIN NUM_GT(INTEGERToBIGINT(propertynum1m41),"30"^^BIGINT)
                     FILTER IS_NOT_NULL(label10m9)
                        EXTENSIONAL "product1"(0:product0m8,1:label10m9)
                     EXTENSIONAL "productfeatureproduct1"(0:product0m8,1:"89"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct1"(0:product0m8,1:"91"^^INTEGER)
                     FILTER IS_NOT_NULL(propertytex1m10)
                        EXTENSIONAL "product1"(0:product0m8,10:propertytex1m10)
                     FILTER IS_NOT_NULL(propertynum1m41)
                        EXTENSIONAL "product1"(0:product0m8,4:propertynum1m41)
                  JOIN NUM_GT(INTEGERToBIGINT(propertynum1m41),"30"^^BIGINT)
                     FILTER IS_NOT_NULL(label10m9)
                        EXTENSIONAL "product2"(0:product0m8,1:label10m9)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m8,1:"89"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m8,1:"91"^^INTEGER)
                     FILTER IS_NOT_NULL(propertytex1m10)
                        EXTENSIONAL "product2"(0:product0m8,10:propertytex1m10)
                     FILTER IS_NOT_NULL(propertynum1m41)
                        EXTENSIONAL "product2"(0:product0m8,4:propertynum1m41)
         CONSTRUCT [product0m8, label10m9, propertytex1m10] []
            DISTINCT
               UNION [product0m8, propertytex1m10, label10m9, propertynum1m40]
                  JOIN NUM_GT(INTEGERToBIGINT(propertynum1m40),"50"^^BIGINT)
                     FILTER IS_NOT_NULL(label10m9)
                        EXTENSIONAL "product1"(0:product0m8,1:label10m9)
                     EXTENSIONAL "productfeatureproduct1"(0:product0m8,1:"89"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct1"(0:product0m8,1:"86"^^INTEGER)
                     FILTER IS_NOT_NULL(propertytex1m10)
                        EXTENSIONAL "product1"(0:product0m8,10:propertytex1m10)
                     FILTER IS_NOT_NULL(propertynum1m40)
                        EXTENSIONAL "product1"(0:product0m8,5:propertynum1m40)
                  JOIN NUM_GT(INTEGERToBIGINT(propertynum1m40),"50"^^BIGINT)
                     FILTER IS_NOT_NULL(label10m9)
                        EXTENSIONAL "product2"(0:product0m8,1:label10m9)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m8,1:"89"^^INTEGER)
                     EXTENSIONAL "productfeatureproduct2"(0:product0m8,1:"86"^^INTEGER)
                     FILTER IS_NOT_NULL(propertytex1m10)
                        EXTENSIONAL "product2"(0:product0m8,10:propertytex1m10)
                     FILTER IS_NOT_NULL(propertynum1m40)
                        EXTENSIONAL "product2"(0:product0m8,5:propertynum1m40)

computed cost: 0




------SPARQL Q5------------------------------------------------------------------------------------------------------------------
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>

SELECT ?product ?productLabel
WHERE {
	?product rdfs:label ?productLabel .
    FILTER (<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> != ?product)
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> bsbm:productFeature ?prodFeature .
	?product bsbm:productFeature ?prodFeature .
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> bsbm:productPropertyNumeric1 ?origProperty1 .
	?product bsbm:productPropertyNumeric1 ?simProperty1 .
	FILTER (?simProperty1 < (?origProperty1 + 120))
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> bsbm:productPropertyNumeric2 ?origProperty2 .
	?product bsbm:productPropertyNumeric2 ?simProperty2 .
	FILTER (?simProperty2 < (?origProperty2 + 170) )
}



generated IQ tree:
CONSTRUCT [product, productLabel] [productLabel/RDF(VARCHARToTEXT(label10m46),xsd:string), product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product0m4)),IRI)]
   DISTINCT
      JOIN AND3(NUM_LT(INTEGERToBIGINT(propertynum1m15),BIGINT+(INTEGERToBIGINT(propertynum1m40),"170"^^BIGINT)),NUM_LT(INTEGERToBIGINT(propertynum1m10),BIGINT+(INTEGERToBIGINT(propertynum1m41),"120"^^BIGINT)),STRICT_NEQ2(product0m4,"88"^^INTEGER))
         UNION [label10m46, product0m4]
            FILTER IS_NOT_NULL(label10m46)
               EXTENSIONAL "product1"(0:product0m4,1:label10m46)
            FILTER IS_NOT_NULL(label10m46)
               EXTENSIONAL "product2"(0:product0m4,1:label10m46)
         UNION [productfeature2m2]
            EXTENSIONAL "productfeatureproduct1"(0:"88"^^INTEGER,1:productfeature2m2)
            EXTENSIONAL "productfeatureproduct2"(0:"88"^^INTEGER,1:productfeature2m2)
         UNION [product0m4, productfeature2m2]
            EXTENSIONAL "productfeatureproduct1"(0:product0m4,1:productfeature2m2)
            EXTENSIONAL "productfeatureproduct2"(0:product0m4,1:productfeature2m2)
         UNION [propertynum1m41]
            FILTER IS_NOT_NULL(propertynum1m41)
               EXTENSIONAL "product1"(0:"88"^^INTEGER,4:propertynum1m41)
            FILTER IS_NOT_NULL(propertynum1m41)
               EXTENSIONAL "product2"(0:"88"^^INTEGER,4:propertynum1m41)
         UNION [product0m4, propertynum1m10]
            FILTER IS_NOT_NULL(propertynum1m10)
               EXTENSIONAL "product1"(0:product0m4,4:propertynum1m10)
            FILTER IS_NOT_NULL(propertynum1m10)
               EXTENSIONAL "product2"(0:product0m4,4:propertynum1m10)
         UNION [propertynum1m40]
            FILTER IS_NOT_NULL(propertynum1m40)
               EXTENSIONAL "product1"(0:"88"^^INTEGER,5:propertynum1m40)
            FILTER IS_NOT_NULL(propertynum1m40)
               EXTENSIONAL "product2"(0:"88"^^INTEGER,5:propertynum1m40)
         UNION [product0m4, propertynum1m15]
            FILTER IS_NOT_NULL(propertynum1m15)
               EXTENSIONAL "product1"(0:product0m4,5:propertynum1m15)
            FILTER IS_NOT_NULL(propertynum1m15)
               EXTENSIONAL "product2"(0:product0m4,5:propertynum1m15)

computed cost: [24, 0]
new generated tree
CONSTRUCT [product, productLabel] [productLabel/RDF(VARCHARToTEXT(label10m46),xsd:string), product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product0m4)),IRI)]
   DISTINCT
      JOIN AND3(NUM_LT(INTEGERToBIGINT(propertynum1m15),BIGINT+(INTEGERToBIGINT(propertynum1m40),"170"^^BIGINT)),NUM_LT(INTEGERToBIGINT(propertynum1m10),BIGINT+(INTEGERToBIGINT(propertynum1m41),"120"^^BIGINT)),STRICT_NEQ2(product0m4,"88"^^INTEGER))
         UNION [label10m46, product0m4]
            FILTER IS_NOT_NULL(label10m46)
               EXTENSIONAL "product1"(0:product0m4,1:label10m46)
            FILTER IS_NOT_NULL(label10m46)
               EXTENSIONAL "product2"(0:product0m4,1:label10m46)
         UNION [productfeature2m2]
            EXTENSIONAL "productfeatureproduct1"(0:"88"^^INTEGER,1:productfeature2m2)
            EXTENSIONAL "productfeatureproduct2"(0:"88"^^INTEGER,1:productfeature2m2)
         UNION [product0m4, productfeature2m2]
            EXTENSIONAL "productfeatureproduct1"(0:product0m4,1:productfeature2m2)
            EXTENSIONAL "productfeatureproduct2"(0:product0m4,1:productfeature2m2)
         UNION [propertynum1m41]
            FILTER IS_NOT_NULL(propertynum1m41)
               EXTENSIONAL "product1"(0:"88"^^INTEGER,4:propertynum1m41)
            FILTER IS_NOT_NULL(propertynum1m41)
               EXTENSIONAL "product2"(0:"88"^^INTEGER,4:propertynum1m41)
         UNION [product0m4, propertynum1m10]
            FILTER IS_NOT_NULL(propertynum1m10)
               EXTENSIONAL "product1"(0:product0m4,4:propertynum1m10)
            FILTER IS_NOT_NULL(propertynum1m10)
               EXTENSIONAL "product2"(0:product0m4,4:propertynum1m10)
         UNION [propertynum1m40]
            FILTER IS_NOT_NULL(propertynum1m40)
               EXTENSIONAL "product1"(0:"88"^^INTEGER,5:propertynum1m40)
            FILTER IS_NOT_NULL(propertynum1m40)
               EXTENSIONAL "product2"(0:"88"^^INTEGER,5:propertynum1m40)
         UNION [product0m4, propertynum1m15]
            FILTER IS_NOT_NULL(propertynum1m15)
               EXTENSIONAL "product1"(0:product0m4,5:propertynum1m15)
            FILTER IS_NOT_NULL(propertynum1m15)
               EXTENSIONAL "product2"(0:product0m4,5:propertynum1m15)

computed cost: 0




------SPARQL Q6------------------------------------------------------------------------------------------------------------------
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>

SELECT ?product ?label
WHERE {
	?product rdfs:label ?label .
 ?product rdf:type bsbm:Product .
	FILTER regex(?label, "%word1%")
}


generated IQ tree:
CONSTRUCT [product, label] [product/RDF(v4m46,IRI), label/RDF(VARCHARToTEXT(label10m46),xsd:string)]
   UNION [v4m46, label10m46]
      CONSTRUCT [v4m46, label10m46] [v4m46/http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(nr1m1))]
         DISTINCT
            JOIN REGEXP_LIKE2(VARCHARToTEXT(label10m46),"%word1%"^^TEXT)
               UNION [label10m46, nr1m1]
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product1"(0:nr1m1,1:label10m46)
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product2"(0:nr1m1,1:label10m46)
               UNION [nr1m1]
                  EXTENSIONAL "product1"(0:nr1m1)
                  EXTENSIONAL "product2"(0:nr1m1)
                  FILTER IS_NOT_NULL(nr1m1)
                     EXTENSIONAL "review"(1:nr1m1)
                  FILTER IS_NOT_NULL(nr1m1)
                     EXTENSIONAL "reviewc"(1:nr1m1)
                  FILTER IS_NOT_NULL(nr1m1)
                     EXTENSIONAL "offer"(1:nr1m1)
      CONSTRUCT [v4m46, label10m46] [v4m46/http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature{}(INTEGERToTEXT(nr8m46))]
         FILTER AND3(REGEXP_LIKE2(VARCHARToTEXT(label10m46),"%word1%"^^TEXT),IS_NOT_NULL(label10m46),IS_NOT_NULL(publisher36m54))
            EXTENSIONAL "productfeature"(0:nr8m46,1:label10m46,3:publisher36m54)
      CONSTRUCT [v4m46, label10m46] [v4m46/http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType{}(INTEGERToTEXT(nr13m46))]
         FILTER AND3(REGEXP_LIKE2(VARCHARToTEXT(label10m46),"%word1%"^^TEXT),IS_NOT_NULL(label10m46),IS_NOT_NULL(publisher41m54))
            EXTENSIONAL "producttype"(0:nr13m46,1:label10m46,4:publisher41m54)

computed cost: [7, 0]


*************need extra rules to be added to remove redundancy******************************
new generated tree
CONSTRUCT [product, label] [product/RDF(v4m46,IRI), label/RDF(VARCHARToTEXT(label10m46),xsd:string)]
   UNION [v4m46, label10m46]
      CONSTRUCT [v4m46, label10m46] [v4m46/http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(nr1m1))]
         DISTINCT
            JOIN REGEXP_LIKE2(VARCHARToTEXT(label10m46),"%word1%"^^TEXT)
               UNION [label10m46, nr1m1]
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product1"(0:nr1m1,1:label10m46)
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product2"(0:nr1m1,1:label10m46)
               UNION [nr1m1]
                  EXTENSIONAL "product1"(0:nr1m1)
                  EXTENSIONAL "product2"(0:nr1m1)
                  FILTER IS_NOT_NULL(nr1m1)
                     EXTENSIONAL "review"(1:nr1m1)
                  FILTER IS_NOT_NULL(nr1m1)
                     EXTENSIONAL "offer"(1:nr1m1)
      CONSTRUCT [v4m46, label10m46] [v4m46/http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature{}(INTEGERToTEXT(nr8m46))]
         FILTER AND3(REGEXP_LIKE2(VARCHARToTEXT(label10m46),"%word1%"^^TEXT),IS_NOT_NULL(label10m46),IS_NOT_NULL(publisher36m54))
            EXTENSIONAL "productfeature"(0:nr8m46,1:label10m46,3:publisher36m54)
      CONSTRUCT [v4m46, label10m46] [v4m46/http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType{}(INTEGERToTEXT(nr13m46))]
         FILTER AND3(REGEXP_LIKE2(VARCHARToTEXT(label10m46),"%word1%"^^TEXT),IS_NOT_NULL(label10m46),IS_NOT_NULL(publisher41m54))
            EXTENSIONAL "producttype"(0:nr13m46,1:label10m46,4:publisher41m54)

computed cost: 0




------SPARQL Q7------------------------------------------------------------------------------------------------------------------
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle
 ?reviewer ?revName ?rating1 ?rating2
WHERE {
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> rdfs:label ?productLabel .
 OPTIONAL {
 ?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> .
 ?offer bsbm:price ?price .
 ?offer bsbm:vendor ?vendor .
 ?vendor rdfs:label ?vendorTitle .
 ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#DE> .
 ?offer dc:publisher ?vendor .
 ?offer bsbm:validTo ?date .
 FILTER (?date > '1988-01-01'^^xsd:date)
 }
 OPTIONAL {
	?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> .
	?review rev:reviewer ?reviewer .
	?reviewer foaf:name ?revName .
	?review dc:title ?revTitle .
 OPTIONAL { ?review bsbm:rating1 ?rating1 . }
 OPTIONAL { ?review bsbm:rating2 ?rating2 . }
 }
}



generated IQ tree:
CONSTRUCT [productLabel, offer, price, vendor, vendorTitle, review, revTitle, reviewer, revName, rating1, rating2] [offer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer{}(INTEGERToTEXT(nr1m5)),IF_ELSE_NULL(IS_NOT_NULL(nr1m5),IRI)), productLabel/RDF(VARCHARToTEXT(label10m46),xsd:string), rating1/RDF(INTEGERToTEXT(rating1m16),IF_ELSE_NULL(IS_NOT_NULL(rating1m16),xsd:integer)), rating2/RDF(INTEGERToTEXT(rating1m17),IF_ELSE_NULL(IS_NOT_NULL(rating1m17),xsd:integer)), review/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Review{}(INTEGERToTEXT(nr0m4)),IF_ELSE_NULL(IS_NOT_NULL(nr0m4),IRI)), price/RDF(DOUBLE PRECISIONToTEXT(price1m39),IF_ELSE_NULL(IS_NOT_NULL(price1m39),xsd:double)), vendor/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor{}(INTEGERToTEXT(vendor1m8)),IF_ELSE_NULL(IS_NOT_NULL(vendor1m8),IRI)), reviewer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Reviewer{}(INTEGERToTEXT(person2m7)),IF_ELSE_NULL(IS_NOT_NULL(person2m7),IRI)), revName/RDF(VARCHARToTEXT(name1m12),IF_ELSE_NULL(IS_NOT_NULL(name1m12),xsd:string)), revTitle/RDF(VARCHARToTEXT(title2m11),IF_ELSE_NULL(IS_NOT_NULL(title2m11),xsd:string)), vendorTitle/RDF(VARCHARToTEXT(label10m4),IF_ELSE_NULL(IS_NOT_NULL(label10m4),xsd:string))]
   DISTINCT
      CONSTRUCT [label10m46, nr1m5, price1m39, vendor1m8, label10m4, validto1m45, nr0m4, person2m7, name1m12, title2m11, nr0m16, rating1m16, nr0m17, rating1m17] [nr0m16/IF_ELSE_NULL(IS_NOT_NULL(rating1m16),nr0m4), nr0m17/IF_ELSE_NULL(IS_NOT_NULL(rating1m17),nr0m4)]
         LJ IS_NOT_NULL(name1m12)
            LJ AND4(DATE_GT(validto1m45,"1988-01-01"^^DATE),IS_NOT_NULL(price1m39),IS_NOT_NULL(label10m4),IS_NOT_NULL(validto1m45))
               UNION [label10m46]
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product1"(0:"88"^^INTEGER,1:label10m46)
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product2"(0:"88"^^INTEGER,1:label10m46)
               JOIN
                  EXTENSIONAL "offer"(0:nr1m5,1:"88"^^INTEGER,4:price1m39,3:vendor1m8,6:validto1m45)
                  EXTENSIONAL "vendor"(0:vendor1m8,1:label10m4,4:"DE"^^BPCHAR)
            LJ
               LJ
                  JOIN
                     UNION [nr0m4]
                        EXTENSIONAL "review"(0:nr0m4,1:"88"^^INTEGER)
                        EXTENSIONAL "reviewc"(0:nr0m4,1:"88"^^INTEGER)
                     UNION [nr0m4, person2m7, name1m12]
                        JOIN
                           EXTENSIONAL "review"(0:nr0m4,3:person2m7)
                           EXTENSIONAL "person"(0:person2m7,1:name1m12)
                        JOIN
                           EXTENSIONAL "reviewc"(0:nr0m4,3:person2m7)
                           EXTENSIONAL "person"(0:person2m7,1:name1m12)
                     UNION [nr0m4, title2m11]
                        FILTER IS_NOT_NULL(title2m11)
                           EXTENSIONAL "review"(0:nr0m4,5:title2m11)
                        FILTER IS_NOT_NULL(title2m11)
                           EXTENSIONAL "reviewc"(0:nr0m4,5:title2m11)
                  UNION [nr0m4, rating1m16]
                     FILTER IS_NOT_NULL(rating1m16)
                        EXTENSIONAL "review"(0:nr0m4,8:rating1m16)
                     FILTER IS_NOT_NULL(rating1m16)
                        EXTENSIONAL "reviewc"(0:nr0m4,8:rating1m16)
               UNION [nr0m4, rating1m17]
                  FILTER IS_NOT_NULL(rating1m17)
                     EXTENSIONAL "review"(0:nr0m4,9:rating1m17)
                  FILTER IS_NOT_NULL(rating1m17)
                     EXTENSIONAL "reviewc"(0:nr0m4,9:rating1m17)

computed cost: [21, 0]


new generated tree
CONSTRUCT [productLabel, offer, price, vendor, vendorTitle, review, revTitle, reviewer, revName, rating1, rating2] [offer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer{}(INTEGERToTEXT(nr1m5)),IF_ELSE_NULL(IS_NOT_NULL(nr1m5),IRI)), productLabel/RDF(VARCHARToTEXT(label10m46),xsd:string), rating1/RDF(INTEGERToTEXT(rating1m16),IF_ELSE_NULL(IS_NOT_NULL(rating1m16),xsd:integer)), rating2/RDF(INTEGERToTEXT(rating1m17),IF_ELSE_NULL(IS_NOT_NULL(rating1m17),xsd:integer)), review/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Review{}(INTEGERToTEXT(nr0m4)),IF_ELSE_NULL(IS_NOT_NULL(nr0m4),IRI)), price/RDF(DOUBLE PRECISIONToTEXT(price1m39),IF_ELSE_NULL(IS_NOT_NULL(price1m39),xsd:double)), vendor/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor{}(INTEGERToTEXT(vendor1m8)),IF_ELSE_NULL(IS_NOT_NULL(vendor1m8),IRI)), reviewer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Reviewer{}(INTEGERToTEXT(person2m7)),IF_ELSE_NULL(IS_NOT_NULL(person2m7),IRI)), revName/RDF(VARCHARToTEXT(name1m12),IF_ELSE_NULL(IS_NOT_NULL(name1m12),xsd:string)), revTitle/RDF(VARCHARToTEXT(title2m11),IF_ELSE_NULL(IS_NOT_NULL(title2m11),xsd:string)), vendorTitle/RDF(VARCHARToTEXT(label10m4),IF_ELSE_NULL(IS_NOT_NULL(label10m4),xsd:string))]
   DISTINCT
      CONSTRUCT [label10m46, nr1m5, price1m39, vendor1m8, label10m4, validto1m45, nr0m4, person2m7, name1m12, title2m11, nr0m16, rating1m16, nr0m17, rating1m17] [nr0m16/IF_ELSE_NULL(IS_NOT_NULL(rating1m16),nr0m4), nr0m17/IF_ELSE_NULL(IS_NOT_NULL(rating1m17),nr0m4)]
         LJ IS_NOT_NULL(name1m12)
            LJ AND4(DATE_GT(validto1m45,"1988-01-01"^^DATE),IS_NOT_NULL(price1m39),IS_NOT_NULL(label10m4),IS_NOT_NULL(validto1m45))
               UNION [label10m46]
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product1"(0:"88"^^INTEGER,1:label10m46)
                  FILTER IS_NOT_NULL(label10m46)
                     EXTENSIONAL "product2"(0:"88"^^INTEGER,1:label10m46)
               JOIN
                  EXTENSIONAL "offer"(0:nr1m5,1:"88"^^INTEGER,4:price1m39,3:vendor1m8,6:validto1m45)
                  EXTENSIONAL "vendor"(0:vendor1m8,1:label10m4,4:"DE"^^BPCHAR)
            JOIN
               EXTENSIONAL "reviewc"(0:nr0m4,1:"88"^^INTEGER)
               JOIN
                  EXTENSIONAL "review"(0:nr0m4,3:person2m7)
                  EXTENSIONAL "person"(0:person2m7,1:name1m12)
               FILTER IS_NOT_NULL(title2m11)
                  EXTENSIONAL "review"(0:nr0m4,5:title2m11)
               FILTER IS_NOT_NULL(rating1m16)
                  EXTENSIONAL "reviewc"(0:nr0m4,8:rating1m16)
               FILTER IS_NOT_NULL(rating1m17)
                  EXTENSIONAL "reviewc"(0:nr0m4,9:rating1m17)

computed cost: 0




------SPARQL Q8------------------------------------------------------------------------------------------------------------------
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>

SELECT ?title ?text ?reviewDate ?reviewer ?reviewerName ?rating1 ?rating2 ?rating3 ?rating4
WHERE {
	?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> .
	?review dc:title ?title .
	?review rev:text ?text .
	?review bsbm:reviewDate ?reviewDate .
	?review rev:reviewer ?reviewer .
	?reviewer foaf:name ?reviewerName .
	OPTIONAL { ?review bsbm:rating1 ?rating1 . }
	OPTIONAL { ?review bsbm:rating2 ?rating2 . }
	OPTIONAL { ?review bsbm:rating3 ?rating3 . }
	OPTIONAL { ?review bsbm:rating4 ?rating4 . }
}



generated IQ tree:
CONSTRUCT [title, text, reviewDate, reviewer, reviewerName, rating1, rating2, rating3, rating4] [reviewerName/RDF(VARCHARToTEXT(name1m12),xsd:string), rating1/RDF(INTEGERToTEXT(rating1m16),IF_ELSE_NULL(IS_NOT_NULL(rating1m16),xsd:integer)), rating2/RDF(INTEGERToTEXT(rating1m17),IF_ELSE_NULL(IS_NOT_NULL(rating1m17),xsd:integer)), reviewDate/RDF(DATEToTEXT(reviewdate2m43),xsd:date), rating3/RDF(INTEGERToTEXT(rating1m13),IF_ELSE_NULL(IS_NOT_NULL(rating1m13),xsd:integer)), rating4/RDF(INTEGERToTEXT(rating1m15),IF_ELSE_NULL(IS_NOT_NULL(rating1m15),xsd:integer)), reviewer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Reviewer{}(INTEGERToTEXT(person2m7)),IRI), text/RDF(VARCHARToTEXT(text2m14),xsd:string), title/RDF(VARCHARToTEXT(title2m11),xsd:string)]
   DISTINCT
      CONSTRUCT [nr0m4, title2m11, text2m14, reviewdate2m43, person2m7, name1m12, nr0m16, rating1m16, nr0m17, rating1m17, nr0m13, rating1m13, nr0m15, rating1m15] [nr0m13/IF_ELSE_NULL(IS_NOT_NULL(rating1m13),nr0m4), nr0m16/IF_ELSE_NULL(IS_NOT_NULL(rating1m16),nr0m4), nr0m17/IF_ELSE_NULL(IS_NOT_NULL(rating1m17),nr0m4), nr0m15/IF_ELSE_NULL(IS_NOT_NULL(rating1m15),nr0m4)]
         FILTER IS_NOT_NULL(name1m12)
            LJ
               LJ
                  LJ
                     LJ
                        JOIN
                           UNION [nr0m4]
                              EXTENSIONAL "review"(0:nr0m4,1:"88"^^INTEGER)
                              EXTENSIONAL "reviewc"(0:nr0m4,1:"88"^^INTEGER)
                           UNION [nr0m4, title2m11]
                              FILTER IS_NOT_NULL(title2m11)
                                 EXTENSIONAL "review"(0:nr0m4,5:title2m11)
                              FILTER IS_NOT_NULL(title2m11)
                                 EXTENSIONAL "reviewc"(0:nr0m4,5:title2m11)
                           UNION [nr0m4, text2m14]
                              FILTER IS_NOT_NULL(text2m14)
                                 EXTENSIONAL "review"(0:nr0m4,6:text2m14)
                              FILTER IS_NOT_NULL(text2m14)
                                 EXTENSIONAL "reviewc"(0:nr0m4,6:text2m14)
                           UNION [nr0m4, reviewdate2m43]
                              FILTER IS_NOT_NULL(reviewdate2m43)
                                 EXTENSIONAL "review"(0:nr0m4,4:reviewdate2m43)
                              FILTER IS_NOT_NULL(reviewdate2m43)
                                 EXTENSIONAL "reviewc"(0:nr0m4,4:reviewdate2m43)
                           UNION [nr0m4, person2m7, name1m12]
                              JOIN
                                 EXTENSIONAL "review"(0:nr0m4,3:person2m7)
                                 EXTENSIONAL "person"(0:person2m7,1:name1m12)
                              JOIN
                                 EXTENSIONAL "reviewc"(0:nr0m4,3:person2m7)
                                 EXTENSIONAL "person"(0:person2m7,1:name1m12)
                        UNION [nr0m4, rating1m16]
                           FILTER IS_NOT_NULL(rating1m16)
                              EXTENSIONAL "review"(0:nr0m4,8:rating1m16)
                           FILTER IS_NOT_NULL(rating1m16)
                              EXTENSIONAL "reviewc"(0:nr0m4,8:rating1m16)
                     UNION [nr0m4, rating1m17]
                        FILTER IS_NOT_NULL(rating1m17)
                           EXTENSIONAL "review"(0:nr0m4,9:rating1m17)
                        FILTER IS_NOT_NULL(rating1m17)
                           EXTENSIONAL "reviewc"(0:nr0m4,9:rating1m17)
                  UNION [nr0m4, rating1m13]
                     FILTER IS_NOT_NULL(rating1m13)
                        EXTENSIONAL "review"(0:nr0m4,10:rating1m13)
                     FILTER IS_NOT_NULL(rating1m13)
                        EXTENSIONAL "reviewc"(0:nr0m4,10:rating1m13)
               UNION [nr0m4, rating1m15]
                  FILTER IS_NOT_NULL(rating1m15)
                     EXTENSIONAL "review"(0:nr0m4,11:rating1m15)
                  FILTER IS_NOT_NULL(rating1m15)
                     EXTENSIONAL "reviewc"(0:nr0m4,11:rating1m15)

computed cost: [30, 0]


new generated tree
CONSTRUCT [title, text, reviewDate, reviewer, reviewerName, rating1, rating2, rating3, rating4] [reviewerName/RDF(VARCHARToTEXT(name1m12),xsd:string), rating1/RDF(INTEGERToTEXT(rating1m16),IF_ELSE_NULL(IS_NOT_NULL(rating1m16),xsd:integer)), rating2/RDF(INTEGERToTEXT(rating1m17),IF_ELSE_NULL(IS_NOT_NULL(rating1m17),xsd:integer)), reviewDate/RDF(DATEToTEXT(reviewdate2m43),xsd:date), rating3/RDF(INTEGERToTEXT(rating1m13),IF_ELSE_NULL(IS_NOT_NULL(rating1m13),xsd:integer)), rating4/RDF(INTEGERToTEXT(rating1m15),IF_ELSE_NULL(IS_NOT_NULL(rating1m15),xsd:integer)), reviewer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Reviewer{}(INTEGERToTEXT(person2m7)),IRI), text/RDF(VARCHARToTEXT(text2m14),xsd:string), title/RDF(VARCHARToTEXT(title2m11),xsd:string)]
   DISTINCT
      CONSTRUCT [nr0m4, title2m11, text2m14, reviewdate2m43, person2m7, name1m12, nr0m16, rating1m16, nr0m17, rating1m17, nr0m13, rating1m13, nr0m15, rating1m15] [nr0m13/IF_ELSE_NULL(IS_NOT_NULL(rating1m13),nr0m4), nr0m16/IF_ELSE_NULL(IS_NOT_NULL(rating1m16),nr0m4), nr0m17/IF_ELSE_NULL(IS_NOT_NULL(rating1m17),nr0m4), nr0m15/IF_ELSE_NULL(IS_NOT_NULL(rating1m15),nr0m4)]
         FILTER IS_NOT_NULL(name1m12)
            JOIN
               EXTENSIONAL "reviewc"(0:nr0m4,1:"88"^^INTEGER)
               FILTER IS_NOT_NULL(title2m11)
                  EXTENSIONAL "reviewc"(0:nr0m4,5:title2m11)
               FILTER IS_NOT_NULL(text2m14)
                  EXTENSIONAL "reviewc"(0:nr0m4,6:text2m14)
               FILTER IS_NOT_NULL(reviewdate2m43)
                  EXTENSIONAL "reviewc"(0:nr0m4,4:reviewdate2m43)
               JOIN
                  EXTENSIONAL "review"(0:nr0m4,3:person2m7)
                  EXTENSIONAL "person"(0:person2m7,1:name1m12)
               FILTER IS_NOT_NULL(rating1m16)
                  EXTENSIONAL "reviewc"(0:nr0m4,8:rating1m16)
               FILTER IS_NOT_NULL(rating1m17)
                  EXTENSIONAL "reviewc"(0:nr0m4,9:rating1m17)
               FILTER IS_NOT_NULL(rating1m13)
                  EXTENSIONAL "reviewc"(0:nr0m4,10:rating1m13)
               FILTER IS_NOT_NULL(rating1m15)
                  EXTENSIONAL "reviewc"(0:nr0m4,11:rating1m15)

computed cost: 0




------SPARQL Q9------------------------------------------------------------------------------------------------------------------
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?p ?mbox_sha1sum ?country ?r ?product ?title
WHERE {
<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Review88> rev:reviewer ?p .
?p foaf:name ?name .
?p foaf:mbox_sha1sum ?mbox_sha1sum .
?p bsbm:country ?country .
?r rev:reviewer ?p .
?r bsbm:reviewFor ?product .
?r dc:title ?title .
}


generated IQ tree:
CONSTRUCT [p, mbox_sha1sum, country, r, product, title] [p/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Reviewer{}(INTEGERToTEXT(person2m7)),IRI), country/RDF(http://downlode.org/rdf/iso-3166/countries#{}(BPCHARToTEXT(country3m1)),IRI), product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product2m4)),IRI), r/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Review{}(INTEGERToTEXT(nr0m4)),IRI), mbox_sha1sum/RDF(BPCHARToTEXT(mbox_sha1sum1m47),xsd:string), title/RDF(VARCHARToTEXT(title2m11),xsd:string)]
   DISTINCT
      JOIN AND3(IS_NOT_NULL(name1m12),IS_NOT_NULL(mbox_sha1sum1m47),IS_NOT_NULL(country3m1))
         UNION [person2m7, name1m12, mbox_sha1sum1m47, country3m1]
            JOIN
               EXTENSIONAL "review"(0:"88"^^INTEGER,3:person2m7)
               EXTENSIONAL "person"(0:person2m7,1:name1m12,2:mbox_sha1sum1m47,3:country3m1)
            JOIN
               EXTENSIONAL "reviewc"(0:"88"^^INTEGER,3:person2m7)
               EXTENSIONAL "person"(0:person2m7,1:name1m12,2:mbox_sha1sum1m47,3:country3m1)
         UNION [nr0m4, person2m7]
            FILTER IS_NOT_NULL(person2m7)
               EXTENSIONAL "review"(0:nr0m4,3:person2m7)
            FILTER IS_NOT_NULL(person2m7)
               EXTENSIONAL "reviewc"(0:nr0m4,3:person2m7)
         UNION [nr0m4, product2m4]
            FILTER IS_NOT_NULL(product2m4)
               EXTENSIONAL "review"(0:nr0m4,1:product2m4)
            FILTER IS_NOT_NULL(product2m4)
               EXTENSIONAL "reviewc"(0:nr0m4,1:product2m4)
         UNION [nr0m4, title2m11]
            FILTER IS_NOT_NULL(title2m11)
               EXTENSIONAL "review"(0:nr0m4,5:title2m11)
            FILTER IS_NOT_NULL(title2m11)
               EXTENSIONAL "reviewc"(0:nr0m4,5:title2m11)

computed cost: [14, 0]


new generated tree
CONSTRUCT [p, mbox_sha1sum, country, r, product, title] [p/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Reviewer{}(INTEGERToTEXT(person2m7)),IRI), country/RDF(http://downlode.org/rdf/iso-3166/countries#{}(BPCHARToTEXT(country3m1)),IRI), product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product2m4)),IRI), r/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Review{}(INTEGERToTEXT(nr0m4)),IRI), mbox_sha1sum/RDF(BPCHARToTEXT(mbox_sha1sum1m47),xsd:string), title/RDF(VARCHARToTEXT(title2m11),xsd:string)]
   DISTINCT
      JOIN AND3(IS_NOT_NULL(name1m12),IS_NOT_NULL(mbox_sha1sum1m47),IS_NOT_NULL(country3m1))
         JOIN
            EXTENSIONAL "review"(0:"88"^^INTEGER,3:person2m7)
            EXTENSIONAL "person"(0:person2m7,1:name1m12,2:mbox_sha1sum1m47,3:country3m1)
         FILTER IS_NOT_NULL(person2m7)
            EXTENSIONAL "review"(0:nr0m4,3:person2m7)
         FILTER IS_NOT_NULL(product2m4)
            EXTENSIONAL "review"(0:nr0m4,1:product2m4)
         FILTER IS_NOT_NULL(title2m11)
            EXTENSIONAL "review"(0:nr0m4,5:title2m11)

computed cost: 0




------SPARQL Q10------------------------------------------------------------------------------------------------------------------
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT DISTINCT ?offer ?product
WHERE {
	?offer bsbm:product  ?product .
	?product bsbm:productId ?id .
	FILTER (?id<100)
	?offer bsbm:vendor ?vendor .
    ?offer dc:publisher ?vendor .
	?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#US> .
	?offer bsbm:deliveryDays ?deliveryDays .
	FILTER (?deliveryDays <= 3)
	?offer bsbm:price ?price .
 ?offer bsbm:validTo ?date .
 FILTER (?date > '1988-01-01'^^xsd:date)
}


generated IQ tree:
CONSTRUCT [offer, product] [offer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer{}(INTEGERToTEXT(nr1m5)),IRI), product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product1m5)),IRI)]
   DISTINCT
      CONSTRUCT [nr1m5, product1m5] []
         FILTER AND6(DATE_GT(validto1m45,"1988-01-01"^^DATE),NUM_LTE(INTEGERToBIGINT(deliverydays1m37),"3"^^BIGINT),NUM_LT(INTEGERToBIGINT(product1m5),"100"^^BIGINT),IS_NOT_NULL(deliverydays1m37),IS_NOT_NULL(price1m39),IS_NOT_NULL(validto1m45))
            UNION [product1m5, nr1m5, deliverydays1m37, price1m39, validto1m45]
               CONSTRUCT [product1m5, nr1m5, deliverydays1m37, price1m39, validto1m45] []
                  JOIN
                     EXTENSIONAL "product1"(0:product1m5)
                     EXTENSIONAL "offer"(0:nr1m5,1:product1m5,3:vendor1m8,7:deliverydays1m37,4:price1m39,6:validto1m45)
                     EXTENSIONAL "vendor"(0:vendor1m8,4:"US"^^BPCHAR)
               CONSTRUCT [product1m5, nr1m5, deliverydays1m37, price1m39, validto1m45] []
                  JOIN
                     EXTENSIONAL "product2"(0:product1m5)
                     EXTENSIONAL "offer"(0:nr1m5,1:product1m5,3:vendor1m8,7:deliverydays1m37,4:price1m39,6:validto1m45)
                     EXTENSIONAL "vendor"(0:vendor1m8,4:"US"^^BPCHAR)

computed cost: [4, 0]


new generated tree
CONSTRUCT [offer, product] [offer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer{}(INTEGERToTEXT(nr1m5)),IRI), product/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product1m5)),IRI)]
   DISTINCT
      CONSTRUCT [nr1m5, product1m5] []
         FILTER AND6(DATE_GT(validto1m45,"1988-01-01"^^DATE),NUM_LTE(INTEGERToBIGINT(deliverydays1m37),"3"^^BIGINT),NUM_LT(INTEGERToBIGINT(product1m5),"100"^^BIGINT),IS_NOT_NULL(deliverydays1m37),IS_NOT_NULL(price1m39),IS_NOT_NULL(validto1m45))
            UNION [product1m5, nr1m5, deliverydays1m37, price1m39, validto1m45]
               CONSTRUCT [product1m5, nr1m5, deliverydays1m37, price1m39, validto1m45] []
                  JOIN
                     EXTENSIONAL "product1"(0:product1m5)
                     EXTENSIONAL "offer"(0:nr1m5,1:product1m5,3:vendor1m8,7:deliverydays1m37,4:price1m39,6:validto1m45)
                     EXTENSIONAL "vendor"(0:vendor1m8,4:"US"^^BPCHAR)
               CONSTRUCT [product1m5, nr1m5, deliverydays1m37, price1m39, validto1m45] []
                  JOIN
                     EXTENSIONAL "product2"(0:product1m5)
                     EXTENSIONAL "offer"(0:nr1m5,1:product1m5,3:vendor1m8,7:deliverydays1m37,4:price1m39,6:validto1m45)
                     EXTENSIONAL "vendor"(0:vendor1m8,4:"US"^^BPCHAR)

computed cost: 0

Process finished with exit code 0




------SPARQL Q11------------------------------------------------------------------------------------------------------------------
SELECT ?property ?hasValue ?isValueOf
WHERE {
 { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer88> ?property ?hasValue }
 UNION
 { ?isValueOf ?property <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer88> }
}

///////
没用到hint-based query rewriting




------SPARQL Q12------------------------------------------------------------------------------------------------------------------
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?offer ?productURI ?productlabel ?vendorURI ?vendorname ?offerURL ?price ?deliveryDays ?validTo
WHERE {
 ?offer bsbm:offerId ?id .
 FILTER ( ?id < 1000)
 ?offer bsbm:product ?productURI .
 ?productURI rdfs:label ?productlabel .
 ?offer bsbm:vendor ?vendorURI .
 ?vendorURI rdfs:label ?vendorname .
 ?vendorURI foaf:homepage ?vendorhomepage .
 ?offer bsbm:offerWebpage ?offerURL .
 ?offer bsbm:price ?price .
 ?offer bsbm:deliveryDays ?deliveryDays .
 ?offer bsbm:validTo ?validTo
 }


generated IQ tree:
CONSTRUCT [offer, productURI, productlabel, vendorURI, vendorname, offerURL, price, deliveryDays, validTo] [offer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer{}(INTEGERToTEXT(nr1m21)),IRI), productURI/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product1m5)),IRI), vendorURI/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor{}(INTEGERToTEXT(vendor1m8)),IRI), productlabel/RDF(VARCHARToTEXT(label10m46),xsd:string), offerURL/RDF(VARCHARToTEXT(offerwebpage1m24),xsd:string), deliveryDays/RDF(INTEGERToTEXT(deliverydays1m37),xsd:integer), price/RDF(DOUBLE PRECISIONToTEXT(price1m39),xsd:double), vendorname/RDF(VARCHARToTEXT(label10m4),xsd:string), validTo/RDF(DATEToTEXT(validto1m45),xsd:date)]
   DISTINCT
      FILTER AND7(NUM_LT(INTEGERToBIGINT(nr1m21),"1000"^^BIGINT),IS_NOT_NULL(label10m4),IS_NOT_NULL(homepage2m48),IS_NOT_NULL(offerwebpage1m24),IS_NOT_NULL(price1m39),IS_NOT_NULL(deliverydays1m37),IS_NOT_NULL(validto1m45))
         UNION [label10m46, product1m5, nr1m21, vendor1m8, offerwebpage1m24, price1m39, deliverydays1m37, validto1m45, label10m4, homepage2m48]
            JOIN IS_NOT_NULL(label10m46)
               EXTENSIONAL "product1"(0:product1m5,1:label10m46)
               EXTENSIONAL "offer"(0:nr1m21,1:product1m5,3:vendor1m8,8:offerwebpage1m24,4:price1m39,7:deliverydays1m37,6:validto1m45)
               EXTENSIONAL "vendor"(0:vendor1m8,1:label10m4,3:homepage2m48)
            JOIN IS_NOT_NULL(label10m46)
               EXTENSIONAL "product2"(0:product1m5,1:label10m46)
               EXTENSIONAL "offer"(0:nr1m21,1:product1m5,3:vendor1m8,8:offerwebpage1m24,4:price1m39,7:deliverydays1m37,6:validto1m45)
               EXTENSIONAL "vendor"(0:vendor1m8,1:label10m4,3:homepage2m48)

computed cost: [4, 0]


new generated tree
CONSTRUCT [offer, productURI, productlabel, vendorURI, vendorname, offerURL, price, deliveryDays, validTo] [offer/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer{}(INTEGERToTEXT(nr1m21)),IRI), productURI/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product{}(INTEGERToTEXT(product1m5)),IRI), vendorURI/RDF(http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Vendor{}(INTEGERToTEXT(vendor1m8)),IRI), productlabel/RDF(VARCHARToTEXT(label10m46),xsd:string), offerURL/RDF(VARCHARToTEXT(offerwebpage1m24),xsd:string), deliveryDays/RDF(INTEGERToTEXT(deliverydays1m37),xsd:integer), price/RDF(DOUBLE PRECISIONToTEXT(price1m39),xsd:double), vendorname/RDF(VARCHARToTEXT(label10m4),xsd:string), validTo/RDF(DATEToTEXT(validto1m45),xsd:date)]
   DISTINCT
      FILTER AND7(NUM_LT(INTEGERToBIGINT(nr1m21),"1000"^^BIGINT),IS_NOT_NULL(label10m4),IS_NOT_NULL(homepage2m48),IS_NOT_NULL(offerwebpage1m24),IS_NOT_NULL(price1m39),IS_NOT_NULL(deliverydays1m37),IS_NOT_NULL(validto1m45))
         UNION [label10m46, product1m5, nr1m21, vendor1m8, offerwebpage1m24, price1m39, deliverydays1m37, validto1m45, label10m4, homepage2m48]
            JOIN IS_NOT_NULL(label10m46)
               EXTENSIONAL "product1"(0:product1m5,1:label10m46)
               EXTENSIONAL "offer"(0:nr1m21,1:product1m5,3:vendor1m8,8:offerwebpage1m24,4:price1m39,7:deliverydays1m37,6:validto1m45)
               EXTENSIONAL "vendor"(0:vendor1m8,1:label10m4,3:homepage2m48)
            JOIN IS_NOT_NULL(label10m46)
               EXTENSIONAL "product2"(0:product1m5,1:label10m46)
               EXTENSIONAL "offer"(0:nr1m21,1:product1m5,3:vendor1m8,8:offerwebpage1m24,4:price1m39,7:deliverydays1m37,6:validto1m45)
               EXTENSIONAL "vendor"(0:vendor1m8,1:label10m4,3:homepage2m48)

computed cost: 0



------END END END------------------------------------------------------------------------------------------------------------------




