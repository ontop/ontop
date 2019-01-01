# Build image
```
cd $ONTOP/build/distribution/ontop-dist
docker build -t ghxiao/ontop-endpoint -f  ../src/dockerfiles/Dockerfile .  
```

# Create a container without a name
```
docker run --rm \
-v /Users/xiao/.m2/repository/com/h2database/h2/1.4.196:/opt/ontop/jdbc \
-v /Users/xiao/Development/ontop-demo/university:/obda \
-e ONTOLOGY_FILE=/obda/university-complete.ttl \
-e MAPPING_FILE=/obda/university-complete.obda \
-e PROPERTIES_FILE=/obda/university-complete.docker.properties \
-p 8080:8080 ghxiao/ontop-endpoint
```

```
docker run --name=ontop-endpoint \
-v /Users/xiao/Development/ontop-demo/university:/obda \
-e ONTOLOGY_FILE=/obda/university-complete.ttl \
-e MAPPING_FILE=/obda/university-complete.obda \
-e PROPERTIES_FILE=/obda/university-complete.docker.properties \
-p 8080:8080 ghxiao/ontop-endpoint
```

# Start a container
```
docker start ontop-endpoint
```

