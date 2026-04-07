package pl.wrona.webserver.bussiness.route.details;

import org.junit.jupiter.api.Test;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TripComparatorUtilsTest {

    @Test
    public void shouldCheckOrderOfTrip() {
        // given
        var mainFrontNormal = TripEntity.builder()
                .mainVariant(Boolean.TRUE)
                .variantMode(TripVariantMode.FRONT)
                .trafficMode(TripTrafficMode.NORMAL)
                .build();

        var mainBackNormal = TripEntity.builder()
                .mainVariant(Boolean.TRUE)
                .variantMode(TripVariantMode.BACK)
                .trafficMode(TripTrafficMode.NORMAL)
                .build();

        var mainFrontTraffic = TripEntity.builder()
                .mainVariant(Boolean.TRUE)
                .variantMode(TripVariantMode.FRONT)
                .trafficMode(TripTrafficMode.TRAFFIC)
                .build();

        var mainBackTraffic = TripEntity.builder()
                .mainVariant(Boolean.TRUE)
                .variantMode(TripVariantMode.BACK)
                .trafficMode(TripTrafficMode.TRAFFIC)
                .build();

        var notMainFrontNormal = TripEntity.builder()
                .mainVariant(Boolean.FALSE)
                .variantMode(TripVariantMode.FRONT)
                .trafficMode(TripTrafficMode.NORMAL)
                .variantDesignation("H")
                .variantDescription("Wiazd do miejscowości Holendry")
                .build();

        var notMainBackNormal = TripEntity.builder()
                .mainVariant(Boolean.FALSE)
                .variantMode(TripVariantMode.BACK)
                .trafficMode(TripTrafficMode.NORMAL)
                .variantDesignation("H")
                .variantDescription("Wiazd do miejscowości Holendry")
                .build();

        // when
        var trips = List.of(notMainBackNormal, notMainFrontNormal, mainFrontTraffic, mainBackTraffic, mainFrontNormal, mainBackNormal);
        var sorted = trips.stream().sorted(TripComparatorUtils.tripEntityComparator()).toList();

        // then
        assertThat(sorted).isNotNull().isNotEmpty();
        assertThat(sorted.size()).isEqualTo(trips.size());

        assertThat(sorted.get(0).isMainVariant()).isTrue();
        assertThat(sorted.get(0).getVariantMode()).isEqualTo(TripVariantMode.FRONT);
        assertThat(sorted.get(0).getTrafficMode()).isEqualTo(TripTrafficMode.NORMAL);

        assertThat(sorted.get(1).isMainVariant()).isTrue();
        assertThat(sorted.get(1).getVariantMode()).isEqualTo(TripVariantMode.FRONT);
        assertThat(sorted.get(1).getTrafficMode()).isEqualTo(TripTrafficMode.TRAFFIC);

        assertThat(sorted.get(2).isMainVariant()).isTrue();
        assertThat(sorted.get(2).getVariantMode()).isEqualTo(TripVariantMode.BACK);
        assertThat(sorted.get(2).getTrafficMode()).isEqualTo(TripTrafficMode.NORMAL);

        assertThat(sorted.get(3).isMainVariant()).isTrue();
        assertThat(sorted.get(3).getVariantMode()).isEqualTo(TripVariantMode.BACK);
        assertThat(sorted.get(3).getTrafficMode()).isEqualTo(TripTrafficMode.TRAFFIC);

        assertThat(sorted.get(4).isMainVariant()).isFalse();
        assertThat(sorted.get(4).getVariantMode()).isEqualTo(TripVariantMode.FRONT);
        assertThat(sorted.get(4).getTrafficMode()).isEqualTo(TripTrafficMode.NORMAL);
        assertThat(sorted.get(4).getVariantDesignation()).isEqualTo("H");

        assertThat(sorted.get(5).isMainVariant()).isFalse();
        assertThat(sorted.get(5).getVariantMode()).isEqualTo(TripVariantMode.BACK);
        assertThat(sorted.get(5).getTrafficMode()).isEqualTo(TripTrafficMode.NORMAL);
        assertThat(sorted.get(5).getVariantDesignation()).isEqualTo("H");
    }

}