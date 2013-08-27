package com.peergreen.store.api.rest.resources;

import java.util.Collection;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.ejb.entity.Vendor;

@Path(value="/vendors")
public class VendorOperations {

    private IPetalController petalController;
    
    /**
     * @param petalController the petalController to set
     */
    public void setPetalController(IPetalController petalController) {
        this.petalController = petalController;
    }

    /**
     * retrieve a vendor metadata
     */
    @GET
    @Path("/{name}")
    public Response getVendor(@Context UriInfo uri, @PathParam(value="name") String name){
        //TO DO method on the controller to get metadata of a vendor
        return null;
    }
    /**
     * Retrieve all the vendors existing 
     * @return All existing the vendors  
     */
    @GET
    public Response getVendors(@Context UriInfo uri){
        JSONObject jsonObject = new JSONObject();
        String path = uri.getAbsolutePath().toString();
         Collection<Vendor> vendors = petalController.collectVendors();
         Iterator<Vendor> iterator = vendors.iterator();
         Vendor vendor ; 
         while(iterator.hasNext()){
             vendor = iterator.next();
             try {
                jsonObject.put(vendor.getVendorName(), path.concat("/"+vendor.getVendorName()));
                
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
         }
         return Response.status(200).entity(jsonObject.toString()).build();
    }

    /**
     * Retrieve all the petals provided by a vendor s
     * @param name The vendor's name 
     * @return All the petals provided by a vendor 
     */
    @GET
    @Path("/{name}/petals")
    public Collection<Petal> getPetals(@PathParam("name") String name ){
        //TODO method in the controller  collectPetalsByVendor
        return null;  
    }
    
    @PUT
    @Path("/{name}")
    public Response updateVendor(@Context UriInfo uri, @PathParam(value="name") String name, String payload){
        
        try {
            JSONObject jsonObject = new JSONObject(payload);
            //String description = jsonObject.getString("description");
            //TODO method in controller to update a vendor 
           
        } catch (JSONException e) {
            // TODO Auto-generated catch block
       
        }
        
        
        return null;
    }

    
    
    
    
    
    
    
    
    
    }
