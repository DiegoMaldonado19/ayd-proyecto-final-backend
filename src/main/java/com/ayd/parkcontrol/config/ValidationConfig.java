package com.ayd.parkcontrol.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;

import java.util.Locale;

@Configuration
public class ValidationConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/validation");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.of("es", "GT"));
        messageSource.setCacheSeconds(3600);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.setValidationMessageSource(messageSource());
        return validatorFactory;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}