package com.bestpay.paycenter.entry.http.controller.biz;

import com.bestpay.paycenter.entry.http.controller.ccb.debit.CcbDebitUtil;
import com.solab.iso8583.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 银行路由
 * @author 李杰
 */

@Controller
@Slf4j
public class BankRouter {
    @RequestMapping(value = "/bankRouter", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String messageRelay (HttpServletRequest request) {
        log.info("银行路由--进入");
        try {
            String ip = request.getParameter("ip");
            String port = request.getParameter("port");
            String message = request.getParameter("message");

            log.info("银行路由--发送给银行的ip地址为:{}", ip);
            log.info("银行路由--发送给银行的port端口号为:{}", port);
            log.info("银行路由--发送给银行的字节数组转换为十六进制字符串的报文为:{}", message);

            byte[] requestData = ConvertUtil.hexStringToBytes(message); // 将字符串转换成字节数组

            byte[] responseData = send( ip, port, requestData );
            String responseDateStr = ConvertUtil.bytesToHexString(responseData);
            log.info("银行路由--从银行返回的字节数组转换为十六进制字符串的报文为:{}", responseDateStr);

            return responseDateStr;
        } catch (Exception e) {
            log.info("银行路由--发生异常:{}", e);
        }
        return "";
    }

    /**
     * socket发送,返回响应byte[]
     */
    public static byte[] send(String ip, String sPort, byte[] datas) throws Exception {
        int port = Integer.parseInt(sPort);
        InetSocketAddress endpoint = new InetSocketAddress(ip, port);
        Socket socket = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            socket = new Socket();
            socket.setSoLinger(true, 2); // 设置发送逗留时间2秒
            socket.setSoTimeout(200000); // 设置InputStream上调用 read()阻塞超时时间200秒
            socket.setSendBufferSize(32 * 1024); // 设置socket发包缓冲为32k；
            socket.setReceiveBufferSize(32 * 1024); // 设置socket底层接收缓冲为32k
            socket.setTcpNoDelay(true); // 关闭Nagle算法.立即发包
            socket.connect(endpoint); // 连接服务器
            out = socket.getOutputStream(); // 获取输出输入流
            in = socket.getInputStream();
            out.write(datas);
            out.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "GBK"));
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] bs = outStream.toByteArray();
            if (bs == null) {
                return null;
            }
            return bs;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
        }
    }


}
