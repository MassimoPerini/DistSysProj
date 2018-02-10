FROM openjdk:8
MAINTAINER massimo
COPY ./target/distsys_proj-1.0-SNAPSHOT-jar-with-dependencies.jar /home/distsys.jar
#WORKDIR /usr/src/myapp
CMD ["java","-jar","/home/distsys.jar", "/home/distsys.jar"]
