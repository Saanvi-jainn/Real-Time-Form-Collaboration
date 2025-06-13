package com.collabform.controller;

import com.collabform.model.Form;
import com.collabform.model.User;
import com.collabform.repository.FormRepository;
import com.collabform.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener for WebSocket events to handle user connections, disconnections, and subscriptions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final CollaborationService collaborationService;
    private final FormRepository formRepository;

    // Store active users by session ID and the forms they're collaborating on
    private final Map<String, UserSession> activeUserSessions = new ConcurrentHashMap<>();

    // Pattern to extract form ID from subscription destination
    private final Pattern formTopicPattern = Pattern.compile("/topic/form/(\\d+)");

    /**
     * Handle WebSocket connection established events.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.debug("WebSocket connection established: sessionId={}", sessionId);
    }

    /**
     * Handle WebSocket subscription events.
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();

        if (destination == null) {
            return;
        }

        Matcher matcher = formTopicPattern.matcher(destination);
        if (matcher.matches()) {
            Long formId = Long.parseLong(matcher.group(1));

            UsernamePasswordAuthenticationToken authentication =
                    (UsernamePasswordAuthenticationToken) headerAccessor.getUser();

            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();

                // Store the user session
                UserSession userSession = activeUserSessions.computeIfAbsent(
                        sessionId,
                        k -> new UserSession(user)
                );
                userSession.addForm(formId);

                // Fetch the form and notify collaborators
                Form form = formRepository.findById(formId)
                        .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formId));
                collaborationService.notifyUserJoined(form, user);

                log.debug("User joined form collaboration: sessionId={}, userId={}, formId={}",
                        sessionId, user.getId(), formId);
            }
        }
    }

    /**
     * Handle WebSocket disconnection events.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        UserSession userSession = activeUserSessions.remove(sessionId);
        if (userSession != null) {
            User user = userSession.getUser();

            for (Long formId : userSession.getFormIds()) {
                Form form = formRepository.findById(formId)
                        .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formId));
                collaborationService.notifyUserLeft(form, user);

                log.debug("User left form collaboration: sessionId={}, userId={}, formId={}",
                        sessionId, user.getId(), formId);
            }
        }
    }

    /**
     * Helper class to track a user's WebSocket session and the forms they're collaborating on.
     */
    private static class UserSession {
        private final User user;
        private final Map<Long, Boolean> formIds = new ConcurrentHashMap<>();

        public UserSession(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void addForm(Long formId) {
            formIds.put(formId, true);
        }

        public Iterable<Long> getFormIds() {
            return formIds.keySet();
        }
    }
}
