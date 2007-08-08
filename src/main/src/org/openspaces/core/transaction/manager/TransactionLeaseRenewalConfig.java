/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core.transaction.manager;

import net.jini.lease.LeaseListener;

/**
 * @author kimchy
 */
public class TransactionLeaseRenewalConfig {

    private int poolSize = 1;

    private long renewRTT = 1000;

    private long renewDuration = 2000;

    private LeaseListener leaseListener;

    public long getRenewDuration() {
        return renewDuration;
    }

    public void setRenewDuration(long renewDuration) {
        this.renewDuration = renewDuration;
    }

    public long getRenewRTT() {
        return renewRTT;
    }

    public void setRenewRTT(long renewRTT) {
        this.renewRTT = renewRTT;
    }

    public LeaseListener getLeaseListener() {
        return leaseListener;
    }

    public void setLeaseListener(LeaseListener leaseListener) {
        this.leaseListener = leaseListener;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
