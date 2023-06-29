package tsar.alex.config;


import static tsar.alex.utils.Endpoints.AUTH_AVAILABLE;
import static tsar.alex.utils.Endpoints.GAME_MASTER_AVAILABLE;
import static tsar.alex.utils.Endpoints.INITIALIZE_USERS_RATINGS;
import static tsar.alex.utils.Endpoints.UPDATE_USERS_RATINGS;

import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;


@Configuration
@Order(1)
@SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/ws/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/game/{\\w+}/ratings").permitAll()
                .antMatchers(HttpMethod.POST, "/api" + INITIALIZE_USERS_RATINGS).hasIpAddress("127.0.0.1")
                .antMatchers(HttpMethod.POST, "/api" + UPDATE_USERS_RATINGS).hasIpAddress("127.0.0.1")
                .antMatchers(HttpMethod.POST, "/api" + AUTH_AVAILABLE).hasIpAddress("127.0.0.1")
                .antMatchers(HttpMethod.POST, "/api" + GAME_MASTER_AVAILABLE).hasIpAddress("127.0.0.1")
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );
    }

    @Bean
    public JwtDecoder jwtDecoderBean() {
        return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }

}