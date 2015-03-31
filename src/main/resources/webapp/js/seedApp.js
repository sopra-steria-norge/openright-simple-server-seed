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


window.Handlebars.registerHelper('select', function( value, options ){
  var $el = $('<select />').html( options.fn(this) );
  $el.find('[value=' + value + ']').attr({'selected':'selected'});
  return $el.html();
});


var orderRepository = {
  get: function(id) {
    return ajax.get('api/orders/' + id);
  },

  list: function() {
    return ajax.get('api/orders');
  },

  save: function(order) {
    return ajax.post('api/orders', order, order.id);
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
    return ajax.post('api/products', product, product.id);
  }
};
