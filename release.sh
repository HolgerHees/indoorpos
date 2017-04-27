#!/bin/bash

cd `dirname $0`

git pull

../apache-maven-3.3.1/bin/mvn clean install

echo "install ../apache-tomcat-8.0.21/webapps/ROOT.war"
mv target/waywise.war ../apache-tomcat-8.0.21/webapps/ROOT.war

# to avoid memory leaks use a shared mysql lib
echo "install ../apache-tomcat-8.0.21/lib/mysql-connector*"
rm ../apache-tomcat-8.0.21/lib/mysql-connector*
cp target/waywise/WEB-INF/lib/mysql-connector* ../apache-tomcat-8.0.21/lib/

echo "restart tomcat"
../apache-tomcat-8.0.21/bin/catalina.sh stop 5 -force

../apache-tomcat-8.0.21/bin/catalina.sh start

tail -f ../apache-tomcat-8.0.21/logs/catalina.out