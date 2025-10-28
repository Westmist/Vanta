package com.game.vanta.persistent.checker.abs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.List;

public class CheckRunner implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(CheckRunner.class);

    private final List<IChecker> checkers;

    public CheckRunner(List<IChecker> checkers) {
        this.checkers = checkers;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        for (IChecker checker : checkers) {
            try {
                checker.check();
            } catch (Exception e) {
                throw new IllegalStateException(checker.name() + " is not available", e);
            }
        }
    }

}
