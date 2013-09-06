package com.peergreen.store.api.rest.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.ejb.entity.Category;
import com.peergreen.store.db.client.ejb.entity.Group;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.ejb.entity.User;
import com.peergreen.store.db.client.ejb.entity.Vendor;

/**
 * Rest operations to collect entity collections from database.
 */
@Path("/")
public class GetOperations {

    private IStoreManagment storeManagement;
    private IPetalController petalController;

    /**
     * Retrieve all existing users in database.
     *
     * @param uri Identifier uri injected by jax-rs
     * @return A collection of all the user existing
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("users")
    public Response getUsers(@Context UriInfo uri) throws JSONException {
        Collection<User> users = storeManagement.collectUsers();

        ArrayList<JSONObject> usersList = new ArrayList<>();
        Iterator<User> userIt = users.iterator();
        while (userIt.hasNext()) {
            User u = userIt.next();

            JSONObject obj = new JSONObject();
            obj.put("name", u.getPseudo());
            obj.put("href", uri.getBaseUri().toString()
                    .concat("user/" + u.getPseudo()));
            usersList.add(obj);
        }

        JSONObject res = new JSONObject();
        res.put("groups", usersList);

        return Response.status(Status.OK).entity(res.toString()).build();
    }

    /**
     * Retrieve all existing groups in database.
     *
     * @param uri context uri injected by JAX-RS
     * @return all existing groups of users in database
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("groups")
    public Response getGroups(@Context UriInfo uri) throws JSONException {
        Collection<Group> groups = storeManagement.collectGroups();

        ArrayList<JSONObject> groupsList = new ArrayList<>();
        Iterator<Group> groupIt = groups.iterator();
        while (groupIt.hasNext()) {
            Group g = groupIt.next();

            JSONObject obj = new JSONObject();
            obj.put("name", g.getGroupname());
            obj.put("href", uri.getBaseUri().toString()
                    .concat("group/" + g.getGroupname()));
            groupsList.add(obj);
        }

        JSONObject res = new JSONObject();
        res.put("groups", groupsList);

        return Response.status(Status.OK).entity(res.toString()).build();
    }

    /**
     * Retrieve all existing vendors in database.
     *
     * @param uri context uri injected by JAX-RS
     * @return all existing vendors in database
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("vendors")
    public Response getVendors(@Context UriInfo uri) throws JSONException {
        Collection<Vendor> vendors = petalController.collectVendors();

        ArrayList<JSONObject> vendList = new ArrayList<>();
        Iterator<Vendor> vendIt = vendors.iterator();
        while (vendIt.hasNext()) {
            Vendor v = vendIt.next();

            JSONObject obj = new JSONObject();
            obj.put("name", v.getVendorName());
            obj.put("href", uri.getBaseUri().toString()
                    .concat("vendor/" + v.getVendorName()));
            vendList.add(obj);
        }

        JSONObject res = new JSONObject();
        res.put("vendors", vendList);

        return Response.status(Status.OK).entity(res.toString()).build();
    }

    /**
     * Retrieve all existing categories in database.
     *
     * @param uri context uri injected by JAX-RS
     * @return all existing categories in database
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("categories")
    public Response getCategories(@Context UriInfo uri) throws JSONException {
        Collection<Category> cats = storeManagement.collectCategories();

        ArrayList<JSONObject> catList = new ArrayList<>();
        Iterator<Category> catIt = cats.iterator();
        while (catIt.hasNext()) {
            Category c = catIt.next();

            JSONObject obj = new JSONObject();
            obj.put("name", c.getCategoryName());
            obj.put("href", uri.getBaseUri().toString()
                    .concat("category/" + c.getCategoryName()));
            catList.add(obj);
        }

        JSONObject res = new JSONObject();
        res.put("categories", catList);

        return Response.status(Status.OK).entity(res.toString()).build();
    }

    /**
     * Method to set IStoreManagement instance to use.
     * 
     * @param storeManagement the storeManagement to set
     */
    public void setStoreManagment(IStoreManagment storeManagment) {
        this.storeManagement = storeManagment;
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
     * Retrieve all the petals from the local repository.
     *
     * @return A collection of petals existing in the local repository
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/petals/local")
    public Response getLocalPetals(@Context UriInfo uri) throws JSONException {
        Collection<Petal> petals = storeManagement.collectPetalsFromLocal();
        Iterator<Petal> it = petals.iterator();

        List<JSONObject> petalsList = new ArrayList<>();
        while(it.hasNext()) {
            JSONObject jsonObject = new JSONObject();
            Petal p = it.next();
            jsonObject.put("id", p.getPid());
            jsonObject.put("vendorName", p.getVendor().getVendorName());
            jsonObject.put("artifactId", p.getArtifactId());
            jsonObject.put("version", p.getVersion());
            jsonObject.put("href" , uri.getBaseUri().toString()
                    .concat("petal/" + p.getPid() + "/metadata"));

            petalsList.add(jsonObject);
        }

        JSONObject res = new JSONObject();
        res.put("petals", petalsList);

        return Response.status(Status.OK).entity(res.toString()).build();
    }

    /**
     * Retrieve all the petals from the staging repository.
     *
     * @return A collection of petals existing in the staging repository
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/petals/staging")
    public Response getStagingPetals(@Context UriInfo uri) throws JSONException {
        Collection<Petal> petals = storeManagement.collectPetalsFromStaging();
        Iterator<Petal> it = petals.iterator();

        List<JSONObject> petalsList = new ArrayList<>();
        while(it.hasNext()) {
            JSONObject jsonObject = new JSONObject();
            Petal p = it.next();
            jsonObject.put("id", p.getPid());
            jsonObject.put("vendorName", p.getVendor().getVendorName());
            jsonObject.put("artifactId", p.getArtifactId());
            jsonObject.put("version", p.getVersion());
            jsonObject.put("href" , uri.getBaseUri().toString()
                    .concat("petal/" + p.getPid() + "/metadata"));

            petalsList.add(jsonObject);
        }

        JSONObject res = new JSONObject();
        res.put("petals", petalsList);

        return Response.status(Status.OK).entity(res.toString()).build();
    }

    /**
     * Retrieve all the petals from the remote repository.
     *
     * @return A collection of petals existing in the remote repository
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/petals/remote")
    public Response getRemotePetals(@Context UriInfo uri) throws JSONException {
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
            jsonObject.put("href" , uri.getBaseUri().toString()
                    .concat("petal/" + p.getPid() + "/metadata"));

            petalsList.add(jsonObject);
        }

        JSONObject res = new JSONObject();
        res.put("petals", petalsList);

        return Response.status(Status.OK).entity(res.toString()).build();
    }
    
}
