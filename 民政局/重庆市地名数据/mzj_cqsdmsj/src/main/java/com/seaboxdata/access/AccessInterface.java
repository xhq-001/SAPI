package com.seaboxdata.access;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.seaboxdata.api.core.auth.FieldHandle;
import com.seaboxdata.api.core.constant.ErrorFlagCode;
import com.seaboxdata.api.core.constant.ResultCode;
import com.seaboxdata.api.core.model.Column;
import com.seaboxdata.api.core.model.PageResponse;
import com.seaboxdata.api.core.model.ResultInfo;
import com.seaboxdata.api.core.utils.DESUtil;
import com.seaboxdata.api.core.utils.HttpClientCallSoapUtil;
import com.seaboxdata.api.core.utils.RSAUtil;
import com.seaboxdata.api.core.xml.XmlParseObject;
import com.seaboxdata.api.dto.ResourceFieldAuth;
import com.seaboxdata.constant.CustomConstant;
import com.seaboxdata.model.Dysqmx;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.json.JSONObject;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class AccessInterface {

    @Value("${custom.url}")
    private String url;
    @Value("${custom.key}")
    private String key;


    /**
     * 调用接口
     * 1、使用接口授权码生产32DES加密串
     * 2、使用DES加密串加密参数xmldoc
     * 3、使用RAS算法和publickey加密DES加密串
     * @return 接口字符串（解密后） 返回值使用DES加密串解密
     */
    public ResultInfo call(Integer pageNo, Integer pageSize, String search, String showColumns, List<Column> columns)throws Exception{
        ResultInfo resultInfo = new ResultInfo();
        Map<String, String> extParam = parseSearch(search);
        String message ="";
        if (extParam.size() == 0) {
            message = "请输入查询条件...";
            resultInfo.setMessage(message);
            return resultInfo;
        }else if(extParam.get("quhua")==null){
            message = "行政区划必输...";
            resultInfo.setMessage(message);
            return resultInfo;
        }
        //分页条件设置默认值
        pageNo = pageNo==null ? 1 : pageNo;
        pageSize = pageSize==null ? 10 : pageSize;

        String url1 = url + "?key="+key+"&pageIndex="+pageNo.toString()+"&pageSize="+pageSize.toString();
        Set<String> keyset = extParam.keySet();
        String str = "";
        for (String s: keyset) {
            str = str + "&"+s+"="+extParam.get(s);
        }
        url1 = url1+str;
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url1);
        get.setHeader("Content-Type","application/json;charset=UTF-8");
        HttpResponse respnse = httpClient.execute(get);
        int statusCode = respnse.getStatusLine().getStatusCode();
        if(statusCode != 200){
            resultInfo.setCode(ResultCode.ERROR);
            resultInfo.setErrorFlag(ErrorFlagCode.PROVIDEERROR);
            resultInfo.setMessage("提供方数据调用异常"+statusCode);
            return resultInfo;
        }
        String content = EntityUtils.toString(respnse.getEntity(),"UTF-8");
        System.out.println(content);
//        String result = testMoniData();
        List<Dysqmx> data = null;
        PageResponse<Dysqmx> page = new PageResponse<>();
        page.setTitle(columns);
        // 装配并解析对象
        JSONObject jsonObject = new JSONObject(content);
        String list = jsonObject.get("list").toString();

        if (StringUtils.isNotBlank(content)) {
            try {
                Gson gson = new Gson();
                data = gson.fromJson(list,new TypeToken<List<Dysqmx>>(){}.getType());
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
        page.setTotalPages(Integer.parseInt(jsonObject.get("totalPage").toString()));
        page.setTotalRecords(Integer.parseInt(jsonObject.get("totalRow").toString()));
        page.setResult(data);
        page.setPageNo(pageNo);
        resultInfo.setData(page);
        resultInfo.setRealDataType(2);//设置查询条件有没有and或者or
        resultInfo.setMessage(message);
        /*
         * 日志记录
         */
        //ServiceLog.logSuccess(resourceId,userToken.getUserId(),userToken.getIp(),start,resultInfo);
        return resultInfo;




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
