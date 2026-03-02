package com.alvayonara.finguardriskservice.user.filter;

import com.alvayonara.finguardriskservice.user.UserRepository;
import com.alvayonara.finguardriskservice.user.context.UserContext;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class UserContextWebFilter implements WebFilter {

  private static final String HEADER_USER_UID = "X-User-Uid";

  private final UserRepository userRepository;
  private final String allowedPublicPaths;

  public UserContextWebFilter(
      UserRepository userRepository,
      @Value("${security.allowedPublicPaths:}") String allowedPublicPaths) {
    this.userRepository = userRepository;
    this.allowedPublicPaths = allowedPublicPaths;
  }

  @Override
  @NonNull
  public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    boolean isPublic =
        Arrays.stream(allowedPublicPaths.split(","))
            .map(String::trim)
            .anyMatch(p -> pathMatches(p, path));
    if (isPublic) {
      return chain.filter(exchange);
    }
    String userUid = exchange.getRequest().getHeaders().getFirst(HEADER_USER_UID);
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
              return chain
                  .filter(exchange)
                  .contextWrite(ctx -> ctx.put(UserContext.CONTEXT_KEY, context));
            });
  }

  private boolean pathMatches(String pattern, String path) {
    if (pattern.endsWith("/**")) {
      return path.startsWith(pattern.substring(0, pattern.length() - 3));
    }
    return pattern.equals(path);
  }
}
