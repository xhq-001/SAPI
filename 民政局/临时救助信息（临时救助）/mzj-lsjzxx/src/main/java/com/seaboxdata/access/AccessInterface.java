package com.seaboxdata.access;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.seaboxdata.api.core.auth.Authenticate;
import com.seaboxdata.api.core.auth.FieldHandle;
import com.seaboxdata.api.core.constant.ErrorFlagCode;
import com.seaboxdata.api.core.constant.ResultCode;
import com.seaboxdata.api.core.model.Column;
import com.seaboxdata.api.core.model.PageResponse;
import com.seaboxdata.api.core.model.ResultInfo;
import com.seaboxdata.api.core.model.auth.AuthField;
import com.seaboxdata.api.core.model.token.JwtUserToken;
import com.seaboxdata.api.core.utils.*;
import com.seaboxdata.api.core.xml.XmlParseObject;
import com.seaboxdata.api.dto.ResourceFieldAuth;
import com.seaboxdata.constant.CustomConstant;
import com.seaboxdata.model.Dysqmx;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
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
    public ResultInfo obtainData(Integer pageNo, Integer pageSize, String search, String showColumns, List<Column> columns) throws Exception {
        LocalDateTime start = LocalDateTime.now();
        String message = "";
        ResultInfo resultInfo = new ResultInfo();
        ResourceFieldAuth authField = null;
        /*
         * 构建title
         */
//        List<Column> columns = TitleUtil.buildTitle();
        pageNo = pageNo == null ? 1 : pageNo;//设置默认值
        pageSize = pageSize == null ? 10 :pageSize;
        //解析参数拼接xmldoc，访问接口
        Map<String, String> extParam = parseSearch(search);
        if (extParam.size() == 0) {
            message = "请输入查询条件...";
            resultInfo.setMessage(message);
            return resultInfo;
        }else if(extParam.get("county_id") != null && (extParam.get("audit_date_s") == null || extParam.get("audit_date_e") == null)){
            message = "区县代码和开始、结束时间必须一起输入...";
            resultInfo.setMessage(message);
            return resultInfo;
        }else if(extParam.get("county_id") == null && (extParam.get("audit_date_s") == null || extParam.get("audit_date_e") == null)){
            message = "开始、结束时间必须一起输入...";
            resultInfo.setMessage(message);
            return resultInfo;
        }

        Map<String, String> paramsMap = new HashMap<>();
        String result = null;
        paramsMap.put("county_id","");
        paramsMap.put("audit_date_s","");
        paramsMap.put("audit_date_e","");
        String county_id = extParam.get("county_id");
        String audit_date_s = extParam.get("audit_date_s");
        String audit_date_e = extParam.get("audit_date_e");
        if(county_id != null){
            paramsMap.put("county_id",county_id);
        }
        if(audit_date_s!=null){
            paramsMap.put("audit_date_s",audit_date_s);
        }
        if(audit_date_e!=null){
            paramsMap.put("audit_date_e",audit_date_e);
        }
        // 拼装参数xmlDoc
        String xmlDoc = CustomConstant.queryXML.replaceAll("\\$pageNo\\$", pageNo.toString())
                .replaceAll("\\$pageSize\\$", pageSize.toString())
                .replaceAll("\\$condition\\$", buildQueryXml(paramsMap));
        System.out.println(xmlDoc);
        //根据开始和结束时间查询
        if(county_id == null && audit_date_s != null && audit_date_e != null){
            result = call("01", "01Q27", xmlDoc);
        }else if(county_id != null && audit_date_s != null && audit_date_e != null){
            result = call("01", "01Q28", xmlDoc);
        }
//        String result = testMoniData();
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

    private Map<String, String> parseSearch(String search) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isBlank(search)) {
            return map;
        }
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(search).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            Set<String> keys = jsonObject.keySet();
            for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                String value = jsonObject.get(key).getAsString();
                key = key.replaceAll("and.|or.|.neq|.eq|.gte|.lte|.gt|.lt|.like|.in|.isNotNull|.isNull", "");
                map.put(key.toLowerCase(), value);
            }
        }
        return map;
    }
}
