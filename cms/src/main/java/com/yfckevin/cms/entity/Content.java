package com.yfckevin.cms.entity;

import com.yfckevin.cms.enums.MediaType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "content")
public class Content {
    @Id
    private String id;
    private String title;
    private String path;
    private String memberId;
    private MediaType mediaType;
    private String creationDate;
    private String creator;
    private String modificationDate;
    private String modifier;
    private String deletionDate;

    private String text;    //影片旁白narration
    private long size;      //語音和影片的檔案大小

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Content{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", memberId='" + memberId + '\'' +
                ", mediaType=" + mediaType +
                ", creationDate='" + creationDate + '\'' +
                ", creator='" + creator + '\'' +
                ", modificationDate='" + modificationDate + '\'' +
                ", modifier='" + modifier + '\'' +
                ", deletionDate='" + deletionDate + '\'' +
                ", text='" + text + '\'' +
                ", size=" + size +
                '}';
    }
}
