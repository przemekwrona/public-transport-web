package pl.wrona.webserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String publicUrl) {

    public String loginUrl() {
        String base = publicUrl == null ? "" : publicUrl.strip();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/#/signin";
    }
}
