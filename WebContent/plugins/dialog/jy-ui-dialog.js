var Dialog = {
	tips:function(config){
		var clz = "success";
		if(config.type&&config.type==2)
			clz = "error";
		var ele = $("<div class='ui-confirm "+clz+"'><div>");
		if(config&&config.message){
			ele.html(config.message);
		}
		var close = $("<div class='close'>х</div>");
		close.on("click",function(){
			ele.remove();
		});
		ele.append(close);
		ele.append("<span class='icon'></span>");
		ele.appendTo(document.body);
		ele.css({left:($(document.body).width()-ele.outerWidth())/2});
		
		setTimeout(function () {
			ele.remove();
		}, 3000);
	}
};