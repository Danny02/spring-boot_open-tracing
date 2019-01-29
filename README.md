# Webflux support

Open Tracing [has support](https://github.com/opentracing-contrib/java-spring-web) for instrumentation of Spring Boot application which use Servlets.
It does not exist any support for the reactive stack named [Webflux](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/web-reactive.html).

Webflux is based on [reactive streams](http://www.reactive-streams.org/) and therefor can not use anything based on thread locals. Opentracing active span functionality sadly depends on thread-locals and can therefor not be used.

Spring Security had a similiar problem and they created a [reactive security context](https://docs.spring.io/spring-security/site/docs/5.0.0.RELEASE/reference/htmlsingle/#jc-erms) based on the `context` of [Project Reactor](https://projectreactor.io/docs/core/release/reference/#context).

This experiment tries to find a solution to the problem of opentracing in a reactive-streams enviroment similiar to Spring Security.

## Used libs
* [span-reporter](https://github.com/opentracing-contrib/java-span-reporter) log spans instead of real tracer implementation
