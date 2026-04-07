package pl.wrona.webserver.core.agency;

import lombok.Getter;

public enum TripVariantMode {
    FRONT(20), BACK(10);

    @Getter
    private final int weight;

    TripVariantMode(int weight) {
        this.weight = weight;
    }

}
