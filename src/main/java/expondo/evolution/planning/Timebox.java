package expondo.evolution.planning;

import expondo.evolution.okr.Cycle;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timeboxes")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @OneToMany(mappedBy = "timebox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeboxReport> timeboxReports = new ArrayList<>();
}
