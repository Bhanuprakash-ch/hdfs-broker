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
package org.trustedanalytics.servicebroker.hdfs.plans.provisioning;

import java.io.IOException;
import java.util.UUID;

import org.apache.hadoop.fs.Path;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.trustedanalytics.cfbroker.store.hdfs.helper.HdfsPathTemplateUtils;
import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;

@Component
class HdfsProvisioningClient
    implements HdfsDirectoryProvisioningOperations, HdfsPlanEncryptedDirectoryProvisioningOperations {

  private final HdfsClient hdfsClient;
  private final HdfsClient encryptedHdfsClient;
  private final String userspacePathTemplate;

  @Autowired
  public HdfsProvisioningClient(HdfsClient hdfsClient, HdfsClient encryptedHdfsClient, String userspacePathTemplate) {
    this.hdfsClient = hdfsClient;
    this.encryptedHdfsClient = encryptedHdfsClient;
    this.userspacePathTemplate = userspacePathTemplate;
  }

  @Override
  public void provisionDirectory(UUID instanceId, UUID orgId) throws ServiceBrokerException {
    try {
      String path = HdfsPathTemplateUtils.fill(userspacePathTemplate, instanceId, orgId);
      hdfsClient.createDir(path);
    } catch (IOException e) {
      throw new ServiceBrokerException("Unable to provision directory for: " + instanceId, e);
    }
  }

  @Override
  public void createEncryptedZone(UUID instanceId, UUID orgId) throws ServiceBrokerException {
    try {
      String path = HdfsPathTemplateUtils.fill(userspacePathTemplate, instanceId, orgId);
      encryptedHdfsClient.createKeyAndEncryptedZone(instanceId.toString(), new Path(path));
    } catch (IOException e) {
      throw new ServiceBrokerException("Unable to provision encrypted directory for: " + instanceId, e);
    }
  }
}
