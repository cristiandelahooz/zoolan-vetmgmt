package com.wornux.security;

import com.vaadin.flow.server.VaadinSession;
import com.wornux.data.entity.User;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class UserUtils {

  private UserUtils() {
    // It's not required
  }

  public static void clear() {
    if (Objects.nonNull(VaadinSession.getCurrent())) {
      VaadinSession.getCurrent().setAttribute(User.class, null);
    }
  }

  public static User getUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (Objects.nonNull(authentication)
        && authentication.getPrincipal()
        instanceof UserDetailsImpl(User user)) {
      return user;
    }
    if (Objects.nonNull(VaadinSession.getCurrent())) {
      return VaadinSession.getCurrent().getAttribute(User.class);
    }
    return null;
  }
}
