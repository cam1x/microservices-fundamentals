package com.chachotkin.resource.service.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SpringTransactionHooks implements BeanFactoryAware {

    private BeanFactory beanFactory;
    private TransactionStatus transactionStatus;

    @Override
    public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Before(value = "@txn", order = 100)
    public void startTransaction() {
        transactionStatus = obtainPlatformTransactionManager()
                .getTransaction(new DefaultTransactionDefinition());
    }

    @After(value = "@txn", order = 100)
    public void rollBackTransaction() {
        obtainPlatformTransactionManager()
                .rollback(transactionStatus);
    }

    private PlatformTransactionManager obtainPlatformTransactionManager() {
        return beanFactory.getBean(PlatformTransactionManager.class);
    }
}
