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
package org.orbisgis.orbisserver.wpsservice;

import org.orbisgis.orbisserver.coreserver.model.ExecuteRequest;
import org.orbisgis.orbisserver.coreserver.model.Service;
import org.orbisgis.orbisserver.coreserver.model.StatusInfo;
import org.orbisgis.orbisserver.coreserver.model.StatusRequest;
import org.orbiswps.scripts.WpsScriptPlugin;
import org.orbiswps.server.WpsServer;
import org.orbiswps.server.WpsServerImpl;

import javax.sql.DataSource;
import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * Service for the core-server module managing the wps part
 */
public class WpsService implements Service {

    private WpsServer wpsServer;
    private ExecutorService executorService;
    private File workspaceFolder;
    private DataSource ds;

    public WpsService(DataSource ds, ExecutorService executorService, File workspaceFolder){
        this.ds = ds;
        this.executorService = executorService;
        this.workspaceFolder = workspaceFolder;
        createWpsServerInstance();
    }

    @Override
    public StatusInfo executeOperation(ExecuteRequest request) {
        return null;
    }

    @Override
    public StatusInfo getStatus(StatusRequest request) {
        return null;
    }

    /**
     * Creates an  instance of the WpsServer.
     */
    private void createWpsServerInstance(){
        wpsServer = new WpsServerImpl(workspaceFolder.getAbsolutePath(), ds);
        wpsServer.setExecutorService(executorService);
        wpsServer.setDatabase(WpsServer.Database.H2GIS);
        wpsServer.setDataSource(ds);

        WpsScriptPlugin scriptPlugin = new WpsScriptPlugin();
        scriptPlugin.setWpsServer(wpsServer);
        scriptPlugin.activate();
    }
}
