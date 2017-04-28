/**
 * OrbisServer is part of the platform OrbisGIS
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
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisServer is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisServer is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * OrbisServer. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.orbisserver;

import org.apache.felix.ipojo.annotations.Requires;
import org.orbiswps.scripts.WpsScriptPlugin;
import org.orbiswps.server.WpsServer;
import org.orbiswps.server.WpsServerImpl;

import javax.sql.DataSource;

/**
 * Class managing the WpsServer instances.
 *
 * @author Sylvain PALOMINOS
 */
public class WpsServerManager {

    /**
     * Data source used by the WpsServer.
     */
    @Requires
    private static DataSource ds;

    /**
     * Instance of the WpsServer.
     */
    private static WpsServer wpsServer;

    /**
     * Returns the instance of the WpsServer. If it was not already created, create it.
     * @return The instance of the WpsServer
     */
    public static WpsServer getWpsServer(){
        if(wpsServer == null){
            createWpsServerInstance();
        }
        return wpsServer;
    }

    /**
     * Creates an instance of the WpsServer.
     */
    private static void createWpsServerInstance(){
        wpsServer = new WpsServerImpl(System.getProperty("java.io.tmpdir"), ds);
        WpsScriptPlugin scriptPlugin = new WpsScriptPlugin();
        scriptPlugin.setWpsServer(wpsServer);
        scriptPlugin.activate();
    }
}
