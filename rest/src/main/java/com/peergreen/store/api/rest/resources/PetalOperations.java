package com.peergreen.store.api.rest.resources;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.controller.util.DependencyRequest;
import com.peergreen.store.controller.util.DependencyResult;
import com.peergreen.store.db.client.ejb.entity.Capability;
import com.peergreen.store.db.client.ejb.entity.Category;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.ejb.entity.Requirement;
import com.peergreen.store.db.client.ejb.entity.Vendor;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;
import com.peergreen.store.db.client.exception.NoEntityFoundException;


@Path(value = "/petal")
public class PetalOperations {

    private IStoreManagment storeManagement;
    private IPetalController petalController;

    /**
     * Retrieves metadata associated to a petal.
     * 
     * @param vendor petal's vendor
     * @param artifactId petal's artifactId
     * @param version petal's version
     * @return corresponding metadata (code 200) or code 404
     * @throws JSONException 
     */
    @GET
    @Path("/{vendor}/{artifactId}/{version}/metadata")
    // TODO: change to id
    public Response getPetalMetadata (
            @Context UriInfo uri,
            @PathParam(value = "vendor") String name,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) throws JSONException {

        try {
            Map<String, Object> mapResult = petalController
                    .getPetalMetadata(name, artifactId, version);

            JSONObject n = new JSONObject();
            n.put("vendor", mapResult.get("vendor"));
            n.put("artifactId", mapResult.get("artifactId"));
            n.put("version", mapResult.get("version"));
            n.put("description", mapResult.get("description"));

            Category category = (Category)mapResult.get("category");
            String catName = category.getCategoryName();
            
            JSONObject cat = new JSONObject();
            cat.put("categoryName", catName);
            cat.put("href", uri.getBaseUri().toString()
                    .concat("category/"+catName));
            
            n.put("category", cat.toString());

            // TODO: change link to flat representation

            //            if(mapResult.get("requirements")
            //                    instanceof Collection<?>){
            //                @SuppressWarnings("unchecked")
            //                Collection<Requirement> reqs =
            //                        (Collection<Requirement>) mapResult.get("requirements") ;
            //                        JSONObject reqsJson = new JSONObject();
            //            Iterator<Requirement> reqIt = reqs.iterator();
            //            
            //            }

            System.out.println(n.toString());
            return Response.ok(Status.OK).entity(n.toString()).build();
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).entity(
                    e.getMessage()).build();
        } 
    }

    /**
     * Add a petal to the local repository.
     */
    @POST
    @Path("/local")
    @Consumes("*/*")
    public Response uploadPetal (String payload, InputStream stream ) {
        /*
         * payload:
        String vendorName
        String artifactId
        String version
        String description
        String categoryName
        Set<Requirement> Requirements
        Set<Capability> Capabilities
        int Origin
        InputStream File
         */

        return null;
    }

    /**
     * Delete a petal from the local store.
     *
     * @param vendorName name of the vendor which provide the petal to delete
     * @param artifactId artifactId of the petal to delete
     * @param version version of the petal to delete
     * @return HTTP status code 200 if the deletion has been made, or 404 if not
     * because the petal doesn't exist.
     */
    // TODO: change to id
    @DELETE
    @Path("/local/{vendorName}/{artifactId}/{version}")
    public Response deletePetal (
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) {

        if(petalController.
                removePetal(vendorName, artifactId, version) == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            return Response.status(Status.OK).build();
        }
    }

    /**
     * Get petal's description.
     *
     * @param vendorName name of the vendor which provide the petal
     * @param artifactId artifactId of the petal
     * @param version version of the petal
     * @return Http status code 200 if the description is retrieved,
     * or 404 if not because the petal doesn't exist.
     * @throws JSONException
     */
    // TODO: change to id
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

            if (result.size() == 0) {
                return Response.status(Status.NOT_FOUND).build();
            }

            resultJson.put("Description", result.get("description"));
            return Response.status(Status.OK).entity(resultJson.toString()).build();
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * Get local store petal's category.
     * @param vendorName name of the vendor which provide the petal
     * @param artifactId artifactId of the petal
     * @param version version of the petal
     * @return Http status code 200 if the category is retrieved,
     * or 404 if not because the petal doesn't exist.
     * @throws JSONException
     */
    // TODO: change to id
    @GET
    @Path("/local/{vendorName}/{artifactId}/{version}/category")
    public Response getCategoryLocal ( 
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) throws JSONException {

        Map<String, Object> result ;
        JSONObject resultJson = new JSONObject(); 
        try {
            result = petalController.
                    getPetalMetadata(vendorName, artifactId, version);

            if (result.size() == 0) {
                return Response.status(Status.NOT_FOUND).build();
            }

            resultJson.put("Category", ((Category)result.get("category")).
                    getCategoryName());
            return Response.status(Status.OK).entity(
                    resultJson.toString()).build();
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }

    }

    /**
     * Get staging store petal's category.
     *
     * @param vendorName name of the vendor which provide the petal
     * @param artifactId artifactId of the petal
     * @param version version of the petal
     * @return Http status code 200 if the category is retrieved,
     * or 404 if not because the petal doesn't exist.
     * @throws JSONException
     */
    // TODO: change to id
    @GET
    @Path("/staging/{vendorName}/{artifactId}/{version}/category")
    public Response getCategoryStaging ( 
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) throws JSONException {

        Map<String, Object> result ;
        JSONObject resultJson = new JSONObject(); 
        try {
            result = petalController.
                    getPetalMetadata(vendorName, artifactId, version);

            if (result.size() == 0) {
                return Response.status(Status.NOT_FOUND).build();
            }

            resultJson.put("Category", ((Category)result.get("category")).
                    getCategoryName());
            return Response.status(Status.OK).entity(
                    resultJson.toString()).build();
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }

    }

    /**
     * Method to update a petal (description and category) in local store.<br />
     * Return codes:
     * <ul>
     *      <li>200 if petal updated successfully</li>
     *      <li>404 if no corresponding petal found in database</li>
     * </ul>
     *
     * @param payload payload containing new description and category
     * @param vendorName petal's vendor name
     * @param artifactId petal's artifactId
     * @param version petal's version
     * @return updated petal
     * @throws JSONException
     */
    // TODO: change to id
    @PUT
    @Path("/local/{vendorName}/{artifactId}/{version}")
    public Response updatePetalLocal(
            String payload,
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version)
                    throws JSONException {

        JSONObject jsonObject = new JSONObject(payload);
        String description = jsonObject.getString("description");
        String categoryName = jsonObject.getString("categoryName");

        try {
            Petal p = petalController.updateDescription(
                    vendorName, artifactId, version, description);
            p = petalController.setCategory(
                    vendorName, artifactId, version, categoryName);
            return Response.status(Status.OK).entity(p).build();
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * Method to update a petal (description and category) in staging store.<br />
     * Return codes:
     * <ul>
     *      <li>200 if petal updated successfully</li>
     *      <li>404 if no corresponding petal found in database</li>
     * </ul>
     *
     * @param payload payload containing new description and category
     * @param vendorName petal's vendor name
     * @param artifactId petal's artifactId
     * @param version petal's version
     * @return updated petal
     * @throws JSONException
     */
    // TODO: change to id
    @PUT
    @Path("/staging/{vendorName}/{artifactId}/{version}")
    public Response updatePetalStaging(
            String payload,
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version)
                    throws JSONException {

        JSONObject jsonObject = new JSONObject(payload);
        String description = jsonObject.getString("description");
        String categoryName = jsonObject.getString("categoryName");

        try {
            Petal p = petalController.updateDescription(
                    vendorName, artifactId, version, description);
            p = petalController.setCategory(
                    vendorName, artifactId, version, categoryName);
            return Response.status(Status.OK).entity(p).build();
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * Method to retrieve all capabilities provided by a petal.<br />
     * Return codes:
     * <ul>
     *      <li>200 if petal found in database</li>
     *      <li>404 if petal couldn't be found in database</li>
     * </ul>
     *
     * @param vendorName petal's vendor name
     * @param artifactId petal's artifactId
     * @param version petal's version
     * @return associated capabilities
     */
    // TODO: change to id
    @GET
    @Path("/{vendorName}/{artifactId}/{version}/capabilities")
    public Response getCapabilities(
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) {

        Petal p = petalController.getPetal(vendorName, artifactId, version);

        if (p == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        return Response.ok(Status.OK).entity(p.getCapabilities()).build();
    }

    /**
     * Method to retrieve all requirements needed by a petal.<br />
     * Return codes:
     * <ul>
     *      <li>200 if petal found in database</li>
     *      <li>404 if petal couldn't be found in database</li>
     * </ul>
     *
     * @param vendorName petal's vendor name
     * @param artifactId petal's artifactId
     * @param version petal's version
     * @return associated requirements
     */
    // TODO: change to id
    @GET
    @Path("/{vendorName}/{artifactId}/{version}/requirements")
    public Response getRequirements(
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) {

        Petal p = petalController.getPetal(vendorName, artifactId, version);

        if (p == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        return Response.ok(Status.OK).entity(p.getRequirements()).build();
    }

    /**
     * Method to retrieve all petals available for each required capability.
     * 
     * @param request DependencyRequest containing all constraints.
     * @return list of all petals available for each required capability
     * @throws NoEntityFoundException 
     * @see DependencyRequest
     */
    public Response getTransitiveDependencies(
            @PathParam(value = "vendorName") String vendorName,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) {

        Petal p = petalController.getPetal(vendorName, artifactId, version);
        Vendor v = petalController.getVendor(vendorName);

        if (p == null || v == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        // build request
        DependencyRequest request = new DependencyRequest();
        request.setVendor(v);
        request.setArtifactId(artifactId);
        request.setVersion(version);

        DependencyResult result = null;
        try {
            result = petalController
                    .getTransitiveDependencies(request);
        } catch (NoEntityFoundException e) {
            // never reached cause of if (v == null) before
            e.printStackTrace();
        }

        return Response.ok(Status.OK).entity(result).build();
    }

    /**
     * Method to retrieve a petal from the local store.
     *
     * @param vendor petal's vendor
     * @param artifactId petal's artifactId
     * @param version petal's version
     * @return {@link Response} response containing URL to the petal
     */
    // TODO: change to id
    @GET
    @Path("/local/{vendor}/{artifactId}/{version}")
    public Response getPetalFromLocal(
            @PathParam(value = "vendor") String vendor,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) throws Exception {

        File petal = storeManagement.getPetalFromLocal(
                vendor, artifactId, version);
        ResponseBuilder response = Response.ok(petal);
        response.header("Content-Disposition",
                "attachment; filename="+artifactId+"."+version+".jar");
        return response.status(Status.OK).build();
    }

    /**
     * Method to retrieve a petal from the remote associated stores.
     *
     * @param vendor petal's vendor
     * @param artifactId petal's artifactId
     * @param version petal's version
     * @return {@link Response} response containing URL to the petal
     */
    // TODO: change to id
    @GET
    @Path("/remote/{vendor}/{artifactId}/{version}")
    public Response getPetalFromRemote(
            @PathParam(value = "vendor") String vendor,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version) throws Exception {

        File petal = storeManagement
                .getPetalFromRemote(vendor, artifactId, version);

        ResponseBuilder response = Response.ok(petal);
        response.header("Content-Disposition",
                "attachment; filename="+artifactId+"."+version+".jar");
        return response.status(Status.OK).build();
    }

    /**
     * Retrieve the list of all petals in local store.
     *
     * @param uri context
     * @return collection of existing local petals
     * @throws JSONException
     */
    @GET
    public Response getPetals(@Context UriInfo uri) throws JSONException
    {
        Collection<Petal> petals = storeManagement.collectPetals();
        /*
        Iterator<Petal> it = petals.iterator();

        JSONObject jsonObject = new JSONObject();
        while(it.hasNext()) {
            Petal p = it.next();
            String vendorName = p.getVendor().getVendorName();
            jsonObject.put(p.getArtifactId() , uri.getAbsolutePath().toString()
                    .concat("/" + vendorName + "/" +
                            p.getArtifactId() + "/" + p.getVersion()));

        }
         */
        return Response.status(Status.OK).entity(petals).build();
    }

    /**
     * Retrieve all the petals from the local repository.
     *
     * @return A collection of petals existing in the local repository
     */
    @GET
    @Path("/local")
    public Response getLocalPetals() {
        Collection<Petal> coll = storeManagement.collectPetalsFromLocal();
        return Response.ok(Status.OK).entity(coll.toString()).build();
    }

    /**
     * Retrieve all the petals from the staging repository.
     *
     * @return A collection of petals existing in the staging repository
     */
    @GET
    @Path("/staging")
    public Response getStagingPetals(){
        Collection<Petal> coll = storeManagement.collectPetalsFromRemote();
        return Response.ok(Status.OK).entity(coll.toString()).build();
    }

    /**
     * Retrieve all the petals from the remote repository.
     *
     * @return A collection of petals existing in the remote repository
     */
    @GET
    @Path("/remote")
    public Response getRemotePetals(){
        Collection<Petal> coll = storeManagement.collectPetalsFromRemote();
        return Response.ok(Status.OK).entity(coll.toString()).build();
    }



























    /**
     * Method to submit a petal to validate and add to the store.
     *
     * @param vendor petal vendor
     * @param artifactId petal artifactId
     * @param version petal version
     * @param description petal description
     * @param category petal category
     * @param requirements petal requirements
     * @param capabilities petal capabilities
     * @param petalBinary petal binary
     * @return petal instance created in database
     */
    @POST
    @Path(value = "/staging")
    //TODO add @Consumes directive for binary
    public Response submitPetal(
            String vendorName,
            String artifactId,
            String version,
            String description,
            Category category,
            Set<Requirement> requirements,
            Set<Capability> capabilities,
            File petalBinary) {

        try {
            Petal p = storeManagement.submitPetal(
                    vendorName,
                    artifactId,
                    version,
                    description,
                    category.getCategoryName(),
                    requirements,
                    capabilities,
                    petalBinary);

            return Response.ok(Status.CREATED).entity(p).build();
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
    @Path(value = "/staging/{id}")
    public Response validatePetal(
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version,
            @PathParam(value = "vendorName") String vendorName,
            @Context UriInfo uri) {
        try {
            Petal p = storeManagement.validatePetal(
                    vendorName, artifactId, version);
            return Response.ok(Status.OK).entity(p).build();
        } catch (NoEntityFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // TODO: test purpose
    @GET
    @Path(value = "/remote/{vendor}/{artifactId}/{version}/{url}")
    public File getPetalFromRemote(
            @PathParam(value = "vendor") String vendor,
            @PathParam(value = "artifactId") String artifactId,
            @PathParam(value = "version") String version,
            @PathParam(value = "url") String url) {

        File file = null;

        //        file = storeManagement.getPetalFromRemote(
        //                url,
        //                vendor,
        //                artifactId,
        //                version
        //        );

        return file;
    }

    /**
     * Method to set IPetalController instance to use.
     *
     * @param petalController the PetalController to set
     */
    public void setPetalController(IPetalController petalController) {
        this.petalController = petalController;
    }

    /**
     * Method to set IStoreManagment instance to use.
     *
     * @param storeManagement the StoreManagment to set
     */
    public void setStoreManagement(IStoreManagment storeManagement) {
        this.storeManagement = storeManagement;
    }
}
