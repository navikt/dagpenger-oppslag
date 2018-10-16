package no.nav.dagpenger.oppslag;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.trust.STSClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class WsClient<T> {

    private EndpointSTSClientConfig endpointStsClientConfig;

    private UUIDCallIdGenerator generator;

    private static final String POLICY_PATH = "classpath:policy/";
    private static final String STS_CLIENT_AUTHENTICATION_POLICY = POLICY_PATH + "untPolicy.xml";

    private final String stsUrl;
    private final String serviceUser;
    private final String servicePwd;

    public WsClient(String stsUrl, String serviceUser, String servicePwd) {
        Bus bus = BusFactory.getThreadDefaultBus();
        this.stsUrl = stsUrl;
        this.serviceUser = serviceUser;
        this.servicePwd = servicePwd;
        this.endpointStsClientConfig = new EndpointSTSClientConfig(configureSTSClient(bus, true));

        generator = new UUIDCallIdGenerator("");
    }

    private STSClient configureSTSClient(Bus bus, boolean debug) {
        STSClient stsClient = new STSClient(bus);
        if(debug){
            stsClient.setFeatures(new ArrayList<Feature>(Arrays.asList(new LoggingFeature())));
        }
        stsClient.setEnableAppliesTo(false);
        stsClient.setAllowRenewing(false);
        stsClient.setLocation(stsUrl);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(org.apache.cxf.rt.security.SecurityConstants.USERNAME, serviceUser);
        properties.put(org.apache.cxf.rt.security.SecurityConstants.PASSWORD, servicePwd);

        stsClient.setProperties(properties);
        // used for the STS client to authenticate itself to the STS provider.
        stsClient.setPolicy(STS_CLIENT_AUTHENTICATION_POLICY);
        return stsClient;
    }

    public T createPortForSystemUser(String serviceUrl, Class<?> portType) {
        T port  = createAndConfigurePort(serviceUrl, portType);
        endpointStsClientConfig.configureRequestSamlToken(port);
        return port;
    }
    
    @SuppressWarnings("unchecked")
    private T createAndConfigurePort(String serviceUrl, Class<?> portType){
    	JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(portType);
        jaxWsProxyFactoryBean.setAddress(Objects.requireNonNull(serviceUrl));
        T port = (T) jaxWsProxyFactoryBean.create();
        Client client = ClientProxy.getClient(port);
        client.getOutInterceptors().add(new CallIdHeader(generator));
        return port;
    }
}
