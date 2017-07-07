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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Session of the server usage
 *
 * @author Sylvain PALOMINOS
 */
public class Session {

    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

    /** Unique token associated to the session.*/
    private UUID token;

    private DataSource ds;

    private ExecutorService executorService;

    private File workspaceFolder;

    public Session(){
        token = UUID.randomUUID();
        workspaceFolder = new File(System.getProperty("java.io.tmpdir"), token.toString());
        executorService = Executors.newFixedThreadPool(3);

        String dataBaseLocation = new File(workspaceFolder, "h2_db.mv.db").getAbsolutePath();
        try {
            ds = SFSUtilities.wrapSpatialDataSource(H2GISDBFactory.createDataSource(dataBaseLocation, true));
        } catch (SQLException e) {
            LOGGER.error("Unable to create the database : \n"+e.getMessage());
        }
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
}
