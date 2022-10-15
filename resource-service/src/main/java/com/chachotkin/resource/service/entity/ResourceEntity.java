package com.chachotkin.resource.service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false)
    private String sourcePath;

    @Column(nullable = false)
    private String checksum;

    @Column(nullable = false)
    private Long size;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
