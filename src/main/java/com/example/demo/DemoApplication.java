package com.example.demo;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.contrib.reporter.slf4j.DefaultLoggerTracer;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

@SpringBootApplication
@RestController
@EnableReactiveCassandraRepositories
public class DemoApplication {

    @Autowired
    KeyValueRepo repo;

    @GetMapping("/keys")
    public Flux<String> getKeys() {
        return repo.findAll().map(kv -> kv.getKey());
    }

    @GetMapping("/key/{key}")
    public Mono<KeyValue> get(@PathVariable String key) {
        return repo.findById(key).transform(withSpan("query db", b -> b.withTag("key", key)));
    }

    @PutMapping("/key/{key}")
    public Mono<KeyValue> put(@PathVariable String key, @RequestBody String value) {
        return repo.insert(new KeyValue(key, value));
    }

    @Bean
    public WebFilter tracing() {
        return (ex, chain) ->
                chain.filter(ex)
                     .transform(withSpan("HttpRequest",
                                         b -> b.withTag("path", ex.getRequest().getPath().value())
                                               .withTag("method", ex.getRequest().getMethodValue())));
    }

    public Mono<Optional<Span>> activeSpan() {
        return Mono.subscriberContext().map(c -> c.<Span[]>getOrEmpty("SPAN").map(sa -> sa[0]));
    }

    public <T> Function<Mono<T>, Publisher<T>> withSpan(String name,
                                                        Function<SpanBuilder, SpanBuilder> f) {

        return p -> activeSpan().flatMap(parent -> {
            Span[] span = new Span[1];

            return p.doOnSubscribe(s -> {
                var setup = tracer().buildSpan(name);
                var next = parent.map(setup::asChildOf).orElse(setup);
                var nextSpan = f.apply(next);
                span[0] = nextSpan.ignoreActiveSpan().start();
            })
                    .doOnTerminate(() -> span[0].finish())
                    .subscriberContext(c -> c.put("SPAN", span));
        });
    }

    @Bean
    public Tracer tracer() {
        return new DefaultLoggerTracer();
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

