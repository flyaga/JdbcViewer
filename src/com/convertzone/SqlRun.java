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

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.convertzone.lib.FileLib;
import com.convertzone.lib.FileSearchNoGui;

public class SqlRun {
	private Connection conn=null;
	private Statement stmt = null;
	private String resultFile="";
	private String downloadUrl="";
	private String isOverDownloadLimit;
	private String isDownload="off";
	private HttpServletRequest request=null;
	
	Map<String, Object> poolMap=null;
	
	public SqlRun(Map<String, Object> poolMap){
		this.poolMap=poolMap;
	}
	
	public void runSql(HttpServletRequest request,
			PrintWriter out,String sql,int iNo) throws ServletException, IOException {	
		this.request=request;
		Long start = System.currentTimeMillis();
		Long stop = (long) 0;
		List results = new ArrayList();
		
		int limit = 0;
		int downloadLimit = 0;
		try {
			limit = Integer.parseInt(request.getParameter("limit"));
			downloadLimit = Integer.parseInt(request.getParameter("downloadLimit"));
			isDownload=request.getParameter("isDownload");
			getConnection();

			results = queryForList(sql,iNo);

			stop = System.currentTimeMillis();

			out.write("<h3>" + FileLib.DateToStr(new Date()) + "查询结果如下：</h3>");
			out.write("查询sql：" + sql + "<br>");
			if ((results.size() + 1) > limit) {
				out.write("警告，结果数量太多，只能够显示" + limit + "行，请修改显示行数或者下载进行查看<br>");
			}
			if(isDownload!=null){
				if (isOverDownloadLimit.equals("1")) {
					out.write("警告，结果数量太多，只能够下载" + downloadLimit
							+ "行，请修改下载行数后重新查询和下载");
				} else
					out.write("结果总数=" + (results.size() - 1));
				out.write("，<a target='_blank' href='" + downloadUrl
					+ "'>结果下载</a><br>");
			}else{
				out.write("结果总数=" + (results.size() - 1));
			}

			boolean isEnd = false;
			boolean isBreak = false;
			for (int i = 0; i < results.size(); i++) {
				List aList = (ArrayList) results.get(i);
				if (i >= limit) {
					isEnd = true;
					isBreak = true;
				}
				if (i == results.size() - 1)
					isEnd = true;
				if (i == 0) {
					out.write("<table id=\"result"+iNo+"\" class=\"table table-bordered table-striped\">");
					out.write("<thead><tr>");
				} else if (isEnd) {
					out.write("<tr>");
				} else {
					out.write("<tr>");
				}
				for (int j = 0; j < aList.size(); j++) {
					String sTemp = "";
					if (aList.get(j) != null)
						sTemp = aList.get(j).toString();
					if (i == 0) {
						if (j == 0)
							out.write("<th>NO.</th>");
						out.write("<th>" + sTemp + "</th>");
					} else if (isEnd) {
						if (j == 0)
							out.write("<td>" + i + "</td>");
						out.write("<td>" + sTemp + "</td>");
					} else {
						if (j == 0)
							out.write("<td>" + i + "</td>");
						out.write("<td>" + sTemp + "</td>");
					}
				}
				if (i == 0) {
					out.write("</tr></thead><tbody>");
				} else if (isEnd) {
					out.write("</tbody></table>");
				} else {
					out.write("</tr>");
				}
				if (isBreak)
					break;
			}

			out.write(MessageFormat.format("查询耗时={0}，显示耗时={1}，一共耗时={2}<br>",
					(stop - start) * 1.0 / 1000 + "s",
					(System.currentTimeMillis() - stop) * 1.0 / 1000 + "s",
					(System.currentTimeMillis() - start) * 1.0 / 1000 + "s"));

			saveSetting();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			out.write("<h3>错误：</h3>");
			out.write(e.toString());
			return;
		}
		finally{
			try{
				closeConnection();
			}catch(Exception e){
				e.printStackTrace();
				out.write(e.toString());
				return;
			}
		}
	}

	private void saveSetting() throws Exception {
		String settingFile=request.getParameter("settingFile");
		Properties p = new Properties(); 
		for(Object entryObj:request.getParameterMap().entrySet()){
			Map.Entry<String,String[]>  entry = (Map.Entry<String,String[]>)entryObj;
			p.setProperty(entry.getKey(),entry.getValue()[0]);
		}
		FileOutputStream out = new FileOutputStream(settingFile);
		p.store(out,settingFile);
		out.close();
	}
	
	private List queryForList(String sql,int iNo) throws Exception {
		ResultSet rs=null;
		FileWriter writer = null;
		List list = new ArrayList();
		try {			
			rs= stmt.executeQuery(sql);
			if (rs == null)
				return Collections.EMPTY_LIST;

			if(isDownload!=null){
				resultFile = MessageFormat.format("result-{0}-{1}.csv",Thread.currentThread().hashCode(),iNo);
				downloadUrl = request.getContextPath() + "/" + resultFile;

				File dest = null;
				dest = new File(request.getSession().getServletContext().getRealPath(resultFile));
				writer = new FileWriter(dest);
			}
			
			int downloadLimit = Integer.parseInt(request
					.getParameter("downloadLimit"));
			isOverDownloadLimit = "0";

			ResultSetMetaData md = rs.getMetaData(); // 得到结果集(rs)的结构信息，比如字段数、字段名等
			List colList = new ArrayList();
			
			int columnCount = md.getColumnCount(); // 返回此 ResultSet 对象中的列数
			for (int i = 1; i <= columnCount; i++) {
				String sTemp = md.getColumnName(i);
				colList.add(sTemp);
				
				if(isDownload!=null){
					if (sTemp.indexOf(",") > 0)
						writer.write("\"" + sTemp + "\"");
					else
						writer.write(sTemp);
					if (i != columnCount)
						writer.write(",");
					else
						writer.write("\n");
				}
			}
			list.add(colList);

			iNo = 0;
			while (rs.next()) {
				iNo++;
				if (iNo > downloadLimit) {
					isOverDownloadLimit = "1";
					break;
				}
				colList = new ArrayList();
				for (int i = 1; i <= columnCount; i++) {
					String sTemp = "";
					if (rs.getObject(i) != null)
						sTemp = rs.getObject(i).toString();
					colList.add(sTemp);
					
					if(isDownload!=null){
						if (sTemp.indexOf(",") > 0)
							writer.write("\"" + sTemp + "\"");
						else
							writer.write(sTemp);
						if (i != columnCount)
							writer.write(",");
						else
							writer.write("\n");
					}
				}
				list.add(colList);
			}
			if(isDownload!=null){
				writer.flush();
			}
		} finally {
			if(writer!=null){
				writer.close();
				writer=null;
			}
			if(rs!=null){
				rs.close();
				rs=null;
			}	
		}
		
		return list;
	}

	private void getConnection() throws Exception {
		String driverClassName = request.getParameter("driverClassName");
		String url = request.getParameter("url");
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		String key=url+"-"+userName+"-"+password;
		
		DataSource dataSource=null;
		
		if(poolMap.containsKey(key)){
			dataSource=(DataSource)poolMap.get(key);
		}
		else{
			PoolProperties pool=new PoolProperties();
			pool.setUrl(url);
			pool.setUsername(userName);
			pool.setPassword(password);
			pool.setDriverClassName(driverClassName);
			
			//pool.set
			dataSource=new DataSource();
			dataSource.setPoolProperties(pool);
			
			poolMap.put(key, dataSource);
		}
		
		conn=dataSource.getConnection();
		stmt = conn.createStatement();
	}

	private void closeConnection() throws Exception {
		if (stmt != null){
			stmt.close();
			stmt=null;
		}
		if (conn != null){
			conn.close();
			conn=null;
		}
	}
	
	private void getConnection1() throws Exception {
		if(conn!=null){
			if(!conn.isClosed())
				return;
			conn.close();
			conn=null;
		}			
		
		String driverClassName = request.getParameter("driverClassName");
		String url = request.getParameter("url");
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");

		Class.forName(driverClassName);

		DriverManager.setLoginTimeout(0);
		conn = DriverManager.getConnection(url, userName, password);
		stmt = conn.createStatement();
	}

	private void closeConnection1() throws Exception {
		if (stmt != null){
			stmt.close();
			stmt=null;
		}
		if (conn != null){
			conn.close();
			conn=null;
		}
	}
	
	public static List<String> sql2List(String sql) throws Exception { 
		List<String> sqlList = new ArrayList<String>();   
		String[] sqlArr = sql.split(";");
		for (int i = 0; i < sqlArr.length; i++) {  
			sql = sqlArr[i].replaceAll("--.*", "").trim();  
			if (!sql.equals("")) {  
				sqlList.add(sql);  
			}  
		}
		return sqlList;
	}

	public void main(String[] args) throws Exception {
		String executePath="";//request.getSession().getServletContext().getRealPath(".");
		//scan all set*.ini from execute path
		List<String> aList = FileSearchNoGui.getListFiles(executePath,"set*.ini", false);
		
		//if no file is found, then new set-threadno.ini, save default content
		String iniFileName=executePath+MessageFormat.format("set-{0}.csv",Thread.currentThread().hashCode());
		if(aList.isEmpty())
			aList.add(iniFileName);
		else
			iniFileName=aList.get(0);
		
		//default setxxx.ini is checked
	}
}
