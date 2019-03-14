# Ontop-endpoint

## How to use this image

The image `ontop/ontop-endpoint` is for fast setting up an Ontop SPARQL endpoint. 
One can either use `ontop/ontop-endpoint` directly, or create a dedicated image based on this image.   

## Example

Assume that the following files are in the working directory:  

```console
.
├── input
│   ├── university-complete.docker.properties
│   ├── university-complete.obda
│   └── university-complete.ttl
└── jdbc
    └── h2-1.4.196.jar
```

### Use `ontop/ontop-endpoint` directly

You can start an Ontop SPARQL endpoint by using the `ontop/ontop-endpoint` image directly:


#### Linux/Mac
```console
docker run --rm \
-v $PWD/input:/opt/ontop/input \
-v $PWD/jdbc:/opt/ontop/jdbc \
-e ONTOLOGY_FILE=/opt/ontop/input/university-complete.ttl \
-e MAPPING_FILE=/opt/ontop/input/university-complete.obda \
-e PROPERTIES_FILE=/opt/ontop/input/university-complete.docker.properties \
-p 8080:8080 ontop/ontop-endpoint
```

#### Windows
```console
docker run --rm ^
-v %CD%/input:/opt/ontop/input ^
-v %CD%/jdbc:/opt/ontop/jdbc ^
-e ONTOLOGY_FILE=/opt/ontop/input/university-complete.ttl ^
-e MAPPING_FILE=/opt/ontop/input/university-complete.obda ^
-e PROPERTIES_FILE=/opt/ontop/input/university-complete.docker.properties ^
-p 8080:8080 ontop/ontop-endpoint
```


### Create a dedicated image 

In case you need to deploy a self-contained image, you can write a complete `Dockerfile`:

```dockerfile
FROM ontop/ontop-endpoint
WORKDIR /opt/ontop
COPY input/university-complete.ttl input/university-complete.obda input/university-complete.docker.properties input/ 
COPY jdbc/h2-1.4.196.jar jdbc/
EXPOSE 8080
ENTRYPOINT java -cp ./lib/*:./jdbc/* -Dlogback.configurationFile=file:./log/logback.xml \
        it.unibz.inf.ontop.endpoint.OntopEndpointApplication \
        --ontology=input/university-complete.ttl \
        --mapping=input/university-complete.obda \
        --properties=input/university-complete.docker.properties \
        --cors-allowed-origins=http://yasgui.org
```

Then, run the commands to build and run the Docker image:

```console
$ docker build -t my-ontop-endpoint .
$ docker run -it --rm --name my-running-ontop-endpoint -p 8080:8080 my-ontop-endpoint
```

