package RSP.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "trip_table")
@Entity
@NamedQueries({
        @NamedQuery(name = "Trip.getAll",query = "SELECT c FROM Trip c"),
        @NamedQuery(
                name = "Trip.getByName",
                query = "SELECT c FROM Trip c WHERE c.Name = :Name")
})

public class Trip extends  AbstractEntity{
    @Column
    private String location;

    @Column
    private String Name;

    @Column
    private Date startDate;
    @Column
    private Date endDate;
    @ManyToMany
    private List<User> users;

    @ManyToMany
    private List<Tag> tags;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<User> getUsers() {
        if(users == null){
            setUsers(new ArrayList<>());
        }
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Tag> getTags() {
        if(tags == null){
            setTags(new ArrayList<>());
        }
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}