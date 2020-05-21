package com.seaboxdata.controller.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seaboxdata.api.core.model.DataResponse;
import com.seaboxdata.api.core.model.ResultInfo;
import com.seaboxdata.api.core.xml.Dom4jBuildXml;
import com.seaboxdata.controller.IRestfulController;
import com.seaboxdata.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class RestfulController implements IRestfulController {
    @Autowired
    private DataService dataService;


    @Override
    public ResponseEntity getDataXml(Integer pageNo, Integer pageSize, String search,
                                Long resourceId,String token,HttpServletRequest request) {
        ResultInfo data = dataService.getData(pageNo, pageSize, search, null,resourceId,token,request);
        String xmlString = null;
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            Dom4jBuildXml dom4jBuildXml = new Dom4jBuildXml();
            xmlString = dom4jBuildXml.rootObjToXML(null, DataResponse.getDataXmlResponse(data));
        } catch (Exception e) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            xmlString = DataResponse.getDataXmlErrorResponse(e.getMessage());
        }
        return new ResponseEntity(xmlString, httpStatus);
    }


    @Override
    public ResponseEntity getDataJson(Integer pageNo, Integer pageSize, String search,
                                      Long resourceId, String token, HttpServletRequest request) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();
        ResultInfo data = dataService.getData(pageNo, pageSize, search, null,resourceId,token,request);
        Map dataJsonResponse = DataResponse.getDataJsonResponse(gson.toJson(data));
        return new ResponseEntity(dataJsonResponse, HttpStatus.OK);
    }


    @Override
    public ResponseEntity getDataXmlWithoutTitle(Integer pageNo, Integer pageSize, String search,String showColumns,
                                             Long resourceId,String token,HttpServletRequest request)  {
        ResultInfo data = dataService.getData(pageNo, pageSize, search, showColumns,resourceId,token,request);
        data.getData().setTitle(null);
        String xmlString = null;
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            Dom4jBuildXml dom4jBuildXml = new Dom4jBuildXml();
            xmlString = dom4jBuildXml.rootObjToXML(null, DataResponse.getDataXmlWithoutTitleResponse(data));
        } catch (Exception e) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            xmlString = DataResponse.getDataXmlWithoutTitleErrorResponse(e.getMessage());
        }
        return new ResponseEntity(xmlString, httpStatus);
    }


    @Override
    public ResponseEntity getDataJsonWithoutTitle(Integer pageNo, Integer pageSize, String search,String showColumns,
                                          Long resourceId,String token,HttpServletRequest request) {
        ResultInfo data = dataService.getData(pageNo, pageSize, search, showColumns,resourceId,token,request);
        data.getData().setTitle(null);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();
        Map dataJsonWithoutTitleResponse = DataResponse.getDataJsonWithoutTitleResponse(gson.toJson(data));
        return new ResponseEntity(dataJsonWithoutTitleResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity getIsHasData(String search,Long resourceId,String token,HttpServletRequest request) {
        ResultInfo dataXml = dataService.getData(1, 1, search, null, resourceId, token, request);
        List result = dataXml.getData().getResult();
        Boolean res;
        if (result.size() > 0) {
            res = true;
        } else {
            res = false;
        }
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();
        Map data = DataResponse.getIsHasDataResponse(gson.toJson(res));
        return new ResponseEntity(data, HttpStatus.OK);
    }
}
