package com.convertzone;

import java.util.*;
import java.util.Date;
import java.sql.*;
import java.io.*;
import java.text.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class JdbcViewer
 */
@WebServlet("/JdbcViewer")
public class JdbcViewer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Map<String, Object> poolMap=new HashMap<String, Object>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JdbcViewer() {
		super();
		// TODO Auto-generated constructor stub

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		response.setCharacterEncoding("UTF-8");// 设置将字符以"UTF-8"编码输出到客户端浏览器
		// 通过设置响应头控制浏览器以UTF-8的编码显示数据，如果不加这句话，那么浏览器显示的将是乱码
		response.setHeader("content-type", "text/html;charset=UTF-8");

		List<String> sqlList=null;
		PrintWriter out=null;

		try {
			SqlRun sqlRun=new SqlRun(poolMap);
			out = response.getWriter();
			sqlList = sqlRun.sql2List(request.getParameter("sql"));
			int iNo=0;
			for (String sql : sqlList) {
				sqlRun.runSql(request,out,sql,++iNo);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			out.write("<h3>错误：</h3>");
			out.write(e.toString());
		}finally {
			if(out!=null){
				out.close();
				out=null;
			}
		}
	}


	public static void main(String[] args) throws Exception {

	}
}
