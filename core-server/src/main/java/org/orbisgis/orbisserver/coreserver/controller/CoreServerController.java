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
package org.orbisgis.orbisserver.coreserver.controller;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.SFSUtilities;
import org.orbisgis.orbisserver.coreserver.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;

import javax.sql.DataSource;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller of the module. This class is the core managing the user auth.
 *
 * @author Sylvain PALOMINOS
 */

@Controller
public class CoreServerController extends DefaultController {

    /** Logger of the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreServerController.class);

    /** Administration DataSource.*/
    private static DataSource ds;

    /** Cache list of the opened sessions. */
    private static List<Session> openSessionList;

    /**
     * Main Constructor. It initiate the administration database.
     */
    public CoreServerController(){
        openSessionList = new ArrayList<>();
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

    /**
     * Get the session  corresponding to the given username and password.
     * @param username Username to use to log in.
     * @param password Password to use to log in.
     * @return The session of the user.
     */
    public static Session getSession(String username, String password){
        if(!testUser(username, password)){
            return null;
        }
        for(Session s : openSessionList){
            if(s.getUsername().equals(username)){
                return s;
            }
        }
        Session session = buildSession(username);
        openSessionList.add(session);
        return session;
    }

    private static Session buildSession(String username){
        return new Session(username);
    }

    /**
     * Test if the user name and password are corrects.
     * @param username Name of the user.
     * @param password Password of the user.
     * @return True if the user is correct, false otherwise.
     */
    private static boolean testUser(String username, String password){
        try {
            ResultSet rs = ds.getConnection().createStatement().executeQuery("SELECT COUNT(username) FROM users_table WHERE " +
                    "username LIKE '" + username + "' AND password LIKE '" + password + "';");
            rs.first();
            return rs.getInt(1) != 0;
        } catch (SQLException e) {
            LOGGER.error("Unable to request the database\n"+e.getMessage());
        }
        return false;
    }

    /**
     * Create a session for the given user.
     * @param username Name of the user.
     * @return The user session.
     */
    public static Session createSession(String username, String password){
        if(!testUser(username, password)) {
            try {
                ds.getConnection().createStatement().execute("INSERT INTO users_table VALUES ('" + username + "','" + password + "');");
            } catch (SQLException e) {
                LOGGER.error("Unable to add a user\n" + e.getMessage());
            }
        }
        return getSession(username, password);
    }
}
