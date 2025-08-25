package com.jendouba.agroconnect;
import com.jendouba.agroconnect.auth.JwtAuthFilter;
import com.jendouba.agroconnect.auth.JwtAuthenticator;
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

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.util.EnumSet;

public class AgroConnectApplication extends Application<AgroConnectConfig> {

    private final HibernateBundle<AgroConnectConfig> hibernate =new HibernateBundle<AgroConnectConfig>(User.class, Crop.class, Message.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(AgroConnectConfig agroConnectConfig) {
            return agroConnectConfig.getDataSourceFactory();
        }
    };

    public static void main(String[] args) throws Exception {
        new AgroConnectApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<AgroConnectConfig> bootstrap)
    {

        bootstrap.addBundle(hibernate);
        bootstrap.addBundle( new MultiPartBundle());
    }

    @Override
    public void run(AgroConnectConfig agroConnectConfig, Environment environment) throws Exception {
        environment.jersey().register(MultiPartFeature.class);
        UserDAO userDAO = new UserDAO(hibernate.getSessionFactory());
        CropDAO cropDAO = new CropDAO(hibernate.getSessionFactory());
        MessageDAO messageDAO=new MessageDAO(hibernate.getSessionFactory());


        JwtAuthenticator auth = new UnitOfWorkAwareProxyFactory(hibernate)
                .create(JwtAuthenticator.class, UserDAO.class, userDAO);



        environment.jersey().register(new FarmResource(userDAO,cropDAO));
        environment.jersey().register(new AuthResource(userDAO));
        environment.jersey().register(new UserResource(userDAO));
        environment.jersey().register(new CropResource(cropDAO, userDAO));
        environment.jersey().register(new WeatherResource());
        environment.jersey().register(new MessageResource(messageDAO, userDAO));


        environment.jersey().register(new AuthDynamicFeature(
                new JwtAuthFilter.Builder<User>()
                        .setAuthenticator(auth)
                        .setPrefix("Bearer")
                        .buildAuthFilter()
        ));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));


        // Config CORS to authorize frontend React
        var cors = environment.servlets().addFilter("CORS", org.eclipse.jetty.servlets.CrossOriginFilter.class);

        cors.addMappingForUrlPatterns(EnumSet.allOf(jakarta.servlet.DispatcherType.class), true, "/*");
        cors.setInitParameter("allowedOrigins", "http://localhost:5173");
        cors.setInitParameter("allowedMethods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin,Authorization,Content-Disposition");
        cors.setInitParameter("allowCredentials", "true");
        cors.setInitParameter("chainPreflight", "false"); // Important pour les requêtes preflight
    }

}
