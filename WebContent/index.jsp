<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*,java.text.*,java.io.*,com.convertzone.lib.FileSearchNoGui"
    pageEncoding="UTF-8"%>
<%!
	public boolean SaveSetIni(String settingFile,String driverClassName,String url,String userName,String password,
		int limit,int downloadLimit,String isDownload,String sql)throws Exception{
		Properties p = new Properties(); 
		p.setProperty("downloadLimit",String.valueOf(downloadLimit));
		p.setProperty("limit",String.valueOf(limit));
		p.setProperty("url",url);
		p.setProperty("userName",userName);
		p.setProperty("driverClassName",driverClassName);
		p.setProperty("sql",sql);
		if(isDownload!=null)
			p.setProperty("isDownload",isDownload);
		FileOutputStream out = new FileOutputStream(settingFile);
		p.store(out,settingFile);
		out.close();	
		return true;
	}
	
	String driverClassName="org.apache.hive.jdbc.HiveDriver";  
    String url="jdbc:hive2://hadoop01:8083/default";  
    String userName="hive";
    String password="hive";
    int limit=1000;
    int downloadLimit=10000;
    String isDownload=null;
    String sql="";
 %>
 <% 
	String executePath=request.getSession().getServletContext().getRealPath(".");
	//scan all set*.ini from execute path
	List<String> setIniList = FileSearchNoGui.getListFiles(executePath,"set-*.ini", false);
		
	//if no file is found, then new set-threadno.ini, save default content
	String settingFile=executePath+"set-1.ini";
	if(setIniList.isEmpty()){
		setIniList.add(settingFile);
		SaveSetIni(settingFile,driverClassName,url,userName,password,limit,
			downloadLimit,isDownload,sql);
	}else
		settingFile=setIniList.get(0);
			
	Properties p = new Properties();
	InputStream in=null;
	try{ 
		in = new FileInputStream(settingFile);
		p.load(in);
		in.close();
		
		if(p.containsKey("driverClassName")){  
			driverClassName = p.getProperty("driverClassName").trim();  
		}
	
		if(p.containsKey("url")){  
			url = p.getProperty("url").trim();  
		}
	
		if(p.containsKey("userName")){  
			userName = p.getProperty("userName").trim();  
		}
	
		if(p.containsKey("password")){  
			password = p.getProperty("password").trim();  
		}
	
		if(p.containsKey("limit")){  
			limit = Integer.parseInt(p.getProperty("limit").trim());  
		}
	
		if(p.containsKey("downloadLimit")){  
			downloadLimit = Integer.parseInt(p.getProperty("downloadLimit").trim());  
		}
		
		if(p.containsKey("isDownload")){  
			isDownload = p.getProperty("downloadLimit").trim();  
		}
				
		if(p.containsKey("sql")){  
			sql = p.getProperty("sql").trim();  
		}
	} catch (Exception e) {
	}    		
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>JDBC Viewer</title>

 <!-- Bootstrap 3.3.4 -->
 <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
 <!-- DATA TABLES -->
 <link href="plugins/datatables/dataTables.bootstrap.css" rel="stylesheet" type="text/css" />
 
 <link rel="stylesheet" href="plugins/select2/select2.css" />
 <link rel="stylesheet" href="css/font-awesome.min.css" />
 
</head>
<body style="margin:10px">

<!-- jQuery 2.1.4 -->
<script src="plugins/jQuery/jQuery-2.1.4.min.js"></script>
<!-- Bootstrap 3.3.2 JS -->
<script src="bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
<!-- DATA TABES SCRIPT -->
<script src="plugins/datatables/jquery.dataTables.min.js" type="text/javascript"></script>
<script src="plugins/datatables/dataTables.bootstrap.min.js" type="text/javascript"></script>
<script src="plugins/select2/select2.min.js" type="text/javascript"></script>
<script src="plugins/dialog/lhgdialog.min.js" type="text/javascript"></script>

<h1>JDBC Viewer</h1>
<form id="ajaxFrm" action="${pageContext.request.contextPath}/JdbcViewer" method="post">
 	<input name="settingFile" type="hidden" value="<%=settingFile %>"/>
    <p>
Data Source:
<select name="connSelect">
<%for(int i=0;i<setIniList.size();i++){ %>
	<option value="<%=setIniList.get(i) %>">setting <%=i+1 %></option>
<%} %>
</select>
<span class="input-icon"> <button class="btn btn-info btn-xs" id="add_t1"> <i class="icon-save bigger-110"></i>Add </button> </span>
<span class="input-icon"> <button class="btn btn-info btn-xs" id="edit_t1"> <i class="icon-edit bigger-110"></i>Modify </button> </span>
<span class="input-icon"> <button class="btn btn-info btn-xs" id="delete_t1"> <i class="icon-trash bigger-110"></i>Del </button> </span>

<div id="connDiv" style1="display:none">driverClassName：
      <input name="driverClassName" type="text" value="<%=driverClassName %>" size="50"/>
    <br>
    url：
    <input name="url" type="text" value="<%=url %>" size="50"/>
    <br>
    userName：
    <input type="text" name="userName" size="30" value="<%=userName %>">
    <br>
    password：
    <input type="password" name="password" size="30" value="<%=password %>">
    <p>
      <input type="button" value="Ok" is="ok" onClick="callDoScan();">
  &nbsp;&nbsp;
      <input type="button" value="Cancel" id="cancel" >
  &nbsp;&nbsp;  
      <input type="reset" value="Reset">
    </p>  
</div>
    Show Line Limit:
    <input type="text" name="limit" size="30" value="<%=limit %>">
    &nbsp;&nbsp;    
  <input type="checkbox" name="isDownload" <%if(isDownload!=null){ %>checked="checked"<%} %>>Whether download?
    &nbsp;&nbsp;&nbsp;&nbsp;
    Download Line Limit:
    <input type="text" name="downloadLimit" size="30" value="<%=downloadLimit %>">
   <br>
    SQL：<br>    
      <label>
      <textarea name="sql" cols="130" rows="5"><%=sql %></textarea>
      </label>
   </p>
    <p>
      <input type="button" value="Run" onClick="callDoScan();">
  &nbsp;&nbsp;
      <input type="button" value="Run1" id="test" >
  &nbsp;&nbsp;  
      <input type="reset" value="Reset">
                </p>
 </form>
<div id="processDiv" style="display:none">Please waiting . . . <img src="imgs/loading3.gif"></div>
<div id="resultDiv"></div>
 <script type="text/javascript">
 function callDoScan1(){
   $("#processDiv").hide();
 }
 
$("#test").click(function(){
	$("#processDiv").hide();
	return;
});
  
 function callDoScan(){
   $("#resultDiv").html("");
   $("#processDiv").show();
   window.setTimeout(function(){
		doScan()
	},"0");
 }
 function doScan(){
	$.ajax({
		cache: false,
		type: "POST",
		url:"JdbcViewer",	
		data:$('#ajaxFrm').serialize(),	
		async: true,
		error: function(request) {
			alert("Send Failure!");
			$("#processDiv").hide();
		},
		success: function(data) {
			$("#resultDiv").html(data);	
			$("table").dataTable({
          		"bPaginate": true,
          		"bLengthChange": true,
          		"bFilter": true,
          		"bSort": true,
          		"bInfo": true,
          		"bAutoWidth": true
        	});                                
    		$("#processDiv").hide();
		}
	});	
	//$("#processDiv").hide();
}

//add
$('#add_t1').click(function(){
	var h = $('#connDiv').html();
	var dialog = $.dialog({
		title: "Add", 
		width: 400, 
		height: 300, 
		lock: true, 
		content: h,
		frm:false,
		fixed:true,
		left:"30%",
		top:"20%"
	});
	return false;
});


//modify
$('#edit_t1').click(function(e){
	if(!selected_t1){
		xalert(e,"Hint","No selection!");
		return;
	}
	var dialog = $.dialog({
		title: "Modify",
		width: 800, 
		height: 600, 
		lock: true, 
		content:'url:' + 'edit.html' +'?id='+selected_t1.id,
		frm:false,
		fixed:true,
		left:"30%",
		top:"20%"
	});
});

//Del
$('#delete_t1').click(function(e){
	if(!selected_t1){
		xalert(e,"Hint","No selection!");
		return;
	}
	bootbox.confirm("Do you want to delete this?", function(result) {
		if(result) {
			ajax({
				type:"post",
				url : 'delete.html',
				data: selected_t1,
				success : function(result) {
					reload_t1();
				},
				error : function(result) {
					xalert(e,"Hint",result);
				}
			});
		}
	});
});
</script>
</body>
</html>