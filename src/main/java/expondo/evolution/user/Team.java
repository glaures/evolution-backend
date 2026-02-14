package expondo.evolution.user;

import expondo.evolution.planning.TimeboxReport;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer memberCount;

    /**
     * Hex color code for charts and reports (e.g., "#4A90D9").
     */
    @Column(length = 7)
    private String color;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeboxReport> timeboxReports = new ArrayList<>();
}