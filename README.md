
##Implementation Details

- JavaEE Web application using the Google App Engine Java (with Jetty)
- Database with NOSQL Google Datastore
- RESTFul API with Google Endpoints
- Maven and Bower
- jQuery, AngularJS 1.5.8 and Twitter Bootstrap.

##Live Demo

- [See Live Demo](http://recontabilizei-146811.appspot.com/)

##Step-by-step

- 1) Install JDK (1.8), Eclipse Neon.1 Release (4.6.1), Maven e Bower
- 2) Clone this repository on any folder of your computer
- 3) Browser into the folder "/teste" of your project and install the maven packages with mvn clean install -U
- 4) Force the path refresh in Eclipse with mvn eclipse:eclipse
- 5) Run bower install to download the front-end packages
- 6) Execute mvn appengine:devserver_start (or mvn appengine:devserver to log)
- 7) Open your browser and access locally (probably http://localhost:8080 or http://localhost:8888)
- 8) You are ready! Or you can access throught this link [Demo](http://recontabilizei-146811.appspot.com/)

