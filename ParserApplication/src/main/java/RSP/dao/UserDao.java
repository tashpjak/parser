package RSP.dao;

import RSP.model.User;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;

@Repository
public class UserDao extends AbstractDao<User>
{
    UserDao(EntityManager em)
    {
        super(em);
    }

    @Override
    public User get(int id)
    {
        return em.find(User.class,id);
    }

    @Override
    public List<User> getAll()
    {
        return em.createNamedQuery("User.getAll").getResultList();
    }

    @Override
    public void add(User entity)
    {
        Objects.requireNonNull(entity);
        em.persist(entity);
    }

    @Override
    public User update(User entity)
    {
        Objects.requireNonNull(entity);
        return em.merge(entity);
    }

    @Override
    public void remove(User entity)
    {
        Objects.requireNonNull(entity);
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }
}