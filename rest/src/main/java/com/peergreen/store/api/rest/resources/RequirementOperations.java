package com.peergreen.store.api.rest.resources;

import java.util.Collection;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.exception.NoEntityFoundException;

@Path(value="/requirement")
public class RequirementOperations {

    private IPetalController petalController;

    /**
     * Retrieve all petals satisfying a requirement.
     * @param id the requirement's id
     * @return All the petals existing which satisfy the given requirement
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}/petals")
    public Response getPetals(@Context UriInfo uri,
            @PathParam(value = "name") String name) throws JSONException {

        JSONObject result = new JSONObject();
        String path = uri.getBaseUri().toString();

        try {
            Collection<Petal> petals = petalController.
                    getPetalsForRequirement(name);
            if (!petals.isEmpty()) {
                Iterator<Petal> it = petals.iterator();
                Petal p;
                while (it.hasNext()) {
                    p = it.next();
                    result.put(p.getArtifactId(), path.concat("petal/"
                            + p.getVendor().getVendorName() + "/"
                            + p.getArtifactId() + "/" + p.getVersion()));
                }
                return Response.status(Status.OK).entity(
                        result.toString()).build();
            } else{
                return Response.status(Status.OK).entity(
                        "No petals have " + name + "in requirement").build();
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
