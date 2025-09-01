package com.jendouba.agroconnect.db;

import com.jendouba.agroconnect.core.Crop;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;
/**
 * Data Access Object for Crop entity.
 * Provides methods to create, update, delete, and query crops in the database.
 */
public class CropDAO extends AbstractDAO<Crop> {
    public CropDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    //Persist a new crop in the database
    public Crop create(Crop crop){
        return persist(crop);
    }

    //Find a crop by its ID.
    public Optional<Crop> findById(Long id){
        return Optional.ofNullable(get(id));
    }

    //Retrieve all crops with their associated farmers.
    public List<Crop> findAll(){
        return currentSession()
                .createQuery("SELECT c FROM Crop c JOIN FETCH c.farmer", Crop.class)
                .list();
    }

    //Retrieve all crops belonging to a specific farmer.
    public List<Crop> findByFarmerId(Long farmerId){
        return currentSession()
                .createQuery("SELECT c FROM Crop c JOIN FETCH c.farmer f WHERE f.user_id = :farmerId", Crop.class)
                .setParameter("farmerId", farmerId)
                .list();
    }

    //Update an existing crop.
    public Crop update(Crop crop){
        return persist(crop);
    }

    //Delete a crop from the database.
    public void delete(Crop crop){
        currentSession().delete(crop);
    }
}