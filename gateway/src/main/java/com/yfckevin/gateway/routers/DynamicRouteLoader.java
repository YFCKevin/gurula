package com.yfckevin.gateway.routers;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
@Slf4j
@Component
public class DynamicRouteLoader {
    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter writer;
    private final ObjectMapper objectMapper;
    private final String dataId = "gateway-route.json";
    private final String group = "DEFAULT_GROUP";
    private final Set<String> routeIds = new HashSet<>();

    public DynamicRouteLoader(NacosConfigManager nacosConfigManager, RouteDefinitionWriter writer, ObjectMapper objectMapper) {
        this.nacosConfigManager = nacosConfigManager;
        this.writer = writer;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initRouteConfigListener () throws NacosException, JsonProcessingException {
        //啟動Spring boot時，取得config資訊，並添加到config listener
        final String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        //監聽到config有變更，要更新路由表
                        try {
                            updateConfigInfo(configInfo);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        //第一次取得config，也要更新到路由表中
        updateConfigInfo(configInfo);
    }


    public void updateConfigInfo (String configInfo) throws JsonProcessingException {
        log.info("監聽路由表：{}", configInfo);
        if (configInfo != null) {
            //將config資訊，轉換成RouteDefinition
            final List<RouteDefinition> routeDefinitions = objectMapper.readValue(configInfo, new TypeReference<>() {});
            //清空舊的路由表
            for (String routeId : routeIds) {
                writer.delete(Mono.just(routeId)).subscribe();
            }
            //清空路由Set
            routeIds.clear();
            //更新路由表
            for (RouteDefinition routeDefinition : routeDefinitions) {
                writer.save(Mono.just(routeDefinition)).subscribe();
                //紀錄路由ID，以便下次更新時要刪除
                routeIds.add(routeDefinition.getId());
            }
        }
    }
}
