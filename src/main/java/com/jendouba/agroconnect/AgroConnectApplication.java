package com.jendouba.agroconnect;
import com.jendouba.agroconnect.auth.JwtAuthFilter;
import com.jendouba.agroconnect.auth.JwtAuthenticator;
import com.jendouba.agroconnect.auth.OptionsResponseFilter;
import com.jendouba.agroconnect.core.Crop;
import com.jendouba.agroconnect.core.Message;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.CropDAO;
import com.jendouba.agroconnect.db.MessageDAO;
import com.jendouba.agroconnect.db.UserDAO;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import com.jendouba.agroconnect.resources.*;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.hibernate.HibernateBundle;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.util.EnumSet;


/**
 * Main Dropwizard application class for AgroConnect.
 * Configures Hibernate, authentication, resources, CORS, and multipart support.
 */
public class AgroConnectApplication extends Application<AgroConnectConfig> {

    /**
     * Hibernate bundle to manage User, Crop, and Message entities.
     */
    private final HibernateBundle<AgroConnectConfig> hibernate =new HibernateBundle<AgroConnectConfig>(User.class, Crop.class, Message.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(AgroConnectConfig agroConnectConfig) {
            return agroConnectConfig.getDataSourceFactory();
        }
    };

    public static void main(String[] args) throws Exception {
        new AgroConnectApplication().run(args);
    }

    /**
     * Initialize Dropwizard bundles: Hibernate + MultiPart (for file uploads)
     */
    @Override
    public void initialize(Bootstrap<AgroConnectConfig> bootstrap)
    {

        bootstrap.addBundle(hibernate);
        bootstrap.addBundle( new MultiPartBundle());
    }

    /**
     * Configure application environment: register resources, authentication, CORS, static files, etc.
     */
    @Override
    public void run(AgroConnectConfig agroConnectConfig, Environment environment) throws Exception {

        // Enable Jersey multipart support
        environment.jersey().register(MultiPartFeature.class);

        // Initialize DAOs
        UserDAO userDAO = new UserDAO(hibernate.getSessionFactory());
        CropDAO cropDAO = new CropDAO(hibernate.getSessionFactory());
        MessageDAO messageDAO=new MessageDAO(hibernate.getSessionFactory());

       // Register WADL (for API documentation) and OPTIONS handler globally
        environment.jersey().register(new org.glassfish.jersey.server.wadl.internal.WadlResource());
        // This will respond 200 OK to all OPTIONS requests
        environment.jersey().register(new OptionsResponseFilter());

        // Initialize JWT authenticator with Hibernate session
        JwtAuthenticator auth = new UnitOfWorkAwareProxyFactory(hibernate)
                .create(JwtAuthenticator.class, UserDAO.class, userDAO);


        // Register all API resources
        environment.jersey().register(new FarmResource(userDAO,cropDAO));
        environment.jersey().register(new AuthResource(userDAO));
        environment.jersey().register(new UserResource(userDAO));
        environment.jersey().register(new CropResource(cropDAO, userDAO));
        environment.jersey().register(new WeatherResource());
        environment.jersey().register(new MessageResource(messageDAO, userDAO));




        // Disable WADL generation
        environment.jersey().property("jersey.config.server.wadl.disableWadl", true);

        // Serve the uploads folder as static
        environment.jersey().register(new org.glassfish.jersey.server.mvc.Viewable("/uploads/"));

        // Register JWT auth filter
        environment.jersey().register(new AuthDynamicFeature(
                new JwtAuthFilter.Builder<User>()
                        .setAuthenticator(auth)
                        .setPrefix("Bearer")
                        .buildAuthFilter()
        ));

        // Support for @RolesAllowed annotations
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        // Bind authenticated user to method parameters
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));

        // Serve static files from the "uploads" folder
        environment.servlets()
                .addServlet("static-file-servlet", new DefaultServlet())
                .addMapping("/uploads/*");

        // Configure CORS to allow React frontend requests
        var cors = environment.servlets().addFilter("CORS", org.eclipse.jetty.servlets.CrossOriginFilter.class);

        cors.addMappingForUrlPatterns(EnumSet.allOf(jakarta.servlet.DispatcherType.class), true, "/*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization,Content-Disposition");
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
        cors.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, "true");
    }

}
