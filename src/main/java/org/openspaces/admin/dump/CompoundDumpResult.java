/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.dump;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.internal.dump.InternalDumpResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipOutputStream;

/**
 * A dump results that holds one or more Dump results.
 *
 * @author kimchy
 */
public class CompoundDumpResult implements DumpResult {

    private List<DumpResult> dumpResults = new CopyOnWriteArrayList<DumpResult>();

    public CompoundDumpResult add(DumpResult dumpResult) {
        dumpResults.add(dumpResult);
        return this;
    }

    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder();
        for (DumpResult dumpResult : dumpResults) {
            sb.append(dumpResult.getName()).append(",");
        }
        return sb.toString();
    }

    public DumpResult[] getResults() {
        return dumpResults.toArray(new DumpResult[dumpResults.size()]);
    }

    @Override
    public long downloadSize() {
        long downloadSize = 0;
        for (DumpResult dumpResult : dumpResults) {
            downloadSize += dumpResult.downloadSize();
        }
        return downloadSize;
    }

    @Override
    public void download(File targetDirectory, String fileName, DumpDownloadListener listener) throws AdminException {
        if (!fileName.endsWith(".zip")) {
            fileName = fileName + ".zip";
        }
        File zipFile = new File(targetDirectory, fileName);
        download(zipFile, listener);
    }

    @Override
    public void download(File target, DumpDownloadListener listener) throws AdminException {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(target));
            for (DumpResult dumpResult : dumpResults) {
                ((InternalDumpResult) dumpResult).download(zos, listener);
            }
            zos.close();
        } catch (IOException e) {
            throw new AdminException("Failed to download", e);
        }
    }
}
