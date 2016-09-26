/*
 * Copyright (c) 2016 Cisco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fd.vpp.jvpp.core.test;

import static io.fd.vpp.jvpp.core.test.NotificationUtils.getChangeInterfaceState;
import static io.fd.vpp.jvpp.core.test.NotificationUtils.getDisableInterfaceNotificationsReq;
import static io.fd.vpp.jvpp.core.test.NotificationUtils.getEnableInterfaceNotificationsReq;
import static io.fd.vpp.jvpp.core.test.NotificationUtils.printNotification;

import io.fd.vpp.jvpp.JVpp;
import io.fd.vpp.jvpp.JVppRegistry;
import io.fd.vpp.jvpp.JVppRegistryImpl;
import io.fd.vpp.jvpp.VppCallbackException;
import io.fd.vpp.jvpp.core.JVppCoreImpl;
import io.fd.vpp.jvpp.core.callback.SwInterfaceSetFlagsCallback;
import io.fd.vpp.jvpp.core.callback.SwInterfaceSetFlagsNotificationCallback;
import io.fd.vpp.jvpp.core.callback.WantInterfaceEventsCallback;
import io.fd.vpp.jvpp.core.dto.SwInterfaceSetFlagsNotification;
import io.fd.vpp.jvpp.core.dto.SwInterfaceSetFlagsReply;
import io.fd.vpp.jvpp.core.dto.WantInterfaceEventsReply;

public class CallbackNotificationApiTest {

    private static class TestCallback implements SwInterfaceSetFlagsNotificationCallback,
            WantInterfaceEventsCallback, SwInterfaceSetFlagsCallback {

        @Override
        public void onSwInterfaceSetFlagsNotification(
                final SwInterfaceSetFlagsNotification msg) {
            printNotification(msg);
        }

        @Override
        public void onWantInterfaceEventsReply(final WantInterfaceEventsReply wantInterfaceEventsReply) {
            System.out.println("Interface notification stream updated");
        }

        @Override
        public void onSwInterfaceSetFlagsReply(final SwInterfaceSetFlagsReply swInterfaceSetFlagsReply) {
            System.out.println("Interface flags set successfully");
        }

        @Override
        public void onError(VppCallbackException ex) {
            System.out.printf("Received onError exception in getNodeIndexCallback: call=%s, reply=%d, context=%d\n",
                    ex.getMethodName(), ex.getErrorCode(), ex.getCtxId());

        }
    }

    private static void testCallbackApi() throws Exception {
        System.out.println("Testing Java callback API for notifications");
        JVppRegistry registry = new JVppRegistryImpl("CallbackNotificationTest");
        JVpp jvpp = new JVppCoreImpl();

        registry.register(jvpp, new TestCallback());
        System.out.println("Successfully connected to VPP");

        getEnableInterfaceNotificationsReq().send(jvpp);
        System.out.println("Interface notifications started");
        // TODO test ifc dump which also triggers interface flags send

        System.out.println("Changing interface configuration");
        getChangeInterfaceState().send(jvpp);

        // Notifications are received
        Thread.sleep(500);

        getDisableInterfaceNotificationsReq().send(jvpp);
        System.out.println("Interface events stopped");

        Thread.sleep(2000);

        System.out.println("Disconnecting...");
        registry.close();
        Thread.sleep(1000);
    }

    public static void main(String[] args) throws Exception {
        testCallbackApi();
    }
}