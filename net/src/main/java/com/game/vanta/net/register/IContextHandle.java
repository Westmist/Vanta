package com.game.vanta.net.register;

@FunctionalInterface
public interface IContextHandle<C extends GameActorContext, M> {
    Object invoke(C ctx, M req) throws Throwable;
}
