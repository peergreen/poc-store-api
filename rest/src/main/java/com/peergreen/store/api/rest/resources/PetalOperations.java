package com.peergreen.store.api.rest.resources;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.ejb.entity.Capability;
import com.peergreen.store.db.client.ejb.entity.Category;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.ejb.entity.Requirement;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;
import com.peergreen.store.db.client.exception.NoEntityFoundException;


@Path(value = "/petal")
public class PetalOperations {

    private IStoreManagment storeManagement;
    private IPetalController petalController;

    /**
     * @param petalController the petalController to set
     */
    public void setPetalController(IPetalController petalController) {
        this.petalController = petalController;
    }

    /**
     * @param storeManagement the storeManagement to set
     */
    public void setStoreManagement(IStoreManagment storeManagement) {
        this.storeManagement = storeManagement;
    }

    /**
     * Retrieves metadata associated to a petal 
     * @param vendor
     * @param artifactId
     * @param version
     * @return
     * @throws JSONException 
     */
    @GET
    @Path("/{vendor}/{artifactId}/{version}")
    public Response getPetalMetadata (@Context UriInfo uri, @PathParam(value = "vendor") String name,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) throws JSONException {

        Map<String, Object> mapResult;
        JSONObject n = new JSONObject();
        try {
            mapResult = petalController.getPetalMetadata(name, 
                    artifactId, version);

            n.put("vendor", mapResult.get("vendor"));
            n.put("artifactId", mapResult.get("artifactId"));
            n.put("version", mapResult.get("version"));
            n.put("description", mapResult.get("description"));

            Category category = (Category)mapResult.get("category");
            String catName = category.getCategoryName();
            n.put("category", uri.getBaseUri().toString()
                    .concat("category/"+catName));

            //            if(mapResult.get("requirements") 
            //                    instanceof Collection<?>){
            //                @SuppressWarnings("unchecked")
            //                Collection<Requirement> reqs = 
            //                        (Collection<Requirement>) mapResult.get("requirements") ;
            //                        JSONObject reqsJson = new JSONObject();
            //            Iterator<Requirement> reqIt = reqs.iterator();
            //            
            //            }



            return Response.status(200).entity(n.toString()).build();  
        } catch (NoEntityFoundException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } 


    }

    /**
     * Add a petal to the local repository.
     */
    @POST
    @Path("/local")
    @Consumes("*/*")
    public Response uploadPetal (String payload, InputStream stream ) {
        return null;
    }
    /**
     * Method to retrieve a petal from the local store.
     * 
     * @return {@link Response} response containing URL to the petal
     */
    @GET
    @Path("/local/{vendor}/{artifactId}/{version}")
    public Response getPetalFromLocal(@Context UriInfo uri,
            @PathParam(value = "vendor") String vendor,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) throws Exception {
        // retrieve petal with this id
        //    	storeManagement.
        //    	
        //    	if (/* want the pdf file */) {
        //	        return Response.ok(new File(/*...*/)).type("application/pdf").build(); 
        //	    }
        //
        //    	    /* default to xml file */
        //    	    return Response.ok(new FileInputStream("custom.xml")).type("application/xml").build();
        //    	}
        //    	
        //    	Response rep = Response.status()
        //    	Response.status(200).entity(jsonObject.toString()).build();

        File petal = storeManagement.getPetalFromLocal(vendor, artifactId, version);
        ResponseBuilder response = Response.ok(petal);
        response.header("Content-Disposition", "attachment; filename="+artifactId+"."+version+".jar");
        return response.build();
    }


    @PUT
    @Path("/{vendorName}/{artifactId}/{version}/desc")
    public Response updateDescription(String payload,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version,
            @PathParam(value = "vendorName") String vendorName,
            @Context UriInfo uri)
                    throws JSONException {

        JSONObject jsonObject = new JSONObject(payload);
        String path = uri.getBaseUri().toString();
        String description = jsonObject.getString("description");

        try {
            petalController.updateDescription(vendorName, artifactId, 
                    version, description);
            return Response.status(200).entity(path.
                    concat("/{vendorName}/{artifactId}/{version}")).build();
        } catch (NoEntityFoundException e) {
            return Response.status(404).build();
        }

    }
    /**
     * Retrieve all the petals existing 
     * @return A collection of petals existing 
     * @throws JSONException 
     */
    @GET
    public Response getPetals(@Context UriInfo uri) throws JSONException
    {
        Collection<Petal> petals = storeManagement.collectPetals();
        Iterator<Petal> it = petals.iterator();

        JSONObject jsonObject = new JSONObject();
        // int i = 1;
        while(it.hasNext()) {
            Petal p = it.next();
            String vendorName = p.getVendor().getVendorName();
            jsonObject.put(p.getArtifactId() , uri.getAbsolutePath().toString().concat("/" +
                    vendorName + "/" +
                    p.getArtifactId() + "/" + p.getVersion()));

        }
        return Response.status(200).entity(jsonObject).build();
    }

    /**
     * Retrieve all the petals from the local repository 
     * @return A collection of petals existing in the local repository 
     */
    @GET
    @Path("/local-repository")
    //TODO change return type to Response
    public Collection<Petal> getLocalPetals(){
        return storeManagement.collectPetalsFromLocal();
    }

    /**
     * Retrieve all the petals from the remote repository 
     * @return A collection of petals existing in the remote repository 
     */
    @GET
    @Path("/remote-repository")
    //TODO change return type to Response
    public Collection<Petal> getRemotePetals(){
        return storeManagement.collectPetalsFromRemote();
    }

    /**
     * Retrieve all the petals from the staging repository 
     * @return A collection of petals existing in the staging repository 
     */
    @GET
    @Path("/staging-repository")
    //TODO change return type to Response
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
    //TODO change return type to Response
    public Petal addPetalToStaging(String vendorName, String artifactId, String version,String description, Category category, Set<Requirement> requirements,
            Set<Capability> capabilities, File petalBinary){

        try {
            return storeManagement.submitPetal(vendorName, artifactId, version, description, category.getCategoryName(), requirements, capabilities, petalBinary);
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
    //TODO change return type to Response
    public Petal validatePetal(String vendorName, String artifactId, String version){
        try {
            return storeManagement.validatePetal(vendorName, artifactId, version);
        } catch (NoEntityFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete a petal from the local store.
     * @param vendorName name of the vendor which provide the petal to delete
     * @param artifactId artifactId of the petal to delete
     * @param version version of the petal to delete
     * @return Http status code 200 if the deletion was made, or 404 if it isn't
     * cause the petal doesn't exist.
     */
    @DELETE
    @Path("/local/{vendorName}/{artifactId}/{version}")
    public Response deletePetal (
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) {

        if(petalController.
                removePetal(vendorName, artifactId, version) == null) {
            return Response.status(404).build();
        } else {
            return Response.status(200).build();
        }
    }

    /**
     * Get petal's description.
     * @param vendorName name of the vendor which provide the petal
     * @param artifactId artifactId of the petal
     * @param version version of the petal
     * @return Http status code 200 if the description is retrieved,
     * or 404 if it isn't cause the petal doesn't exist.
     * @throws JSONException 
     */
    @GET
    @Path("/{vendorName}/{artifactId}/{version}/desc")
    public Response getDescription ( 
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) throws JSONException {

        Map<String, Object> result ;
        JSONObject resultJson = new JSONObject(); 
        try {
            result = petalController.
                    getPetalMetadata(vendorName, artifactId, version);
            resultJson.put("Description", result.get("description"));
            return Response.status(200).entity(resultJson.toString()).build();
        } catch (NoEntityFoundException e) {
            return Response.status(404).build();
        }

    }

    /**
     * Get petal's category.
     * @param vendorName name of the vendor which provide the petal
     * @param artifactId artifactId of the petal
     * @param version version of the petal
     * @return Http status code 200 if the category is retrieved,
     * or 404 if it isn't cause the petal doesn't exist.
     * @throws JSONException 
     */
    @GET
    @Path("/{vendorName}/{artifactId}/{version}/desc")
    public Response getCategory ( 
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) throws JSONException {

        Map<String, Object> result ;
        JSONObject resultJson = new JSONObject(); 
        try {
            result = petalController.
                    getPetalMetadata(vendorName, artifactId, version);
            resultJson.put("Category", ((Category)result.get("category")).
                    getCategoryName());
            return Response.status(200).entity(resultJson.toString()).build();
        } catch (NoEntityFoundException e) {
            return Response.status(404).build();
        }

    }

}
