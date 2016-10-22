package com.contabilizei;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import com.github.javafaker.Faker;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
* This class consists exclusively of receiving API Calls that operate on or return
* entity collections.  It contains annotations that are identified by the Google Endpoints Engine 
* and can only return Entities as described in the documentation:
* https://cloud.google.com/appengine/docs/java/endpoints/parameter-and-return-types
*
* This class contains endpoints for all the resources in the application: Order, OrderItem, Product, Customer
* The paths were designed to attend the Restful API principle where all the paths are singular and attend
* POST, GET, UPDATE, DELETE operation of the main resource which is Order and order items.
*
*
*
* @author  Thiago Mello
* @see     Api
* @see     ApiMethod
* @see     List
* @see     Faker
* @since   1.0
*/

@Api(name = "orders", version = "v1", description = "An API to manage orders")
public class OrdersAPI {

	static final String ALF = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static SecureRandom rnd = new SecureRandom();

	/**
	 * GET for the Order resource with limit and page parameter to enable pagination.
	 * If requested without these parameters specified or with parameters less or equal to 0, it will return
	 * all resources
	 * @param limit
	 * @param page
	 * @return List<Entity>
	 */
	
	@ApiMethod(httpMethod = ApiMethod.HttpMethod.GET, path = "order")
	public List<Entity> indexOrder(@Nullable @Named("limit") Integer limit, @Nullable @Named("page") Integer page) {
		if (limit == null) {
			limit = 0;
		}
		if (page == null) {
			page = 0;
		}
		return Order.list(limit.intValue(), page.intValue());
	}

	/**
	 * GET for a single result resource
	 * If the resource is not found a generic error response is responded to the client
	 * @param limit
	 * @param page
	 * @return Entity
	 */

	@ApiMethod(httpMethod = ApiMethod.HttpMethod.GET, path = "order/{code}")
	public Entity indexSingleOrder(@Named("code") String code) throws Exception {
		if (code != "") {
			Entity order = Order.findByCode(code);
			if (order != null) {
				return order;
			}
			throw new Exception("Order not found!");
		}
		throw new Exception("Arguments is not valid!");
	}

	/**
	 * POST for the Order resource. The OrderBean is received from the request and
	 * represent the POST payload. The OrderBean is a fundamental part of the API functionality
	 *  since it holds an interface for the request. Once the request is received, a unique random code
	 *  is generated with the codeGenerator method loop and passed to the static Order class to create the 
	 *  Entity/Resource in the database.
	 *  
	 * @param code
	 * @return Entity
	 */

	@ApiMethod(httpMethod = ApiMethod.HttpMethod.POST, path = "order")
	public Entity storeOrder(OrderBean requestData) throws EntityNotFoundException {
		String code = codeGenerator(20);
		do{
			code = codeGenerator(20);
		}while (Order.findByCode(code) != null);
		Entity db_customer = Customer.find(requestData.getCustomer());
		Entity order = Order.create(code, db_customer, requestData.getProducts());
		Customer.addOrder(db_customer, order.getKey());
		return order;
	}

	/**
	 * PUT Endpoint for the Order resource. The order code is received from the request (via query path)
	 * and it's also received a OrderBean which is a representation of the PUT payload. The OrderBean is
	 * fundamental for the functionality of the API since it holds an interface for the request.
	 * Once the request is received, the order Entity is found and updated with the OrderBean data.
	 *  
	 * @param code
	 * @return Entity
	 */
	
	@ApiMethod(httpMethod = ApiMethod.HttpMethod.PUT, path = "order/{code}")
	public Entity updateOrder(@Named("code") String code, OrderBean requestData) throws EntityNotFoundException {
		Entity response = new Entity("Response");
		Entity order = Order.findByCode(code);
		if(order != null)
		{
			boolean result = Order.update(order, requestData.getProducts());
			if(result == true)
			{
				response.setProperty("result", true);
				response.setProperty("message", "Updated successfully!");
				return response;
			}
		}

		response.setProperty("result", false);
		response.setProperty("message", "The requested order was not found in the database!");
		return response;

	}
	
	/**
	 * DELETE for the Order resource. The order code is received from the request then used
	 * to find it's key in the Database and remove the reference to the order in the customer resource
	 *  
	 * @param code
	 * @return Entity
	 */
	
	@ApiMethod(httpMethod = ApiMethod.HttpMethod.DELETE, path = "order/{code}")
	public Entity deleteOrder(@Named("code") String code) throws EntityNotFoundException {
		Entity response = new Entity("Response");
		Entity order = Order.findByCode(code);
		if(order != null)
		{
			Key customer_key = order.getParent();
			Key order_key = order.getKey();
			boolean result = Order.delete(order_key, customer_key);
			if(result == true)
			{
			    //Refresh customer array
				if(Customer.removeOrder(customer_key, order_key) == true)
				{
					response.setProperty("result", true);
					response.setProperty("message", "Deleted successfully!");
					return response;
				}
				response.setProperty("result", false);
				response.setProperty("message", "Oh man! Error when removing the order from the customer!");
				return response;
			}else{
				response.setProperty("result", false);
				response.setProperty("message", "Oh man! Error when deleting the order!");
				return response;				
			}
		}

		response.setProperty("result", false);
		response.setProperty("message", "Woops! It seems the order was not found in the database!");
		return response;

	}
	
	/**
	 * Helper method to generate random order codes based on the length parameter (Not an API Endpoint)
	 * @param length
	 * @return String
	 */
	
	private String codeGenerator(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(ALF.charAt(rnd.nextInt(ALF.length())));
		return sb.toString();
	}

	/**
	 * GET Endpoint for the Product resource with limit and page parameter to enable pagination.
	 * If requested without these parameters specified or with parameters less or equal to 0, it will return
	 * all resources
	 * @param limit
	 * @param page
	 * @return List<Entity>
	 */

	@ApiMethod(httpMethod = ApiMethod.HttpMethod.GET, path = "product")
	public List<Entity> indexProduct(@Nullable @Named("limit") Integer limit, @Nullable @Named("page") Integer page) {
		if (limit == null) {
			limit = 0;
		}
		if (page == null) {
			page = 0;
		}
		return Product.list(limit.intValue(), page.intValue());
	}


	/**
	 * Method to seed fake products (Testing purpose only)
	 * @see Faker
	 * @return List<Entity>
	 */
	
	@ApiMethod(httpMethod = ApiMethod.HttpMethod.GET, path = "seed_products")
	public List<Entity> storeProduct() {
		Faker faker = new Faker();
		List<Entity> l = new ArrayList<Entity>();
		for (int i = 0; i < 20; i++) {
			l.add(Product.create(faker.number().numberBetween(1, 10000), faker.book().title(),
					faker.number().numberBetween(1, 200)));
		}
		return l;
	}

	/**
	 * GET for the Customer resource with limit and page parameter to enable pagination.
	 * If requested without these parameters specified or with parameters less or equal to 0, it will return
	 * all resources
	 * @param limit
	 * @param page
	 * @return List<Entity>
	 */
	
	@ApiMethod(httpMethod = ApiMethod.HttpMethod.GET, path = "customer")
	public List<Entity> indexCustomer(@Nullable @Named("limit") Integer limit, @Nullable @Named("page") Integer page) {
		if (limit == null) {
			limit = 0;
		}
		if (page == null) {
			page = 0;
		}
		return Customer.list(limit.intValue(), page.intValue());
	}

	/**
	 * Method to seed fake customers (Testing purpose only)
	 * @see Faker
	 * @return List<Entity>
	 */
	
	@ApiMethod(httpMethod = ApiMethod.HttpMethod.GET, path = "seed_customers")
	public List<Entity> storeCustomer() {
		Faker faker = new Faker();
		List<Entity> l = new ArrayList<Entity>();
		for (int i = 0; i < 20; i++) {
			String cpf = faker.number().numberBetween(100, 999) + "." + faker.number().numberBetween(100, 999) + "."
					+ faker.number().numberBetween(100, 999) + "-" + faker.number().numberBetween(10, 99);
			l.add(Customer.create(cpf, faker.superhero().name(), faker.phoneNumber().phoneNumber(),
					faker.internet().emailAddress()));
		}
		return l;
	}

}
