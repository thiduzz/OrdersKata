package com.contabilizei;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class Customer {

	/**
	 * Retrieve current Datastoreservice
	 */
	
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	/**
	 * Static method to find a unique Customer resource by it's Key ID
	 * @param code
	 * @return Entity
	 */
	
	public static Entity find(long customer) throws EntityNotFoundException
	{
		Key k = KeyFactory.createKey("Customer", customer);
		return datastore.get(k);
	}
	
	/**
	 * Static method to retrieve all the Customers within a given limit and page. If not specified, it will
	 * retrieve all the resources entities.
	 * @param limit
	 * @param page
	 * @return List<Entity>
	 */
	
	public static List<Entity> list(int limit, int page)
	{
		Query q = new Query("Customer").addSort("name", Query.SortDirection.ASCENDING);

		if(limit <= 0)
		{
			return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		}else{
			return datastore.prepare(q).asList(FetchOptions.Builder.withOffset(page*limit).limit(limit));
		}
	}
	
	/**
	 * Static method to insert a new Entity into the Customer resource. 
	 * A property called orders is also created to insert the Customers orders in a easier way 
	 * @param identification
	 * @param name
	 * @param phone
	 * @param name
	 * @return Entity
	 */
	
	public static Entity create(String identification, String name, String phone, String email)
	{
		Entity customer = new Entity("Customer");
		customer.setProperty("identification", identification);
		customer.setProperty("name",name);
		customer.setProperty("phone", phone);
		customer.setProperty("email", email);
		customer.setIndexedProperty("orders", new ArrayList<Key>());		
		datastore.put(customer);
		return customer;
	}
	
	/**
	 * Static method to add a new order to the Customer property "orders". 
	 * @param customer
	 * @param order
	 * @return Entity
	 */
	
	public static Entity addOrder(Entity customer, Key order) throws EntityNotFoundException
	{
		@SuppressWarnings("unchecked") 
		ArrayList<Key> orders = (ArrayList<Key>) customer.getProperty("orders");
		if(orders == null)
		{
			orders = new ArrayList<Key>();
		}
		orders.add(order);
		customer.setIndexedProperty("orders", orders);
		datastore.put(customer);
		return customer;
	}
	
	/**
	 * Static method to remove a deleted order from the Customer property "orders". 
	 * @param customer_key
	 * @param order
	 * @return boolean
	 */
	
	public static boolean removeOrder(Key customer_key, Key order)
	{
		Entity customer;
		try {
			customer = datastore.get(customer_key);
			@SuppressWarnings("unchecked") 
			ArrayList<Key> orders = (ArrayList<Key>) customer.getProperty("orders");
			orders.remove(order);
			customer.setIndexedProperty("orders", orders);
			datastore.put(customer);
			return true;
		} catch (EntityNotFoundException e) {
			return false;
		}
	}
}
