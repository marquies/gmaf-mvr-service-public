[![Maven Package](https://github.com/marquies/gmaf-mvr-service/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/marquies/gmaf-mvr-service/actions/workflows/maven-publish.yml)
# GMAF Service

GMAF Service provides a web service with REST and SOAP APIs.


## Build

Setup Maven settings.xml to access remote repository

```shell
mvn clean package
```
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
## Run

```shell
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.management/javax.management.openmbean=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED --illegal-access=permit -Djava.library.path=path_to_open_cv - jar path_to_jar
```


