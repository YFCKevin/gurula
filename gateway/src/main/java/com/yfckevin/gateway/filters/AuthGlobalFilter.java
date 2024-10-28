package com.yfckevin.gateway.filters;

import com.yfckevin.gateway.config.AuthProperties;
import com.yfckevin.gateway.utils.JwtTool;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public AuthGlobalFilter(AuthProperties authProperties, JwtTool jwtTool) {
        this.authProperties = authProperties;
        this.jwtTool = jwtTool;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.取得request
        final ServerHttpRequest request = exchange.getRequest();
        System.out.println("使用者指定造訪資源路徑 = " + request.getPath());
        //2.判斷該路徑是否需要做驗證
        if (isExclude(request.getPath().toString())) {
            return chain.filter(exchange);
        }

        //3.取得token
//        final HttpHeaders headers = request.getHeaders();
//        final String token = headers.getFirst("Authorization");
        String token = null;
        if (request.getCookies().containsKey("JWT_TOKEN")) {
            HttpCookie jwtCookie = request.getCookies().getFirst("JWT_TOKEN");
            token = jwtCookie != null ? jwtCookie.getValue() : null;
        }
        System.out.println("token = " + token);

        //4.解析token
        String memberId = null;
        try {
            memberId = jwtTool.parseToken(token);
            System.out.println("memberId = " + memberId);
        } catch (Exception e) {
            //401
            final ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //5.得到會員資料
        String memberInfo = memberId;
        final ServerWebExchange serverWebExchange = exchange.mutate()
                .request(builder -> builder.header("member-info", memberInfo))
                .build();

        //6.放行
        return chain.filter(serverWebExchange);
    }

    private boolean isExclude(String path) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(pathPattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
