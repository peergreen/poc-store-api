package com.peergreen.store.api.rest.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.json.JSONException;
import org.json.JSONObject;


@Component
@Instantiate
@Provides
public class StoreClient {

    private String url = "http://localhost:9000/apistore/user";

    @Validate
    public void main() {
        System.out.println("Running");
        Client client = ClientBuilder.newClient();

        JSONObject payload = new JSONObject();
        try {
            payload.put("pseudo","toto");
            payload.put("password","pwd");
            payload.put("email","toto@peergreen.com");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Response response = client.target(url).request("application/json").post(Entity.text(payload.toString()));
        
        //We can retrieve information result from the response
        response.getEntity(); 

        System.out.println("Running done !");
    }



}
