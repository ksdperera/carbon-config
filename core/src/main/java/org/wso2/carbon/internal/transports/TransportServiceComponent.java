/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.internal.transports;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.startupcoordinator.RequireCapabilityListener;
import org.wso2.carbon.transports.CarbonTransport;
import org.wso2.carbon.transports.TransportManager;

import java.util.Map;

/**
 * OSGi declarative services component which handled registration & uregistration of Carbon transports
 * and starts all the transports when all of them are registered
 */
@Component(
        name = "org.wso2.carbon.internal.transports.TransportServiceComponent",
        immediate = true,
        service = RequireCapabilityListener.class,
        property = "required-service-interface=org.wso2.carbon.transports.CarbonTransport"
)
public class TransportServiceComponent implements RequireCapabilityListener {
    private static final Logger logger = LoggerFactory.getLogger(TransportServiceComponent.class);

    private TransportManager transportManager = new TransportManager();

    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(TransportManager.class, transportManager, null);
    }

    @Reference(
            name = "carbon.transport",
            service = CarbonTransport.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterTransport"
    )
    protected void registerTransport(CarbonTransport transport, Map<String, ?> ref) {
        transportManager.registerTransport(transport);
    }

    protected void unregisterTransport(CarbonTransport transport, Map<String, ?> ref) {
        transportManager.unregisterTransport(transport);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting Registered Transports");
        }
        transportManager.startTransports();
    }
}
