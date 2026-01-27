// ABOUTME: CDI interceptor that captures semantic action names for tracing.
// ABOUTME: Logs class/method information when @Traced methods are invoked.

package com.ibm.websphere.samples.pbw.trace;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * CDI interceptor that captures entrypoint (semantic action) information.
 * Applied to methods annotated with @Traced.
 */
@Interceptor
@Traced
@Priority(Interceptor.Priority.APPLICATION)
public class EntrypointTraceInterceptor {

    @AroundInvoke
    public Object traceEntrypoint(InvocationContext ctx) throws Exception {
        if (TraceContext.isActive()) {
            String className = getSimpleClassName(ctx.getTarget());
            String methodName = ctx.getMethod().getName();
            String action = getActionName(ctx.getMethod());

            TraceWriter.writeEntrypoint(className, methodName, action);
        }

        return ctx.proceed();
    }

    private String getSimpleClassName(Object target) {
        if (target == null) {
            return "Unknown";
        }

        Class<?> clazz = target.getClass();
        String name = clazz.getSimpleName();

        // Handle CDI proxy classes (e.g., AccountBean$Proxy$_$$_WeldSubclass)
        if (name.contains("$")) {
            // Get the superclass which should be the actual bean
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                name = superClass.getSimpleName();
            }
        }

        return name;
    }

    private String getActionName(Method method) {
        Traced traced = method.getAnnotation(Traced.class);
        if (traced != null && !traced.action().isEmpty()) {
            return traced.action();
        }

        // Check class-level annotation
        Class<?> declaringClass = method.getDeclaringClass();
        traced = declaringClass.getAnnotation(Traced.class);
        if (traced != null && !traced.action().isEmpty()) {
            return traced.action();
        }

        // Derive action from method name
        return deriveActionFromMethodName(method.getName());
    }

    private String deriveActionFromMethodName(String methodName) {
        // Remove common prefixes like "perform", "do", "handle"
        if (methodName.startsWith("perform")) {
            String action = methodName.substring(7);
            return lowercaseFirstChar(action);
        }
        if (methodName.startsWith("do")) {
            String action = methodName.substring(2);
            return lowercaseFirstChar(action);
        }
        if (methodName.startsWith("handle")) {
            String action = methodName.substring(6);
            return lowercaseFirstChar(action);
        }

        return methodName;
    }

    private String lowercaseFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
