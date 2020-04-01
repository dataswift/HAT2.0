FROM openjdk:8-jre
WORKDIR /opt/docker
ADD opt /opt
RUN ["chown", "-R", "daemon:daemon", "."]
USER daemon
EXPOSE 8080
ENV JAVA_OPTS="-Xmx500m -Xms100m"
ENTRYPOINT ["bin/hat", "-Dhttp.port=8080"]
CMD []
