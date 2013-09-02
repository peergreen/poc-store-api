package com.peergreen.store.api.rest.resources;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IUserController;
import com.peergreen.store.db.client.ejb.entity.Group;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;
import com.peergreen.store.db.client.exception.NoEntityFoundException;

@Path(value = "/user")
public class UserOperations {

    private IUserController userController;

    private static Logger theLogger =
            Logger.getLogger(UserOperations.class.getName());

    /**
     * @param userController the userController to set
     */
    public void setUserController(IUserController userController) {
        this.userController = userController;
    }


    /**
     * Retrieve data about the user  
     * @param pseudo the user's pseudo 
     * @return A map containing the user attribute and value 
     */
    @GET
    @Produces("application/json")
    @Path("{pseudo}")
    public Response showUser(@PathParam(value = "pseudo") String pseudo) {
        Map<String, String> mapResult;
        try {
            mapResult = userController.getUserMetadata(pseudo);
            JSONObject n = new JSONObject(mapResult);
            return Response.status(200).entity(n.toString()).build();  
        } catch (NoEntityFoundException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }

    }

    /**
     * Creates a new user 
     * @param pseudo the user's pseudo 
     * @param password the user's password 
     * @param email the user's mail 
     * @return The user created 
     * @throws JSONException 
     */
    @POST
    @Produces("application/json")
    public Response createUser(@Context UriInfo uri,  String payload)
            throws JSONException {

        JSONObject userInfo;
        try {
            userInfo = new JSONObject(payload);
            String pseudo = userInfo.getString("pseudo");
            String password = userInfo.getString("password");
            String email = userInfo.getString("email");
            userController.addUser(pseudo, password, email);
            return Response.status(201).
                    entity(uri.getBaseUri().toString().
                            concat("user/" + pseudo)).build();
        } 
        catch (EntityAlreadyExistsException e) {
            theLogger.log(Level.SEVERE, e.getMessage());
            return Response.status(Status.CONFLICT).build();
        }
    }

    /**
     * Delete a user 
     * @param pseudo the pseudo of the user to delete 
     */
    @DELETE
    @Path("{pseudo}")
    public Response removeUser(@PathParam("pseudo") String pseudo){

        if (userController.removeUser(pseudo) == null) {
            return Response.status(200).
                    entity("The user " + pseudo + " was deleted sucessfully.").
                    build();
        } else {
            return Response.status(404).
                    entity("The user " + pseudo + " doesn't exist.").build();
        }
    }

    /**
     * Modify an existing user.
     * @param pseudo the user's pseudo
     * @param payload the user's password and email
     * @return the user whose data has been modified
     * @throws JSONException 
     */
    @PUT
    @Path("{pseudo}")
    @Produces("application/json")
    public Response updateUser(@PathParam("pseudo") String pseudo,
            String payload) throws JSONException {
        JSONObject jsonObject;
        String password;
        String email;
        try {
            jsonObject = new JSONObject(payload);
            password = jsonObject.getString("password");
            email = jsonObject.getString("email");
            userController.updateUser(pseudo, password, email);
            return Response.status(200).
                    entity("The user has been updated successfully :" + '\n'
                            + jsonObject.toString()).build();
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE, e.getMessage());
            return Response.status(404).
                    entity("The user " + pseudo + " doesn't exist.").build();
        }
    }


    /**
     * Retrieve all the groups to which a user belongs.
     * @param pseudo the user's pseudo
     * @return All the groups to which the user belongs
     * @throws JSONException
     */
    @GET
    @Path("{pseudo}/groups")
    public Response getGroups(@Context UriInfo uri,
            @PathParam("pseudo") String pseudo) throws JSONException {

        JSONObject jsonObject = new JSONObject();

        String path = uri.getBaseUri().toString();

        String groupName;
        try {
            //Retrieve all the groups to which the user belongs
            Collection<Group> groups = userController.collectGroups(pseudo);

            if (groups.isEmpty() == false) {
                Iterator<Group> itG = groups.iterator();
                while (itG.hasNext()) {
                    Group group = itG.next();
                    groupName = group.getGroupname();
                    //Put in the JsonObject
                    //the name of each group and link to it
                    jsonObject.put(groupName,
                            path.concat("group/" + groupName));
                }

                return Response.status(200).
                        entity(jsonObject.toString()).build(); }
            else {
                return Response.status(200).
                        entity("No groups for user " + pseudo ).build();
            }
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE, e.getMessage());
            return Response.status(404).
                    entity("The user " + pseudo + " doesn't exist.").build();

        }
    }

    /**
     * Retrieve all the petals to which a user has access
     * @param pseudo the user's pseudo
     * @return All the petals to which the user has access
     * @throws JSONException
     */
    @GET
    @Path("{pseudo}/petals")
    public Response getPetals(@Context UriInfo uri,
            @PathParam("pseudo") String pseudo) throws JSONException {

        JSONObject jsonObject = new JSONObject();

        String path = uri.getBaseUri().toString();
        try {

            //Retrieve all the petals to which the user have access
            Collection<Petal> petals = userController.collectPetals(pseudo);
            if (petals.isEmpty() == false) {
                Iterator<Petal> itP = petals.iterator();

                while (itP.hasNext()) {
                    Petal petal = itP.next();
                    //Put in the JsonObject the artifactId and link to it
                    jsonObject.put(petal.getArtifactId(), path.concat("petal/"
                            + petal.getVendor() + "/" + petal.getArtifactId()
                            + "/" + petal.getVersion()));
                }
                return Response.status(200).
                        entity(jsonObject.toString()).build();
            } else {
                return Response.status(200).
                        entity("No petals accessible for user " + pseudo).
                        build();
            }
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return Response.status(404).
                    entity("The user " + pseudo + " doesn't exist.").build();

        }
    }

}

