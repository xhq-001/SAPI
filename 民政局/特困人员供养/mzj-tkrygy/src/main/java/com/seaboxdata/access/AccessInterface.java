package com.seaboxdata.access;

import com.seaboxdata.api.dto.ResourceFieldAuth;
import org.json.JSONArray;
import org.json.JSONObject;
import com.seaboxdata.api.core.auth.FieldHandle;
import com.seaboxdata.api.core.model.Column;
import com.seaboxdata.api.core.model.ColumnShort;
import com.seaboxdata.api.core.model.PageResponse;
import com.seaboxdata.api.core.model.ResultInfo;
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

    public ResultInfo postRequest(Integer pageNo, Integer pageSize, String search , String showColumns, List<Column> columns)throws Exception {
        ResultInfo resultInfo = new ResultInfo();
        Map<String, String> paramsMap = new HashMap<>();
        String result = null;
        paramsMap.put("sfzhm","");
        Map<String, String> extParam = parseSearchs(search);
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
        ResourceFieldAuth authField = null;
        List<String> allowField = FieldHandle.getAllowField(authField.getFieldNames(), showColumns);
        FieldHandle.removeFields(data,allowField);
        page.setResult(data);
        page.setPageNo(pageNo);
        resultInfo.setData(page);
        resultInfo.setMessage("查询成功");
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
    /**
     * 提取查询条件（该方法可复用）
     *
     * @param search
     * @return <条件名称全小写，条件值>
     * @author dengshengyu
     * @date 2018年5月7日上午11:57:02
     */
    private Map<String, String> parseSearchs(String search) {
        Map<String, String> map = new HashMap<>();
        if (search == null || "".equals(search.trim())) {
            return map;
        }
        try {
            JSONArray jsonArr = new JSONArray(search);
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                String key = jsonObj.keys().next();
                String value = jsonObj.getString(key);
                key = key.toLowerCase();
                key = key.replace(".eq", "");
                key = key.replace(".like", "");
                key = key.replace(".gt", "");
                key = key.replace(".lt", "");
                key = key.replace(".or", "");
                key = key.replace(".and", "");
                key = key.replace(".gte", "");
                key = key.replace(".lte", "");
                key = key.replace(".in", "");
                key = key.replace(".isNotNull", "");
                key = key.replace(".isNull", "");
                map.put(key.toLowerCase(), value);
            }
        } catch (Exception e) {
            return map;
        }
        return map;
    }

}
