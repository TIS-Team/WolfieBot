package pl.tispmc.wolfie.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration(proxyBeanMethods = false)
public class AsyncConfig
{
    @Bean
    public ApplicationEventMulticaster simpleApplicationEventMulticaster()
    {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();

        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setVirtualThreads(true);
        simpleAsyncTaskExecutor.setConcurrencyLimit(2);

        eventMulticaster.setTaskExecutor(simpleAsyncTaskExecutor);
        return eventMulticaster;
    }
}
