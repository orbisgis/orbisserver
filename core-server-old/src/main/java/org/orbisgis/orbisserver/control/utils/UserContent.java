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
package org.orbisgis.orbisserver.control.utils;

import net.opengis.wps._2_0.ProcessSummaryType;
import net.opengis.wps._2_0.WPSCapabilitiesType;
import org.orbiswps.server.utils.WpsServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Information linked to the user.
 *
 * @author Sylvain PALOMINOS
 */
public class UserContent {

    private Logger LOGGER =LoggerFactory.getLogger(UserContent.class);

    private List<ProcessContent> processContentList;

    public UserContent(){
        processContentList = new ArrayList<>();
    }

    public void setCapabilities(WPSCapabilitiesType wpsCapabilitiesType){
        processContentList = new ArrayList<>();
        for(ProcessSummaryType summary : wpsCapabilitiesType.getContents().getProcessSummary()){
            String id = summary.getIdentifier().getValue();
            ProcessContent content = new ProcessContent(id.replaceAll("([-/:,;.])", ""), id);
            content.setTitle(summary.getTitle().get(0).getValue());
            content.setVersion(summary.getProcessVersion());
            processContentList.add(content);
        }
    }

    public List<ProcessContent> getProcessContentList(){
        return processContentList;
    }

    public void addJob(String processId, JobContent jobContent) {
        jobContent.setProcessID(processId);
        for(ProcessContent process : processContentList){
            if (process.getWpsId().equals(processId)) {
                process.addJobContent(jobContent);
            }
        }
    }

    public List<JobContent> getAllJobContent(){
        List<JobContent> jobContentList = new ArrayList<>();
        for(ProcessContent process : processContentList){
            jobContentList.addAll(process.getJobContentList());
        }
        return jobContentList;
    }

    public List<JobContent> getAllJobToRefresh() {
        List<JobContent> allJobToRefresh = new ArrayList<>();
        List<JobContent> list = getAllJobContent();
        long timeMillisNow = System.currentTimeMillis();
        for(JobContent jobContent : list){
            long comparison = -1;
            if(jobContent.getNextPoll() != null) {
                long timeMillisPoll = jobContent.getNextPoll().toGregorianCalendar().getTime().getTime();
                comparison = timeMillisPoll - timeMillisNow;
            }
            if(comparison < 0) {
                allJobToRefresh.add(jobContent);
            }
        }
        return allJobToRefresh;
    }
}
