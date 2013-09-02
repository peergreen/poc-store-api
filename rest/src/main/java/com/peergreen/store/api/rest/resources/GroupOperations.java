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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IGroupController;
import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.ejb.entity.Group;
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

    @GET
    @Path("{name}")
    public Response getGroup(@Context UriInfo uri, @PathParam(value = "name") String name){

        JSONObject jsonObject = new JSONObject();
        String path = uri.getBaseUri().toString();

        JSONObject usersJson = new JSONObject();
        Collection<User> users;
        JSONObject petalsJson = new JSONObject();
        Collection<Petal> petals ;
        try {
            users = groupController.collectUsers(name);
            Iterator<User> iteratorUser = users.iterator();
            User u ; 
            while(iteratorUser.hasNext())
            {
                u = iteratorUser.next();
                usersJson.put(u.getPseudo(), path.concat("user/"+u.getPseudo()));
            }


            petals = groupController.collectPetals(name);
            Iterator<Petal> pIterator = petals.iterator();
            Petal p; 
            while(pIterator.hasNext())
            {
                p = pIterator.next();
                petalsJson.put(p.getArtifactId(), path.concat("petal/"+p.getArtifactId()));
            }

            jsonObject.put("users", usersJson.toString());
            jsonObject.put("petals", petalsJson.toString());
            return Response.status(200).entity(jsonObject.toString()).build();   
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        } catch (JSONException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        }
    }



    /**
     * Create a group of users  
     * @param name the name of the group to create
     * @return the group created
     */
    @POST
    public Response addGroup(String payload){   

        JSONObject jsonObject = null;
        String name = null; 
        try {
            jsonObject = new JSONObject(payload);
            name = jsonObject.getString("name");
            groupController.createGroup(name);
            return Response.status(201).entity("The group " + name + " was created successfully").build();
        } catch (JSONException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        } catch (EntityAlreadyExistsException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        } 
    }

    /**
     * Delete a group 
     * @param name the name of the group to delete 
     */
    @DELETE
    @Path("{name}")
    public Response deleteGroup(@PathParam("name") String name){  
        groupController.deleteGroup(name);  
        return Response.status(201).entity("The group " + name + " was deleted successfully").build();
    }

    /**
     * Add a user to an existing group 
     * @param groupName the name of the group to which add a new user 
     * @param pseudo the pseudo of the user to add to a group 
     * @return The group with the new user  
     */
    @PUT
    @Path("{name}/user/{pseudo}")
    public Response addUser(@Context UriInfo uri,@PathParam("name") String groupName, @PathParam("pseudo") String pseudo){

        JSONObject jsonObject = new JSONObject();
        String path = uri.getBaseUri().toString();

        JSONObject usersJson = new JSONObject();
        JSONObject petalsJson = new JSONObject();

        Collection<User> users;
        Collection<Petal> petals ;
        try {
            groupController.addUser(groupName, pseudo);
            users = groupController.collectUsers(groupName);
            Iterator<User> iteratorUser = users.iterator();
            User u ; 
            while(iteratorUser.hasNext())
            {
                u = iteratorUser.next();
                usersJson.put(u.getPseudo(), path.concat("user/"+u.getPseudo()));
            }

            petals = groupController.collectPetals(groupName);
            Iterator<Petal> pIterator = petals.iterator();
            Petal p; 
            while(pIterator.hasNext())
            {
                p = pIterator.next();
                petalsJson.put(p.getArtifactId(), path.concat("petal/"+p.getArtifactId()));
            }

            jsonObject.put("users", usersJson.toString());
            jsonObject.put("petals", petalsJson.toString());
            return Response.status(200).entity(jsonObject.toString()).build();   
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        } catch (JSONException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
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
    public Response removeUser(@Context UriInfo uri, @PathParam("group-name") String groupName, @PathParam("pseudo") String pseudo){

        JSONObject jsonObject = new JSONObject();
        String path = uri.getBaseUri().toString();

        JSONObject usersJson = new JSONObject();
        JSONObject petalsJson = new JSONObject();

        Collection<User> users;
        Collection<Petal> petals ;
        try {
            groupController.removeUser(groupName, pseudo);

            users = groupController.collectUsers(groupName);
            Iterator<User> iteratorUser = users.iterator();
            User u ; 
            while(iteratorUser.hasNext())
            {
                u = iteratorUser.next();
                usersJson.put(u.getPseudo(), path.concat("user/"+u.getPseudo()));
            }


            petals = groupController.collectPetals(groupName);
            Iterator<Petal> pIterator = petals.iterator();
            Petal p; 
            while(pIterator.hasNext())
            {
                p = pIterator.next();
                petalsJson.put(p.getArtifactId(), path.concat("petal/"+p.getArtifactId()));
            }


            jsonObject.put("users", usersJson.toString());
            jsonObject.put("petals", petalsJson.toString());

            return Response.status(200).entity(jsonObject.toString()).build();   
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        } catch (JSONException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        }
    }

    /**
     * Retrieve all the user of a group 
     * @param groupName the name of group to which collect its users 
     * @return A collection of users belongs to a group 
     */
    @GET
    @Path("{name}/users")
    public Response collectUsersofGroup(@Context UriInfo uri, @PathParam("group-name") String groupName){       
        JSONObject jsonObject = new JSONObject();
        String path = uri.getBaseUri().toString();

        Collection<User> users;
        try {

            users = groupController.collectUsers(groupName);
            Iterator<User> iteratorUser = users.iterator();
            User u ; 
            while(iteratorUser.hasNext())
            {
                u = iteratorUser.next();
                jsonObject.put(u.getPseudo(), path.concat("user/"+u.getPseudo()));
            }

            return Response.status(200).entity(jsonObject.toString()).build();   
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        } catch (JSONException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        }
    }

    @GET
    @Path("{name}/petals")
    public Response collectPetalsofGroup(@Context UriInfo uri, @PathParam("group-name") String groupName){       
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
