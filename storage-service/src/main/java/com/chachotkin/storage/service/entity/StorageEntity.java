package com.chachotkin.storage.service.entity;

import com.chachotkin.storage.service.dto.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "storages")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StorageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private StorageType type;

    @Column(nullable = false)
    private String bucket;

    @Column(nullable = false)
    private String path;
}
