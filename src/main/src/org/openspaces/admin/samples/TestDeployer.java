package org.openspaces.admin.samples;

import com.gigaspaces.log.*;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import static com.gigaspaces.log.LogEntryMatchers.*;

/**
 * @author kimchy
 */
public class TestDeployer {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();

        admin.getGridServiceContainers().waitFor(1);
        final GridServiceContainer container = admin.getGridServiceContainers().getContainers()[0];

        System.out.println("Getting logs...");

        GridServiceAgent agent = admin.getGridServiceAgents().waitForAtLeastOne();
        LogEntryMatcher matcher = forwardChunk(size(2));
        while (true) {
            CompoundLogEntries entries = agent.logEntries(LogProcessType.GSC, matcher);
            if (entries.isEmpty()) {
                break;
            }
            for (LogEntries logEntries : entries.getSafeEntries()) {
                for (LogEntry log : logEntries.logEntries()) {
                    System.out.println(logEntries.getProcessType() + "/" + logEntries.getPid() + ": " + log.getText());
                }
            }
        }

//        LogEntryMatcher matcher = reverse(lastN(2));
//        while (true) {
//            final LogEntries logs = container.logEntries(matcher);
//            if (logs.logEntries().isEmpty()) {
//                break;
//            }
//            for (LogEntry log : logs.logEntries()) {
//                System.out.print(log.getTextWithLF());
//            }
//        }

//        LogEntryMatcher matcher = forwardChunk(size(2));
//        while (true) {
//            final LogEntries logs = container.logEntries(matcher);
//            if (logs == null) {
//                break;
//            }
//            for (LogEntry log : logs.logEntries()) {
//                System.out.print(log.getTextWithLF());
//            }
//        }

//        LogEntryMatcher matcher = afterTime("2009-10-06 14:41:00", beforeTime("2009-10-06 14:41:20"));
//        LogEntries logEntries = container.logEntries(matcher);
//        for (LogEntry logEntry : logEntries.logEntries()) {
//            System.out.println(logEntry.getText());
//        }

//        new Thread(new Runnable() {
//
//            private LogEntryMatcher matcher = continuous(lastN(100));
//
//            public void run() {
//                while (true) {
//                    final LogEntries logs = container.logEntries(matcher);
//                    for (LogEntry log : logs.logEntries()) {
//                        System.out.println(log.getText());
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        // ignore
//                    }
//                }
//            }
//        }).start();

        System.out.println("done...");

        Thread.sleep(10000000);


        System.out.println("Closing Admin");
        admin.close();
    }
}
