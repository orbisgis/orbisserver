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

package org.orbisgis.orbisserver.control.xml;

 import net.opengis.ows._2.*;
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

 import org.orbisgis.orbisserver.control.web.IndexController;

 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;

 /**
  * Instance of DefaultController containing a WpsServer.
  *
  * @author Guillaume MANDE
  */
 @Controller
 public class GetCapabilitiesController extends DefaultController {

   /**
    * IndexController's object, used to get some useful attributes.
    */
   private IndexController indexController = new IndexController();

   /** Logger */
   private static final Logger LOGGER = LoggerFactory.getLogger(GetCapabilitiesController.class);
   /** I18N object */
   private static final I18n I18N = I18nFactory.getI18n(GetCapabilitiesController.class);

   /**
    * The action method returning the xml file corresponding to the GetCapabilities method. It handles
    * HTTP GET request on the "/orbisserver/wps" URL. Displays exception (MissingParameterValue or InvalidParameterValue) in the logger if is the request is not well writen.
    * A good request should be http://localhost:8080/orbisserver/wps?service=WPS&version=2.0.0&request=GetCapabilities
    *
    * @Parameter service Name of the service you want to use. Should be WPS here.
    * @Parameter version Version of the service. It must be an accepted version like 2.0.0.
    * @Parameter request Request according to the service that you ask to the server. It could be GetCapabilities.
    *
    * @return the xml file
    */
   @Route(method = HttpMethod.GET, uri = "/orbisserver/wps")
   public Result displayXML(@Parameter("service") String service, @Parameter("version") String version, @Parameter("request") String request){

     ExceptionType exceptionType = new ExceptionType();
     ExceptionReport exceptionReport = new ExceptionReport();

     //Simple example of getting information from the WpsServer
     if(service != null && !service.isEmpty()){
       if(service.equals("WPS")){
         if(version != null && !version.isEmpty()){
           if(version.equals("2.0.0")){
              if(request!= null && !request.isEmpty()){
                if(request.equals("GetCapabilities")){
                  try {
                   indexController.GetXMLFromGetCapabilities();
                  } catch (JAXBException e) {
                   LOGGER.error(I18N.tr("Unable to parse the incoming xml. \nCause : {0}.", e.getMessage()));
                  }
                  return ok(indexController.wpsCapabilitiesType);
                }else{
                  exceptionType.setExceptionCode("InvalidParameterValue");
                  exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
                  exceptionReport.getException().add(exceptionType);
                  LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : " + exceptionReport.getException().get(0).getExceptionText().get(0)));
                  return badRequest(I18N.tr("The request was not properly written"));
                }
              }else{
                exceptionType.setExceptionCode("MissingParameterValue");
                exceptionType.getExceptionText().add("Operation request does not include a parameter value");
                exceptionReport.getException().add(exceptionType);
                LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : " + exceptionReport.getException().get(0).getExceptionText().get(0)));
                return badRequest(I18N.tr("You need to enter the request to get the corresponding xml file"));
              }
            }else{
              exceptionType.setExceptionCode("InvalidParameterValue");
              exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
              exceptionReport.getException().add(exceptionType);
              LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : " + exceptionReport.getException().get(0).getExceptionText().get(0)));
              return badRequest(I18N.tr("Please enter a good version of WPS, it should be 2.0.0"));
            }
          }else{
            exceptionType.setExceptionCode("MissingParameterValue");
            exceptionType.getExceptionText().add("Operation request does not include a parameter value");
            exceptionReport.getException().add(exceptionType);
            LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : " + exceptionReport.getException().get(0).getExceptionText().get(0)));
            return badRequest(I18N.tr("You need to enter the version of WPS to get the corresponding xml file"));
          }
        }else{
          exceptionType.setExceptionCode("InvalidParameterValue");
          exceptionType.getExceptionText().add("Operation request contains an invalid parameter value");
          exceptionReport.getException().add(exceptionType);
          LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : " + exceptionReport.getException().get(0).getExceptionText().get(0)));
          return badRequest(I18N.tr("The service was not properly written, it should be WPS here"));
        }
      }else{
        exceptionType.setExceptionCode("MissingParameterValue");
        exceptionType.getExceptionText().add("Operation request does not include a parameter value");
        exceptionReport.getException().add(exceptionType);
        LOGGER.error(I18N.tr(exceptionReport.getException().get(0).getExceptionCode() + " : " + exceptionReport.getException().get(0).getExceptionText().get(0)));
        return ok(I18N.tr("You need to enter a service to do queries, it should be WPS here"));
      }
    }
}
