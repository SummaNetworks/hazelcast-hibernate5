# Development notes:



## How to deploy manually hibernate53 module:
mvn deploy:deploy-file -DgroupId=com.hazelcast -DartifactId=hazelcast-hibernate53 \
 -Dversion=1.3.3-SUMMA-0.0.1 -Dpackaging=jar -Dfile=<path-to-jar> -DpomFile=<path-to-pom> \
 -DrepositoryId=onevox -Durl=dav:https://devel.onevox.io/repository