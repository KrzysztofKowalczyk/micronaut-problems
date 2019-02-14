package micronaut.problems

import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.exceptions.HttpClientException
import spock.lang.Specification

/**
 * Actual problem is with property.b not being provided but used in a scheduled bean.
 * ScheduledTask is found and tries to start automatically but fails, because property.b is missing.
 * Then when trying to get other beans confusing errors are reported, different depending on
 * what is injected or which micronaut dependencies are on the classpath.
 *
 * In logs there is another exception if one has proper debug and check all the logs:
 * ForkJoinPool.commonPool-worker-1] ERROR i.m.context.DefaultBeanContext - Error processing bean method Definition: micronaut.problems.ScheduledTask.void task() with processor (io.micronaut.scheduling.processor.ScheduledMethodProcessor@7131f599): Could not resolve placeholder ${property.b} in value: ${property.b}* io.micronaut.context.exceptions.ConfigurationException: Could not resolve placeholder ${property.b} in value: ${property.b}* 	at io.micronaut.context.env.DefaultPropertyPlaceholderResolver.resolveExpression(DefaultPropertyPlaceholderResolver.java:163)
 * 	at io.micronaut.context.env.DefaultPropertyPlaceholderResolver.resolvePlaceholders(DefaultPropertyPlaceholderResolver.java:112)
 * 	at io.micronaut.context.env.DefaultPropertyPlaceholderResolver.resolveRequiredPlaceholders(DefaultPropertyPlaceholderResolver.java:82)
 * 	at io.micronaut.inject.annotation.EnvironmentConvertibleValuesMap.doResolveIfNecessary(EnvironmentConvertibleValuesMap.java:120)
 * 	at io.micronaut.inject.annotation.EnvironmentConvertibleValuesMap.get(EnvironmentConvertibleValuesMap.java:79)
 * 	at io.micronaut.core.annotation.AnnotationValue.get(AnnotationValue.java:162)
 * 	at io.micronaut.core.value.ValueResolver.get(ValueResolver.java:54)
 * 	at io.micronaut.scheduling.processor.ScheduledMethodProcessor.process(ScheduledMethodProcessor.java:85)
 * 	at io.micronaut.context.DefaultBeanContext.lambda$null$24(DefaultBeanContext.java:1096)
 * 	at java.util.concurrent.ForkJoinTask$RunnableExecuteAction.exec(ForkJoinTask.java:1402)
 * 	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
 * 	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056)
 * 	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
 * 	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157)
 */
class ConfusingErrorSpec extends Specification {

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

        Thread.sleep(100) // stops it from succeeding sometimes, see "null value 2"

        when:
        def a = context.getBean(ClassUsingValueOfPropertyA).getProperty()

        then:
        a == "https://x" // a is null

        cleanup:
        context.close()
    }

    def "wrong property reported missing - DependencyInjectionException"(){
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
            io.micronaut.context.exceptions.DependencyInjectionException: Failed to inject value for field [property] of class: micronaut.problems.ClassUsingPropertyA

            Message: Error resolving field value [property.a]. Property doesn't exist or cannot be converted
            Path Taken: ClassUsingPropertyA.property
                at io.micronaut.context.AbstractBeanDefinition.getValueForField(AbstractBeanDefinition.java:1185)
                at io.micronaut.context.AbstractBeanDefinition.injectBeanField(AbstractBeanDefinition.java:717)
                at io.micronaut.context.DefaultBeanContext.doCreateBean(DefaultBeanContext.java:1319)
                at io.micronaut.context.DefaultBeanContext.createAndRegisterSingleton(DefaultBeanContext.java:1902)
                at io.micronaut.context.DefaultBeanContext.getBeanForDefinition(DefaultBeanContext.java:1623)
                at io.micronaut.context.DefaultBeanContext.getBeanInternal(DefaultBeanContext.java:1603)
                at io.micronaut.context.DefaultBeanContext.getBean(DefaultBeanContext.java:501)
                at micronaut.problems.ConfusingErrorSpec.no such bean reported(ConfusingErrorSpec.groovy:67)
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
        context.getBean(HttpClientUsingPropertyA).callSomething()

        then:
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

    // almost exactly the same test, but this one usually succeeds,
    // sometimes it might fail, race condition with scheduled task shutting down context
    def "null value passing"(){
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
        a == "https://x"

        cleanup:
        context.close()
    }
}
