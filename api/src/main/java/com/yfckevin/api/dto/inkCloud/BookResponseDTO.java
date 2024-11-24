package com.yfckevin.api.dto.inkCloud;

import com.yfckevin.common.enums.VisionType;

public class BookResponseDTO {
    private String id;
    private String title;
    private String author;
    private String publisher;
    private VisionType visionType;
    private String sourceNarrationId;
    private String sourceImageId;
    private String sourceCoverName;
    private String sourceAudioId;
    private String sourceVideoId;
    private String creationDate;
    private String creator;
    private String memberId;
    private String modificationDate;
    private String modifier;
    private String deletionDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public VisionType getVisionType() {
        return visionType;
    }

    public void setVisionType(VisionType visionType) {
        this.visionType = visionType;
    }

    public String getSourceNarrationId() {
        return sourceNarrationId;
    }

    public void setSourceNarrationId(String sourceNarrationId) {
        this.sourceNarrationId = sourceNarrationId;
    }

    public String getSourceImageId() {
        return sourceImageId;
    }

    public void setSourceImageId(String sourceImageId) {
        this.sourceImageId = sourceImageId;
    }

    public String getSourceAudioId() {
        return sourceAudioId;
    }

    public void setSourceAudioId(String sourceAudioId) {
        this.sourceAudioId = sourceAudioId;
    }

    public String getSourceVideoId() {
        return sourceVideoId;
    }

    public void setSourceVideoId(String sourceVideoId) {
        this.sourceVideoId = sourceVideoId;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(String deletionDate) {
        this.deletionDate = deletionDate;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getSourceCoverName() {
        return sourceCoverName;
    }

    public void setSourceCoverName(String sourceCoverName) {
        this.sourceCoverName = sourceCoverName;
    }
}
