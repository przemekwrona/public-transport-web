package pl.wrona.webserver.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PhpFileRequestFilterTest {

    private final PhpFileRequestFilter filter = new PhpFileRequestFilter();

    @Test
    void rejectsPhpFileBeforeTheRestOfTheChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wp-login.php");
        request.setQueryString("redirect=1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoInteractions(chain);
    }

    @Test
    void rejectsPhpPathRegardlessOfLetterCase() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/INDEX.PHP");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoInteractions(chain);
    }

    @Test
    void letsOrdinaryRequestsThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(request, response);
    }

}
