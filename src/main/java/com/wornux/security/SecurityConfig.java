package com.wornux.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.wornux.data.enums.SystemRole;
import com.wornux.views.auth.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Order(2)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends VaadinWebSecurity {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize ->
        authorize
            .requestMatchers("/images/*.png", "/icons/**", "/validate/callback/**", "/line-awesome/**")
            .permitAll()
            .requestMatchers("/share/**")
            .anonymous()
            .requestMatchers("/admin/**")
            .hasRole(SystemRole.SYSTEM_ADMIN.name())
            .requestMatchers("/empleados/**")
            .hasAnyRole(SystemRole.SYSTEM_ADMIN.name(), SystemRole.MANAGER.name())
            .requestMatchers("/api/**")
            .authenticated()
    );

    super.configure(http);
    setLoginView(http, LoginView.class);
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    super.configure(web);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
