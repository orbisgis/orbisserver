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
package org.orbisgis.orbisserver.coreserver.web;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

/**
 * Main orbisserver controller
 *
 * @author Sylvain PALOMINOS
 */
@Controller
public class MainController extends DefaultController {

    @View("Home")
    Template home;

    @View("BaseLog_In")
    Template logIn;

    @View("BaseLog_Out")
    Template logOut;

    @View("SignIn")
    Template signIn;

    @View("Workspace")
    Template workspace;

    @View("Data")
    Template data;

    @View("Import")
    Template tImport;

    @View("Export")
    Template export;

    @Route(method = HttpMethod.GET, uri = "/")
    public Result home() {
        return ok(render(home));
    }

    @Route(method = HttpMethod.GET, uri = "/login")
    public Result logIn() {
        return ok(render(logIn));
    }

    @Route(method = HttpMethod.GET, uri = "/logout")
    public Result logOut() {
        return ok(render(logOut));
    }

    @Route(method = HttpMethod.GET, uri = "/signIn")
    public Result signIn() {return ok(render(signIn));}

    @Route(method = HttpMethod.GET, uri = "/workspace")
    public Result workspace() {
        return ok(render(workspace));
    }

    @Route(method = HttpMethod.GET, uri = "/data")
    public Result data() {return ok(render(data));}

    @Route(method = HttpMethod.GET, uri = "/data/import")
    public Result tImport() {return ok(render(tImport));}

    @Route(method = HttpMethod.GET, uri = "/data/export")
    public Result export() {return ok(render(export));}
}
