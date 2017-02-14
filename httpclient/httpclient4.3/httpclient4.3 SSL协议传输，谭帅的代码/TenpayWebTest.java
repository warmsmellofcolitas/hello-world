package com.bestpay.paycenter.bank.fastpay.tenpay;

import com.bestpay.paycenter.commons.util.MD5Util;
import com.bestpay.paycenter.gateway.bank.models.BankQuery;
import com.bestpay.paycenter.gateway.bank.models.BankRefund;
import com.bestpay.paycenter.gateway.bank.service.impl.tenpay.TenpayWebImpl;
import com.bestpay.paycenter.gateway.bank.utils.HttpClientUtil;
import org.apache.commons.lang.StringUtils;

/*
     * Created with IntelliJ IDEA.
     * User: t
     * Date: 14-6-5
     * Time: 下午8:33
     * To change this template use File | Settings | File Templates
 */


public class TenpayWebTest {

    @org.junit.Test
    public void query() throws Exception {
        TenpayWebImpl tenpayWeb = new TenpayWebImpl();
//        tenpayWeb.init();
//        HttpClientUtil.init();
        BankQuery params = new BankQuery();
        params.setTranReqNo("1000237388");
//        System.out.println(tenpayWeb.query(params));
        System.out.println("1111217225001201406128316998".substring(10,18));
    }

    @org.junit.Test
    public void queryResponse() throws Exception {
        TenpayWebImpl tenpayWeb = new TenpayWebImpl();
        tenpayWeb.init();
        HttpClientUtil.init();
        BankQuery params = new BankQuery();
        params.setTranReqNo("1000237388");
        String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root>\n" +
                "  <agentid />\n" +
                "  <bank_billno />\n" +
                "  <bank_type>BL</bank_type>\n" +
                "  <buyer_alias />\n" +
                "  <cash_ticket_fee>0</cash_ticket_fee>\n" +
                "  <discount>0</discount>\n" +
                "  <fee_type>1</fee_type>\n" +
                "  <input_charset>UTF-8</input_charset>\n" +
                "  <is_refund>false</is_refund>\n" +
                "  <is_split>false</is_split>\n" +
                "  <out_trade_no>1000237388</out_trade_no>\n" +
                "  <partner>1217225001</partner>\n" +
                "  <product_fee>5000</product_fee>\n" +
                "  <retcode>0</retcode>\n" +
                "  <retmsg />\n" +
                "  <rmb_total_fee />\n" +
                "  <sign>251F66BFA19D8FB6951266F1BF90CF2E</sign>\n" +
                "  <sign_key_index>1</sign_key_index>\n" +
                "  <sign_type>MD5</sign_type>\n" +
                "  <time_end>20140611132641</time_end>\n" +
                "  <total_fee>5000</total_fee>\n" +
                "  <trade_mode>1</trade_mode>\n" +
                "  <trade_state>0</trade_state>\n" +
                "  <transaction_id>1217225001201406110646690073</transaction_id>\n" +
                "  <transport_fee>0</transport_fee>\n" +
                "</root>";
        System.out.println(tenpayWeb.getQueryBankResponseResponse(params, res));
    }


    @org.junit.Test
    public void refund() throws Exception {

        String urlContent = "https://mch.tenpay.com/refundapi/gateway/refund.xml?op_user_id=1217225001008&op_user_passwd=sys134567&out_refund_no=1000233632&out_trade_no=1000233377&partner=1217225001&refund_fee=0&sign=5A86014943F729CEE6CFD7B07F393477&total_fee=0&transaction_id=12172250012040631705214";
        String keyPath = "C:/Users/t/bestpay/cert/tenpay/1217225001_20130923164950.pfx";
        String keyPwd = "1217225001";
        String trustPath = "C:/Users/t/bestpay/cert/tenpay/tenpay_cacert.jks";
        String trustPwd = StringUtils.EMPTY;
        try {
            System.out.println(HttpClientUtil.invokeGetHttps(urlContent, keyPath, keyPwd, trustPath, trustPwd));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    @org.junit.Test
    public void refundRepsponce() throws Exception {

        String res = "<root>\n" +
                "  <input_charset>GBK</input_charset>\n" +
                "  <out_refund_no>1000237691</out_refund_no>\n" +
                "  <out_trade_no>1000237690</out_trade_no>\n" +
                "  <partner>1217225001</partner>\n" +
                "  <reccv_user_name />\n" +
                "  <recv_user_id />\n" +
                "  <refund_channel>1</refund_channel>\n" +
                "  <refund_fee>1</refund_fee>\n" +
                "  <refund_id>1111217225001201406128316998</refund_id>\n" +
                "  <refund_status>9</refund_status>\n" +
                "  <retcode>0</retcode>\n" +
                "  <retmsg />\n" +
                "  <sign>FE59EB7657BC78D13F976F16B3EBDCCC</sign>\n" +
                "  <sign_key_index>1</sign_key_index>\n" +
                "  <sign_type>MD5</sign_type>\n" +
                "  <transaction_id>1217225001201406120648541058</transaction_id>\n" +
                "</root>";
//        TenpayWebImpl tenpayWeb = new TenpayWebImpl();
//        tenpayWeb.init();
//        BankRefund params = new BankRefund();
//        System.out.println(tenpayWeb.getRefundBankResponseResponse(params, res));
        String a = "input_charset=GBK&out_refund_no=1000237691&out_trade_no=1000237690&partner=1217225001&refund_channel=1&refund_fee=1&refund_id=1111217225001201406128316998&refund_status=9&retcode=0&sign_key_index=1&sign_type=MD5&transaction_id=1217225001201406120648541058&key=f0a7ef116fd6f644cde965fec8dbdec2";
        String b = "FE59EB7657BC78D13F976F16B3EBDCCC"      ;
        System.out.println(b.equals(MD5Util.MD5Encode(a,"GBK")));
    }
}
