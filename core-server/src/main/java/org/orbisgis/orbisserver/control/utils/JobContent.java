package org.orbisgis.orbisserver.control.utils;

/**
 * Class containing information about a job
 *
 * @author Sylvain PALOMINOS
 */
public class JobContent {

    private String jobId;
    private String processID;
    private String processTitle;

    public JobContent(String jobId){
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getProcessTitle() {
        return processTitle;
    }

    public void setProcessTitle(String processTitle) {
        this.processTitle = processTitle;
    }
}
