package tsar.alex.config;

import static org.zalando.logbook.BodyFilter.merge;
import static org.zalando.logbook.BodyFilters.defaultValue;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonStringProperty;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HeaderFilters;

@AllArgsConstructor
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .maxAge(3600L)
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public BodyFilter bodyFilter() {
        return merge(
                defaultValue(),
                replaceJsonStringProperty(Stream.of("password", "accessToken").collect(Collectors.toSet()), "***this field is hidden by security policy***"));
    }

    @Bean
    public HeaderFilter headerFilter() {
        Set<String> headersToDisplay = Stream.of("Content-Type", "Date", "cookie", "origin", "Set-Cookie").collect(Collectors.toSet());
        HeaderFilter onlyRequiredHeaders = HeaderFilter.merge(HeaderFilters.defaultValue(), HeaderFilters.removeHeaders((header) -> !headersToDisplay.contains(header)));
        return HeaderFilter.merge(onlyRequiredHeaders, HeaderFilters.replaceCookies("refresh-token"::equals, "***this cookie is hidden by security policy***"));
    }

}