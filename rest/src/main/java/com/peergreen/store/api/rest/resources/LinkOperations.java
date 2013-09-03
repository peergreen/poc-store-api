package com.peergreen.store.api.rest.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;

@Path(value = "/links")
public class LinkOperations {

    private IStoreManagment storeManagement; 

    private static Logger theLogger =
            Logger.getLogger(UserOperations.class.getName());

    /**
     * Create a new link between store
     * @param url the link's url 
     * @param description the link's description
     * @return the link created 
     */
    @POST
    public Response addLink(String payload){
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(payload); 
            String url = jsonObject.getString("url");
            String description = jsonObject.getString("description");
            storeManagement.addLink(url, description);
            return Response.status(Status.CREATED).entity(
                    "The link with thir url : " + url +
                    " was created successfully").build();

        } catch (JSONException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        } catch (EntityAlreadyExistsException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteLink(@PathParam(value="id") int id){
        //TODO how to get a link with his id 
        //storeManagement.removeLink(linkUrl);
        return null;
    }

    @PUT
    @Path("/{id}")
    public Response updateLink(@PathParam(value="id") int id, String payload){
        //        try {
        //            JSONObject jsonObject = new JSONObject(payload);
        //            String newDescription = jsonObject.getString("description");
        //        } catch (JSONException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        
        //TODO how to get a link with his id 
        //storeManagement.removeLink(linkUrl);

        return null;
    }

    /**
     * Retrieve all the links existing
     * @return A collection of links 
     */
    @GET
    public Response getLinks(@Context UriInfo uri){   
        //        JSONObject jsonObject = new JSONObject();
        //        
        //        Collection<Link> links = storeManagement.collectLinks();
        //        Iterator<Link> iterator = links.iterator();
        //      //  Link Link ; 
        //        while(iterator.hasNext()){
        //            Link = iterator.next();
        ////            try {
        ////                jsonObject.put(Link.getLinkName(),uri.getAbsolutePath().toString().concat("/" + Link.getLinkName()));
        ////            } catch (JSONException e) {
        ////                // TODO Auto-generated catch block
        ////                e.printStackTrace();
        ////            }
        //        }
        //        return Response.status(Status.OK).entity(jsonObject.toString()).build();
        return null; 
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
