# JAVA_OPTS
DOCKER_SBT_JAVA_OPT="-Dapplication.mode=DEV -Denv=stage -Dhttp.port=9000 -Dhttps.port=9001 -Dpidfile.path=/dev/null -Dplay.server.pidfile.path=/dev/null -Dconfig.resource=docker.conf"
DEV_SBT_JAVA_OPT="-Dapplication.mode=DEV -Denv=stage -Dhttp.port=9000 -Dhttps.port=9001 -Dconfig.resource=dev.conf"
PROD_SBT_JAVA_OPT="-Dhttp.port=9000 -Dhttps.port=9001 -Dplay.server.pidfile.path=/dev/null -Dconfig.resource=application.conf compile"

clean:
	$(info > Running sbt clean)
	sbt clean

docker:
	$(info > Building a docker image to use with docker-compose.)
	@sbt ${DOCKER_SBT_JAVA_OPT} docker:publishLocal

dev:
	$(info > Building HAT2.0 for development.)
	@sbt ${DEV_SBT_JAVA_OPT} compile

run-dev:
	$(info > Running HAT2.0 in development.)
	@sbt "project hat" "run -Dconfig.resource=dev.conf"


prod:
	$(info > Building HAT2.0 for production.)
	@sbt ${PROD_SBT_JAVA_OPT} compile

# Default is dev
.DEFAULT_GOAL := dev
