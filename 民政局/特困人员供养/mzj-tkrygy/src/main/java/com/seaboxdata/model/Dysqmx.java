package com.seaboxdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Accessors(chain = true)
public class Dysqmx {
    private String sfzhm;	//身份证号码
    private String Sqr;	//姓名
    private String nrrq;	//纳入日期
    private String czbs;	//城镇标识
    private String xjzdz;	//现居住地
    private String qxgyxs;	//供养方式
    private String rylb;	//人员类别
    private String tcsj;	//退出时间
    private String ssqxmc;	//所属区县名称
    private String xbmc;	//性别名称
}
