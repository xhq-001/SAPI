package com.seaboxdata.access;

import com.seaboxdata.api.core.auth.FieldHandle;
import com.seaboxdata.api.core.model.PageResponse;
import com.seaboxdata.api.core.utils.DESUtil;
import com.seaboxdata.api.core.utils.HttpClientCallSoapUtil;
import com.seaboxdata.api.core.utils.RSAUtil;
import com.seaboxdata.api.core.xml.XmlParseObject;
import com.seaboxdata.constant.CustomConstant;
import com.seaboxdata.model.Dysqmx;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class AccessInterface {

    @Value("${custom.jksqm}")
    private String jksqm;
    @Value("${custom.publicKey}")
    private String publicKey;
    @Value("${custom.webserviceURL}")
    private String webserviceURL;
    @Value("${custom.namespaceURI}")
    private String namespaceURI;

    private String postRequest(Integer pageNo, Integer pageSize, Map<String, String> extParam) {
        Map<String, String> paramsMap = new HashMap<>();
        String result = null;
        paramsMap.put("sfzhm","");
        if (extParam.get("sfzhm")!=null){
            paramsMap.put("sfzhm",extParam.get("sfzhm"));
        }
        // 拼装参数xmlDoc
        String xmlDoc = CustomConstant.queryXML.replaceAll("\\$pageNo\\$", pageNo.toString())
                .replaceAll("\\$pageSize\\$", pageSize.toString())
                .replaceAll("\\$condition\\$", buildQueryXml(paramsMap));
        System.out.println(xmlDoc);
        result = call("01", "01Q40", xmlDoc);


        List<Dysqmx> data = null;
        PageResponse<Dysqmx> page = new PageResponse<>();
        page.setTitle(columns);
        // 装配并解析对象
        if (StringUtils.isNotBlank(result)) {
            try {
                data = XmlParseObject.convertXMLtoObjectList(Dysqmx.class,result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*
         * 字段处理，取出多余字段
         */
        List<String> allowField = FieldHandle.getAllowField(authField.getFieldNames(), showColumns);
        FieldHandle.removeFields(data,allowField);
        page.setResult(data);
        page.setPageNo(pageNo);
        resultInfo.setData(page);
        resultInfo.setMessage(message);
        /*
         * 日志记录
         */
        //ServiceLog.logSuccess(resourceId,userToken.getUserId(),userToken.getIp(),start,resultInfo);
        return resultInfo;

        return result;
    }

    /**
     * 调用接口
     * 1、使用接口授权码生产32DES加密串
     * 2、使用DES加密串加密参数xmldoc
     * 3、使用RAS算法和publickey加密DES加密串
     * @return 接口字符串（解密后） 返回值使用DES加密串解密
     */
    public String call(String xtlb,String jkid,String xmldoc){
        String des_32 = DESUtil.getKey(jksqm);
        String des_xmldoc = DESUtil.encryptor(xmldoc == null ? "" : xmldoc, des_32);
        String ms = RSAUtil.encrypt(des_32, publicKey);
        Map<String,String> param = new HashMap<>();
        param.put("xtlb",xtlb);
        param.put("jkid",jkid);
        param.put("jksqm",jksqm);
        param.put("ms",ms);
        param.put("xmldoc",des_xmldoc);

        // 调用webservice接口
        String soapXml = HttpClientCallSoapUtil.buildSoapXml(CustomConstant.postXML, param);
        String result = HttpClientCallSoapUtil.doPostSoap1_1(webserviceURL, soapXml, null);
        String res = null;
        if (StringUtils.isNotBlank(result)) {
            try {
                Document dom = XmlParseObject.load(new ByteArrayInputStream(result.getBytes("UTF-8")));
                res = XmlParseObject.getElement(dom, "//return");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //解密内容
        return res == null?null:DESUtil.decryptor(res, des_32);
    }
    private String buildQueryXml(Map<String, String> param) {
        StringBuilder sb = new StringBuilder();
        Set<String> keys = param.keySet();
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            String value = param.get(key);
            if (StringUtils.isNotBlank(value)) {
                sb.append("<" + key + ">" + value + "</" + key + ">");
            }
        }
        return sb.toString();
    }
}
