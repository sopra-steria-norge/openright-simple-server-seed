var ajax = {
  get : function(localUrl) {
    return $.get('/seedapp/' + localUrl);
  },
  post : function(localUrl, object, id) {
    if (id) localUrl += "/" + id;
    return $.ajax({
      url : '/seedapp/' + localUrl,
      type : 'POST',
      data : JSON.stringify(object),
      async : false,
      contentType : "application/json; charset=utf-8"
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

var productRepository = {
  get: function(id) {
    return ajax.get('api/products/' + id);
  },

  list: function() {
    return ajax.get('api/products');
  },

  save: function(product) {
    console.log(product);
    return ajax.post('api/products', product, product.id);
  }
};
