/*
 * OrbisServer is an OSGI web application to expose OGC services.
 *
 * OrbisServer is part of the OrbisGIS platform
 *
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 *
 * OrbisServer is distributed under LGPL 3 license.
 *
 * Copyright (C) 2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * OrbisServer is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisServer is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * OrbisServer. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.orbisserver.coreserver.model;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Information about the status of the operation execution.
 *
 * @author Sylvain PALOMINOS
 */
public class StatusInfo {

    private String jobId;
    private String processID;
    private String processTitle;
    private String status;
    private Integer percent = 0;
    private XMLGregorianCalendar estimatedCompletion;
    private XMLGregorianCalendar nextPoll;
    private long nextRefreshMillis = 0;

    public StatusInfo(String jobId){
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

    public Integer getPercentCompleted(){
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
