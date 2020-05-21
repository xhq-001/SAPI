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
    private String apply_id;	//业务申请编号
    private String county_id;	//区县代码
    private String town_id;	//镇街代码
    private String domicile_id;	//村社代码
    private String audit_date;	//救助时间
    private String family_name;	//户主姓名
    private String family_card_no;	//户主身份证号码
    private String sex;	//性别
    private String assistance_type;	//对象类别
    private String apanage_flag;	//救助属地
    private String address;	//家庭地址
    private String people_num;	//家庭人口数
    private String apply_reason;	//申请理由
    private String temporary_apply_type;	//申请救助类型
    private String debit;	//遭受困难支出金额
    private String oneself_pay;	//家庭或个人实际承担金额
    private String check_money;	//乡镇审批金额
    private String audit_money;	//区县审批金额
    private String town_money;	//乡镇发放金额
    private String county_money;	//区县发放金额
}
