package com.yfckevin.inkCloud.config;


import com.yfckevin.inkCloud.ConfigProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ConfigProperties configProperties;
    public WebConfig(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/image/**").addResourceLocations("file:"+ configProperties.getPicSavePath());
        registry.addResourceHandler("/audio/**").addResourceLocations("file:"+ configProperties.getAudioSavePath());
        registry.addResourceHandler("/video/**").addResourceLocations("file:"+ configProperties.getVideoSavePath());
        registry.addResourceHandler("/ai_images/**").addResourceLocations("file:"+ configProperties.getAiPicSavePath());
        registry.addResourceHandler("/inkCloud/**").addResourceLocations("classpath:/static/");
//        super.addResourceHandlers(registry);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setCaseSensitive(false);
        configurer.setPathMatcher(matcher);
        configurer.setUseTrailingSlashMatch(true);
    }
}

