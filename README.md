# Build a release

```
VERSION=xxxx
./build.sh $VERSION
```

# Use with docker

The needed environment variables are:
- `CONFIG_FILE`: The path to the configuration file
  - Default: All public access
- `DATA_FOLDER`: The path to the folder where the data will be stored
  - Default: `/data`

## Development / Testing

Start the container:
```
./build.sh snapshot && \
docker run --rm -ti \
    -v /data \
    -p 8080:8080 \
    simple_file_vault:snapshot
```

Go on http://localhost:8080/


## Production
Start the container:
```
mkdir -p $HOME/simple_file_vault/data
cat << EOF > $HOME/simple_file_vault/config.json
{
 TODO +++++
}
EOF

docker pull foilen/simple_file_vault:latest ; \
docker run -d --restart always \
    -v $HOME/simple_file_vault/:/mount \
    --user $(id -u):$(id -g) \
    --env CONFIG_FILE=/mount/config.json \
    --env DATA_FOLDER=/mount/data \
    -p 8080:8080 \
    --name simple_file_vault \
    foilen/simple_file_vault:latest && \
docker logs -f simple_file_vault
```
