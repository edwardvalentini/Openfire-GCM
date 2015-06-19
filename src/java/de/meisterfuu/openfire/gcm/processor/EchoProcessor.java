/*
 * Copyright 2014 Wolfram Rittmeyer.
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
package de.meisterfuu.openfire.gcm.processor;

import de.meisterfuu.openfire.gcm.CcsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Handles an echo request.
 */
public class EchoProcessor implements PayloadProcessor{
    private static final Logger Log = LoggerFactory.getLogger(EchoProcessor.class);


    @Override
    public void handleMessage(CcsMessage msg) {
        Log.info("[EchoProcessor] ", msg);

        /*
        String msgId = dao.getUniqueMessageId();
        String jsonRequest =
                CcsClient.createJsonMessage(
                        msg.getFrom(), 
                        msgId, 
                        msg.getPayload(), 
                        null, 
                        null, // TTL (null -> default-TTL) 
                        false);
        client.send(jsonRequest);
        */
    }

}
