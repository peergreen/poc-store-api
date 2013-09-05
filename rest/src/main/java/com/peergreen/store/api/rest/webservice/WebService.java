package com.peergreen.store.api.rest.webservice;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;

import com.peergreen.store.api.rest.resources.CapabilityOperations;
import com.peergreen.store.api.rest.resources.CategoryOperations;
import com.peergreen.store.api.rest.resources.GetOperations;
import com.peergreen.store.api.rest.resources.GroupOperations;
import com.peergreen.store.api.rest.resources.LinkOperations;
import com.peergreen.store.api.rest.resources.PetalOperations;
import com.peergreen.store.api.rest.resources.RequirementOperations;
import com.peergreen.store.api.rest.resources.UserOperations;
import com.peergreen.store.api.rest.resources.VendorOperations;
import com.peergreen.store.controller.IGroupController;
import com.peergreen.store.controller.IPetalController;
import com.peergreen.store.controller.IStoreManagment;
import com.peergreen.store.controller.IUserController;


@Component
@Provides(specifications=Application.class, properties=
@StaticServiceProperty(name="jonas.jaxrs.context-path",
type="java.lang.String",
value="/apistore"))
public class WebService extends Application {

    @Property
    private String tmpPath;
    private IStoreManagment storeManagment;
    private IUserController userController;
    private IGroupController groupController;
    private IPetalController petalController;

    public WebService() {

    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> s = new HashSet<Object>();
        CapabilityOperations capOp = new CapabilityOperations();
        CategoryOperations catOp = new CategoryOperations();
        GroupOperations groupOp = new GroupOperations();
        LinkOperations linkOp = new LinkOperations();
        PetalOperations petalOp = new PetalOperations();      
        RequirementOperations reqOp = new RequirementOperations();
        UserOperations userOp = new UserOperations();
        VendorOperations vendorOp = new VendorOperations();
        GetOperations getOp = new GetOperations();

        s.add(capOp);
        s.add(catOp);
        s.add(groupOp);
        s.add(linkOp);
        s.add(petalOp);
        s.add(reqOp);
        s.add(vendorOp);
        s.add(getOp);
        s.add(userOp);

        catOp.setStoreManagement(storeManagment);
        groupOp.setGroupController(groupController);
        linkOp.setStoreManagement(storeManagment);
        petalOp.setStoreManagement(storeManagment);
        petalOp.setPetalController(petalController);
        getOp.setStoreManagment(storeManagment);
        getOp.setPetalController(petalController);
        userOp.setUserController(userController);
        vendorOp.setPetalController(petalController);
        vendorOp.setStoreManagment(storeManagment);
        reqOp.setPetalController(petalController);
        capOp.setPetalController(petalController);
        return s;
    }

    @Bind
    public void bindStoreManagement (IStoreManagment sManagment){
        System.out.println("StoreManagement appears");
        storeManagment = sManagment;
    }

    @Bind
    public void bindUserController(IUserController uController){
        System.out.println("UserController appears");
        userController = uController;
    }

    @Bind
    public void bindGroupController(IGroupController gController){
        System.out.println("GroupController appears");
        groupController = gController;
    }

    @Bind
    public void bindPetalController(IPetalController pController){
        System.out.println("PetalController appears");
        petalController = pController;
    }

}
