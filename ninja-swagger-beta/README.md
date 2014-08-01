# Ninja-Swagger

The Ninja Swagger module generates Swagger 1.2 compliant documentation from properly annotated
Ninja controllers.  The generated documents are served as JSON or XML for consumption by Swagger
clients.  They are also viewable using the rich Swagger-UI.

## Getting started

### Install the `SwaggerModule` in your `conf.Module` class.

    @Singleton
    public class Module extends AbstractModule {

      @Override
      protected void configure() {

        install(new SwaggerModule());

      }
    }


### Initialize the SwaggerRouter in your `conf.Routes` class.

    @Inject
    SwaggerRouter swaggerRouter;
    
    @Override
    public void init(Router router) {
    
        swaggerRouter.setPath("/api");
        swaggerRouter.init(router);
        
    }

### Config settings

The Swagger service needs to know the base url for your deployment. It builds this from the
`application.server.name` and the `ninja.context` settings.

    application.server.name = http://localhost:8080

## Documenting

Now you are ready to start annotating your controllers.

1. Start by sprinkling `@Api` on  one of your controllers.  You need to at least annotate the controller `Class` and one method.
2. Start your App and browse to `/api`, or whichever route your declared for the `SwaggerRouter`.
3. Start annotating everything else and refreshing your browser.

## Maintaining Annotations

If you find that the `ninja.api` annotations are cluttering your controllers, create an interface for each controller and move **all** of the `ninja.api` annotations to these interfaces.  This will keep your controller implementations clean.

## Overriding Swagger UI

Copy `views/swagger/api-docs.ftl.html` into your own application to the same location and modify it as needed.