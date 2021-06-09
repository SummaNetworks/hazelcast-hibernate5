# Development notes:



## How to deploy manually hibernate53 module:
>mvn deploy:deploy-file -DgroupId=com.hazelcast -DartifactId=hazelcast-hibernate53 \
 -Dversion=1.3.3-SUMMA-0.0.1 -Dpackaging=jar -Dfile=<path-to-jar> -DpomFile=<path-to-pom> \
 -DrepositoryId=onevox -Durl=dav:https://devel.onevox.io/repository
 
 
# Snapshot deploy (in order that repo works, all artifacts must be deployed):
>mvn deploy:deploy-file -DgroupId=com.hazelcast -DartifactId=hazelcast-hibernate5-parent -Dversion=1.3.3-SUMMA-0.0.2-SNAPSHOT -Dpackaging=pom -Dfile=pom.xml -DpomFile=pom.xml -DrepositoryId=onevox -Durl=dav:https://devel.onevox.io/repository-snapshots
>mvn deploy:deploy-file -DgroupId=com.hazelcast -DartifactId=hazelcast-hibernate5 -Dversion=1.3.3-SUMMA-0.0.2-SNAPSHOT -Dpackaging=jar -Dfile=hazelcast-hibernate5/target/hazelcast-hibernate5-1.3.3-SUMMA-0.0.2-SNAPSHOT.jar -DpomFile=pom.xml -DrepositoryId=onevox -Durl=dav:https://devel.onevox.io/repository-snapshots
>mvn deploy:deploy-file -DgroupId=com.hazelcast -DartifactId=hazelcast-hibernate52 -Dversion=1.3.3-SUMMA-0.0.2-SNAPSHOT -Dpackaging=jar -Dfile=hazelcast-hibernate52/target/hazelcast-hibernate52-1.3.3-SUMMA-0.0.2-SNAPSHOT.jar -DpomFile=pom.xml -DrepositoryId=onevox -Durl=dav:https://devel.onevox.io/repository-snapshots

>mvn deploy:deploy-file -DgroupId=com.hazelcast -DartifactId=hazelcast-hibernate53 \
 -Dversion=1.3.3-SUMMA-0.0.2-SNAPSHOT -Dpackaging=jar -Dfile=hazelcast-hibernate53/target/hazelcast-hibernate53-1.3.3-SUMMA-0.0.2-SNAPSHOT.jar \ 
 -DpomFile=hazelcast-hibernate53/pom.xml \
 -DrepositoryId=onevox -Durl=dav:https://devel.onevox.io/repository-snapshots
 
 
 
## How to release:
 >mvn -B release:prepare -Darguments="-Dmaven.test.skip=true"
 
 >mvn release:perform -Darguments="-Dgpg.skip"
 
### Inidicating TAG and version information.
 >mvn -B -Dtag=TAG-VERSION release:prepare -DreleaseVersion=RELEASE-VERSION -DdevelopmentVersion=NEXT-SNAPSHOT-VERSION -Darguments="-Dmaven.test.skip=true"
 
 Ej:
 >mvn -B -Dtag=1.3.3-SUMMA-0.0.2 release:prepare -DreleaseVersion=1.3.3-SUMMA-0.0.2 -DdevelopmentVersion=1.3.3-SUMMA-0.0.3-SNAPSHOT -Darguments="-Dmaven.test.skip=true"
 
 And then:
 >mvn release:perform -Darguments="-Dgpg.skip -Dmaven.test.skip=true -Dmaven.javadoc.skip=true"

 
 
# Añadir en el setting.xml
```
 <settings>
    <servers>
       <server>
          <id>git</id>
          <username>myUser</username>
          <password>myPassword</password>
       </server>
    </servers>
 </settings

```


#If you have to repeat, delete tag just created in summa remote repository. If it is called origin, change:
>git tag -d TagName && git push summa :TagName