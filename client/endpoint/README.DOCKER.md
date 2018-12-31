# Build image
```
docker build -t ghxiao/ontop-endpoint .  
```

# Create a container
```
docker run --name=ontop-endpoint --rm \
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

