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

import net.opengis.wps._2_0.ProcessOfferings;
import org.junit.Assert;
import org.junit.Test;

/**
 * A couple of unit tests.
 */
public class WpsServerManagerTest {
    /**
     * Checks that the WpsOperationController is returning OK, and returning the good response corresponding to the DescribeProcess method.
     */
    @Test
    public void testGetXMLFromDescribeProcess() throws Exception {
        WpsServerManager wpsServerManager = new WpsServerManager();
        Object resultObject = wpsServerManager.getXMLFromDescribeProcess("orbisgis:wps:official:deleteRows");

        Assert.assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        Assert.assertTrue("Error on unmarshalling the WpsService answer, the object should be a ProcessOfferings",
                resultObject instanceof ProcessOfferings);
        Assert.assertNotNull("Error on unmarshalling the WpsService answer, the ProcessOfferings should not be null",
                ((ProcessOfferings)resultObject).getProcessOffering());
    }
}
