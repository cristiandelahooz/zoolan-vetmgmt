package com.wornux.security.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringComponent
@RequiredArgsConstructor
public class NavigationAccessConfig implements VaadinServiceInitListener {

    private final AccessAnnotationChecker accessAnnotationChecker;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().addBeforeEnterListener(beforeEnterEvent -> {
                if (!accessAnnotationChecker.hasAccess(beforeEnterEvent.getNavigationTarget())) {
                    beforeEnterEvent.rerouteTo("login");
                }
            });
        });
    }
}