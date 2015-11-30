# HAT docker scripts

Create the docker hat and hat-postgres images:

    deployment/docker/docker-build-images.sh

Test both images (cautious that it stops and removes running docker images!):  

    deployment/docker/docker-test-images.sh

Pushes both images to their respective docker hub repositories:

    deployment/docker/docker-push-images.sh
