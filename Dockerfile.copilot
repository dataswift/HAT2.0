FROM adoptopenjdk/openjdk11:jdk-11.0.6_10-alpine

ARG FILE_PATH
ENV FILE_PATH=$FILE_PATH
ARG STRING
ENV STRING=$STRING
ARG DELIMITER
ENV DELIMITER=$DELIMITER
ARG KEYWORD
ENV KEYWORD=$KEYWORD

ARG SBT_VERSION=1.3.10

RUN set -x \
  && apk --update add --no-cache --virtual .build-deps curl \
  && ESUM="3060065764193651aa3fe860a17ff8ea9afc1e90a3f9570f0584f2d516c34380" \
  && SBT_URL="https://github.com/sbt/sbt/releases/download/v1.3.10/sbt-1.3.10.tgz" \
  && apk add bash \
  && curl -Ls ${SBT_URL} > /tmp/sbt-${SBT_VERSION}.tgz \
  && sha256sum /tmp/sbt-${SBT_VERSION}.tgz \
  && (echo "${ESUM}  /tmp/sbt-${SBT_VERSION}.tgz" | sha256sum -c -) \
  && tar -zxf /tmp/sbt-${SBT_VERSION}.tgz -C /opt/ \
  && sed -i -r 's#run \"\$\@\"#unset JAVA_TOOL_OPTIONS\nrun \"\$\@\"#g' /opt/sbt/bin/sbt \
  && apk del --purge .build-deps \
  && rm -rf /tmp/sbt-${SBT_VERSION}.tgz /var/cache/apk/*


ENV PATH="/opt/sbt/bin:$PATH" \
    JAVA_OPTS="-XX:+UseContainerSupport -Dfile.encoding=UTF-8" \
    SBT_OPTS="-Xmx2048M -Xss2M"

WORKDIR /app
ADD . /app

RUN ["chmod", "-R", "777", "start.sh"]

CMD ./start.sh

