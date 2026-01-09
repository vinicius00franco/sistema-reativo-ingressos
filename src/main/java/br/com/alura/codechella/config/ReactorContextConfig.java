package br.com.alura.codechella.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class ReactorContextConfig {

    @PostConstruct
    public void enableReactorContextPropagation() {
        // Ensure Reactor automatically propagates Micrometer context (trace/span)
        Hooks.enableAutomaticContextPropagation();
    }
}
