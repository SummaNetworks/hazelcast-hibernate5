# Development notes:



## How to deploy manually hibernate53 module:
>mvn deploy:deploy-file -DgroupId=com.hazelcast -DartifactId=hazelcast-hibernate53 \
 -Dversion=1.3.3-SUMMA-0.0.1 -Dpackaging=jar -Dfile=<path-to-jar> -DpomFile=<path-to-pom> \
 -DrepositoryId=onevox -Durl=dav:https://devel.onevox.io/repository
 
 
 
 
## How to release:
 >mvn -B release:prepare -Darguments="-Dmaven.test.skip=true"
 
 >mvn release:perform -Darguments="-Dgpg.skip"
 
### Inidicating TAG and version information.
 >mvn -B -Dtag=TAG-VERSION release:prepare -DreleaseVersion=RELEASE-VERSION -DdevelopmentVersion=NEXT-SNAPSHOT-VERSION -Darguments="-Dmaven.test.skip=true"
 
 Ej:
 >mvn -B -Dtag=1.3.3-SUMMA-0.1.0 release:prepare -DreleaseVersion=1.3.3-SUMMA-0.1.0 -DdevelopmentVersion=1.3.3-SUMMA-0.1.1-SNASHOT -Darguments="-Dmaven.test.skip=true"
 
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


#If you have to repeat, delete tag just created:
>git tag -d TagName && git push origin :TagName