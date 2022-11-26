## lightweight-db-images

This repository contains the necessary files to build the lightweight images for ontop basic tests with different SQL engines.

### Start and stop all DB-containers

To start the containers:
1. Create the .env file in which all the passwords are specified:
  ```
  cp .env.example .env
  ```
2. Start the Docker container:
  ```
  docker-compose -f docker-compose.lightweight.yml up
  ```
The container is run on the foreground and can be stopped by pressing CTRL-C.

3. Stop and remove the containers:
  ```
  docker-compose -f docker-compose.public.yml down
  ```

### Build images from local Dockerfiles
1. Create the .env file in which all the passwords are specified:
  ```
  cp .env.example .env
  ```
2. Build the docker images:
  ```
  docker-compose build 
  ```
3. Start the Docker container:
  ```
  docker-compose up
  ```
The container is run on the foreground and can be stopped by pressing CTRL-C.

4. Stop and remove the containers:
  ```
  docker-compose down
  ```