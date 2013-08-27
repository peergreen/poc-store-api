package com.peergreen.store.api.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(value="/capabilities")
public class CapabilityOperations {

    /**
     * Retrieve petals which provide a given capability
     * @param id the capability's id 
     */
     @GET
    @Path("/{id}/petals")
    public Response getPetals(@Context UriInfo uri, @PathParam(value = "id") int id){ 
        //TODO method in the controller which retrieve a capability by his id 
        
        return null;
    }    
     
     
    @GET
    @Path("/{id}")
    public Response getCapability(){
        //TODO method in the controller which retrieve a capability by his id 
        return null; 
    }
    
     
    @GET
    public Response getCapabilities(){
        //TODO method in the controller to collect capabilities in the database 
        return null; 
    }
}