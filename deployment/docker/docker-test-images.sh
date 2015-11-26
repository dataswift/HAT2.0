docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

port=3001

for name in jorge nichola junior; do

  docker run\
    -e "DATABASE=$name"\
    -e "DBUSER=$name"\
    -e "DBPASS=$name"\
    -e "POSTGRES_PASSWORD=$name"\
    -e "POSTGRES_USER=$name"\
    -e "POSTGRES_DB=$name"\
    -e "HAT_OWNER=$name@gmail.com"\
    -e "HAT_OWNER_NAME=$name"\
    -e "HAT_OWNER_PASSWORD=$name"\
    -d --name hat-postgres-$name 4knahs/hat-postgres

  docker run\
    -e "DATABASE=$name"\
    -e "DBUSER=$name"\
    -e "DBPASS=$name"\
    -e "POSTGRES_PASSWORD=$name"\
    -e "POSTGRES_USER=$name"\
    -e "POSTGRES_DB=$name"\
    -e "HAT_OWNER=$name@gmail.com"\
    -e "HAT_OWNER_NAME=$name"\
    -e "HAT_OWNER_PASSWORD=$name"\
    -d --name hat-$name --link hat-postgres-$name -p $port:8080 4knahs/hat

   echo -n "The hat-$name is linked to:"
   docker inspect -f "{{ .HostConfig.Links }}" hat-$name

   echo -n "The hat-$name IP is:"
   ip=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' hat-$name)
   echo "$ip:$port"
   #echo "The hat-postgres-$name IP is:"
   #pg=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' hat-postgres-$name)
   #echo $pg

  port=$((port+1))
done 

echo "Running processes:"
docker ps