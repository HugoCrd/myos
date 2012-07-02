var eb = new vertx.EventBus("http://localhost:8080/eventbus");


$(document).ready(function () {
	startLoadingDot();
	sessionid = "0dd48b7c-5c47-4c6f-b219-83263d80f67c";
	eb.onopen = function() {
		eb.send('vertx.excilys.myos', {"action":"recipe","sessionid": sessionid}, function(reply) {
			stopLoadingDot();
		    if (reply.status === 'ok') {
		    	console.log(reply);
		  		stopLoadingDot();
		  		feedRecipe(reply.results[0].myos);
		    } else {
		      console.error("Unable to find document");
		    }
		});
		
		eb.send('vertx.excilys.myos', {"action":"availableIngredients", "sessionid": sessionid}, function(reply) {
			stopLoadingDot();
		    if (reply.status === 'ok') {
		    	console.log(reply);
		  		stopLoadingDot();
		  		feedSliders(reply.results[0].recipe);
		    } else {
		      console.error("Unable to find document");
		    }
		});
	}
});


var startLoadingDot = function(element){
	$('#loadingDotContainer').empty().append("<li></li>");
	setInterval(function(){
		$('#loadingDotContainer li').fadeIn(500);
		$('#loadingDotContainer li').fadeOut(500);
	},1000);
}

var stopLoadingDot = function(){
	$('#loadingDotContainer').empty();
}

var feedSliders = function(ingredients){
	var items = [];
	$.each(ingredients, function(key, val) {
		var row = "";
		row += "<div class=\"row\">";
		row += "<label for=\""+key+"\">"+ucfirst(key)+"</label>";
		row += "<div class=\"slider\" id=\""+key+"\"></div>";
		row += "</div class=\"row\">";
		items.push(row);
	});
	$('#ingredientChooser').empty().append("<h3>Pick what you want</h3>").append(items.join(''));
	initSliders(ingredients);
}

var feedRecipe = function(ingredients){
	var ingredientsTab = [];
	$.each(ingredients, function(name, quantity) {
		if(parseInt(quantity)>0){
			var row = "";
			row += "<div class=\"row\">";
			row += quantity;
			row += " "+name;
			row += "</div>";
			ingredientsTab.push(row);
		}
	});
	$('#recipeTable').empty().append("<h3>What's in your soup ?</h3>").append(ingredientsTab.join(''));
}

var initSliders = function(ingredients){
	
	$.each(ingredients, function(key, val) {
		$("#"+key).slider({
			range: "min",
			min: 0,
			max: 20,
			value: parseInt(val),
			stop:function (event, ui) {
				startLoadingDot();
				var sessionid = "0dd48b7c-5c47-4c6f-b219-83263d80f67c";
				var name = $(this).attr("id");
				console.log(name);
				eb.send('vertx.excilys.myos', {"action":"save", "sessionid": sessionid, "ingredient": name, "quantity": ui.value}, function(reply) {
					stopLoadingDot();
				    if (reply.status === 'ok') {
				  		stopLoadingDot();
				  		console.log(reply);
				  		feedRecipe(reply.results[0].myos);
				    } else {
				      console.error("Unable to find document");
				    }
				  });
			}
		});
	});
}

var ucfirst = function(str){
      str=str.toLowerCase();
      return str.replace(/(\b)([a-zA-Z])/,
               function(firstLetter){
                  return   firstLetter.toUpperCase();
               }
      );
 }
