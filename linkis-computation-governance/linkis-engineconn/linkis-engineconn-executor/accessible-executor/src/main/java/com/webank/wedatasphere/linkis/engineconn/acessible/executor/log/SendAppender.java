/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.log;

import com.webank.wedatasphere.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration;
import com.webank.wedatasphere.linkis.engineconn.executor.listener.EngineConnSyncListenerBus;
import com.webank.wedatasphere.linkis.engineconn.executor.listener.ExecutorListenerBusContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Plugin(name = "Send", category = "Core", elementType = "appender", printObject = true)
public class SendAppender extends AbstractAppender {

    /**
     * @fields serialVersionUID
     */
    private static final long serialVersionUID = -830237775522429777L;

    private static EngineConnSyncListenerBus engineConnSyncListenerBus = ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnSyncListenerBus();

    private LogCache logCache;
    private static final Logger logger = LoggerFactory.getLogger(SendAppender.class);

    private static final String IGNORE_WORDS = AccessibleExecutorConfiguration.ENGINECONN_IGNORE_WORDS().getValue();

    private static final String[] IGNORE_WORD_ARR = IGNORE_WORDS.split(",");

    private static final String PASS_WORDS = AccessibleExecutorConfiguration.ENGINECONN_PASS_WORDS().getValue();

    private static final String[] PASS_WORDS_ARR = PASS_WORDS.split(",");

    public SendAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
                        final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.logCache = LogHelper.logCache();
        //SendThread thread = new SendThread();
        logger.info("SendAppender init success");
        //TIMER.schedule(thread, 2000, (Integer) AccessibleExecutorConfiguration.ENGINECONN_LOG_SEND_TIME_INTERVAL().getValue());
    }


    @Override
    public void append(LogEvent event) {
        if (engineConnSyncListenerBus == null) {
            return;
        }
        String logStr = new String(getLayout().toByteArray(event));
        if (event.getLevel().intLevel() == Level.INFO.intLevel()) {
            boolean flag = false;
            for (String ignoreLog : IGNORE_WORD_ARR) {
                if (logStr.contains(ignoreLog)) {
                    flag = true;
                    break;
                }
            }
            for (String word : PASS_WORDS_ARR) {
                if (logStr.contains(word)) {
                    flag = false;
                    break;
                }
            }
            if (!flag) {
                logCache.cacheLog(logStr);
            }
        } else {
            logCache.cacheLog(logStr);
        }
    }

    @PluginFactory
    public static SendAppender createAppender(@PluginAttribute("name") String name,
                                              @PluginElement("Filter") final Filter filter,
                                              @PluginElement("Layout") Layout<? extends Serializable> layout,
                                              @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        if (name == null) {
            LOGGER.error("No name provided for SendAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new SendAppender(name, filter, layout, ignoreExceptions);
    }

}