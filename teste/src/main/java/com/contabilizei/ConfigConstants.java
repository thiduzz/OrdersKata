package com.contabilizei;

import java.util.List;

import com.github.javafaker.Faker;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

/**
* Constants helper class, here you can add the scope if requesting OAuth authentication 
* (in this case we are not using OAuth but just to save the troube in the API Explorer it is still being set
* as a reminder we are setting the userinfo.email)
*
* @author  Thiago Mello
* @see     Api
* @see     ApiMethod
* @see     List
* @see     Faker
* @since   1.0
*/

public class ConfigConstants {
	public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
	public static final String CLIENT_ID = "374325422132-23td9ormaj07tgpv39pse5j0qdcnff1g.apps.googleusercontent.com";
}
