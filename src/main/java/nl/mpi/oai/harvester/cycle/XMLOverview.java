/*
 * Copyright (C) 2015, The Max Planck Institute for
 * Psycholinguistics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License is included in the file
 * LICENSE-gpl-3.0.txt. If that file is missing, see
 * <http://www.gnu.org/licenses/>.
 */

package nl.mpi.oai.harvester.cycle;

import nl.mpi.oai.harvester.generated.ObjectFactory;
import nl.mpi.oai.harvester.generated.OverviewType;
import nl.mpi.oai.harvester.harvesting.HarvestingException;

import javax.xml.bind.*;

import java.io.File;

/**
 * <br> OverviewType object marshalling <br><br>
 *
 * Read and write an overview to and from an XML file by using the adapters
 * defined in the package. The methods in this class communicate the general
 * cycle and endpoint attributes through the overview and endpoint interfaces.
 *
 * After the XML file is read by the constructor, the getOverview and
 * getEndpoint methods can be invoked. When a client modifies an overview or
 * endpoint object, it needs to write back the overview to the file. It can do
 * so by invoking the finalize method in this class.
 *
 * Note: this class relies on JAXB to generate the types that reflect the XSD
 * defined overviews.
 *
 * kj: consider adding a getNextEndpoint () method
 * A method like this would be used for traversing, rather than searching. Also
 * add a getEndpoint for resetting the traversal. However, things might become
 * complicated, because an endpoint could be added in parallel somewhere in the
 * file, making traversing difficult. A getEndpoint (EndpointType endpointType)
 * method could also work.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public final class XMLOverview {

    // the file supplied on construction
    private final File file;

    // reference to a JAXB generated overviewType object representing the XML
    OverviewType overviewType;

    // factory that creates objects of the generated classes
    final ObjectFactory factory;

    /**
     * <br> Associate the cycle with an XML file <br><br>
     *
     * This constructor initialises the harvestingType and endpointType objects
     * wrapped in the cycle class with data from the file.
     *
     * @param fileName name of the file
     */
    public XMLOverview(String fileName) {

        // create factory that creates objects of the generated classes
        factory = new ObjectFactory();

        // ask the factory for an object that can represent XML
        overviewType = factory.createOverviewType();

        // remember the XML file
        file = new File(fileName);

        // get the XML from this file
        Object object = JAXB.unmarshal(file, OverviewType.class);

        /* Check if the object is in the CycleType class. Note: if the
           unmarshalling method returns null, the object is not in the class,
           otherwise it is.
          */
        if (object == null) {
            throw new HarvestingException();
        } else {
            overviewType = (OverviewType) object;
        }
    }

    /**
     * <br> Create an adapter for the harvestingType object <br><br>
     *
     * Note: at the level of the XSD, the endpoints are part of harvesting, at
     * the level of the interface the general harvesting characteristics are
     * separate from the list of endpoints. In fact, the client never gets the
     * complete list, it gets an endpoint referred to by the URI.
     *
     * @return cycle attributes
     */
    public Overview getOverview() {

        return new OverviewAdapter(this);
    }

    /**
     * <br> Create an adapter for an endpointType object <br><br>
     *
     * This method returns the adapter object for the endpoint indicated
     * by the endpointURI
     *
     * Note: at the level of the XSD, the endpoints are part of harvesting, at
     * the level of the interface the general harvesting characteristics are
     * separate from the list of endpoints. In fact, the client never gets the
     * complete list, it gets an endpoint referred to by the URI.
     *
     * @param endpointURI the URI of the endpoint state requested
     * @param group       the group the endpoint belongs to
     * @return            endpoint attributes
     */
    public Endpoint getEndpoint(String endpointURI, String group) {

        if (endpointURI == null){
            throw new IllegalArgumentException("endpoint URI is null");
        }
        if (group == null){
            throw new IllegalArgumentException("endpoint group is null");
        }

        return new EndpointAdapter(endpointURI, group, this);
    }

    /**
     * Save the overview
     */
    protected synchronized void save (){

        // marshall the overview
        JAXB.marshal(overviewType, file);
    }
}