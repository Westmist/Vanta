package org.markeb.common.event;


import org.springframework.context.ApplicationEvent;

public class NetworkStartedEvent extends ApplicationEvent {
    public NetworkStartedEvent(Object source) {
        super(source);
    }
}
