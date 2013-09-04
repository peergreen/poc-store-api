package com.peergreen.store.api.rest.resources;

import java.util.Collection;
import java.util.Iterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.ejb.entity.Category;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;



@Path("/categories")
@Produces("application/json")  
@Consumes("application/json") 
public class CategoryOperations {

    private IStoreManagment storeManagement;

    /**
     * Retrieve information about a category.
     *
     * @param uri
     * @param name category name
     * @return corresponding category (code 200) or code 404
     * @throws JSONException 
     */
    @GET
    @Path("{name}")
    public Response showCategory(
            @Context UriInfo uri,
            @PathParam("name") String name) throws JSONException {

        Category c = storeManagement.getCategory(name);
        
        JSONObject obj = new JSONObject();
        
        obj.put("name", c.getCategoryName());
        obj.put("href", uri.getBaseUri().toString()
                .concat("categories/" + c.getCategoryName()));
        
        return Response.ok(Status.OK).entity(obj.toString()).build(); 
    }

    /**
     * Retrieve petals of a category.
     *
     * @param name A category's name
     * @return A collection of petals which belongs to the category
     */
    @GET
    @Path("{name}/petals")
    public Response getPetals(@PathParam("name") String name){

        //TODO method in the controller to retrieve petals by category 
        return null; 
    }

    /**
     * Create a category.
     *
     * @param name name of the category to create
     * @return the category created
     */
    @POST
    public Response addCategory(@Context UriInfo uri,  String payload){
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(payload);
            String name = jsonObject.getString("name");
            storeManagement.createCategory(name);
            return Response.status(Status.CREATED).entity(
                    uri.getBaseUri().toString().concat("/"+name)).build();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (EntityAlreadyExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null; 
        }
    }

    /**
     * Retrieve all the petal's category existing.
     *
     * @return A collection of petals's category existing
     */
    @GET
    public Response getCategories(@Context UriInfo uri){   
        JSONObject jsonObject = new JSONObject();

        Collection<Category> categories = storeManagement.collectCategories();
        Iterator<Category> iterator = categories.iterator();
        Category category ; 
        while(iterator.hasNext()){
            category = iterator.next();
            try {
                jsonObject.put(
                        category.getCategoryName(),
                        uri.getAbsolutePath().toString()
                        .concat("/" + category.getCategoryName()));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return Response.status(Status.OK).entity(jsonObject.toString()).build();
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
