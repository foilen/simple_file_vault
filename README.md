# Build a release

```
VERSION=xxxx
./build.sh $VERSION
```

# Some definitions

- `Namespace`: A folder to group files together
- `Version`: The version of the file being uploaded
- `Link`: A redirection to a release (eg: `prod`)

# Use with docker

The needed environment variables are:
- `CONFIG_FILE`: The path to the configuration file
  - Default: All public access for reading and writing
- `DATA_FOLDER`: The path to the folder where the data will be stored
  - Default: `/data`

## Development / Testing

### Public access

Start the container:
```
./build.sh snapshot && \
docker run --rm -ti \
    -v /data \
    -p 8080:8080 \
    simple_file_vault:snapshot
```

Go on http://localhost:8080/

## Some security

In the sample configuration:
- `deploy-all` can write to all namespaces
- `deploy-example` can write to `example_*` namespace
- `consumer-all` can read all namespaces
- `public (non-authenticated users)` can read `example_*` namespace only

Start the container:
```
./build.sh snapshot && \
docker run --rm -ti \
    -v sample-config.json:/config.json \
    -v /data \
    --env CONFIG_FILE=/config.json \
    -p 8080:8080 \
    simple_file_vault:snapshot
```

## Production

```
mkdir -p $HOME/simple_file_vault/data
cat << EOF > $HOME/simple_file_vault/config.json
{
  "public": {
    "readNamespaces": [
      "example_*"
    ]
  },
  "users": {
    "deploy-all": {
      "password": "qwerty",
      "writeNamespaces": [
        "*"
      ]
    },
    "deploy-example": {
      "password": "qwerty",
      "writeNamespaces": [
        "example_*"
      ]
    },
    "consumer-all": {
      "password": "qwerty",
        "readNamespaces": [
            "**"
        ]
    }
  }
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
