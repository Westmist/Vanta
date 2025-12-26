# Markeb API

Protocol definition module containing all Protobuf message definitions.

## Module Structure

```
markeb-api/
閳规壕鏀㈤埞鈧?proto-message/     # Client protocol (player-facing)
閳?  閳规柡鏀㈤埞鈧?src/main/proto/
閳?      閳规壕鏀㈤埞鈧?options.proto
閳?      閳规壕鏀㈤埞鈧?login.proto
閳?      閳规柡鏀㈤埞鈧?test.proto
閳规柡鏀㈤埞鈧?proto-notice/      # Internal protocol (inter-service)
    閳规柡鏀㈤埞鈧?src/main/proto/
        閳规壕鏀㈤埞鈧?options.proto
        閳规壕鏀㈤埞鈧?forward.proto
        閳规柡鏀㈤埞鈧?session.proto
```

## Submodule Description

| Module | Description | ID Range |
|--------|-------------|----------|
| proto-message | Client-server protocol | 10000-19999 |
| proto-notice | Inter-service protocol | 20000-29999 |

## Usage

Add dependencies in other modules:

```xml
<!-- Client protocol -->
<dependency>
    <groupId>org.markeb</groupId>
    <artifactId>proto-message</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>

<!-- Internal protocol -->
<dependency>
    <groupId>org.markeb</groupId>
    <artifactId>proto-notice</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Build

```bash
mvn compile
```

Generated Java code is in each submodule's `target/generated-sources/protobuf/` directory.