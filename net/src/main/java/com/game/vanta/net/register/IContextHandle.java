package com.game.vanta.net.register;

@FunctionalInterface
public interface IContextHandle<C extends GameActorContext, M> {
    M invoke(C ctx, M req) throws Throwable;
}
