package com.peergreen.store.api.rest.resources;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IGroupController;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.ejb.entity.User;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;
import com.peergreen.store.db.client.exception.NoEntityFoundException;

@Path("/group")
public class GroupOperations {

    private IGroupController groupController;

    private static Logger theLogger =
            Logger.getLogger(GroupOperations.class.getName());


    /**
     * @param groupController the groupController to set
     */
    public void setGroupController(IGroupController groupController) {
        this.groupController = groupController;
    }

    /**
     * Method to retrieve a group thanks to its name.
     *
     * @param uri
     * @param name group's name
     * @return specified group, or {@literal null}
     * if no corresponding group found
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{name}")
    public Response getGroup(@Context UriInfo uri,
            @PathParam(value = "name") String name) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        String path = uri.getBaseUri().toString();

        JSONObject usersJson = new JSONObject();
        Collection<User> users;
        JSONObject petalsJson = new JSONObject();
        Collection<Petal> petals;
        try {
            users = groupController.collectUsers(name);
            Iterator<User> iteratorUser = users.iterator();
            User u; 
            while (iteratorUser.hasNext()) {
                u = iteratorUser.next();
                usersJson.
                put(u.getPseudo(), path.concat("user/" + u.getPseudo()));
            }


            petals = groupController.collectPetals(name);
            Iterator<Petal> pIterator = petals.iterator();
            Petal p;
            while (pIterator.hasNext()) {
                p = pIterator.next();
                petalsJson.put(p.getArtifactId(), path.concat("petal/"
                        + p.getVendor() + "/" + p.getArtifactId()
                        + "/" + p.getVersion()));
            }

            jsonObject.put("users", usersJson.toString());
            jsonObject.put("petals", petalsJson.toString());
            
            return Response.status(Status.OK).entity(jsonObject.get("users").
                    toString() + '\n'
                    + jsonObject.get("petals").toString()).build();
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE, e.getMessage());
            return Response.status(Status.NOT_FOUND).
                    entity("Group " + name + " doesn't exist.").build();
        }
    }


    /**
     * Create a group of users.
     *
     * @param name the name of the group to create
     * @return the group created
     * @throws JSONException 
     */
    @POST
    public Response createGroup(String payload) throws JSONException {   

        JSONObject jsonObject = null;
        String name = null;
        try {
            jsonObject = new JSONObject(payload);
            name = jsonObject.getString("name");
            groupController.createGroup(name);
            // TODO: return instance (with link?)
            return Response.status(201).entity("The group " 
                    + name + " was created successfully").build();
        } catch (EntityAlreadyExistsException e) {
            theLogger.log(Level.SEVERE, e.getMessage());
            return Response.status(Status.CONFLICT).build();
        }
    }

    /**
     * Delete a group.
     *
     * @param name the name of the group to delete
     */
    @DELETE
    @Path("{name}")
    public Response deleteGroup(@PathParam("name") String name) {
        if (groupController.deleteGroup(name)!= null) {
            return Response.status(200).entity("The group " + name
                    + " was deleted successfully").build();
        } else {
            return Response.status(404).
                    entity("Group " + name + " doesn't exist.").build();
        }
    }

    /**
     * Add a user to an existing group 
     * @param groupName the name of the group to which add a new user 
     * @param pseudo the pseudo of the user to add to a group 
     * @return The group with the new user  
     */
    @PUT
    @Path("{name}/user/{pseudo}")
    public Response addUser(@Context UriInfo uri,
            @PathParam("name") String groupName,
            @PathParam("pseudo") String pseudo) {

        String path = uri.getBaseUri().toString();

        try {
            groupController.addUser(groupName, pseudo);
            return Response.status(200).
                    entity("We have a new user in the group "
                            + path.concat(groupName)).build();
        } catch (NoEntityFoundException e1) {
            return Response.status(404).
                    entity("Group " + groupName + " doesn't exist.").build();
        }

    }

    /**
     * Remove a user from a group 
     * @param groupName the name of the group to which remove a user 
     * @param pseudo the pseudo of the user to remove from a group 
     * @return the group without the user removed 
     */
    @DELETE
    @Path("{name}/user/{pseudo}")
    public Response removeUser(@Context UriInfo uri,
            @PathParam("name") String groupName,
            @PathParam("pseudo") String pseudo) {

        String path = uri.getBaseUri().toString();

        try {
            groupController.removeUser(groupName, pseudo);
            return Response.status(200).entity("A user is less in the group " 
                    + path + "/" + groupName).build();
        } catch (NoEntityFoundException e1) {
            return Response.status(404).entity("Group "
                    + groupName + " doesn't exist.").build();
        }
    }

    /**
     * Retrieve all the user of a group 
     * @param groupName the name of group to which collect its users 
     * @return A collection of users belongs to a group 
     * @throws JSONException 
     */
    @GET
    @Path("{name}/users")
    public Response collectUsersofGroup(@Context UriInfo uri, 
            @PathParam("name") String groupName) throws JSONException {       
        JSONObject jsonObject = new JSONObject();
        String path = uri.getBaseUri().toString();

        Collection<User> users;
        try {

            users = groupController.collectUsers(groupName);
            Iterator<User> iteratorUser = users.iterator();
            User u; 
            while (iteratorUser.hasNext()) {
                u = iteratorUser.next();
                jsonObject.put(u.getPseudo(),
                        path.concat("user/" + u.getPseudo()));
            }
            return Response.status(200).entity("Users of group "
                    + groupName + " : " + jsonObject.toString()).build();
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE, e.getMessage());
            return Response.status(404).
                    entity("Group " + groupName + " doesn't exist.").build();
        }
    }

    @GET
    @Path("{name}/petals")
    public Response collectPetalsofGroup(@Context UriInfo uri,
            @PathParam("name") String groupName) throws JSONException {       
        JSONObject jsonObject = new JSONObject();
        String path = uri.getBaseUri().toString();

        Collection<Petal> petals;
        try {

            petals = groupController.collectPetals(groupName);
            Iterator<Petal> iteratorPetal = petals.iterator();
            Petal p;
            while (iteratorPetal.hasNext()) {
                p = iteratorPetal.next();
                jsonObject.put(p.getArtifactId(),
                        path.concat("petal/" + p.getVendor() + "/"
                                + p.getArtifactId() + "/" + p.getVersion()));
            }
            return Response.status(200).entity("Petals accessible for group "
                    + groupName + " : " + jsonObject.toString()).build();
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE, e.getMessage());
            return Response.status(404).
                    entity("Group " + groupName + " doesn't exist.").build();
        } 
    }
}
