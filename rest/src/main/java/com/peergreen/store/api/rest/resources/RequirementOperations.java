package com.peergreen.store.api.rest.resources;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.peergreen.store.db.client.ejb.entity.Petal;

@Path(value="/requirements")
public class RequirementOperations {
/**
 * Retrieve all petals satisfying a requirement 
 * @param id the requirement's id 
 * @return All the petals existing which satisfy the given requirement 
 */
    @GET
    @Path("/{id}/petals")
    public Collection<Petal> getPetals(@Context UriInfo uri, @PathParam(value = "id") int id){ 
        // TODO method in the controller a requirement given his id 
        return null;
    }    
    
    @GET
    @Path("/{id}")
    public Response getRequirement(){
    
        // TODO method in the controller a requirement given his id 
        return null; 
    }
    
    @GET
    public Response getRequirements(){
        
        //TODO method in the controller to collect requirements in the database 
        return null; 
    }
}
