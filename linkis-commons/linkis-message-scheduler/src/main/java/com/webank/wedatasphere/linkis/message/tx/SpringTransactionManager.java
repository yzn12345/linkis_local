/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.message.tx;

import com.webank.wedatasphere.linkis.common.utils.JavaLog;
import com.webank.wedatasphere.linkis.message.utils.MessageUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;


public class SpringTransactionManager extends JavaLog implements TransactionManager {

    private final PlatformTransactionManager platformTransactionManager;

    public SpringTransactionManager() {
        platformTransactionManager = MessageUtils.getBean(PlatformTransactionManager.class);
    }


    @Override
    public Object begin() {
        if (platformTransactionManager != null) {
            return platformTransactionManager.getTransaction(new DefaultTransactionAttribute());
        }
        return null;
    }

    @Override
    public void commit(Object o) {
        if (o instanceof TransactionStatus && platformTransactionManager != null) {
            platformTransactionManager.commit((TransactionStatus) o);
        }
    }

    @Override
    public void rollback(Object o) {
        if (o instanceof TransactionStatus && platformTransactionManager != null) {
            platformTransactionManager.rollback((TransactionStatus) o);
        }
    }
}
