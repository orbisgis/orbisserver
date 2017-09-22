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

import org.apache.commons.io.filefilter.NameFileFilter;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.orbisgis.orbisserver.api.model.*;
import org.orbisgis.orbisserver.api.service.Service;
import org.orbisgis.orbisserver.api.service.ServiceFactory;
import org.orbisgis.orbisserver.coreserver.CoreServerImpl;
import org.orbisgis.orbisserver.coreserver.web.MainController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Session of the server. A session contains a list of Services, a DataSource and a workspace.
 *
 * @author Sylvain PALOMINOS
 */
public class Session {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

    /** Token associated to the session. It is used for the identification of the web client requests. */
    private UUID token;
    /** DataSource associated to the session. This data source is used for the differents services associated. */
    private DataSource ds;
    /** Executor services dedicated to the session. */
    private ExecutorService executorService;
    /** Workspace folder. */
    private File workspaceFolder;
    /** Username associated to the session. */
    private String username;
    /** List of services instance for the Session. */
    private List<Service> serviceList;
    /** List of StatusInfo. This list is used as a cache saving all the process executed and waiting for the data
     * retrieving.*/
    private List<StatusInfo> statusInfoList;
    /** Map linking the job id with the service executing it. */
    private Map<String, Service> jobIdServiceMap;
    /** Map with the finished job. */
    private Map<String, StatusInfo> finishedJobMap;
    /** Time before expiration of the session. If equals to -1, there is no expiration. */
    private long expirationTimeMillis;
    /** Timer for the jobs expiration. */
    private Timer timer;
    private CoreServerImpl coreServerImpl;
    private boolean isActive;

    /**
     * Main constructor.
     */
    public Session(Map<String, Object> propertyMap, List<Service> serviceList, CoreServerImpl coreServerImpl){
        isActive = false;
        jobIdServiceMap = new HashMap<>();
        finishedJobMap = new HashMap<>();
        statusInfoList = new ArrayList<>();
        this.token = (UUID)propertyMap.get(ServiceFactory.TOKEN_PROP);
        this.username = (String)propertyMap.get(ServiceFactory.USERNAME_PROP);
        this.ds = (DataSource) propertyMap.get(ServiceFactory.DATA_SOURCE_PROP);
        this.executorService = (ExecutorService) propertyMap.get(ServiceFactory.EXECUTOR_SERVICE_PROP);
        this.workspaceFolder = (File)propertyMap.get(ServiceFactory.WORKSPACE_FOLDER_PROP);
        expirationTimeMillis = -1;
        timer = new Timer();
        this.serviceList = serviceList;
        this.coreServerImpl = coreServerImpl;
    }

    public void setExpirationTime(long timeInMillis){
        this.expirationTimeMillis = timeInMillis;
    }

    /**
     * Returns the DataSource of the Session.
     * @return The session DataSource.
     */
    public DataSource getDataSource(){
        return ds;
    }

    /**
     * Returns the ExecutorService of the session.
     * @return The session ExecutorService.
     */
    public ExecutorService getExecutorService(){
        return executorService;
    }

    /**
     * Returns the workspace folder of the sessions.
     * @return The session workspace folder.
     */
    public File getWorkspaceFolder(){
        return workspaceFolder;
    }

    /**
     * Returns the username of the session.
     * @return The session username.
     */
    public String getUsername(){
        return username;
    }

    /**
     * Returns the list of operations available in ths session.
     * @return The available operation list.
     */
    public List<Operation> getOperationList(){
        List<Operation> operationList = new ArrayList<>();
        for(Service service : serviceList) {
            operationList.addAll(service.getAllOperation());
        }
        return operationList;
    }

    /**
     * Returns the Operation with the given identifier.
     * @param id Identifier of the operation.
     * @return The operation with the given identifier.
     */
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

    /**
     * Execute the operation corresponding to the given identifier, using the given input data Map.
     * @param id Identifier of the operation to execute.
     * @param inputData Input data Map to use on the execution.
     */
    public void executeOperation(String id, Map<String, String> inputData) {
        isActive = true;
        Operation operation = getOperation(id);
        Map<String, String> tmpMap = new HashMap<>();
        for(Input input : operation.getInputList()){
            if(input.getName().equalsIgnoreCase("RawData")){
                for(Map.Entry<String, String> entry : inputData.entrySet()){
                    if(input.getId().equalsIgnoreCase(entry.getKey())){
                        tmpMap.put(entry.getKey(), new File(workspaceFolder, entry.getValue()).getAbsolutePath());
                    }
                }
            }
        }
        inputData.putAll(tmpMap);
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

    /**
     * Returns the title of the operation with the given identifier. If no operation is found, returns an empty string.
     * @param id Identifier of the operation to find.
     * @return The title of the operation.
     */
    private String getTitle(String id){
        for(Service service : serviceList){
            if(service.hasOperation(id)){
                return service.getOperation(id).getTitle();
            }
        }
        return "";
    }

    /**
     * Returns the list of the cached list of StatusInfo responses.
     * @return The cached StatusInfo list.
     */
    public List<StatusInfo> getAllStatusInfo(){
        List<StatusInfo> list = new ArrayList<>();
        list.addAll(statusInfoList);
        for(Map.Entry<String, StatusInfo> entry : finishedJobMap.entrySet()){
            list.add(entry.getValue());
        }
        return list;
    }

    /**
     * Return the list of all the StatusInfo with the next poll date is reached and which needs to be refreshed.
     * @return The list of Status info to refresh.
     */
    public List<StatusInfo> getAllStatusInfoToRefresh() {
        List<StatusInfo> allStatusInfoToRefresh = new ArrayList<>();
        long timeMillisNow = System.currentTimeMillis();
        for(StatusInfo statusInfo : statusInfoList){
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

    /**
     * Refresh the status of the job with the given identifier.
     */
    public long refreshStatus(StatusInfo statusInfo) {
        long timeMillisNow = System.currentTimeMillis();
        long minRefresh = Long.MAX_VALUE;
        String jobId = statusInfo.getJobId();
        Service service = jobIdServiceMap.get(jobId);
        StatusRequest statusRequest = new StatusRequest(jobId);

        StatusInfo statusInfoToRemove = null;
        for(StatusInfo info : statusInfoList){
            if(info.getJobId().equalsIgnoreCase(jobId)){
                statusInfoToRemove = info;
            }
        }
        statusInfoList.remove(statusInfoToRemove);

        StatusInfo info = service.getStatus(statusRequest);
        if(info != null){
            statusRequest.setProcessId(statusInfo.getProcessID());
            info.setProcessID(statusInfo.getProcessID());
            info.setProcessTitle(statusInfo.getProcessTitle());
            info.setNextRefreshMillis(-1);
            if (info.getNextPoll() != null) {
                long timeMillisPoll = info.getNextPoll().toGregorianCalendar().getTime().getTime();
                info.setNextRefreshMillis(timeMillisPoll - timeMillisNow);
            }
            if(info.getStatus().equalsIgnoreCase("SUCCEEDED") || info.getStatus().equalsIgnoreCase("FAILED")){
                info.setResult(service.getResult(statusRequest));
                timer.schedule(new TimerExpirationTask(jobId, this),
                        info.getResult().getExpirationDate().toGregorianCalendar().getTime());
                jobIdServiceMap.remove(jobId);
                finishedJobMap.put(jobId, info);
            }
            else{
                statusInfoList.add(info);
            }
        }
        return minRefresh;
    }

    /**
     * Returns the token of the session.
     * @return The token of the session.
     */
    public UUID getToken() {
        return token;
    }

    /**
     * Returns the DatabaseContent object which contains the representation of the Database.
     * @return The DatabaseContent object.
     */
    public DatabaseContent getDatabaseContent(){
        DatabaseContent dbContent = new DatabaseContent();
        try(Connection connection = ds.getConnection()) {
            for(String tableName : JDBCUtilities.getTableNames(connection.getMetaData(), null, null, null, new String[]{"TABLE","LINKED TABLE","VIEW","EXTERNAL","UIodfsghjmodfhjgodujhfg"})){
                DatabaseTable dbTable = new DatabaseTable(TableLocation.parse(tableName));
                //Get the list of the columns of a table
                ResultSet rs1 = connection.createStatement().executeQuery(String.format("select * from %s limit 1",
                        dbTable.getName()));
                ResultSetMetaData metaData = rs1.getMetaData();
                //If the column isn't a geometry, add it to the map
                for(int i=1; i<=metaData.getColumnCount(); i++){
                    if(!metaData.getColumnTypeName(i).equalsIgnoreCase("GEOMETRY")){
                        dbTable.addField(metaData.getColumnLabel(i), metaData.getColumnTypeName(i));
                    }
                }
                //Once the non geometric columns are get, do the same with the geometric one.
                Statement statement = connection.createStatement();
                String query = "SELECT * FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME LIKE '" +
                        TableLocation.parse(dbTable.getName()).getTable() + "';";
                ResultSet rs = statement.executeQuery(query);
                while (rs.next()) {
                    dbTable.addField(rs.getString(4), SFSUtilities.getGeometryTypeNameFromCode(rs.getInt(6)));
                }
                dbContent.addTable(dbTable);
            }
        } catch (SQLException e) {
            LOGGER.error("Unable to get the database information.\nCause : "+e.getMessage());
        }
        return dbContent;
    }

    public File getResultAchive(String jobId){
        File jobFolder = new File(workspaceFolder, jobId);
        for(StatusInfo statusInfo : getAllStatusInfo()){
            if(statusInfo.getJobId().equalsIgnoreCase(jobId)){
                for(Output out : statusInfo.getResult().getOutputList()){
                    if(out.getData() != null){
                        try {
                            for(Object content : out.getData().getContent()) {
                                File outFile;
                                if(jobFolder.list(new NameFileFilter(out.getTitle()))!= null){
                                    int diff=1;
                                    while(jobFolder.list(new NameFileFilter(out.getTitle()+diff)) != null){
                                        diff++;
                                    }
                                    outFile = new File(jobFolder, out.getTitle().replaceAll(File.separator, "")+diff);
                                }
                                else {
                                    outFile = new File(jobFolder, out.getTitle().replaceAll(File.separator, ""));
                                }
                                if (jobFolder.mkdirs() || outFile.createNewFile()) {
                                    try (FileWriter fileWriter = new FileWriter(outFile)) {
                                        try (PrintWriter out1 = new PrintWriter(fileWriter)) {
                                            out1.append(content.toString());
                                        }
                                    }
                                } else {
                                    LOGGER.error("Unable to create the output as a file.");
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.error("Unable to write the output as a file.\n"+e.getMessage());
                        }
                    }
                    else if(out.getReference() != null){
                        try {
                            URL url = new URL(out.getReference());
                            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                            FileOutputStream fos = new FileOutputStream(out.getTitle());
                            fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                        } catch (IOException e) {
                            LOGGER.error("Unable to download the result.\n"+e.getMessage());
                        }
                    }
                }
            }
        }
        try {
            File zipFile = new File(workspaceFolder, "Result.zip");
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(File f : jobFolder.listFiles()){
                FileInputStream fis = new FileInputStream(f);
                ZipEntry zipEntry = new ZipEntry(f.getName());
                zos.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }

                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
            return zipFile;
        } catch (IOException e) {
            LOGGER.error("Unable to zip the result folder.\n"+e.getMessage());
        }
        return null;
    }

    private void checkExpiration() {
        if(statusInfoList.isEmpty() && finishedJobMap.isEmpty()){
            timer.schedule(new TimerKillSessionTask(this), expirationTimeMillis);
        }
    }

    public void shutdown(){
        try {
            ds.getConnection().close();
        } catch (SQLException ignored) {}
        executorService.shutdownNow();
        for(Service service : serviceList){
            service.shutdown();
        }
        timer.purge();
        isActive = false;
    }

    public void setWorkspace(File workspace) {
        this.workspaceFolder = workspace;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setDataSource(DataSource dataSource) {
        this.ds = dataSource;
    }

    /**
     * Shutdown the service with the given class.
     * @param serviceClass The class of the service to shutdown.
     */
    public void shutdownService(Class serviceClass){
        Service toRemove = null;
        for(Service service : serviceList){
            if(serviceClass.isInstance(service)){
                service.shutdown();
                toRemove = service;
            }
        }
        serviceList.remove(toRemove);
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    private class TimerExpirationTask extends TimerTask{

        private String jobId;
        private Session session;

        public TimerExpirationTask(String jobId, Session session){
            this.jobId = jobId;
            this.session = session;
        }

        @Override
        public void run() {
            finishedJobMap.remove(jobId);
            session.checkExpiration();
        }
    }

    private class TimerKillSessionTask extends TimerTask{

        private Session session;

        public TimerKillSessionTask(Session session){
            this.session = session;
        }

        @Override
        public void run() {
            session.setIsActive(false);
            coreServerImpl.inactiveSession(session);
        }
    }

    public void setServiceList(List<Service> serviceList){
        this.serviceList = serviceList;
    }
}
