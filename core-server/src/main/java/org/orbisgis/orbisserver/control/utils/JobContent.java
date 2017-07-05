package org.orbisgis.orbisserver.control.utils;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Class containing information about a job
 *
 * @author Sylvain PALOMINOS
 */
public class JobContent {

    private String jobId;
    private String processID;
    private String processTitle;
    private String status;
    private int percent = 0;
    private XMLGregorianCalendar estimatedCompletion;
    private XMLGregorianCalendar nextPoll;
    private long nextRefreshMillis = 0;

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

    public void setStatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

    public void setPercentCompleted(int percent){
        this.percent = percent;
    }

    public int getPercentCompleted(){
        return percent;
    }

    public void setEstimatedCompletion(XMLGregorianCalendar date){
        this.estimatedCompletion = date;
    }

    public XMLGregorianCalendar getEstimatedCompletion(){
        return estimatedCompletion;
    }

    public void setNextPoll(XMLGregorianCalendar nextPoll) {
        this.nextPoll = nextPoll;
    }

    public XMLGregorianCalendar getNextPoll() {
        return nextPoll;
    }

    public void setNextRefreshMillis(long nextRefreshMillis) {
        this.nextRefreshMillis = nextRefreshMillis;
    }

    public long getNextRefreshMillis(){
        return nextRefreshMillis;
    }
}
