package com.healt.cloud.checkup.service.ws.his;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 3.1.6
 * 2022-06-29T14:47:19.792+08:00
 * Generated source version: 3.1.6
 * 
 */
@WebServiceClient(name = "HipWebServicesService", 
                  wsdlLocation = "file:/D:/ideaIU-2021.1.1.win/workspace/healt-cloud/healt-cloud-checkup-api/src/main/resources/soap.xml",
                  targetNamespace = "http://wegohis.com") 
public class HipWebServicesService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://wegohis.com", "HipWebServicesService");
    public final static QName HipWebServicesPort = new QName("http://wegohis.com", "hipWebServicesPort");
    static {
        URL url = null;
        try {
            url = new URL("file:/D:/ideaIU-2021.1.1.win/workspace/healt-cloud/healt-cloud-checkup-api/src/main/resources/soap.xml");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(HipWebServicesService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "file:/D:/ideaIU-2021.1.1.win/workspace/healt-cloud/healt-cloud-checkup-api/src/main/resources/soap.xml");
        }
        WSDL_LOCATION = url;
    }

    public HipWebServicesService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public HipWebServicesService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public HipWebServicesService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public HipWebServicesService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    public HipWebServicesService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public HipWebServicesService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }    




    /**
     *
     * @return
     *     returns HipWebServices
     */
    @WebEndpoint(name = "hipWebServicesPort")
    public HipWebServices getHipWebServicesPort() {
        return super.getPort(HipWebServicesPort, HipWebServices.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns HipWebServices
     */
    @WebEndpoint(name = "hipWebServicesPort")
    public HipWebServices getHipWebServicesPort(WebServiceFeature... features) {
        return super.getPort(HipWebServicesPort, HipWebServices.class, features);
    }

}
