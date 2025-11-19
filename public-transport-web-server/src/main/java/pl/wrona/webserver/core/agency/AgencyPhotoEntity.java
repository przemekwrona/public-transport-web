package pl.wrona.webserver.core.agency;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "agency_photo")
@NoArgsConstructor
@AllArgsConstructor
public class AgencyPhotoEntity {

    @Id
    @Column(name = "agency_photo_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "agency_photo_id_seq")
    @SequenceGenerator(name = "agency_photo_id_seq", sequenceName = "agency_photo_id_seq", allocationSize = 1)
    private Long agencyPhotoId;

    @Lob
    @Column(name = "photo")
    @Basic(fetch = FetchType.EAGER)
    private byte[] photo;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "content_size_kb")
    private long contentSizeKb;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "agency_id", referencedColumnName = "agency_id")
    private AgencyEntity agency;

}
