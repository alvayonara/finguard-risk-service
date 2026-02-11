package com.alvayonara.finguardriskservice.user.filter;

import com.alvayonara.finguardriskservice.user.UserRepository;
import com.alvayonara.finguardriskservice.user.context.UserContext;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class UserContextWebFilter implements WebFilter {
  @Autowired private UserRepository userRepository;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    if (path.equals("/v1/users/anonymous")) {
      return chain.filter(exchange);
    }
    String userUid = exchange.getRequest().getHeaders().getFirst("X-User-Uid");
    if (Objects.isNull(userUid) || userUid.isBlank()) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }
    return userRepository
        .findByUserUid(userUid)
        .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
        .flatMap(
            user -> {
              UserContext context =
                  UserContext.builder().internalUserId(user.getId()).userUid(userUid).build();
              return chain.filter(exchange).contextWrite(ctx -> ctx.put("userContext", context));
            });
  }
}
