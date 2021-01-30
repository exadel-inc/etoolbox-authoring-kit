package com.exadel.aem.toolkit.api.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.handlers.Source;


/**
 * Marker for fields of handlers being auto-initialized with a reference to {@link RuntimeContext}
 *
 * @deprecated Since AEM Authoring Toolkit v. 2.0.0 users are encouraged to use new custom handlers API that is based
 * on {@link Source} and {@link Target} objects handling. Legacy API will be revoked in the versions to come
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Deprecated
public @interface Injected {
}
