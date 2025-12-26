# Proto Notice

Internal protocol module for inter-service Protobuf message definitions.

## Directory Structure

```
src/main/proto/
閳规壕鏀㈤埞鈧?options.proto    # Custom options (noticeId)
閳规壕鏀㈤埞鈧?forward.proto    # Message forwarding protocols
閳规柡鏀㈤埞鈧?session.proto    # Session management protocols
```

## Usage

1. Write `.proto` files in `src/main/proto/` directory
2. Run `mvn compile` to auto-generate Java code
3. Generated code is in `target/generated-sources/protobuf/`

## Protocol Conventions

- Internal notice messages end with `Notice`, e.g. `ForwardNotice`
- Each message must define `noticeId` option
- noticeId starts from 20000, separate from client msgId

```protobuf
message ForwardNotice {
    option (noticeId) = 20001;
    string playerId = 1;
    int32 msgId = 2;
    bytes payload = 3;
}
```

## Protocol Categories

| File | Purpose | ID Range |
|------|---------|----------|
| forward.proto | Message forwarding | 20001-20099 |
| session.proto | Session management | 20101-20199 |