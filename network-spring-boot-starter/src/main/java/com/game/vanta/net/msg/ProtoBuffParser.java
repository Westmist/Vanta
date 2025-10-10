package com.game.vanta.net.msg;


import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ProtoBuffParser implements IGameParser<Message> {

    private static final Logger log = LoggerFactory.getLogger(ProtoBuffParser.class);

    private static final String MSG_ID_OPTION = "msgId";

    private final Map<Integer, Parser<? extends Message>> parserMap = new HashMap<>();

    private static final Map<Class<? extends Message>, Integer> messageToMsgId = new HashMap<>();

    public void register(int id, Class<? extends Message> messageClazz, Parser<? extends Message> parser) {
        if (parserMap.containsKey(id)) {
            throw new IllegalArgumentException("Message ID already registered: " + id);
        }
        parserMap.put(id, parser);
        messageToMsgId.put(messageClazz, id);
        log.info("Registered message: {} with msgId: {}", messageClazz.getName(), id);
    }

    @Override
    public int messageId(Class<Message> message) {
        Integer msgId = messageToMsgId.get(message);
        if (msgId == null) {
            throw new IllegalArgumentException("Message class not registered: " + message.getName());
        }
        return msgId;
    }

    @Override
    public Class<Message> messageClazz() {
        return Message.class;
    }

    @Override
    public Message parseFrom(int id, byte[] bodyBytes) {
        Parser<? extends Message> parser = parserMap.get(id);
        if (parser == null) {
            throw new IllegalArgumentException("Unknown message id: " + id);
        }
        try {
            return parser.parseFrom(bodyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse message with id " + id, e);
        }
    }

    @Override
    public void register(Class<Message> clazz) {
        int msgId = findMsgIdFromClass(clazz);
        Parser<? extends Message> parserFromClass = findParserFromClass(clazz);
        register(msgId, clazz, parserFromClass);
    }

    private int findMsgIdFromClass(Class<? extends Message> messageClazz) {
        try {
            Method method = messageClazz.getMethod("getDescriptor");
            Descriptors.Descriptor descriptor = (Descriptors.Descriptor) method.invoke(null);
            DescriptorProtos.MessageOptions opts = descriptor.toProto().getOptions();

            Map<Descriptors.FieldDescriptor, Object> allFields = descriptor.getOptions().getAllFields();
            for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : allFields.entrySet()) {
                Descriptors.FieldDescriptor fd = entry.getKey();
                Object value = entry.getValue();
                // 判断是不是我们关心的 msgId 扩展
                if (fd.isExtension() && MSG_ID_OPTION.equals(fd.getName()) && value instanceof Integer) {
                    return (Integer) value;
                }
            }

            for (DescriptorProtos.UninterpretedOption uo : opts.getUninterpretedOptionList()) {
                if (uo.getNameCount() == 1 && MSG_ID_OPTION.equals(uo.getName(0).getNamePart())) {
                    if (uo.hasPositiveIntValue()) {
                        return (int) uo.getPositiveIntValue();
                    }
                    if (uo.hasNegativeIntValue()) {
                        return (int) uo.getNegativeIntValue();
                    }
                    if (uo.hasIdentifierValue()) {
                        return Integer.parseInt(uo.getIdentifierValue());
                    }
                }
            }
            throw new IllegalArgumentException(MSG_ID_OPTION + " option not found in " + messageClazz.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + MSG_ID_OPTION + ": " + messageClazz.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Parser<? extends Message> findParserFromClass(Class<? extends Message> messageClazz) {
        try {
            Field field = messageClazz.getDeclaredField("PARSER");
            field.setAccessible(true);
            return (Parser<? extends Message>) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get PARSER field from " + messageClazz.getName(), e);
        }
    }

}
