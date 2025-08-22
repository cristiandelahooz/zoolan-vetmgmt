package com.wornux.views.auth;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;

/**
 * @author cristiandelahoz
 * @created 21/8/25 - 14:48
 */
@PageTitle("Logout")
@Route(value = "logout", autoLayout = false)
@AnonymousAllowed
public class LogoutView extends Div {

  private final transient AuthenticationContext authenticationContext;

  public LogoutView(AuthenticationContext authenticationContext) {
    this.authenticationContext = authenticationContext;
    logout();
  }

  public void logout() {
    authenticationContext.logout();
  }
}
