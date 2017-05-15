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
package org.orbisgis.orbisserver.control.web;

import net.opengis.ows._2.AcceptVersionsType;
import net.opengis.ows._2.SectionsType;
import net.opengis.ows._2.CodeType;
import net.opengis.wps._2_0.GetCapabilitiesType;
import net.opengis.wps._2_0.ObjectFactory;
import net.opengis.wps._2_0.ProcessSummaryType;
import net.opengis.wps._2_0.ProcessOffering;
import net.opengis.wps._2_0.WPSCapabilitiesType;
import net.opengis.wps._2_0.ProcessDescriptionType;
import net.opengis.wps._2_0.DescribeProcess;
import org.orbiswps.server.WpsServer;
import org.orbiswps.server.model.JaxbContainer;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.*;

import org.orbisgis.orbisserver.manager.WpsServerManager;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

/**
 * Very light instance of DefaultController containing a WpsServer.
 *
 * @author Sylvain PALOMINOS
 * @author Guillaume MANDE
 */
@Controller
public class IndexController extends DefaultController {

    /** Gets the instance of the WpsServer. */
    private WpsServer wpsServer = WpsServerManager.getWpsServer();

    /** List all of the OrbisWPS processes into a String which be displayed on the index page. */
    private String processesList = "";

    /**  Used to display the corresponding xml file to GetCapabilities method. */
    public WPSCapabilitiesType wpsCapabilitiesType = null;

    /** Used by methods like DescribeProcessRequest to get all the ProcessSummaryType */
    public List<ProcessSummaryType> processSummaryTypeList = null;

    /** List of all identifier of all the processes */
    private List<CodeType> codeTypeList = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);
    private static final I18n I18N = I18nFactory.getI18n(IndexController.class);

    /**
     * Injects a template named 'index'.
     */
    @View("index")
    Template index;

    /**
     * The action method returning the html index page containing a list of all the OrbisWPS processes
     * readable by a human. It handles HTTP GET request on the "/index" URL.
     *
     */
    @Route(method = HttpMethod.GET, uri = "/index")
    public Result index() {
        //Simple example of getting information from the WpsServer
        try {
          GetXMLFromGetCapabilities();
        } catch (JAXBException ignored) {
          LOGGER.error(I18N.tr("Test error message {0}", ignored.toString()));
        }
        return ok(render(index,"liste", processesList));
    }


    /**
     * Method to get the xml fille corresponding to the GetCapabilies request.
     * @throws JAXBException JAXB Exception.
     */
    public void GetXMLFromGetCapabilities() throws JAXBException {
        this.processesList = "";
        Unmarshaller unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
        Marshaller marshaller = JaxbContainer.JAXBCONTEXT.createMarshaller();
        ObjectFactory factory = new ObjectFactory();
        //Creates the getCapabilities
        GetCapabilitiesType getCapabilitiesType = new GetCapabilitiesType();
        GetCapabilitiesType.AcceptLanguages acceptLanguages = new GetCapabilitiesType.AcceptLanguages();
        acceptLanguages.getLanguage().add("*");
        getCapabilitiesType.setAcceptLanguages(acceptLanguages);
        AcceptVersionsType acceptVersionsType = new AcceptVersionsType();
        acceptVersionsType.getVersion().add("2.0.0");
        getCapabilitiesType.setAcceptVersions(acceptVersionsType);
        SectionsType sectionsType = new SectionsType();
        sectionsType.getSection().add("All");
        getCapabilitiesType.setSections(sectionsType);
        //Marshall the DescribeProcess object into an OutputStream
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(factory.createGetCapabilities(getCapabilitiesType), out);
        //Write the OutputStream content into an Input stream before sending it to the wpsService
        InputStream in = new DataInputStream(new ByteArrayInputStream(out.toByteArray()));
        ByteArrayOutputStream xml = (ByteArrayOutputStream) wpsServer.callOperation(in);
        //Get back the result of the DescribeProcess request as a BufferReader
        ByteArrayInputStream resultXml = new ByteArrayInputStream(xml.toByteArray());
        //Unmarshall the result and check that the object is the same as the resource unmashalled xml.
        Object resultObject = unmarshaller.unmarshal(resultXml);
        WPSCapabilitiesType wpsCapabilitiesType = (WPSCapabilitiesType)((JAXBElement)resultObject).getValue();
        this.wpsCapabilitiesType = wpsCapabilitiesType;
        List<ProcessSummaryType> list = wpsCapabilitiesType.getContents().getProcessSummary();
        for(ProcessSummaryType processSummaryType : list){
            this.processesList = this.processesList + processSummaryType.getTitle().get(0).getValue() + "\n";
        }
        this.processSummaryTypeList = list;
    }
}
