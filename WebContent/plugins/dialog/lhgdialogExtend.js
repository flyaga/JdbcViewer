var DialogExtend={
	edit:null,
	bindEdit:function(edit){
		DialogExtend.edit=edit;
	},
	getApi:function(){
		if (DialogExtend.api==null){
			var api = frameElement.api;
			api._close = api.close;
			api.close = function(){
				DialogExtend.hideProgress(api);
				api._close();
			};
			DialogExtend.api = api;
		}
		return DialogExtend.api;
	}, 
	getOpener:function(){
		return DialogExtend.getApi().opener;
	}, 
	getEdit:function(){
		return DialogExtend.edit;
	},
	getIndex:function(){
		return DialogExtend.getOpener()[DialogExtend.edit.options.bindIndex];
	},
	getForm:function(){
		return $(DialogExtend.edit.options.bindForm);
	},
	showProgress:function(){
		var parent = $(DialogExtend.api.iframe).parent();
		parent.css("display","none");
		parent.parent().append("<span id='loading' style='border:1px solid #ccc;background:#eee;padding:10px 20px;'>数据正在保存...</span>");
	},
	hideProgress:function(){
		var parent = $(DialogExtend.api.iframe).parent();
		parent.css("display","");
		parent.parent().children("#loading").remove();
	},
	reset:function(){
		DialogExtend.hideProgress();
	},
	getSubmitDataBySelector:function(fields){
		var data = {};
		for(var i=0;i<fields.length;i++){
			var field = fields[i];
			var ele = $(field);
			data[ele.attr("name")] = ele.val();
		}
		return data;
	},
	post:function(config){    
		DialogExtend.showProgress(DialogExtend.api);
		
		$.ajax({
			type: "post",
			url: config.url,
			data: config.params, 
			dataType: config.dataType,  
			contentType:config.contentType, 
			success: function(rtn, status){
				if(rtn.status=='200')
					config.success(rtn.data);
				else
					config.error(rtn.errors);
			},   
			error: function(err){  
				config.error(err);
			}
		});
	}, 
	get:function(config){    
		$.ajax({  
		   type: "get",
		   url: config.url,
		   dataType: "json",  
		   data: config.params,    
		   success: function(rtn,status){  
			   if(rtn.status=='200')
					config.success(rtn);
				else
					config.error(rtn.errors); 
		   },   
		   error: function(err){  
			   config.error({content:'操作失败了哦，请检查您的网络链接！'});
		   }  
		});  
	}
};
