#!/bin/bash

BUCKET="hat-cloud-formation-eu"
VERSION=`git log --format="%H" -n 1`
APPLICATION_NAME="hat"

echo "Build"
sbt "project hat" docker:stage

echo "Create package"
cp -r   deployment/elasticBeanstalk/Dockerrun.aws.json  deployment/elasticBeanstalk/.ebextensions hat/target/docker/stage
cd hat/target/docker/stage
zip -q -r ${APPLICATION_NAME}-${VERSION}.zip * .ebextensions

echo "Upload package"
aws s3api put-object --bucket $BUCKET --key apps/${APPLICATION_NAME}-${VERSION} --body ${APPLICATION_NAME}-${VERSION}.zip

echo "Cleanup"
rm ${APPLICATION_NAME}-${VERSION}.zip
