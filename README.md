Sopra Steria reference Java architecture
========================================

A senior developer should master the total technologies to create and run a complete, if simple, application.
The Openright Seed project aims to teach you everything you need to accomplish this.

Th application demonstrates a simple JavaScript + Java servlet + PostgreSQL database application.
It introduces all the technologies needed to build and test all layers of the application. To make it as
easy as possible to understand, we have picked technologies that are as simple as possible, yet suitable
for production use. we avoid "clever" technologies. This includes technologies that rely on reflection or
classpath manipulation as well as front-end technologies that rely on data-binding.
It's quite possible to add such technologies to the reference, but we don't consider it a good starting point
on the path to mastery.

A running version of the code can be examined at http://openright-orders.herokuapp.com


* Any change developers make during development should be reflected in the running application within few seconds
* The application starting point should minimize the number of technologies and especially limit the use of sophisticated technologies
* The application should support the latest version of any involved technologies
* The application should promote cross-tier understanding
* The application should answer deployment considerations

Lessons:
--------

The application can be used to illustrate the following lessons:

* Operations: Create a portable, complete, self-executable Jar-file by use of the shade-maven-plugin
  * This is the technique used by DropWizard
  * A similar effect is achieved differently with Spring Boot
* Operations: Start the application with your own main() method
* Development: JavaScript-to-database tests with WebDriver
* Front-end: URL-driven Single-Page Application (SPA) via window.onhashchange
* Front-end: Rendering AJAX results with Handlebars
* Front-end: Serializing forms as JSON and posting to REST
* Backend: Injecting dependencies manually through a FrontController
* Backend: Parsing JSON with JSON-buddy
* Backend: Developing and testing Repositories
* Operations: Managing migrations with Flyway


![Alt text](http://g.gravizo.com/g?
@startuml;
  actor User;
  cloud {;
     User -> [HTTP];
  };
  package "Application" {;
    %28%29 "Big IP";
    package "Proxy tier" {;
      node "Nginx 1";
      node "Nginx 2";
      HTTP -down- [Big IP];
      [Big IP]  -down- [Nginx 1];
      [Big IP]  -down- [Nginx 2];
    };
    package "Application tier" {;
      node "Jetty 1";
      node "Jetty 2";
      node "Jetty 3";
      [Nginx 1] -- [Jetty 1];
      [Nginx 2] -- [Jetty 1];
      [Nginx 1] -- [Jetty 2];
      [Nginx 2] -- [Jetty 2];
      [Nginx 1] -- [Jetty 3];
      [Nginx 2] -- [Jetty 3];
    };
    package "Data tier" {;
      database "pgsql 1";
      database "pgsql 2";
      [Jetty 1] -- [pgsql 1];
      [Jetty 2] -- [pgsql 1];
      [Jetty 3] -- [pgsql 1];
      [pgsql 1] .right.> [pgsql 2] : replication;
    };
  };
  @enduml
  )

Getting started developing
--------------------------

### Prerequisites

* A git client (for Windows, we recommend [TortoiseGit](https://code.google.com/p/tortoisegit/) and [Msysgit](https://msysgit.github.io/))
* An IDE (we recommend Eclipse or IntelliJ with Infinitest plugin)
* JDK 1.8
* Maven 3
* PostgreSQL
* Recommended: An SQL client (we recommend  [dbeaver](http://dbeaver.jkiss.org/))
* Recommended: An account at Github and at Heroku (free)

### Steps

1. Retrieve the source code from github:  `git clone https://github.com/steria/openright-simple-server-seed.git`
  * For IntelliJ just choose `VCS` > `Checkout from Version Control` > `Github` and enter the repository url.
2. Import the project into your IDE
  * For Eclipse, run `mvn eclipse:eclipse -DdownloadSources` from the command line and do `File` > `Import...` > `Existing project into workspace` in Eclipse
  * This should be pretty much automatic with IntelliJ
3. Create the database schema
  * Log in with the root user on PostgreSQL (locally)
  * `CREATE USER seed WITH PASSWORD 'seed'`
  * `CREATE DATABASE seed OWNER seed`
  * `CREATE USER seed_test WITH PASSWORD 'seed_test'`
  * `CREATE DATABASE seed_test OWNER seed_test`
4. Run the tests
  * In Eclipse, right click on the project and select `Run As` > `JUnit test`
  * In IntelliJ, right click on the project and select `Run 'All tests'`
5. Run the `net.openright.simpleserverseed.SeedAppServer` main class in the debugger
6. Visit the application at http://localhost:3000/
7. You can edit client HTML, JavaScript or CSS under `src/main/resources/webapp`. Any changes will be reflected when you refresh the browser
8. You can edit server side code, for example `net.openright.simpleserverseed.domain.order.OrderApiController `. Any changes that don't cross a method boundary will be reflected when you refresh, as long as you run in the debugger
9. Any changes that are not supported by the debugger will be reflected when you start `net.openright.simpleserverseed.SeedAppServer` again (starting again will shut down the old server instance)
0. To change the database schema, add new migrations under `src/main/resources/db`. The changes will be reflected when you restart the application server

### Configuration (Optional)

The application starts with a working default configuration given you have followed the steps above. Default values are set in SeedAppConfigFile.java. Two configuration files are loaded if you create them.

1. seedapp.properties - This configuration is loaded when you run SeedAppServer.java
2. seedapp-test.properties - This configuration is loaded by the JUnit tests.

Values in the configuration are key=value pairs. The following parameters are examples of configurable parameters:
```
seed.db.username=some_database_user
seed.db.password=a_password_for_the_database_user
```
By default, the database settings use Postgresql on localhost with «seed» as username and password when running the dev server and «seed_test” when running unit tests.

### Deploying to Heroku cloud

1. Clone this project on github.com
2. Sign up for a Heroku account
3. Under the "+" menu in Heroku, select "Create a new app"
4. Under the "Resources" pane, add Postgres as a resource
5. Under the "Deploy" pane, click "Connect to Github"
6. Connect the cloned repository
7. At the bottom of the screen, select Deploy branch
8. Under the "..." overflow menu, select "Open app"

You can now see your app running on the web.

The ingredients that make this happen are:
* `pom.xml` makes Heroku figure out that this is a Maven project and run "mvn install"
* `Procfile`` points out to Heroku the command to run to start the server
* `SeedAppConfigFile.createDataSource` calls `AppConfigFile.createDataSourceFromEnv` which reads the DATABASE_URL
  environment property set by Heroku



Development quick guide
------------------------------

![Component diagram](http://g.gravizo.com/g?
@startuml;
  skinparam componentStyle uml2;
  package "Application" {;
    node Client {;
      [Frame] --> [Product];
      [Frame] --> [Orders];
	    [Orders] --> [jQuery.AJAX];
      package "Product" {;
        [product view 1.html] --> [ProductController.js];
        [product view 2.html] --> [ProductController.js];
        [ProductController.js] --> [jQuery.AJAX];
      };
      package "Orders";
    };
    node Server {;
      [Database.java];
      [jQuery.AJAX] --> [FrontController];
      folder "ProductServer" {;
        [FrontController] --> [ProductController];
        [Product domain];
        [ProductController] --> [ProductRepository];
        [ProductRepository] --> [Database.java];
      };
      package "OrderServer";
      [OrderServer] --> [Database.java];
      [FrontController] --> [OrderServer];
    };
    [Database.java] --> [Database];
    database Database {;
      folder "Products table";
      folder "Orders table";
    };
  };
@enduml;
)

### Adding view

E.g. a new view to list users. See `src/main/resources/webapp/products` as a reference.

1. Add 'Users' as a link to `#users` the top level menu in `src/main/resources/webapp/index.html` in the nav section
2. Add `src/main/resources/webapp/users/index.html`
3. Make `users/index.html` display 'users/list'
4. Add `src/main/resources/webapp/users/_list.html` with the content of the list view

### Adding a JavaScript read view

E.g. a new view to list users. See `src\main\resources\webapp\products\_list.html`as a reference.

1. Create a Mustache template for the user list: `<script type="text/x-handlebars-template" id="usersTemplate">`
2. Load the template `var usersTemplate = Handlebars.compile($("#usersTemplate").html());`
3. When the page is loaded, call
   `ajax.get('api/users').then(function(d) { $("#target").html(usersTemplate(d.users)); }`
4. Linking to specific items

### Adding a JavaScript write view

E.g. a new view to create users. See `src\main\resources\webapp\products\_edit.html`as a reference.

1. The client delegate JavaScript (with promises)
2. Posting data from a form
2. Validation with HTML5
3. Client side validation
4. Handling responses and errors

### Providing a JSON (or XML) endpoint

All requests to `.../api` will be routed to SeedAppFrontServlet.

1. Add the routing to `SeedAppFrontServlet.getControllerForPath`
  * E.g. `case "orders": return new JsonResourceController(new UsersApiController(config));`
2. Create the resource controller as an implementation of `ResourceApi`
   * E.g. `public class UsersApiController implements ResourceApi`
3. Create a repository to encapsulate the database logic
4. Map from the domain/persistent object to JSON in the ResourceApi controller class

### Consuming a JSON or XML endpoint

1. The naming standard for the gateway and the HTTP client
2. Configuring the server address
3. Mapping to and from the network format with JSON
  * Alternative: Mapping with XML
  * Alternative: Mapping with a specialized format (EDIFACT, fixed width records)


### Saving data in the database

1. Creating a repository class
2. Create a new migration file under `src/main/resources/db`. This file must contain DDL


Deploying on a standalone JVM
-------------------------------------
(This alternative is based on the maven-shade-plugin approach. It's possible to use maven-dependency-plugin as well as a script that builds the classpath as well)

(Creating a self-executing _war_ (as opposed to jar) will lead to longer build times, larger deployment files and use of temp-files. If 3rd party app server support is not needed, jar-files are a better option)

1. Run `mvn package`
2. Copy `target/openright-rest-jdbc-server-1.0.0.jar` to the target server.
  * This can be a folder on the local machine. It can even be the `target` directory itself
3. Extract the base configuration `java -jar openright-rest-jdbc-server-1.0.0.jar` on the target directory. This will extract `seedapp.properties`
4. Modify the configuration settings in `seedapp.properties`
5. Start the server `java -jar openright-simple-seed-server-1.0.0.jar`
6. Visit the application at http://localhost:8000/seedapp
7. Visit the status page for the application at http://localhost:8000/status/admin

