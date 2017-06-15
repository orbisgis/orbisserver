/**
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
package org.orbisgis.orbisserver.control.web;

import org.orbisgis.orbisserver.manager.Wps_2_0_0_Operations;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Instance of DefaultController used to control the welcome's page
 *
 * @author Guillaume MANDE
 * @author Sylvain PALOMINOS
 */
@Controller
public class ExecuteController extends DefaultController {
    /**
     * Injects a template named 'welcome'.
     */
    @View("execute")
    Template statusInfo;

    /**
     * The action method returning the html welcome page containing a formulary to do an execute request.
     * @return The execute result.
     */
    @Route(method = HttpMethod.POST, uri = "/internal/execute")
    public Result execute() throws IOException, JAXBException {
        String urlContent = URLDecoder.decode(context().reader().readLine(), "UTF-8");
        String[] split = urlContent.split("&");
        Map<String, String> inputData = new HashMap<>();
        String id = "";
        for(String str : split){
            String[] val = str.split("=");
            if(val[0].equals("processId")){
                id = val[1];
            }
            else {
                if(val.length==1) {
                    inputData.put(val[0], "");
                }
                else {
                    inputData.put(val[0], val[1]);
                }
            }
        }
        Map<String, String> outputData = new HashMap<>();
        Wps_2_0_0_Operations.getResponseFromExecute(id, "document", "auto", inputData, outputData);
        return ok(render(statusInfo));
    }

}
