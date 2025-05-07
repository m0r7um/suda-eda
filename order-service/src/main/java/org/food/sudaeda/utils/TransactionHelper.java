package org.food.sudaeda.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@RequiredArgsConstructor
public class TransactionHelper {
    private final PlatformTransactionManager txManager;

    public TransactionStatus createTransaction(String transactionName) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(transactionName);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return txManager.getTransaction(def);
    }

    public void commit(TransactionStatus status) {
        txManager.commit(status);
    }

    public void rollback(TransactionStatus status) {
        txManager.rollback(status);
    }
}
