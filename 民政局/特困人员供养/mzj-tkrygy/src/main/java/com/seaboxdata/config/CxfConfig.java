package com.seaboxdata.config;

import com.seaboxdata.constant.CustomConstant;
import com.seaboxdata.controller.IWebServiceController;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

/**
 * @author 姜雷
 */
@Configuration
@Slf4j
public class CxfConfig {

    @Autowired
    private Bus bus;

    @Autowired
    IWebServiceController webServiceController;


    /**
     * 将编写的webservice服务发布到端点，端点地址为根路径
     * 本地使用http://localhost:8803/services/?wsdl进行访问
     *
     * @return 端点
     */
    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, webServiceController);
        endpoint.publish("/"+ CustomConstant.serviceCode);
        log.info("共享接口WebService服务发布成功");
        return endpoint;
    }
}
