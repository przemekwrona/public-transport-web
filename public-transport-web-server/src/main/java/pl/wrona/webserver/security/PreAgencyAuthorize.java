package pl.wrona.webserver.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreAgencyAuthorize {

    /**
     * Roles that grant access. The user must have at least one of them.
     * An empty list does not restrict by role.
     */
    Role[] roles() default {};

    enum Role {
        SUPER_USER,
        AGENCY_OWNER,
        AGENCY_ADMIN,
        PLANNER,
        BRIGADIER,
        DISPATCHER,
        DRIVER
    }

}
