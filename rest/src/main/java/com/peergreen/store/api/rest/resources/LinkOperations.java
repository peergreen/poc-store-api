package com.peergreen.store.api.rest.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.ejb.entity.Link;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;

@Path(value = "/link")
public class LinkOperations {

    private IStoreManagment storeManagement; 
    private static Log logger = LogFactory.getLog(LinkOperations.class);

    /**
     * Create a new link between store
     * @param uri contextual URI injected by JAX-RS
     * @param payload HTTP request payload
     * @return the link created 
     * @throws JSONException 
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLink(
            @Context UriInfo uri,
            String payload) throws JSONException {

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(payload); 
            String url = jsonObject.getString("url");
            String description = jsonObject.getString("description");
            Link l = storeManagement.addLink(url, description);

            JSONObject res = new JSONObject();
            res.put("id", l.getLinkId());
            res.put("href", uri.getAbsolutePath().toString()
                    .concat(Integer.toString(l.getLinkId())));

            return Response.status(Status.CREATED)
                    .entity(res.toString()).build();

        } catch (EntityAlreadyExistsException e) {
            logger.warn("Link with specified URL " +
                    "already exists in database.", e);
            return Response.ok(Status.CONFLICT).build();
        }
    }

    /**
     * Method to remove a link to a remote store.
     *
     * @param id link id
     * @return Response containing details on removed link
     * @throws JSONException 
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response deleteLink(@PathParam(value="id") int id) throws JSONException {
        Link link = storeManagement.getLink(id);

        if (link == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        storeManagement.removeLink(link.getUrl());

        JSONObject result = new JSONObject();
        result.put("id", link.getLinkId());
        result.put("description", link.getDescription());
        result.put("url", link.getUrl());

        return Response.ok(Status.OK).entity(result.toString()).build();
    }

    /**
     * Method to update a link (description)
     *
     * @param id link's id
     * @param payload HTTP request payload (containing new description)
     * @return Response containing details on modified link
     * @throws JSONException 
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response updateLink(
            @PathParam(value="id") int id,
            String payload) throws JSONException {

        Link link = storeManagement.getLink(id);

        if (link == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        JSONObject result = new JSONObject();
        result.put("id", link.getLinkId());
        result.put("description", link.getDescription());
        result.put("url", link.getUrl());

        return Response.ok(Status.OK).entity(result.toString()).build();
    }

    /**
     * Method to retrieve a link.
     *
     * @param uri contextual URI injected by JAX-RS
     * @param id link id
     * @return JSON response containing link's details
     * @throws JSONException 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response getLink(
            @Context UriInfo uri,
            @PathParam(value = "id") int id) throws JSONException {

        JSONObject result = new JSONObject();

        Link link = storeManagement.getLink(id);

        if (link == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        result.put("id", link.getLinkId());
        result.put("description", link.getDescription());
        result.put("url", link.getUrl());

        return Response.ok(Status.OK).entity(result.toString()).build();
    }

    /**
     * Method to set IStoreManagment instance to use.
     *
     * @param storeManagement the StoreManagement to set
     */
    public void setStoreManagement(IStoreManagment storeManagement) {
        this.storeManagement = storeManagement;
    }

}
