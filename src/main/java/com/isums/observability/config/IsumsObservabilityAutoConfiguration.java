package com.isums.observability.config;

import java.util.HashMap;
import java.util.Map;

import com.isums.observability.audit.AuditPublisher;
import com.isums.observability.audit.AuditPublisherAspect;
import com.isums.observability.audit.KafkaAuditPublisher;
import com.isums.observability.audit.NoopAuditPublisher;
import com.isums.observability.http.HttpAuditFilter;
import com.isums.observability.http.RequestCorrelationFilter;
import com.isums.observability.kafka.ContextPropagatingProducerInterceptor;
import io.micrometer.tracing.Tracer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

@AutoConfiguration
@EnableConfigurationProperties(ObservabilityProperties.class)
public class IsumsObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "isums.observability", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RequestCorrelationFilter> requestCorrelationFilter(
            ObservabilityProperties properties,
            ObjectProvider<Tracer> tracerProvider) {
        FilterRegistrationBean<RequestCorrelationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestCorrelationFilter(properties, tracerProvider));
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 100);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnProperty(prefix = "isums.observability.audit", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<HttpAuditFilter> httpAuditFilter(
            ObservabilityProperties properties,
            AuditPublisher auditPublisher) {
        FilterRegistrationBean<HttpAuditFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HttpAuditFilter(properties, auditPublisher));
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 90);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(prefix = "isums.observability.audit", name = "enabled", havingValue = "true")
    public AuditPublisher kafkaAuditPublisher(
            Environment environment,
            ObservabilityProperties properties,
            ObjectProvider<Tracer> tracerProvider) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, properties.getAudit().getProducerRetries());
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, ContextPropagatingProducerInterceptor.class.getName());
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
        return new KafkaAuditPublisher(kafkaTemplate, properties, tracerProvider);
    }

    @Bean
    @ConditionalOnMissingBean(AuditPublisher.class)
    public AuditPublisher noopAuditPublisher() {
        return new NoopAuditPublisher();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditPublisherAspect auditPublisherAspect(AuditPublisher auditPublisher) {
        return new AuditPublisherAspect(auditPublisher);
    }
}
