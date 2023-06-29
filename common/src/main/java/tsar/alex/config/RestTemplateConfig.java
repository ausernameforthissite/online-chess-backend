package tsar.alex.config;

import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;

@AllArgsConstructor
@Configuration
public class RestTemplateConfig {
    private final LogbookHttpRequestInterceptor logbookHttpRequestInterceptor;
    private final LogbookHttpResponseInterceptor logbookHttpResponseInterceptor;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        RestTemplate template = restTemplateBuilder
                .requestFactory(new MyRequestFactorySupplier())
                .build();

        return template;
    }

    class MyRequestFactorySupplier implements Supplier<ClientHttpRequestFactory> {
        @Override
        public ClientHttpRequestFactory get() {
            CloseableHttpClient client = HttpClientBuilder.create()
                    .addInterceptorFirst(logbookHttpRequestInterceptor)
                    .addInterceptorFirst(logbookHttpResponseInterceptor)
                    .build();
            return new HttpComponentsClientHttpRequestFactory(client);
        }
    }
}
