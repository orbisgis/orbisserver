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

import org.orbisgis.orbisserver.control.utils.ProcessContent;
import org.orbisgis.orbisserver.manager.Wps_2_0_0_Operations;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.*;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

/**
 * Instance of DefaultController used to control the GetCapabilities page with a http request.
 *
 * @author Sylvain PALOMINOS
 * @author Guillaume MANDE
 */
@Controller
public class GetCapabilitiesController extends DefaultController {
    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetCapabilitiesController.class);
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(GetCapabilitiesController.class);

    /**
     * Injects a template named 'getCapabilities'.
     */
    @View("getCapabilities")
    Template getCapabilities;

    /**
     * The action method returning the html page containing a list of all the OrbisWPS processes
     * readable by a human. It handles HTTP GET request on the "/internal/getcapabilities" URL.
     *
     * @return The index page including the processes list.
     */
    @Route(method = HttpMethod.GET, uri = "/internal/getcapabilities")
    public Result getCapabilities() {
        List<ProcessContent> processContentList = new ArrayList<>();
        try {
            processContentList = Wps_2_0_0_Operations.getProcessIdList();
        } catch (JAXBException e) {
            LOGGER.error(I18N.tr("Unable to get the xml file corresponding to the GetCapabilities request." +
                    " \nCause : {0}.", e.getMessage()));
        }
        return ok(render(getCapabilities, "processList", processContentList));
    }
}

