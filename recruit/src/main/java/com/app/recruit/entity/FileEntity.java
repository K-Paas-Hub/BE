package com.app.recruit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileUUID;
    private String filePath;
    private String fileType;
    private String fileOriginalName;
    private String fileStatus;

    @Builder
    public FileEntity(String fileUUID, String filePath, String fileType,
                      String fileOriginalName, String fileStatus) {
        this.fileUUID = fileUUID;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileOriginalName = fileOriginalName;
        this.fileStatus = fileStatus;
    }
}
