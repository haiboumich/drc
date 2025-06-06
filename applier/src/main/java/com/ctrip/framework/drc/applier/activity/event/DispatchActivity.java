package com.ctrip.framework.drc.applier.activity.event;

import com.ctrip.framework.drc.applier.event.transaction.Transaction;
import com.ctrip.framework.drc.fetcher.resource.condition.LWM;
import com.ctrip.framework.drc.fetcher.resource.condition.LWMPassHandler;
import com.ctrip.framework.drc.fetcher.system.InstanceConfig;
import com.ctrip.framework.drc.fetcher.system.InstanceResource;
import com.ctrip.framework.drc.fetcher.activity.event.EventActivity;

/**
 * @Author Slight
 * Jul 07, 2020
 */
public class DispatchActivity extends EventActivity<Transaction, Transaction> {

    @InstanceResource
    public LWM lwm;

    @InstanceConfig(path = "registryKey")
    public String registryKey;

    @Override
    public Transaction doTask(Transaction transaction) throws InterruptedException {
        loggerTL.info("[{}] dispatch {}", registryKey, transaction.identifier());
        transaction.begin(lwm, new BeginHandler(transaction, this::hand));
        return null;
    }


    @FunctionalInterface
    private interface DispatchFunction {
        void onBegin(Transaction transaction) throws InterruptedException;
    }

    private static class BeginHandler implements LWMPassHandler {

        private final Transaction transaction;
        private final DispatchFunction func;

        private BeginHandler(Transaction transaction, DispatchFunction func) {
            this.transaction = transaction;
            this.func = func;
        }

        @Override
        public void onBegin() throws InterruptedException {
            func.onBegin(transaction);
        }

        @Override
        public void close() {
            try {
                transaction.close();
            } catch (Exception e) {
                loggerTL.warn("close transaction fail ", e);
            }
        }
    }
}
