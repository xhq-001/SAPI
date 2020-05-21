package com.seaboxdata.constant;

public class CustomConstant {
    public static final String postXML="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.bus.cqhg.com/\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <ws:queryObject>\n" +
            "         <xtlb>$xtlb$</xtlb>\n" +
            "         <jkid>$jkid$</jkid>\n" +
            "         <jksqm>$jksqm$</jksqm>\n" +
            "         <ms>$ms$</ms>\n" +
            "         <xmldoc>$xmldoc$</xmldoc>\n" +
            "      </ws:queryObject>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
    public static  final String queryXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<root>\n" +
            "<Page>\n" +
            "<pagenum>$pageNo$</pagenum>\n" +
            "<pagesize>$pageSize$</pagesize>\n" +
            "</Page>\n" +
            "<QueryCondition>\n" +
            "$condition$" +
            "</QueryCondition>\n" +
            "</root>\n";
    public static final String serviceCode = "1234";
}
