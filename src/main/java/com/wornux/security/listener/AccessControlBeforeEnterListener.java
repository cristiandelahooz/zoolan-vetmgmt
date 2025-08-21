package com.wornux.security.listener;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.wornux.security.service.PermissionService;
import com.wornux.security.service.SecurityContextService;
import com.wornux.views.auth.LoginView;
import com.wornux.views.error.AccessDeniedView;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Global listener that checks access permissions before entering any view. Redirects to login or
 * access denied page when necessary.
 */
@Slf4j
@SpringComponent
public class AccessControlBeforeEnterListener implements VaadinServiceInitListener {

  @Autowired private ApplicationContext applicationContext;

  @Override
  public void serviceInit(ServiceInitEvent event) {
    event
        .getSource()
        .addUIInitListener(
            uiEvent -> {
              uiEvent
                  .getUI()
                  .addBeforeEnterListener(
                      new BeforeEnterListener() {
                        @Override
                        public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
                          checkAccess(beforeEnterEvent);
                        }
                      });
            });
  }

  private void checkAccess(BeforeEnterEvent event) {
    Class<?> targetView = event.getNavigationTarget();
    String targetRoute = event.getLocation().getPath();

    log.info("Checking access for view: {} (route: {})", targetView.getSimpleName(), targetRoute);

    // Skip access control for login and access denied views
    if (targetView.equals(LoginView.class) || targetView.equals(AccessDeniedView.class)) {
      log.debug("Skipping access control for {} view", targetView.getSimpleName());
      return;
    }

    // Check if view allows anonymous access
    if (targetView.isAnnotationPresent(AnonymousAllowed.class)) {
      log.debug("Anonymous access allowed for {}", targetView.getSimpleName());
      return;
    }

    // Get services (lazy loading to avoid circular dependency)
    PermissionService permissionService = applicationContext.getBean(PermissionService.class);
    SecurityContextService securityContextService =
        applicationContext.getBean(SecurityContextService.class);

    // Log current user info
    log.info(
        "Current user: {}, SystemRole: {}",
        securityContextService.getCurrentUsername(),
        securityContextService.getCurrentSystemRole().orElse(null));

    // Check if user has permission
    boolean hasPermission = false;
    String accessDeniedMessage = "";

    try {
      // Log permission check details for debugging
      if (log.isDebugEnabled()) {
        permissionService.logPermissionCheck(targetView);
      }

      hasPermission = permissionService.hasPermission(targetView);

      if (!hasPermission) {
        accessDeniedMessage = permissionService.getAccessDeniedMessage(targetView);
      }
    } catch (Exception e) {
      log.error("Error checking permissions for view: {}", targetView.getSimpleName(), e);
      // In case of error, deny access for safety
      hasPermission = false;
      accessDeniedMessage = "Error al verificar permisos. Por favor, contacte al administrador.";
    }

    // Handle access denied
    if (!hasPermission) {
      log.warn("Access denied to {} for current user", targetView.getSimpleName());

      // Check if user is authenticated
      if (!isAuthenticated(permissionService)) {
        // Redirect to login
        log.debug("Redirecting unauthenticated user to login");
        event.forwardTo(LoginView.class);
      } else {
        // Redirect to access denied page with message
        try {
          String encodedMessage = URLEncoder.encode(accessDeniedMessage, StandardCharsets.UTF_8);
          String accessDeniedUrl = "access-denied/" + encodedMessage + "?route=" + targetRoute;

          log.debug("Redirecting to access denied page: {}", accessDeniedUrl);
          event.rerouteTo(accessDeniedUrl);
        } catch (Exception e) {
          log.error("Error encoding access denied message", e);
          event.rerouteTo("access-denied");
        }
      }
    } else {
      log.debug("Access granted to {} for current user", targetView.getSimpleName());
    }
  }

  private boolean isAuthenticated(PermissionService permissionService) {
    try {
      SecurityContextService securityContextService =
          applicationContext.getBean(SecurityContextService.class);
      return securityContextService.isAuthenticated();
    } catch (Exception e) {
      log.error("Error checking authentication status", e);
      return false;
    }
  }
}

