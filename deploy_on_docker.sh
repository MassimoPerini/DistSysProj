#!/bin/bash
#docker stop container1 -t 4
docker rmi distsys
mvn install
#cp target/distsys_proj-1.0-SNAPSHOT-jar-with-dependencies.jar Docker/distsys_proj-1.0-SNAPSHOT-jar-with-dependencies.jar
docker build -t distsys . #building image
docker run --rm --name container1 --network br0 distsys
docker run --rm --name container2 --network br0 distsys
docker run --rm --name container3 --network br0 distsys