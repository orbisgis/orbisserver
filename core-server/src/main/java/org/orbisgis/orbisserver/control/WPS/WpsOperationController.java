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

package org.orbisgis.orbisserver.control.wps;

import net.opengis.ows._2.*;
import org.orbisgis.orbisserver.manager.Wps_2_0_0_Operations;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.*;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

/**
 * Instance of DefaultController used to control the wps operation's page with the good http request.
 * It gets an instance of WpsServerManager to be able to display the result of wps operation,
 * here it display the xml file corresponding to wps operation.
 *
 * @author Guillaume MANDE
 */
@Controller
public class WpsOperationController extends DefaultController {
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WpsOperationController.class);
    /**
     * I18N object
     */
    private static final I18n I18N = I18nFactory.getI18n(WpsOperationController.class);

    /**
     * The action method returning the xml file corresponding to the wps operations. It handles
     * HTTP GET request on the "/orbisserver/ows" URL. Displays exception (MissingParameterValue or InvalidParameterValue)
     * in the logger if is the request is not well written.
     *
     * A good request for the GetCapabilities operation should be
     * http://localhost:8080/orbisserver/ows?service=WPS&version=2.0.0&request=GetCapabilities
     *
     * A good request for the DescribeProcess operation should be
     * http://localhost:8080/orbisserver/ows?service=WPS&version=2.0.0&
     * request=DescribeProcess&identifier=orbisgis:wps:official:deleteRows
     *
     * @Parameter service Name of the service you want to use. Should be wps here.
     * @Parameter version Version of the service. It must be an accepted version like 2.0.0.
     * @Parameter request Request according to the service that you ask to the server. It could be GetCapabilities.
     * @Parameter identifier Identifier of the process used by operation like DescribeProcess.
     * @return the xml file
     */
    @Route(method = HttpMethod.GET, uri = "/orbisserver/ows")
    public Result displayXML(@Parameter("service") String service, @Parameter("version") String version,
                             @Parameter("request") String request, @Parameter("identifier") String identifier)
            throws JAXBException,IOException {

        ExceptionType exceptionType = new ExceptionType();
        ExceptionReport exceptionReport = new ExceptionReport();

        if (service == null || service.isEmpty()) {
            exceptionType.setExceptionCode("MissingParameterValue");
            exceptionType.getExceptionText().add("Operation request does not include a parameter value");
            exceptionReport.getException().add(exceptionType);
            LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                    + exceptionReport.getException().get(0).getExceptionText().get(0)));
            return badRequest(I18N.tr("You need to enter a service to do queries, it should be wps here"));
        }

        if (!service.equals("WPS")) {
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
            exceptionReport.getException().add(exceptionType);
            LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                    + exceptionReport.getException().get(0).getExceptionText().get(0)));
            return badRequest(I18N.tr("The service was not properly written, it should be wps here"));
        }

        if (version == null || version.isEmpty()) {
            exceptionType.setExceptionCode("MissingParameterValue");
            exceptionType.getExceptionText().add("Operation request does not include a parameter value");
            exceptionReport.getException().add(exceptionType);
            LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                    + exceptionReport.getException().get(0).getExceptionText().get(0)));
            return badRequest(I18N.tr("You need to enter the version of wps to get the corresponding xml file"));
        }

        if (!version.equals("2.0.0")) {
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
            exceptionReport.getException().add(exceptionType);
            LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                    + exceptionReport.getException().get(0).getExceptionText().get(0)));
            return badRequest(I18N.tr("Please enter a good version of wps, it should be 2.0.0"));
        }

        if (request == null || request.isEmpty()) {
            exceptionType.setExceptionCode("MissingParameterValue");
            exceptionType.getExceptionText().add("Operation request does not include a parameter value");
            exceptionReport.getException().add(exceptionType);
            LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                    + exceptionReport.getException().get(0).getExceptionText().get(0)));
            return badRequest(I18N.tr("You need to enter the request to get the corresponding xml file"));
        }

        switch (request) {
            case "GetCapabilities":
                if (identifier != null) {
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
                    exceptionReport.getException().add(exceptionType);
                    LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                            + exceptionReport.getException().get(0).getExceptionText().get(0)));
                    return badRequest(I18N.tr("GetCapabilities does not need identifier, so don't write it."));
                }
                try {
                    return ok(Wps_2_0_0_Operations.getResponseFromGetCapabilities());
                } catch (JAXBException e) {
                    LOGGER.error(I18N.tr("Unable to get the xml file corresponding to the GetCapabilities request." +
                            " \nCause : {0}.", e.getMessage()));
                    return ok(e);
                }
            case "DescribeProcess":
                if (identifier == null || identifier.isEmpty()) {
                    exceptionType.setExceptionCode("MissingParameterValue");
                    exceptionType.getExceptionText().add("Operation request does not include a parameter value");
                    exceptionReport.getException().add(exceptionType);
                    LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                            + exceptionReport.getException().get(0).getExceptionText().get(0)));
                    return badRequest(I18N.tr("An Identifier is missing."));
                }

                if (!Wps_2_0_0_Operations.getCodeTypeList().contains(identifier)) {
                    exceptionType.setExceptionCode("NoSuchProcess");
                    exceptionType.getExceptionText().add("One of the identifiers passed does not match with any " +
                            "of the processes offered by this server.");
                    exceptionReport.getException().add(exceptionType);
                    LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                            + exceptionReport.getException().get(0).getExceptionText().get(0)));
                    return badRequest(I18N.tr("No process has this identifier, please be more accurate."));
                }

                try {
                    return ok(Wps_2_0_0_Operations.getResponseFromDescribeProcess(identifier));
                } catch (JAXBException e) {
                    LOGGER.error(I18N.tr("Unable to get the xml file corresponding to the DescribeProcess request." +
                            " \nCause : {0}.", e.getMessage()));
                    return ok(e);
                }
            default:
                exceptionType.setExceptionCode("InvalidParameterValue");
                exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
                exceptionReport.getException().add(exceptionType);
                LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                        + exceptionReport.getException().get(0).getExceptionText().get(0)));
                return badRequest(I18N.tr("This request does not exist, please try something else like GetCapabilities."));
        }
    }

    /**
     * The action method returning the xml file corresponding to the wps operations. It handles
     * HTTP POST request on the "/orbisserver/ows/ExecuteRequest" URL.
     * This method is called whenever the customer adds parameters in the form at /execute.
     *
     * @FormParameter identifier Identifier of the process you want to use, like orbisgis:wps:official:deleteRows.
     * @FormParameter response Desired response format, i.e. a response document or raw data.
     * @FormParameter mode Desired execution mode.
     * @FormParameter input Data inputs provided to this process execution
     * @FormParameter output Specification of outputs expected from the process execution, including the desired format
     * and transmission mode for each output.
     * @return the xml file
     */
    @Route(method = HttpMethod.POST, uri = "/orbisserver/ows/ExecuteRequest")
    public Result displayXMLForExecute(@FormParameter("identifier") String identifier,
                                       @FormParameter("response") String response, @FormParameter("mode") String mode,
                                       @FormParameter("input") String input, @FormParameter("output") String output)
            throws JAXBException, IOException {

        ExceptionType exceptionType = new ExceptionType();
        ExceptionReport exceptionReport = new ExceptionReport();

        if (!Wps_2_0_0_Operations.getCodeTypeList().contains(identifier)) {
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
            exceptionReport.getException().add(exceptionType);
            LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                    + exceptionReport.getException().get(0).getExceptionText().get(0)));
            return badRequest(I18N.tr("No process has this identifier, please be more accurate."));
        }

        if(!response.equals("raw") && !response.equals("document")) {
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
            exceptionReport.getException().add(exceptionType);
            LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                    + exceptionReport.getException().get(0).getExceptionText().get(0)));
            return badRequest(I18N.tr("The desired response format is incorrect, please set it to document or raw."));
        }

        if(!mode.equals("auto") && !mode.equals("sync") && !mode.equals("async")) {
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
            exceptionReport.getException().add(exceptionType);
            LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : "
                    + exceptionReport.getException().get(0).getExceptionText().get(0)));
            return badRequest(I18N.tr("The desired execution method is incorrect, please set it to auto, sync or async."));
        }

        try {
            return ok(Wps_2_0_0_Operations.getResponseFromExecute(identifier, response, mode, input, output));
        } catch (JAXBException e) {
            LOGGER.error(I18N.tr("Unable to get the xml file corresponding to the Execute request." +
                    " \nCause : {0}.", e.getMessage()));
            return ok(e);
        }
    }
}
