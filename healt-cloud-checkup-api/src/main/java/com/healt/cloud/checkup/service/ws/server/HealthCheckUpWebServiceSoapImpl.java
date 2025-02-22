
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package com.healt.cloud.checkup.service.ws.server;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.healt.cloud.checkup.entity.*;
import com.healt.cloud.checkup.service.*;
import com.healt.cloud.checkup.service.ws.his.costChargeState.Item;
import com.healt.cloud.checkup.service.ws.server.vo.ChargeItemListVo;
import com.healt.cloud.checkup.service.ws.server.vo.ChargeItemVo;
import com.healt.cloud.common.utils.DateUtils;
import com.healt.cloud.common.utils.PropertiesUtils;
import com.healt.cloud.common.utils.SoapUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 3.1.6
 * 2022-08-22T20:11:59.756+08:00
 * Generated source version: 3.1.6
 *
 */

@WebService(
        serviceName = "HealthCheckUpWebService",
        portName = "HealthCheckUpWebServiceSoap",
        targetNamespace = "http://server.ws.service.checkup.cloud.healt.com",
        wsdlLocation = "file:/D:/vsworkspace/MyWG.Proxy2.Service.HealthCheckUpWebServiceImplPort.xml",
        endpointInterface = "com.healt.cloud.checkup.service.ws.server.HealthCheckUpWebServiceSoap")
@Component
public class HealthCheckUpWebServiceSoapImpl implements HealthCheckUpWebServiceSoap {

    private static final Logger LOG = Logger.getLogger(HealthCheckUpWebServiceSoapImpl.class.getName());

    @Resource
    HcPersonAppregItemsService hcPersonAppregItemsService;

    @Resource
    private CostDetailBillingService costDetailBillingService;

    @Resource
    private CostChargeStateMasterService costChargeStateMasterService;

    @Resource
    private CostChargeStateDetailService costChargeStateDetailService;

    @Resource
    private WebserviceLogService webserviceLogService;

    @Resource
    private AccountsChargeBackService accountsChargeBackService;

    @Resource
    private CompanyCostBackService companyCostBackService;

    /* (non-Javadoc)
     * @see com.healt.cloud.checkup.service.ws.server.HealthCheckUpWebServiceSoap#costChargeState(com.healt.cloud.checkup.service.ws.server.CostChargeStateRequest  parameters )*
     */
    public CostChargeStateResponse costChargeState(CostChargeStateRequest parameters) {
        String costChargeStateRequestStr = parameters.getCostChargeStateRequest();

        String code = "CA";//CA 成功 CE 失败
        String desc = "成功";

        String chargeItemListXmlStr = SoapUtil.getChargeItemList(costChargeStateRequestStr,"<ChargeItemList>", "</ChargeItemList>");
        if(StringUtils.isNotEmpty(chargeItemListXmlStr)){
            String orderId = "";//SoapUtil.getValue(costChargeStateRequestStr,"<OrderId>", "</OrderId>");
            String msgId = "";
            String sender = "";
            msgId = SoapUtil.getValue(costChargeStateRequestStr,"<MsgId>", "</MsgId>");
            sender = SoapUtil.getValue(costChargeStateRequestStr,"<Sender>", "</Sender>");
            WebserviceLog log = new WebserviceLog();
            log.setUuid(msgId);
            log.setInsertDate(new Date());
            log.setDataStr(costChargeStateRequestStr);
            log.setDataType("入参");
            log.setMethod("单位收费状态回传");
            webserviceLogService.save(log);
            ChargeItemListVo chargeItemListVo = SoapUtil.xml2Bean(chargeItemListXmlStr, ChargeItemListVo.class);
            for(ChargeItemVo chargeItemVo : chargeItemListVo.getChargeItem()){
                orderId = chargeItemVo.getOrderId();
                if(StringUtils.isNotEmpty(orderId)){
                    orderId = orderId.trim();
                    if(orderId.length()==10&&orderId.startsWith("11")){//团检收费状态回传
                        //团检状态回传 插入 HEALTHCHECKUP.ACCOUNTS_CHARGE_BACK
                        try {
                            String examCompanyID = SoapUtil.getValue(costChargeStateRequestStr,"<ExamCompanyID>", "</ExamCompanyID>");
                            String examCompanyNo = SoapUtil.getValue(costChargeStateRequestStr,"<ExamCompanyNo>", "</ExamCompanyNo>");
                            String examCompanyName = SoapUtil.getValue(costChargeStateRequestStr,"<ExamCompanyName>", "</ExamCompanyName>");
                            String areaCode = SoapUtil.getValue(costChargeStateRequestStr,"<AreaCode>", "</AreaCode>");
                            String medInstId = SoapUtil.getValue(costChargeStateRequestStr,"<MedInstId>", "</MedInstId>");
                            String feeStatus = SoapUtil.getValue(costChargeStateRequestStr,"<FeeStatus>", "</FeeStatus>");
                            String rekNo = chargeItemVo.getRekNo();
                            String rekId = chargeItemVo.getRekId();
                            String operator = chargeItemVo.getOperator();
                            String operatorTime = chargeItemVo.getOperateTime();
                            if(StringUtils.isEmpty(operatorTime)){
                                operatorTime = DateUtils.getDateYYYYMMDDHHMMSS();
                            }
                            AccountsChargeBack accountsChargeBack = new AccountsChargeBack();
                            accountsChargeBack.setExamcompanyid(examCompanyID);
                            accountsChargeBack.setExamcompanyno(examCompanyNo);
                            accountsChargeBack.setExamcompanyname(examCompanyName);
                            accountsChargeBack.setApplyorgcode(areaCode);
                            accountsChargeBack.setMedinstid(medInstId);
                            accountsChargeBack.setOrderid(orderId);
                            accountsChargeBack.setFeestatus(feeStatus);
                            accountsChargeBack.setRekno(rekNo);
                            accountsChargeBack.setRekid(rekId);
                            accountsChargeBack.setOperator(operator);
                            accountsChargeBack.setOperatetime(DateUtils.parseString(operatorTime, "yyyyMMddhhmmss"));
//                  accountsChargeBack.setInvoiceid();
                            accountsChargeBackService.save(accountsChargeBack);
                            //更新 HEALTHCHECKUP.Company_Cost_Back 的结算标识 rekid ,
                            LambdaUpdateWrapper<CompanyCostBack> companyCostBackLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                            companyCostBackLambdaUpdateWrapper.set(CompanyCostBack :: getRekid, rekId);
                            companyCostBackLambdaUpdateWrapper.eq(CompanyCostBack :: getExamcompanyid, examCompanyID);
                            companyCostBackLambdaUpdateWrapper.eq(CompanyCostBack :: getOrderid, orderId);
                            companyCostBackService.update(companyCostBackLambdaUpdateWrapper);
                            //更新 hc_person_appreg_items 的 BILL_INDICATOR
                            LambdaUpdateWrapper<HcPersonAppregItems> hcPersonAppregItemsLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                            hcPersonAppregItemsLambdaUpdateWrapper.set(HcPersonAppregItems::getBillIndicator, "1");
                            hcPersonAppregItemsLambdaUpdateWrapper.eq(HcPersonAppregItems::getTjOrderId, orderId);
                            hcPersonAppregItemsService.update(hcPersonAppregItemsLambdaUpdateWrapper);
                        }catch (Exception e){
                            e.printStackTrace();
                            code = "CE";
                            desc += orderId +"处理收费回传出错;";
                        }
                    }
                }
            }
            CostChargeStateResponse costChargeStateResponseObj = new CostChargeStateResponse();
            StringBuilder costChargeStateResponse = new StringBuilder();
            costChargeStateResponse.append("<Response>")
                    .append("<Header>")
                    .append("<Sender>").append(PropertiesUtils.SENDER).append("</Sender>")
                    .append("<Receiver>").append(sender).append("</Receiver>")
                    .append("<SendDate>").append(DateUtils.getDateYYYYMMDDHHMMSS()).append("</SendDate>")
                    .append("<ServiceCode>ACK_COST_CHARGE_STATE</ServiceCode>")
                    .append("<MsgId>").append(msgId).append("</MsgId>")
                    .append("<AuthCode>").append(PropertiesUtils.AUTHCODE).append("</AuthCode>")
                    .append("<Version>1.0</Version>")
                    .append("</Header>")
                    .append("<Body>")
                    .append("<Result>")
                    .append("<Code>").append(code).append("</Code>")
                    .append("<Desc>").append(desc).append("</Desc>")
                    .append("</Result>")
                    .append("</Body>")
                    .append("</Response>");
            WebserviceLog logRes = new WebserviceLog();
            logRes.setUuid(msgId);
            logRes.setInsertDate(new Date());
            logRes.setDataStr(costChargeStateResponse.toString());
            logRes.setDataType("返参");
            logRes.setMethod("单位收费状态回传");
            webserviceLogService.save(logRes);
            costChargeStateResponseObj.setCostChargeStateResponse(costChargeStateResponse.toString());
            return costChargeStateResponseObj;
        }



        //解析xml字符串
        com.healt.cloud.checkup.service.ws.his.costChargeState.CostChargeStateRequest costChargeStateRequest = SoapUtil.xml2Bean(costChargeStateRequestStr, com.healt.cloud.checkup.service.ws.his.costChargeState.CostChargeStateRequest.class);

        // 修改项目收费状态
        costChargeStateRequest.getBody().getChargeState().getItemListVo().getItem();
        // 就诊次数
        String examNo = costChargeStateRequest.getBody().getChargeState().getExamNo();
        if(StringUtils.isEmpty(examNo)){
            code = "CE";
            desc = "体检号不能为空";
        }
        //对体检号进行拆分
        String[] examNoAry = examNo.split("_");
        String personId = examNoAry[0];
        String visitId = examNoAry[1];
        // 患者ID
        String patientId = costChargeStateRequest.getBody().getChargeState().getPatientId();
        // 就诊次数
        //String visitId = costChargeStateRequest.getBody().getChargeState().getVisitId();
        if(StringUtils.isEmpty(visitId)){
            code = "CE";
            desc = "体检次数不能为空";
        }
        // 收费状态
        String feeStatus = costChargeStateRequest.getBody().getChargeState().getFeeStatus();
        if(StringUtils.isEmpty(feeStatus)){
            code = "CE";
            desc = "收费状态不能为空";
        }

        WebserviceLog log = new WebserviceLog();
        log.setUuid(costChargeStateRequest.getHeader().getMsgId());
        log.setInsertDate(new Date());
        log.setDataStr(costChargeStateRequestStr);
        log.setDataType("入参");
        log.setMethod("个人收费状态回传");
        webserviceLogService.save(log);

        //RekId HIS 收费结算号
        String rekid = costChargeStateRequest.getBody().getChargeState().getRekId();
        //修改计费标记 hc_person_appreg_items 里的BILL_INDICATOR，完成1，退和未收费0， 主键HOSPITAL_ID, PERSON_ID, PERSON_VISIT_ID, ITEM_PACK_CODE, ITEM_NO
        List<Item> items = costChargeStateRequest.getBody().getChargeState().getItemListVo().getItem();
        for(Item item : items){
            LambdaUpdateWrapper<HcPersonAppregItems> hcPersonAppregItemsLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            // 根据orderId查询响应的收费项目
            String orderId = item.getOrderId();
            LambdaQueryWrapper<CostDetailBilling> costDetailBillingQueryWrapper = new LambdaQueryWrapper<>();
            costDetailBillingQueryWrapper.eq(CostDetailBilling::getOrderId, orderId);
//			costDetailBillingQueryWrapper.eq(CostDetailBilling::getPatientId, patientId);
//			costDetailBillingQueryWrapper.eq(CostDetailBilling::getPersonVisitId, visitId);
//			costDetailBillingQueryWrapper.eq(CostDetailBilling::getFeeItemId, itemCode);
            List<CostDetailBilling> costDetailBillings = costDetailBillingService.list(costDetailBillingQueryWrapper);
            if(costDetailBillings.size()<1){
                code = "CE";
                desc = "未找到对应的组合项目";
            }else{

                String itemPackCode = costDetailBillings.get(0).getItemPackCode();
                LambdaUpdateWrapper<CostDetailBilling> costDetailBillingLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                HcPersonAppregItems hcPersonAppregItems = new HcPersonAppregItems();
                switch (feeStatus){//0=未收、1=已收、2=已退、3=红冲
                    case "0":
                        hcPersonAppregItems.setBillIndicator("0");
                        //hcPersonAppregItemsLambdaUpdateWrapper.set(HcPersonAppregItems::getBillIndicator, "0");
                        break;
                    case "1":
                        hcPersonAppregItems.setBillIndicator("1");
                        hcPersonAppregItems.setUploadFlag("1");
//                        hcPersonAppregItemsLambdaUpdateWrapper.set(HcPersonAppregItems::getBillIndicator, "1");
//                        hcPersonAppregItemsLambdaUpdateWrapper.set(HcPersonAppregItems::getUploadFlag, "1");
                        //更新COST_DETAIL_BILLING表的收据号REKID字段
                        costDetailBillingLambdaUpdateWrapper.set(CostDetailBilling::getRekid, rekid);
                        costDetailBillingLambdaUpdateWrapper.eq(CostDetailBilling::getOrderId, orderId);
                        costDetailBillingService.update(costDetailBillingLambdaUpdateWrapper);
                        break;
                    case "2":
                        hcPersonAppregItems.setBillIndicator("0");
                        hcPersonAppregItems.setUploadFlag(null);
//                        hcPersonAppregItemsLambdaUpdateWrapper.set(HcPersonAppregItems::getBillIndicator, "0");
//                        hcPersonAppregItemsLambdaUpdateWrapper.set(HcPersonAppregItems::getUploadFlag, null);
                        //更新COST_DETAIL_BILLING的RETURN_REKID 退费收据号为 退费状态回传 的 REKID
                        costDetailBillingLambdaUpdateWrapper.set(CostDetailBilling::getReturnRekid, rekid);
                        costDetailBillingLambdaUpdateWrapper.eq(CostDetailBilling::getOrderId, orderId);
                        costDetailBillingService.update(costDetailBillingLambdaUpdateWrapper);
                        break;
                    case "3":
                        hcPersonAppregItems.setBillIndicator("0");
                        hcPersonAppregItems.setUploadFlag(null);
//                        hcPersonAppregItemsLambdaUpdateWrapper.set(HcPersonAppregItems::getBillIndicator, "0");
//                        hcPersonAppregItemsLambdaUpdateWrapper.set(HcPersonAppregItems::getUploadFlag, null);
                        costDetailBillingLambdaUpdateWrapper.set(CostDetailBilling::getReturnRekid, rekid);
                        costDetailBillingLambdaUpdateWrapper.eq(CostDetailBilling::getOrderId, orderId);
                        costDetailBillingService.update(costDetailBillingLambdaUpdateWrapper);
                        break;
                }
                hcPersonAppregItemsLambdaUpdateWrapper.eq(HcPersonAppregItems::getPersonId, personId);
                hcPersonAppregItemsLambdaUpdateWrapper.eq(HcPersonAppregItems::getPersonVisitId, visitId);
                hcPersonAppregItemsLambdaUpdateWrapper.eq(HcPersonAppregItems::getItemPackCode, itemPackCode);

                if(!hcPersonAppregItemsService.update(hcPersonAppregItems, hcPersonAppregItemsLambdaUpdateWrapper)){
                    code = "CE";
                    desc = "未找到对应的诊疗项目";
                }
            }
        }
        //收费记录状态回传增加了两个表HEALTHCHECKUP.COST_CHARGE_STATE_MASTER;与HEALTHCHECKUP.COST_CHARGE_STATE_DETAIL;
        CostChargeStateMaster costChargeStateMaster = new CostChargeStateMaster();
        costChargeStateMaster.setHospitalId("0001");
        costChargeStateMaster.setUnitId(costChargeStateRequest.getBody().getChargeState().getExamCompanyNo());
        //costChargeStateMaster.setUnitVisitId();
        costChargeStateMaster.setPersonId(personId);
        //costChargeStateMaster.setPersonVisitId(Integer.parseInt(costChargeStateRequest.getBody().getChargeState().getVisitId()));
        costChargeStateMaster.setPersonVisitId(Integer.parseInt(visitId));
        costChargeStateMaster.setExamtype(costChargeStateRequest.getBody().getChargeState().getExamType());
        costChargeStateMaster.setExamno(costChargeStateRequest.getBody().getChargeState().getExamNo());
        costChargeStateMaster.setExamcompanyno(costChargeStateRequest.getBody().getChargeState().getExamCompanyNo());
        costChargeStateMaster.setExamcompanyname(costChargeStateRequest.getBody().getChargeState().getExamCompanyName());
        costChargeStateMaster.setPatientid(costChargeStateRequest.getBody().getChargeState().getPatientId());
        costChargeStateMaster.setVisitid(visitId);
        costChargeStateMaster.setFeestatus(costChargeStateRequest.getBody().getChargeState().getFeeStatus());
        costChargeStateMaster.setOperator(costChargeStateRequest.getBody().getChargeState().getOperator());
        costChargeStateMaster.setOperatetime(DateUtils.parseString(costChargeStateRequest.getBody().getChargeState().getOperateTime(), "yyyyMMddHHmmss"));
        costChargeStateMaster.setRekid(costChargeStateRequest.getBody().getChargeState().getRekId());
        costChargeStateMaster.setInvoiceid(costChargeStateRequest.getBody().getChargeState().getInvoiceId());
        costChargeStateMaster.setApplydoctorcode(costChargeStateRequest.getBody().getChargeState().getApplyDoctorCode());
        costChargeStateMaster.setApplydoctorname(costChargeStateRequest.getBody().getChargeState().getApplyDoctorName());
        costChargeStateMaster.setExecorgcode(costChargeStateRequest.getBody().getChargeState().getExecOrgCode());
        costChargeStateMaster.setExecorgname(costChargeStateRequest.getBody().getChargeState().getExecOrgName());
        costChargeStateMaster.setExecflag(costChargeStateRequest.getBody().getChargeState().getExecFlag());
        costChargeStateMaster.setAreacode(costChargeStateRequest.getBody().getChargeState().getAreaCode());
        try {
            costChargeStateMasterService.save(costChargeStateMaster);
        }catch (Exception e){
            code = "CE";
            if(e.getMessage().contains("ORA-00001")){
                desc = "违反唯一约束条件";
            }
        }
        for(Item item : items){
            CostChargeStateDetail costChargeStateDetail = new CostChargeStateDetail();
            costChargeStateDetail.setHospitalId("0001");
            costChargeStateDetail.setUnitId("");
            //costChargeStateDetail.setUnitVisitId("");
            costChargeStateDetail.setPersonId(personId);
            costChargeStateDetail.setPersonVisitId(Integer.parseInt(visitId));
            costChargeStateDetail.setExamtype(costChargeStateMaster.getExamtype());
            costChargeStateDetail.setExamno(costChargeStateMaster.getExamno());
            costChargeStateDetail.setExamcompanyname(costChargeStateMaster.getExamcompanyname());
            costChargeStateDetail.setExamcompanyno(costChargeStateMaster.getExamcompanyno());
            costChargeStateDetail.setPatientid(costChargeStateMaster.getPatientid());
            costChargeStateDetail.setVisitid(costChargeStateMaster.getVisitid());
            costChargeStateDetail.setFeestatus(costChargeStateMaster.getFeestatus());
            costChargeStateDetail.setRekid(costChargeStateMaster.getRekid());
            costChargeStateDetail.setItemcode(item.getClinicItemId());
            costChargeStateDetail.setItemname(item.getClinicItemName());
            costChargeStateDetail.setItemunit(item.getUnit());
            costChargeStateDetail.setUnitprice(item.getUnitPrice());
            costChargeStateDetail.setNum(new BigDecimal(item.getNum()));
            //costChargeStateDetail.setItemprice(new BigDecimal(item.getUnitPrice()).multiply(costChargeStateDetail.getNum()));
            try {
                costChargeStateDetailService.save(costChargeStateDetail);
            }catch (Exception e){
                code = "CE";
                if(e.getMessage().contains("ORA-00001")){
                    desc = "违反唯一约束条件";
                }
            }
        }

        CostChargeStateResponse costChargeStateResponseObj = new CostChargeStateResponse();
        StringBuilder costChargeStateResponse = new StringBuilder();
        costChargeStateResponse.append("<Response>")
                .append("<Header>")
                .append("<Sender>").append(PropertiesUtils.SENDER).append("</Sender>")
                .append("<Receiver>").append(costChargeStateRequest.getHeader().getSender()).append("</Receiver>")
                .append("<SendDate>").append(DateUtils.getDateYYYYMMDDHHMMSS()).append("</SendDate>")
                .append("<ServiceCode>ACK_COST_CHARGE_STATE</ServiceCode>")
                .append("<MsgId>").append(costChargeStateRequest.getHeader().getMsgId()).append("</MsgId>")
                .append("<AuthCode>").append(PropertiesUtils.AUTHCODE).append("</AuthCode>")
                .append("<Version>1.0</Version>")
                .append("</Header>")
                .append("<Body>")
                .append("<Result>")
                .append("<Code>").append(code).append("</Code>")
                .append("<Desc>").append(desc).append("</Desc>")
                .append("</Result>")
                .append("</Body>")
                .append("</Response>");
        WebserviceLog logRes = new WebserviceLog();
        logRes.setUuid(costChargeStateRequest.getHeader().getMsgId());
        logRes.setInsertDate(new Date());
        logRes.setDataStr(costChargeStateResponse.toString());
        logRes.setDataType("返参");
        logRes.setMethod("个人收费状态回传");
        webserviceLogService.save(logRes);
        costChargeStateResponseObj.setCostChargeStateResponse(costChargeStateResponse.toString());
        return costChargeStateResponseObj;
    }

}
