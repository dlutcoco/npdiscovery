package org.springframework.cloud.npdiscovery.ribbon;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DependenciesPassedCondition extends SpringBootCondition {

    private static final String NETPOSA_DEPENDENCIES_PROP = "spring.cloud.netposa.discovery.dependencies";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> subProperties = new RelaxedPropertyResolver(context.getEnvironment()).getSubProperties(NETPOSA_DEPENDENCIES_PROP);
        if (!subProperties.isEmpty()) {
            return ConditionOutcome.match("Dependencies are defined in configuration");
        }
        Boolean dependenciesEnabled = context.getEnvironment()
                .getProperty("spring.cloud.netposa.discovery.dependency.enabled", Boolean.class, false);
        if (dependenciesEnabled) {
            return ConditionOutcome.match("Dependencies are not defined in configuration, but switch is turned on");
        }
        return ConditionOutcome.noMatch("No dependencies have been passed for the service");
    }
}