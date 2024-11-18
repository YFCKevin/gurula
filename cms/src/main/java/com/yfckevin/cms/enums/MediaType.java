package com.yfckevin.cms.enums;

public enum MediaType {
    Text(1, "文字"),
    Audio(2, "語音"),
    Image(3, "圖片"),
    Video(4, "影片");

    private MediaType(){
    }

    private int value;
    private String label;
    private MediaType(int value,String label){
        this.value = value;
        this.label = label;
    }
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

}
