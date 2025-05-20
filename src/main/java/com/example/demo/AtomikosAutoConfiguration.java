/**
 * Copyright (C) 2000-2023 Atomikos <info@atomikos.com>
 * <p>
 * LICENSE CONDITIONS
 * <p>
 * See http://www.atomikos.com/Main/WhichLicenseApplies for details.
 */

package com.example.demo;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.spring.*;
import jakarta.jms.Message;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.util.Properties;

/**
 * This is a copy of {@link com.atomikos.spring.AtomikosAutoConfiguration} which fixed spring boot 3.4 compatibility
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({SpringJtaAtomikosProperties.class, AtomikosProperties.class})
@ConditionalOnClass({JtaTransactionManager.class, UserTransactionManager.class})
@ConditionalOnMissingBean(org.springframework.transaction.TransactionManager.class)
@AutoConfigureBefore(
        value = {com.atomikos.spring.AtomikosAutoConfiguration.class, ActiveMQAutoConfiguration.class, XADataSourceAutoConfiguration.class, ArtemisAutoConfiguration.class, HibernateJpaAutoConfiguration.class},
        name = "org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration")
@Slf4j
public class AtomikosAutoConfiguration {

    @Bean(initMethod = "init", destroyMethod = "shutdownWait")
    @ConditionalOnMissingBean(UserTransactionService.class)
    UserTransactionServiceImp userTransactionService(SpringJtaAtomikosProperties springJtaAtomikosProperties, AtomikosProperties atomikosProperties) {
        log.info("creating UserTransactionService");
        Properties properties = new Properties();
        properties.putAll(springJtaAtomikosProperties.asProperties());
        properties.putAll(atomikosProperties.asProperties());
        return new UserTransactionServiceImp(properties);
    }


    @Bean(initMethod = "init", destroyMethod = "close")
    @ConditionalOnMissingBean(TransactionManager.class)
    UserTransactionManager atomikosTransactionManager(UserTransactionService userTransactionService) throws Exception {
        log.info("creating UserTransactionManager");
        UserTransactionManager manager = new UserTransactionManager();
        manager.setStartupTransactionService(false);
        manager.setForceShutdown(true);
        return manager;
    }

    @Bean
    @ConditionalOnMissingBean(XADataSourceWrapper.class)
    AtomikosXADataSourceWrapper xaDataSourceWrapper() {
        log.info("creating AtomikosXADataSourceWrapper");
        return new AtomikosXADataSourceWrapper();
    }

    @Bean
    @ConditionalOnMissingBean
    static AtomikosDependsOnBeanFactoryPostProcessor atomikosDependsOnBeanFactoryPostProcessor() {
        log.info("creating AtomikosDependsOnBeanFactoryPostProcessor");
        return new AtomikosDependsOnBeanFactoryPostProcessor();
    }

    @Bean
    JtaTransactionManager transactionManager(UserTransaction userTransaction, TransactionManager transactionManager,
                                             ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        log.info("creating JtaTransactionManager");
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, transactionManager);
        transactionManagerCustomizers.ifAvailable((customizers) -> customizers.customize(jtaTransactionManager));
        return jtaTransactionManager;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({Message.class, JtaTransactionManager.class, UserTransactionManager.class})
    static class AtomikosJtaJmsConfiguration {

        @Bean
        @ConditionalOnMissingBean(XAConnectionFactoryWrapper.class)
        AtomikosXAConnectionFactoryWrapper xaConnectionFactoryWrapper() {
            log.info("creating AtomikosXAConnectionFactoryWrapper");
            return new AtomikosXAConnectionFactoryWrapper();
        }

    }

}
