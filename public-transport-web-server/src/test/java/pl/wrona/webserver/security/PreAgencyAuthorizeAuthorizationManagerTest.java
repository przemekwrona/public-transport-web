package pl.wrona.webserver.security;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.util.SimpleMethodInvocation;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreAgencyAuthorizeAuthorizationManagerTest {

    @Mock
    private AgencyAccessSecurity agencyAccessSecurity;

    @Mock
    private Authentication authentication;

    @Test
    void usesInstanceParameterByDefault() throws Exception {
        when(agencyAccessSecurity.checkAccess("demo")).thenReturn(true);

        AuthorizationDecision decision = manager().check(() -> authentication, invocation("readByInstance", "demo"));

        assertThat(decision.isGranted()).isTrue();
        verify(agencyAccessSecurity).checkAccess("demo");
    }

    @Test
    void grantsAccessWhenUserHasOneOfTheRoles() throws Exception {
        when(agencyAccessSecurity.checkAccess("demo")).thenReturn(true);
        when(agencyAccessSecurity.hasRole("demo", "SUPER_USER")).thenReturn(true);

        AuthorizationDecision decision = manager().check(() -> authentication, invocation("readWithRoles", "demo"));

        assertThat(decision.isGranted()).isTrue();
        verify(agencyAccessSecurity).hasRole("demo", "SUPER_USER");
    }

    @Test
    void deniesAccessWhenUserHasNoneOfTheRoles() throws Exception {
        when(agencyAccessSecurity.checkAccess("demo")).thenReturn(true);
        when(agencyAccessSecurity.hasRole("demo", "SUPER_USER")).thenReturn(false);

        AuthorizationDecision decision = manager().check(() -> authentication, invocation("readWithRoles", "demo"));

        assertThat(decision.isGranted()).isFalse();
    }

    private PreAgencyAuthorizeAuthorizationManager manager() {
        return new PreAgencyAuthorizeAuthorizationManager(agencyAccessSecurity);
    }

    private MethodInvocation invocation(String methodName, String agencyCode) throws Exception {
        Method method = Sample.class.getDeclaredMethod(methodName, String.class);
        return new SimpleMethodInvocation(new Sample(), method, agencyCode);
    }

    static class Sample {

        @PreAgencyAuthorize
        void readByInstance(String instance) {
        }

        @PreAgencyAuthorize(roles = PreAgencyAuthorize.Role.SUPER_USER)
        void readWithRoles(String instance) {
        }

    }

}
