package pl.wrona.webserver.bussiness.route;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LineNameCleaner {

    public String clean(String name) {
        return name
                .replaceAll("\\s{2,}", " ")
                .replaceAll(" -", "-")
                .replaceAll("- ", "-")
                .replaceAll("-", " - ")
                .trim()
                .toUpperCase();
    }
}
