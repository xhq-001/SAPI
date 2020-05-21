package com.seaboxdata.controller;

import com.seaboxdata.constant.CustomConstant;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/services/{resourceId}/${serviceCode}/{token}")
public interface IRestfulController {
    @GetMapping(value = "/getDataXml", produces = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity getDataXml(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                              @RequestParam(value = "pageSize", required = false) Integer pageSize,
                              @RequestParam(value = "search", required = false) String search,
                              @PathVariable("resourceId") Long resourceId,
                              @PathVariable("token") String token,
                              HttpServletRequest request
    );

    @GetMapping(value = "/getDataJson", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getDataJson(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                               @RequestParam(value = "pageSize", required = false) Integer pageSize,
                               @RequestParam(value = "search", required = false) String search,
                               @PathVariable("resourceId") Long resourceId,
                               @PathVariable("token") String token,
                               HttpServletRequest request);

    @GetMapping(value = "/getDataXmlWithoutTitle", produces = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity getDataXmlWithoutTitle(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                          @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                          @RequestParam(value = "search", required = false) String search,
                                          @RequestParam(value = "showColumns", required = false) String showColumns,
                                          @PathVariable("resourceId") Long resourceId,
                                          @PathVariable("token") String token,
                                          HttpServletRequest request);

    @GetMapping(value = "/getDataJsonWithoutTitle", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getDataJsonWithoutTitle(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                           @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                           @RequestParam(value = "search", required = false) String search,
                                           @RequestParam(value = "showColumns", required = false) String showColumns,
                                           @PathVariable("resourceId") Long resourceId,
                                           @PathVariable("token") String token,
                                           HttpServletRequest request);

    @GetMapping(value = "/getIsHasData", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getIsHasData(@RequestParam(value = "search", required = false) String search, @PathVariable("resourceId") Long resourceId,
                                @PathVariable("token") String token, HttpServletRequest request);
}
