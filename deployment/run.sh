#!/bin/bash
echo "Running HAT2.0!"
if [ -z "$VCAP_APP_PORT" ]; 
	then SERVER_PORT=5000; 
	else SERVER_PORT="$VCAP_APP_PORT"; 
fi 
echo port is $SERVER_PORT 
./target/universal/stage/bin/hatdex.hat-hatdex.hat.dal -Dhttp.port=$SERVER_PORT
