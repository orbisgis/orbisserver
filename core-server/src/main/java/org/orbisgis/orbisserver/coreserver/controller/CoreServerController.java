package org.orbisgis.orbisserver.coreserver.controller;

import org.apache.felix.ipojo.annotations.Requires;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.SFSUtilities;
import org.orbisgis.orbisserver.coreserver.model.Session;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;

import javax.sql.DataSource;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller of the module. This class is the core managing the user auth.
 *
 * @author Sylvain PALOMINOS
 */

@Controller
public class CoreServerController extends DefaultController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreServerController.class);

    private static DataSource ds;

    public CoreServerController(){
        openSessionList = new ArrayList<>();
        String dataBaseLocation = new File("main_h2_db.mv.db").getAbsolutePath();
        try {
            ds = SFSUtilities.wrapSpatialDataSource(H2GISDBFactory.createDataSource(dataBaseLocation, true));
        } catch (SQLException e) {
            LOGGER.error("Unable to create the database : \n"+e.getMessage());
        }
        try {
            ds.getConnection().createStatement().execute("DROP TABLE IF EXISTS USER");
            ds.getConnection().createStatement().execute("CREATE TABLE USER (username VARCHAR(50), password VARCHAR(50))");
            ds.getConnection().createStatement().execute("INSERT INTO USER VALUES ('admin', 'admin')");
        } catch (SQLException e) {
            LOGGER.error("Unable to start the database\n"+e.getMessage());
        }
    }

    private static List<Session> openSessionList;

    public static Session getSession(String username, String password){
        if(!testUser(username, password)){
            return null;
        }
        for(Session s : openSessionList){
            if(s.getUsername().equals(username)){
                return s;
            }
        }
        Session session = createSession(username);
        openSessionList.add(session);
        return session;
    }

    private static boolean testUser(String username, String password){
        try {
            ResultSet rs = ds.getConnection().createStatement().executeQuery("SELECT COUNT(username) FROM USER WHERE " +
                    "username LIKE '" + username + "' AND password LIKE '" + password + "';");
            rs.first();
            return rs.getInt(1) == 1;
        } catch (SQLException e) {
            LOGGER.error("Unable to request the database\n"+e.getMessage());
        }
        return false;
    }

    private static Session createSession(String username){
        return new Session(username);
    }
}
