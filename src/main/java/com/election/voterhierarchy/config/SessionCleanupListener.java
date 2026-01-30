package com.election.voterhierarchy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@Component
public class SessionCleanupListener implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupListener.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application started - all sessions should be cleared");
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        log.debug("Session created: {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.debug("Session destroyed: {}", se.getSession().getId());
    }
}