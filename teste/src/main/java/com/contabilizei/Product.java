package com.contabilizei;

import java.util.List;

import com.github.javafaker.Faker;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;

/**
* This class consists of  creating static functions for the Product resources in which the OrderAPI class
* can use without any prejudice and making the code isolated and cleaner. 
* This class has a DatastoreService statically declared to be used whenever it is needed.
*
*
* @author  Thiago Mello
* @see     List
* @see     Faker
* @since   1.0
*/

public class Product {

	/**
	 * Retrieve current Datastoreservice
	 */
	
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	/**
	 * Static method to retrieve all the Products within a given limit and page. If not specified, it will
	 * retrieve all the resources entities.
	 * @param limit
	 * @param page
	 * @return List<Entity>
	 */
	
	public static List<Entity> list(int limit, int page) {
		Query q = new Query("Product").addSort("code", Query.SortDirection.ASCENDING);
		if (limit <= 0) {
			return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		} else {
			return datastore.prepare(q).asList(FetchOptions.Builder.withOffset(page * limit).limit(limit));
		}
	}

	/**
	 * Static method to insert a new Entity into the Product resource. 
	 * This should be fresh - and changes on it will not impact in the OrderItems value (besides the name)
	 * @param code
	 * @param description
	 * @param unit_value
	 * @return Entity
	 */
	
	public static Entity create(int code, String description, int unit_value) {
		Entity product = new Entity("Product");
		product.setProperty("code", String.valueOf(code));
		product.setProperty("description", description);
		product.setProperty("unit_value", unit_value);
		datastore.put(product);
		return product;
	}
}
