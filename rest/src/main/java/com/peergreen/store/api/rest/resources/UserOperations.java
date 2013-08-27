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
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.controller.IUserController;
import com.peergreen.store.db.client.ejb.entity.Group;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.ejb.entity.User;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;
import com.peergreen.store.db.client.exception.NoEntityFoundException;

@Path(value = "/user")
public class UserOperations {

    //TODO Comments 
    private IStoreManagment storeManagment;
    private IUserController userController;

    private static Logger theLogger =
            Logger.getLogger(UserOperations.class.getName());
    /**
     * @param storeManagment the storeManagment to set
     */
    public void setStoreManagment(IStoreManagment storeManagment) {
        this.storeManagment = storeManagment;
    }


    /**
     * @param userController the userController to set
     */
    public void setUserController(IUserController userController) {
        this.userController = userController;
    }


    /**
     * Retrieve all the user existing  
     * @return A collection of all the user existing 
     */
    @GET
    public Collection<User> getUsers(){
        return storeManagment.collectUsers();
    }

    /**
     * Retrieve data about the user  
     * @param pseudo the user's pseudo 
     * @return A map containing the user attribute and value 
     */
    @GET
    @Produces("application/json")
    @Path("{pseudo}")
    public Response showUser(@PathParam(value = "pseudo") String pseudo){
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
     */
    @POST
    @Produces("application/json")
    public Response addUser(@Context UriInfo uri,  String payload){

        JSONObject userInfo;
        try {
            userInfo = new JSONObject(payload);  
            String pseudo = userInfo.getString("pseudo");
            String password = userInfo.getString("password");
            String email = userInfo.getString("email");
            userInfo.put("pseudo", pseudo);
            userInfo.put("password", password);
            userInfo.put("email",email);
            userController.addUser(pseudo, password, email);
            return Response.status(201).entity(uri.getBaseUri().toString().concat("/"+pseudo)).build(); 
        } catch (JSONException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        }
        catch (EntityAlreadyExistsException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            //TODO Defines an HTTP status code to this case for all methods  
            return null;

        }
    }

    /**
     * Delete a user 
     * @param pseudo the pseudo of the user to delete 
     */
    @DELETE
    @Path("/{pseudo}")
    public Response removeUser(@PathParam("pseudo") String pseudo){
        userController.removeUser(pseudo);
        return Response.status(200).entity("The user " + pseudo + " was deleted sucessfully").build();
    }

    /**
     * Modify an existing user
     * @param pseudo the user's pseudo
     * @param payload the user's password and email
     * @return 
     */
    @PUT
    @Path("{pseudo}")
    @Produces("application/json")
    public Response updateUser(@PathParam("pseudo") String pseudo, String payload){
        JSONObject jsonObject;
        String password;
        String email;
        try {
            jsonObject = new JSONObject(payload);
            password = jsonObject.getString("password");
            email = jsonObject.getString("email");
            userController.updateUser(pseudo, password, email);
            return Response.status(201).entity(jsonObject.toString()).build();
        } catch (JSONException e1) {
            theLogger.log(Level.SEVERE,e1.getMessage());
            return null;
        }
        catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        }
    }


    /**
     * Retrieve all the groups to which a user belongs
     * @param pseudo the user's pseudo 
     * @return All the groups to which the user belongs 
     */
    @GET
    @Path("{pseudo}/groups")
    public Response getGroups(@Context UriInfo uri, @PathParam("pseudo") String pseudo){

        JSONObject jsonObject = new JSONObject();

        String path = uri.getBaseUri().toString();

        String groupName; 
        try {
            //Retrieve all the groups to which the user belongs 
            Collection<Group> groups = userController.collectGroups(pseudo);

            Iterator<Group> itG = groups.iterator();

            while(itG.hasNext()){
                Group group = itG.next();
                groupName = group.getGroupname();
                //Put in the JsonObject the name of each group and link to it
                jsonObject.put(groupName,path.concat("group/" + groupName));
            }

            return Response.status(200).entity(jsonObject.toString()).build(); 
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        }
    }

    /**
     * Retrieve all the petals to which a user has access
     * @param pseudo the user's pseudo 
     * @return All the petals to which the user has access 
     */
    @GET
    @Path("{pseudo}/petals")
    public Response getPetals(@Context UriInfo uri, @PathParam("pseudo") String pseudo){

        JSONObject jsonObject = new JSONObject();

        String path = uri.getBaseUri().toString();

        String artifactId; 
        try {
            //Retrieve all the petals to which the user have access
            Collection<Petal> petals = userController.collectPetals(pseudo);

            Iterator<Petal> itP = petals.iterator();

            while(itP.hasNext()){
                Petal petal = itP.next();
                artifactId = petal.getArtifactId();
                //Put in the JsonObject the name of each group and link to it
                jsonObject.put(artifactId,path.concat("petal/" + artifactId));
            }

            return Response.status(200).entity(jsonObject.toString()).build(); 
        } catch (NoEntityFoundException e) {
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            theLogger.log(Level.SEVERE,e.getMessage());
            return null;
        }
    }

}

