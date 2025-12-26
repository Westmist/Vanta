package org.markeb.actor.impl;

import org.markeb.actor.Actor;
import org.markeb.actor.ActorContext;
import org.markeb.actor.ActorSystem;
import org.markeb.actor.mailbox.Envelope;

import java.util.concurrent.CompletableFuture;

/**
 * 默认 Actor 上下文实现
 */
public class DefaultActorContext implements ActorContext {

    private final Actor self;
    private final ActorSystem system;
    private final Envelope envelope;

    public DefaultActorContext(Actor self, ActorSystem system, Envelope envelope) {
        this.self = self;
        this.system = system;
        this.envelope = envelope;
    }

    @Override
    public Actor self() {
        return self;
    }

    @Override
    public ActorSystem system() {
        return system;
    }

    @Override
    public Actor sender() {
        if (envelope.getSender() != null) {
            return system.lookup(envelope.getSender().actorId())
                    .map(ref -> (Actor) ref)
                    .orElse(null);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void reply(T response) {
        if (envelope.isAsk()) {
            envelope.complete(response);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> future() {
        return (CompletableFuture<T>) envelope.getFuture();
    }

    @Override
    public void scheduleOnce(Object message, long delayMs) {
        ((DefaultActorSystem) system).scheduleOnce(self.actorId(), message, delayMs);
    }

    @Override
    public String schedulePeriodic(Object message, long initialDelayMs, long periodMs) {
        return ((DefaultActorSystem) system).schedulePeriodic(self.actorId(), message, initialDelayMs, periodMs);
    }

    @Override
    public void cancelSchedule(String scheduleId) {
        ((DefaultActorSystem) system).cancelSchedule(scheduleId);
    }

}

