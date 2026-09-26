package pl.wrona.webserver.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgencyAccessSecurityTest {

    private final AgencyAccessSecurity security = new AgencyAccessSecurity();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerCanUseTheWholeAgency() {
        authenticate("/agencies/KOWAL/owners");

        assertThat(security.canRead("KOWAL")).isTrue();
        assertThat(security.canReadPlan("KOWAL")).isTrue();
        assertThat(security.canPlan("KOWAL")).isTrue();
        assertThat(security.canReadDispatch("KOWAL")).isTrue();
        assertThat(security.canDispatch("KOWAL")).isTrue();
        assertThat(security.canOwn("KOWAL")).isTrue();
        assertThat(security.canRead("NEXTSTOP")).isFalse();
    }

    @Test
    void viewerCanOnlyRead() {
        authenticate("/agencies/KOWAL/viewers");

        assertThat(security.canReadPlan("KOWAL")).isTrue();
        assertThat(security.canReadDispatch("KOWAL")).isTrue();
        assertThat(security.canPlan("KOWAL")).isFalse();
        assertThat(security.canDispatch("KOWAL")).isFalse();
        assertThat(security.canOwn("KOWAL")).isFalse();
    }

    @Test
    void plannerIsLimitedToRoutesTripsAndCalendars() {
        authenticate("/agencies/KOWAL/planners");

        assertThat(security.canReadPlan("KOWAL")).isTrue();
        assertThat(security.canPlan("KOWAL")).isTrue();
        assertThat(security.canReadDispatch("KOWAL")).isFalse();
        assertThat(security.canDispatch("KOWAL")).isFalse();
        assertThat(security.canOwn("KOWAL")).isFalse();
    }

    @Test
    void dispatcherIsLimitedToBrigades() {
        authenticate("/agencies/KOWAL/dispatchers");

        assertThat(security.canReadDispatch("KOWAL")).isTrue();
        assertThat(security.canDispatch("KOWAL")).isTrue();
        assertThat(security.canReadPlan("KOWAL")).isFalse();
        assertThat(security.canPlan("KOWAL")).isFalse();
        assertThat(security.canOwn("KOWAL")).isFalse();
    }

    private void authenticate(String group) {
        var authentication = new UsernamePasswordAuthenticationToken(
                "user",
                null,
                List.of(new SimpleGrantedAuthority("GROUP_" + group)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
