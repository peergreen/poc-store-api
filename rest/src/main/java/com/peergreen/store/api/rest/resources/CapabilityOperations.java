package com.peergreen.store.api.rest.resources;


import java.util.Collection;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.exception.NoEntityFoundException;

@Path(value = "/capabilities")
public class CapabilityOperations {

    private IPetalController petalController;

    /**
     * Retrieve all petals satisfying a requirement.
     *
     * @param name capability name
     * @param version capability version
     * @param namespace capability namespace
     * @return all petals providing the given capability
     * @throws JSONException
     */
    @GET
    @Path("/{name}/{version}/{namespace}/petals")
    public Response getPetals(@Context UriInfo uri, 
            @PathParam(value = "name") String name,
            @PathParam(value = "version") String version,
            @PathParam(value = "namespace") String namespace)
                    throws JSONException {

        JSONObject result = new JSONObject();
        String path = uri.getBaseUri().toString();

        try {
            Collection<Petal> petals = petalController.
                    getPetalsForCapability(name, version, namespace);
            if (!petals.isEmpty()) {
                Iterator<Petal> it = petals.iterator();
                Petal p;
                while (it.hasNext()) {
                    p = it.next();
                    result.put(p.getArtifactId(), path.concat("petal/"
                            + p.getVendor() + "/" + p.getArtifactId()
                            + "/" + p.getVersion()));
                }
                return Response.status(Status.OK).entity(
                        result.toString()).build();
            } else{
                return Response.status(Status.OK).entity(
                        "No petal does not provide " + name).build();
            }
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
    
    /**
     * Method to set IPetalController instance to use.
     *
     * @param petalController the PetalController to set
     */
    public void setPetalController(IPetalController petalController) {
        this.petalController = petalController;
    }

}