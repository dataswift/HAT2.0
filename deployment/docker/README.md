# HAT docker scripts

Create the docker hat and hat-postgres images:

    deployment/docker/docker-build-images.sh

Test both images (*WARNING that it stops and removes all hat docker images!*):  

    deployment/docker/docker-test-images.sh

Pushes both images to their respective docker hub repositories:

    deployment/docker/docker-push-images.sh

## Testing docker images

If you want to give it a test drive just execute:

    docker-test-images.sh

It downloads the latest images from docker-hub so you dont need to build them yourself.

It starts 3 hat and 3 hat-postgres images. All accessible on localhost with different ports.

Find the corresponding port with: 

    docker ps

And finally test in your browser or better still - postman (you might need to set port, username and password accordingly):

    http://127.0.0.1:3003/users/access_token?username=junior@gmail.com&password=junior
