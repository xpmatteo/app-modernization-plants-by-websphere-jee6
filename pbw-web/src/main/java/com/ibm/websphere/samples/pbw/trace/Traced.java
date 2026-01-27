// ABOUTME: CDI interceptor binding annotation for entrypoint tracing.
// ABOUTME: Applied to methods that should be traced as semantic actions.

package com.ibm.websphere.samples.pbw.trace;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interceptor binding annotation for entrypoint tracing.
 * Methods annotated with @Traced will be logged when a trace scenario is active.
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Traced {

    /**
     * Optional action name. If not specified, the method name is used.
     */
    String action() default "";
}
