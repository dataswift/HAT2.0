# JAVA_OPTS
DEV_DOCKER_SBT_JAVA_OPT="-Dapplication.mode=DEV -Denv=stage -Dhttp.port=9000 -Dhttps.port=9001 -Dpidfile.path=/dev/null -Dplay.server.pidfile.path=/dev/null -Dconfig.resource=docker.conf"
PROD_DOCKER_SBT_JAVA_OPT="-Denv=prod -Dhttp.port=9000 -Dpidfile.path=/dev/null -Dplay.server.pidfile.path=/dev/null -Dconfig.resource=application.conf"

DEV_SBT_JAVA_OPT="-Dapplication.mode=DEV -Denv=stage -Dhttp.port=9000 -Dhttps.port=9001 -Dconfig.resource=dev.conf"
TEST_SBT_JAVA_OPT="-Dhttp.port=9000 -Dhttps.port=9001 -Dplay.server.pidfile.path=/dev/null -Dconfig.resource=application.test.conf"
PROD_SBT_JAVA_OPT="-Denv=prod -Dhttp.port=9000 -Dhttps.port=9001 -Dplay.server.pidfile.path=/dev/null -Dconfig.resource=application.conf"

prod:
	$(info > Running sbt clean.)
	sbt ${PROD_SBT_JAVA_OPT} compile


clean:
	$(info > Running sbt clean.)
	sbt clean


test:
	$(info > Building HAT2.0 for test.)
	@sbt ${TEST_SBT_JAVA_OPT} compile


docker-prod:
	$(info > Building a docker image to use in production.)
	@sbt ${PROD_DOCKER_SBT_JAVA_OPT} docker:stage


docker-dev:
	$(info > Building a docker image to use with docker-compose.)
	@sbt ${DEV_DOCKER_SBT_JAVA_OPT} docker:publishLocal


dev:
	$(info > Building HAT2.0 for development.)
	@sbt ${DEV_SBT_JAVA_OPT} compile


run-dev:
	$(info > Running HAT2.0 in development.)
	@sbt "project hat" "run -Dconfig.resource=dev.conf"


# List the targets for users
.PHONY: list
list:
	@$(MAKE) -pRrq -f $(lastword $(MAKEFILE_LIST)) : 2>/dev/null | awk -v RS= -F: '/^# File/,/^# Finished Make data base/ {if ($$1 !~ "^[#.]") {print $$1}}' | sort | egrep -v -e '^[^[:alnum:]]' -e '^$@$$'

# Default is dev
.DEFAULT_GOAL := dev
