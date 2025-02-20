# Webhook Vector Ingest
This project contains a simple Rest API for a kind of "poor mans RAG ingestion". It supports the almost 
all document types with the following embedding providers...

- OpenAI
- Pinecone
- Ollama

... and these Vector databases...

- Milvus
- Pinecone

... with these transformations: 

- HTML markup removal
- Markdown markup removal
- Chunking by paragraph, work, sentence etc 

## Quickstart
The service is packaged as a docker container, and you can configure it via environment variables, like so: 

```
docker run -it \
    -e OLLAMA_URI="https://..." \
    -e MILVUS_URI="https://..." \
    -e MILVUS_TOKEN="..." \
    -e MILVUS_DATABASE="..." \
    -e MILVUS_COLLECTION="..." \
    ghcr.io/fungrim/webhook-vector-ingest:0.0.11
```

However, it is easier to configure the service with a custom configuration file (see below), that 
you can mount like this: 

```
docker run -it \
    --volume ${PWD}/myconfig.yaml:/deployments/config/application.yaml \
    -e INGEST_CONFIG=/deployments/config/application.yaml \
    ghcr.io/fungrim/webhook-vector-ingest:0.0.11
```

## Swagger UI
By default, a Swagger UI is provided at `/q/swagger-ui` and can be used for simple testing. 

## Configuration

### Environment variables vs YAML
This project uses Quarkus as a framework, and as such, anything that is configurable via a YAML file is also available as environment variables. For example, 
this value from a YAML file...

```yaml
milvus:
  uri: https://mylittleuri.com
```

.... can also be set using this environment variable: 

```
MILVUS_URI=https://mylittleuri.com
```

### Example configuration file

```yaml
quarkus:
  swagger-ui:
    always-include: false
  config:
    log:
      values: true
  log:
    category:
      "net.larsan.ai":
        level: debug
      "io.smallrye.config":
        level: info

storage:
  default-storage:
    provider: "milvus"

embedding:
  default-model: 
    provider: "ollama"
    name: "mxbai-embed-large:latest"

milvus:
  uri: "..."
  token: "..."
  database: "..."
  collection: "..."

ollama:
  uri: "..."

openai:
  api-key: "..."

pinecone:
  index: "..."
  api-key: "..."
  uri: "..."
  
chunking:
  strategy: "PARAGRAPH"
  limits:
    max-size: 512
    max-overlap: 128

metadata:
  content-type:
    enabled: true
  file-name:
    enabled: true
  html:
    include-headers:
      - "title"

security: 
  api-keys:
    - "..."
```

### Providers
For providers, when something is "mandatory" it means that it is mandatory only if you plan to use that provider. You do *not* have to configure, say, Milvus to use Pinecode. But if you
are planning on using Milvus, then all Milvus configuration options are mandatory. 

#### Milvus

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| milvus.uri | string | yes | n/a | The URI of your Milvus installation |
| milvus.token | string | yes | n/a | Milvus access token |
| milvus.database | string | yes | n/a | The Milvus database to use |
| milvus.collection | string | yes | n/a | The Milvus collection to use |
| milvus.secure | boolean | no | false | If this is an HTTPS connection |

#### Pinecone

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| pinecone.index | string | yes | n/a | Index name for storage |
| pinecone.api-key | string | yes | n/a | Pinecode access token |
| pinecone.uri | string | yes | n/a | Database host URI |

#### Ollama

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| ollama.uri | string | yes | n/a | The ollama access URI |

#### OpenAI

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| openai.api-key | string | yes | n/a | The OpenAI access token |

### Default providers
You can specify default providers to use. This can be overridden by the Rest API. 

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| storage.default-storage.provider | string | no | n/a | Values: "milvus" or "pinecone" |
| embedding.default-model.provider | string | no | n/a | Values: "ollama", "openai" or "pinecone" |
| embedding.default-model.name | string | no | n/a | The model name, e.g. "mxbai-embed-large:latest" | 

### Chunking
Chunking can be configured by strategy:

- `WORD`
- `SENTENCE`
- `PARAGRAPH`
- `LINE`
- `CHARACTER`

Each strategy also has to limits: 

- `max-size` - The max length of a chunk
- `max-overlap` - The max overlap between chunks

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| chunking.strategy | string | no | n/a | See above for allowed values |
| chunking.limits.max-size | int | no | 512 | Max chunk size |
| chunking.limits.max-overlap | int | no | 0 | Max overlap size |

### Metadata 
The service can attempt to extract metadata from the content. 

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| metadata.content-type.enabled | boolean | false | true | Try to extract content type if not given |
| metadata.content-type.name | string | false | "content-type" | The field name to store the metadata as |
| metadata.file-name.enabled | boolean | false | true | Try to extract the document file name |
| metadata.file-name.name | string | false | "file-name" | The field name to store the metadata as |
| metadata.html.include-headers | list(string) | false  | n/a | A list of HTML document headers to include in the metadata |

### Security
The API can be protected by API keys. If no API key is configured the service is open to the public. 

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| security.api-keys | list(string) | no | n/a | Valid API keys |

## Milvus DB scheme
By default the service will assume a Milvus scheme that looks like this: 

| Column name | Column type 
| --- | --- |
| id | varchar |
| vector | floatvector |
| metadata | json |

The `id` column cannot be renmamed, but the vector column and the metadata column can. 

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| milvus.json-metadata | boolean | no | true | Set to false to flatten metadata to separate columns |
| milvus.json-metadata-field-name | string | no | "metadata" | Column name of the JSON metadata |
| milvus.vector-field-name | string | no | "vector" | Column name of the vector |

## Request object
The request is an HTTP PUT with JSON object that contains the data to ingest, and optionally metadata and 
storage and embedding model overrides. 

| Key | Type | Mandatory | Default value | Comment
| --- | --- | --- | --- | --- |
| data | object | true | n/a | The document data |
| data.id | string | false | new(nanoid) | Document ID, if known |
| data.contentType | string | false | n/a | The content type, if known |
| data.fileName | string | false | n/a | The file name, if known |
| data.metadata | map(string, string) | false | n/a | Additional metadata as a dictionary |
| data.encoding | string | false | "UTF8" | The `content` encoding, values: "BASE64" or "UTF8" |
| data.content | string | true | n/a | The content, use `encoding: BASE64` for binary files |
| embedding | object | false | n/a | Optional embedding model |
| embedding.provider | string | true | n/a | Emnbedding mode provider |
| embedding.name | string | false | n/a | Embedding model name |
| storage | object | false | n/a | Optional storage provider |
| storage.provider | string | true | n/a | Values: "milvus" or "pinecone" |
| storage.namespace | string | false | n/a | Milvus partition or Pionecone namespace, optional |
| chunking | object | false | n/a | Optional chunking config |
| chunking.strategy | string | true | n/a | See config above for allowed values |
| chunking.limits | object | true | n/a | Chunking limits |
| chunking.limits.maxSize | int | true | n/a | Max chunk size |
| chunking.limits.maxOverlap | int | true | n/a | Max overlap size |

### Example request objects

#### With default config
If the embedding model and the vector storage have been configured using the `default-model` and 
`default-storage` properties all you need to provide is the `content` itself:

```json
{
  "data": {
    "content": "This is my text content"
  }
}
```

Note that this content is `UTF8`, i.e. is assumed to be clear text in the JSON, you you need to ingest binary documents (word, open office, PDF etc), it will
need to be `BASE64` encoded first: 

```json
{
  "data": {
    "content": "...",
    "encoding": "BASE64"
  }
}
```

#### With default config and extra metadata

```json
{
  "data": {
    "contentType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "fileName": "my-little-document.docx",
    "content": "...",
    "encoding": "BASE64",
    "metadata": {
      "created": "2024-12-01"
    }
  }
}
```

#### With specific config
If the `default-model` or `default-storage` are not configured, or if you want to override them, you can do so in the
request, for example: 

```json
{
  "data": {
    "contentType": "application/pdf",
    "fileName": "my-little-document.pdf",
    "content": "...",
    "encoding": "BASE64"
  },
  "embedding": {
    "provider": "ollama",
    "name": "mxbai-embed-large:latest"
  },
  "storage": {
    "provider": "mivlus"
  }
}
```

#### With specific chunking
You can also override the chunking configuration

```json
{
  "data": {
    "contentType": "application/json",
    "content": "...",
    "encoding": "BASE64"
  },
  "embedding": {
    "provider": "ollama",
    "name": "mxbai-embed-large:latest"
  },
  "chunking": {
    "strategy": "PARAGRAPH",
    "limits": {
      "maxSize": 1024,
      "maxOverlap": 256
    }
  }
}
```
