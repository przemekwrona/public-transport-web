package pl.wrona.webserver.bussiness.route.details;

import org.junit.jupiter.api.Test;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TripComparatorUtilsTest {

    @Test
    public void shouldCheckOrderOfTrip() {
        // given
        var mainFront = TripEntity.builder()
                .mainVariant(Boolean.TRUE)
                .variantMode(TripVariantMode.FRONT)
                .build();

        var mainBack = TripEntity.builder()
                .mainVariant(Boolean.TRUE)
                .variantMode(TripVariantMode.BACK)
                .build();

        var notMainFront = TripEntity.builder()
                .mainVariant(Boolean.FALSE)
                .variantMode(TripVariantMode.FRONT)
                .variantDesignation("H")
                .variantDescription("Wiazd do miejscowości Holendry")
                .build();

        var notMainBack = TripEntity.builder()
                .mainVariant(Boolean.FALSE)
                .variantMode(TripVariantMode.BACK)
                .variantDesignation("H")
                .variantDescription("Wiazd do miejscowości Holendry")
                .build();

        // when
        var trips = List.of(notMainBack, notMainFront, mainBack, mainFront);
        var sorted = trips.stream().sorted(TripComparatorUtils.tripEntityComparator()).toList();

        // then
        assertThat(sorted).isNotNull().isNotEmpty();
        assertThat(sorted.size()).isEqualTo(trips.size());

        assertThat(sorted.get(0).isMainVariant()).isFalse();
        assertThat(sorted.get(0).getVariantMode()).isEqualTo(TripVariantMode.BACK);

        assertThat(sorted.get(1).isMainVariant()).isFalse();
        assertThat(sorted.get(1).getVariantMode()).isEqualTo(TripVariantMode.FRONT);
        assertThat(sorted.get(1).getVariantDesignation()).isEqualTo("H");

        assertThat(sorted.get(2).isMainVariant()).isTrue();
        assertThat(sorted.get(2).getVariantMode()).isEqualTo(TripVariantMode.BACK);

        assertThat(sorted.get(3).isMainVariant()).isTrue();
        assertThat(sorted.get(3).getVariantMode()).isEqualTo(TripVariantMode.FRONT);
    }

}
