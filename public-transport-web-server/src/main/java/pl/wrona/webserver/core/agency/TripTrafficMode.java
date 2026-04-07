package pl.wrona.webserver.core.agency;

import lombok.Getter;

@Getter
public enum TripTrafficMode {
    NORMAL(20), TRAFFIC(10);

    private final int weight;

    TripTrafficMode(int weight) {
        this.weight = weight;
    }
}
