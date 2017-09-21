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
package org.orbisgis.orbisserver.coreserver;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.SFSUtilities;
import org.orbisgis.orbisserver.api.CoreServer;
import org.orbisgis.orbisserver.api.service.Service;
import org.orbisgis.orbisserver.api.service.ServiceFactory;
import org.orbisgis.orbisserver.coreserver.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.concurrent.ManagedExecutorService;

import javax.sql.DataSource;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main class of the module. This class is the core managing the user auth.
 *
 * @author Sylvain PALOMINOS
 */

@Controller
@Provides(specifications={CoreServerImpl.class})
@Instantiate
public class CoreServerImpl extends DefaultController implements CoreServer {

    /** Logger of the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreServerImpl.class);

    /** Administration DataSource.*/
    private DataSource ds;

    /** Cache list of the opened sessions. */
    private List<Session> openSessionList;

    private List<ServiceFactory> serviceFactoryList;

    @Requires(filter = "(name=" + ManagedExecutorService.SYSTEM + ")", proxy = false)
    ExecutorService executor;

    /**
     * Main Constructor. It initiate the administration database.
     */
    public CoreServerImpl(){
        openSessionList = new ArrayList<>();
        serviceFactoryList = new ArrayList<>();
        String dataBaseLocation = new File("main_h2_db.mv.db").getAbsolutePath();
        try {
            ds = SFSUtilities.wrapSpatialDataSource(H2GISDBFactory.createDataSource(dataBaseLocation, true));
        } catch (SQLException e) {
            LOGGER.error("Unable to create the database : \n"+e.getMessage());
        }
        //Read the resource sql script and execute it
        try {
            Statement st = ds.getConnection().createStatement();
            InputStream inStream = this.getClass().getResourceAsStream("db_script.sql");
            BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
            String line = br.readLine();
            int queryCount = 1;
            while(line != null) {
                st.addBatch(line);
                line = br.readLine();
                queryCount++;
                if(queryCount == 100){
                    st.executeBatch();
                    queryCount = 0;
                }
            }
            if(queryCount != 0){
                st.executeBatch();
            }
        } catch (SQLException e) {
            LOGGER.error("Unable to start the database\n"+e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Unable to read the database initiation script\n"+e.getMessage());
        }
    }

    @Override
    public void registerServiceFactory(ServiceFactory serviceFactory){
        serviceFactoryList.add(serviceFactory);
    }

    @Override
    public void unregisterServiceFactory(ServiceFactory serviceFactory) {

    }

    /**
     * Get the session  corresponding to the given username and password.
     * @param username Username to use to log in.
     * @param password Password to use to log in.
     * @return The session of the user.
     */
    public Session getSession(String username, String password){
        if(!testUser(username, password)){
            return null;
        }
        for(Session s : openSessionList){
            if(s.getUsername().equals(username)){
                return s;
            }
        }
        Session session = buildSession(username);
        setSessionOptions(session);
        openSessionList.add(session);
        return session;
    }

    /**
     * Instantiate a session with the user name.
     * @param username User name.
     * @return An instantiated session.
     */
    private Session buildSession(String username){
        Map<String, Object> propertyMap = new HashMap<>();

        propertyMap.put(ServiceFactory.USERNAME_PROP, username);

        UUID token = UUID.randomUUID();
        propertyMap.put(ServiceFactory.TOKEN_PROP, token);

        Session session = new Session(propertyMap, new ArrayList<Service>());

        SessionInitializer init = new SessionInitializer(session, propertyMap, token);
        executor.submit(init);

        return session;
    }

    private class SessionInitializer implements Runnable {

        private Session session;
        private Map<String, Object> propertyMap;
        private UUID token;

        public SessionInitializer(Session session, Map<String, Object> propertyMap, UUID token){
            this.session = session;
            this.propertyMap = propertyMap;
            this.token = token;
        }

        @Override
        public void run() {
            File workspaceFolder = new File("workspace", token.toString());
            workspaceFolder.mkdirs();
            session.setWorkspace(workspaceFolder);
            propertyMap.put(ServiceFactory.WORKSPACE_FOLDER_PROP, workspaceFolder);

            ExecutorService executorService = Executors.newFixedThreadPool(3);
            session.setExecutorService(executorService);
            propertyMap.put(ServiceFactory.EXECUTOR_SERVICE_PROP, executorService);

            DataSource dataSource = null;
            String dataBaseLocation = new File(workspaceFolder, "h2_db.mv.db").getAbsolutePath();
            try {
                dataSource = SFSUtilities.wrapSpatialDataSource(H2GISDBFactory.createDataSource(dataBaseLocation, true));
            } catch (SQLException e) {
                LOGGER.error("Unable to create the database : \n"+e.getMessage());
            }
            LOGGER.info("Session database started.");
            session.setDataSource(dataSource);
            propertyMap.put(ServiceFactory.DATA_SOURCE_PROP, dataSource);


            List<Service> serviceList = new ArrayList<>();
            for(ServiceFactory factory : serviceFactoryList) {
                Service service = factory.createService(propertyMap);
                serviceList.add(service);
                LOGGER.info("Service "+service.getClass().getSimpleName()+" started.");
            }
            session.setServiceList(serviceList);
        }
    }

    /**
     * sets the session with its properties.
     * @param session Session to set.
     */
    private void setSessionOptions(Session session){
        try {
            ResultSet rs = ds.getConnection().createStatement().executeQuery("SELECT expirationTime FROM session_table WHERE " +
                    "username LIKE '" + session.getUsername() + "';");
            rs.first();
            session.setExpirationTime(rs.getLong(1));
            rs.close();
        } catch (SQLException e) {
            LOGGER.error("Unable to request the database\n"+e.getMessage());
        }
    }

    /**
     * Test if the user name and password are correct.
     * @param username Name of the user.
     * @param password Password of the user.
     * @return True if the user is correct, false otherwise.
     */
    private boolean testUser(String username, String password){
        try {
            ResultSet rs = ds.getConnection().createStatement().executeQuery("SELECT COUNT(username) FROM session_table WHERE " +
                    "username LIKE '" + username + "' AND password LIKE '" + password + "';");
            rs.first();
            boolean isUser = rs.getInt(1) != 0;
            rs.close();
            return isUser;
        } catch (SQLException e) {
            LOGGER.error("Unable to request the database\n"+e.getLocalizedMessage());
            for(StackTraceElement el : e.getStackTrace()){
                LOGGER.error(el.toString());
            }
        }
        return false;
    }

    /**
     * Test if the user name is correct.
     * @param username Name of the user.
     * @return True if the user is correct, false otherwise.
     */
    private boolean testUser(String username){
        try {
            ResultSet rs = ds.getConnection().createStatement().executeQuery("SELECT COUNT(username) FROM session_table WHERE " +
                    "username LIKE '" + username + "';");
            rs.first();
            boolean isUser = rs.getInt(1) != 0;
            rs.close();
            return isUser;
        } catch (SQLException e) {
            LOGGER.error("Unable to request the database\n"+e.getLocalizedMessage());
            for(StackTraceElement el : e.getStackTrace()){
                LOGGER.error(el.toString());
            }
        }
        return false;
    }

    /**
     * Create a session for the given user.
     * @param username Name of the user.
     * @return The user session.
     */
    public Session createSession(String username, String password){
        if(!testUser(username, password)) {
            try {
                ds.getConnection().createStatement().execute("INSERT INTO session_table VALUES ('" + username + "','" + password + "');");
            } catch (SQLException e) {
                LOGGER.error("Unable to add a user\n" + e.getMessage());
            }
        }
        return getSession(username, password);
    }

    /**
     * Updates the password of a user
     * @param username User name.
     * @param newPassword New password.
     */
    public void changePassword(String username, String newPassword) {
        if(testUser(username)) {
            try {
                ds.getConnection().createStatement().execute("UPDATE user_table SET password = " + newPassword +
                        " WHERE username='" + username + "';");
            } catch (SQLException e) {
                LOGGER.error("Unable to change password\n" + e.getMessage());
            }
        }
    }
}
