# Proto Message

Client protocol module for player-facing Protobuf message definitions.

## Directory Structure

```
src/main/proto/
閳规壕鏀㈤埞鈧?options.proto    # Custom options (msgId)
閳规壕鏀㈤埞鈧?login.proto      # Login protocols
閳规柡鏀㈤埞鈧?test.proto       # Test protocols
```

## Usage

1. Write `.proto` files in `src/main/proto/` directory
2. Run `mvn compile` to auto-generate Java code
3. Generated code is in `target/generated-sources/protobuf/`

## Protocol Conventions

- Request messages start with `Req`, e.g. `ReqLoginMessage`
- Response messages start with `Res`, e.g. `ResLoginMessage`
- Each message must define `msgId` option

```protobuf
message ReqLoginMessage {
    option (msgId) = 11000;
    string openId = 1;
    string token = 2;
}
```