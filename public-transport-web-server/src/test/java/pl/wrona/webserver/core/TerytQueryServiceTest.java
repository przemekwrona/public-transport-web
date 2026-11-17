package pl.wrona.webserver.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.bussiness.stop.TerytQueryService;
import pl.wrona.webserver.core.stop.TerytNameView;
import pl.wrona.webserver.core.stop.TerytRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerytQueryServiceTest {

    @Mock
    private TerytRepository terytRepository;

    @InjectMocks
    private TerytQueryService terytQueryService;

    @Test
    void resolvesGminaNameBySixDigitPrefixWhenExactIdtercMissing() {
        when(terytRepository.findNamesByIdtercInOrPrefixIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(view("2604044", "Chmielnik")));

        Map<String, String> names = terytQueryService.findGminaNamesByIdtercIn(List.of("2604043"));

        assertThat(names).containsEntry("2604043", "Chmielnik");
    }

    private static TerytNameView view(String idterc, String nazwa) {
        return new TerytNameView() {
            @Override
            public String getIdterc() {
                return idterc;
            }

            @Override
            public String getNazwa() {
                return nazwa;
            }
        };
    }
}
