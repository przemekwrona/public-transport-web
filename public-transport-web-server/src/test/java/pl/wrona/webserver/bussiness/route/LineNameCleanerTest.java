package pl.wrona.webserver.bussiness.route;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LineNameCleanerTest {

    static Stream<Arguments> provideRouteName() {
        return Stream.of(
                Arguments.of("Kije - Chmielnik", "KIJE - CHMIELNIK"),
                Arguments.of("   Kije - Chmielnik     ", "KIJE - CHMIELNIK"),
                Arguments.of("   Kije     -      Chmielnik     ", "KIJE - CHMIELNIK"),
                Arguments.of("   Śladków Duży  - Chmielnik     ", "ŚLADKÓW DUŻY - CHMIELNIK")
        );
    }

    @ParameterizedTest
    @MethodSource("provideRouteName") // six numbers
    void isOdd_ShouldReturnTrueForOddNumbers(String routeName, String expectedRouteName) {
        assertThat(LineNameCleaner.clean(routeName)).isEqualTo(expectedRouteName);
    }

}