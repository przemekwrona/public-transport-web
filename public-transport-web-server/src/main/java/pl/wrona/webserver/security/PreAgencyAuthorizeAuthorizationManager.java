package pl.wrona.webserver.security;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.stream.Stream;

class PreAgencyAuthorizeAuthorizationManager implements AuthorizationManager<MethodInvocation> {

    private final AgencyAccessSecurity agencyAccessSecurity;
    private final MethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
    private final Expression instanceExpression = expressionHandler.getExpressionParser().parseExpression("#instance");

    PreAgencyAuthorizeAuthorizationManager(AgencyAccessSecurity agencyAccessSecurity) {
        this.agencyAccessSecurity = agencyAccessSecurity;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation invocation) {
        Method method = AopUtils.getMostSpecificMethod(invocation.getMethod(), targetClass(invocation));
        PreAgencyAuthorize annotation = findAnnotation(method);
        if (annotation == null) {
            return null;
        }

        EvaluationContext context = expressionHandler.createEvaluationContext(authentication, invocation);
        Object agencyCode = instanceExpression.getValue(context);
        boolean granted = agencyCode instanceof String code
                && agencyAccessSecurity.checkAccess(code)
                && hasRequiredRole(code, annotation.roles());
        return new AuthorizationDecision(granted);
    }

    private boolean hasRequiredRole(String agencyCode, PreAgencyAuthorize.Role[] roles) {
        if (roles.length == 0) {
            return true;
        }
        return Stream.of(roles)
                .anyMatch(role -> agencyAccessSecurity.hasRole(agencyCode, role.name()));
    }

    private PreAgencyAuthorize findAnnotation(Method method) {
        return AnnotationUtils.findAnnotation(method, PreAgencyAuthorize.class);
    }

    private Class<?> targetClass(MethodInvocation invocation) {
        Object target = invocation.getThis();
        return target != null ? target.getClass() : invocation.getMethod().getDeclaringClass();
    }

}
