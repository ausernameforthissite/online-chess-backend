package tsar.alex.config;

import static tsar.alex.utils.Endpoints.MATCHER_AVAILABLE;
import static tsar.alex.utils.Endpoints.START_GAMES;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;


@Configuration
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/ws/**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/game/{\\d+}/state").permitAll()
                .antMatchers(HttpMethod.POST,"/api/game" + START_GAMES).hasIpAddress("127.0.0.1")
                .antMatchers(HttpMethod.POST,"/api/game" + MATCHER_AVAILABLE).hasIpAddress("127.0.0.1")
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );
    }

}