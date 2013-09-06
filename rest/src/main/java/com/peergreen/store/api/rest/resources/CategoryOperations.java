package com.peergreen.store.api.rest.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.felix.ipojo.annotations.Requires;
import org.json.JSONException;
import org.json.JSONObject;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.ejb.entity.Category;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;
import com.peergreen.store.db.client.exception.NoEntityFoundException;


@Path("/category")
public class CategoryOperations {

    private static Log logger = LogFactory.getLog(PetalOperations.class);
    @Requires
    private IStoreManagment storeManagement;

    /**
     * Method to create a category.<br />
     * HTTP codes returned:
     * <ul>
     *      <li>201 if category created without problem</li>
     *      <li>409 if category of same name already exist</li>
     * </ul>
     *
     * @param uri contextual URI
     * @param payload payload of request
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)  
    public Response createCategory(@Context UriInfo uri, String payload) {
        JSONObject jsonObject;
        String name = null;
        try {
            jsonObject = new JSONObject(payload);
            name = jsonObject.getString("name");

            Category category = storeManagement.createCategory(name);

            JSONObject obj = new JSONObject();
            obj.put("categoryName", category.getCategoryName());
            obj.put("href", uri.getAbsolutePath()
                    .toString().concat("name"));

            return Response.ok(Status.CREATED)
                    .entity(obj.toString()).build();
        } catch (EntityAlreadyExistsException e) {
            logger.warn("Category with name "
                    + name + "already exist in database.", e);
            return Response.ok(Status.CONFLICT).build();
        }  catch (JSONException e) {
            logger.error("Error during parsing received data.", e);
            return Response.ok(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Method to retrieve information about a category.<br />
     * HTTP codes returned:
     * <ul>
     *      <li>200 if category retrieved without problem</li>
     *      <li>404 if category cannot be found in database</li>
     * </ul>
     * @param uri contextual URI
     * @param name category name
     * @return corresponding category
     * @throws JSONException 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{name}")
    public Response showCategory(
            @Context UriInfo uri,
            @PathParam("name") String name) throws JSONException {

        Category c = storeManagement.getCategory(name);

        JSONObject obj = new JSONObject();

        obj.put("name", c.getCategoryName());
        obj.put("href", uri.getAbsolutePath().toString()
                .concat(c.getCategoryName()));

        return Response.ok(Status.OK).entity(obj.toString()).build(); 
    }

    /**
     * Retrieve petals of a category.<br/>
     * HTTP codes returned:
     * <ul>
     *      <li>200 if category found</li>
     *      <lI>404 if category cannot be found</li>
     * </ul>
     *
     * @param uri contextual URI
     * @param name A category's name
     * @return A collection of petals which belongs to the category
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{name}/petals")
    public Response getPetals(
            @Context UriInfo uri,
            @PathParam("name") String name) throws JSONException {

        Collection<Petal> petals = null;
        try {
            petals = storeManagement.getPetalsForCategory(name);
        } catch (NoEntityFoundException e) {
            logger.warn("Category with name " + name + " cannot be found.", e);
            return Response.ok(Status.NOT_FOUND).build();
        }
        
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
        
        JSONObject res = new JSONObject();
        res.put("petals", petalsList);
        
        return Response.status(Status.OK).entity(res.toString()).build();
    }

    /**
     * Delete a category.
     *
     * @param name the name of the category to delete
     */
    @DELETE
    @Path("{name}")
    public Response delete(@PathParam("name") String name){
        storeManagement.removeCategory(name);
        return Response.status(Status.CREATED).entity(
                "The category " + name + " was deleted sucessfully").build();
    }

    /**
     * Method to set IStoreManagement instance to use.
     *
     * @param storeManagement the storeManagement to set
     */
    public void setStoreManagement(IStoreManagment storeManagement) {
        this.storeManagement = storeManagement;
    }

}
