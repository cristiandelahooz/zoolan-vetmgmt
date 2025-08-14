package com.wornux.security;

import com.vaadin.flow.server.VaadinSession;
import com.wornux.data.entity.User;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class UserUtils {

  public static void clear() {
    if (VaadinSession.getCurrent() != null) {
      VaadinSession.getCurrent().setAttribute(User.class, null);
    }
  }

  public static User getUser() {
    if (VaadinSession.getCurrent() != null) {
      return VaadinSession.getCurrent().getAttribute(User.class);
    }
    return null;
  }
}
