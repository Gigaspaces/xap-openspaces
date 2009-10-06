package org.openspaces.admin.samples;

import com.gigaspaces.log.*;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;

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
        LogEntryMatcher matcher = new ForwardChunkLogEntryMatcher(new NLogEntryMatcher(2));
        while (true) {
            LogEntries[] entries = agent.log(LogProcessType.GSC, matcher);
            boolean notAllNull = false;
            for (LogEntries logEntries : entries) {
                if (logEntries == null) {
                    continue;
                }
                notAllNull = true;
                for (LogEntry log : logEntries.logEntries()) {
                    System.out.println(logEntries.getProcessType() + "/" + logEntries.getPid() + ": " + log.getText());
                }
            }
            if (!notAllNull) {
                break;
            }
        }

//        LogEntryMatcher matcher = new ReverseLogEntryMatcher(new LastNLogEntryMatcher(2));
//        while (true) {
//            final LogEntries logs = container.log(matcher);
//            if (logs.logEntries().isEmpty()) {
//                break;
//            }
//            for (LogEntry log : logs.logEntries()) {
//                System.out.print(log.getTextWithLF());
//            }
//        }

//        LogEntryMatcher matcher = new ForwardChunkLogEntryMatcher(new NLogEntryMatcher(2));
//        while (true) {
//            final LogEntries logs = container.log(matcher);
//            if (logs == null) {
//                break;
//            }
//            for (LogEntry log : logs.logEntries()) {
//                System.out.print(log.getTextWithLF());
//            }
//        }

//        new Thread(new Runnable() {
//
//            private LogEntryMatcher matcher = new ContinuousLogEntryMatcher(new LastNLogEntryMatcher(100));
//
//            public void run() {
//                while (true) {
//                    final LogEntries logs = container.log(matcher);
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
