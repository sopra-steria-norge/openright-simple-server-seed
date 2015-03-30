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


var orderRepository = {
  list: function() {
    return ajax.get('api/orders');
  },
  
  save: function(order) {
    return ajax.post('api/orders', order);
  }
};
