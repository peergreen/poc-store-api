package com.peergreen.store.api.rest.resources;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.ejb.entity.Capability;
import com.peergreen.store.db.client.ejb.entity.Category;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.ejb.entity.Requirement;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;
import com.peergreen.store.db.client.exception.NoEntityFoundException;

@Path(value = "/petal")
public class PetalOperations {

    //TODO Identify if an id is useless for the petal
    private IStoreManagment storeManagement;    

//    private static Logger theLogger =
//            Logger.getLogger(GroupOperations.class.getName());
    
    /**
     * @param storeManagement the storeManagement to set
     */
    public void setStoreManagement(IStoreManagment storeManagement) {
        this.storeManagement = storeManagement;
    }

    /**
     * To retrieve a petal from the sore
     * 
     */
    
    @GET
    @Path("/id")
    public Response getPetalFromStore(){
        
        return null; 
        
    }
    /**
     * Retrieve all the petals existing 
     * @return A collection of petals existing 
     */
    @GET
    public Response getPetals()
    {
      //  Collection<Petal> petals = storeManagement.collectPetals();
        //Get all the petal and link to it using her id or artifactId
        return null;
    }

    /**
     * Retrieve all the petals from the local repository 
     * @return A collection of petals existing in the local repository 
     */
    @GET
    @Path("/local-repository")
    public Collection<Petal> getLocalPetals(){
        return storeManagement.collectPetalsFromLocal();
    }

    /**
     * Retrieve all the petals from the remote repository 
     * @return A collection of petals existing in the remote repository 
     */
    @GET
    @Path("/remote-repository")
    public Collection<Petal> getRemotePetals(){
        return storeManagement.collectPetalsFromRemote();
    }

    /**
     * Retrieve all the petals from the staging repository 
     * @return A collection of petals existing in the staging repository 
     */
    @GET
    @Path("/staging-repository")
    public Collection<Petal> getStagingPetals(){
        return storeManagement.collectPetalsFromStaging();
    }

    /**
     * Method to submit a petal to validate and add to the store 
     * @param vendor the vendor of the petal to submit 
     * @param artifactId the artifactId of the petal to submit
     * @param version the version of the petal to submit
     * @param description the description of the petal to submit
     * @param category the category of the petal to submit
     * @param requirements All the requirements of the petal to submit
     * @param capabilities All the capabilities of the petal to submit
     * @param petalBinary The file corresponding 
     * @return The petal to validate 
     */
    @POST
    @Path(value = "/staging-repository/add")
    public Petal addPetalToStaging(String vendorName, String artifactId, String version,String description, Category category, Set<Requirement> requirements,
            Set<Capability> capabilities, File petalBinary){
        
        try {
            return storeManagement.submitPetal(vendorName, artifactId, version, description, category, requirements, capabilities, petalBinary);
        } catch (EntityAlreadyExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoEntityFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Validate a petal for add it to the store
     * @param vendor The petal's vendor
     * @param artifactId The petal's artifactID
     * @param version The petal's version 
     * @return The validated petal 
     */
    @PUT
    @Path(value = "/staging-repository/{id}")
    public Petal validatePetal(String vendorName, String artifactId, String version){
        try {
            return storeManagement.validatePetal(vendorName, artifactId, version);
        } catch (NoEntityFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    
}
