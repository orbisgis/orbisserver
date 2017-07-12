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

import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.SFSUtilities;
import org.orbisgis.orbisserver.coreserver.controller.WpsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Session of the server usage
 *
 * @author Sylvain PALOMINOS
 */
public class Session {

    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

    private UUID token;

    private DataSource ds;

    private ExecutorService executorService;

    private File workspaceFolder;

    private String username;

    private List<Service> serviceList;

    private List<StatusInfo> statusInfoList;

    private Map<String, Service> jobIdServiceMap;

    public Session(String username){
        jobIdServiceMap = new HashMap<>();
        serviceList = new ArrayList<>();
        statusInfoList = new ArrayList<>();
        token = UUID.randomUUID();
        workspaceFolder = new File(System.getProperty("java.io.tmpdir"), token.toString());
        executorService = Executors.newFixedThreadPool(3);
        this.username = username;

        String dataBaseLocation = new File(workspaceFolder, "h2_db.mv.db").getAbsolutePath();
        try {
            ds = SFSUtilities.wrapSpatialDataSource(H2GISDBFactory.createDataSource(dataBaseLocation, true));
        } catch (SQLException e) {
            LOGGER.error("Unable to create the database : \n"+e.getMessage());
        }
        serviceList.add(new WpsService(ds, executorService, workspaceFolder));
    }

    public DataSource getDataSource(){
        return ds;
    }

    public ExecutorService getExecutorService(){
        return executorService;
    }

    public File getWorkspaceFolder(){
        return workspaceFolder;
    }

    public String getUsername(){
        return username;
    }

    public List<Operation> getOperationList(){
        List<Operation> operationList = new ArrayList<>();
        for(Service service : serviceList) {
            operationList.addAll(service.getAllOperation());
        }
        return operationList;
    }

    public Operation getOperation(String id) {
        Service serv = null;
        for(Service service : serviceList){
            if(service.hasOperation(id)){
                serv = service;
            }
        }
        if(serv == null) {
            return null;
        }
        return serv.getOperation(id);
    }

    public void executeOperation(String id, Map<String, String> inputData) {
        ExecuteRequest executeRequest = new ExecuteRequest(id, inputData);
        Service serv = null;
        for(Service service : serviceList){
            if(service.hasOperation(id)){
                serv = service;
            }
        }
        if(serv != null) {
            StatusInfo statusInfo = serv.executeOperation(executeRequest);
            statusInfo.setProcessID(id);
            statusInfo.setProcessTitle(getTitle(id));
            statusInfoList.add(statusInfo);
            jobIdServiceMap.put(statusInfo.getJobId(), serv);
        }
    }

    private String getTitle(String id){
        for(Service service : serviceList){
            if(service.hasOperation(id)){
                return service.getOperation(id).getTitle();
            }
        }
        return "";
    }

    public List<StatusInfo> getAllStatusInfo(){
        return statusInfoList;
    }

    public List<StatusInfo> getAllStatusInfoToRefresh() {
        List<StatusInfo> allStatusInfoToRefresh = new ArrayList<>();
        List<StatusInfo> list = getAllStatusInfo();
        long timeMillisNow = System.currentTimeMillis();
        for(StatusInfo statusInfo : list){
            long comparison = -1;
            if(statusInfo.getNextPoll() != null) {
                long timeMillisPoll = statusInfo.getNextPoll().toGregorianCalendar().getTime().getTime();
                comparison = timeMillisPoll - timeMillisNow;
            }
            if(comparison < 0) {
                allStatusInfoToRefresh.add(statusInfo);
            }
        }
        return allStatusInfoToRefresh;
    }

    public StatusInfo refreshStatus(String jobId) {
        Service service = jobIdServiceMap.get(jobId);
        StatusRequest statusRequest = new StatusRequest(jobId);

        StatusInfo info = service.getStatus(statusRequest);
        return info;
    }

    /** Unique token associated to the session.*/
    public UUID getToken() {
        return token;
    }
}
