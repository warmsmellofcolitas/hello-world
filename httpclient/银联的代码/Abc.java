package com.mipo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Abc extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("xxx");
		Enumeration e1 = req.getParameterNames();
		while (e1.hasMoreElements()) {
			String key = (String) e1.nextElement();
			String valu = req.getParameter(key);
			System.out.println("key:"+key);
			System.out.println("val:"+valu);
		}
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		
		out.write("jackÄãºÃ");
		out.flush();
	}
}
