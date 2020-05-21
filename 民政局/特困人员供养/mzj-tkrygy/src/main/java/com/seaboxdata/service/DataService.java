package com.seaboxdata.service;

import com.seaboxdata.access.AccessInterface;
import com.seaboxdata.api.core.auth.Authenticate;
import com.seaboxdata.api.core.constant.ErrorFlagCode;
import com.seaboxdata.api.core.constant.ResultCode;
import com.seaboxdata.api.core.log.SinkLog;
import com.seaboxdata.api.core.model.Column;
import com.seaboxdata.api.core.model.ColumnShort;
import com.seaboxdata.api.core.model.PageResponse;
import com.seaboxdata.api.core.model.ResultInfo;
import com.seaboxdata.api.core.utils.JacksonUtil;
import com.seaboxdata.api.core.utils.JwtUtils;
import com.seaboxdata.api.core.utils.TitleUtil;
import com.seaboxdata.api.dto.FieldDto;
import com.seaboxdata.api.dto.ResourceFieldAuth;
import com.seaboxdata.model.Dysqmx;
import com.seaboxdata.sesb.auth.ip.IPProcess;
import com.seaboxdata.sesb.log.model.ResourceTransferLog;
import com.seaboxdata.sesb.pojo.dto.JwtIPDto;
import com.seaboxdata.sesb.pojo.dto.JwtUserTokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataService {
    @Autowired
    private SinkLog sinkLog;


    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private AccessInterface accessInterface;

    @Autowired
    private Authenticate authenticate;


    @Value("${custom.isTokenVerification}")
    private String isTokenVerification;

    @Value("${serviceCode}")
    private String serviceCode;

    private static Map<String, ColumnShort> columnShortMap;

    static {
        columnShortMap = new HashMap<>();
        columnShortMap.put("sfzhm", new ColumnShort("sfzhm", "eq", "身份证号码"));

    }

    public ResultInfo getData(Integer pageNo, Integer pageSize, String search, String showColumns,
                              Long resourceId, String token, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        List<Dysqmx> data = null;
        PageResponse<Dysqmx> page = new PageResponse<>();
        ResultInfo resultInfo = new ResultInfo();
        JwtUserTokenDto userToken = null;
        JwtIPDto requestIp = null;
        Integer stdcall = 1;
//            token认证过程 12345
        ResourceFieldAuth authField = null;

        List<Column> columns = null;
        //测试时候isTokenVerification设置为false
        if ("false".equals(isTokenVerification)) {
            //ip校验
            requestIp = IPProcess.getRequestIp(request);
            stdcall = requestIp.getStdcall();
            userToken = new JwtUserTokenDto();
            userToken.setUserId(225577573428629504L);
            userToken.setIp("127.0.0.1");
            //字段权限的控制
            authField = testMoniAuthField();
            //构建title
            columns = TitleUtil.buildTitle(authField, columnShortMap);
        } else {
            try {
                //ip校验
                requestIp = IPProcess.getRequestIp(request);
                stdcall = requestIp.getStdcall();
                //token校验
                userToken = jwtUtil.getJwtUserToken(token);
                //字段权限的控制
                authField = authenticate.authorityVer(userToken.getUserId(), resourceId);
                //构建title
                columns = TitleUtil.buildTitle(authField, columnShortMap);
//                //构建title
//                columns = TitleUtil.buildTitle(authField, columnShortMap);

            } catch (Exception e) {
                resultInfo.setCode(ResultCode.ERROR);
                resultInfo.setErrorFlag(ErrorFlagCode.PLATFORMERROR);
                resultInfo.setMessage(e.getMessage());
                sinkLogKafka(resourceId, userToken, null, startTime, "1", "401", "0", stdcall);
                return resultInfo;
            }

        }

        try {
            resultInfo = accessInterface.postRequest(pageNo, pageSize, search, null,columns);
            sinkLogKafka(resourceId, userToken, data, startTime, "0", "200", "0", stdcall);//日志记录
            return resultInfo;
        } catch (Exception e) {
            e.printStackTrace();
            resultInfo.setCode(ResultCode.ERROR);
            resultInfo.setErrorFlag(ErrorFlagCode.PROVIDEERROR);
            resultInfo.setMessage(e.getMessage());
            sinkLogKafka(resourceId, userToken, data, startTime, "1", "401", "0", stdcall);
            return resultInfo;
        }

    }
    /**
     * 测试字段权限控制
     * @return
     */
    /**
     * 测试字段权限控制
     *
     * @return
     */
    private ResourceFieldAuth testMoniAuthField() {
        //todo 待删除
        ResourceFieldAuth authField = new ResourceFieldAuth();
        authField.setFlag(true);
        FieldDto fieldDto = new FieldDto();
        fieldDto.setDataCode("工商登记号码_社团登记号");
        fieldDto.setDataName("测试哈哈哈");
        List<FieldDto> list = new ArrayList<>();
        list.add(fieldDto);

        authField.setFields(list);
        authField.setFieldNames(list.stream().map(FieldDto::getDataCode).collect(Collectors.toList()));
        return authField;
    }

    /**
     * 日志记录
     *
     * @param resourceId
     * @param userToken
     * @param data
     * @param startTime
     */
    private void sinkLogKafka(Long resourceId,
                              JwtUserTokenDto userToken,
                              List<Dysqmx> data,
                              Long startTime,
                              String type,
                              String statusCode, String logType, Integer stdcall) {
        ResourceTransferLog resourceTransferLog = new ResourceTransferLog();
        resourceTransferLog.setResourceId(resourceId + "");
        resourceTransferLog.setServiceCode(serviceCode);

        if (data == null) {
            resourceTransferLog.setDataNumber("0");
        } else {
            resourceTransferLog.setDataNumber(data.size() + "");
        }

        if (userToken == null) {
            resourceTransferLog.setCallsIp(null);
            resourceTransferLog.setUserId(null);
        } else {
            resourceTransferLog.setCallsIp(userToken.getIp());
            resourceTransferLog.setUserId(userToken.getUserId() + "");
        }

        resourceTransferLog.setLogType(logType);
        resourceTransferLog.setResponseTime((System.currentTimeMillis() - startTime) / 1000.000 + "");
        resourceTransferLog.setStatusCode(statusCode);
        //0:平台调用  1：非平台调用
        resourceTransferLog.setStdcall(stdcall + "");
        resourceTransferLog.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        resourceTransferLog.setTraffic((JacksonUtil.getJson(data).getBytes().length / 1000.00) + "");
        resourceTransferLog.setType(type);
        sinkLog.sinkLogToKafka(resourceTransferLog);
    }
}
