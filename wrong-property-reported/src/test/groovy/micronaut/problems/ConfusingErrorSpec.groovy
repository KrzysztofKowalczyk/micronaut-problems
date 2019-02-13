package micronaut.problems

import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.exceptions.HttpClientException
import spock.lang.Specification

/**
 * Actual problem is with property.b not being provided but used in a scheduled bean.
 * ScheduledTask is found and tries to start automatically but fails, because property.b is missing.
 * Then when trying to get other beans confusing errors are reported, different depending on
 * what is injected or which micronaut dependencies are on the classpath.
 */
class ConfusingErrorSpec extends Specification {

    def "no such bean reported"(){
        given:
        def context = ApplicationContext
            .build()
            .properties(
                "property.a": "https://x" // providing "property.a"
            )
            .build()
            .start()

        when:
        context.getBean(ClassUsingPropertyA).getProperty()

        then:
        noExceptionThrown()
        /*
            No bean of type [com.tesco.magicpipe.http.client.ClassUsingPropertyA] exists. Ensure the class is declared a bean and if you are using Java or Kotlin make sure you have enabled annotation processing.
            io.micronaut.context.exceptions.NoSuchBeanException: No bean of type [com.tesco.magicpipe.http.client.ClassUsingPropertyA] exists. Ensure the class is declared a bean and if you are using Java or Kotlin make sure you have enabled annotation processing.
                at io.micronaut.context.DefaultBeanContext.getBeanInternal(DefaultBeanContext.java:1609)
                at io.micronaut.context.DefaultBeanContext.getBean(DefaultBeanContext.java:501)
                at ConfusingErrorSpec.no such bean reported(ConfusingErrorSpec.groovy:18)
         */

        cleanup:
        context.close()
    }

    def "wrong property reported missing - ConfigurationException"(){
        given:
        def context = ApplicationContext
            .build()
            .properties(
                "property.a": "https://x" // providing "property.a"
            )
            .build()
            .start()

        when:
        def a = context.getBean(HttpClientUsingPropertyA).callSomething()

        then:
        a == "https://x"
        thrown(HttpClientException)

        /*
            Could not resolve placeholder ${property.a} in value: ${property.a}
            io.micronaut.context.exceptions.ConfigurationException: Could not resolve placeholder ${property.a} in value: ${property.a}
                at io.micronaut.context.env.DefaultPropertyPlaceholderResolver.resolveExpression(DefaultPropertyPlaceholderResolver.java:163)
                at io.micronaut.context.env.DefaultPropertyPlaceholderResolver.resolvePlaceholders(DefaultPropertyPlaceholderResolver.java:112)
                at io.micronaut.context.env.DefaultPropertyPlaceholderResolver.resolveRequiredPlaceholders(DefaultPropertyPlaceholderResolver.java:82)
                at io.micronaut.inject.annotation.EnvironmentConvertibleValuesMap.doResolveIfNecessary(EnvironmentConvertibleValuesMap.java:120)
                at io.micronaut.inject.annotation.EnvironmentConvertibleValuesMap.get(EnvironmentConvertibleValuesMap.java:79)
                at io.micronaut.core.annotation.AnnotationValue.get(AnnotationValue.java:162)
                at io.micronaut.core.annotation.AnnotationValue.getValue(AnnotationValue.java:180)
                at io.micronaut.core.annotation.AnnotationValue.getValue(AnnotationValue.java:202)
                at io.micronaut.http.client.interceptor.HttpClientIntroductionAdvice.getClient(HttpClientIntroductionAdvice.java:559)
                at io.micronaut.http.client.interceptor.HttpClientIntroductionAdvice.intercept(HttpClientIntroductionAdvice.java:163)
                at io.micronaut.aop.MethodInterceptor.intercept(MethodInterceptor.java:41)
                at io.micronaut.aop.chain.InterceptorChain.proceed(InterceptorChain.java:147)
                at io.micronaut.retry.intercept.RecoveryInterceptor.intercept(RecoveryInterceptor.java:74)
                at io.micronaut.aop.MethodInterceptor.intercept(MethodInterceptor.java:41)
                at io.micronaut.aop.chain.InterceptorChain.proceed(InterceptorChain.java:147)
                at ConfusingErrorSpec.wrong property reported - ConfigurationException(ConfusingErrorSpec.groovy:81)
         */

        cleanup:
        context.close()
    }

    // behaviour here would be different if 'io.micronaut:micronaut-http-client' would not be
    // on classpath, this is scary, see ConfusingErrorSpec2
    def "null value"(){
        given:
        def context = ApplicationContext
            .build()
            .properties(
            "property.a": "https://x" // providing "property.a"
        )
            .build()
            .start()

        when:
        def a = context.getBean(ClassUsingValueOfPropertyA).getProperty()

        then:
        a == "https://x" // a is null

        cleanup:
        context.close()
    }
}
