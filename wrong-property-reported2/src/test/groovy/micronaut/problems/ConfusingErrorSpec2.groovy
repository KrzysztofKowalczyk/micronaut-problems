package micronaut.problems

import io.micronaut.context.ApplicationContext
import spock.lang.Specification

/**
 * Actual problem is with property.b not being provided but used in a scheduled bean.
 * ScheduledTask is found and tries to start automatically but fails, because property.b is missing.
 * Then when trying to get other beans confusing errors are reported, different depending on
 * what is injected or which micronaut dependencies are on the classpath.
 */
class ConfusingErrorSpec2 extends Specification {

    /*
        Code here is identical with ConfusingErrorSpec."null value",
        but the difference here is in gradle dependencies, this one does not have
        compile 'io.micronaut:micronaut-http-client'
     */
    def "wrong property reported - DependencyInjectionException"(){
        given:
        def context = ApplicationContext
            .build()
            .properties(
                "property.a": "https://x" // providing "property.a"
            )
            .build()
            .start()

        when:
        context.getBean(ClassUsingValueOfPropertyA).getProperty()

        then:
        noExceptionThrown()

        /*
            io.micronaut.context.exceptions.DependencyInjectionException: Failed to inject value for field [property] of class: micronaut.problems.ClassUsingValueOfPropertyA
            Message: Error resolving field value [${property.a}]. Property doesn't exist or cannot be converted
            Path Taken: ClassUsingValueOfPropertyA.property
                at io.micronaut.context.AbstractBeanDefinition.getValueForField(AbstractBeanDefinition.java:1185)
                at io.micronaut.context.AbstractBeanDefinition.injectBeanField(AbstractBeanDefinition.java:717)
                at io.micronaut.context.DefaultBeanContext.doCreateBean(DefaultBeanContext.java:1319)
                at io.micronaut.context.DefaultBeanContext.getScopedBeanForDefinition(DefaultBeanContext.java:1721)
                at io.micronaut.context.DefaultBeanContext.getBeanForDefinition(DefaultBeanContext.java:1625)
                at io.micronaut.context.DefaultBeanContext.getBeanInternal(DefaultBeanContext.java:1603)
                at io.micronaut.context.DefaultBeanContext.getBean(DefaultBeanContext.java:501)
                at micronaut.problems.ConfusingErrorSpec.wrong property reported - DependencyInjectionException(ConfusingErrorSpec2.groovy:24)
         */

        cleanup:
        context.close()
    }
}
