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
package org.orbisgis.orbisserver.manager;

import net.opengis.ows._2.AcceptVersionsType;
import net.opengis.ows._2.CodeType;
import net.opengis.ows._2.SectionsType;
import net.opengis.wps._2_0.*;
import org.apache.felix.ipojo.annotations.Requires;
import org.orbiswps.scripts.WpsScriptPlugin;
import org.orbiswps.server.WpsServer;
import org.orbiswps.server.WpsServerImpl;
import org.orbiswps.server.model.JaxbContainer;

import javax.sql.DataSource;
import javax.xml.bind.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class managing the WpsServer instances.
 *
 * @author Sylvain PALOMINOS
 */
public class WpsServerManager extends DescribeProcess{
    /**
     * Data source used by the WpsServer.
     */
    @Requires
    private static DataSource ds;

    /**
     * Instance of the WpsServer.
     */
    private static WpsServer wpsServer;

    /** List of all identifier of all the processes */
    private List<CodeType> codeTypeList;

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
     * Creates an  instance of the WpsServer.
     */
    private static void createWpsServerInstance(){
        wpsServer = new WpsServerImpl(System.getProperty("java.io.tmpdir"), ds);
        WpsScriptPlugin scriptPlugin = new WpsScriptPlugin();
        scriptPlugin.setWpsServer(wpsServer);
        scriptPlugin.activate();
    }

    /**
     * Method to get the xml file corresponding to the GetCapabilities request.
     *
     * @throws JAXBException JAXB Exception.
     * @Return The processes list into a String.
     */
    public String getListFromGetCapabilities() throws JAXBException {
        String processesList = "";

        this.codeTypeList = new ArrayList<CodeType>();

        List<ProcessSummaryType> list = getXMLFromGetCapabilities().getContents().getProcessSummary();
        for (ProcessSummaryType processSummaryType : list) {
            processesList = processesList + processSummaryType.getTitle().get(0).getValue() + "\n";
            this.codeTypeList.add(processSummaryType.getIdentifier());
        }
        return processesList;
    }

    /**
     * Return the wpsCapabilitiesType object which is a xml object.
     *
     * @throws JAXBException JAXB Exception.
     * @Return the wpsCapabilitiesType object.
     */
    public WPSCapabilitiesType getXMLFromGetCapabilities() throws JAXBException {
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
        ByteArrayOutputStream xml = (ByteArrayOutputStream) this.getWpsServer().callOperation(in);
        //Get back the result of the DescribeProcess request as a BufferReader
        ByteArrayInputStream resultXml = new ByteArrayInputStream(xml.toByteArray());
        //Unmarshall the result and check that the object is the same as the resource unmashalled xml.
        Object resultObject = unmarshaller.unmarshal(resultXml);
        WPSCapabilitiesType wpsCapabilitiesType = (WPSCapabilitiesType) ((JAXBElement) resultObject).getValue();

        return wpsCapabilitiesType;
    }

    /**
     * Return the xml file corresponding to the DescribeProcess request.
     *
     * @throws JAXBException JAXB Exception.
     * @Return a ProcessOfferings object
     */
    public Object getXMLFromDescribeProcess(String id) throws JAXBException {
        getListFromGetCapabilities();
        Unmarshaller unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
        Marshaller marshaller = JaxbContainer.JAXBCONTEXT.createMarshaller();
        //Creates the DescribeProcess
        DescribeProcess describeProcess = new DescribeProcess();
        describeProcess.setLang("en");
        describeProcess.getIdentifier().add(getCodeTypeFromId(id));
        //Marshall the DescribeProcess object into an OutputStream
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(describeProcess, out);
        //Write the OutputStream content into an Input stream before sending it to the wpsService
        InputStream in = new DataInputStream(new ByteArrayInputStream(out.toByteArray()));
        ByteArrayOutputStream xml = (ByteArrayOutputStream) this.getWpsServer().callOperation(in);
        //Get back the result of the DescribeProcess request as a BufferReader
        InputStream resultXml = new ByteArrayInputStream(xml.toByteArray());
        //Unmarshall the result and check that the object is the same as the resource unmashalled xml.
        Object resultObject = unmarshaller.unmarshal(resultXml);

        return resultObject;
    }

    /**
     * Return the xml file corresponding to the Execute request.
     *
     * @throws JAXBException JAXB Exception.
     * @Return a StatusInfo object
     */
    public Object getXMLFromExecute(String id, String response, String mode, String input, String output) throws JAXBException,IOException {
        Unmarshaller unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
        Marshaller marshaller = JaxbContainer.JAXBCONTEXT.createMarshaller();
        ObjectFactory factory = new ObjectFactory();
        //Creates the ExecuteRequestType
        ExecuteRequestType ert = new ExecuteRequestType();
        ert.setIdentifier(getCodeTypeFromId(id));
        ert.setResponse(response);
        ert.setMode(mode);
        //Marshall the DescribeProcess object into an OutputStream
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(factory.createExecute(ert), out);
        //Write the OutputStream content into an Input stream before sending it to the wpsService
        InputStream in = new DataInputStream(new ByteArrayInputStream(out.toByteArray()));
        ByteArrayOutputStream xml = (ByteArrayOutputStream) this.getWpsServer().callOperation(in);
        //Get back the result of the DescribeProcess request as a BufferReader
        InputStream resultXml = new ByteArrayInputStream(xml.toByteArray());
        //Unmarshall the result and check that the object is the same as the resource unmashalled xml.
        Object resultObject = unmarshaller.unmarshal(resultXml);

        return resultObject;  //resultObject is a StatusInfo Object
    }

    /**
     * This method returns the list of identifiers from the CodeType's list.
     *
     * @throws JAXBException
     * @return List of String
     */
    public List<String> getCodeTypeList() throws JAXBException {
        getListFromGetCapabilities();
        List<String> listId = new ArrayList<String>();

        for(CodeType codeType : this.codeTypeList){
            listId.add(codeType.getValue());
        }
        return listId;
    }

    /**
     * This method returns the CodeType corresponding to the identifier.
     *
     * @throws JAXBException
     * @return CodeType object
     */
    public CodeType getCodeTypeFromId(String id) throws JAXBException{
        getListFromGetCapabilities();
        CodeType codeTypeFinal = new CodeType();
        for(CodeType codeType : codeTypeList){
            if(codeType.getValue().equals(id)){
                codeTypeFinal = codeType;
            }
        }
        return codeTypeFinal;
    }
}
