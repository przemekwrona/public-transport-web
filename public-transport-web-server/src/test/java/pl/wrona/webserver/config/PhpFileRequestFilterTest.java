package pl.wrona.webserver.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @ValueSource(strings = {
            "/wordpress",
            "/wp-admin/",
            "/wp-admin/css/",
            "/wp-admin/images/",
            "/wp-admin/includes/",
            "/wp-admin/js/",
            "/wp-admin/maint/",
            "/wp-admin/network/",
            "/wp-admin/user/",
            "/wp-content/",
            "/wp-content/plugins/",
            "/wp-content/themes/",
            "/wp-content/uploads/",
            "/wp-content/uploads/2019/03/Need-a-designer-logo-www.png",
            "/wp-content/languages/",
            "/wp-content/mu-plugins/",
            "/wp-includes/",
            "/wp-includes/ID3/",
            "/wp-includes/IXR/",
            "/wp-includes/PHPMailer/",
            "/wp-includes/Requests/",
            "/wp-includes/SimplePie/",
            "/wp-includes/Text/",
            "/wp-includes/assets/",
            "/wp-includes/block-bindings/",
            "/wp-includes/block-patterns/",
            "/wp-includes/block-supports/",
            "/wp-includes/blocks/",
            "/wp-includes/certificates/",
            "/wp-includes/css/",
            "/wp-includes/customize/",
            "/wp-includes/fonts/",
            "/wp-includes/html-api/",
            "/wp-includes/images/",
            "/wp-includes/js/",
            "/wp-includes/pomo/",
            "/wp-includes/rest-api/",
            "/wp-includes/sitemaps/",
            "/wp-includes/style-engine/",
            "/wp-includes/widgets/",
            "/license.txt",
            "/WP-INCLUDES/js/"
    })
    void rejectsWordPressProbePaths(String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
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
