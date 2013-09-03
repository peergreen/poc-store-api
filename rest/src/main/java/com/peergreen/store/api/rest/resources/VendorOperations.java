package com.peergreen.store.api.rest.resources;

import java.util.Collection;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.db.client.ejb.entity.Petal;
import com.peergreen.store.db.client.ejb.entity.Vendor;
import com.peergreen.store.db.client.exception.EntityAlreadyExistsException;
import com.peergreen.store.db.client.exception.NoEntityFoundException;

@Path(value="/vendors")
public class VendorOperations {

    private IStoreManagment storeManagment;

    private IPetalController petalController; 

    /**
     * @param petalController the petalController to set
     */
    public void setPetalController(IPetalController petalController) {
        this.petalController = petalController;
    }


    /**
     * @param storeManagment the storeManagment to set
     */
    public void setStoreManagment(IStoreManagment storeManagment) {
        this.storeManagment = storeManagment;
    }


    /**
     * Returns metadata associated with a vendor.
     * @param name The name of a vendor for which you want to retrieve 
     * the information
     * @return An JSON representation of the vendor with code 201
     * if the vendor exists, code 404 if it doesn't 
     * @throws JSONException 
     */
    @GET
    @Path("/{name}")
    public Response getVendor(@PathParam(value="name") String name) 
            throws JSONException {

        Vendor vendor = petalController.getVendor(name);
        if(vendor == null) {
            /* the vendor doesn't exist */
            return Response.status(Status.NOT_FOUND).build() ;
        }
        else{
            JSONObject result = new JSONObject(); 
            result.put("name", vendor.getVendorName());
            result.put("description", vendor.getVendorDescription());
            return Response.status(200).entity(result.toString()).build();
        }
    }

    /**
     * Creates a vendor.
     * @param payload payload with the vendor's name and vendor's description 
     * @return An JSON representation of the vendor with code 201
     * if the vendor doesn't exist, code 409 if it already exists
     * @throws JSONException
     */
    @POST
    public Response createVendor(String payload) throws JSONException {

        JSONObject vendorInfo; 
        vendorInfo = new JSONObject(payload);
        String name = vendorInfo.getString("Name");
        String description = vendorInfo.getString("Description");
        try {
            petalController.createVendor(name, description);
            return Response.status(201).entity(vendorInfo.toString()).build();
        } catch (EntityAlreadyExistsException e) {
            return Response.status(Status.CONFLICT).build();
        }
    }

    /**
     * Retrieve all the petals provided by a vendor s
     * @param name The vendor's name 
     * @return All the petals provided by a vendor 
     * @throws JSONException 
     */
    @GET
    @Path("/{name}/petals")
    public Response getPetals(@Context UriInfo uri  ,
            @PathParam("name") String name ) throws JSONException{

        JSONObject result = new JSONObject();
        String path = uri.getBaseUri().toString();

        try {
            Collection<Petal> petals = storeManagment.
                    collectPetalsByVendor(name);
            if(!petals.isEmpty()) {
                Iterator<Petal> it = petals.iterator();
                Petal p;
                while(it.hasNext()){
                    p = it.next();

                    result.put(p.getArtifactId(), path.concat("petal/" 
                            + p.getVendor() + "/" + p.getArtifactId()
                            + "/" + p.getVersion()));              
                }
                return Response.status(200).entity(result.toString()).build();
            } else{
                return Response.status(200).entity("No petals for " + name)
                        .build();
            }
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).build() ;
        }
    }

    @PUT
    @Path("/{name}")
    public Response updateVendor(@Context UriInfo uri, 
            @PathParam(value="name") String name, String payload)
                    throws JSONException {

        JSONObject jsonObject = new JSONObject(payload);
        String description = jsonObject.getString("description");
        try {
            petalController.updateVendor(name, description);
            return Response.status(201).entity(jsonObject.toString()).build();
        } catch (NoEntityFoundException e) {
            return Response.status(Status.NOT_FOUND).build() ;
        }
    }

}
