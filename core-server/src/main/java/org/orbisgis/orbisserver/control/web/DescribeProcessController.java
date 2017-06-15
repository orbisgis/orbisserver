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

import net.opengis.ows._2.RangeType;
import net.opengis.ows._2.ValueType;
import net.opengis.wps._2_0.*;
import org.orbisgis.orbisserver.control.utils.InputContent;
import org.orbisgis.orbisserver.manager.Wps_2_0_0_Operations;
import org.orbiswps.server.model.Enumeration;
import org.orbiswps.server.model.JDBCColumn;
import org.orbiswps.server.model.JDBCTable;
import org.orbiswps.server.model.JDBCValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instance of DefaultController used to control the describeProcess page with a http request.
 *
 * @author Sylvain PALOMINOS
 */
@Controller
public class DescribeProcessController extends DefaultController {
    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(DescribeProcessController.class);
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(DescribeProcessController.class);

    /**
     * Injects a template named 'describeProcess'.
     */
    @View("describeProcess")
    Template describeProcess;

    /**
     * The action method returning the html index page containing a form corresponding to a process.
     * It handles HTTP GET request on the "/internal/describeProcess" URL.
     *
     * @return The page including the processes form.
     */
    @Route(method = HttpMethod.GET, uri = "/internal/describeProcess")
    public Result describeProcess(@Parameter("id") String id) {
        ProcessOfferings processOfferings = null;
        try {
            processOfferings = (ProcessOfferings)Wps_2_0_0_Operations.getResponseFromDescribeProcess(id);
        } catch (JAXBException e) {
            LOGGER.error(I18N.tr("Unable to get the xml file corresponding to the DescribeProcess request." +
                    " \nCause : {0}.", e.getMessage()));
        }
        ProcessDescriptionType process = processOfferings.getProcessOffering().get(0).getProcess();
        String abstr = process.getAbstract().get(0).getValue();
        List<InputContent> inputList = new ArrayList<>();
        for(InputDescriptionType input : process.getInput()){
            String title = input.getTitle().get(0).getValue();
            String inputId = input.getIdentifier().getValue();
            String name = input.getDataDescription().getValue().getClass().getSimpleName();
            String type = "";
            Map<String, Object> attributeMap = new HashMap<>();
            DataDescriptionType dataDescriptionType = input.getDataDescription().getValue();
            if(dataDescriptionType instanceof LiteralDataType){
                LiteralDataType literalData  =(LiteralDataType)dataDescriptionType;
                for(LiteralDataType.LiteralDataDomain ldd : literalData.getLiteralDataDomain()) {
                    if(ldd.isDefault()) {
                        String dataType = ldd.getDataType().getValue();
                        if (dataType.equalsIgnoreCase("string")) {
                            type = "string";
                        }
                        if (dataType.equalsIgnoreCase("boolean")) {
                            type = "boolean";
                            attributeMap.put("value", "false");
                            if(ldd.isSetDefaultValue()) {
                                attributeMap.put("value", ldd.getDefaultValue().getValue());
                            }
                        }
                        if (dataType.equalsIgnoreCase("double") || dataType.equalsIgnoreCase("integer") ||
                                dataType.equalsIgnoreCase("float") || dataType.equalsIgnoreCase("short") ||
                                dataType.equalsIgnoreCase("byte") || dataType.equalsIgnoreCase("unsigned_byte") ||
                                dataType.equalsIgnoreCase("long")) {
                            if(dataType.equalsIgnoreCase("double") || dataType.equalsIgnoreCase("float")) {
                                attributeMap.put("spacing", "0.1");
                            }
                            type = "number";
                            if(ldd.isSetDefaultValue()) {
                                attributeMap.put("value", ldd.getDefaultValue().getValue());
                            }
                            if(ldd.isSetAllowedValues()) {
                                for(Object valueOrRange : ldd.getAllowedValues().getValueOrRange()) {
                                    if(valueOrRange instanceof ValueType) {
                                        ValueType value = (ValueType)valueOrRange;
                                        attributeMap.put("value", value.getValue());
                                    }
                                    if(valueOrRange instanceof RangeType) {
                                        RangeType range = (RangeType)valueOrRange;
                                        attributeMap.put("min", range.getMinimumValue().getValue());
                                        attributeMap.put("max", range.getMaximumValue().getValue());
                                        attributeMap.put("spacing", range.getSpacing().getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(dataDescriptionType instanceof JDBCTable){
                attributeMap.put("value", "Table name");
                JDBCTable table = (JDBCTable)dataDescriptionType;
                if(table.getDefaultValue() != null && !table.getDefaultValue().isEmpty()){
                    attributeMap.put("value", table.getDefaultValue());
                }
            }
            if(dataDescriptionType instanceof JDBCColumn){
                attributeMap.put("value", "Columns name");
                JDBCColumn column = (JDBCColumn)dataDescriptionType;
                if(column.getDefaultValues() != null && column.getDefaultValues().length>0){
                    StringBuilder str = new StringBuilder();
                    for(String val : column.getDefaultValues()){
                        if(str.length() > 0){
                            str.append(",");
                        }
                        str.append(val);
                    }
                    attributeMap.put("value", str.toString());
                }
            }
            if(dataDescriptionType instanceof JDBCValue){
                attributeMap.put("value", "Values name");
                JDBCValue value = (JDBCValue)dataDescriptionType;
                if(value.getDefaultValues() != null && value.getDefaultValues().length>0){
                    StringBuilder str = new StringBuilder();
                    for(String val : value.getDefaultValues()){
                        if(str.length() > 0){
                            str.append(",");
                        }
                        str.append(val);
                    }
                    attributeMap.put("value", str.toString());
                }
            }
            if(dataDescriptionType instanceof Enumeration){
                Enumeration enumeration = (Enumeration)dataDescriptionType;
                attributeMap.put("multiSelection", enumeration.isMultiSelection());
                attributeMap.put("valueList", enumeration.getValues());
            }
            InputContent inputContent = new InputContent(title, name,inputId, type, attributeMap);
            inputList.add(inputContent);
        }
        List<OutputDescriptionType> outputList = processOfferings.getProcessOffering().get(0).getProcess().getOutput();
        return ok(render(describeProcess, "abstr", abstr, "inputList", inputList,
                "outputList", outputList, "processId", process.getIdentifier().getValue()));
    }
}

