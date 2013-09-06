package com.peergreen.store.api.rest.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.controller.util.DependencyRequest;
import com.peergreen.store.controller.util.DependencyResult;
import com.peergreen.store.db.client.ejb.entity.Capability;
import com.peergreen.store.db.client.ejb.entity.Category;
import com.peergreen.store.db.client.ejb.entity.Group;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.ejb.entity.Requirement;
import com.peergreen.store.db.client.ejb.entity.Vendor;
import com.peergreen.store.db.client.enumeration.Origin;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;
import com.peergreen.store.db.client.exception.NoEntityFoundException;


@Path(value = "/petal")
public class PetalOperations {

    private static Log logger = LogFactory.getLog(PetalOperations.class);
    private String tmpPath;
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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/metadata")
    public Response getPetalMetadata (
            @Context UriInfo uri,
            @PathParam(value = "id") String id) throws JSONException {

        int intId = Integer.parseInt(id);
        Petal p = petalController.getPetalById(intId);
        if (p == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        try {
            Map<String, Object> mapResult = petalController.getPetalMetadata(
                    p.getVendor().getVendorName(),
                    p.getArtifactId(),
                    p.getVersion());

            JSONObject n = new JSONObject();
            n.put("vendor", mapResult.get("vendor"));
            n.put("artifactId", mapResult.get("artifactId"));
            n.put("version", mapResult.get("version"));
            n.put("description", mapResult.get("description"));

            // Category
            Category category = (Category)mapResult.get("category");
            String catName = category.getCategoryName();

            JSONObject cat = new JSONObject();
            cat.put("categoryName", catName);
            cat.put("href", uri.getBaseUri().toString()
                    .concat("category/"+catName));

            n.put("category", cat.toString());

            // Requirements
            if(mapResult.get("requirements") instanceof Collection<?>){
                @SuppressWarnings("unchecked")
                Collection<Requirement> reqs =
                (Collection<Requirement>) mapResult.get("requirements") ;

                ArrayList<JSONObject> reqList = new ArrayList<>();
                Iterator<Requirement> reqIt = reqs.iterator();
                while (reqIt.hasNext()) {
                    Requirement r = reqIt.next();

                    JSONObject obj = new JSONObject();
                    obj.put("id", r.getRequirementId());
                    obj.put("name", r.getRequirementName());
                    obj.put("filter", r.getFilter());
                    obj.put("namespace", r.getNamespace());
                    obj.put("href", uri.getBaseUri().toString()
                            .concat("requirement/" + r.getRequirementId()));
                    reqList.add(obj);
                }

                n.put("requirements", reqList);
            }

            // Capabilities
            if(mapResult.get("capabilities") instanceof Collection<?>){
                @SuppressWarnings("unchecked")
                Collection<Capability> capabilities =
                (Collection<Capability>) mapResult.get("capabilities") ;

                ArrayList<JSONObject> capList = new ArrayList<>();
                Iterator<Capability> capIt = capabilities.iterator();
                while (capIt.hasNext()) {
                    Capability c = capIt.next();

                    JSONObject obj = new JSONObject();
                    obj.put("id", c.getCapabilityId());
                    obj.put("name", c.getCapabilityName());
                    obj.put("namespace", c.getVersion());
                    obj.put("namespace", c.getNamespace());
                    obj.put("href", uri.getBaseUri().toString()
                            .concat("capability/"
                                    + c.getCapabilityName() + "/"
                                    + c.getVersion() + "/petals"));
                    capList.add(obj);
                }

                n.put("capabilities", capList);
            }

            // Origin
            Origin origin = (Origin) mapResult.get("origin");
            if (origin != null) {
                if (origin.equals(Origin.LOCAL)) {
                    n.put("Origin", "local");
                } else if (origin.equals(Origin.STAGING)) {
                    n.put("Origin", "staging");
                } else if (origin.equals(Origin.REMOTE)) {
                    n.put("Origin", "remote");
                }
            }

            // Groups
            if(mapResult.get("groups") instanceof Collection<?>){
                @SuppressWarnings("unchecked")
                Collection<Group> groups =
                (Collection<Group>) mapResult.get("groups") ;

                ArrayList<JSONObject> groupList = new ArrayList<>();
                Iterator<Group> groupIt = groups.iterator();
                while (groupIt.hasNext()) {
                    Group g = groupIt.next();

                    JSONObject obj = new JSONObject();
                    obj.put("name", g.getGroupname());
                    obj.put("href", uri.getBaseUri().toString()
                            .concat("group/" + g.getGroupname()));
                    groupList.add(obj);
                }

                n.put("groups", groupList);
            }

            return Response.ok(Status.OK).entity(n.toString()).build();
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).entity(
                    e.getMessage()).build();
        } 
    }

    /**
     * Add a petal to the local repository.
     */
    // TODO: write a client => dependency to Jersey?
    @POST
    @Path("/local")
    @Consumes("*/*")
    public Response uploadPetal (String payload, InputStream stream) {
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
     * @param id petal's id
     * @return HTTP status code 200 if the deletion has been made,
     * or 404 if not because the petal doesn't exist.
     */
    @DELETE
    @Path("/local/{id}")
    public Response deletePetal (
            @PathParam(value = "id") int id) {

        Petal petal = petalController.getPetalById(id);
        if (petal == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        if(petalController.
                removePetal(
                        petal.getVendor().getVendorName(),
                        petal.getArtifactId(),
                        petal.getVersion()) == null) {
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
    @GET
    @Path("/{id}/desc")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDescription ( 
            @PathParam(value = "id") int id) throws JSONException {

        Petal p = petalController.getPetalById(id);
        if (p == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        Map<String, Object> result ;
        JSONObject resultJson = new JSONObject(); 
        try {
            result = petalController.getPetalMetadata(
                    p.getVendor().getVendorName(),
                    p.getArtifactId(),
                    p.getVersion());

            if (result.size() == 0) {
                return Response.status(Status.NOT_FOUND).build();
            }

            resultJson.put("Description", result.get("description"));
            return Response.status(Status.OK)
                    .entity(resultJson.toString()).build();
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/local/{id}/category")
    public Response getCategoryLocal (@PathParam(value = "id") int id)
            throws JSONException {

        Petal p = petalController.getPetalById(id);
        if (p == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        Map<String, Object> result ;
        JSONObject resultJson = new JSONObject(); 
        try {
            result = petalController.getPetalMetadata(
                    p.getVendor().getVendorName(),
                    p.getArtifactId(),
                    p.getVersion());

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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/staging/{id}/category")
    public Response getCategoryStaging (@PathParam(value = "id") int id)
            throws JSONException {

        Petal p = petalController.getPetalById(id);
        if (p == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        Map<String, Object> result ;
        JSONObject resultJson = new JSONObject(); 
        try {
            result = petalController.getPetalMetadata(
                    p.getVendor().getVendorName(),
                    p.getArtifactId(),
                    p.getVersion());

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
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/local/{id}")
    public Response updatePetalLocal(
            String payload,
            @Context UriInfo uri,
            @PathParam(value = "id") int id) throws JSONException {

        Petal p = petalController.getPetalById(id);

        JSONObject jsonObject = new JSONObject(payload);
        String description = jsonObject.getString("description");
        String categoryName = jsonObject.getString("categoryName");

        try {
            p = petalController.updateDescription(
                    p.getVendor().getVendorName(),
                    p.getArtifactId(),
                    p.getVersion(),
                    description);

            p = petalController.setCategory(
                    p.getVendor().getVendorName(),
                    p.getArtifactId(),
                    p.getVersion(),
                    categoryName);

            JSONObject rep = new JSONObject();
            rep.put("id", p.getPid());
            rep.put("vendorName", p.getVendor().getVendorName());
            rep.put("artifactId", p.getArtifactId());
            rep.put("version", p.getVersion());
            rep.put("href", uri.getBaseUri().toString()
                    .concat("petal/" + p.getPid() + "/metadata"));

            return Response.status(Status.OK).entity(rep.toString()).build();
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * Method to update a petal (description and category)
     * in staging store.<br />
     * Return codes:
     * <ul>
     *      <li>200 if petal updated successfully</li>
     *      <li>404 if no corresponding petal found in database</li>
     * </ul>
     *
     * @param payload payload containing new description and category
     * @param id petal's id
     * @return updated petal
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/staging/{id}")
    public Response updatePetalStaging(
            String payload,
            @Context UriInfo uri,
            @PathParam(value = "id") int id) throws JSONException {

        Petal p = petalController.getPetalById(id);

        JSONObject jsonObject = new JSONObject(payload);
        String description = jsonObject.getString("description");
        String categoryName = jsonObject.getString("categoryName");

        try {
            p = petalController.updateDescription(
                    p.getVendor().getVendorName(),
                    p.getArtifactId(),
                    p.getVersion(),
                    description);

            p = petalController.setCategory(
                    p.getVendor().getVendorName(),
                    p.getArtifactId(),
                    p.getVersion(),
                    categoryName);

            JSONObject rep = new JSONObject();
            rep.put("id", p.getPid());
            rep.put("vendorName", p.getVendor().getVendorName());
            rep.put("artifactId", p.getArtifactId());
            rep.put("version", p.getVersion());
            rep.put("href", uri.getBaseUri().toString()
                    .concat("petal/" + p.getPid() + "/metadata"));

            return Response.status(Status.OK).entity(rep.toString()).build();
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
     * @param id petal's id
     * @return associated capabilities
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/capabilities")
    public Response getCapabilities(
            @Context UriInfo uri,
            @PathParam(value = "id") int id) throws JSONException {

        Petal p = petalController.getPetalById(id);

        if (p == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        // Capabilities
        JSONObject capJson = new JSONObject();
        if (p.getCapabilities() != null){
            Collection<Capability> capabilities = p.getCapabilities();

            ArrayList<JSONObject> capList = new ArrayList<>();
            Iterator<Capability> capIt = capabilities.iterator();
            while (capIt.hasNext()) {
                Capability c = capIt.next();

                JSONObject obj = new JSONObject();
                obj.put("id", c.getCapabilityId());
                obj.put("name", c.getCapabilityName());
                obj.put("namespace", c.getVersion());
                obj.put("namespace", c.getNamespace());
                obj.put("href", uri.getBaseUri().toString()
                        .concat("capabilities/" + c.getCapabilityId()));
                capList.add(obj);
            }

            capJson.put("capabilities", capList);
        }

        return Response.ok(Status.OK).entity(capJson.toString()).build();
    }

    /**
     * Method to retrieve all requirements needed by a petal.<br />
     * Return codes:
     * <ul>
     *      <li>200 if petal found in database</li>
     *      <li>404 if petal couldn't be found in database</li>
     * </ul>
     *
     * @param id petal's id
     * @return associated requirements
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/requirements")
    public Response getRequirements(
            @Context UriInfo uri,
            @PathParam(value = "id") int id) throws JSONException {

        Petal p = petalController.getPetalById(id);

        if (p == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        JSONObject rep = new JSONObject();

        List<JSONObject> reqList = new ArrayList<>();
        Collection<Requirement> reqs = p.getRequirements();
        Iterator<Requirement> itReq = reqs.iterator();
        while (itReq.hasNext()) {
            Requirement r = itReq.next();
            JSONObject curr = new JSONObject();
            curr.put("name", r.getRequirementName());
            curr.put("filter", r.getFilter());
            curr.put("namespace", r.getNamespace());
            curr.put("href", uri.getBaseUri().toString()
                    .concat("requirement/" + r.getRequirementName()));

            reqList.add(curr);
        }

        rep.put("requirements", reqList);

        return Response.ok(Status.OK).entity(rep.toString()).build();
    }

    /**
     * Method to retrieve all petals available for each required capability.
     * 
     * @param request DependencyRequest containing all constraints.
     * @return list of all petals available for each required capability
     * @throws NoEntityFoundException 
     * @see DependencyRequest
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/local/{id}/dependencies")
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
     * Method to retrieve a petal from the local store.<br />
     * Return codes:
     * <ul>
     *      <li>200 if petal found</li>
     *      <li>200 if petal not found</li>
     * </ul>
     *
     * @param id petal's id
     * @return {@link Response} response containing URL to the petal
     */
    @GET
    @Path("/local/{id}")
    public Response getPetalFromLocal(
            @PathParam(value = "id") int id) throws Exception {

        Petal p = petalController.getPetalById(id);

        if (p == null) {
            return Response.ok(Status.NOT_FOUND).build();
        }

        File petal = storeManagement.getPetalFromLocal(
                p.getVendor().getVendorName(),
                p.getArtifactId(),
                p.getVersion());

        ResponseBuilder response = Response.ok(petal);
        response.header("Content-Disposition",
                "attachment; filename=" + p.getArtifactId() + "."
                        + p.getVersion() + ".jar");
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPetals(@Context UriInfo uri) throws JSONException
    {
        Collection<Petal> petals = storeManagement.collectPetals();
        Iterator<Petal> it = petals.iterator();

        List<JSONObject> petalsList = new ArrayList<>();
        while(it.hasNext()) {
            JSONObject jsonObject = new JSONObject();
            Petal p = it.next();
            jsonObject.put("id", p.getPid());
            jsonObject.put("vendorName", p.getVendor().getVendorName());
            jsonObject.put("artifactId", p.getArtifactId());
            jsonObject.put("version", p.getVersion());
            jsonObject.put("href" , uri.getAbsolutePath().toString()
                    .concat("local/" + p.getPid()));

            petalsList.add(jsonObject);
        }

        JSONObject res = new JSONObject();
        res.put("petals", petalsList);

        return Response.status(Status.OK).entity(res.toString()).build();
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
     * @throws JSONException
     */
    @POST
    @Path("/submit")
    @Consumes("*/*")
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitPetal(
            @Context UriInfo uri,
            String payload,
            InputStream is
            ) throws JSONException {


        JSONObject jsonObject = new JSONObject(payload);
        String vendorName = jsonObject.getString("vendorName");
        String artifactId = jsonObject.getString("artifactId");
        String version = jsonObject.getString("version");
        String description = jsonObject.getString("description");
        String categoryName = jsonObject.getString("categoryName");
        // TODO: extract requirements from petal
        Set<Requirement> requirements = new HashSet<>();
        // TODO: extract capabilities from petal
        Set<Capability> capabilities = new HashSet<>();

        try {
            File stagingFile = new File(tmpPath + "/" + vendorName + ":"
                    + artifactId + ":" + version + ".jar");

            try (FileOutputStream fos = new FileOutputStream(stagingFile)) {
                BufferedInputStream bis = new BufferedInputStream(is);

                // Buffer size => 1kB
                byte[] buffer = new byte[1024];

                // Keep reading until there is no more content left.
                // -1 (EOF) is returned when end of file is reached.
                while ((bis.read(buffer)) != -1) {
                    fos.write(buffer);
                }
            } catch (FileNotFoundException e) {
                logger.error("Couldn't create temporary file." +
                        " Please check write permission for folder" +
                        " specified in config file.", e);
                return Response.ok(Status.INTERNAL_SERVER_ERROR).build();
            } catch (IOException e) {
                logger.error("Error during file storing on system.", e);
                return Response.ok(Status.INTERNAL_SERVER_ERROR).build();
            }

            Petal p = storeManagement.submitPetal(
                    vendorName,
                    artifactId,
                    version,
                    description,
                    categoryName,
                    requirements,
                    capabilities,
                    stagingFile
                    );

            JSONObject obj = new JSONObject();
            obj.put("id", p.getPid());
            obj.put("vendorName", p.getVendor().getVendorName());
            obj.put("artifactId", p.getArtifactId());
            obj.put("version", p.getVersion());
            obj.put("href" , uri.getAbsolutePath().toString()
                    .concat("staging/" + p.getPid()));

            return Response.ok(Status.CREATED)
                    .entity(obj.toString()).build();
        } catch (EntityAlreadyExistsException e) {
            logger.warn("A used entity already exist in database.", e);
        } catch (NoEntityFoundException e) {
            logger.warn("A used entity cannot be found in database.", e);
        }
        return null;
    }

    /**
     * Validate a petal for add it to the store.
     *
     * @param id petal's id
     * @return The validated petal 
     * @throws JSONException 
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/validate")
    public Response validatePetal(
            @Context UriInfo uri,
            @PathParam(value = "id") int id) throws JSONException {

        try {
            Petal p = petalController.getPetalById(id);

            p = storeManagement.validatePetal(
                    p.getVendor().getVendorName(),
                    p.getArtifactId(),
                    p.getVersion());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", p.getPid());
            jsonObject.put("vendorName", p.getVendor().getVendorName());
            jsonObject.put("artifactId", p.getArtifactId());
            jsonObject.put("version", p.getVersion());
            jsonObject.put("href" , uri.getAbsolutePath().toString()
                    .concat("local/" + p.getPid()));

            return Response.ok(Status.CREATED)
                    .entity(jsonObject.toString()).build();
        } catch (NoEntityFoundException e) {
            logger.warn("A used entity cannot be found in database.", e);
            return Response.ok(Status.NOT_FOUND).build();
        }
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
     * Method to retrieve temporary folder to store upload binaries.
     *
     * @return temporary folder
     */
    public String getTmpPath() {
        return tmpPath;
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
