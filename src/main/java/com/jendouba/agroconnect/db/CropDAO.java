package com.jendouba.agroconnect.db;

import com.jendouba.agroconnect.core.Crop;
import io.dropwizard.hibernate.AbstractDAO;
import jakarta.persistence.NamedQuery;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class CropDAO extends AbstractDAO<Crop> {
    public CropDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
    public Crop create(Crop crop){
        return persist(crop);
    }
    public Optional<Crop> findById(Long id){
        return Optional.ofNullable(get(id));
    }
    public List<Crop> findAll(){
        return currentSession()
                .createQuery("SELECT c FROM Crop c JOIN FETCH c.farmer", Crop.class)
                .list();
    }

    public List<Crop> findByFarmerId(Long farmerId){
        return currentSession()
                .createQuery("SELECT c FROM Crop c JOIN FETCH c.farmer WHERE c.farmer.user_id = :farmerId", Crop.class)
                .setParameter("farmerId",farmerId)
                .list();

    }
    public Crop update(Crop crop){
        return persist(crop);
    }
    public void delete(Crop crop){
        currentSession().delete(crop);
    }

}
