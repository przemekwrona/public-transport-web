package pl.wrona.webserver.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/browser/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*.js").addResourceLocations("classpath:/static/browser/");
        registry.addResourceHandler("/*.css").addResourceLocations("classpath:/static/browser/");
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/static/browser/assets/");
        registry.addResourceHandler("/media/**").addResourceLocations("classpath:/static/browser/media/");
    }

}
