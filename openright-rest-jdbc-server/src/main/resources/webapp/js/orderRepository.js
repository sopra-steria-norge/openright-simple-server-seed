var ajax = {
	get: function(localUrl) {
		return $.get('/myapp/' + localUrl);
	},
	post: function(localUrl, object) {
		return $.ajax({
	        url: '/myapp/' + localUrl,
	        type: 'POST',
	        data: JSON.stringify(object),
	        contentType: "application/json; charset=utf-8"
	    });
	}
};

$(document).ajaxError(function( event, jqxhr, settings, thrownError) {
	console.log( event, jqxhr, settings, thrownError);
	if (jqxhr.status >= 500) {
		notify("error", "A terrible error occurred", "We are very sorry and looking into it");
	} else {
		notify("warning", "Problems", thrownError);
	}
});


var orderRepository = {
  list: function() {
    return ajax.get('api/orders');
  },
  
  save: function(order) {
    return ajax.post('api/orders', order);
  }
};
