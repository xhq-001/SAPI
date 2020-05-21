package com.seaboxdata.controller;

import com.seaboxdata.api.core.model.ResultInfo;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * @author xhq
 */
@WebService
public interface IWebServiceController {
    @WebMethod(operationName = "getDataJson")
    @WebResult(name = "getDataJsonResponse")
    String getDataJson(@WebParam(name = "pageNo") Integer pageNo, @WebParam(name = "pageSize") Integer pageSize, @WebParam(name = "search") String search);
    @WebMethod(operationName = "getDataXmlWithoutTitile")
    @WebResult(name = "getDataXmlWithoutTitileResponse")
    ResultInfo getDataXmlWithoutTitle(@WebParam(name = "pageNo") Integer pageNo, @WebParam(name = "pageSize") Integer pageSize, @WebParam(name = "search") String search, @WebParam(name = "showCloumns") String showCloumns);
    @WebMethod(operationName = "getDataXml")
    @WebResult(name = "getDataXmlResponse")
    ResultInfo getDataXml(@WebParam(name = "pageNo") Integer pageNo, @WebParam(name = "pageSize") Integer pageSize, @WebParam(name = "search") String search);
    @WebMethod(operationName = "getDataJsonWithoutTitile")
    @WebResult(name = "getDataJsonWithoutTitileResponse")
    String getDataJsonWithoutTitle(@WebParam(name = "pageNo") Integer pageNo, @WebParam(name = "pageSize") Integer pageSize, @WebParam(name = "search") String search, @WebParam(name = "showCloumns") String showCloumns);
    @WebMethod(operationName = "getIsHasData")
    @WebResult(name = "getIsHasDataResponse")
    String getIsHasData(@WebParam(name = "search") String search);
}
