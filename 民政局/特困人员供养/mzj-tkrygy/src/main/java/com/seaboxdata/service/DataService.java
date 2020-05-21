package com.seaboxdata.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.seaboxdata.access.AccessInterface;
import com.seaboxdata.api.core.auth.Authenticate;
import com.seaboxdata.api.core.auth.FieldHandle;
import com.seaboxdata.api.core.constant.ErrorFlagCode;
import com.seaboxdata.api.core.constant.ResultCode;
import com.seaboxdata.api.core.model.Column;
import com.seaboxdata.api.core.model.ColumnShort;
import com.seaboxdata.api.core.model.PageResponse;
import com.seaboxdata.api.core.model.ResultInfo;
import com.seaboxdata.api.core.model.auth.AuthField;
import com.seaboxdata.api.core.model.auth.Field;
import com.seaboxdata.api.core.model.token.JwtUserToken;
import com.seaboxdata.api.core.utils.IpUtil;
import com.seaboxdata.api.core.utils.JwtUtil;
import com.seaboxdata.api.core.utils.TitleUtil;
import com.seaboxdata.api.core.xml.XmlParseObject;
import com.seaboxdata.constant.CustomConstant;
import com.seaboxdata.model.Dysqmx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataService {
    @Autowired
    private AccessInterface accessInterface;
    private static Map<String, ColumnShort> columnShortMap;
    static {
        columnShortMap = new HashMap<>();
        columnShortMap.put("sfzhm",new ColumnShort("sfzhm","eq","身份证号码"));
//        columnShortMap.put("death_date",new ColumnShort(null,"eq",true,false,null,true));
    }

    public ResultInfo getData(Integer pageNo, Integer pageSize, String search, String showColumns,
                              Long resourceId, String token, HttpServletRequest request) {
        LocalDateTime start = LocalDateTime.now();
        String message = "";
        ResultInfo resultInfo = new ResultInfo();
        // search  [{"a.eq":"1"},{"and.b.eq":"2"}]
        /*
          token认证过程
         */
        /*JwtUserToken userToken = null;
        try {
            userToken = JwtUtil.getJwtUserToken(token);
        } catch (Exception e) {
            resultInfo.setCode(ResultCode.ERROR);
            resultInfo.setErrorFlag(ErrorFlagCode.NORMAL);
            resultInfo.setMessage(e.getMessage());
            return resultInfo;
        }*/
        /*
         * IP校验
         */
        /*if (!IpUtil.IpVerify(userToken.getIp(),request)){
            resultInfo.setCode(ResultCode.ERROR);
            resultInfo.setErrorFlag(ErrorFlagCode.NORMAL);
            resultInfo.setMessage("IP地址无效");
            return resultInfo;
        }*/
        /*
         * 字段权限的控制
         */
//        AuthField authField = Authenticate.getInstance().authorityVer(userToken.getUserId(), resourceId);
        AuthField authField = testMoniAuthField();
        //权限的判断
        /*
         * 构建title
         */
        List<Column> columns = TitleUtil.buildTitle(authField, columnShortMap);
        //解析参数拼接xmldoc，访问接口
        Map<String, String> extParam = parseSearch(search);
        if (extParam.size() == 0) {
            message = "请输入查询条件...";
            resultInfo.setMessage(message);
            return resultInfo;
        } else if (extParam.size() != 1) {
            message = "只能输入一个查询条件...";
            resultInfo.setMessage(message);
            return resultInfo;
        }

        String result = postRequest(pageNo, pageSize, extParam);
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
     * 测试字段权限控制
     * @return
     */
    private AuthField testMoniAuthField(){
        AuthField authField = new AuthField();
        authField.setFlag(true);
        Field field1 = new Field().setDataCode("sfzhm");
        List<Field> list = new ArrayList<>();
        list.add(field1);
        authField.setFields(list);
        authField.setFieldNames(list.stream().map(Field::getDataCode).collect(Collectors.toList()));
        return authField;
    }

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
        result = accessInterface.call("01", "01Q40", xmlDoc);
        return result;
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
