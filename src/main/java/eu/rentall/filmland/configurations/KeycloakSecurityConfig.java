package eu.rentall.filmland.configurations;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Configuration of security aspects of the application, using a Keycloak server as the authentication provider.
 *
 * @author Holly Schoene
 * @version 2.0
 * Created 30-12-2020 13:16
 */
@Slf4j
@Configuration
@EnableWebSecurity
/* The jsr250Enabled property allows us to use the @RoleAllowed annotation. */
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    log.debug("configure HttpSecurity");
    super.configure(http);
    http
        .cors().and()
        .authorizeRequests().antMatchers("/h2/**", "/authentication/login").permitAll()
        .and().csrf().ignoringAntMatchers("/h2/**", "/authentication/login")
        .and().headers().frameOptions().sameOrigin()
        .and()
        .authorizeRequests(auth -> auth
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .antMatchers("/actuator/**").hasRole("ADMIN")
            .antMatchers("/api-docs/**", "/public/**", "/api/public/**", "/api/doc/**").permitAll()
            .antMatchers("/**").authenticated()
        )
        .oauth2ResourceServer().jwt();
    http.formLogin().disable();
    http.logout().disable();
  }

  /**
   * Registers the KeycloakAuthenticationProvider with the authentication manager.
   *
   * @param auth AuthenticationManagerBuilder
   */
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) {
    log.debug("configureGlobal AuthenticationManagerBuilder");
    KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
    SimpleAuthorityMapper authorityMapper = new SimpleAuthorityMapper();
    authorityMapper.setConvertToUpperCase(true);
    keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(authorityMapper);
    auth.authenticationProvider(keycloakAuthenticationProvider);
  }

  /**
   * Defines the session authentication strategy.
   *
   * @return the configured SessionAuthenticationStrategy
   */
  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    log.debug("creating SessionAuthenticationStrategy");
    return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
  }

  /**
   * Make sure Spring Security Adapter looks at the configuration provided by the Spring Boot Adapter by adding this bean.
   *
   * @return the configured KeycloakConfigResolver
   */
  @Bean
  public KeycloakConfigResolver KeycloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  @Bean
  AuthenticationEntryPoint forbiddenEntryPoint() {
    return new HttpStatusEntryPoint(UNAUTHORIZED);
  }

  String jwkSetUri = "http://localhost:8081/auth/realms/filmland/protocol/openid-connect/certs";

  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
  }

}
