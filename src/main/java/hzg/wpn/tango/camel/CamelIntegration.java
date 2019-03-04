package hzg.wpn.tango.camel;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hzg.wpn.xenv.ResourceManager;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.DeviceState;
import org.tango.server.InvocationContext;
import org.tango.server.ServerManager;
import org.tango.server.ServerManagerUtils;
import org.tango.server.annotation.*;
import org.tango.server.dynamic.DynamicManager;
import org.tango.server.dynamic.command.ProxyCommand;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 2/12/16
 */
@Device
public class CamelIntegration {
    private final Logger log = LoggerFactory.getLogger(CamelIntegration.class);

    @DeviceProperty(name = "TIK_TAK_SERVER")
    private String tikTakUri;

    @DynamicManagement
    private DynamicManager dynamicManager;

    public void setDynamicManager(DynamicManager dynamicManager) {
        this.dynamicManager = dynamicManager;
    }

    public void setTikTakUri(String tikTakUri) {
        this.tikTakUri = tikTakUri;
    }

    private CamelContext camelContext;

    @Init
    @StateMachine(endState = DeviceState.ON)
    public void init() throws Exception {

        SimpleRegistry registry = new SimpleRegistry();


        camelContext = new DefaultCamelContext(registry);


        RoutesDefinition routeDefinition = camelContext.loadRoutesDefinition(
                ResourceManager.loadResource("etc/CamelIntegration","routes.xml"));

        //TODO set default error handler

        List<RouteDefinition> routes = routeDefinition.getRoutes();
        camelContext.addRouteDefinitions(routes);
    }

    @Attribute
    public String[] getRouteDefinitions(){
        return
                camelContext.getRouteDefinitions().stream().map(RouteDefinition::getId).toArray(String[]::new);
    }

    @Attribute
    public String[] getRoutes(){
        return
                camelContext.getRoutes().stream().map(Route::getId).toArray(String[]::new);
    }

    @AroundInvoke
    public void aroundInvoke(InvocationContext ctx){
        System.out.println(ctx);
    }

    @Command
    @StateMachine(endState = DeviceState.RUNNING)
    public void start() throws Exception {
        camelContext.start();
    }

    @Command
    @StateMachine(endState = DeviceState.ON)
    public void stop() throws Exception {
        camelContext.stop();
    }

    @Delete
    public void delete() throws Exception {
        stop();
    }

    public static void main(String[] args) throws IOException {
        ServerManager.getInstance().start(args, CamelIntegration.class);
        ServerManagerUtils.writePidFile(null);
    }
}
