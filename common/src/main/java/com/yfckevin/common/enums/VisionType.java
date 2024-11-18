package com.yfckevin.common.enums;

public enum VisionType {
    Public(1, "公開"),
    Private(2, "私有");

    private VisionType(){
    }

    private int value;
    private String label;
    private VisionType(int value,String label){
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
