package com.game.vanta.net.register;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface IContextHandle<C extends GameActorContext, M> {

    @Nullable
    M invoke(C ctx, M req) throws Throwable;
}
