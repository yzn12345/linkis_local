/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.core.interactor.execution.observer.event;

import com.webank.wedatasphere.linkis.cli.core.interactor.execution.observer.listener.LinkisClientObserver;

/**
 * @description: simplified version of Observer pattern (currently we don't need a full version)
 */
public abstract class SingleObserverEvent implements LinkisClientEvent {
    private LinkisClientObserver observer;

    @Override
    public boolean isRegistered() {
        return observer != null;
    }

    @Override
    public void register(LinkisClientObserver observer) {
        this.observer = observer;
    }

    @Override
    public void unRegister(LinkisClientObserver observer) {
        this.observer = null;
    }

    @Override
    public void notifyObserver(LinkisClientEvent event, Object message) {
        observer.update(event, message);
    }
}