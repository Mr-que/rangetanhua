package com.tanhua.gateway.filter;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.commons.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//用户请求，网关判断是否有令牌 无——>登录 白名单——>放行
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    //排除的链接
    //@Value("#{'${gateway.excludedUrls}'.split(',')}")
    @Value("${gateway.excludedUrls}")
    private List<String> excludedUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().getPath();//当前的请求链接
        //判断是否是特殊接口
        if (excludedUrls.contains(url)){
            return chain.filter(exchange);
        }
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (!StringUtils.isEmpty(token)){
            //有些vue请求可能会带有Bearer 需要替换掉
            token = token.replace("Bearer ", "");
        }
        ServerHttpResponse response = exchange.getResponse();
        //2、使用工具类，判断token是否有效
        boolean verifyToken = JwtUtils.verifyToken(token);
        //3、如果token失效，返回状态码401，拦截
        if (!verifyToken){
            Map<String,Object> responseData = new HashMap<>();
            responseData.put("errCode", 401);
            responseData.put("errMessage", "用户未登录");
            return responseError(response, responseData);
        }
        return chain.filter(exchange);
    }

    //响应错误数据
    private Mono<Void> responseError(ServerHttpResponse response,Map<String, Object> responseData){
        // 将信息转换为 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data = new byte[0];
        try {
            data = objectMapper.writeValueAsBytes(responseData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 输出错误信息到页面
        DataBuffer buffer = response.bufferFactory().wrap(data);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 设置过滤器的执行顺序，数字越小级别越高
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}