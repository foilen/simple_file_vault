# Build a release

```
VERSION=xxxx
./build.sh $VERSION && \
./deploy.sh $VERSION
```

# Some definitions

- `Namespace`: A folder to group files together
- `Version`: The version of the file being uploaded
- `Link`: A redirection to a release (eg: `prod`)

# Some features

- Auto cleanup of old versions: You can set a maximum number of versions per namespace with `maxVersionByNamespace`. It won't delete any versions that are tagged.

# Use with docker

The needed environment variables are:
- `CONFIG_FILE`: The path to the configuration file
  - Default: All public access for reading and writing
- `DATA_FOLDER`: The path to the folder where the data will be stored
  - Default: `/tmp/simple-file-vault`

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

Upload a file:
```
cat << EOF > /tmp/test.txt
Hello World
EOF
curl -X POST "http://localhost:8080/example/1/test.txt" --data-binary @/tmp/test.txt
```

Read the file:
```
curl "http://localhost:8080/example/1/test.txt" > /tmp/test2.txt
```

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
    -v $PWD:/local \
    -v /data \
    --env CONFIG_FILE=/local/sample_config.json \
    -p 8080:8080 \
    simple_file_vault:snapshot
```

## Production

```
mkdir -p $HOME/simple_file_vault/data
cat << EOF > $HOME/simple_file_vault/config.json
{
  "maxVersionByNamespace": {
    "example_test": 5
  },
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
            "*"
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

Upload a file:
```
cat << EOF > /tmp/test.txt
Hello World
EOF
curl -u deploy-example:qwerty -X POST "http://localhost:8080/example_test/1/test.txt" --data-binary @/tmp/test.txt
curl -u deploy-all:qwerty -X POST "http://localhost:8080/anything/1/test.txt" --data-binary @/tmp/test.txt
```

Read the file:
```
curl "http://localhost:8080/example_test/1/test.txt" > /tmp/test2.txt
curl -u consumer-all:qwerty "http://localhost:8080/anything/1/test.txt" > /tmp/test2.txt
```

List the files
```
curl "http://localhost:8080/anything/"
curl -u consumer-all:qwerty "http://localhost:8080/anything/"
```

Create tags:
```
curl -u deploy-example:qwerty -X POST "http://localhost:8080/example_test/tags/latest/1"
```

Get version for tag `latest`
```
curl "http://localhost:8080/example_test/tags/latest"
curl "http://localhost:8080/example_test/latest/test.txt"
```
