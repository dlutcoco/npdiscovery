package org.springframework.cloud.npdiscovery.ribbon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ConditionalOnProperty(value = "ribbon.netposa.enabled", matchIfMissing = true)
public @interface ConditionalOnRibbonNetposaDiscovery {

}
