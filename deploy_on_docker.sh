#!/bin/bash
#docker stop container1 -t 4
docker rmi distsys
mvn install
#cp target/distsys_proj-1.0-SNAPSHOT-jar-with-dependencies.jar Docker/distsys_proj-1.0-SNAPSHOT-jar-with-dependencies.jar
docker build -t distsys .
docker run --rm --name container1 distsys