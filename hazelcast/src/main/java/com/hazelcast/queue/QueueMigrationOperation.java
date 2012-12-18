/*
 * Copyright (c) 2008-2012, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.queue;

import com.hazelcast.config.QueueConfig;
import com.hazelcast.spi.AbstractOperation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: ali
 * Date: 11/21/12
 * Time: 11:23 AM
 */

public class QueueMigrationOperation extends AbstractOperation {

    Map<String, QueueContainer> migrationData;

    public QueueMigrationOperation() {
    }

    public QueueMigrationOperation(Map<String, QueueContainer> migrationData, int partitionId, int replicaIndex) {
        setPartitionId(partitionId).setReplicaIndex(replicaIndex);
        this.migrationData = migrationData;
    }

    public void run() {
        QueueService service = getService();
        for (Map.Entry<String, QueueContainer> entry : migrationData.entrySet()) {
            String name = entry.getKey();
            QueueContainer container = entry.getValue();
            QueueConfig conf = getNodeEngine().getConfig().getQueueConfig(name);
            System.out.println(conf.getQueueStoreConfig() + " store");
            container.setConfig(conf);
            service.addContainer(name, container);
        }
    }

    protected void writeInternal(DataOutput out) throws IOException {
        out.writeInt(migrationData.size());
        for (Map.Entry<String, QueueContainer> entry : migrationData.entrySet()) {
            out.writeUTF(entry.getKey());
            QueueContainer container = entry.getValue();
            container.writeData(out);
        }
    }

    protected void readInternal(DataInput in) throws IOException {
        int mapSize = in.readInt();
        migrationData = new HashMap<String, QueueContainer>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            String name = in.readUTF();
            QueueContainer container = new QueueContainer();
            container.readData(in);
            migrationData.put(name, container);
        }
    }

    public String getServiceName() {
        return QueueService.NAME;
    }
}
