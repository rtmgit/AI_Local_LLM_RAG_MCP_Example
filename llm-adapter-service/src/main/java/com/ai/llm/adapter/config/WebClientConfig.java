package com.ai.llm.adapter.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient ollamaWebClient(WebClient.Builder builder, @Value("${ollama.base-url}") String baseUrl) {

        // Connect timeout (e.g., 5s): time allowed to establish TCP connection
        // Response timeout (e.g., 180s): time allowed for server to start sending response
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .responseTimeout(Duration.ofSeconds(320))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(320, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(320, TimeUnit.SECONDS)));

        return builder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

}



