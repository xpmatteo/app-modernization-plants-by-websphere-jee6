#IMAGE: Get the base image for Liberty
FROM icr.io/appcafe/open-liberty:full-java8-openj9-ubi


# Add MySQL  Type 4 JDBC driver
RUN mkdir -p /opt/ol/wlp/usr/shared/resources/mysql
COPY wlp/usr/shared/resources/mysql/mysql-connector-java-5.1.38.jar /opt/ol/wlp/usr/shared/resources/mysql/
USER root
RUN chown 1001:0 /opt/ol/wlp/usr/shared/resources/mysql/*.jar
USER default

# CONFIG: Add in server.xml
COPY wlp/config/server.xml /config/server.xml
USER root
RUN chown 1001:0 /config/server.xml
USER default

RUN configure.sh

ADD target/plants-by-websphere-jee6-mysql.ear /opt/ol/wlp/usr/servers/defaultServer/apps
