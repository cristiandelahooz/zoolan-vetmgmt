package com.wornux.views.auth;

import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.wornux.security.UserUtils;

@Route(value = "login", autoLayout = false)
@PageTitle("Login | Zoolandia")
@AnonymousAllowed
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

  public LoginView() {
    configureLoginAction();
    configureBranding();
    configureInternationalization();
    configureVisibility();
  }

  @Override
  public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    if (isUserAlreadyLoggedIn()) {
      redirectToMainPage(beforeEnterEvent);
      return;
    }

    displayAuthenticationErrorIfPresent(beforeEnterEvent);
  }

  private void configureLoginAction() {
    setAction("login");
  }

  private void configureBranding() {
    setTitle("Zoolandia Vet");
    setDescription("Sistema de Gestión Veterinaria");
  }

  private void configureInternationalization() {
    LoginI18n i18n = createSpanishI18n();
    setI18n(i18n);
  }

  private LoginI18n createSpanishI18n() {
    LoginI18n i18n = LoginI18n.createDefault();
    configureFormLabels(i18n);
    configureErrorMessages(i18n);
    return i18n;
  }

  private void configureFormLabels(LoginI18n i18n) {
    i18n.getForm().setTitle("Iniciar Sesión");
    i18n.getForm().setUsername("Usuario");
    i18n.getForm().setPassword("Contraseña");
    i18n.getForm().setSubmit("Ingresar");
  }

  private void configureErrorMessages(LoginI18n i18n) {
    i18n.getErrorMessage().setTitle("Error de Autenticación");
    i18n.getErrorMessage().setMessage("Usuario o contraseña incorrectos");
  }

  private void configureVisibility() {
    setOpened(true);
    setForgotPasswordButtonVisible(false);
  }

  private boolean isUserAlreadyLoggedIn() {
    return UserUtils.getUser().isPresent();
  }

  private void redirectToMainPage(BeforeEnterEvent beforeEnterEvent) {
    setOpened(false);
    beforeEnterEvent.forwardTo("");
  }

  private void displayAuthenticationErrorIfPresent(BeforeEnterEvent beforeEnterEvent) {
    boolean hasErrorParameter =
        beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error");
    setError(hasErrorParameter);
  }
}
