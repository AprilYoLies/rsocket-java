/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.transport.netty.server;

import static io.rsocket.frame.FrameUtil.FRAME_MAX_SIZE;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.test.StepVerifier;

final class WebsocketRouteTransportTest {

  @Test
  public void testThatSetupWithUnSpecifiedFrameSizeShouldSetMaxFrameSize() {
    ArgumentCaptor<Consumer> captor = ArgumentCaptor.forClass(Consumer.class);
    HttpServer httpServer = Mockito.spy(HttpServer.create());
    HttpServerRoutes routes = Mockito.mock(HttpServerRoutes.class);
    Mockito.doAnswer(a -> httpServer).when(httpServer).route(captor.capture());
    Mockito.doAnswer(a -> Mono.empty()).when(httpServer).bind();

    WebsocketRouteTransport serverTransport =
        new WebsocketRouteTransport(httpServer, (r) -> {}, "");

    serverTransport.start(c -> Mono.empty(), 0).subscribe();

    captor.getValue().accept(routes);

    Mockito.verify(routes)
        .ws(
            Mockito.any(Predicate.class),
            Mockito.any(BiFunction.class),
            Mockito.nullable(String.class),
            Mockito.eq(FRAME_MAX_SIZE));
  }

  @Test
  public void testThatSetupWithSpecifiedFrameSizeButLowerThanWsDefaultShouldSetToWsDefault() {
    ArgumentCaptor<Consumer> captor = ArgumentCaptor.forClass(Consumer.class);
    HttpServer httpServer = Mockito.spy(HttpServer.create());
    HttpServerRoutes routes = Mockito.mock(HttpServerRoutes.class);
    Mockito.doAnswer(a -> httpServer).when(httpServer).route(captor.capture());
    Mockito.doAnswer(a -> Mono.empty()).when(httpServer).bind();

    WebsocketRouteTransport serverTransport =
        new WebsocketRouteTransport(httpServer, (r) -> {}, "");

    serverTransport.start(c -> Mono.empty(), 1000).subscribe();

    captor.getValue().accept(routes);

    Mockito.verify(routes)
        .ws(
            Mockito.any(Predicate.class),
            Mockito.any(BiFunction.class),
            Mockito.nullable(String.class),
            Mockito.eq(65536));
  }

  @Test
  public void
      testThatSetupWithSpecifiedFrameSizeButHigherThanWsDefaultShouldSetToSpecifiedFrameSize() {
    ArgumentCaptor<Consumer> captor = ArgumentCaptor.forClass(Consumer.class);
    HttpServer httpServer = Mockito.spy(HttpServer.create());
    HttpServerRoutes routes = Mockito.mock(HttpServerRoutes.class);
    Mockito.doAnswer(a -> httpServer).when(httpServer).route(captor.capture());
    Mockito.doAnswer(a -> Mono.empty()).when(httpServer).bind();

    WebsocketRouteTransport serverTransport =
        new WebsocketRouteTransport(httpServer, (r) -> {}, "");

    serverTransport.start(c -> Mono.empty(), 65536 + 1000).subscribe();

    captor.getValue().accept(routes);

    Mockito.verify(routes)
        .ws(
            Mockito.any(Predicate.class),
            Mockito.any(BiFunction.class),
            Mockito.nullable(String.class),
            Mockito.eq(65536 + 1000));
  }

  @DisplayName("creates server")
  @Test
  void constructor() {
    new WebsocketRouteTransport(HttpServer.create(), routes -> {}, "/test-path");
  }

  @DisplayName("constructor throw NullPointer with null path")
  @Test
  void constructorNullPath() {
    assertThatNullPointerException()
        .isThrownBy(() -> new WebsocketRouteTransport(HttpServer.create(), routes -> {}, null))
        .withMessage("path must not be null");
  }

  @DisplayName("constructor throw NullPointer with null routesBuilder")
  @Test
  void constructorNullRoutesBuilder() {
    assertThatNullPointerException()
        .isThrownBy(() -> new WebsocketRouteTransport(HttpServer.create(), null, "/test-path"))
        .withMessage("routesBuilder must not be null");
  }

  @DisplayName("constructor throw NullPointer with null server")
  @Test
  void constructorNullServer() {
    assertThatNullPointerException()
        .isThrownBy(() -> new WebsocketRouteTransport(null, routes -> {}, "/test-path"))
        .withMessage("server must not be null");
  }

  @DisplayName("starts server")
  @Test
  void start() {
    WebsocketRouteTransport serverTransport =
        new WebsocketRouteTransport(HttpServer.create(), routes -> {}, "/test-path");

    serverTransport
        .start(duplexConnection -> Mono.empty(), 0)
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

  @DisplayName("start throw NullPointerException with null acceptor")
  @Test
  void startNullAcceptor() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new WebsocketRouteTransport(HttpServer.create(), routes -> {}, "/test-path")
                    .start(null, 0))
        .withMessage("acceptor must not be null");
  }
}
