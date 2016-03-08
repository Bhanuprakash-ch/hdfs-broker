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
package org.trustedanalytics.servicebroker.hdfs.plans;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceInstance;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;
import org.trustedanalytics.servicebroker.hdfs.plans.binding.HdfsBindingClientFactory;
import org.trustedanalytics.servicebroker.hdfs.plans.provisioning.HdfsProvisioningClientFactory;

@RunWith(MockitoJUnitRunner.class)
public final class HdfsPlanEncryptedTest extends HdfsPlanTestBase {

  private HdfsPlanEncrypted planUnderTest;

  @Mock
  private HdfsClient hdfsClient;

  @Mock
  private HdfsClient encryptedHdfsClient;

  @Before
  public void setup() {
    planUnderTest = new HdfsPlanEncrypted(
        HdfsProvisioningClientFactory.create(hdfsClient, encryptedHdfsClient, USERSPACE_PATH_TEMPLATE),
        HdfsBindingClientFactory.create(getInputCredentials(), USERSPACE_PATH_TEMPLATE));
  }

  @Test
  public void provision_templateWithOrgAndInstanceVariables_replaceVariablesWithValuesAndCreateDirAndEncryptedZone()
      throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    planUnderTest.provision(serviceInstance);

    verify(hdfsClient).createDir(getDirectoryPathToProvision(serviceInstance));
    verify(encryptedHdfsClient).createKeyAndEncryptedZone(serviceInstance.getServiceInstanceId(),
        new Path(getDirectoryPathToProvision(serviceInstance)));
    verifyNoMoreInteractions(hdfsClient, encryptedHdfsClient);
  }

  @Test(expected = ServiceBrokerException.class)
  public void provision_hdfsClientFails_rethrowAsServiceBrokerException() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    doThrow(new IOException()).when(hdfsClient).createDir(getDirectoryPathToProvision(serviceInstance));
    planUnderTest.provision(serviceInstance);
  }

  @Test(expected = ServiceBrokerException.class)
  public void provision_encryptedHdfsClientFails_rethrowAsServiceBrokerException() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    doThrow(new IOException()).when(encryptedHdfsClient).createKeyAndEncryptedZone(
        serviceInstance.getServiceInstanceId(), new Path(getDirectoryPathToProvision(serviceInstance)));
    planUnderTest.provision(serviceInstance);
  }

  @Test
  public void bind_templateWithOrgAndInstanceVariables_replaceVariablesWithValuesAndAppendUriToCredentialsMap()
      throws Exception {

    ServiceInstance serviceInstance = getServiceInstance();
    Map<String, Object> actualOutputCredentials = planUnderTest.bind(serviceInstance);
    assertThat(actualOutputCredentials, equalTo(getExpectedOutputCredentialsMap(serviceInstance)));
  }
}
