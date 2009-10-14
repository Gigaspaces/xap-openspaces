package org.openspaces.admin.dump;

import org.openspaces.admin.internal.dump.InternalDumpResult;
import org.openspaces.admin.AdminException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

/**
 * @author kimchy
 */
public class CompoundDumpResult implements DumpResult {

    private ArrayList<DumpResult> dumpResults = new ArrayList<DumpResult>();

    public CompoundDumpResult add(DumpResult dumpResult) {
        dumpResults.add(dumpResult);
        return this;
    }

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

    public void download(File targetDirectory, String fileName) throws AdminException {
        if (!fileName.endsWith(".zip")) {
            fileName = fileName + ".zip";
        }
        try {
            File zipFile = new File(targetDirectory, fileName);
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for (DumpResult dumpResult : dumpResults) {
                ((InternalDumpResult) dumpResult).download(zos);
            }
            zos.close();
        } catch (IOException e) {
            throw new AdminException("Failed to download", e);
        }
    }
}
