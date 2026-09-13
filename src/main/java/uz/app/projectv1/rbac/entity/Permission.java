package uz.app.projectv1.rbac.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import uz.app.projectv1.common.entity.BaseEntity;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@SQLDelete(sql = "UPDATE permissions SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String name;

    @Column(length = 255)
    private String description;

    @Override
    public String toString() {
        return name;
    }
}
