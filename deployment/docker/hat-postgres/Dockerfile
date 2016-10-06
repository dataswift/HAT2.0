# vim:set ft=dockerfile:
FROM debian:jessie
#FROM gliderlabs/alpine:3.1

# explicitly set user/group IDs
RUN groupadd -r postgres --gid=999 && useradd -r -g postgres --uid=999 postgres

# grab gosu for easy step-down from root
RUN gpg --keyserver pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4
RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates wget && rm -rf /var/lib/apt/lists/* \
	&& wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/1.2/gosu-$(dpkg --print-architecture)" \
	&& wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/1.2/gosu-$(dpkg --print-architecture).asc" \
	&& gpg --verify /usr/local/bin/gosu.asc \
	&& rm /usr/local/bin/gosu.asc \
	&& chmod +x /usr/local/bin/gosu \
	&& apt-get purge -y --auto-remove ca-certificates wget

# make the "en_US.UTF-8" locale so postgres will be utf-8 enabled by default
RUN apt-get update && apt-get install -y locales && rm -rf /var/lib/apt/lists/* \
	&& localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8
ENV LANG en_US.utf8

RUN mkdir /docker-entrypoint-initdb.d

RUN apt-key adv --keyserver ha.pool.sks-keyservers.net --recv-keys B97B0AFCAA1A47F044F244A07FCC7D46ACCC4CF8

ENV PG_MAJOR 9.4
ENV PG_VERSION 9.4.9-0+deb8u1

RUN echo 'deb http://apt.postgresql.org/pub/repos/apt/ jessie-pgdg main' $PG_MAJOR > /etc/apt/sources.list.d/pgdg.list

RUN apt-get update \
	&& apt-get install -y postgresql-common \
	&& sed -ri 's/#(create_main_cluster) .*$/\1 = false/' /etc/postgresql-common/createcluster.conf \
	&& apt-get install -y \
		postgresql-$PG_MAJOR=$PG_VERSION \
		postgresql-contrib-$PG_MAJOR=$PG_VERSION \
	&& rm -rf /var/lib/apt/lists/*

RUN mkdir -p /var/run/postgresql && chown -R postgres /var/run/postgresql

ENV PATH /usr/lib/postgresql/$PG_MAJOR/bin:$PATH
ENV PGDATA /var/lib/postgresql/data
VOLUME /var/lib/postgresql/data

ADD init/docker-entrypoint.sh /

ENTRYPOINT ["/docker-entrypoint.sh"]

#EXPOSE 5432
CMD ["postgres"]

#------------------------HAT SPECIFIC-------------------------------

#Setup environment variables used by docker-deploy-db.sh in docker
# Here just the default values get set, can be overriden by providing --env parameters
ENV HAT_HOME=/opt/hat
ENV DATABASE=hat20
ENV DBUSER=hat20
ENV DBPASS=hat20

ENV HAT_OWNER=bob@gmail.com
ENV HAT_OWNER_ID=5974832d-2dc1-4f49-adf1-c6d8bc790274
ENV HAT_OWNER_NAME=bob
ENV HAT_OWNER_PASSWORD=pa55word

ENV HAT_PLATFORM=hatdex.org
ENV HAT_PLATFORM_ID=47dffdfd-55e8-4575-836c-151e30bb5a50
ENV HAT_PLATFORM_NAME=hatdex
ENV HAT_PLATFORM_PASSWORD_HASH='$2a$04$oL2CXTHzB..OekL1z8Vijus3RkHQjSsbkAYOiA5Rj.7.6GA7a4qAq'

#Required by the postgres container (docker-entrypoint.sh)
#Also sets up authMethod=md5
#Check : https://github.com/docker-library/postgres/blob/ed23320582f4ec5b0e5e35c99d98966dacbc6ed8/9.4/docker-entrypoint.sh
ENV POSTGRES_PASSWORD=$DBPASS
ENV POSTGRES_USER=$DBUSER
ENV POSTGRES_DB=$DBPASS

#Get HAT sql files sql.
RUN mkdir -p /opt/hat
ADD required/ /opt/hat/

#This will initialize the database
ADD init/docker-deploy-db.sh /docker-entrypoint-initdb.d/

