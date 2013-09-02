package com.peergreen.store.api.rest.resources;

import java.util.Collection;
import java.util.Iterator;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.ejb.entity.Group;
import com.peergreen.store.db.client.ejb.entity.User;
import com.peergreen.store.db.client.ejb.entity.Vendor;

/**
 * Rest operations to collect entities from database.
 */
@Path("/")
public class GetOperations {

    /**
     * StoreManagement to use methods to collect entities.
     */
    private IStoreManagment storeManagment;

    private IPetalController petalController;
    /**
     * @param storeManagment the storeManagment to set
     */
    public void setStoreManagment(IStoreManagment storeManagment) {
        this.storeManagment = storeManagment;
    }

    public void setPetalController(IPetalController petalController) {
        this.petalController = petalController;
    }

    /**
     * Retrieve all the user existing.
     * @param uri Identifier uri injected by jax-rs
     * @return A collection of all the user existing
     * @throws JSONException
     */
    @GET
    @Path("users")
    public Response getUsers(@Context UriInfo uri) throws JSONException {
        String path = uri.getBaseUri().toString();
        JSONObject usersJson = new JSONObject();
        Collection<User> users = storeManagment.collectUsers();
        Iterator<User> iteratorUser = users.iterator();
        User u;
        while (iteratorUser.hasNext()) {
            u = iteratorUser.next();
            if (u.getPseudo() != "Administrator") {
                usersJson.put(u.getPseudo(),
                        path.concat("user/" + u.getPseudo()));
            }
        }
        return Response.status(200).entity(usersJson.toString()).build();
    }

    /**
     * Retrieve all the groups existing.
     * @return A collection of all the group of users existing
     * @throws JSONException
     */
    @GET
    @Path("groups")
    public Response getGroups(@Context UriInfo uri) throws JSONException{
        JSONObject jsonObject = new JSONObject();
        String path = uri.getAbsolutePath().toString();

        Collection<Group> groups = storeManagment.collectGroups();
        Group group;
        Iterator<Group> gIterator = groups.iterator();
        while (gIterator.hasNext()) {
            group = gIterator.next();

            jsonObject.put(group.getGroupname(),
                    path.concat("/" + group.getGroupname()));
        }
        return Response.status(200).entity(jsonObject.toString()).build();
    }

    /**
     * Retrieve all the vendors existing.
     * @return All existing the vendors
     * @throws JSONException
     */
    @GET
    public Response getVendors(@Context UriInfo uri) throws JSONException{
        JSONObject jsonObject = new JSONObject();
        String path = uri.getAbsolutePath().toString();
        Collection<Vendor> vendors = petalController.collectVendors();
        Iterator<Vendor> iterator = vendors.iterator();
        Vendor vendor;
        while (iterator.hasNext()) {
            vendor = iterator.next();
            jsonObject.put(vendor.getVendorName(),
                    path.concat("/" + vendor.getVendorName()));
        }
        return Response.status(200).entity(jsonObject.toString()).build();
    }
}
