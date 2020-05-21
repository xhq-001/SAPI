package com.seaboxdata.controller.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seaboxdata.api.core.model.ResultInfo;
import com.seaboxdata.api.core.model.webservice.WsUrlInfo;
import com.seaboxdata.api.core.utils.WebServiceUrlUtil;
import com.seaboxdata.controller.IWebServiceController;
import com.seaboxdata.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

@WebService(serviceName = "DataWebService",
        targetNamespace = "http://controller.seaboxdata.com/",
        endpointInterface = "com.seaboxdata.controller.IWebServiceController")
@Component
public class WebServiceController implements IWebServiceController {
    @Resource
    WebServiceContext wsContext;
    @Autowired
    private DataService dataService;


    @Override
    public ResultInfo getDataXml(Integer pageNo,
                                 Integer pageSize,
                                 String search) {
        WsUrlInfo urlInfo = WebServiceUrlUtil.getUrlInfo(wsContext);
        ResultInfo data = dataService.getData(pageNo, pageSize, search, null,urlInfo.getResourceId(),urlInfo.getToken(),urlInfo.getRequest());
        return data;
    }


    @Override
    public String getDataJson(Integer pageNo,
                              Integer pageSize,
                              String search) {
        WsUrlInfo urlInfo = WebServiceUrlUtil.getUrlInfo(wsContext);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();
        ResultInfo data = dataService.getData(pageNo, pageSize, search, null,urlInfo.getResourceId(),urlInfo.getToken(),urlInfo.getRequest());
        return gson.toJson(data);
    }


    @Override
    public ResultInfo getDataXmlWithoutTitle(Integer pageNo,
                                             Integer pageSize,
                                             String search,
                                             String showColumns) {
        WsUrlInfo urlInfo = WebServiceUrlUtil.getUrlInfo(wsContext);
        ResultInfo data = dataService.getData(pageNo, pageSize, search, showColumns,urlInfo.getResourceId(),urlInfo.getToken(),urlInfo.getRequest());
        data.getData().setTitle(null);
        return data;
    }


    @Override
    public String getDataJsonWithoutTitle(Integer pageNo,
                                          Integer pageSize,
                                          String search,
                                          String showColumns) {
        WsUrlInfo urlInfo = WebServiceUrlUtil.getUrlInfo(wsContext);
        ResultInfo data = dataService.getData(pageNo, pageSize, search, showColumns,urlInfo.getResourceId(),urlInfo.getToken(),urlInfo.getRequest());
        data.getData().setTitle(null);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();
        return gson.toJson(data);
    }

    @Override
    public String getIsHasData(String search) {
        ResultInfo dataXml = getDataXml(1, 1, search);
        java.util.List result = dataXml.getData().getResult();
        Boolean res;
        if (result.size() > 0) {
            res = true;
        } else {
            res = false;
        }
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();
        return gson.toJson(res);
    }
}
