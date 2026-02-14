package expondo.evolution.planning;

import expondo.evolution.okr.Cycle;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timeboxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Timebox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private Cycle cycle;

    /**
     * Sequential number within the cycle (1, 2, 3, ...)
     */
    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * When closed, teams can no longer submit or edit reports for this timebox.
     */
    @Column(nullable = false)
    private boolean closed = false;

    @OneToMany(mappedBy = "timebox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeboxReport> timeboxReports = new ArrayList<>();
}
