angular.module('contabilizeiApp', ['ngRoute','ngResource'])

.controller('AppStatusController', function($scope, $http, $rootScope) {
  var appStatus = this;
  appStatus.count_customers = 0;
  appStatus.count_orders = 0;
  appStatus.count_products = 0;
  $rootScope.$on("RefreshCountCustomers", function(event, count){
      appStatus.count_customers = count;
  });
  $rootScope.$on("RefreshCountOrders", function(event, count){
      appStatus.count_orders = count;
  });
  $rootScope.$on("RefreshCountProducts", function(event, count){
      appStatus.count_products = count;
  });
})
.controller('OrderCreationController', function($scope, $http, $rootScope) {

	var creationForm = this;
  creationForm.cart = [];
  creationForm.customer = null;
  creationForm.products = [];
  creationForm.customers = [];
  creationForm.selected_customer = -1;
  creationForm.submitting = false;

  $http.get('https://contabilizei-146018.appspot.com/_ah/api/orders/v1/product').
  success(function(data, status, headers, config) {
    creationForm.products = data.items;
    $rootScope.$emit("RefreshCountProducts", data.items.length);
  }).
  error(function(data, status, headers, config) {
      console.log("error loading products!");
 });
  $http.get('https://contabilizei-146018.appspot.com/_ah/api/orders/v1/customer').
  success(function(data, status, headers, config) {
    creationForm.customers = data.items;

    $rootScope.$emit("RefreshCountCustomers", data.items.length);
  }).
  error(function(data, status, headers, config) {
      console.log("error loading customers!");
  });
  creationForm.storeOrder = function() {
    	//creationForm.todos.push({text:todoList.todoText, done:false});
      var dataObj = {
        key: 0,
        customer : creationForm.customer.key.id,
        products : JSON.stringify(creationForm.cart),
      };

      creationForm.submitting = true;  
      var res = $http.post('https://contabilizei-146018.appspot.com/_ah/api/orders/v1/order', dataObj, {
        headers: { 'Content-Type': 'application/json'}
      });
      res.success(function(data, status, headers, config) {
        swal("Order created!", "You successfully added a new order!", "success");

          $rootScope.$emit("RefreshOrders", {});
          creationForm.cart = [];
          creationForm.selected_customer = -1;
          creationForm.customer = null;
          creationForm.submitting = false; 
      });
      res.error(function(data, status, headers, config) {
          creationForm.submitting = false; 
          swal("Request failed!", JSON.stringify({data: data.error.message}), "error");
      }); 
    };

    creationForm.validateCart = function(){
      var flag = false;
      if(creationForm.customer == null || creationForm.cart.length <= 0)
      {
        return flag;
      }
      angular.forEach(creationForm.cart, function(product) {
        if(product.qty > 0)
        {
          flag = true;       
        }
      });
      return flag;
    }

    creationForm.addToCart = function(product) {
      var flag = false;
      angular.forEach(creationForm.cart, function(cart_item) {
        if(product.key.id == cart_item.product.key.id)
        {
          flag  = true;         
        }
      });
      if(flag == false)
      {
        creationForm.cart.push({product:product, qty:1});
      }
    };

    creationForm.removeFromCart = function(index)
    {
      creationForm.cart.splice(index, 1);     
    };

    creationForm.selectCustomer = function()
    {
      if(creationForm.selected_customer >= 0)
      {
        creationForm.customer = creationForm.customers[creationForm.selected_customer];        
      }else{
        creationForm.customer = null;
      }
    };

    creationForm.cart_total = function() {
      var total_value = 0;
      angular.forEach(creationForm.cart, function(product) {
       if(product.qty >= 0)
       {
         total_value += product.product.properties.unit_value * product.qty;    		  
       }
     });
      return total_value;
    };


  }).controller('OrdersController', function($scope, $http, $rootScope) {

    var currentOrders = this;
    currentOrders.orders = [];
    currentOrders.submitting = false;
    currentOrders.selected_order = null;



    currentOrders.loadOrders = function()
    {      
      $http.get('https://contabilizei-146018.appspot.com/_ah/api/orders/v1/order').
      success(function(data, status, headers, config) {
        currentOrders.orders = data.items;
        $rootScope.$emit("RefreshCountOrders", data.items.length);

        angular.forEach(currentOrders.orders, function(order, index) {
          currentOrders.orders[index].properties.customer = angular.fromJson(order.properties.customer);
          currentOrders.orders[index].properties.order_items = angular.fromJson(order.properties.order_items);
          angular.forEach(currentOrders.orders[index].properties.order_items, function(item, index2) {
            currentOrders.orders[index].properties.order_items[index2].product = angular.fromJson(item.product[0]);
            currentOrders.orders[index].properties.order_items[index2].qty = item.qty[0];
            currentOrders.orders[index].properties.order_items[index2].unit_value = item.unit_value[0];
            currentOrders.orders[index].properties.order_items[index2].key = item.key[0];
          });
        });
      }).
      error(function(data, status, headers, config) {
        console.log("error loading orders!");
      });
    }

    $rootScope.$on("RefreshOrders", function(){
      currentOrders.loadOrders();
    });

    currentOrders.loadOrders();

    currentOrders.validateProducts = function(){
      var flag = false;
      if(currentOrders.selected_order == null)
      {
        return flag;
      }
      angular.forEach(currentOrders.selected_order.products, function(product) {
        if(product.qty > 0)
        {
          flag = true;       
        }
      });
      return flag;
    }

    currentOrders.updateOrder = function(order)
    {
  
      var dataObj = {
        key: 0,
        customer : 0,
        products : JSON.stringify(order.properties.order_items),
      };

      currentOrders.submitting = true;  
      var res = $http.put('https://contabilizei-146018.appspot.com/_ah/api/orders/v1/order/'+order.properties.code, dataObj, {
        headers: { 'Content-Type': 'application/json'}
      });
      res.success(function(data, status, headers, config) {
        currentOrders.submitting = false;
        if(data.properties.result == true)
        {
          swal("Order updated!", "You successfully updated the order " +order.properties.code+ "!", "success");
          currentOrders.loadOrders();  
        }else{
          swal("Update failed!", JSON.stringify({data: data.properties.message}), "error");
        }
      });
      res.error(function(data, status, headers, config) {
          currentOrders.submitting = false;
          swal("Update failed!", JSON.stringify({data: data.error.message}), "error");
      }); 
    }

    currentOrders.deleteOrder = function(order)
    {
  
      var res = $http.delete('https://contabilizei-146018.appspot.com/_ah/api/orders/v1/order/'+order.properties.code, null, {
        headers: { 'Content-Type': 'application/json'}
      });
      res.success(function(data, status, headers, config) {
        if(data.properties.result == true)
        {
          swal("Order deleted!", "You successfully deleted the order " +order.properties.code+ "!", "success");
          currentOrders.loadOrders();
        }else{
          swal("Deletion failed!", JSON.stringify({data: data.properties.message}), "error");
        }
      });
      res.error(function(data, status, headers, config) {
          swal("Deletion failed!", JSON.stringify({data: data.error.message}), "error");
      }); 
    }

    currentOrders.cart_total = function() {
      var total_value = 0;
      if(currentOrders.selected_order  != null)
      {
         angular.forEach(currentOrders.selected_order.properties.order_items, function(product) {
         if(product.qty >= 0)
         {
           total_value += product.product.properties.unit_value * product.qty;          
         }
        });
      }
      return total_value;
    };

    currentOrders.selectOrder = function(order)
    {
        currentOrders.selected_order = angular.copy(order);
    };

  });