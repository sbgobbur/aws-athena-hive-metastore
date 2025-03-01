/*-
 * #%L
 * hms-lambda-handler
 * %%
 * Copyright (C) 2019 - 2022 Amazon Web Services
 * %%
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
 * #L%
 */
package com.amazonaws.athena.hms.handler;

import com.amazonaws.athena.hms.HiveMetaStoreClient;
import com.amazonaws.athena.hms.HiveMetaStoreConf;
import com.amazonaws.athena.hms.RenamePartitionRequest;
import com.amazonaws.athena.hms.RenamePartitionResponse;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.thrift.TDeserializer;

public class RenamePartitionHandler extends BaseHMSHandler<RenamePartitionRequest, RenamePartitionResponse>
{
    public RenamePartitionHandler(HiveMetaStoreConf conf, HiveMetaStoreClient client)
    {
        super(conf, client);
    }

    @Override
    public RenamePartitionResponse handleRequest(RenamePartitionRequest request, Context context)
    {
        HiveMetaStoreConf conf = getConf();
        try {
            if (request.getPartitionDesc() == null || request.getPartitionDesc().length() == 0) {
                context.getLogger().log("Rename partition: New Partition definition is missing");
                return new RenamePartitionResponse().setSuccessful(false);
            }
            context.getLogger().log("Connecting to HMS: " + conf.getMetastoreUri());
            HiveMetaStoreClient client = getClient();
            TDeserializer deserializer = new TDeserializer(getTProtocolFactory());
            Partition partition = new Partition();
            deserializer.fromString(partition, request.getPartitionDesc());

            client.renamePartition(request.getDbName(), request.getTableName(), request.getPartitionValue(), partition);
            context.getLogger().log("Rename partition to : " + partition);
            return new RenamePartitionResponse().setSuccessful(true);
        }
        catch (Exception e) {
            context.getLogger().log("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
