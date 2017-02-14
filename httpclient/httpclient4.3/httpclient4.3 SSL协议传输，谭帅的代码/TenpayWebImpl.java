package com.bestpay.paycenter.gateway.bank.service.impl.tenpay;

import com.bestpay.paycenter.commons.model.Response;
import com.bestpay.paycenter.commons.util.DateUtil;
import com.bestpay.paycenter.commons.util.MD5Util;
import com.bestpay.paycenter.gateway.bank.beans.TranStatusConst;
import com.bestpay.paycenter.gateway.bank.models.*;
import com.bestpay.paycenter.gateway.bank.service.BaseBankImpl;
import com.bestpay.paycenter.gateway.bank.service.IBankPayResponse;
import com.bestpay.paycenter.gateway.bank.utils.HttpClientUtil;
import com.bestpay.paycenter.gateway.bank.utils.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 财付通
 * Author tanshuai
 * Date 2013-4-24 下午3:02:10
 * Version 1.0
 * See (201107正确版本) 财付通支付网关商户开发指南.doc
 */
@Service
@Slf4j
public class TenpayWebImpl extends BaseBankImpl implements IBankPayResponse {

    PropertiesConfiguration config;

    @PostConstruct
    public void init() throws ConfigurationException {
        log.info("TenpayWebImpl init start");
        config = new PropertiesConfiguration("bank/tenpay/tenpay.properties");
        log.info("TenpayWebImpl init end");
    }

    @Override
    public Response<ForwardInfo> webPay(BankPay params) {
        TreeMap<String, String> reqMap = new TreeMap<String, String>();
        reqMap.put("input_charset", config.getString("INPUT_CHARSET"));
        reqMap.put("bank_type", StringUtils.defaultString(params.getBankCode())); // 银行类型
        reqMap.put("body", StringUtils.defaultIfEmpty(params.getGoodsName(), "天翼电子商务有限公司")); // 商品描述
        reqMap.put("return_url", config.getString("RETURN_URL"));
        reqMap.put("notify_url", config.getString("NOTIFY_URL"));
        reqMap.put("partner", config.getString("PARTNER"));
        reqMap.put("out_trade_no", params.getPayReqNO()); // 商家订单号
        reqMap.put("total_fee", String.valueOf(params.getTransAmt())); // 商品金额，分
        reqMap.put("fee_type", config.getString("FEE_TYPE"));

        reqMap.put("spbill_create_ip", params.getUserIp());// 用户IP，不能为空
        reqMap.put("sign", createSign(reqMap.entrySet(), config.getString("KEY"), config.getString("INPUT_CHARSET")));

        log.info("财付通支付请求参数：{}", reqMap.toString());
        return setResponse(new ForwardInfo(config.getString("PAY_URL"), reqMap));
    }

    @Override
    public Response<BankResponse> query(BankQuery params) {
        TreeMap<String, String> reqMap = new TreeMap<String, String>();
        reqMap.put("input_charset", config.getString("INPUT_CHARSET"));
        reqMap.put("partner", config.getString("PARTNER"));
        reqMap.put("out_trade_no", params.getTranReqNo()); // 商家订单号
        reqMap.put("sign", createSign(reqMap.entrySet(), config.getString("KEY"), config.getString("INPUT_CHARSET")));
        log.info("请求参数:{}", reqMap);
        String res = HttpClientUtil.invoke(config.getString("QUERY_URL"), reqMap);
        log.info("返回参数:{}", res);
        return getQueryBankResponseResponse(params, res);
    }

    @Override
    public Response<BankResponse> refund(BankRefund params) {
        String tranAmt = String.valueOf(params.getTranAmt());
        TreeMap<String, String> reqMap = new TreeMap<String, String>();
        reqMap.put("partner", config.getString("PARTNER"));
        reqMap.put("out_trade_no", params.getOldTranReqNo()); // 商家订单号
        reqMap.put("transaction_id", params.getOldTranRespNo()); // 财付通订单号
        reqMap.put("out_refund_no", params.getTranReqNo());
        reqMap.put("total_fee", String.valueOf(params.getOldTranAmt()));
        reqMap.put("refund_fee", tranAmt);
        reqMap.put("op_user_id", config.getString("TENPAY_USERID"));
        reqMap.put("op_user_passwd", config.getString("TENPAY_PASSWORD"));
        reqMap.put("sign", createSign(reqMap.entrySet(), config.getString("KEY"), config.getString("INPUT_CHARSET")));

        StringBuffer sValue = new StringBuffer(config.getString("REFUND_URL"));
        sValue.append("?");
        Set<Map.Entry<String, String>> set = reqMap.entrySet();
        for (Map.Entry<String, String> entry : set) {
            sValue.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sValue.deleteCharAt(sValue.length() - 1);
        String reqContent = sValue.toString();
        log.info("请求参数：{}", reqContent);

        String res = HttpClientUtil.invokeGetHttps(reqContent, config.getString("CERT_FILE"), config.getString("CERT_PASSWORD"), config.getString("TRUST_FILE"), StringUtils.EMPTY);
        log.info("响应结果：{}", res);

        return getRefundBankResponseResponse(params, res);
    }

    @Override
    public BankResponse getPgCallResponse(Map<String, String> params) {
        return getResponse(params);
    }

    @Override
    public BankResponse getNotifyResponse(Map<String, String> params) {
        BankResponse response = getResponse(params);
        response.setRespMsg("success");
        return response;
    }

    public BankResponse getResponse(Map<String, String> params) {
        if (!isTenpaySign(params)) {
            log.error("签名验证失败！");
            return null;
        }

        String upReqTranSeq = params.get("out_trade_no");
        String bankTranNo = params.get("transaction_id");
        String bankTranDate = params.get("time_end");
        String respCode = params.get("trade_state");
        String respDesc = params.get("pay_info");
        String tranStatus = "0".equals(respCode) ? TranStatusConst.SUCCESS : TranStatusConst.FAIL;
        String tranAmt = params.get("total_fee");
        return BankResponse.getRealResult(upReqTranSeq, bankTranNo, bankTranDate, respCode, respDesc, tranStatus, tranAmt);
    }

    public boolean isTenpaySign(Map<String, String> params) {
        SortedMap<String, String> parameters = new TreeMap<String, String>(params);
        Set<Map.Entry<String, String>> es = parameters.entrySet();

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : es) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (!"sign".equals(k) && v != null && !"".equals(v))
                sb.append(k).append("=").append(v).append("&");
        }
        sb.append("key=").append(config.getString("KEY"));
        // 因编码不确定，所以验证两个编码
        String tenpaySign = params.get("sign");
        return StringUtils.equalsIgnoreCase(tenpaySign, MD5Util.MD5Encode(sb.toString(), "gbk"))
                || StringUtils.equalsIgnoreCase(tenpaySign, MD5Util.MD5Encode(sb.toString(), "UTF-8"));
    }

    private String createSign(Set<Map.Entry<String, String>> set, String key, String inputCharset) {
        String result = "";
        StringBuffer sign = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : set) {
                sign.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sign.append("key=").append(key);

            String s = sign.toString();
            log.debug("加密串：{}", s);
            result = DigestUtils.md5Hex(s.getBytes(inputCharset)).toUpperCase();
        } catch (Exception e) {
            log.error("加密异常", e);
        }
        return result;
    }

    public Response<BankResponse> getQueryBankResponseResponse(BankQuery params, String res) {
        Map<String, Object> resMap = XmlUtil.parseXml(res);
        String retCode = String.valueOf(resMap.get("retcode"));
        String retMsg = String.valueOf(resMap.get("retmsg"));
        String tranStatus = String.valueOf(resMap.get("trade_state"));
        String transAmt = String.valueOf(resMap.get("total_fee"));
        String transDate = String.valueOf(resMap.get("time_end"));
        String transNo = String.valueOf(resMap.get("transaction_id"));
        String ourTranStatus = TranStatusConst.REQ;
        if ("0".equals(retCode)) {
            //0表示成功，其它未定义
            if (!"0".equals(tranStatus)) {
                //支付结果状态码,0表示成功,其它为失败
                ourTranStatus = TranStatusConst.FAIL;
            } else if (verifySign(resMap)) {
                ourTranStatus = TranStatusConst.SUCCESS;
            } else {
                retMsg += "验签失败";
            }
        }
        return setResponse(BankResponse.getAcceptResult(params.getTranReqNo(), transNo, transDate, retCode, retMsg, ourTranStatus, transAmt));
    }

    private boolean verifySign(Map<String, Object> resMap) {
        SortedMap<String, Object> parameters = new TreeMap<String, Object>(resMap);
        Set<Map.Entry<String, Object>> es = parameters.entrySet();
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, Object> entry : es) {
            String k = entry.getKey();
            String v = String.valueOf(entry.getValue());
            if (!"sign".equals(k) && v != null && !"".equals(v))
                sb.append(k).append("=").append(v).append("&");
        }
        sb.append("key=").append(config.getString("KEY"));

        String tenpaySign = String.valueOf(resMap.get("sign"));
        // 因编码不确定，所以验证两个编码
        if (StringUtils.equalsIgnoreCase(tenpaySign, MD5Util.MD5Encode(sb.toString(), "gbk"))
                || StringUtils.equalsIgnoreCase(tenpaySign, MD5Util.MD5Encode(sb.toString(), "UTF-8"))) {
            return true;
        }
        return false;
    }

    public Response<BankResponse> getRefundBankResponseResponse(BankRefund params, String res) {
        Map<String, Object> resMap = XmlUtil.parseXml(res);
        String retCode = String.valueOf(resMap.get("retcode"));
        String retMsg = String.valueOf(resMap.get("retmsg"));
        String transNo = StringUtils.EMPTY;
        String ourTranStatus;
        //状态码，0表示成功，其他未定义
        if ("0".equals(retCode)) {
            transNo = resMap.get("refund_id").toString();
            /*退款状态：1:待审批 2:审批流程中3:审批失败4:退款成功 5:退款失败6:资料重填7:转入代发
            8:暂不处理9:退款流程中10:转入代发成功11:转入代发中12:分账回退中13:分帐回退成功*/
            String refundStatus = resMap.get("refund_status").toString();
            if ( verifySign(resMap)){
                if("5".equals(refundStatus)||"3".equals(refundStatus)) {
                    ourTranStatus = TranStatusConst.FAIL;
                } else{
                    ourTranStatus = TranStatusConst.SUCCESS;
                }
            } else {
               log.error("验签失败");
                ourTranStatus = TranStatusConst.SUCCESS;
            }
        }else {
            ourTranStatus  = TranStatusConst.FAIL;
        }
        return setResponse(BankResponse.getRealResult(params.getTranReqNo(), transNo,DateUtil.getCurrent(), retCode, retMsg, ourTranStatus, String.valueOf(params.getTranAmt())));
    }

}
