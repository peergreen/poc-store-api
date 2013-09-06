package com.peergreen.store.api.rest.resources;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.db.client.ejb.entity.Capability;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.exception.NoEntityFoundException;

@Path(value = "/capability")
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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{name}/{version}/{namespace}")
    public Response getCapability(
            @Context UriInfo uri, 
            @PathParam(value = "name") String name,
            @PathParam(value = "version") String version,
            @PathParam(value = "namespace") String namespace)
                    throws JSONException {

        JSONObject result = new JSONObject();

        Capability capability = petalController
                .getCapability(name, version, namespace);

        if (capability == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        result.put("name", capability.getCapabilityName());
        result.put("version", capability.getVersion());
        result.put("namespace", capability.getNamespace());
        result.put("petals", uri.getAbsolutePath().toString()
                .concat("/petals"));

        return Response.ok(Status.OK).entity(result.toString()).build();
    }

    /**
     * Retrieve all petals providing a capability.
     *
     * @param name capability name
     * @param version capability version
     * @param namespace capability namespace
     * @return all petals providing the given capability
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}/{version}/{namespace}/petals")
    public Response getPetals(@Context UriInfo uri, 
            @PathParam(value = "name") String name,
            @PathParam(value = "version") String version,
            @PathParam(value = "namespace") String namespace)
                    throws JSONException {

        JSONObject result = new JSONObject();

        try {
            Collection<Petal> petals = petalController.
                    getPetalsForCapability(name, version, namespace);
            if (!petals.isEmpty()) {
                Iterator<Petal> iterator = petals.iterator();

                List<JSONObject> petalsList = new ArrayList<>();
                while(iterator.hasNext()){
                    Petal p = iterator.next();

                    JSONObject obj = new JSONObject();
                    obj.put("vendorName", p.getVendor().getVendorName());
                    obj.put("artifactId", p.getArtifactId());
                    obj.put("version", p.getVersion());
                    obj.put("href", uri.getBaseUri().toString()
                            .concat("petal/" + p.getPid() + "/metadata"));

                    petalsList.add(obj);
                }
                
                result.put("petals", petalsList);
                
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