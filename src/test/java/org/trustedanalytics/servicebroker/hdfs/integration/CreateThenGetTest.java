/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.servicebroker.hdfs.integration;

import org.trustedanalytics.servicebroker.hdfs.config.Application;
import org.trustedanalytics.servicebroker.hdfs.integration.config.HdfsLocalConfiguration;
import org.trustedanalytics.servicebroker.hdfs.integration.utils.CfModelsAssert;
import org.trustedanalytics.servicebroker.hdfs.integration.utils.CfModelsFactory;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, HdfsLocalConfiguration.class})
@WebAppConfiguration
@IntegrationTest("server.port=0")
@ActiveProfiles("integration-test")
public class CreateThenGetTest {

    @Autowired
    private ServiceInstanceService serviceBean;

    @Test
    public void getServiceInstance_instanceCreated_returnsInstance() throws Exception {

        String testId = "id2";

        //arrange
        ServiceInstance instance = CfModelsFactory.getServiceInstance(testId);
        CreateServiceInstanceRequest request = new CreateServiceInstanceRequest(
            CfModelsFactory.getServiceDefinition().getId(),
            instance.getPlanId(),
            instance.getOrganizationGuid(),
            instance.getSpaceGuid()).withServiceInstanceId(
            instance.getServiceInstanceId()).withServiceDefinition(
            CfModelsFactory.getServiceDefinition());

        serviceBean.createServiceInstance(request);

        //act
        ServiceInstance savedInstance = serviceBean.getServiceInstance(testId);

        //assert
        CfModelsAssert.serviceInstancesAreEqual(savedInstance, instance);
    }
}
